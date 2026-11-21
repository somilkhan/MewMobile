package com.nuvio.app.features.player

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nuvio.app.core.ui.nuvio
import com.nuvio.app.features.streams.StreamItem
import com.nuvio.app.features.streams.StreamsUiState
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.action_close
import nuvio.composeapp.generated.resources.compose_player_episode_code_full
import nuvio.composeapp.generated.resources.compose_player_panel_sources
import nuvio.composeapp.generated.resources.compose_player_playing
import org.jetbrains.compose.resources.stringResource

@Composable
fun PlayerSourcesPanel(
    visible: Boolean,
    streamsUiState: StreamsUiState,
    contentTitle: String,
    currentSeason: Int?,
    currentEpisode: Int?,
    currentEpisodeTitle: String?,
    currentStream: PlayerCurrentStream?,
    onFilterSelected: (String?) -> Unit,
    onStreamSelected: (StreamItem) -> Unit,
    onReload: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = MaterialTheme.nuvio
    val contentLabel = if (currentSeason != null && currentEpisode != null) {
        buildString {
            append(stringResource(Res.string.compose_player_episode_code_full, currentSeason, currentEpisode))
            currentEpisodeTitle?.takeIf { it.isNotBlank() }?.let {
                append(" • ")
                append(it)
            }
        }
    } else {
        contentTitle
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(NuvioTokens.Motion.normalMillis)),
        exit = fadeOut(tween(NuvioTokens.Motion.normalMillis)),
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
        ) {
            PlayerPanelHeader(
                title = stringResource(Res.string.compose_player_panel_sources),
            ) {
                PlayerDialogButton(
                    label = stringResource(Res.string.action_close),
                    onClick = onDismiss,
                )
                .background(tokens.colors.overlayScrim.copy(alpha = tokens.opacity.medium)),
            contentAlignment = Alignment.Center,
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(tween(NuvioTokens.Motion.sheetEnterMillis)) { it / 3 } +
                    fadeIn(tween(NuvioTokens.Motion.sheetEnterMillis)),
                exit = slideOutVertically(tween(NuvioTokens.Motion.sheetExitMillis)) { it / 3 } +
                    fadeOut(tween(NuvioTokens.Motion.sheetExitMillis)),
            ) {
                Box(
                    modifier = Modifier
                        .widthIn(max = tokens.components.playerPanelMaxWidth)
                        .fillMaxWidth(0.92f)
                        .heightIn(max = tokens.components.dialogMaxWidth + NuvioTokens.Space.s40)
                        .clip(tokens.shapes.playerPanel)
                        .background(tokens.colors.surfaceSheet)
                        .border(tokens.borders.thin, tokens.colors.borderDefault, tokens.shapes.playerPanel)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = {},
                        ),
                ) {
                    Column {
                        // Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = tokens.spacing.sheetPadding, vertical = tokens.spacing.cardPadding),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(Res.string.compose_player_panel_sources),
                                color = tokens.colors.textPrimary,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(tokens.spacing.controlGap)) {
                                PanelChipButton(
                                    label = stringResource(Res.string.compose_action_reload),
                                    icon = Icons.Rounded.Refresh,
                                    onClick = onReload,
                                )
                                PanelChipButton(
                                    label = stringResource(Res.string.action_close),
                                    onClick = onDismiss,
                                )
                            }
                        }

                        // Addon filter chips
                        val addonNames = remember(streamsUiState.groups) {
                            streamsUiState.groups.map { it.addonName }.distinct()
                        }
                        if (addonNames.size > 1) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(horizontal = tokens.spacing.sheetPadding)
                                    .padding(bottom = tokens.spacing.listGap),
                                horizontalArrangement = Arrangement.spacedBy(tokens.spacing.controlGap),
                            ) {
                                AddonFilterChip(
                                    label = stringResource(Res.string.collections_tab_all),
                                    isSelected = streamsUiState.selectedFilter == null,
                                    onClick = { onFilterSelected(null) },
                                )
                                addonNames.forEach { addon ->
                                    val group = streamsUiState.groups.firstOrNull { it.addonName == addon }
                                    AddonFilterChip(
                                        label = addon,
                                        isSelected = streamsUiState.selectedFilter == group?.addonId,
                                        isLoading = group?.isLoading == true,
                                        hasError = group?.error != null,
                                        onClick = { onFilterSelected(group?.addonId) },
                                    )
                                }
                            }
                        }

                        // Content
                        when {
                            streamsUiState.isAnyLoading && streamsUiState.allStreams.isEmpty() -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = NuvioTokens.Space.s40),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    NuvioLoadingIndicator(
                                        color = tokens.colors.accent,
                                        modifier = Modifier.size(tokens.icons.lg + NuvioTokens.Space.s4),
                                    )
                                }
                            }

                            streamsUiState.allStreams.isEmpty() -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = NuvioTokens.Space.s40),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = stringResource(Res.string.compose_player_no_streams_found),
                                        color = tokens.colors.textMuted,
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }
                            }

            PlayerProviderFilterRow(
                streamsUiState = streamsUiState,
                onFilterSelected = onFilterSelected,
                onRefresh = onReload,
            )

@Composable
internal fun AddonFilterChip(
    label: String,
    isSelected: Boolean,
    isLoading: Boolean = false,
    hasError: Boolean = false,
    onClick: () -> Unit,
) {
    val tokens = MaterialTheme.nuvio

            PlayerStreamList(
                streamsUiState = streamsUiState,
                onStreamSelected = onStreamSelected,
                modifier = Modifier.weight(1f),
                currentStream = currentStream,
                currentLabel = stringResource(Res.string.compose_player_playing),
            )
        }
    }
}

@Composable
internal fun PanelChipButton(
    label: String,
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
) {
    val tokens = MaterialTheme.nuvio

    Box(
        modifier = Modifier
            .clip(tokens.shapes.compactCard)
            .background(tokens.colors.surfacePopover, tokens.shapes.compactCard)
            .border(tokens.borders.thin, tokens.colors.borderSubtle, tokens.shapes.compactCard)
            .clickable(onClick = onClick)
            .padding(horizontal = NuvioTokens.Space.s12, vertical = NuvioTokens.Space.s6),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(NuvioTokens.Space.s4),
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tokens.colors.textMuted,
                    modifier = Modifier.size(NuvioTokens.Space.s14),
                )
            }
            Text(
                text = label,
                color = tokens.colors.textMuted,
                fontSize = NuvioTokens.Type.labelSm,
            )
        }
    }
}

data class PlayerCurrentStream(
    val url: String? = null,
    val name: String? = null,
    val description: String? = null,
    val addonId: String? = null,
    val infoHash: String? = null,
    val fileIdx: Int? = null,
    val filename: String? = null,
)

internal fun List<StreamItem>.currentPlayerStreamIndex(current: PlayerCurrentStream?): Int {
    if (current == null) return -1
    val isSameAddon = { stream: StreamItem ->
        current.addonId.isNullOrBlank() || stream.addonId == current.addonId
    }

    if (!current.infoHash.isNullOrBlank()) {
        val hashMatch = indexOfFirst { stream ->
            stream.p2pInfoHash.equals(current.infoHash, ignoreCase = true) &&
                (current.fileIdx == null || stream.p2pFileIdx == current.fileIdx) &&
                isSameAddon(stream)
        }
        if (hashMatch >= 0) return hashMatch
    }

    if (!current.url.isNullOrBlank()) {
        val urlMatch = indexOfFirst { stream -> stream.playableDirectUrl == current.url }
        if (urlMatch >= 0) return urlMatch
    }

    // Addons may issue a new URL per request, so match what identifies the release instead.
    // Filenames and templates can repeat across streams, so only trust a single result.
    if (!current.filename.isNullOrBlank()) {
        val filenameMatches = indices.filter { index ->
            val stream = this[index]
            (stream.behaviorHints.filename ?: stream.clientResolve?.filename) == current.filename &&
                isSameAddon(stream)
        }
        filenameMatches.singleOrNull()?.let { return it }
    }

    if (!current.name.isNullOrBlank()) {
        val nameMatches = indices.filter { index ->
            val stream = this[index]
            stream.streamLabel.equals(current.name, ignoreCase = true) &&
                isSameAddon(stream) &&
                (current.description == null || stream.streamSubtitle == current.description)
        }
        nameMatches.singleOrNull()?.let { return it }
    }

    return -1
}
