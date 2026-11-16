package com.nuvio.app.features.autosync

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AutoSyncRunPolicyTest {
    @Test
    fun startupAutoSyncRespectsThePreference() {
        assertFalse(AutoSyncCandidateScope.STARTUP_SEARCH.shouldRunAutoSync(false))
        assertTrue(AutoSyncCandidateScope.STARTUP_SEARCH.shouldRunAutoSync(true))
    }

    @Test
    fun explicitSubtitleSelectionRunsEvenWhenStartupAutoSyncIsDisabled() {
        assertTrue(AutoSyncCandidateScope.SELECTED_ONLY.shouldRunAutoSync(false))
    }

    @Test
    fun startupSearchMayUseAlternativeSubtitlesButExplicitSelectionMayNot() {
        val candidates = listOf(
            AutoSyncSubtitleCandidate(url = "https://example.test/a.srt", language = "en"),
            AutoSyncSubtitleCandidate(url = "https://example.test/b.srt", language = "en"),
        )

        assertEquals(candidates, AutoSyncCandidateScope.STARTUP_SEARCH.alternativeCandidates(candidates))
        assertTrue(AutoSyncCandidateScope.SELECTED_ONLY.alternativeCandidates(candidates).isEmpty())
        assertTrue(AutoSyncCandidateScope.STARTUP_SEARCH.usesAlternativeProvider)
        assertFalse(AutoSyncCandidateScope.SELECTED_ONLY.usesAlternativeProvider)
    }

    @Test
    fun originalSubtitleIsRestoredOnlyWhenNoSidecarIsActive() {
        assertTrue(shouldRestoreOriginalSubtitle(null))
        assertFalse(shouldRestoreOriginalSubtitle("https://example.test/subtitle.srt"))
    }
}
