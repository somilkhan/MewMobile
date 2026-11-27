package com.nuvio.app.features.home

// Mew: CloudStream home shelves (provider selection, MainPage scan, section building).
// Kept out of the upstream HomeRepository.kt / HomeModels.kt (same package, so callers
// are unchanged) to keep luq merges conflict-free. See MEW_INTEGRATION.md.

import com.nuvio.app.features.catalog.CatalogTarget
import com.nuvio.app.features.cloudstream.CLOUDSTREAM_DISCOVERY_TIMEOUT_MS
import com.nuvio.app.features.cloudstream.CloudStreamPluginItem
import com.nuvio.app.features.cloudstream.CloudStreamRepository
import com.nuvio.app.features.cloudstream.CloudStreamSearchItem
import com.nuvio.app.features.cloudstream.isCloudStreamDiscoveryAllowed
import com.nuvio.app.features.cloudstream.toMetaPreview
import com.nuvio.app.features.tmdb.TmdbSettingsRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withTimeoutOrNull

internal const val HOME_CLOUDSTREAM_PROVIDER_TIMEOUT_MS = CLOUDSTREAM_DISCOVERY_TIMEOUT_MS + 5_000L
private const val HOME_CLOUDSTREAM_PROVIDER_CONCURRENCY = 4

internal fun cloudStreamSectionKey(
    providerStorageKey: String,
    categoryName: String,
    categoryIndex: Int = 0,
): String = "cloudstream:$providerStorageKey:$categoryIndex:$categoryName"

/**
 * CloudStream home shelves are a fallback discovery surface only. When at least one
 * Stremio-style add-on catalog is enabled, its catalogs own Home; CloudStream remains
 * available independently for stream extraction.
 */
internal fun shouldShowCloudStreamHomeCatalogs(
    hasAddonCatalogs: Boolean,
    tmdbEnabled: Boolean,
): Boolean = !hasAddonCatalogs && tmdbEnabled

internal fun effectiveHomeCloudStreamProviderId(
    selectedProviderId: String?,
    hasAddonCatalogs: Boolean,
): String? = selectedProviderId.takeUnless { hasAddonCatalogs }

internal fun buildCloudStreamHomeSections(
    providerStorageKey: String,
    providerId: String,
    providerName: String,
    categories: List<Pair<String, List<CloudStreamSearchItem>>>,
): List<HomeCatalogSection> {
    val categoryNameCounts = categories.groupingBy { it.first }.eachCount()
    val occurrenceByName = mutableMapOf<String, Int>()
    return categories.mapIndexedNotNull { categoryIndex, (categoryName, items) ->
        if (items.isEmpty()) return@mapIndexedNotNull null
        val occurrence = (occurrenceByName[categoryName] ?: 0) + 1
        occurrenceByName[categoryName] = occurrence
        val displayName = if ((categoryNameCounts[categoryName] ?: 0) > 1) {
            "$categoryName $occurrence"
        } else {
            categoryName
        }
        val previews = items.take(HOME_CATALOG_PREVIEW_FETCH_LIMIT).map { it.toMetaPreview() }
        HomeCatalogSection(
            key = cloudStreamSectionKey(providerStorageKey, categoryName, categoryIndex),
            title = displayName,
            subtitle = "$providerName · CloudStream",
            addonName = providerName,
            target = CatalogTarget.CloudStream(
                providerId = providerId,
                categoryName = categoryName,
                contentType = items.first().type.nuvioType,
                supportsPagination = false,
                categoryIndex = categoryIndex,
            ),
            items = previews,
            availableItemCount = items.size,
            hasMore = items.size > previews.size,
        )
    }
}

internal data class CloudHomeProviderResult(
    val sections: List<HomeCatalogSection>,
    val errorMessage: String?,
)

internal data class CloudHomeSectionsResult(
    val sections: List<HomeCatalogSection>,
    val errorMessage: String?,
)

internal fun selectHomeCloudStreamPlugins(
    plugins: List<CloudStreamPluginItem>,
    hasAddonCatalogs: Boolean,
    selectedProviderId: String?,
): List<CloudStreamPluginItem> {
    // CloudStream catalogs are a fallback only. Do not mix their home shelves with
    // enabled add-on catalogs; CloudStream provider execution for stream extraction
    // is handled independently by StreamsRepository.
    val tmdbEnabled = isCloudStreamDiscoveryAllowed(TmdbSettingsRepository.snapshot().enabled)
    return if (shouldShowCloudStreamHomeCatalogs(hasAddonCatalogs, tmdbEnabled)) {
        plugins
            .filter(CloudStreamPluginItem::isRunnable)
            .filter { selectedProviderId == null || it.metadata.id.value == selectedProviderId }
    } else {
        emptyList()
    }
}

internal fun StringBuilder.appendCloudStreamHomeRequestKey(
    registryRevision: Long,
    cloudPlugins: List<CloudStreamPluginItem>,
    selectedProviderId: String?,
) {
    append("|cloudstream=")
    append(registryRevision)
    append(':')
    append(cloudPlugins.joinToString(separator = ",") { it.metadata.id.value })
    append("|selectedCloudStreamProvider=")
    append(selectedProviderId.orEmpty())
}

internal suspend fun loadCloudStreamHomeSections(
    plugins: List<CloudStreamPluginItem>,
    onProgress: (List<HomeCatalogSection>) -> Unit,
): CloudHomeSectionsResult {
    val sections = mutableListOf<HomeCatalogSection>()
    var firstError: String? = null
    val scanPlugins = plugins
    val gate = Semaphore(HOME_CLOUDSTREAM_PROVIDER_CONCURRENCY)
    val resultChannel = Channel<Pair<CloudStreamPluginItem, CloudHomeProviderResult>>(Channel.BUFFERED)
    coroutineScope {
        scanPlugins.forEach { plugin ->
            launch {
                val result = gate.withPermit {
                    val mainPage = withTimeoutOrNull(HOME_CLOUDSTREAM_PROVIDER_TIMEOUT_MS) {
                        CloudStreamRepository.getMainPage(plugin.metadata.id.value, page = 1)
                    }
                    if (mainPage == null) {
                        CloudHomeProviderResult(emptyList(), "${plugin.metadata.name} timed out")
                    } else {
                        mainPage.fold(
                            onSuccess = { categories ->
                                CloudHomeProviderResult(
                                    buildCloudStreamHomeSections(
                                        providerStorageKey = plugin.metadata.id.storageKey,
                                        providerId = plugin.metadata.id.value,
                                        providerName = plugin.metadata.name,
                                        categories = categories,
                                    ),
                                    null,
                                )
                            },
                            onFailure = { error -> CloudHomeProviderResult(emptyList(), error.message) },
                        )
                    }
                }
                resultChannel.send(plugin to result)
            }
        }
        repeat(scanPlugins.size) {
            val (_, result) = resultChannel.receive()
            // Keep every category returned by each runnable provider as its own shelf.
            // Per-provider timeouts keep a broken extension from blocking the progressive scan.
            if (result.sections.isNotEmpty()) sections += result.sections
            if (firstError == null) firstError = result.errorMessage
            if (sections.isNotEmpty()) {
                onProgress(sections.toList())
            }
        }
    }
    resultChannel.close()
    return CloudHomeSectionsResult(sections = sections, errorMessage = firstError)
}
