package com.nuvio.app.features.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nuvio.app.core.ui.nuvio
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.mini_player_close
import nuvio.composeapp.generated.resources.mini_player_expand
import nuvio.composeapp.generated.resources.mini_player_pause
import nuvio.composeapp.generated.resources.mini_player_play
import org.jetbrains.compose.resources.stringResource

/**
 * Owns the launch whose live engine is kept alive while the full player is left.
 *
 * The engine lives in [MiniPlayerHost], above the navigation host, so leaving the player route
 * does not dispose the runtime. Identity is the [PlayerLaunch] object, so expanding the card
 * re-pushes the same launch and reuses the same engine without restarting playback.
 */
internal object MiniPlayerController {
    var hostedLaunch: PlayerLaunch? by mutableStateOf(null)
        private set

    var minimized: Boolean by mutableStateOf(false)
        private set

    var hostedRuntime: PlayerScreenRuntime? by mutableStateOf(null)
        private set

    val isActive: Boolean
        get() = minimized && hostedLaunch != null

    /** Runs after each composition with the launch of the top player route, if any. */
    fun commit(topLaunch: PlayerLaunch?, enabled: Boolean) {
        if (!enabled) {
            if (hostedLaunch != null || minimized) reset()
            return
        }
        if (topLaunch != null) {
            if (topLaunch !== hostedLaunch) {
                hostedLaunch = topLaunch
                minimized = false
            }
        } else if (!minimized) {
            hostedLaunch = null
        }
    }

    fun requestMinimize(): Boolean {
        if (hostedLaunch == null) return false
        minimized = true
        return true
    }

    /** Returns the launch to reopen full screen, or null when nothing is hosted. */
    fun expand(): PlayerLaunch? {
        val launch = hostedLaunch ?: return null
        minimized = false
        return launch
    }

    fun close() {
        hostedRuntime?.flushWatchProgress()
        reset()
    }

    private fun reset() {
        minimized = false
        hostedLaunch = null
    }

    fun attach(runtime: PlayerScreenRuntime) {
        hostedRuntime = runtime
    }

    fun detach(runtime: PlayerScreenRuntime) {
        if (hostedRuntime === runtime) hostedRuntime = null
    }
}

/** Called before leaving the full player. Keeps the engine alive as a mini card when enabled. */
internal fun PlayerScreenRuntime.handOffToMiniPlayerIfEnabled() {
    if (!PlayerSettingsRepository.uiState.value.miniPlayerEnabled) return
    if (isMiniPresentation) return
    if (MiniPlayerController.hostedRuntime !== this) return
    MiniPlayerController.requestMinimize()
}

private val MiniPlayerShape = RoundedCornerShape(14.dp)
private val MiniPlayerInfoHeight = 52.dp
private val MiniPlayerProgressHeight = 2.dp
private val MiniPlayerEdgeMargin = 12.dp

/**
 * Hosts the live player above the navigation host. In full mode it covers the screen. When the
 * player is minimized it shows a card with the live video and a bar of controls, placed above the
 * bottom navigation bar. [content] renders PlayerScreen; its composition position stays fixed so
 * the video surface is not recreated when switching between the two modes.
 */
@Composable
internal fun MiniPlayerHost(
    topLaunch: PlayerLaunch?,
    enabled: Boolean,
    bottomBarHeightPx: Int,
    onExpand: (PlayerLaunch) -> Unit,
    content: @Composable (launch: PlayerLaunch, modifier: Modifier, miniPresentation: Boolean) -> Unit,
) {
    SideEffect { MiniPlayerController.commit(topLaunch, enabled) }
    if (!enabled) return

    val hosted = MiniPlayerController.hostedLaunch
    val minimized = MiniPlayerController.minimized
    val isNewTop = topLaunch != null && topLaunch !== hosted
    val activeLaunch = when {
        isNewTop -> topLaunch
        minimized -> hosted
        topLaunch != null -> topLaunch
        else -> null
    } ?: return
    val mini = !isNewTop && minimized

    val density = LocalDensity.current
    val bottomClearance = with(density) {
        maxOf(bottomBarHeightPx, WindowInsets.safeDrawing.getBottom(density)).toDp()
    } + MiniPlayerEdgeMargin

    key(System.identityHashCode(activeLaunch)) {
        MiniPlayerFrame(
            launch = activeLaunch,
            mini = mini,
            bottomClearance = bottomClearance,
            onExpand = {
                MiniPlayerController.expand()?.let(onExpand)
            },
            content = content,
        )
    }
}

@Composable
private fun MiniPlayerFrame(
    launch: PlayerLaunch,
    mini: Boolean,
    bottomClearance: Dp,
    onExpand: () -> Unit,
    content: @Composable (launch: PlayerLaunch, modifier: Modifier, miniPresentation: Boolean) -> Unit,
) {
    val colors = MaterialTheme.nuvio.colors
    val runtime = MiniPlayerController.hostedRuntime
    val snapshot = runtime?.playbackSnapshot
    val durationMs = snapshot?.durationMs ?: 0L
    val progressFraction = if (snapshot != null && durationMs > 0L) {
        (snapshot.positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
    val playing = snapshot != null &&
        (snapshot.isPlaying || (runtime?.shouldPlay == true && snapshot.isLoading))
    val subtitle = if (launch.seasonNumber != null && launch.episodeNumber != null) {
        listOfNotNull(
            "S${launch.seasonNumber} E${launch.episodeNumber}",
            launch.episodeTitle?.takeIf { it.isNotBlank() },
        ).joinToString(" · ")
    } else {
        launch.streamSubtitle?.takeIf { it.isNotBlank() }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val cardWidth = when {
            !mini -> maxWidth
            maxWidth >= 600.dp -> 320.dp
            else -> maxWidth * 0.58f
        }
        val videoHeight = cardWidth * 9f / 16f

        Column(
            modifier = if (mini) {
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = MiniPlayerEdgeMargin, bottom = bottomClearance)
                    .width(cardWidth)
                    .shadow(16.dp, MiniPlayerShape)
                    .clip(MiniPlayerShape)
                    .background(colors.surfaceElevated)
            } else {
                Modifier.fillMaxSize()
            },
        ) {
            // Stays the first child in both modes so the player keeps its composition position.
            Box(
                modifier = if (mini) {
                    Modifier
                        .fillMaxWidth()
                        .height(videoHeight)
                } else {
                    Modifier.fillMaxSize()
                },
            ) {
                content(launch, Modifier.fillMaxSize(), mini)
                if (mini) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(onClickLabel = stringResource(Res.string.mini_player_expand)) {
                                onExpand()
                            },
                    )
                    if (snapshot?.isLoading == true && runtime?.errorMessage == null) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .height(26.dp),
                            color = Color.White,
                            strokeWidth = 2.dp,
                        )
                    }
                }
            }

            if (mini) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(MiniPlayerProgressHeight)
                        .background(Color.White.copy(alpha = 0.12f)),
                ) {
                    if (durationMs > 0L) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progressFraction)
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
                            .clickable { onExpand() },
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
                    IconButton(onClick = { runtime?.togglePlayback() }) {
                        Icon(
                            imageVector = if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = stringResource(
                                if (playing) Res.string.mini_player_pause else Res.string.mini_player_play,
                            ),
                            tint = colors.textPrimary,
                        )
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
}
