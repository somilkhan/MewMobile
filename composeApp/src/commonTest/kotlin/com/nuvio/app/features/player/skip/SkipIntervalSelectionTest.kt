package com.nuvio.app.features.player.skip

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SkipIntervalSelectionTest {
    @Test
    fun overlappingIntervalsChooseTheFurthestSeekTarget() {
        val shorter = SkipInterval(10.0, 30.0, "intro", "provider-a")
        val longer = SkipInterval(15.0, 42.0, "recap", "provider-b")

        assertEquals(longer, selectActiveSkipInterval(listOf(shorter, longer), positionSec = 20.0))
    }

    @Test
    fun intervalEndIsExclusiveAndNextActiveIntervalCanTakeOver() {
        val first = SkipInterval(10.0, 20.0, "intro", "provider-a")
        val second = SkipInterval(20.0, 35.0, "recap", "provider-b")

        assertEquals(second, selectActiveSkipInterval(listOf(first, second), positionSec = 20.0))
        assertNull(selectActiveSkipInterval(listOf(first, second), positionSec = 35.0))
    }

    @Test
    fun ignoresInvalidIntervalsAndNonFinitePositions() {
        val invalid = listOf(
            SkipInterval(Double.NaN, 30.0, "intro", "provider-a"),
            SkipInterval(10.0, Double.POSITIVE_INFINITY, "recap", "provider-b"),
            SkipInterval(20.0, 20.0, "outro", "provider-c"),
            SkipInterval(10.0, Double.MAX_VALUE, "intro", "provider-d"),
        )

        assertNull(selectActiveSkipInterval(invalid, positionSec = 25.0))
        assertNull(selectActiveSkipInterval(invalid, positionSec = Double.NaN))
    }
}
