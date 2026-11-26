package com.nuvio.app.features.player

import androidx.media3.common.MimeTypes
import androidx.media3.exoplayer.DefaultLoadControl
import java.util.Locale

// Mew-only helpers kept outside PlayerEngine.android.kt so upstream engine updates merge cleanly.
// Covered by androidHostTest (ExternalSubtitleStyleRoutingTest, DolbyDecoderPolicyTest, PlaybackMediaItemsTest).

internal fun isAssSubtitleCodec(codec: String?): Boolean = when (codec?.trim()?.lowercase()) {
    "ass", "ssa" -> true
    else -> false
}

internal fun shouldExcludeDolbyPlatformDecoder(
    mimeType: String,
    decoderName: String,
    extensionRenderersEnabled: Boolean,
): Boolean =
    extensionRenderersEnabled &&
        (mimeType == MimeTypes.AUDIO_AC3 ||
            mimeType == MimeTypes.AUDIO_E_AC3 ||
            mimeType == MimeTypes.AUDIO_E_AC3_JOC) &&
        decoderName.contains("dolby", ignoreCase = true)

internal fun shouldProbeSubtitleMimeType(url: String): Boolean {
    val normalized = url.substringBefore('#').substringBefore('?').lowercase(Locale.US)
    if (!normalized.startsWith("http://") && !normalized.startsWith("https://")) return false
    val filename = normalized.substringAfterLast('/')
    val extension = filename.substringAfterLast('.', missingDelimiterValue = "")
    return extension !in setOf("srt", "vtt", "webvtt", "ass", "ssa", "ttml", "dfxp", "xml")
}

// Mew default load control used by PlaybackBuffering when custom buffers are off.
internal fun buildAndroidLoadControl(memorySafeBufferEnabled: Boolean): DefaultLoadControl =
    DefaultLoadControl.Builder().apply {
        if (memorySafeBufferEnabled) {
            setTargetBufferBytes(48 * 1024 * 1024)
            setBufferDurationsMs(
                10_000,
                40_000,
                DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_MS,
                4_000,
            )
            setBackBuffer(0, false)
            setPrioritizeTimeOverSizeThresholds(false)
        } else {
            setTargetBufferBytes(100 * 1024 * 1024)
            setBufferDurationsMs(
                15_000,
                70_000,
                DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_MS,
                5_000,
            )
        }
    }.build()
