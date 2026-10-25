package com.nuvio.app.features.cloudstream

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CloudStreamAutoUpdatePolicyTest {
    @Test
    fun profileGuardRejectsResultsFromAnInactiveProfile() {
        assertTrue(isCloudStreamProfileCurrent(2, 2))
        assertFalse(isCloudStreamProfileCurrent(1, 2))
    }

    @Test
    fun operationGuardRejectsResultsAfterProfileResetOrGenerationChange() {
        assertTrue(isCloudStreamOperationCurrent(1, 1, 4L, 4L))
        assertFalse(isCloudStreamOperationCurrent(1, 1, 4L, 5L))
        assertFalse(isCloudStreamOperationCurrent(1, 2, 4L, 4L))
    }

    @Test
    fun updateCheckCooldownAppliesOnlyToRecentSuccessfulCheckTimestamp() {
        val cooldownMs = 6 * 60 * 60 * 1_000L
        assertTrue(shouldThrottleCloudStreamAutoUpdateCheck(2, 2, 10_000L, 10_001L, cooldownMs))
        assertFalse(shouldThrottleCloudStreamAutoUpdateCheck(2, 2, 0L, 10_001L, cooldownMs))
        assertFalse(shouldThrottleCloudStreamAutoUpdateCheck(1, 2, 10_000L, 10_001L, cooldownMs))
        assertFalse(shouldThrottleCloudStreamAutoUpdateCheck(2, 2, 10_000L, 10_000L + cooldownMs, cooldownMs))
        assertFalse(shouldThrottleCloudStreamAutoUpdateCheck(2, 2, 10_000L, 9_999L, cooldownMs))
    }

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
