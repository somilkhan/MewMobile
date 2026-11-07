package com.nuvio.app.features.player

import com.nuvio.app.features.streams.StreamAutoPlayMode
import kotlin.test.Test
import kotlinx.coroutines.Job
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PlayerNextEpisodeAutoPlayPolicyTest {
    @Test
    fun manualAutoPlayCanRestrictToPreferredBingeGroupWhenFallbackIsOff() {
        assertTrue(
            shouldRestrictNextEpisodeAutoPlayToBingeGroup(
                mode = StreamAutoPlayMode.MANUAL,
                autoPlayNextEpisodeEnabled = true,
                fallbackEnabled = false,
                preferBingeGroup = true,
            ),
        )
    }

    @Test
    fun manualAutoPlayAllowsAllProviderStreamsWhenFallbackIsEnabled() {
        assertFalse(
            shouldRestrictNextEpisodeAutoPlayToBingeGroup(
                mode = StreamAutoPlayMode.MANUAL,
                autoPlayNextEpisodeEnabled = true,
                fallbackEnabled = true,
                preferBingeGroup = true,
            ),
        )
    }

    @Test
    fun bingeGroupPreferenceAloneRestrictsManualSelection() {
        assertTrue(
            shouldRestrictNextEpisodeAutoPlayToBingeGroup(
                mode = StreamAutoPlayMode.MANUAL,
                autoPlayNextEpisodeEnabled = false,
                fallbackEnabled = true,
                preferBingeGroup = true,
            ),
        )
    }

    @Test
    fun nonManualModesNeverApplyManualBingeGroupRestriction() {
        assertFalse(
            shouldRestrictNextEpisodeAutoPlayToBingeGroup(
                mode = StreamAutoPlayMode.FIRST_STREAM,
                autoPlayNextEpisodeEnabled = true,
                fallbackEnabled = false,
                preferBingeGroup = true,
            ),
        )
    }

    @Test
    fun cancellingPreviousSearchResetsUiBeforeDownloadedEpisodeFastPath() {
        val previous = Job()
        var searching = true
        var sourceName: String? = "stale source"
        var countdown: Int? = 2

        cancelPreviousNextEpisodeSearch(
            previousJob = previous,
            onSearchingChanged = { searching = it },
            onSourceNameChanged = { sourceName = it },
            onCountdownChanged = { countdown = it },
        )

        assertTrue(previous.isCancelled)
        assertFalse(searching)
        assertEquals(null, sourceName)
        assertEquals(null, countdown)
    }
}
