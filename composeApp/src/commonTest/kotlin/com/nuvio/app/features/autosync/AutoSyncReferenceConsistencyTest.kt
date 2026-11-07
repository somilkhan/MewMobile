package com.nuvio.app.features.autosync

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AutoSyncReferenceConsistencyTest {
    private val base = irregularTimeline(300)

    @Test
    fun mistimedTrackIsDroppedWhenIndependentSourcesAgree() {
        val references = listOf(
            reference("english", base),
            reference("french", independentAuthoring(base, seed = 3)),
            reference("german", independentAuthoring(base, seed = 5)),
            reference("mistimed", shift(base, 6_000L)),
        )

        assertEquals(setOf("mistimed"), AutoSyncReferenceConsistency.findOutliers(references))
    }

    @Test
    fun correlatedCopiesCannotOutvoteAnIndependentTrack() {
        val chinese = shift(base, 3_700L)
        val references = listOf(
            reference("english", base),
            reference("chs", chinese),
            reference("cht", chinese),
            reference("chs-eng", chinese),
        )

        assertTrue(AutoSyncReferenceConsistency.findOutliers(references).isEmpty())
    }

    @Test
    fun agreeingTracksAreAllKept() {
        val references = listOf(
            reference("english", base),
            reference("french", independentAuthoring(base, seed = 3)),
            reference("german", independentAuthoring(base, seed = 5)),
        )

        assertTrue(AutoSyncReferenceConsistency.findOutliers(references).isEmpty())
    }

    private fun reference(key: String, cues: List<AutoSyncSubtitleCue>) =
        AutoSyncReferenceConsistency.Reference(
            key = key,
            activity = assertNotNull(AutoSyncTimelineRetimer.prepareUnitActivity(cues)),
            cueCount = cues.size,
        )

    /** Same dialogue, different author: per-line start jitter and different durations. */
    private fun independentAuthoring(cues: List<AutoSyncSubtitleCue>, seed: Int) =
        cues.mapIndexed { index, cue ->
            val jitterMs = (((index * seed * 37) % 7) - 3) * 90L
            val start = cue.startTimeMs + jitterMs
            AutoSyncSubtitleCue(start, start + 700L + ((index * seed * 131) % 1_900), cue.text)
        }

    private fun shift(cues: List<AutoSyncSubtitleCue>, deltaMs: Long) = cues.map { cue ->
        cue.copy(startTimeMs = cue.startTimeMs + deltaMs, endTimeMs = cue.endTimeMs + deltaMs)
    }

    private fun irregularTimeline(count: Int): List<AutoSyncSubtitleCue> {
        var start = 30_000L
        return (0 until count).map { index ->
            if (index > 0) start += 1_400L + ((index * 977L) % 4_300L)
            AutoSyncSubtitleCue(start, start + 900L + ((index * 313L) % 1_700L), "irregular $index")
        }
    }
}
