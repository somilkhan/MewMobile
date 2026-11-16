package com.nuvio.app.features.player

import androidx.media3.common.MimeTypes
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DolbyDecoderPolicyTest {
    @Test
    fun excludesDolbyNamedPlatformDecodersForAc3FamilyWhenExtensionsAreEnabled() {
        assertTrue(shouldExcludeDolbyPlatformDecoder(MimeTypes.AUDIO_AC3, "c2.dolby.ac3.decoder", true))
        assertTrue(shouldExcludeDolbyPlatformDecoder(MimeTypes.AUDIO_E_AC3, "c2.dolby.eac3.decoder", true))
        assertTrue(shouldExcludeDolbyPlatformDecoder(MimeTypes.AUDIO_E_AC3_JOC, "vendor.dolby.joc.decoder", true))
    }

    @Test
    fun preservesOtherDecodersAndMimeTypes() {
        assertFalse(shouldExcludeDolbyPlatformDecoder(MimeTypes.AUDIO_E_AC3, "c2.android.eac3.decoder", true))
        assertFalse(shouldExcludeDolbyPlatformDecoder(MimeTypes.AUDIO_AAC, "c2.dolby.aac.decoder", true))
    }

    @Test
    fun preservesPlatformDolbyDecoderWhenExtensionRenderersAreDisabled() {
        assertFalse(shouldExcludeDolbyPlatformDecoder(MimeTypes.AUDIO_E_AC3, "c2.dolby.eac3.decoder", false))
    }
}
