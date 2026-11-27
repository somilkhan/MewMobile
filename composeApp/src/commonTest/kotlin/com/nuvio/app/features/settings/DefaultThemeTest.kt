package com.nuvio.app.features.settings

import com.nuvio.app.core.ui.AppTheme
import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultThemeTest {
    @Test
    fun unsetThemeFallsBackToSumi() {
        assertEquals(AppTheme.SUMI, DefaultAppTheme)
        assertEquals(AppTheme.SUMI, (null as String?).toAppTheme())
        assertEquals(AppTheme.SUMI, "NOT_A_THEME".toAppTheme())
    }

    @Test
    fun explicitlyChosenThemesAreKept() {
        assertEquals(AppTheme.MEW, "MEW".toAppTheme())
        assertEquals(AppTheme.GOLD, "GOLD".toAppTheme())
        assertEquals(AppTheme.SUNSET, "ULTRAVIOLET".toAppTheme())
    }
}
