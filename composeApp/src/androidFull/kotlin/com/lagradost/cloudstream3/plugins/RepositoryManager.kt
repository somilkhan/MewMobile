package com.lagradost.cloudstream3.plugins

import android.content.Context
import com.lagradost.cloudstream3.CloudStreamApp.Companion.getKeyClass
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

    /** Host bridge for extensions that register repositories dynamically. */
    var onRepositoryAdded: (suspend (RepositoryData) -> Unit)? = null

    val PREBUILT_REPOSITORIES: Array<RepositoryData> = emptyArray()

    @Serializable
    data class Repository(
        @SerialName("iconUrl") val iconUrl: String? = null,
        @SerialName("name") val name: String = "",
        @SerialName("description") val description: String? = null,
        @SerialName("manifestVersion") val manifestVersion: Int = 1,
        @SerialName("pluginLists") val pluginLists: List<String> = emptyList(),
    )

    fun getRepositories(): Array<RepositoryData> =
        getKeyClass(REPOSITORIES_KEY, Array<RepositoryData>::class.java) ?: emptyArray()

    suspend fun parseRepository(url: String): Repository? = withContext(Dispatchers.IO) {
        runCatching {
            val connection = URL(url).openConnection() as HttpURLConnection
            try {
                connection.connectTimeout = 15_000
                connection.readTimeout = 15_000
                connection.instanceFollowRedirects = true
                connection.requestMethod = "GET"
                connection.setRequestProperty("Accept", "application/json")
                connection.setRequestProperty("User-Agent", "MewMobile-CloudStream")
                require(connection.responseCode in 200..299) {
                    "HTTP ${connection.responseCode}"
                }
                connection.inputStream.bufferedReader().use { reader ->
                    json.decodeFromString<Repository>(reader.readText())
                }
            } finally {
                connection.disconnect()
            }
        }.getOrNull()
    }

    suspend fun addRepository(repository: RepositoryData) {
        val added = repoLock.withLock {
            val current = getRepositories().toList()
            if (current.any { it.url == repository.url }) false
            else {
                setKey(REPOSITORIES_KEY, (current + repository).toTypedArray())
                true
            }
        }
        // Notify outside repoLock because the host callback may perform network I/O.
        if (added) onRepositoryAdded?.invoke(repository)
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
