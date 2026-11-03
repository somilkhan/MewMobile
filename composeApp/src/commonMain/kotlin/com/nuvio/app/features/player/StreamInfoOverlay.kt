package com.nuvio.app.features.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.player_stream_info_title
import nuvio.composeapp.generated.resources.player_stream_info_provider
import nuvio.composeapp.generated.resources.player_stream_info_stream
import nuvio.composeapp.generated.resources.player_stream_info_description
import nuvio.composeapp.generated.resources.player_stream_info_addon_id
import nuvio.composeapp.generated.resources.player_stream_info_filename
import nuvio.composeapp.generated.resources.player_stream_info_hash
import nuvio.composeapp.generated.resources.player_stream_info_engine
import nuvio.composeapp.generated.resources.player_stream_info_type
import nuvio.composeapp.generated.resources.player_stream_info_resolution
import nuvio.composeapp.generated.resources.player_stream_info_codec
import nuvio.composeapp.generated.resources.player_stream_info_fps
import nuvio.composeapp.generated.resources.player_stream_info_bitrate
import nuvio.composeapp.generated.resources.player_stream_info_hdr
import nuvio.composeapp.generated.resources.player_stream_info_audio_codec
import nuvio.composeapp.generated.resources.player_stream_info_audio_channels
import nuvio.composeapp.generated.resources.player_stream_info_audio_rate
import org.jetbrains.compose.resources.stringResource
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
internal fun StreamInfoOverlay(
    visible: Boolean,
    provider: String,
    streamTitle: String,
    streamSubtitle: String?,
    addonId: String?,
    filename: String?,
    infoHash: String?,
    engine: AndroidPlaybackEngine?,
    streamType: String?,
    resolution: String?,
    videoCodec: String?,
    videoFrameRate: Float?,
    videoBitrate: Int?,
    audioCodec: String?,
    audioChannels: Int?,
    audioSampleRate: Int?,
    hdrFormat: String?,
    onDismiss: () -> Unit,
) {
    PlayerOverlayScaffold(
        visible = visible,
        onDismiss = onDismiss,
        contentPadding = PaddingValues(horizontal = 32.dp, vertical = 24.dp),
    ) {
        Column(
            modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth(.72f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text("STREAM INFORMATION", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = .6f), fontWeight = FontWeight.Bold)
            InfoLine(stringResource(Res.string.player_stream_info_provider), provider)
            InfoLine(stringResource(Res.string.player_stream_info_stream), streamTitle)
            InfoLine(stringResource(Res.string.player_stream_info_description), streamSubtitle)
            InfoLine(stringResource(Res.string.player_stream_info_addon_id), addonId)
            InfoLine(stringResource(Res.string.player_stream_info_filename), filename)
            InfoLine(stringResource(Res.string.player_stream_info_hash), infoHash)
            InfoLine(stringResource(Res.string.player_stream_info_engine), engine?.label)
            InfoLine(stringResource(Res.string.player_stream_info_type), streamType?.takeIf(String::isNotBlank))
            InfoLine(stringResource(Res.string.player_stream_info_resolution), resolution)
            InfoLine(stringResource(Res.string.player_stream_info_codec), videoCodec)
            InfoLine(stringResource(Res.string.player_stream_info_fps), videoFrameRate?.let(::formatFrameRate))
            InfoLine(stringResource(Res.string.player_stream_info_bitrate), videoBitrate?.let { "${it / 1_000} Kbps" })
            InfoLine(stringResource(Res.string.player_stream_info_hdr), hdrFormat)
            InfoLine(stringResource(Res.string.player_stream_info_audio_codec), audioCodec)
            InfoLine(stringResource(Res.string.player_stream_info_audio_channels), audioChannels?.let { "$it ch" })
            InfoLine(stringResource(Res.string.player_stream_info_audio_rate), audioSampleRate?.let(::formatSampleRate))
        }
    }
}

private fun formatFrameRate(frameRate: Float): String {
    val hundredths = (frameRate * 100f + 0.5f).toInt()
    return "${hundredths / 100}.${(hundredths % 100).toString().padStart(2, '0')} fps"
}

private fun formatSampleRate(sampleRate: Int): String {
    val tenths = (sampleRate + 50) / 100
    return "${tenths / 10}.${tenths % 10} kHz"
}

@Composable
private fun InfoLine(label: String, value: String?) {
    if (value.isNullOrBlank()) return
    Column(verticalArrangement = Arrangement.spacedBy(3.dp), modifier = Modifier.fillMaxWidth()) {
        Text(label, color = Color.White.copy(alpha = .58f), style = MaterialTheme.typography.labelMedium)
        Text(value, color = Color.White, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
    }
}
