package com.nuvio.app.features.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nuvio.app.features.settings.NuvioAudioSelectorStyle
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.compose_player_audio_amplification
import nuvio.composeapp.generated.resources.compose_player_audio_amplification_keep
import nuvio.composeapp.generated.resources.compose_player_audio_delay
import nuvio.composeapp.generated.resources.compose_player_audio_tracks
import nuvio.composeapp.generated.resources.compose_player_center_mix
import nuvio.composeapp.generated.resources.compose_player_no_audio_tracks_available
import nuvio.composeapp.generated.resources.compose_player_reset
import org.jetbrains.compose.resources.stringResource
import kotlin.math.abs

private const val AUDIO_DELAY_HOLD_STEP_MS = 50
private const val AUDIO_DELAY_HOLD_FAST_STEP_MS = 100
private const val AUDIO_DELAY_HOLD_FAST_AFTER_MS = 2_000L
private val AUDIO_PANEL_SIDE_BY_SIDE_MIN_WIDTH = 760.dp

@Composable
internal fun AudioTrackModal(
    visible: Boolean,
    audioTracks: List<AudioTrack>,
    selectedIndex: Int,
    selectorStyle: NuvioAudioSelectorStyle,
    onTrackSelected: (Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    showAudioDelay: Boolean = false,
    audioDelayMs: Int = 0,
    onAudioDelayChanged: (Int) -> Unit = {},
    showAudioAmplification: Boolean = false,
    audioAmplificationDb: Int = 0,
    keepAudioAmplification: Boolean = false,
    onAudioAmplificationChanged: (Int) -> Unit = {},
    onKeepAudioAmplificationChanged: (Boolean) -> Unit = {},
    showCenterMix: Boolean = false,
    centerMixLevelDb: Int = 0,
    onCenterMixLevelChanged: (Int) -> Unit = {},
) {
    if (selectorStyle == NuvioAudioSelectorStyle.Nuvio) {
        NuvioAudioTrackModal(
            visible = visible,
            audioTracks = audioTracks,
            selectedIndex = selectedIndex,
            onTrackSelected = onTrackSelected,
            onDismiss = onDismiss,
            modifier = modifier,
        )
        return
    }

    val colorScheme = MaterialTheme.colorScheme

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(200)),
        exit = fadeOut(tween(200)),
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onDismiss,
                )
                .background(colorScheme.scrim.copy(alpha = 0.52f)),
            contentAlignment = Alignment.Center,
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(tween(300)) { it / 3 } + fadeIn(tween(300)),
                exit = slideOutVertically(tween(250)) { it / 3 } + fadeOut(tween(250)),
            ) {
                Box(
                    modifier = Modifier
                        .widthIn(max = 420.dp)
                        .fillMaxWidth(0.9f)
                        .heightIn(max = 600.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(colorScheme.surface)
                        .border(1.dp, colorScheme.outlineVariant.copy(alpha = 0.8f), RoundedCornerShape(24.dp))
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = {},
                        ),
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(Res.string.compose_player_audio_tracks),
                                color = colorScheme.onSurface,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }

                        Column(
                            modifier = Modifier
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 20.dp)
                            .padding(bottom = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            if (audioTracks.isEmpty()) {
                                AudioEmptyState()
                            } else {
                                audioTracks.forEachIndexed { idx, track ->
                                    AudioTrackRow(
                                        track = track,
                                        isSelected = track.index == selectedIndex,
                                        onClick = { onTrackSelected(track.index) },
                                    )
                                }
                            }
                        }
                    }
                    adjustments()
                }
            }
        }
    }
}

@Composable
private fun AudioTracksHeader() {
    Text(
        text = stringResource(Res.string.compose_player_audio_tracks),
        color = Color.White,
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier.padding(bottom = 8.dp),
    )
}

@Composable
private fun NoAudioTracksText() {
    Text(
        text = stringResource(Res.string.compose_player_no_audio_tracks_available),
        color = Color.White.copy(alpha = 0.7f),
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
    )
}

@Composable
private fun CenterMixSection(
    levelDb: Int,
    onLevelChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentDb = levelDb.coerceIn(CENTER_MIX_LEVEL_MIN_DB, CENTER_MIX_LEVEL_MAX_DB)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = stringResource(Res.string.compose_player_center_mix),
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SubtitleStyleStepper(
                value = if (currentDb > 0) "+$currentDb dB" else "$currentDb dB",
                onDecrease = { onLevelChanged(currentDb - 1) },
                onIncrease = { onLevelChanged(currentDb + 1) },
            )
            SubtitleTextAction(
                label = stringResource(Res.string.compose_player_reset),
                enabled = currentDb != 0,
                onClick = { onLevelChanged(0) },
            )
        }
    }
}

@Composable
private fun AudioAmplificationSection(
    gainDb: Int,
    keep: Boolean,
    onGainChanged: (Int) -> Unit,
    onKeepChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentDb = gainDb.coerceIn(AUDIO_AMPLIFICATION_MIN_DB, AUDIO_AMPLIFICATION_MAX_DB)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = stringResource(Res.string.compose_player_audio_amplification),
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SubtitleStyleStepper(
                value = if (currentDb > 0) "+$currentDb dB" else "$currentDb dB",
                onDecrease = { onGainChanged(currentDb - 1) },
                onIncrease = { onGainChanged(currentDb + 1) },
            )
            AudioToggleChip(
                label = stringResource(Res.string.compose_player_audio_amplification_keep),
                checked = keep,
                onCheckedChange = onKeepChanged,
            )
        }
    }
}

@Composable
private fun AudioToggleChip(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val tokens = MaterialTheme.nuvio
    val contentColor = if (checked) tokens.colors.onAccent else Color.White

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (checked) tokens.colors.accent else Color.White.copy(alpha = 0.08f))
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp),
            )
        }
        Text(
            text = label,
            color = contentColor,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun AudioDelaySection(
    delayMs: Int,
    onDelayChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentDelayMs = delayMs.coerceIn(AUDIO_DELAY_MIN_MS, AUDIO_DELAY_MAX_MS)
    // Holding a button steps faster the longer it is held.
    fun holdStepMs(heldMs: Long): Int =
        if (heldMs >= AUDIO_DELAY_HOLD_FAST_AFTER_MS) AUDIO_DELAY_HOLD_FAST_STEP_MS else AUDIO_DELAY_HOLD_STEP_MS

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = stringResource(Res.string.compose_player_audio_delay),
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SubtitleStyleStepper(
                value = formatAudioDelay(currentDelayMs),
                onDecrease = { onDelayChanged(currentDelayMs - AUDIO_DELAY_STEP_MS) },
                onIncrease = { onDelayChanged(currentDelayMs + AUDIO_DELAY_STEP_MS) },
                onDecreaseHold = { heldMs -> onDelayChanged(currentDelayMs - holdStepMs(heldMs)) },
                onIncreaseHold = { heldMs -> onDelayChanged(currentDelayMs + holdStepMs(heldMs)) },
                valueWidth = 96.dp,
            )
            SubtitleTextAction(
                label = stringResource(Res.string.compose_player_reset),
                enabled = currentDelayMs != 0,
                onClick = { onDelayChanged(0) },
            )
        }
    }
}

private fun formatAudioDelay(delayMs: Int): String {
    val sign = when {
        delayMs > 0 -> "+"
        delayMs < 0 -> "-"
        else -> ""
    }
    val absoluteMs = abs(delayMs)
    return "$sign${absoluteMs / 1000}.${(absoluteMs % 1000).toString().padStart(3, '0')}s"
}

@Composable
private fun AudioTrackRow(
    track: AudioTrack,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val bgColor = if (isSelected) colorScheme.primaryContainer else colorScheme.surfaceVariant.copy(alpha = 0.6f)
    val textColor = if (isSelected) colorScheme.onPrimaryContainer else colorScheme.onSurface
    val weight = if (isSelected) FontWeight.Bold else FontWeight.Normal

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = localizedTrackDisplayName(track.label, track.language, track.index),
            color = textColor,
            fontSize = 15.sp,
            fontWeight = weight,
        )
        if (isSelected) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = colorScheme.primary,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun AudioEmptyState() {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.VolumeOff,
            contentDescription = null,
            tint = colorScheme.onSurfaceVariant,
            modifier = Modifier
                .size(32.dp)
                .then(Modifier),
        )
        Text(
            text = stringResource(Res.string.compose_player_no_audio_tracks_available),
            color = colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 10.dp),
        )
    }
}
