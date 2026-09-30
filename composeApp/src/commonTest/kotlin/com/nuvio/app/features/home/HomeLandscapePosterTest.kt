package com.nuvio.app.features.home

import kotlin.test.Test
import kotlin.test.assertEquals

class HomeLandscapePosterTest {
    @Test
    fun addonLandscapePosterIsParsedAndUsedAsBannerFallback() {
        val result = HomeCatalogParser.parseCatalog(
            """{"metas":[{"id":"tt1","type":"movie","name":"Example","poster":"p","landscapePoster":"land"}]}"""
        ).single()
        assertEquals("land", result.landscapePoster)
        assertEquals("land", result.banner)
    }
}
