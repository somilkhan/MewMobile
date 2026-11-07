package com.nuvio.app.features.player

import kotlin.test.Test
import kotlin.test.assertEquals

class PlayerEngineSwitchTest {
    @Test
    fun exoPlayerSwitchesToLibmpv() {
        assertEquals(AndroidPlaybackEngine.Libmpv, AndroidPlaybackEngine.ExoPlayer.switchTarget)
    }

    @Test
    fun libmpvSwitchesToExoPlayer() {
        assertEquals(AndroidPlaybackEngine.ExoPlayer, AndroidPlaybackEngine.Libmpv.switchTarget)
    }

    @Test
    fun switchTargetIsReversible() {
        assertEquals(AndroidPlaybackEngine.ExoPlayer, AndroidPlaybackEngine.ExoPlayer.switchTarget.switchTarget)
    }

    @Test
    fun autoPrefersLibmpvForMatroska() {
        assertEquals(AndroidPlaybackEngine.Libmpv, AndroidPlaybackEngine.Auto.resolveForStream("mkv"))
        assertEquals(AndroidPlaybackEngine.Libmpv, AndroidPlaybackEngine.Auto.resolveForStream("matroska"))
    }

    @Test
    fun explicitEngineOverridesMatroskaDefault() {
        assertEquals(AndroidPlaybackEngine.ExoPlayer, AndroidPlaybackEngine.ExoPlayer.resolveForStream("mkv"))
        assertEquals(AndroidPlaybackEngine.Libmpv, AndroidPlaybackEngine.Libmpv.resolveForStream("mkv"))
    }

    @Test
    fun autoPrefersExoForOtherStreams() {
        assertEquals(AndroidPlaybackEngine.ExoPlayer, AndroidPlaybackEngine.Auto.resolveForStream("mp4"))
        assertEquals(AndroidPlaybackEngine.ExoPlayer, AndroidPlaybackEngine.Auto.resolveForStream(null))
    }
}
