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
        val original = AndroidPlaybackEngine.ExoPlayer
        assertEquals(original, original.switchTarget.switchTarget)
    }
}
