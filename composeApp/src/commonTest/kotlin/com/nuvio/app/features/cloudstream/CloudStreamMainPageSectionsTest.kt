package com.nuvio.app.features.cloudstream

import kotlin.test.Test
import kotlin.test.assertEquals

class CloudStreamMainPageSectionsTest {
    @Test
    fun `generic raw catalog response uses declared request name`() {
        assertEquals(
            "Trending",
            cloudStreamMainPageSectionTitle(
                apiName = "CineStream",
                requestName = "Trending",
                sectionName = "Raw Catalog",
                multipleApis = false,
            ),
        )
    }

    @Test
    fun `multiple API title keeps provider identity and request category`() {
        assertEquals(
            "Movies API · Popular",
            cloudStreamMainPageSectionTitle(
                apiName = "Movies API",
                requestName = "Popular",
                sectionName = "Raw Catalog",
                multipleApis = true,
            ),
        )
    }

    @Test
    fun `specific response section name remains authoritative`() {
        assertEquals(
            "Recently Added",
            cloudStreamMainPageSectionTitle(
                apiName = "CineStream",
                requestName = "Browse",
                sectionName = "Recently Added",
                multipleApis = false,
            ),
        )
    }

    @Test
    fun `blank names fall back safely`() {
        assertEquals(
            "Raw Catalog",
            cloudStreamMainPageSectionTitle(
                apiName = "CineStream",
                requestName = "",
                sectionName = "",
                multipleApis = false,
            ),
        )
    }
}
