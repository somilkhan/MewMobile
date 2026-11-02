package com.nuvio.app.core.ui.glass

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class LiquidGlassSettingsTest {
    @Test
    fun globalToggleDisablesGlassAndRestoresCapabilityWhenEnabled() {
        val previous = LiquidGlassSettings.enabled
        try {
            LiquidGlassSettings.enabled = false
            assertFalse(isLiquidGlassSupported())

            LiquidGlassSettings.enabled = true
            assertEquals(isLiquidGlassAvailable(), isLiquidGlassSupported())
        } finally {
            LiquidGlassSettings.enabled = previous
        }
    }
}
