package com.nuvio.app.features.catalog

// Mew: CloudStream catalog pages (provider search results and MainPage categories).
// Kept out of the upstream CatalogRepository.kt (same package, so callers are
// unchanged) to keep luq merges conflict-free. See MEW_INTEGRATION.md.

import com.nuvio.app.features.cloudstream.CloudStreamRepository
import com.nuvio.app.features.cloudstream.CloudStreamSearchItem
import com.nuvio.app.features.cloudstream.toMetaPreview

internal fun selectCloudStreamCategoryItems(
    categories: List<Pair<String, List<CloudStreamSearchItem>>>,
    categoryName: String,
    categoryIndex: Int?,
): List<CloudStreamSearchItem> {
    val indexedCategory = categoryIndex?.let { categories.getOrNull(it) }
        ?.takeIf { it.first == categoryName }
    return (indexedCategory ?: categories.firstOrNull { it.first == categoryName })
        ?.second
        .orEmpty()
}

internal suspend fun fetchCloudStreamCatalogPage(
    target: CatalogTarget.CloudStream,
    requestedSkip: Int,
): CatalogPage {
    val pageNumber = requestedSkip.takeIf { it > 0 } ?: 1
    val items = if (!target.searchQuery.isNullOrBlank()) {
        CloudStreamRepository.search(target.searchQuery, target.providerId)
            .firstOrNull()
            ?.getOrThrow()
            .orEmpty()
            .map { it.toMetaPreview() }
    } else {
        selectCloudStreamCategoryItems(
            categories = CloudStreamRepository.getMainPage(target.providerId, pageNumber).getOrThrow(),
            categoryName = target.categoryName,
            categoryIndex = target.categoryIndex,
        ).map { it.toMetaPreview() }
    }
    return CatalogPage(
        items = items,
        rawItemCount = items.size,
        nextSkip = null,
    )
}
