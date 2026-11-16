package com.nuvio.app.features.autosync

import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class AutoSyncSubtitleCueParserTest {
    @Test
    fun parsesSrtAndIgnoresMalformedBlocks() {
        val cues = AutoSyncSubtitleCueParser.parse(
            text = """
                1
                00:00:01,000 --> 00:00:02,500
                <i>Hello</i> world

                malformed block

                2
                00:00:03,000 --> 00:00:04,000
                Second cue
            """.trimIndent(),
            sourceUrl = "https://subs.example/movie.srt?token=private",
        )

        assertEquals(2, cues.size)
        assertEquals(1_000L, cues[0].startTimeMs)
        assertEquals(2_500L, cues[0].endTimeMs)
        assertEquals("Hello world", cues[0].text)
        assertEquals("Second cue", cues[1].text)
    }

    @Test
    fun parsesWebVttCueSettingsAndEntities() {
        val cues = AutoSyncSubtitleCueParser.parse(
            text = """
                WEBVTT

                cue-1
                00:00:01.000 --> 00:00:02.500 align:start position:0%
                <v Narrator>Hello &amp; welcome</v>
            """.trimIndent(),
            sourceUrl = "https://subs.example/movie.vtt",
        )

        assertEquals(1, cues.size)
        assertEquals(1_000L, cues.single().startTimeMs)
        assertEquals(2_500L, cues.single().endTimeMs)
        assertEquals("Hello & welcome", cues.single().text)
    }

    @Test
    fun parsesAssDialogueAndPreservesCommasInText() {
        val cues = AutoSyncSubtitleCueParser.parse(
            text = """
                [Script Info]
                ScriptType: v4.00+
                [Events]
                Format: Layer, Start, End, Style, Name, MarginL, MarginR, MarginV, Effect, Text
                Dialogue: 0,0:00:01.00,0:00:02.50,Default,,0,0,0,,{\\an8}Top, with comma
            """.trimIndent(),
            sourceUrl = "https://subs.example/movie.ass",
        )

        assertEquals(1, cues.size)
        assertEquals(1_000L, cues.single().startTimeMs)
        assertEquals(2_500L, cues.single().endTimeMs)
        assertEquals("Top, with comma", cues.single().text)
    }

    @Test
    fun parsesTtmlBeginAndEndTimes() {
        val cues = AutoSyncSubtitleCueParser.parse(
            text = """<tt><body><div><p begin="00:00:01.000" end="00:00:02.500">Hello <span>there</span></p></div></body></tt>""",
            sourceUrl = "https://subs.example/movie.ttml",
        )

        assertEquals(1, cues.size)
        assertEquals(1_000L, cues.single().startTimeMs)
        assertEquals(2_500L, cues.single().endTimeMs)
        assertEquals("Hello there", cues.single().text)
    }

    @Test
    fun cancellationIsPropagatedBeforeParsing() {
        assertFailsWith<CancellationException> {
            AutoSyncSubtitleCueParser.parse(
                text = "1\\n00:00:01,000 --> 00:00:02,000\\nCaption",
                sourceUrl = "movie.srt",
                cancellationCheck = { throw CancellationException("cancelled") },
            )
        }
    }
}
