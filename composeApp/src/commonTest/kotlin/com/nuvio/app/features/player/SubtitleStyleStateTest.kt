package com.nuvio.app.features.player

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SubtitleStyleStateTest {
    @Test
    fun defaultOutlineIsThin() {
        assertEquals(1, SubtitleStyleState.DEFAULT.outlineWidth)
    }

    @Test
    fun subtitleShadowDefaultsToDisabledWithBoundedStrength() {
        assertFalse(SubtitleStyleState.DEFAULT.shadowEnabled)
        assertEquals(SUBTITLE_SHADOW_STRENGTH_DEFAULT, SubtitleStyleState.DEFAULT.shadowStrength)
        assertEquals(SUBTITLE_SHADOW_STRENGTH_MIN, normalizeSubtitleShadowStrength(Int.MIN_VALUE))
        assertEquals(SUBTITLE_SHADOW_STRENGTH_MAX, normalizeSubtitleShadowStrength(Int.MAX_VALUE))
        assertEquals(SUBTITLE_SHADOW_STRENGTH_DEFAULT, normalizeSubtitleShadowStrength(SUBTITLE_SHADOW_STRENGTH_DEFAULT))
    }

    @Test
    fun customFontDisablesLibassToKeepTheSelectedTypeface() {
        val style = SubtitleStyleState(
            fontFamily = SubtitleFontFamily.Custom,
            customFontPath = "/private/fonts/custom.ttf",
        )

        assertTrue(style.usesCustomFont())
        assertFalse(style.shouldUseLibass(libassEnabled = true))
    }

    @Test
    fun emptyCustomFontPathDoesNotDisableLibass() {
        val style = SubtitleStyleState(fontFamily = SubtitleFontFamily.Custom)

        assertFalse(style.usesCustomFont())
        assertTrue(style.shouldUseLibass(libassEnabled = true))
    }
}
