package com.nuvio.app.features.search

// Mew: CloudStream search sections, Discover providers and Discover main pages.
// Kept out of the upstream SearchRepository.kt (same package, so callers are
// unchanged) to keep luq merges conflict-free. See MEW_INTEGRATION.md.

import com.nuvio.app.features.catalog.CatalogPage
import com.nuvio.app.features.catalog.CatalogTarget
import com.nuvio.app.features.cloudstream.CloudStreamPluginItem
import com.nuvio.app.features.cloudstream.CloudStreamRepository
import com.nuvio.app.features.cloudstream.CloudStreamSearchRouteIndex
import com.nuvio.app.features.cloudstream.toMetaPreview
import com.nuvio.app.features.home.HomeCatalogSection
import com.nuvio.app.features.tmdb.TmdbSettingsRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withTimeoutOrNull

internal fun shouldUseCloudStreamExtensions(tmdbEnabled: Boolean): Boolean = !tmdbEnabled

private const val CLOUDSTREAM_SEARCH_MIN_QUERY_LENGTH = 3
private const val CLOUDSTREAM_SEARCH_CONCURRENCY = 8
private const val CLOUDSTREAM_SEARCH_PROVIDER_TIMEOUT_MS = 15_000L

internal fun cloudStreamSearchPlugins(
    tmdbEnabled: Boolean,
    normalizedQuery: String,
): List<CloudStreamPluginItem> =
    if (
        shouldUseCloudStreamExtensions(tmdbEnabled) &&
        normalizedQuery.length >= CLOUDSTREAM_SEARCH_MIN_QUERY_LENGTH
    ) {
        CloudStreamRepository.uiState.value.plugins.filter(CloudStreamPluginItem::isRunnable)
    } else {
        emptyList()
    }

internal fun cloudStreamDiscoverPlugins(tmdbEnabled: Boolean): List<CloudStreamPluginItem> =
    if (shouldUseCloudStreamExtensions(tmdbEnabled)) {
        CloudStreamRepository.uiState.value.plugins.filter(CloudStreamPluginItem::isRunnable)
    } else {
        emptyList()
    }

internal fun cloudStreamDiscoverSources(
    cloudProviders: List<CloudStreamPluginItem>,
): List<DiscoverCatalogOption> =
    cloudProviders.map { plugin ->
        DiscoverCatalogOption(
            key = "cloudstream:" + plugin.metadata.id.storageKey,
            addonName = plugin.metadata.name,
            manifestUrl = "",
            type = "CloudStream",
            catalogId = plugin.metadata.id.value,
            catalogName = plugin.metadata.name,
            supportsPagination = false,
            cloudStreamProviderId = plugin.metadata.id.value,
        )
    }

internal fun hasRunnableCloudStreamDiscoverProviders(): Boolean =
    shouldUseCloudStreamExtensions(TmdbSettingsRepository.snapshot().enabled) &&
        CloudStreamRepository.uiState.value.plugins.any(CloudStreamPluginItem::isRunnable)

internal suspend fun loadCloudStreamDiscoverPage(
    cloudStreamProviderId: String,
    requestedSkip: Int,
): CatalogPage {
    val sections = CloudStreamRepository.getMainPage(cloudStreamProviderId, requestedSkip / 20 + 1).getOrThrow()
    return CatalogPage(
        items = sections.flatMap { it.second }.map { it.toMetaPreview() },
        rawItemCount = sections.sumOf { it.second.size },
        nextSkip = null,
    )
}

internal suspend fun cloudSearchSections(
    query: String,
    plugins: List<CloudStreamPluginItem>,
    onSection: (HomeCatalogSection) -> Unit,
): List<HomeCatalogSection> = coroutineScope {
    val semaphore = Semaphore(CLOUDSTREAM_SEARCH_CONCURRENCY)
    val sectionMutex = Mutex()
    val sectionsByProviderId = mutableMapOf<String, HomeCatalogSection>()

    plugins.map { plugin ->
        async {
            val section = semaphore.withPermit {
                withTimeoutOrNull(CLOUDSTREAM_SEARCH_PROVIDER_TIMEOUT_MS) {
                    plugin.toCloudSearchSection(query)
                }
            } ?: return@async

            sectionMutex.withLock {
                sectionsByProviderId[plugin.metadata.id.value] = section
            }
            onSection(section)
        }
    }.awaitAll()

    // Preserve repository order in the stable/final state even though sections are
    // displayed progressively in network completion order while the search runs.
    plugins.mapNotNull { plugin -> sectionsByProviderId[plugin.metadata.id.value] }
}

private suspend fun CloudStreamPluginItem.toCloudSearchSection(
    query: String,
): HomeCatalogSection? {
    val result = CloudStreamRepository.search(query, metadata.id.value)
        .firstOrNull()
        ?.getOrNull()
        .orEmpty()
    if (result.isEmpty()) return null
    CloudStreamSearchRouteIndex.remember(
        providerId = metadata.id.value,
        query = query,
        items = result,
    )
    val previews = result.map { it.toMetaPreview() }
    val contentType = result.first().type.nuvioType
    return HomeCatalogSection(
        key = "cloudstream:${metadata.id.storageKey}:search:${query.lowercase()}",
        title = "${metadata.name} · CloudStream",
        subtitle = metadata.language?.uppercase() ?: "CloudStream",
        addonName = metadata.name,
        target = CatalogTarget.CloudStream(
            providerId = metadata.id.value,
            categoryName = "search",
            searchQuery = query,
            contentType = contentType,
            supportsPagination = false,
        ),
        items = previews,
        availableItemCount = previews.size,
        hasMore = false,
    )
}
