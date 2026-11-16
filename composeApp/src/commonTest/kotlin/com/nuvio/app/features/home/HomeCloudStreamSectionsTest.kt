package com.nuvio.app.features.home

import com.nuvio.app.features.cloudstream.CloudStreamSearchItem
import com.nuvio.app.features.cloudstream.CloudStreamTvType
import com.nuvio.app.features.catalog.CatalogTarget
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class HomeCloudStreamSectionsTest {
    @Test
    fun `duplicate raw catalog names remain separate visible sections`() {
        val first = CloudStreamSearchItem(
            providerId = "cine",
            data = "first",
            name = "First title",
            type = CloudStreamTvType.Movie,
        )
        val second = CloudStreamSearchItem(
            providerId = "cine",
            data = "second",
            name = "Second title",
            type = CloudStreamTvType.Movie,
        )

        val sections = buildCloudStreamHomeSections(
            providerStorageKey = "cine",
            providerId = "cine",
            providerName = "CineStream",
            categories = listOf(
                "Raw Catalog" to listOf(first),
                "Raw Catalog" to listOf(second),
            ),
        )

        assertEquals(2, sections.size)
        assertNotEquals(sections[0].key, sections[1].key)
        assertEquals(listOf("Raw Catalog 1", "Raw Catalog 2"), sections.map { it.title })
        assertEquals(
            listOf(0, 1),
            sections.map { (it.target as CatalogTarget.CloudStream).categoryIndex },
        )
    }
}
