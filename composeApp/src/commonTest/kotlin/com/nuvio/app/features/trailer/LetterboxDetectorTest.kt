package com.nuvio.app.features.trailer

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LetterboxDetectorTest {
    private val width = 64
    private val height = 120
    private val black = 0xFF101010.toInt()
    private val bright = 0xFF8090A0.toInt()

    @Test fun fullFrameHasNoBars() {
        val bar = LetterboxDetector.detectBar(frame { _, _ -> bright }, width, height)
        assertEquals(0f, bar)
        assertEquals(1f, LetterboxDetector.zoomFor(bar!!))
    }

    @Test fun detectsStableBars() {
        val pixels = frame { _, y -> if (y < 15 || y >= height - 15) black else bright }
        val bar = LetterboxDetector.detectBar(pixels, width, height)!!
        assertEquals(15f / height + 0.01f, bar, 0.0001f)
        assertEquals(LetterboxDetector.zoomFor(bar), LetterboxDetector.zoomFor(bar), 0.0001f)
    }

    @Test fun blackFrameIsNotValid() {
        assertNull(LetterboxDetector.detectBar(frame { _, _ -> black }, width, height))
    }

    @Test fun trackerRequiresStableSamples() {
        val tracker = LetterboxTracker()
        repeat(3) { assertNull(tracker.onSample(0.135f)) }
        assertEquals(LetterboxDetector.zoomFor(0.135f), tracker.onSample(0.135f)!!, 0.0001f)
    }

    private fun frame(color: (x: Int, y: Int) -> Int): IntArray =
        IntArray(width * height) { index -> color(index % width, index / width) }
}
