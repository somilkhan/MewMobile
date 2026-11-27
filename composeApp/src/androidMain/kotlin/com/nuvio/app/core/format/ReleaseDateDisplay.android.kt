package com.nuvio.app.core.format

import android.text.format.DateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

internal actual fun formatCalendarDate(isoDate: String, localeTag: String, includeYear: Boolean): String {
    val locale = Locale.forLanguageTag(localeTag)
    val skeleton = if (includeYear) "dMMMMy" else "dMMMM"
    val pattern = bestDateTimePatternOrNull(locale, skeleton)
        ?: if (includeYear) "d MMMM y" else "d MMMM"
    return DateTimeFormatter.ofPattern(pattern, locale).format(LocalDate.parse(isoDate))
}

/**
 * android.text.format.DateFormat is a framework stub on JVM host tests, so fall back to a
 * locale-neutral skeleton there instead of crashing.
 */
private fun bestDateTimePatternOrNull(locale: Locale, skeleton: String): String? =
    try {
        DateFormat.getBestDateTimePattern(locale, skeleton)
    } catch (_: RuntimeException) {
        null
    }
