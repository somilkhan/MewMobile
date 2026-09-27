package com.lagradost.cloudstream3.plugins

import android.content.Context
import com.lagradost.cloudstream3.CloudStreamApp.Companion.getKey
import com.lagradost.cloudstream3.CloudStreamApp.Companion.setKey
import com.lagradost.cloudstream3.ui.settings.extensions.RepositoryData
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * CloudStream-compatible repository registry.
 *
 * Mew owns repository discovery/download UI, but CloudStream extensions expect the
 * native RepositoryManager ABI to exist and persist RepositoryData under the
 * standard CloudStream key. This class intentionally implements only that ABI;
 * it does not maintain a second plugin catalog or provider pipeline.
 */
object RepositoryManager {
    const val REPOSITORIES_KEY = "REPOSITORIES_KEY"

    private val repoLock = Mutex()

    fun getRepositories(): Array<RepositoryData> =
        getKey<Array<RepositoryData>>(REPOSITORIES_KEY) ?: emptyArray()

    suspend fun addRepository(repository: RepositoryData) {
        val normalized = repository.copy(url = repository.url.trim())
        if (normalized.url.isBlank()) return

        repoLock.withLock {
            val current = getRepositories()
            setKey(
                REPOSITORIES_KEY,
                (current + normalized).distinctBy { it.url.trim() },
            )
        }
    }

    suspend fun removeRepository(context: Context, repository: RepositoryData) {
        val normalizedUrl = repository.url.trim()
        if (normalizedUrl.isBlank()) return

        repoLock.withLock {
            val current = getRepositories()
            setKey(
                REPOSITORIES_KEY,
                current.filter { it.url.trim() != normalizedUrl }.toTypedArray(),
            )
        }
    }
}
