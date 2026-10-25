package com.nuvio.app.features.player

import kotlin.test.Test
import kotlin.test.assertEquals

class PlayerOrientationPreferenceTest {
    @Test
    fun originalOrientationSurvivesConfigurationRecreation() {
        PlayerOrientationPreference.reset()
        assertEquals(1, PlayerOrientationPreference.captureOriginalOrientation(1))
        // A recreated Activity can report the player-requested orientation; the original must win.
        assertEquals(1, PlayerOrientationPreference.captureOriginalOrientation(2))
        PlayerOrientationPreference.reset()
        assertEquals(3, PlayerOrientationPreference.captureOriginalOrientation(3))
        PlayerOrientationPreference.reset()
    }
}
