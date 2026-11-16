package com.nuvio.app.features.search

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SearchProviderRoutingTest {
    @Test
    fun cloudStreamIsFallbackOnlyWhenTmdbIsDisabled() {
        assertTrue(shouldUseCloudStreamExtensions(tmdbEnabled = false))
        assertFalse(shouldUseCloudStreamExtensions(tmdbEnabled = true))
    }
}
