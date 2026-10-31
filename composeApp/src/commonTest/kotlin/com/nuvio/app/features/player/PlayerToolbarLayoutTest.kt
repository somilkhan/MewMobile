package com.nuvio.app.features.player

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class PlayerToolbarLayoutTest {
    @Test
    fun defaultsPreserveExistingMewActionPills() {
        assertEquals(PlayerToolbarButton.entries, PlayerToolbarLayout.Default.visibleButtons)
    }

    @Test
    fun encodeDecodePreservesVisibilityAndOrder() {
        val layout = PlayerToolbarLayout.Default
            .withVisibility(PlayerToolbarButton.CHANNELS, false)
            .moved(0, 3)
        assertEquals(layout, PlayerToolbarLayout.decode(layout.encode()))
    }

    @Test
    fun unknownAndDuplicateStorageEntriesAreIgnoredAndMissingButtonsAreRestored() {
        val decoded = PlayerToolbarLayout.decode("speed:h,unknown:v,speed:v")
        assertEquals(PlayerToolbarButton.entries.toSet(), decoded.entries.map(PlayerToolbarEntry::button).toSet())
        assertEquals(false, decoded.entries.first { it.button == PlayerToolbarButton.SPEED }.visible)
    }

    @Test
    fun moveAndVisibilityReturnNewLayouts() {
        val defaults = PlayerToolbarLayout.Default
        assertNotEquals(defaults, defaults.withVisibility(PlayerToolbarButton.AUDIO, false))
        assertNotEquals(defaults, defaults.moved(0, 1))
    }
}
