package com.nuvio.app.features.streams

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FilenameEpisodeMatcherTest {
    @Test
    fun buildsNextEpisodeFilenameAndPreservesZeroPadding() {
        assertEquals("Black Clover S02E04 1080p.mkv",
            FilenameEpisodeMatcher.buildNextFilename("Black Clover S02E03 1080p.mkv", 2, 3, 2, 4))
    }

    @Test
    fun supportsSeasonXEpisodeFilename() {
        assertEquals("Show 02x04 WEB-DL.mkv",
            FilenameEpisodeMatcher.buildNextFilename("Show 02x03 WEB-DL.mkv", 2, 3, 2, 4))
    }

    @Test
    fun supportsEpisodeOnlyFilename() {
        assertEquals("Anime EP 004.mkv",
            FilenameEpisodeMatcher.buildNextFilename("Anime EP 003.mkv", null, 3, null, 4))
    }

    @Test
    fun rejectsMismatchedCurrentEpisode() {
        assertNull(FilenameEpisodeMatcher.buildNextFilename("Show S01E08.mkv", 1, 3, 1, 4))
    }

    @Test
    fun matchesFilenameCaseInsensitively() {
        assertTrue(FilenameEpisodeMatcher.matches("Show S01E04.mkv", "show s01e04.MKV"))
        assertFalse(FilenameEpisodeMatcher.matches("Show S01E04.mkv", "Show S01E05.mkv"))
        assertFalse(FilenameEpisodeMatcher.matches(null, "Show S01E04.mkv"))
    }

    @Test
    fun matchesEpisodeIdentityAcrossQualitySuffixes() {
        assertTrue(FilenameEpisodeMatcher.matchesEpisode("Black Clover S02E04 720p.mkv", "Black Clover S02E04 1080p.mkv"))
        assertFalse(FilenameEpisodeMatcher.matchesEpisode("Black Clover S01E04.mkv", "Black Clover S02E04.mkv"))
        assertFalse(FilenameEpisodeMatcher.matchesEpisode("Black Clover S02E03.mkv", "Black Clover S02E04.mkv"))
    }
}
