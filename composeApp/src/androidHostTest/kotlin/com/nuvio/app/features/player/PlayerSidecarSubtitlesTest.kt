package com.nuvio.app.features.player

import androidx.media3.common.MimeTypes
import androidx.media3.common.text.Cue
import androidx.media3.extractor.text.CuesWithTiming
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PlayerSidecarSubtitlesTest {
    @Test
    fun `URL MIME detection ignores query and fragment`() {
        assertEquals(MimeTypes.APPLICATION_SUBRIP, PlayerSubtitleUtils.mimeTypeFromUrl("https://cdn/sub.SRT?token=1"))
        assertEquals(MimeTypes.TEXT_VTT, PlayerSubtitleUtils.mimeTypeFromUrl("https://cdn/sub.webvtt#track"))
        assertEquals(MimeTypes.TEXT_SSA, PlayerSubtitleUtils.mimeTypeFromUrl("https://cdn/sub.ass?x=1#y"))
        assertEquals(MimeTypes.TEXT_SSA, PlayerSubtitleUtils.mimeTypeFromUrl("https://cdn/sub.ssa"))
        assertEquals(MimeTypes.APPLICATION_TTML, PlayerSubtitleUtils.mimeTypeFromUrl("https://cdn/sub.ttml"))
        assertEquals(MimeTypes.APPLICATION_TTML, PlayerSubtitleUtils.mimeTypeFromUrl("https://cdn/sub.dfxp"))
        assertEquals(MimeTypes.APPLICATION_SUBRIP, PlayerSubtitleUtils.mimeTypeFromUrl("https://cdn/subtitle"))
    }

    @Test
    fun `body sniff overrides misleading URL extension`() {
        assertEquals(MimeTypes.TEXT_VTT, PlayerSubtitleUtils.sniffSubtitleMimeType(vtt, "sub.srt"))
        assertEquals(MimeTypes.TEXT_SSA, PlayerSubtitleUtils.sniffSubtitleMimeType(ass, "sub.vtt"))
        assertEquals(MimeTypes.APPLICATION_TTML, PlayerSubtitleUtils.sniffSubtitleMimeType(ttml, "sub.srt"))
        assertEquals(MimeTypes.APPLICATION_SUBRIP, PlayerSubtitleUtils.sniffSubtitleMimeType(srt, "sub.vtt"))
    }

    @Test
    fun `candidate ordering prefers sniff then URL and removes duplicates`() {
        val candidates = PlayerSubtitleUtils.sidecarMimeCandidates(vtt, "sub.ass")

        assertEquals(MimeTypes.TEXT_VTT, candidates[0])
        assertEquals(MimeTypes.TEXT_SSA, candidates[1])
        assertEquals(candidates.distinct(), candidates)
        assertEquals(
            setOf(
                MimeTypes.APPLICATION_SUBRIP,
                MimeTypes.TEXT_VTT,
                MimeTypes.TEXT_SSA,
                MimeTypes.APPLICATION_TTML,
            ),
            candidates.toSet(),
        )
    }

    @Test
    fun `robust parser falls back safely for SRT and VTT on host`() {
        assertParsedCue(srt, "sub.srt", MimeTypes.APPLICATION_SUBRIP, "SRT cue")
        assertParsedCue(vtt, "sub.vtt", MimeTypes.TEXT_VTT, "VTT cue")
    }

    @Test
    fun `ASS and SSA never use the lossy lenient fallback`() {
        assertStructuredFormatUsesMedia3OrNone(ass, "sub.ass", MimeTypes.TEXT_SSA)
        assertStructuredFormatUsesMedia3OrNone(ssa, "sub.ssa", MimeTypes.TEXT_SSA)
    }

    @Test
    fun `TTML and DFXP never use the lossy lenient fallback`() {
        assertStructuredFormatUsesMedia3OrNone(ttml, "sub.ttml", MimeTypes.APPLICATION_TTML)
        assertStructuredFormatUsesMedia3OrNone(ttml, "sub.dfxp", MimeTypes.APPLICATION_TTML)
    }

    @Test
    fun `cursor emits only when active cues change and rewinds on backward seek`() {
        val first = Cue.Builder().setText("first").build()
        val second = Cue.Builder().setText("second").build()
        val timeline = listOf(
            CuesWithTiming(listOf(first), 0L, 1_000_000L),
            CuesWithTiming(listOf(second), 1_000_000L, 1_000_000L),
        )
        val cursor = SidecarCueCursor()

        assertEquals(listOf("first"), cursor.activeCuesOrNull(timeline, 500_000L)?.map { it.text?.toString() })
        assertNull(cursor.activeCuesOrNull(timeline, 600_000L))
        assertEquals(listOf("second"), cursor.activeCuesOrNull(timeline, 1_500_000L)?.map { it.text?.toString() })
        assertEquals(listOf("first"), cursor.activeCuesOrNull(timeline, 500_000L)?.map { it.text?.toString() })
    }

    @Test
    fun `cursor safely scans unsorted timelines`() {
        val first = Cue.Builder().setText("first").build()
        val future = Cue.Builder().setText("future").build()
        val unsortedTimeline = listOf(
            CuesWithTiming(listOf(future), 1_000_000L, 1_000_000L),
            CuesWithTiming(listOf(first), 0L, 1_000_000L),
        )

        val active = SidecarCueCursor().activeCuesOrNull(unsortedTimeline, 500_000L)
        assertEquals(listOf("first"), active?.map { it.text?.toString() })
    }

    @Test
    fun `generation invalidates stale requests including same URL restart`() {
        val generation = SidecarGeneration()
        val first = generation.next()
        val second = generation.next()

        assertFalse(generation.isCurrent(first))
        assertTrue(generation.isCurrent(second))
        generation.invalidate()
        assertFalse(generation.isCurrent(second))
    }

    private fun assertParsedCue(raw: String, url: String, mime: String, expectedText: String) {
        val result = parseSidecarTimedCuesRobust(raw, url)

        assertEquals(mime, result.effectiveMime)
        assertTrue(result.cues.isNotEmpty())
        assertTrue(result.cues.any { entry -> entry.cues.any { it.text?.contains(expectedText) == true } })
    }

    private fun assertStructuredFormatUsesMedia3OrNone(raw: String, url: String, mime: String) {
        val result = parseSidecarTimedCuesRobust(raw, url)

        assertEquals(mime, result.effectiveMime)
        assertTrue(result.source == "media3" || result.source == "none")
    }

    private companion object {
        val srt = """
            1
            00:00:01,000 --> 00:00:03,000
            SRT cue

        """.trimIndent()

        val vtt = """
            WEBVTT

            00:01.000 --> 00:03.000
            VTT cue

        """.trimIndent()

        val ass = """
            [Script Info]
            ScriptType: v4.00+

            [V4+ Styles]
            Format: Name, Fontname, Fontsize, PrimaryColour, SecondaryColour, OutlineColour, BackColour, Bold, Italic, Underline, StrikeOut, ScaleX, ScaleY, Spacing, Angle, BorderStyle, Outline, Shadow, Alignment, MarginL, MarginR, MarginV, Encoding
            Style: Default,Arial,20,&H00FFFFFF,&H000000FF,&H00000000,&H00000000,0,0,0,0,100,100,0,0,1,2,0,2,10,10,10,1

            [Events]
            Format: Layer, Start, End, Style, Name, MarginL, MarginR, MarginV, Effect, Text
            Dialogue: 0,0:00:01.00,0:00:03.00,Default,,0,0,0,,ASS cue
        """.trimIndent()

        val ssa = """
            [Script Info]
            ScriptType: v4.00

            [V4 Styles]
            Format: Name, Fontname, Fontsize, PrimaryColour, SecondaryColour, TertiaryColour, BackColour, Bold, Italic, BorderStyle, Outline, Shadow, Alignment, MarginL, MarginR, MarginV, AlphaLevel, Encoding
            Style: Default,Arial,20,&HFFFFFF,&HFFFF00,&H000000,&H000000,0,0,1,2,0,2,10,10,10,0,1

            [Events]
            Format: Marked, Start, End, Style, Name, MarginL, MarginR, MarginV, Effect, Text
            Dialogue: Marked=0,0:00:01.00,0:00:03.00,Default,,0,0,0,,SSA cue
        """.trimIndent()

        val ttml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <tt xmlns="http://www.w3.org/ns/ttml">
              <body><div><p begin="00:00:01.000" end="00:00:03.000">TTML cue</p></div></body>
            </tt>
        """.trimIndent()
    }

    @Test
    fun subtitleDelayAdjustsSidecarPlaybackClockInBothDirections() {
        assertEquals(9_500_000L, sidecarSubtitlePositionUs(positionMs = 10_000L, delayMs = 500))
        assertEquals(10_500_000L, sidecarSubtitlePositionUs(positionMs = 10_000L, delayMs = -500))
        assertEquals(0L, sidecarSubtitlePositionUs(positionMs = 100L, delayMs = 500))
    }
}
