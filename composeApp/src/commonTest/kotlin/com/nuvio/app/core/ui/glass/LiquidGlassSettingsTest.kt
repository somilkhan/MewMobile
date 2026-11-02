package com.nuvio.app.core.ui.glass

import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class LiquidGlassSettingsTest {
    @Test
    fun globalToggleControlsGlassEnabledState() {
        val previous = LiquidGlassSettings.enabled
        try {
            LiquidGlassSettings.enabled = false
            assertFalse(isLiquidGlassSupported())

            LiquidGlassSettings.enabled = true
            assertTrue(LiquidGlassSettings.enabled)
        } finally {
            LiquidGlassSettings.enabled = previous
        }
    }
}
