package com.nuvio.app.features.player

import androidx.media3.common.MimeTypes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PlaybackMediaItemsTest {
    @Test
    fun localPlaybackUrisAreRecognizedWithoutNetworkAccess() {
        assertTrue(isLocalPlaybackUri("file:///storage/emulated/0/Movies/video"))
        assertTrue(isLocalPlaybackUri("content://media/external/video/1"))
        assertFalse(isLocalPlaybackUri("https://example.com/video.mkv"))
        assertFalse(isLocalPlaybackUri("https://example.com/video.m3u8"))
    }

    @Test
    fun subtitleMimeProbeSkipsKnownExtensionsAndNonHttpSources() {
        assertFalse(shouldProbeSubtitleMimeType("https://example.com/subtitle.srt?token=abc"))
        assertFalse(shouldProbeSubtitleMimeType("https://example.com/subtitle.webvtt"))
        assertFalse(shouldProbeSubtitleMimeType("content://media/subtitle"))
        assertFalse(shouldProbeSubtitleMimeType("file:///storage/subtitle.srt"))
        assertTrue(shouldProbeSubtitleMimeType("https://example.com/subtitle?id=123"))
        assertTrue(shouldProbeSubtitleMimeType("http://example.com/get?id=123"))
    }

    @Test
    fun detectsMatroskaAndMp4FromContainerHeaders() {
        assertEquals(
            MimeTypes.VIDEO_MATROSKA,
            mimeTypeFromHeader(byteArrayOf(0x1A, 0x45, 0xDF.toByte(), 0xA3.toByte())),
        )
        val mp4 = ByteArray(12).also {
            "ftyp".toByteArray(Charsets.US_ASCII).copyInto(it, 4)
        }
        assertEquals(MimeTypes.VIDEO_MP4, mimeTypeFromHeader(mp4))
    }

    @Test
    fun detectsTransportStreamAviFlvAndOggHeaders() {
        val transportStream = ByteArray(189).also {
            it[0] = 0x47
            it[188] = 0x47
        }
        val avi = ByteArray(12).also {
            "RIFF".toByteArray(Charsets.US_ASCII).copyInto(it, 0)
            "AVI ".toByteArray(Charsets.US_ASCII).copyInto(it, 8)
        }
        assertEquals(MimeTypes.VIDEO_MP2T, mimeTypeFromHeader(transportStream))
        assertEquals(MimeTypes.VIDEO_AVI, mimeTypeFromHeader(avi))
        assertEquals(MimeTypes.VIDEO_FLV, mimeTypeFromHeader("FLV".toByteArray(Charsets.US_ASCII)))
        assertEquals("application/ogg", mimeTypeFromHeader("OggS".toByteArray(Charsets.US_ASCII)))
    }

    @Test
    fun unknownContainerHeaderReturnsNull() {
        assertNull(mimeTypeFromHeader("unknown".toByteArray(Charsets.US_ASCII)))
    }
}
