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
    @Test
    fun toggleChangesPortraitStateAndCanToggleBack() {
        PlayerOrientationPreference.reset()
        assertEquals(false, PlayerOrientationPreference.portrait.value)
        PlayerOrientationPreference.toggle()
        assertEquals(true, PlayerOrientationPreference.portrait.value)
        PlayerOrientationPreference.toggle()
        assertEquals(false, PlayerOrientationPreference.portrait.value)
        PlayerOrientationPreference.reset()
    }

    @Test
    fun resetClearsToggleAndOriginalOrientationSession() {
        PlayerOrientationPreference.reset()
        PlayerOrientationPreference.captureOriginalOrientation(1)
        PlayerOrientationPreference.toggle()
        PlayerOrientationPreference.reset()
        assertEquals(false, PlayerOrientationPreference.portrait.value)
        assertEquals(3, PlayerOrientationPreference.captureOriginalOrientation(3))
        PlayerOrientationPreference.reset()
    }

}
