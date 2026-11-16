package com.nuvio.app.features.player

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PipSubtitleScaleTest {
    @Test
    fun fittedWideVideoUsesLetterboxedHeight() {
        assertEquals(
            562.5f,
            calculateFittedVideoHeightPx(1_000f, 1_000f, 1_920, 1_080, 1f),
        )
    }

    @Test
    fun fittedVideoUsesAvailableHeightWhenWidthAllowsIt() {
        assertEquals(
            700f,
            calculateFittedVideoHeightPx(1_600f, 700f, 1_920, 1_080, 1f),
        )
    }

    @Test
    fun pixelAspectRatioIsIncludedInFitCalculation() {
        assertEquals(
            450f,
            calculateFittedVideoHeightPx(900f, 600f, 1_440, 1_080, 1.5f),
        )
    }

    @Test
    fun invalidGeometryReturnsNull() {
        assertNull(calculateFittedVideoHeightPx(0f, 100f, 1_920, 1_080, 1f))
        assertNull(calculateFittedVideoHeightPx(100f, 100f, 0, 1_080, 1f))
        assertNull(calculateFittedVideoHeightPx(100f, 100f, 1_920, 1_080, Float.NaN))
    }
}
