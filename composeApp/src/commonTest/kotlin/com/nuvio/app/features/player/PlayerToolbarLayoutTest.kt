package com.nuvio.app.features.player

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class PlayerToolbarLayoutTest {
    @Test
    fun defaultsPreserveExistingMewActionPills() {
        // luq's layout keeps six primary pills and moves the rest to the overflow; every action,
        // including Mew's subtitle sync and stream info, must still be reachable.
        val defaults = PlayerToolbarLayout.Default
        assertEquals(PlayerToolbarButton.entries.toSet(), defaults.entries.map(PlayerToolbarEntry::button).toSet())
        assertEquals(PlayerToolbarButton.entries.size, defaults.entries.size)
        assertEquals(
            listOf(
                PlayerToolbarButton.NEXT_EPISODE,
                PlayerToolbarButton.SPEED,
                PlayerToolbarButton.RESIZE,
                PlayerToolbarButton.SUBTITLES,
                PlayerToolbarButton.CHAPTERS,
                PlayerToolbarButton.NIGHT_MODE,
            ),
            defaults.visibleButtons,
        )
        assertEquals(true, PlayerToolbarButton.SUBTITLE_SYNC in defaults.hiddenButtons)
        assertEquals(true, PlayerToolbarButton.STREAM_INFO in defaults.hiddenButtons)
    }

    @Test
    fun encodeDecodePreservesVisibilityAndOrder() {
        val layout = PlayerToolbarLayout.Default
            .withVisibility(PlayerToolbarButton.STREAM_INFO, false)
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
    fun streamInfoVisibilityPersistsInToolbarLayout() {
        val layout = PlayerToolbarLayout.Default.withVisibility(PlayerToolbarButton.STREAM_INFO, false)
        assertEquals(false, layout.entries.first { it.button == PlayerToolbarButton.STREAM_INFO }.visible)
        assertEquals(layout, PlayerToolbarLayout.decode(layout.encode()))
    }

    @Test
    fun moveAndVisibilityReturnNewLayouts() {
        val defaults = PlayerToolbarLayout.Default
        assertNotEquals(defaults, defaults.withVisibility(PlayerToolbarButton.SPEED, false))
        assertNotEquals(defaults, defaults.moved(0, 1))
    }
}
