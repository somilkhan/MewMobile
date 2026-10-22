package com.nuvio.app.features.player

import kotlin.test.Test
import kotlin.test.assertEquals

class SubtitleSyncByEarTest {
    @Test
    fun `subtitle appearing after spoken line shifts earlier`() {
        assertEquals(-500, subtitleDelayAfterSyncByEar(0, heardPositionMs = 10_000, sawPositionMs = 10_500))
    }

    @Test
    fun `subtitle appearing before spoken line shifts later`() {
        assertEquals(300, subtitleDelayAfterSyncByEar(0, heardPositionMs = 10_300, sawPositionMs = 10_000))
    }

    @Test
    fun `new correction adjusts existing delay instead of replacing it`() {
        assertEquals(-300, subtitleDelayAfterSyncByEar(200, heardPositionMs = 10_000, sawPositionMs = 10_500))
    }

    @Test
    fun `correction clamps to supported subtitle delay bounds`() {
        assertEquals(SUBTITLE_DELAY_MAX_MS, subtitleDelayAfterSyncByEar(0, Long.MAX_VALUE / 4, Long.MIN_VALUE / 4))
        assertEquals(SUBTITLE_DELAY_MIN_MS, subtitleDelayAfterSyncByEar(0, Long.MIN_VALUE / 4, Long.MAX_VALUE / 4))
    }
}
