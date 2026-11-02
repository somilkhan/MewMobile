package com.nuvio.app.features.player

import com.nuvio.app.features.streams.StreamSubtitle
import kotlin.test.Test
import kotlin.test.assertEquals

class PlayerRuntimeAutoSyncCandidatesTest {
    @Test
    fun includesAddonAndCurrentStreamSubtitleCandidates() {
        val candidates = buildAutoSyncCandidates(
            addonSubtitles = listOf(AddonSubtitle("a", "https://addon/a.srt", "en", "Addon English")),
            streamSubtitles = listOf(StreamSubtitle("https://stream/en.vtt", "en", "Stream English")),
        )

        assertEquals(
            listOf(
                "https://addon/a.srt" to "Addon English",
                "https://stream/en.vtt" to "Stream English",
            ),
            candidates.map { it.url to it.name },
        )
    }

    @Test
    fun addonCandidateWinsWhenStreamSubtitleUrlIsDuplicated() {
        val candidates = buildAutoSyncCandidates(
            addonSubtitles = listOf(AddonSubtitle("a", "https://same/sub.srt", "en", "Addon label")),
            streamSubtitles = listOf(StreamSubtitle("https://same/sub.srt", "en", "Stream label")),
        )

        assertEquals(1, candidates.size)
        assertEquals("Addon label", candidates.single().name)
    }

    @Test
    fun handlesEmptyCandidateSources() {
        assertEquals(emptyList(), buildAutoSyncCandidates(emptyList(), emptyList()))
    }
    @Test
    fun lateAddonRetryRequiresAutoSyncStartupAndNoExplicitSelection() {
        fun shouldRetry(autoSync: Boolean = true, loading: Boolean = false, enabled: Boolean = true,
            addonLoading: Boolean = false, count: Int = 1, applied: Boolean = true,
            manual: Boolean = false, index: Int = -1, addonId: String? = null) =
            shouldRetryPreferredSubtitleAfterAddonLoad(
                isAutoSyncController = autoSync, playbackLoading = loading,
                startupAutoSyncEnabled = enabled, addonLoading = addonLoading,
                addonSubtitleCount = count, preferredSelectionApplied = applied,
                manualSelectionLocked = manual, selectedSubtitleIndex = index,
                selectedAddonSubtitleId = addonId,
            )

        assertEquals(true, shouldRetry())
        assertEquals(false, shouldRetry(autoSync = false))
        assertEquals(false, shouldRetry(loading = true))
        assertEquals(false, shouldRetry(enabled = false))
        assertEquals(false, shouldRetry(addonLoading = true))
        assertEquals(false, shouldRetry(count = 0))
        assertEquals(false, shouldRetry(applied = false))
        assertEquals(false, shouldRetry(manual = true))
        assertEquals(false, shouldRetry(index = 0))
        assertEquals(false, shouldRetry(addonId = "chosen"))
    }
}
