package com.nuvio.app.features.player

import kotlin.test.Test
import kotlin.test.assertEquals

class AndroidLibmpvVideoOutputDefaultTest {
    @Test
    fun unsetRendererPreferenceUsesCompatibilityGpuRenderer() {
        assertEquals(AndroidLibmpvVideoOutput.Gpu, resolveAndroidLibmpvVideoOutput(null))
        assertEquals("gpu", resolveAndroidLibmpvVideoOutput(null).mpvValue)
    }

    @Test
    fun explicitRendererPreferenceIsPreserved() {
        assertEquals(
            AndroidLibmpvVideoOutput.GpuNext,
            resolveAndroidLibmpvVideoOutput(AndroidLibmpvVideoOutput.GpuNext.name),
        )
        assertEquals(
            AndroidLibmpvVideoOutput.Gpu,
            resolveAndroidLibmpvVideoOutput(AndroidLibmpvVideoOutput.Gpu.name),
        )
    }

    @Test
    fun clearingPlayerSettingsResetsToCompatibilityRenderer() {
        PlayerSettingsRepository.clearLocalState()
        assertEquals(AndroidLibmpvVideoOutput.Gpu, PlayerSettingsRepository.uiState.value.androidLibmpvVideoOutput)
    }

    @Test
    fun invalidRendererPreferenceFallsBackSafely() {
        assertEquals(AndroidLibmpvVideoOutput.Gpu, resolveAndroidLibmpvVideoOutput("unknown"))
    }
}
