package com.lagradost.cloudstream3.plugins

import android.content.Context
import com.lagradost.cloudstream3.CloudStreamApp.Companion.getKey
import com.lagradost.cloudstream3.CloudStreamApp.Companion.setKey
import com.lagradost.cloudstream3.ui.settings.extensions.RepositoryData
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Host-owned CloudStream repository registry.
 *
 * CloudStream plugins use this API to register repositories dynamically (for example
 * MegaProvider -> cs-repos). The previous compatibility stub returned an empty list
 * and discarded addRepository(), making dynamic repository registration impossible.
 */
object RepositoryManager {
    private const val REPOSITORIES_KEY = "CLOUDSTREAM_REPOSITORIES"
    private val repoLock = Mutex()
    private val json = Json { ignoreUnknownKeys = true }

    val PREBUILT_REPOSITORIES: Array<RepositoryData> = emptyArray()

    @Serializable
    data class RepositoryManifest(
        @SerialName("iconUrl") val iconUrl: String? = null,
        @SerialName("name") val name: String = "",
        @SerialName("description") val description: String? = null,
        @SerialName("manifestVersion") val manifestVersion: Int = 1,
        @SerialName("pluginLists") val pluginLists: List<String> = emptyList(),
    )

    fun getRepositories(): Array<RepositoryData> =
        getKey<Array<RepositoryData>>(REPOSITORIES_KEY) ?: emptyArray()

    /**
     * Parse a standard CloudStream repo.json. Plugins use this to resolve a repository's
     * display metadata before registering it with addRepository().
     */
    suspend fun parseRepository(url: String): RepositoryManifest? = withContext(Dispatchers.IO) {
        runCatching {
            val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = 15_000
                readTimeout = 15_000
                instanceFollowRedirects = true
                requestMethod = "GET"
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", "MewMobile-CloudStream")
            }
            connection.use {
                require(it.responseCode in 200..299) { "HTTP ${it.responseCode}" }
                it.inputStream.bufferedReader().use { reader ->
                    json.decodeFromString<RepositoryManifest>(reader.readText())
                }
            }
        }.getOrNull()
    }

    /**
     * Persist a dynamically registered repository. This intentionally owns only the
     * CloudStream-compatible registry; Mew's UI remains the source of truth for its
     * own repository/plugin state.
     */
    suspend fun addRepository(repository: RepositoryData) {
        repoLock.withLock {
            val current = getRepositories().toList()
            if (current.any { it.url == repository.url }) return
            setKey(REPOSITORIES_KEY, (current + repository).toTypedArray())
        }
    }

    suspend fun removeRepository(context: Context, repository: RepositoryData) {
        repoLock.withLock {
            val current = getRepositories()
            setKey(
                REPOSITORIES_KEY,
                current.filterNot { it.url == repository.url },
            )
        }
    }
}
