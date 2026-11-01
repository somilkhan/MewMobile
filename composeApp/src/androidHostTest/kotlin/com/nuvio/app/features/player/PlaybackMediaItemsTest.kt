package com.nuvio.app.features.player

import androidx.media3.common.MimeTypes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PlaybackMediaItemsTest {
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
    fun unknownContainerHeaderFallsBackToExtensionInference() {
        assertNull(mimeTypeFromHeader("unknown".toByteArray(Charsets.US_ASCII)))
    }
}
