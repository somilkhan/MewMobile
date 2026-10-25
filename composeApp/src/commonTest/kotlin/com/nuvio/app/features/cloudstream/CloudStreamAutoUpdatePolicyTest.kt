package com.nuvio.app.features.cloudstream

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CloudStreamAutoUpdatePolicyTest {
    @Test
    fun allowsRunOnlyWhenProfilePreferenceAndGenerationStillMatch() {
        assertTrue(
            shouldContinueCloudStreamAutoUpdate(
                checkProfileId = 2,
                currentProfileId = 2,
                autoUpdatesEnabled = true,
                checkGeneration = 4L,
                currentGeneration = 4L,
            ),
        )
    }

    @Test
    fun stopsRunWhenPreferenceIsDisabled() {
        assertFalse(
            shouldContinueCloudStreamAutoUpdate(
                checkProfileId = 2,
                currentProfileId = 2,
                autoUpdatesEnabled = false,
                checkGeneration = 4L,
                currentGeneration = 4L,
            ),
        )
    }

    @Test
    fun stopsRunWhenProfileChanges() {
        assertFalse(
            shouldContinueCloudStreamAutoUpdate(
                checkProfileId = 1,
                currentProfileId = 2,
                autoUpdatesEnabled = true,
                checkGeneration = 4L,
                currentGeneration = 4L,
            ),
        )
    }

    @Test
    fun stopsStaleRunAfterRapidPreferenceToggle() {
        assertFalse(
            shouldContinueCloudStreamAutoUpdate(
                checkProfileId = 2,
                currentProfileId = 2,
                autoUpdatesEnabled = true,
                checkGeneration = 4L,
                currentGeneration = 5L,
            ),
        )
    }
}
