package com.nuvio.app.features.settings

import kotlin.test.Test
import kotlin.test.assertEquals

class NavBarStyleTest {
    @Test
    fun `liquid glass style key is persisted and restored`() {
        assertEquals(NavBarStyle.LIQUID_GLASS, NavBarStyle.fromKey("liquid_glass"))
        assertEquals(NavBarStyle.LIQUID_GLASS, NavBarStyle.fromKey("LIQUID_GLASS"))
    }

    @Test
    fun `unknown navigation style falls back to adaptive`() {
        assertEquals(NavBarStyle.ADAPTIVE, NavBarStyle.fromKey("unknown"))
        assertEquals(NavBarStyle.ADAPTIVE, NavBarStyle.fromKey(null))
    }
}
