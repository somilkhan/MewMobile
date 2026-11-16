package com.nuvio.app.features.player

import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@UnstableApi
class CustomDefaultTrackNameProviderTest {
    @Test
    fun media3CueTracksDoNotExposeInternalMimeAsCodecName() {
        assertNull(
            CustomDefaultTrackNameProvider.resolveCodecName(
                MimeTypes.APPLICATION_MEDIA3_CUES,
                null,
            ),
        )
    }

    @Test
    fun codecStringsResolveToReadableTrackNames() {
        assertEquals(
            "HEVC",
            CustomDefaultTrackNameProvider.resolveCodecName(
                MimeTypes.VIDEO_H265,
                "hvc1.2.4.L153.B0",
            ),
        )
    }

    @Test
    fun subtitleMimeTypesKeepReadableLabels() {
        assertEquals("SRT", CustomDefaultTrackNameProvider.resolveCodecName(MimeTypes.APPLICATION_SUBRIP, null))
        assertEquals("VTT", CustomDefaultTrackNameProvider.resolveCodecName(MimeTypes.TEXT_VTT, null))
    }
}