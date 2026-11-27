package com.nuvio.app.features.details

// Mew: CloudStream detail routing (TMDB gate, detail timeout, provider load).
// Kept out of the upstream MetaDetailsRepository.kt (same package, so callers are
// unchanged) to keep luq merges conflict-free. See MEW_INTEGRATION.md.

import com.nuvio.app.features.cloudstream.CloudStreamRepository
import com.nuvio.app.features.cloudstream.CloudStreamRouteData
import com.nuvio.app.features.cloudstream.isCloudStreamDiscoveryAllowed
import com.nuvio.app.features.cloudstream.parseCloudStreamRouteId
import com.nuvio.app.features.cloudstream.toMetaDetails
import com.nuvio.app.features.tmdb.TmdbSettingsRepository
import kotlinx.coroutines.withTimeoutOrNull

internal const val CLOUDSTREAM_DETAIL_TIMEOUT_MS = 120_000L

internal fun isCloudStreamRouteAllowed(id: String): Boolean {
    if (parseCloudStreamRouteId(id) == null) return true
    TmdbSettingsRepository.ensureLoaded()
    return isCloudStreamDiscoveryAllowed(TmdbSettingsRepository.snapshot().enabled)
}

internal suspend fun loadCloudStreamMetaDetails(route: CloudStreamRouteData): MetaDetails? =
    withTimeoutOrNull(CLOUDSTREAM_DETAIL_TIMEOUT_MS) {
        CloudStreamRepository.load(route.providerId, route.data)
            .getOrNull()
            ?.toMetaDetails()
    }
