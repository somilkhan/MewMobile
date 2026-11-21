package com.nuvio.app.features.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nuvio.app.core.ui.nuvio
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.mini_player_close
import nuvio.composeapp.generated.resources.mini_player_expand
import org.jetbrains.compose.resources.stringResource

/**
 * Playback state the full player hands off when it is left while the mini player is enabled.
 * The video engine is not kept alive: reopening the player resumes from [positionMs].
 */
internal data class MiniPlayerItem(
    val launch: PlayerLaunch,
    val positionMs: Long,
    val durationMs: Long,
    val paused: Boolean,
)

internal object MiniPlayerController {
    var item: MiniPlayerItem? by mutableStateOf(null)
        private set

    val isActive: Boolean
        get() = item != null

    fun handOff(item: MiniPlayerItem) {
        this.item = item
    }

    fun close() {
        item = null
    }
}

/** Hands the current playback state to the mini player when the setting is on. No-op otherwise. */
internal fun PlayerScreenRuntime.handOffToMiniPlayerIfEnabled() {
    val launch = args.miniPlayerLaunch ?: return
    if (!PlayerSettingsRepository.uiState.value.miniPlayerEnabled) return
    val snapshot = playbackSnapshot
    MiniPlayerController.handOff(
        MiniPlayerItem(
            launch = launch,
            positionMs = snapshot.positionMs,
            durationMs = snapshot.durationMs,
            paused = !snapshot.isPlaying,
        ),
    )
}

private val MiniPlayerShape = RoundedCornerShape(14.dp)
private val MiniPlayerInfoHeight = 56.dp
private val MiniPlayerProgressHeight = 2.dp
private val MiniPlayerBottomClearance = 96.dp

/**
 * Floating card shown above the app while a player was handed off and no player route is on top.
 * [onExpand] receives a launch that resumes at the handed-off position.
 */
@Composable
internal fun MiniPlayerHost(
    playerOnTop: Boolean,
    onExpand: (PlayerLaunch) -> Unit,
) {
    val item = MiniPlayerController.item ?: return
    if (playerOnTop) return
    val colors = MaterialTheme.nuvio.colors
    val launch = item.launch
    val reopen = {
        MiniPlayerController.close()
        onExpand(launch.copy(initialPositionMs = item.positionMs, initialProgressFraction = null))
    }
    val episodeLabel = if (launch.seasonNumber != null && launch.episodeNumber != null) {
        "S${launch.seasonNumber} E${launch.episodeNumber}"
    } else {
        null
    }
    val subtitle = listOfNotNull(
        episodeLabel,
        launch.episodeTitle?.takeIf { it.isNotBlank() },
        if (item.paused) "Paused" else null,
    ).joinToString(" · ").takeIf { it.isNotEmpty() }
    val progress = if (item.durationMs > 0L) {
        (item.positionMs.toFloat() / item.durationMs.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 12.dp, end = 12.dp, bottom = MiniPlayerBottomClearance),
        contentAlignment = Alignment.BottomEnd,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth()
                .shadow(16.dp, MiniPlayerShape)
                .clip(MiniPlayerShape)
                .background(colors.surfaceElevated),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(MiniPlayerProgressHeight)
                    .background(Color.White.copy(alpha = 0.12f)),
            ) {
                if (progress > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .fillMaxHeight()
                            .background(colors.accent),
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(MiniPlayerInfoHeight)
                    .padding(start = 12.dp, end = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClickLabel = stringResource(Res.string.mini_player_expand)) { reopen() },
                ) {
                    Text(
                        text = launch.title,
                        color = colors.textPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            color = colors.textMuted,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                IconButton(onClick = { MiniPlayerController.close() }) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = stringResource(Res.string.mini_player_close),
                        tint = colors.textPrimary,
                    )
                }
            }
        }
    }
}
