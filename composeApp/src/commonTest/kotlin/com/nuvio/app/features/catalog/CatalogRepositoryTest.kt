package com.nuvio.app.features.catalog

import com.nuvio.app.features.cloudstream.CloudStreamSearchItem
import com.nuvio.app.features.cloudstream.CloudStreamTvType
import kotlin.test.Test
import kotlin.test.assertEquals

class CatalogRepositoryTest {
    @Test
    fun `view all selects the indexed category when provider names are repeated`() {
        val first = CloudStreamSearchItem("cine", "first", "First", CloudStreamTvType.Movie)
        val second = CloudStreamSearchItem("cine", "second", "Second", CloudStreamTvType.Movie)
        val categories = listOf(
            "Raw Catalog" to listOf(first),
            "Raw Catalog" to listOf(second),
        )

        assertEquals(
            listOf(second),
            selectCloudStreamCategoryItems(categories, "Raw Catalog", categoryIndex = 1),
        )
    }

    @Test
    fun `view all falls back to category name when index is stale`() {
        val item = CloudStreamSearchItem("cine", "first", "First", CloudStreamTvType.Movie)
        val categories = listOf("Raw Catalog" to listOf(item))

        assertEquals(
            listOf(item),
            selectCloudStreamCategoryItems(categories, "Raw Catalog", categoryIndex = 99),
        )
    }
}
