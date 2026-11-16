package com.nuvio.app.features.player

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class SubtitleUrlRedactionTest {
    @Test
    fun removesCredentialsQueryAndFragment() {
        val redacted = redactSubtitleUrlForLogs(
            "https://user:password@cdn.example/subtitles/show.srt?token=secret&expires=123#caption"
        )

        assertEquals("https://cdn.example/subtitles/show.srt", redacted)
        assertFalse(redacted.contains("password"))
        assertFalse(redacted.contains("secret"))
        assertFalse(redacted.contains("caption"))
    }

    @Test
    fun malformedUrlFailsClosed() {
        assertEquals("<redacted-url>", redactSubtitleUrlForLogs("not a valid url?token=secret"))
    }
}
