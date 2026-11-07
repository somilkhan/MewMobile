package com.nuvio.app.features.watchprogress

import kotlin.test.Test
import kotlin.test.assertEquals

class ContinueWatchingPreferencesRepositoryTest {
    @Test
    fun markingContentWatchedClearsExactAndEpisodeSpecificDismissals() {
        val dismissed = setOf(
            "show-1",
            "show-1|s1e2",
            "show-1|s2e4",
            "show-10|s1e2",
            "other-show|s1e1",
        )

        assertEquals(
            setOf("show-10|s1e2", "other-show|s1e1"),
            filterDismissedNextUpKeysForContent(dismissed, " show-1 "),
        )
    }

    @Test
    fun blankContentIdDoesNotClearDismissals() {
        val dismissed = setOf("show-1", "show-1|s1e2")

        assertEquals(dismissed, filterDismissedNextUpKeysForContent(dismissed, "   "))
    }
}
