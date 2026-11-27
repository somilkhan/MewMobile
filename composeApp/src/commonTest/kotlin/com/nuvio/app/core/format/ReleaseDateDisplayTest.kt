package com.nuvio.app.core.format

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReleaseDateDisplayTest {
    @Test
    fun formatsIsoDate() {
        assertLocalizedDate(formatReleaseDate("2025-02-01", includeYear = true, localeTag = "en-US"), "February", "1", "2025")
    }

    @Test
    fun stripsTimePortion() {
        assertLocalizedDate(formatReleaseDate("2024-01-15T12:30:00Z", includeYear = true, localeTag = "en-US"), "January", "15", "2024")
    }

    @Test
    fun leavesYearOnlyUnchanged() {
        assertEquals("2024", formatReleaseDateForDisplay("2024"))
    }

    @Test
    fun leavesNonIsoUnchanged() {
        assertEquals("TBA", formatReleaseDateForDisplay("TBA"))
    }

    @Test
    fun extractsYearFromIso() {
        assertEquals(2025, extractReleaseYearForDisplay("2025-03-15"))
    }

    @Test
    fun extractsYearFromYearOnly() {
        assertEquals(2024, extractReleaseYearForDisplay("2024"))
    }

    @Test
    fun formatsIsoDateWithoutYear() {
        assertLocalizedDate(formatReleaseDate("2025-02-01", includeYear = false, localeTag = "en-US"), "February", "1", year = null)
    }

    @Test
    fun formatReleaseDateWithoutYearStripsTimePortion() {
        assertLocalizedDate(formatReleaseDate("2024-01-15T12:30:00Z", includeYear = false, localeTag = "en-US"), "January", "15", year = null)
    }

    @Test
    fun formatReleaseDateWithoutYearLeavesYearOnlyUnchanged() {
        assertEquals("2024", formatReleaseDateWithoutYear("2024"))
    }

    @Test
    fun formatReleaseDateWithoutYearLeavesNonIsoUnchanged() {
        assertEquals("TBA", formatReleaseDateWithoutYear("TBA"))
    }

    // Release dates follow the device locale (luq 1.1.0), so assert the parts rather than one exact layout.
    private fun assertLocalizedDate(formatted: String, month: String, day: String, year: String?) {
        assertTrue(month in formatted, formatted)
        assertTrue(Regex("\\b$day\\b").containsMatchIn(formatted), formatted)
        if (year != null) assertTrue(year in formatted, formatted) else assertFalse(Regex("\\b\\d{4}\\b").containsMatchIn(formatted), formatted)
        assertFalse(':' in formatted, formatted)
    }
}
