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
    fun backwardAndForwardSeekStepsUseTheirConfiguredDurations() {
        assertEquals(
            5_000L,
            playerSkipStepMs(PlayerSeekDirection.Backward, skipBackwardSeconds = 5, skipForwardSeconds = 30),
        )
        assertEquals(
            30_000L,
            playerSkipStepMs(PlayerSeekDirection.Forward, skipBackwardSeconds = 5, skipForwardSeconds = 30),
        )
    }

    @Test
    fun missingOrUnsupportedDurationsFallBackToTenSeconds() {
        assertEquals(10, normalizePlayerSkipSeconds(null))
        assertEquals(10, normalizePlayerSkipSeconds(0))
        assertEquals(10, normalizePlayerSkipSeconds(-10))
        assertEquals(10, normalizePlayerSkipSeconds(20))
    }
}
