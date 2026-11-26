// Mew-only player controls extras (device status overlay, random-episode toggle,
// clock end time, extra toolbar actions). Upstream PlayerControls/PlayerControlActions
// call into this file through one-line `// Mew hook` calls.
package com.nuvio.app.features.player

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Forward10
import androidx.compose.material.icons.rounded.Forward30
import androidx.compose.material.icons.rounded.Forward5
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Replay10
import androidx.compose.material.icons.rounded.Replay30
import androidx.compose.material.icons.rounded.Replay5
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SignalCellularAlt
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.StayCurrentLandscape
import androidx.compose.material.icons.rounded.StayCurrentPortrait
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nuvio.app.core.ui.AppIconResource
import com.nuvio.app.core.ui.NuvioBackButton
import com.nuvio.app.core.ui.NuvioLoadingIndicator
import com.nuvio.app.core.ui.appIconPainter
import com.nuvio.app.core.ui.nuvioTypeScale
import kotlin.math.roundToLong
import nuvio.composeapp.generated.resources.*
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.player_toolbar_button_stream_info
import org.jetbrains.compose.resources.stringResource


@Composable
internal fun MewPlayerDeviceStatusOverlay(
    status: PlayerDeviceStatus,
    playbackSnapshot: PlayerPlaybackSnapshot,
    metrics: PlayerLayoutMetrics,
    modifier: Modifier = Modifier,
) {
    val typeScale = MaterialTheme.nuvioTypeScale
    val remainingLabel = playerFinishRemainingLabel(playbackSnapshot)
    val finishClockLabel = playerFinishClockLabel(status.timeLabel, playbackSnapshot)
    Surface(
        modifier = modifier.widthIn(min = 220.dp, max = 460.dp),
        shape = RoundedCornerShape(999.dp),
        color = Color.Black.copy(alpha = 0.34f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PlayerDeviceStatusItem(
                icon = when (status.networkType) {
                    PlayerDeviceNetworkType.Wifi -> Icons.Rounded.Wifi
                    PlayerDeviceNetworkType.Cellular -> Icons.Rounded.SignalCellularAlt
                    PlayerDeviceNetworkType.Offline -> Icons.Rounded.WifiOff
                    PlayerDeviceNetworkType.Unknown -> Icons.Rounded.Wifi
                },
                text = when (status.networkType) {
                    PlayerDeviceNetworkType.Wifi -> "Wi-Fi"
                    PlayerDeviceNetworkType.Cellular -> "LTE"
                    PlayerDeviceNetworkType.Offline -> "Offline"
                    PlayerDeviceNetworkType.Unknown -> "Net"
                },
                metrics = metrics,
            )
            Text(
                text = status.timeLabel,
                style = typeScale.labelSm.copy(
                    fontSize = metrics.metadataSize,
                    lineHeight = metrics.metadataSize * 1.2f,
                    fontWeight = FontWeight.SemiBold,
                ),
                color = Color.White.copy(alpha = 0.92f),
                maxLines = 1,
            )
            PlayerBatteryStatusItem(
                percent = status.batteryPercent,
                charging = status.batteryCharging,
                metrics = metrics,
            )
            if (remainingLabel != null) {
                PlayerDeviceStatusItem(
                    icon = Icons.Rounded.AccessTime,
                    text = finishClockLabel?.let { finishClock ->
                        stringResource(Res.string.player_status_ends_at, finishClock, remainingLabel)
                    } ?: stringResource(Res.string.player_status_time_left, remainingLabel),
                    metrics = metrics,
                )
            }
        }
    }
}

private fun playerFinishClockLabel(
    currentTimeLabel: String,
    snapshot: PlayerPlaybackSnapshot,
): String? {
    val durationMs = snapshot.durationMs.takeIf { it > 0L } ?: return null
    val remainingMs = (durationMs - snapshot.positionMs).coerceAtLeast(0L)
    if (remainingMs <= 0L) return null
    val speed = snapshot.playbackSpeed.takeIf { it > 0.05f } ?: 1f
    val adjustedMinutes = ((remainingMs / speed) / 60_000f).roundToLong().coerceAtLeast(1L)
    val clockMatch = Regex("""(\d{1,2})\D+(\d{2})""").find(currentTimeLabel) ?: return null
    val hour = clockMatch.groupValues.getOrNull(1)?.toIntOrNull() ?: return null
    val minute = clockMatch.groupValues.getOrNull(2)?.toIntOrNull() ?: return null
    val totalMinutes = ((hour * 60L + minute + adjustedMinutes) % (24L * 60L)).let { value ->
        if (value < 0L) value + 24L * 60L else value
    }
    val finishHour = (totalMinutes / 60L).toInt()
    val finishMinute = (totalMinutes % 60L).toInt()
    return "${finishHour.toString().padStart(2, '0')}:${finishMinute.toString().padStart(2, '0')}"
}

private fun playerFinishRemainingLabel(snapshot: PlayerPlaybackSnapshot): String? {
    val durationMs = snapshot.durationMs.takeIf { it > 0L } ?: return null
    val remainingMs = (durationMs - snapshot.positionMs).coerceAtLeast(0L)
    if (remainingMs <= 0L) return null
    val speed = snapshot.playbackSpeed.takeIf { it > 0.05f } ?: 1f
    val adjustedMinutes = ((remainingMs / speed) / 60_000f).roundToLong().coerceAtLeast(1L)
    val hours = adjustedMinutes / 60L
    val minutes = adjustedMinutes % 60L
    return when {
        hours <= 0L -> "${minutes}m"
        minutes <= 0L -> "${hours}h"
        else -> "${hours}h ${minutes}m"
    }
}

@Composable
private fun PlayerDeviceStatusItem(
    icon: ImageVector,
    text: String,
    metrics: PlayerLayoutMetrics,
) {
    val typeScale = MaterialTheme.nuvioTypeScale
    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.88f),
            modifier = Modifier.size(metrics.headerIconSize * 0.74f),
        )
        Text(
            text = text,
            style = typeScale.labelSm.copy(
                fontSize = metrics.metadataSize,
                lineHeight = metrics.metadataSize * 1.2f,
                fontWeight = FontWeight.SemiBold,
            ),
            color = Color.White.copy(alpha = 0.9f),
            maxLines = 1,
        )
    }
}

@Composable
private fun PlayerBatteryStatusItem(
    percent: Int?,
    charging: Boolean,
    metrics: PlayerLayoutMetrics,
) {
    val typeScale = MaterialTheme.nuvioTypeScale
    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PlayerBatteryGlyph(
            percent = percent,
            charging = charging,
            metrics = metrics,
        )
        Text(
            text = percent?.let { "$it%" } ?: "--",
            style = typeScale.labelSm.copy(
                fontSize = metrics.metadataSize,
                lineHeight = metrics.metadataSize * 1.2f,
                fontWeight = FontWeight.SemiBold,
            ),
            color = Color.White.copy(alpha = 0.9f),
            maxLines = 1,
        )
    }
}

@Composable
private fun PlayerBatteryGlyph(
    percent: Int?,
    charging: Boolean,
    metrics: PlayerLayoutMetrics,
) {
    val level = percent?.coerceIn(0, 100)
    val fillFraction = (level ?: 0) / 100f
    val activeColor = if (charging) {
        Color(0xFF8CFFB3)
    } else {
        Color.White.copy(alpha = 0.9f)
    }
    val outlineColor = Color.White.copy(alpha = 0.72f)
    val bodyWidth = metrics.headerIconSize * 0.86f
    val bodyHeight = metrics.headerIconSize * 0.46f
    val capWidth = metrics.headerIconSize * 0.08f
    val capHeight = metrics.headerIconSize * 0.24f

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        Box(
            modifier = Modifier
                .size(width = bodyWidth, height = bodyHeight)
                .clip(RoundedCornerShape(3.dp))
                .border(
                    width = 1.2.dp,
                    color = outlineColor,
                    shape = RoundedCornerShape(3.dp),
                )
                .padding(2.dp),
        ) {
            if (level != null) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fillFraction)
                        .clip(RoundedCornerShape(1.5.dp))
                        .background(activeColor),
                )
            }
        }
        Box(
            modifier = Modifier
                .size(width = capWidth, height = capHeight)
                .clip(RoundedCornerShape(topEnd = 1.5.dp, bottomEnd = 1.5.dp))
                .background(outlineColor),
        )
    }
}


/** Mew: quiet status (battery/clock/finish time) shown while controls are hidden, plus
 *  random-episode toggle and wall-clock end time shown under the toolbar. */
@Composable
internal fun BoxScope.MewPlayerControlsOverlays(
    showPlaybackControls: Boolean,
    showParentalGuide: Boolean,
    isLocked: Boolean,
    showDeviceStatusOverlay: Boolean,
    showClockEndTime: Boolean,
    randomNextEpisodeMode: Boolean,
    onRandomNextEpisodeModeToggle: (() -> Unit)?,
    playbackSnapshot: PlayerPlaybackSnapshot,
    metrics: PlayerLayoutMetrics,
) {
    val showQuiet = showDeviceStatusOverlay && !showPlaybackControls && !showParentalGuide && !isLocked
    if (showQuiet) {
        val deviceStatus = rememberPlayerDeviceStatus()
        MewPlayerDeviceStatusOverlay(
            status = deviceStatus,
            playbackSnapshot = playbackSnapshot,
            metrics = metrics,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .windowInsetsPadding(WindowInsets.safeContent.only(WindowInsetsSides.Top))
                .padding(top = metrics.verticalPadding / 4),
        )
    }
    if (showPlaybackControls && (showClockEndTime || onRandomNextEpisodeModeToggle != null)) {
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .windowInsetsPadding(playerToolbarTopInsets(metrics))
                .padding(
                    top = metrics.headerIconSize + 28.dp,
                    end = metrics.horizontalPadding,
                ),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (onRandomNextEpisodeModeToggle != null) {
                PlayerHeaderIconButton(
                    icon = Icons.Rounded.Shuffle,
                    contentDescription = stringResource(
                        if (randomNextEpisodeMode) Res.string.player_random_next_enabled else Res.string.player_random_next_disabled,
                    ),
                    buttonSize = metrics.headerIconSize + 16.dp,
                    iconSize = metrics.headerIconSize,
                    tint = if (randomNextEpisodeMode) MaterialTheme.colorScheme.primary else Color.White,
                    onClick = onRandomNextEpisodeModeToggle,
                )
            }
            if (showClockEndTime) {
                PlayerClockEndTimeOverlay(playbackSnapshot = playbackSnapshot)
            }
        }
    }
}

/** Mew: subtitle-sync and stream-info entries for the luq configurable toolbar. */
@Composable
internal fun MutableMap<PlayerToolbarButton, PlayerControlAction>.mewPlayerToolbarActions(
    onSubtitleSyncClick: (() -> Unit)?,
    onStreamInfoClick: () -> Unit,
) {
    onSubtitleSyncClick?.let {
        this[PlayerToolbarButton.SUBTITLE_SYNC] = PlayerControlAction(
            stringResource(Res.string.compose_player_sync_short), it,
            icon = Icons.Rounded.Sync,
        )
    }
    this[PlayerToolbarButton.STREAM_INFO] = PlayerControlAction(
        stringResource(Res.string.player_toolbar_button_stream_info), onStreamInfoClick,
        icon = Icons.Rounded.Info,
    )
}
