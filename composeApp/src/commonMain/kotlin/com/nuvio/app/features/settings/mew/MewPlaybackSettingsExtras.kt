package com.nuvio.app.features.settings

// Mew-only playback settings. Upstream (Nuvio / Luqman) playback sub-pages call into these
// with a single line each, so upstream page files stay close to their original form and
// future merges only have to keep those one-line hooks.

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nuvio.app.features.player.AddonSubtitleStartupMode
import com.nuvio.app.features.player.PlayerSettingsRepository
import com.nuvio.app.features.player.PlayerSettingsUiState
import nuvio.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

/** Mew: extra rows for Settings > Playback > Player. */
@Composable
internal fun MewPlaybackPlayerExtras(isTablet: Boolean, settings: PlayerSettingsUiState) {
    SettingsSection(title = stringResource(Res.string.settings_nuvio_enhanced_title), isTablet = isTablet) {
        SettingsGroup(isTablet = isTablet) {
            SettingsSwitchRow(
                title = stringResource(Res.string.settings_playback_remember_brightness),
                description = stringResource(Res.string.settings_playback_remember_brightness_description),
                checked = settings.rememberPlayerBrightnessEnabled,
                enabled = !settings.externalPlayerEnabled,
                isTablet = isTablet,
                onCheckedChange = PlayerSettingsRepository::setRememberPlayerBrightnessEnabled,
            )
        }
    }
}

/** Mew: extra rows for Settings > Playback > Interface. */
@Composable
internal fun MewPlaybackInterfaceExtras(isTablet: Boolean, settings: PlayerSettingsUiState) {
    SettingsSection(title = stringResource(Res.string.settings_nuvio_enhanced_title), isTablet = isTablet) {
        SettingsGroup(isTablet = isTablet) {
            SettingsSwitchRow(
                title = stringResource(Res.string.settings_playback_stable_opening_overlay),
                description = stringResource(Res.string.settings_playback_stable_opening_overlay_description),
                checked = settings.stableOpeningOverlayEnabled,
                isTablet = isTablet,
                onCheckedChange = PlayerSettingsRepository::setStableOpeningOverlayEnabled,
            )
            SettingsGroupDivider(isTablet = isTablet)
            SettingsSwitchRow(
                title = stringResource(Res.string.settings_playback_player_liquid_glass),
                description = stringResource(Res.string.settings_playback_player_liquid_glass_description),
                checked = settings.playerLiquidGlassEnabled,
                isTablet = isTablet,
                onCheckedChange = PlayerSettingsRepository::setPlayerLiquidGlassEnabled,
            )
        }
    }
}

/** Mew: extra rows for Settings > Playback > Up Next. */
@Composable
internal fun MewPlaybackUpNextExtras(isTablet: Boolean, settings: PlayerSettingsUiState) {
    SettingsSection(title = stringResource(Res.string.settings_nuvio_enhanced_title), isTablet = isTablet) {
        SettingsGroup(isTablet = isTablet) {
            SettingsSwitchRow(
                title = stringResource(Res.string.settings_playback_next_episode_swipe_dismiss),
                description = stringResource(Res.string.settings_playback_next_episode_swipe_dismiss_description),
                checked = settings.nextEpisodeSwipeDismissEnabled,
                isTablet = isTablet,
                onCheckedChange = PlayerSettingsRepository::setNextEpisodeSwipeDismissEnabled,
            )
        }
    }
}

/** Mew: extra rows for Settings > Playback > Subtitles. */
@Composable
internal fun MewPlaybackSubtitleExtras(isTablet: Boolean, settings: PlayerSettingsUiState) {
    var showStartupModeDialog by remember { mutableStateOf(false) }
    SettingsSection(title = stringResource(Res.string.settings_nuvio_enhanced_title), isTablet = isTablet) {
        SettingsGroup(isTablet = isTablet) {
            SettingsNavigationRow(
                title = stringResource(Res.string.settings_playback_addon_subtitle_startup_mode),
                description = mewAddonSubtitleStartupModeLabel(settings.addonSubtitleStartupMode),
                isTablet = isTablet,
                onClick = { showStartupModeDialog = true },
            )
        }
    }
    if (showStartupModeDialog) {
        MewAddonSubtitleStartupModeDialog(
            selectedMode = settings.addonSubtitleStartupMode,
            onModeSelected = {
                PlayerSettingsRepository.setAddonSubtitleStartupMode(it)
                showStartupModeDialog = false
            },
            onDismiss = { showStartupModeDialog = false },
        )
    }
}

@Composable
private fun mewAddonSubtitleStartupModeLabel(mode: AddonSubtitleStartupMode): String =
    when (mode) {
        AddonSubtitleStartupMode.FAST_STARTUP ->
            stringResource(Res.string.settings_playback_addon_subtitle_startup_fast)
        AddonSubtitleStartupMode.PREFERRED_ONLY ->
            stringResource(Res.string.settings_playback_addon_subtitle_startup_preferred)
        AddonSubtitleStartupMode.ALL_SUBTITLES ->
            stringResource(Res.string.settings_playback_addon_subtitle_startup_all)
    }

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun MewAddonSubtitleStartupModeDialog(
    selectedMode: AddonSubtitleStartupMode,
    onModeSelected: (AddonSubtitleStartupMode) -> Unit,
    onDismiss: () -> Unit,
) {
    val options = listOf(
        Triple(
            AddonSubtitleStartupMode.FAST_STARTUP,
            Res.string.settings_playback_addon_subtitle_startup_fast,
            Res.string.settings_playback_addon_subtitle_startup_fast_description,
        ),
        Triple(
            AddonSubtitleStartupMode.PREFERRED_ONLY,
            Res.string.settings_playback_addon_subtitle_startup_preferred,
            Res.string.settings_playback_addon_subtitle_startup_preferred_description,
        ),
        Triple(
            AddonSubtitleStartupMode.ALL_SUBTITLES,
            Res.string.settings_playback_addon_subtitle_startup_all,
            Res.string.settings_playback_addon_subtitle_startup_all_description,
        ),
    )

    BasicAlertDialog(
        onDismissRequest = onDismiss,
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = stringResource(Res.string.settings_playback_addon_subtitle_startup_mode),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    options.forEach { (mode, titleRes, descriptionRes) ->
                        val isSelected = mode == selectedMode
                        val containerColor = if (isSelected) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        }

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onModeSelected(mode) },
                            shape = RoundedCornerShape(12.dp),
                            color = containerColor,
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(titleRes),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = stringResource(descriptionRes),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                Box(
                                    modifier = Modifier.size(24.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Rounded.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


/** Stacks an upstream settings page and its Mew extras inside a single lazy item. */
@Composable
internal fun MewSettingsStack(isTablet: Boolean, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(if (isTablet) 18.dp else 12.dp)) {
        content()
    }
}
