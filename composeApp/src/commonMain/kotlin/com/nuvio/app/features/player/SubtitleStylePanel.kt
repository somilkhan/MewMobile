package com.nuvio.app.features.player

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Hearing
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nuvio.app.core.ui.nuvio
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.compose_player_auto_sync
import nuvio.composeapp.generated.resources.compose_player_capture_line
import nuvio.composeapp.generated.resources.compose_player_loading_lines
import nuvio.composeapp.generated.resources.compose_player_no_subtitle_lines_found
import nuvio.composeapp.generated.resources.compose_player_reload
import nuvio.composeapp.generated.resources.compose_player_reset
import nuvio.composeapp.generated.resources.compose_player_select_addon_subtitle_first
import nuvio.composeapp.generated.resources.compose_player_style
import nuvio.composeapp.generated.resources.compose_player_subtitle_delay
import nuvio.composeapp.generated.resources.compose_player_sync_by_ear
import nuvio.composeapp.generated.resources.subtitle_reset_appearance
import org.jetbrains.compose.resources.stringResource
import com.nuvio.app.core.ui.isTvLayoutProfileEnabled
import kotlin.math.abs
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

private const val STEPPER_HOLD_START_MS = 500L
private const val STEPPER_HOLD_REPEAT_MS = 100L

@Composable
fun SubtitleStylePanel(
    style: SubtitleStyleState,
    isCompact: Boolean,
    onStyleChanged: ((SubtitleStyleState) -> SubtitleStyleState) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val useTvLayout = isTvLayoutProfileEnabled()
    val uiScale = if (useTvLayout) 1.2f else 1f
    val density = LocalDensity.current
    val sectionPadding = (if (isCompact) 12.dp else 16.dp) * uiScale
    val gap = (if (isCompact) 12.dp else 16.dp) * uiScale

    CompositionLocalProvider(
        LocalDensity provides Density(density.density, density.fontScale * uiScale),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(gap),
        ) {
            StyleControlsCard(
                style = style,
                isCompact = isCompact,
                sectionPadding = sectionPadding,
                colorScheme = colorScheme,
                uiScale = uiScale,
                onStyleChanged = onStyleChanged,
            )
        }
    }
}

@Composable
fun SubtitleSyncPanel(
    subtitleDelayMs: Int,
    selectedAddonSubtitle: AddonSubtitle?,
    subtitleAutoSyncState: SubtitleAutoSyncUiState,
    currentPlaybackPositionMs: Long,
    isCompact: Boolean,
    isPlaying: Boolean,
    onSubtitleDelayChanged: (Int) -> Unit,
    onSubtitleDelayReset: () -> Unit,
    onSyncByEarClick: () -> Unit,
    onAutoSyncCapture: () -> Unit,
    onSyncByEarClick: () -> Unit,
    onAutoSyncCueSelected: (SubtitleSyncCue) -> Unit,
    onAutoSyncReload: () -> Unit,
    onTogglePlayback: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val useTvLayout = isTvLayoutProfileEnabled()
    val uiScale = if (useTvLayout) 1.2f else 1f
    val density = LocalDensity.current
    val sectionPadding = (if (isCompact) 12.dp else 16.dp) * uiScale
    val gap = (if (isCompact) 12.dp else 16.dp) * uiScale

    CompositionLocalProvider(
        LocalDensity provides Density(density.density, density.fontScale * uiScale),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(gap),
        ) {
            SyncControlsCard(
                subtitleDelayMs = subtitleDelayMs,
                selectedAddonSubtitle = selectedAddonSubtitle,
                subtitleAutoSyncState = subtitleAutoSyncState,
                currentPlaybackPositionMs = currentPlaybackPositionMs,
                isCompact = isCompact,
                isPlaying = isPlaying,
                sectionPadding = sectionPadding,
                colorScheme = colorScheme,
                uiScale = uiScale,
                onSubtitleDelayChanged = onSubtitleDelayChanged,
                onSubtitleDelayReset = onSubtitleDelayReset,
                onAutoSyncCapture = onAutoSyncCapture,
                onSyncByEarClick = onSyncByEarClick,
                onAutoSyncCueSelected = onAutoSyncCueSelected,
                onAutoSyncReload = onAutoSyncReload,
                onTogglePlayback = onTogglePlayback,
            )
        }
    }
}

@Composable
private fun SyncControlsCard(
    subtitleDelayMs: Int,
    selectedAddonSubtitle: AddonSubtitle?,
    subtitleAutoSyncState: SubtitleAutoSyncUiState,
    currentPlaybackPositionMs: Long,
    isCompact: Boolean,
    isPlaying: Boolean,
    sectionPadding: androidx.compose.ui.unit.Dp,
    colorScheme: androidx.compose.material3.ColorScheme,
    uiScale: Float,
    onSubtitleDelayChanged: (Int) -> Unit,
    onSubtitleDelayReset: () -> Unit,
    onAutoSyncCapture: () -> Unit,
    onSyncByEarClick: () -> Unit,
    onAutoSyncCueSelected: (SubtitleSyncCue) -> Unit,
    onAutoSyncReload: () -> Unit,
    onTogglePlayback: () -> Unit,
) {
    val btnSize = (if (isCompact) 28.dp else 32.dp) * uiScale
    val btnRadius = (if (isCompact) 14.dp else 16.dp) * uiScale

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colorScheme.surfaceVariant.copy(alpha = 0.45f))
            .padding(sectionPadding),
        verticalArrangement = Arrangement.spacedBy(if (isCompact) 12.dp else 16.dp),
    ) {
        SectionHeader(
            icon = Icons.Rounded.Tune,
            label = stringResource(Res.string.compose_player_sync_short),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(Res.string.compose_player_subtitle_delay),
                color = colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            )
            StepperControl(
                value = formatSubtitleDelay(subtitleDelayMs),
                onMinus = {
                    onSubtitleDelayChanged((subtitleDelayMs - SUBTITLE_DELAY_STEP_MS).coerceAtLeast(SUBTITLE_DELAY_MIN_MS))
                },
                onPlus = {
                    onSubtitleDelayChanged((subtitleDelayMs + SUBTITLE_DELAY_STEP_MS).coerceAtMost(SUBTITLE_DELAY_MAX_MS))
                },
                buttonSize = btnSize,
                buttonRadius = btnRadius,
                minWidth = 72.dp,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
        ) {
            SmallActionPill(
                text = stringResource(Res.string.compose_player_reset),
                onClick = onSubtitleDelayReset,
            )
            SubtitleSyncByEarAction(
                label = stringResource(Res.string.compose_player_sync_by_ear),
                onClick = onSyncByEarClick,
            )
        }

        SubtitleAppearanceControls(
            style = style,
            spacing = sectionGap,
            onStyleChanged = onStyleChanged,
        )

        SubtitleAutoSyncSection(
            selectedAddonSubtitle = selectedAddonSubtitle,
            state = subtitleAutoSyncState,
            onCapture = onAutoSyncCapture,
            onCueSelected = onAutoSyncCueSelected,
            onReload = onAutoSyncReload,
        )

        Text(
            text = stringResource(Res.string.subtitle_reset_appearance),
            modifier = Modifier.clip(RoundedCornerShape(12.dp))
                .clickable { onStyleChanged(style.resetAppearance()) }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
internal fun SubtitleStyleSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = title,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
        )
        content()
    }
}

/**
 * [onDecreaseHold] and [onIncreaseHold], when set, repeat while the button is held and get how
 * long it has been held; releasing after a repeat does not add a tap.
 */
@Composable
internal fun SubtitleStyleStepper(
    value: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    valueWidth: Dp = 84.dp,
    enabled: Boolean = true,
    onDecreaseHold: ((heldMs: Long) -> Unit)? = null,
    onIncreaseHold: ((heldMs: Long) -> Unit)? = null,
) {
    Row(
        modifier = Modifier.alpha(if (enabled) 1f else 0.42f),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SubtitleStepperButton(
            icon = Icons.Rounded.Remove,
            onClick = onDecrease,
            enabled = enabled,
            onHold = onDecreaseHold,
        )
        Box(
            modifier = Modifier
                .widthIn(min = valueWidth)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.06f))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            val currentAlphaPercent = (style.textColor.alpha * 100f).roundToInt().coerceIn(0, 100)
            Text(
                text = stringResource(Res.string.compose_player_text_opacity),
                color = colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            )
            StepperControl(
                value = "$currentAlphaPercent%",
                onMinus = {
                    val newAlpha = (currentAlphaPercent - 10).coerceAtLeast(0) / 100f
                    onStyleChanged { current -> current.copy(textColor = current.textColor.copy(alpha = newAlpha)) }
                },
                onPlus = {
                    val newAlpha = (currentAlphaPercent + 10).coerceAtMost(100) / 100f
                    onStyleChanged { current -> current.copy(textColor = current.textColor.copy(alpha = newAlpha)) }
                },
                buttonSize = btnSize,
                buttonRadius = btnRadius,
                minWidth = 58.dp,
            )
        }
        SubtitleStepperButton(
            icon = Icons.Rounded.Add,
            onClick = onIncrease,
            enabled = enabled,
            onHold = onIncreaseHold,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(colorScheme.surface.copy(alpha = 0.82f))
                    .border(1.dp, colorScheme.outlineVariant.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                    .clickable { onStyleChanged { SubtitleStyleState.DEFAULT } }
                    .padding(horizontal = if (isCompact) 8.dp else 12.dp, vertical = if (isCompact) 6.dp else 8.dp),
            ) {
                Text(
                    text = stringResource(Res.string.compose_player_reset_defaults),
                    color = colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = if (isCompact) 12.sp else 14.sp,
                )
            }
        }
    }
}

@Composable
private fun SubtitleFontFamilyChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean,
    onHold: ((heldMs: Long) -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    // True while a hold is repeating, so the release that ends it is not also counted as a tap.
    // The click runs before the release recomposes and cancels the hold, which clears it.
    val holdRepeated = remember { BooleanArray(1) }
    if (onHold != null) {
        val isPressed by interactionSource.collectIsPressedAsState()
        val latestOnHold by rememberUpdatedState(onHold)
        LaunchedEffect(isPressed, enabled) {
            if (!isPressed || !enabled) return@LaunchedEffect
            try {
                delay(STEPPER_HOLD_START_MS)
                var heldMs = STEPPER_HOLD_START_MS
                while (isActive) {
                    holdRepeated[0] = true
                    latestOnHold(heldMs)
                    delay(STEPPER_HOLD_REPEAT_MS)
                    heldMs += STEPPER_HOLD_REPEAT_MS
                }
            } finally {
                holdRepeated[0] = false
            }
        }
    }

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                enabled = enabled,
            ) {
                if (holdRepeated[0]) holdRepeated[0] = false else onClick()
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = if (selected) colorScheme.onPrimaryContainer else colorScheme.onSurface,
            fontSize = if (isCompact) 12.sp else 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
            maxLines = 1,
        )
    }
}

private fun SubtitleFontFamily.displayLabel(): String =
    when (this) {
        SubtitleFontFamily.System -> "System"
        SubtitleFontFamily.SansSerif -> "Sans"
        SubtitleFontFamily.Serif -> "Serif"
        SubtitleFontFamily.Monospace -> "Mono"
        SubtitleFontFamily.Rounded -> "Rounded"
        SubtitleFontFamily.Custom -> "Custom"
    }

@Composable
private fun SubtitleAutoSyncSection(
    selectedAddonSubtitle: AddonSubtitle?,
    state: SubtitleAutoSyncUiState,
    subtitleDelayMs: Int,
    currentPlaybackPositionMs: Long,
    isCompact: Boolean,
    isPlaying: Boolean,
    onCapture: () -> Unit,
    onCueSelected: (SubtitleSyncCue) -> Unit,
    onReload: () -> Unit,
    onTogglePlayback: () -> Unit,
) {
    val tokens = MaterialTheme.nuvio
    val capturedPositionMs = state.capturedPositionMs
    val cueListState = rememberLazyListState()
    val highlightedCueIndex = capturedPositionMs?.let { captured ->
        state.cues.indices.minByOrNull { index -> abs(state.cues[index].startTimeMs - captured) }
    } ?: -1

    LaunchedEffect(highlightedCueIndex, state.cues.size) {
        if (highlightedCueIndex >= 0) {
            cueListState.animateScrollToItem((highlightedCueIndex - 2).coerceAtLeast(0))
        }
    }
    val subtitlePositionMs = (currentPlaybackPositionMs - subtitleDelayMs).coerceAtLeast(0L)
    val activeCueIndex = sortedCues.indexOf(activeSubtitleSyncCue(sortedCues, subtitlePositionMs))
    val cueListState = rememberLazyListState()
    var followActiveCue by remember { mutableStateOf(true) }
    var autoScrollInProgress by remember { mutableStateOf(false) }

    LaunchedEffect(cueListState) {
        snapshotFlow { cueListState.isScrollInProgress }.collect { isScrolling ->
            if (isScrolling && !autoScrollInProgress) {
                followActiveCue = false
            } else if (!isScrolling && !autoScrollInProgress && !followActiveCue) {
                delay(900)
                val activeItem = cueListState.layoutInfo.visibleItemsInfo
                    .firstOrNull { it.index == activeCueIndex }
                val viewportCenter = (
                    cueListState.layoutInfo.viewportStartOffset +
                        cueListState.layoutInfo.viewportEndOffset
                    ) / 2
                val itemCenter = activeItem?.let { it.offset + it.size / 2 }
                if (itemCenter != null && abs(itemCenter - viewportCenter) <= 96) {
                    followActiveCue = true
                }
            }
        }
    }

        when {
            selectedAddonSubtitle == null -> {
                SubtitleHelperText(stringResource(Res.string.compose_player_select_addon_subtitle_first))
            }
            state.isLoading -> {
                SubtitleHelperText(stringResource(Res.string.compose_player_loading_lines))
            }
            state.errorMessage != null -> {
                Text(
                    text = state.errorMessage,
                    color = tokens.colors.danger,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            capturedPositionMs != null && state.cues.isEmpty() -> {
                SubtitleHelperText(stringResource(Res.string.compose_player_no_subtitle_lines_found))
            }
        }

        if (state.cues.isNotEmpty()) {
            LazyColumn(
                state = cueListState,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                itemsIndexed(state.cues) { index, cue ->
                    val isHighlighted = index == highlightedCueIndex
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isHighlighted) Color.White.copy(alpha = 0.14f)
                                else Color.White.copy(alpha = 0.06f)
                            )
                            .clickable { onCueSelected(cue) }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Text(
                            text = formatCueTimestamp(cue.startTimeMs),
                            color = if (isHighlighted) Color.White else tokens.colors.accent,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = cue.text,
                            modifier = Modifier.weight(1f),
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

internal fun activeSubtitleSyncCue(
    sortedCues: List<SubtitleSyncCue>,
    positionMs: Long,
): SubtitleSyncCue? {
    for (index in sortedCues.indices.reversed()) {
        val cue = sortedCues[index]
        if (cue.startTimeMs > positionMs) continue

        val nextStartTimeMs = sortedCues.getOrNull(index + 1)?.startTimeMs
        val endTimeMs = cue.endTimeMs
            ?.takeIf { it > cue.startTimeMs }
            ?: nextStartTimeMs?.takeIf { it > cue.startTimeMs }
            ?: (cue.startTimeMs + 10_000L)
        if (positionMs < endTimeMs) return cue
    }
    return null
}

internal suspend fun androidx.compose.foundation.lazy.LazyListState.animateSubtitleCueToCenter(
    cueIndex: Int,
) {
    if (cueIndex < 0) return

    // Use a relative offset whenever the cue is laid out. This keeps the active
    // row anchored and moves the whole track in the correct direction.
    repeat(8) {
        if (layoutInfo.totalItemsCount <= cueIndex) {
            yield()
        }
    }

    if (layoutInfo.visibleItemsInfo.none { it.index == cueIndex }) {
        // This is only a fallback for large seeks where the new cue is outside
        // the viewport. The final correction below still establishes the exact
        // center position after the item has been laid out.
        animateScrollToItem(cueIndex)
    }

    repeat(3) {
        val item = layoutInfo.visibleItemsInfo.firstOrNull { it.index == cueIndex } ?: return
        val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
        val itemCenter = item.offset + item.size / 2
        val delta = (itemCenter - viewportCenter).toFloat()
        if (abs(delta) < 1f) return
        animateScrollBy(delta, animationSpec = androidx.compose.animation.core.tween(360))
        yield()
    }
}

internal fun androidx.compose.ui.Modifier.subtitleSyncManualScroll(
    onManualScrollStarted: () -> Unit,
): androidx.compose.ui.Modifier = pointerInput(Unit) {
    awaitEachGesture {
        awaitFirstDown(requireUnconsumed = false)
        onManualScrollStarted()
    }
}

@Composable
internal fun SubtitleTextAction(
    label: String,
    enabled: Boolean,
    onToggle: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
        )
        SmallActionPill(
            text = if (enabled) stringResource(Res.string.compose_action_on)
            else stringResource(Res.string.compose_action_off),
            selected = enabled,
            onClick = onToggle,
        )
    }
}

@Composable
private fun SubtitleSyncByEarAction(
    label: String,
    colors: List<Color>,
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = label,
            color = colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            colors.forEach { color ->
                val isSelected = selectedColor == color
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(if (color.alpha == 0f) colorScheme.surface else color)
                        .border(
                            2.dp,
                            if (isSelected) colorScheme.primary else colorScheme.outlineVariant,
                            CircleShape,
                        )
                        .clickable { onColorSelected(color) },
                )
            }
        }
    }
}

@Composable
private fun SmallActionPill(
    text: String,
    enabled: Boolean = true,
    selected: Boolean = false,
    onClick: () -> Unit,
) {
    val tokens = MaterialTheme.nuvio

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(tokens.colors.accent.copy(alpha = 0.16f))
            .border(
                width = 1.dp,
                color = tokens.colors.accent.copy(alpha = 0.45f),
                shape = RoundedCornerShape(14.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Rounded.Hearing,
            contentDescription = null,
            tint = tokens.colors.accent,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = label,
            color = Color.White,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun StepperControl(
    value: String,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    buttonSize: androidx.compose.ui.unit.Dp,
    buttonRadius: androidx.compose.ui.unit.Dp,
    minWidth: androidx.compose.ui.unit.Dp = 42.dp,
    minusIcon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Rounded.Remove,
    plusIcon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Rounded.KeyboardArrowUp,
) {
    val colorScheme = MaterialTheme.colorScheme

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(buttonSize)
                .clip(RoundedCornerShape(buttonRadius))
                .background(colorScheme.primaryContainer)
                .clickable(onClick = onMinus),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = minusIcon,
                contentDescription = null,
                tint = colorScheme.onPrimaryContainer,
                modifier = Modifier.size(16.dp),
            )
        }

        Box(
            modifier = Modifier
                .widthIn(min = minWidth)
                .clip(RoundedCornerShape(10.dp))
                .background(colorScheme.surface.copy(alpha = 0.82f))
                .border(1.dp, colorScheme.outlineVariant.copy(alpha = 0.8f), RoundedCornerShape(10.dp))
                .padding(horizontal = 6.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = value,
                color = colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
            )
        }

        Box(
            modifier = Modifier
                .size(buttonSize)
                .clip(RoundedCornerShape(buttonRadius))
                .background(colorScheme.primaryContainer)
                .clickable(onClick = onPlus),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = plusIcon,
                contentDescription = null,
                tint = colorScheme.onPrimaryContainer,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

private fun formatSubtitleDelay(delayMs: Int): String {
    val sign = if (delayMs >= 0) "+" else "-"
    val absMs = abs(delayMs)
    val seconds = absMs / 1000
    val millis = absMs % 1000
    return "$sign$seconds.${millis.toString().padStart(3, '0')}s"
}

private fun formatCueTimestamp(timeMs: Long): String {
    val totalSeconds = (timeMs / 1000L).coerceAtLeast(0L)
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return "${minutes}:${seconds.toString().padStart(2, '0')}"
}
