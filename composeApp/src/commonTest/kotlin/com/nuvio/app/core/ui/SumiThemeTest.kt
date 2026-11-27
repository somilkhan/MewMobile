package com.nuvio.app.core.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.nuvio.app.features.profiles.ProfileBackgroundPreset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SumiThemeTest {
    private val palette = ThemeColors.getColorPalette(AppTheme.SUMI)

    @Test
    fun sumiNameRoundTripsForStorage() {
        assertEquals(AppTheme.SUMI, AppTheme.valueOf("SUMI"))
    }

    @Test
    fun sumiButtonsUseOneSolidColour() {
        // A single-entry gradient means accentBrush() is a SolidColor, and the theme is not an
        // animated Enhanced theme, so primary buttons are never painted with a gradient.
        assertEquals(listOf(palette.secondary), palette.accentGradient)
        assertFalse(AppTheme.SUMI.isEnhanced)
        assertTrue(ThemeColors.animatedColors(AppTheme.SUMI).isEmpty())
        assertEquals("#E9DCD7", palette.nativeAccentHex)
    }

    @Test
    fun sumiTextMeetsContrastTargets() {
        assertTrue(contrast(palette.onSecondary, palette.secondary) >= 7f)
        assertTrue(contrast(palette.onSecondaryVariant, palette.secondaryVariant) >= 4.5f)
        assertTrue(contrast(Color(0xFFF5F7F8), palette.background) >= 7f)
        assertTrue(contrast(Color(0xFFF5F7F8), palette.backgroundCard) >= 7f)
    }

    @Test
    fun sumiHasNoRedOrOrangeAccent() {
        listOf(palette.secondary, palette.secondaryVariant, palette.focusRing).forEach { color ->
            // Soft, desaturated ivory/greys only: channels stay within a narrow band.
            val spread = maxOf(color.red, color.green, color.blue) - minOf(color.red, color.green, color.blue)
            assertTrue(spread < 0.12f, "Accent $color is too saturated for Sumi")
        }
    }

    @Test
    fun sumiHasItsOwnProfileBackdrop() {
        assertEquals(ProfileBackgroundPreset.SUMI, ProfileBackgroundPreset.fromTheme(AppTheme.SUMI))
        assertEquals(ProfileBackgroundPreset.SUMI, ProfileBackgroundPreset.fromStoredValue("enhanced-mesh://sumi"))
    }

    private fun contrast(a: Color, b: Color): Float {
        val la = a.luminance() + 0.05f
        val lb = b.luminance() + 0.05f
        return maxOf(la, lb) / minOf(la, lb)
    }
}
