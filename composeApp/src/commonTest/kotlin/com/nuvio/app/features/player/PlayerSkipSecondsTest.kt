package com.nuvio.app.features.player

import kotlin.test.Test
import kotlin.test.assertEquals

class PlayerSkipSecondsTest {
    @Test
    fun supportedSkipDurationsArePreserved() {
        assertEquals(5, normalizePlayerSkipSeconds(5))
        assertEquals(10, normalizePlayerSkipSeconds(10))
        assertEquals(15, normalizePlayerSkipSeconds(15))
        assertEquals(30, normalizePlayerSkipSeconds(30))
    }

    @Test
    fun missingOrUnsupportedDurationsFallBackToTenSeconds() {
        assertEquals(10, normalizePlayerSkipSeconds(null))
        assertEquals(10, normalizePlayerSkipSeconds(0))
        assertEquals(10, normalizePlayerSkipSeconds(-10))
        assertEquals(10, normalizePlayerSkipSeconds(20))
    }
}
