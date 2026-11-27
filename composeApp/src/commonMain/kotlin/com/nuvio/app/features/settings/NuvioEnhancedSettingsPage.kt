@file:Suppress("DEPRECATION") // LocalClipboardManager is the clipboard API exposed by this Compose dependency.

package com.nuvio.app.features.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nuvio.app.core.build.AppFeaturePolicy
import com.nuvio.app.core.build.TrailerPlaybackMode
import com.nuvio.app.core.diagnostics.CrashDiagnostics
import com.nuvio.app.core.network.DnsOverHttpsProvider
import com.nuvio.app.core.network.DnsOverHttpsSettingsRepository
import com.nuvio.app.core.sync.ProfileSettingsSync
import com.nuvio.app.core.ui.AppIconResource
import com.nuvio.app.core.ui.Chip
import com.nuvio.app.core.ui.DialogButton
import com.nuvio.app.core.ui.DialogButtonStyle
import com.nuvio.app.core.ui.DialogButtons
import com.nuvio.app.core.ui.DialogOption
import com.nuvio.app.core.ui.DialogSurface
import com.nuvio.app.core.ui.NuvioToastController
import com.nuvio.app.core.ui.NuvioTokens
import com.nuvio.app.core.ui.appIconPainter
import com.nuvio.app.core.ui.nuvio
import com.nuvio.app.features.details.MetaEpisodeCardStyle
import com.nuvio.app.features.details.MetaScreenSettingsRepository
import com.nuvio.app.features.downloads.DownloadsExternalFolderPlatform
import com.nuvio.app.features.home.HomeCatalogSettingsRepository
import com.nuvio.app.features.player.AndroidPlaybackEngine
import com.nuvio.app.features.player.PlayerSettingsRepository
import com.nuvio.app.isIos
import org.jetbrains.compose.resources.stringResource
import nuvio.composeapp.generated.resources.*
import nuvio.composeapp.generated.resources.Res

private const val NuvioEnhancedGithubUrl = "https://github.com/somilkhan/MewMobile"
internal const val MewTelegramUrl = "https://t.me/MewRelease"
private const val MewDonateUrl = "https://www.thankyouverymuch.co/zehen"
private const val MewUpiUrl = "upi://pay?pa=sahilrain1001@ybl&pn=Zen&cu=INR"

/**
 * Renders one area of the Mew settings. [section] Hub is the landing page with navigation rows;
 * every other section is its own sub-page (see MewSettingsSection).
 */
internal fun LazyListScope.nuvioEnhancedSettingsContent(
    isTablet: Boolean,
    section: MewSettingsSection = MewSettingsSection.Hub,
    onPageChange: (SettingsPage) -> Unit = {},
) {
    item {
        NuvioEnhancedSettingsPageContent(
            isTablet = isTablet,
            section = section,
            onPageChange = onPageChange,
        )
    }
}

@Composable
private fun NuvioEnhancedSettingsPageContent(
    isTablet: Boolean,
    section: MewSettingsSection,
    onPageChange: (SettingsPage) -> Unit,
) {
    val settings by remember {
        NuvioEnhancedSettingsRepository.ensureLoaded()
        NuvioEnhancedSettingsRepository.uiState
    }.collectAsStateWithLifecycle()
    val homeSettings by remember {
        HomeCatalogSettingsRepository.snapshot()
        HomeCatalogSettingsRepository.uiState
    }.collectAsStateWithLifecycle()
    val dnsOverHttpsSettings by remember {
        DnsOverHttpsSettingsRepository.ensureLoaded()
        DnsOverHttpsSettingsRepository.uiState
    }.collectAsStateWithLifecycle()
    val detailSettings by remember {
        MetaScreenSettingsRepository.ensureLoaded()
        MetaScreenSettingsRepository.uiState
    }.collectAsStateWithLifecycle()
    val playerSettings by remember {
        PlayerSettingsRepository.ensureLoaded()
        PlayerSettingsRepository.uiState
    }.collectAsStateWithLifecycle()
    val externalFolderState by DownloadsExternalFolderPlatform.state.collectAsStateWithLifecycle()
    val lastCrashReport by remember {
        CrashDiagnostics.lastReport
    }.collectAsStateWithLifecycle()
    val clipboardManager = LocalClipboardManager.current
    val uriHandler = LocalUriHandler.current
    var backupPayload by remember { mutableStateOf<String?>(null) }
    var showImportDialog by remember { mutableStateOf(false) }
    var importPayload by remember { mutableStateOf("") }
    var importError by remember { mutableStateOf<String?>(null) }
    var showDnsDialog by remember { mutableStateOf(false) }
    val backupImportedMessage = stringResource(Res.string.nuvio_enhanced_toast_backup_imported)
    val invalidBackupPayloadMessage = stringResource(Res.string.nuvio_enhanced_toast_invalid_backup)
    val backupFileReadyMessage = stringResource(Res.string.nuvio_enhanced_toast_backup_ready)
    val backupExportFailedMessage = stringResource(Res.string.nuvio_enhanced_toast_backup_export_failed)
    val backupImportFailedMessage = stringResource(Res.string.nuvio_enhanced_toast_backup_import_failed)
    val backupCopiedMessage = stringResource(Res.string.nuvio_enhanced_toast_backup_copied)
    val crashCopiedMessage = stringResource(Res.string.nuvio_enhanced_toast_crash_copied)
    val diagnosticsCopiedMessage = stringResource(Res.string.nuvio_enhanced_toast_diagnostics_copied)
    val externalFolderFailedMessage = stringResource(Res.string.nuvio_enhanced_external_folder_failed)
    val homeHeroVideoPreviewSupported = AppFeaturePolicy.heroTrailerPlaybackSupported &&
        AppFeaturePolicy.trailerPlaybackMode == TrailerPlaybackMode.IN_APP
    val heroVisualControlsEnabled = !settings.originalNuvioHeroBannerEnabled
    val detailHeroTrailerPlaybackSupported = AppFeaturePolicy.heroTrailerPlaybackSupported &&
        AppFeaturePolicy.trailerPlaybackMode == TrailerPlaybackMode.IN_APP

    fun isNew(feature: NuvioEnhancedFeature): Boolean = settings.isNew(feature)
    fun markSeen(feature: NuvioEnhancedFeature) {
        NuvioEnhancedSettingsRepository.markFeatureSeen(feature)
    }
    fun importBackupPayload(payload: String) {
        ProfileSettingsSync.importBackupJson(payload)
            .onSuccess {
                NuvioToastController.show(backupImportedMessage)
                showImportDialog = false
                importPayload = ""
                importError = null
            }
            .onFailure { error ->
                importError = error.message ?: invalidBackupPayloadMessage
                NuvioToastController.show(importError ?: invalidBackupPayloadMessage)
            }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.nuvio.spacing.listGap),
    ) {
        if (section == MewSettingsSection.Hub) {
            MewSettingsHubRows(
                isTablet = isTablet,
                settings = settings,
                onPageChange = onPageChange,
                onMarkAllSeen = NuvioEnhancedSettingsRepository::markAllFeaturesSeen,
            )
        }

        if (section == MewSettingsSection.Home) {
        SettingsSection(
            title = stringResource(Res.string.nuvio_enhanced_section_nuvio_experience),
            isTablet = isTablet,
        ) {
            SettingsGroup(isTablet = isTablet) {
                SettingsSwitchRow(
                    title = stringResource(Res.string.nuvio_enhanced_home_features_title),
                    description = stringResource(Res.string.nuvio_enhanced_home_features_desc),
                    checked = settings.enhancedHomeFeaturesEnabled,
                    isTablet = isTablet,
                    highlighted = isNew(NuvioEnhancedFeature.HomeExperienceControls),
                    onCheckedChange = {
                        markSeen(NuvioEnhancedFeature.HomeExperienceControls)
                        NuvioEnhancedSettingsRepository.setEnhancedHomeFeaturesEnabled(it)
                    },
                )
                SettingsGroupDivider(isTablet = isTablet)
                SettingsSwitchRow(
                    title = stringResource(Res.string.nuvio_enhanced_concierge_title),
                    description = stringResource(Res.string.nuvio_enhanced_concierge_desc),
                    checked = settings.nuvioConciergeEnabled,
                    enabled = settings.enhancedHomeFeaturesEnabled,
                    isTablet = isTablet,
                    highlighted = isNew(NuvioEnhancedFeature.HomeExperienceControls),
                    onCheckedChange = {
                        markSeen(NuvioEnhancedFeature.HomeExperienceControls)
                        NuvioEnhancedSettingsRepository.setNuvioConciergeEnabled(it)
                    },
                )
                SettingsGroupDivider(isTablet = isTablet)
                SettingsSwitchRow(
                    title = stringResource(Res.string.nuvio_enhanced_smart_resume_title),
                    description = stringResource(Res.string.nuvio_enhanced_smart_resume_desc),
                    checked = settings.smartResumeEnabled,
                    enabled = settings.enhancedHomeFeaturesEnabled && settings.nuvioConciergeEnabled,
                    isTablet = isTablet,
                    highlighted = isNew(NuvioEnhancedFeature.SmartResume2),
                    onCheckedChange = {
                        markSeen(NuvioEnhancedFeature.SmartResume2)
                        NuvioEnhancedSettingsRepository.setSmartResumeEnabled(it)
                    },
                )
                SettingsGroupDivider(isTablet = isTablet)
                SettingsSwitchRow(
                    title = stringResource(Res.string.settings_continue_watching_ready_badge_title),
                    description = stringResource(Res.string.settings_continue_watching_ready_badge_description),
                    checked = settings.showContinueWatchingReadyBadge,
                    isTablet = isTablet,
                    highlighted = isNew(NuvioEnhancedFeature.HomeExperienceControls),
                    onCheckedChange = {
                        markSeen(NuvioEnhancedFeature.HomeExperienceControls)
                        NuvioEnhancedSettingsRepository.setShowContinueWatchingReadyBadge(it)
                    },
                )
                SettingsGroupDivider(isTablet = isTablet)
                SettingsSwitchRow(
                    title = stringResource(Res.string.nuvio_enhanced_release_signals_title),
                    description = stringResource(Res.string.nuvio_enhanced_release_signals_desc),
                    checked = settings.releaseRadarHomeSignalsEnabled,
                    enabled = settings.enhancedHomeFeaturesEnabled && settings.nuvioConciergeEnabled,
                    isTablet = isTablet,
                    highlighted = isNew(NuvioEnhancedFeature.HomeExperienceControls),
                    onCheckedChange = {
                        markSeen(NuvioEnhancedFeature.HomeExperienceControls)
                        NuvioEnhancedSettingsRepository.setReleaseRadarHomeSignalsEnabled(it)
                    },
                )
                SettingsGroupDivider(isTablet = isTablet)
                SettingsSwitchRow(
                    title = stringResource(Res.string.nuvio_enhanced_profile_stats_title),
                    description = stringResource(Res.string.nuvio_enhanced_profile_stats_desc),
                    checked = settings.profileStatsEnabled,
                    enabled = settings.enhancedHomeFeaturesEnabled && settings.nuvioConciergeEnabled,
                    isTablet = isTablet,
                    highlighted = isNew(NuvioEnhancedFeature.HomeExperienceControls),
                    onCheckedChange = {
                        markSeen(NuvioEnhancedFeature.HomeExperienceControls)
                        NuvioEnhancedSettingsRepository.setProfileStatsEnabled(it)
                    },
                )
            }
        }

        SettingsSection(
            title = stringResource(Res.string.nuvio_enhanced_section_premium_labs),
            isTablet = isTablet,
        ) {
            SettingsGroup(isTablet = isTablet) {
                SettingsSwitchRow(
                    title = stringResource(Res.string.nuvio_enhanced_smart_shelf_title),
                    description = stringResource(Res.string.nuvio_enhanced_smart_shelf_desc),
                    checked = settings.smartShelvesEnabled,
                    enabled = settings.enhancedHomeFeaturesEnabled && !settings.quietHomeModeEnabled,
                    isTablet = isTablet,
                    highlighted = isNew(NuvioEnhancedFeature.SmartShelfComposer),
                    onCheckedChange = {
                        markSeen(NuvioEnhancedFeature.SmartShelfComposer)
                        NuvioEnhancedSettingsRepository.setSmartShelvesEnabled(it)
                    },
                )
                SettingsGroupDivider(isTablet = isTablet)
                SettingsSwitchRow(
                    title = stringResource(Res.string.nuvio_enhanced_release_digest_title),
                    description = stringResource(Res.string.nuvio_enhanced_release_digest_desc),
                    checked = settings.releaseRadarDigestEnabled,
                    enabled = settings.enhancedHomeFeaturesEnabled,
                    isTablet = isTablet,
                    highlighted = isNew(NuvioEnhancedFeature.ReleaseRadarDigest),
                    onCheckedChange = {
                        markSeen(NuvioEnhancedFeature.ReleaseRadarDigest)
                        NuvioEnhancedSettingsRepository.setReleaseRadarDigestEnabled(it)
                    },
                )
                SettingsGroupDivider(isTablet = isTablet)
                SettingsSwitchRow(
                    title = stringResource(Res.string.nuvio_enhanced_quiet_home_title),
                    description = stringResource(Res.string.nuvio_enhanced_quiet_home_desc),
                    checked = settings.quietHomeModeEnabled,
                    enabled = settings.enhancedHomeFeaturesEnabled,
                    isTablet = isTablet,
                    highlighted = isNew(NuvioEnhancedFeature.QuietHomeMode),
                    onCheckedChange = {
                        markSeen(NuvioEnhancedFeature.QuietHomeMode)
                        NuvioEnhancedSettingsRepository.setQuietHomeModeEnabled(it)
                    },
                )
                SettingsGroupDivider(isTablet = isTablet)
                SettingsSwitchRow(
                    title = stringResource(Res.string.nuvio_enhanced_library_health_title),
                    description = stringResource(Res.string.nuvio_enhanced_library_health_desc),
                    checked = settings.libraryHealthEnabled,
                    enabled = settings.enhancedHomeFeaturesEnabled,
                    isTablet = isTablet,
                    highlighted = isNew(NuvioEnhancedFeature.LibraryHealth),
                    onCheckedChange = {
                        markSeen(NuvioEnhancedFeature.LibraryHealth)
                        NuvioEnhancedSettingsRepository.setLibraryHealthEnabled(it)
                    },
                )
            }
        }

        }

        if (section == MewSettingsSection.App) {
        SettingsSection(
            title = stringResource(Res.string.nuvio_enhanced_section_app_experience),
            isTablet = isTablet,
        ) {
            SettingsGroup(isTablet = isTablet) {
                SettingsSwitchRow(
                    title = stringResource(Res.string.nuvio_enhanced_status_bar_title),
                    description = stringResource(Res.string.nuvio_enhanced_status_bar_desc),
                    checked = settings.statusBarVisible,
                    isTablet = isTablet,
                    highlighted = isNew(NuvioEnhancedFeature.StatusBarVisibility),
                    onCheckedChange = {
                        markSeen(NuvioEnhancedFeature.StatusBarVisibility)
                        NuvioEnhancedSettingsRepository.setStatusBarVisible(it)
                    },
                )
                SettingsGroupDivider(isTablet = isTablet)
                SettingsSwitchRow(
                    title = stringResource(Res.string.nuvio_enhanced_live_tv_title),
                    description = stringResource(Res.string.nuvio_enhanced_live_tv_desc),
                    checked = settings.liveTvEnabled,
                    isTablet = isTablet,
                    highlighted = isNew(NuvioEnhancedFeature.LiveTvControls),
                    onCheckedChange = {
                        markSeen(NuvioEnhancedFeature.LiveTvControls)
                        NuvioEnhancedSettingsRepository.setLiveTvEnabled(it)
                    },
                )
            }
        }
        }

        if (section == MewSettingsSection.Player) {
            SettingsSection(
                title = stringResource(Res.string.nuvio_enhanced_section_player_tools),
                isTablet = isTablet,
            ) {
                SettingsGroup(isTablet = isTablet) {
                    SettingsSwitchRow(
                        title = stringResource(Res.string.settings_playback_parental_guide),
                        description = stringResource(Res.string.settings_playback_parental_guide_description),
                        checked = playerSettings.showParentalGuide,
                        isTablet = isTablet,
                        highlighted = isNew(NuvioEnhancedFeature.ContentWarnings),
                        onCheckedChange = {
                            markSeen(NuvioEnhancedFeature.ContentWarnings)
                            PlayerSettingsRepository.setShowParentalGuide(it)
                        },
                    )
                    SettingsGroupDivider(isTablet = isTablet)
                    SettingsSwitchRow(
                        title = stringResource(Res.string.nuvio_enhanced_source_pinning_title),
                        description = stringResource(Res.string.nuvio_enhanced_source_pinning_desc),
                        checked = settings.streamSourcePinningEnabled,
                        isTablet = isTablet,
                        highlighted = isNew(NuvioEnhancedFeature.StreamSourcePinning),
                        onCheckedChange = {
                            markSeen(NuvioEnhancedFeature.StreamSourcePinning)
                            NuvioEnhancedSettingsRepository.setStreamSourcePinningEnabled(it)
                        },
                    )
                    SettingsGroupDivider(isTablet = isTablet)
                    SettingsSwitchRow(
                        title = stringResource(Res.string.nuvio_enhanced_background_stream_prefetch_title),
                        description = stringResource(Res.string.nuvio_enhanced_background_stream_prefetch_desc),
                        checked = settings.backgroundStreamPrefetchEnabled,
                        isTablet = isTablet,
                        highlighted = isNew(NuvioEnhancedFeature.BackgroundStreamPrefetch),
                        onCheckedChange = {
                            markSeen(NuvioEnhancedFeature.BackgroundStreamPrefetch)
                            NuvioEnhancedSettingsRepository.setBackgroundStreamPrefetchEnabled(it)
                        },
                    )
                    SettingsGroupDivider(isTablet = isTablet)
                    SettingsSwitchRow(
                        title = stringResource(Res.string.nuvio_enhanced_player_status_overlay_title),
                        description = stringResource(Res.string.nuvio_enhanced_player_status_overlay_desc),
                        checked = settings.playerStatusOverlayEnabled,
                        isTablet = isTablet,
                        highlighted = isNew(NuvioEnhancedFeature.PlayerStatusOverlay),
                        onCheckedChange = {
                            markSeen(NuvioEnhancedFeature.PlayerStatusOverlay)
                            NuvioEnhancedSettingsRepository.setPlayerStatusOverlayEnabled(it)
                        },
                    )
                    SettingsGroupDivider(isTablet = isTablet)
                    SettingsSwitchRow(
                        title = stringResource(Res.string.nuvio_enhanced_subtitle_sync_title),
                        description = stringResource(Res.string.nuvio_enhanced_subtitle_sync_desc),
                        checked = playerSettings.subtitleSyncMenuEnabled,
                        enabled = settings.enhancedHomeFeaturesEnabled,
                        isTablet = isTablet,
                        highlighted = isNew(NuvioEnhancedFeature.SubtitleSyncMenu),
                        onCheckedChange = {
                            markSeen(NuvioEnhancedFeature.SubtitleSyncMenu)
                            PlayerSettingsRepository.setSubtitleSyncMenuEnabled(it)
                        },
                    )
                    SettingsGroupDivider(isTablet = isTablet)
                    EnhancedChoiceRow(
                        title = stringResource(Res.string.nuvio_enhanced_subtitle_selector_title),
                        description = stringResource(Res.string.nuvio_enhanced_subtitle_selector_desc),
                        selected = settings.subtitleSelectorStyle,
                        options = listOf(
                            EnhancedChoiceOption(
                                NuvioSubtitleSelectorStyle.Enhanced,
                                stringResource(Res.string.nuvio_enhanced_subtitle_selector_enhanced),
                            ),
                            EnhancedChoiceOption(
                                NuvioSubtitleSelectorStyle.Nuvio,
                                stringResource(Res.string.nuvio_enhanced_subtitle_selector_nuvio),
                            ),
                        ),
                        isTablet = isTablet,
                        highlighted = isNew(NuvioEnhancedFeature.SubtitleSelectorStyle),
                        onSelected = {
                            markSeen(NuvioEnhancedFeature.SubtitleSelectorStyle)
                            NuvioEnhancedSettingsRepository.setSubtitleSelectorStyle(it)
                        },
                    )
                    SettingsGroupDivider(isTablet = isTablet)
                    EnhancedChoiceRow(
                        title = stringResource(Res.string.nuvio_enhanced_audio_selector_title),
                        description = stringResource(Res.string.nuvio_enhanced_audio_selector_desc),
                        selected = settings.audioSelectorStyle,
                        options = listOf(
                            EnhancedChoiceOption(
                                NuvioAudioSelectorStyle.Enhanced,
                                stringResource(Res.string.nuvio_enhanced_subtitle_selector_enhanced),
                            ),
                            EnhancedChoiceOption(
                                NuvioAudioSelectorStyle.Nuvio,
                                stringResource(Res.string.nuvio_enhanced_subtitle_selector_nuvio),
                            ),
                        ),
                        isTablet = isTablet,
                        highlighted = isNew(NuvioEnhancedFeature.AudioSelectorStyle),
                        onSelected = {
                            markSeen(NuvioEnhancedFeature.AudioSelectorStyle)
                            NuvioEnhancedSettingsRepository.setAudioSelectorStyle(it)
                        },
                    )
                    SettingsGroupDivider(isTablet = isTablet)
                    SettingsSwitchRow(
                        title = stringResource(Res.string.nuvio_enhanced_player_clock_title),
                        description = stringResource(Res.string.nuvio_enhanced_player_clock_desc),
                        checked = playerSettings.playerClockEndTimeEnabled,
                        enabled = settings.enhancedHomeFeaturesEnabled,
                        isTablet = isTablet,
                        highlighted = isNew(NuvioEnhancedFeature.PlayerTimeOverlay),
                        onCheckedChange = {
                            markSeen(NuvioEnhancedFeature.PlayerTimeOverlay)
                            PlayerSettingsRepository.setPlayerClockEndTimeEnabled(it)
                        },
                    )
                    SettingsGroupDivider(isTablet = isTablet)
                    SettingsSwitchRow(
                        title = stringResource(Res.string.settings_playback_random_next_episode),
                        description = stringResource(Res.string.settings_playback_random_next_episode_description),
                        checked = playerSettings.randomNextEpisodeEnabled,
                        enabled = settings.enhancedHomeFeaturesEnabled,
                        isTablet = isTablet,
                        highlighted = isNew(NuvioEnhancedFeature.PersistentEpisodeShuffle),
                        onCheckedChange = {
                            markSeen(NuvioEnhancedFeature.PersistentEpisodeShuffle)
                            PlayerSettingsRepository.setRandomNextEpisodeEnabled(it)
                        },
                    )
                    SettingsGroupDivider(isTablet = isTablet)
                    SettingsSwitchRow(
                        title = stringResource(Res.string.nuvio_enhanced_next_episode_button_title),
                        description = stringResource(Res.string.nuvio_enhanced_next_episode_button_desc),
                        checked = settings.nextEpisodeButtonEnabled,
                        enabled = settings.enhancedHomeFeaturesEnabled,
                        isTablet = isTablet,
                        highlighted = isNew(NuvioEnhancedFeature.NextEpisodeButton),
                        onCheckedChange = {
                            markSeen(NuvioEnhancedFeature.NextEpisodeButton)
                            NuvioEnhancedSettingsRepository.setNextEpisodeButtonEnabled(it)
                        },
                    )
                    if (!isIos) {
                        SettingsGroupDivider(isTablet = isTablet)
                        SettingsSwitchRow(
                            title = stringResource(Res.string.settings_playback_android_memory_safe_buffer),
                            description = stringResource(Res.string.settings_playback_android_memory_safe_buffer_description),
                            checked = playerSettings.androidMemorySafeBufferEnabled,
                            enabled = !playerSettings.externalPlayerEnabled &&
                                playerSettings.androidPlaybackEngine != AndroidPlaybackEngine.Libmpv,
                            isTablet = isTablet,
                            highlighted = isNew(NuvioEnhancedFeature.PlayerStatusOverlay),
                            onCheckedChange = PlayerSettingsRepository::setAndroidMemorySafeBufferEnabled,
                        )
                    }
                }
            }
        }

        if (section == MewSettingsSection.Hero) {
        SettingsSection(
            title = stringResource(Res.string.nuvio_enhanced_section_hero_experience),
            isTablet = isTablet,
        ) {
            SettingsGroup(isTablet = isTablet) {
                SettingsSwitchRow(
                    title = stringResource(Res.string.nuvio_enhanced_original_hero_title),
                    description = stringResource(Res.string.nuvio_enhanced_original_hero_desc),
                    checked = settings.originalNuvioHeroBannerEnabled,
                    isTablet = isTablet,
                    highlighted = isNew(NuvioEnhancedFeature.HeroControlsV2),
                    onCheckedChange = {
                        markSeen(NuvioEnhancedFeature.HeroControlsV2)
                        NuvioEnhancedSettingsRepository.setOriginalNuvioHeroBannerEnabled(it)
                    },
                )
                SettingsGroupDivider(isTablet = isTablet)
                SettingsSwitchRow(
                    title = stringResource(Res.string.nuvio_enhanced_hero_details_button_title),
                    description = stringResource(Res.string.nuvio_enhanced_hero_details_button_desc),
                    checked = settings.showHeroDetailsButton,
                    enabled = heroVisualControlsEnabled,
                    isTablet = isTablet,
                    highlighted = isNew(NuvioEnhancedFeature.HeroControlsV2),
                    onCheckedChange = {
                        markSeen(NuvioEnhancedFeature.HeroControlsV2)
                        NuvioEnhancedSettingsRepository.setShowHeroDetailsButton(it)
                    },
                )
                SettingsGroupDivider(isTablet = isTablet)
                EnhancedChoiceRow(
                    title = stringResource(Res.string.nuvio_enhanced_hero_display_title),
                    description = stringResource(Res.string.nuvio_enhanced_hero_display_desc),
                    selected = settings.heroDisplayMode,
                    options = listOf(
                        EnhancedChoiceOption(
                            NuvioHeroDisplayMode.Cinematic,
                            stringResource(Res.string.nuvio_enhanced_hero_mode_cinematic),
                        ),
                        EnhancedChoiceOption(
                            NuvioHeroDisplayMode.Balanced,
                            stringResource(Res.string.nuvio_enhanced_hero_mode_balanced),
                        ),
                        EnhancedChoiceOption(
                            NuvioHeroDisplayMode.InfoRich,
                            stringResource(Res.string.nuvio_enhanced_hero_mode_info_rich),
                        ),
                    ),
                    isTablet = isTablet,
                    enabled = heroVisualControlsEnabled,
                    highlighted = isNew(NuvioEnhancedFeature.HeroExperienceControls),
                    onSelected = {
                        markSeen(NuvioEnhancedFeature.HeroExperienceControls)
                        NuvioEnhancedSettingsRepository.setHeroDisplayMode(it)
                    },
                )
                SettingsGroupDivider(isTablet = isTablet)
                EnhancedChoiceRow(
                    title = stringResource(Res.string.nuvio_enhanced_hero_artwork_title),
                    description = stringResource(Res.string.nuvio_enhanced_hero_artwork_desc),
                    selected = settings.heroArtworkSource,
                    options = listOf(
                        EnhancedChoiceOption(
                            NuvioHeroArtworkSource.Backdrop,
                            stringResource(Res.string.nuvio_enhanced_hero_artwork_backdrop),
                        ),
                        EnhancedChoiceOption(
                            NuvioHeroArtworkSource.Poster,
                            stringResource(Res.string.nuvio_enhanced_hero_artwork_poster),
                        ),
                    ),
                    isTablet = isTablet,
                    enabled = heroVisualControlsEnabled,
                    highlighted = isNew(NuvioEnhancedFeature.HeroExperienceControls),
                    onSelected = {
                        markSeen(NuvioEnhancedFeature.HeroExperienceControls)
                        NuvioEnhancedSettingsRepository.setHeroArtworkSource(it)
                    },
                )
                SettingsGroupDivider(isTablet = isTablet)
                SettingsSwitchRow(
                    title = stringResource(Res.string.nuvio_enhanced_poster_hero_title),
                    description = stringResource(Res.string.nuvio_enhanced_poster_hero_desc),
                    checked = settings.posterArtHeroEnabled,
                    enabled = heroVisualControlsEnabled,
                    isTablet = isTablet,
                    highlighted = isNew(NuvioEnhancedFeature.HeroExperienceControls),
                    onCheckedChange = {
                        markSeen(NuvioEnhancedFeature.HeroExperienceControls)
                        NuvioEnhancedSettingsRepository.setPosterArtHeroEnabled(it)
                    },
                )
                SettingsGroupDivider(isTablet = isTablet)
                SettingsSwitchRow(
                    title = stringResource(Res.string.nuvio_enhanced_showcase_hero_title),
                    description = stringResource(Res.string.nuvio_enhanced_showcase_hero_desc),
                    checked = settings.streamingShowcaseHeroEnabled,
                    enabled = heroVisualControlsEnabled,
                    isTablet = isTablet,
                    highlighted = isNew(NuvioEnhancedFeature.HeroExperienceControls),
                    onCheckedChange = {
                        markSeen(NuvioEnhancedFeature.HeroExperienceControls)
                        NuvioEnhancedSettingsRepository.setStreamingShowcaseHeroEnabled(it)
                    },
                )
                SettingsGroupDivider(isTablet = isTablet)
                SettingsSwitchRow(
                    title = stringResource(Res.string.nuvio_enhanced_showcase_video_preview_title),
                    description = stringResource(Res.string.nuvio_enhanced_showcase_video_preview_desc),
                    checked = settings.streamingShowcaseVideoPreviewEnabled,
                    enabled = heroVisualControlsEnabled &&
                        settings.streamingShowcaseHeroEnabled && homeHeroVideoPreviewSupported,
                    isTablet = isTablet,
                    highlighted = isNew(NuvioEnhancedFeature.HeroExperienceControls),
                    onCheckedChange = {
                        markSeen(NuvioEnhancedFeature.HeroExperienceControls)
                        NuvioEnhancedSettingsRepository.setStreamingShowcaseVideoPreviewEnabled(it)
                    },
                )
                SettingsGroupDivider(isTablet = isTablet)
                SettingsSwitchRow(
                    title = stringResource(Res.string.nuvio_enhanced_showcase_video_preview_sound_title),
                    description = stringResource(Res.string.nuvio_enhanced_showcase_video_preview_sound_desc),
                    checked = settings.streamingShowcaseVideoPreviewSoundEnabled,
                    enabled = heroVisualControlsEnabled &&
                        settings.streamingShowcaseHeroEnabled &&
                        settings.streamingShowcaseVideoPreviewEnabled &&
                        homeHeroVideoPreviewSupported,
                    isTablet = isTablet,
                    highlighted = isNew(NuvioEnhancedFeature.HeroExperienceControls),
                    onCheckedChange = {
                        markSeen(NuvioEnhancedFeature.HeroExperienceControls)
                        NuvioEnhancedSettingsRepository.setStreamingShowcaseVideoPreviewSoundEnabled(it)
                    },
                )
                SettingsGroupDivider(isTablet = isTablet)
                SettingsSwitchRow(
                    title = stringResource(Res.string.nuvio_enhanced_compact_hero_title),
                    description = stringResource(Res.string.nuvio_enhanced_compact_hero_desc),
                    checked = settings.compactHeroMetadata,
                    enabled = heroVisualControlsEnabled,
                    isTablet = isTablet,
                    highlighted = isNew(NuvioEnhancedFeature.HeroExperienceControls),
                    onCheckedChange = {
                        markSeen(NuvioEnhancedFeature.HeroExperienceControls)
                        NuvioEnhancedSettingsRepository.setCompactHeroMetadata(it)
                    },
                )
                SettingsGroupDivider(isTablet = isTablet)
                SettingsSwitchRow(
                    title = stringResource(Res.string.nuvio_enhanced_hero_ratings_title),
                    description = stringResource(Res.string.nuvio_enhanced_hero_ratings_desc),
                    checked = settings.showHeroRatings,
                    enabled = heroVisualControlsEnabled,
                    isTablet = isTablet,
                    highlighted = isNew(NuvioEnhancedFeature.HeroExperienceControls),
                    onCheckedChange = {
                        markSeen(NuvioEnhancedFeature.HeroExperienceControls)
                        NuvioEnhancedSettingsRepository.setShowHeroRatings(it)
                    },
                )
                SettingsGroupDivider(isTablet = isTablet)
                SettingsSwitchRow(
                    title = stringResource(Res.string.nuvio_enhanced_ratings_above_metadata_title),
                    description = stringResource(Res.string.nuvio_enhanced_ratings_above_metadata_desc),
                    checked = settings.ratingsAboveMetadata,
                    enabled = heroVisualControlsEnabled && settings.showHeroRatings,
                    isTablet = isTablet,
                    highlighted = isNew(NuvioEnhancedFeature.HeroControlsV2),
                    onCheckedChange = {
                        markSeen(NuvioEnhancedFeature.HeroControlsV2)
                        NuvioEnhancedSettingsRepository.setRatingsAboveMetadata(it)
                    },
                )
                SettingsGroupDivider(isTablet = isTablet)
                SettingsSwitchRow(
                    title = stringResource(Res.string.nuvio_enhanced_hero_overview_title),
                    description = stringResource(Res.string.nuvio_enhanced_hero_overview_desc),
                    checked = settings.showHeroOverview,
                    enabled = heroVisualControlsEnabled,
                    isTablet = isTablet,
                    highlighted = isNew(NuvioEnhancedFeature.HeroExperienceControls),
                    onCheckedChange = {
                        markSeen(NuvioEnhancedFeature.HeroExperienceControls)
                        NuvioEnhancedSettingsRepository.setShowHeroOverview(it)
                    },
                )
                SettingsGroupDivider(isTablet = isTablet)
                SettingsSwitchRow(
                    title = stringResource(Res.string.nuvio_enhanced_hero_refresh_haptics_title),
                    description = stringResource(Res.string.nuvio_enhanced_hero_refresh_haptics_desc),
                    checked = settings.heroRefreshHapticsEnabled,
                    isTablet = isTablet,
                    highlighted = isNew(NuvioEnhancedFeature.HeroExperienceControls),
                    onCheckedChange = {
                        markSeen(NuvioEnhancedFeature.HeroExperienceControls)
                        NuvioEnhancedSettingsRepository.setHeroRefreshHapticsEnabled(it)
                    },
                )
                SettingsGroupDivider(isTablet = isTablet)
                SettingsSwitchRow(
                    title = stringResource(Res.string.settings_advanced_hero_auto_scroll),
                    description = stringResource(Res.string.settings_advanced_hero_auto_scroll_description),
                    checked = homeSettings.heroAutoScrollEnabled,
                    enabled = heroVisualControlsEnabled,
                    isTablet = isTablet,
                    highlighted = isNew(NuvioEnhancedFeature.HeroExperienceControls),
                    onCheckedChange = {
                        markSeen(NuvioEnhancedFeature.HeroExperienceControls)
                        HomeCatalogSettingsRepository.setHeroAutoScrollEnabled(it)
                    },
                )
                SettingsGroupDivider(isTablet = isTablet)
                SettingsSwitchRow(
                    title = stringResource(Res.string.settings_advanced_hero_motion_preview),
                    description = stringResource(Res.string.settings_advanced_hero_motion_preview_description),
                    checked = homeSettings.heroMotionPreviewEnabled,
                    enabled = heroVisualControlsEnabled,
                    isTablet = isTablet,
                    highlighted = isNew(NuvioEnhancedFeature.HeroExperienceControls),
                    onCheckedChange = {
                        markSeen(NuvioEnhancedFeature.HeroExperienceControls)
                        HomeCatalogSettingsRepository.setHeroMotionPreviewEnabled(it)
                    },
                )
            }
        }
        }

        if (section == MewSettingsSection.Details) {
            SettingsSection(
                title = stringResource(Res.string.nuvio_enhanced_detail_presentation_title),
                isTablet = isTablet,
            ) {
                SettingsGroup(isTablet = isTablet) {
                    SettingsSwitchRow(
                        title = stringResource(Res.string.nuvio_enhanced_cinematic_detail_header_title),
                        description = stringResource(Res.string.nuvio_enhanced_cinematic_detail_header_desc),
                        checked = settings.cinematicDetailHeaderEnabled,
                        isTablet = isTablet,
                        highlighted = isNew(NuvioEnhancedFeature.CinematicDetailHeader),
                        onCheckedChange = {
                            markSeen(NuvioEnhancedFeature.CinematicDetailHeader)
                            NuvioEnhancedSettingsRepository.setCinematicDetailHeaderEnabled(it)
                        },
                    )
                    SettingsGroupDivider(isTablet = isTablet)
                    EnhancedChoiceRow(
                        title = stringResource(Res.string.nuvio_enhanced_cinematic_header_content_title),
                        description = stringResource(Res.string.nuvio_enhanced_cinematic_header_content_desc),
                        selected = settings.cinematicHeaderContentMode,
                        options = listOf(
                            EnhancedChoiceOption(
                                CinematicHeaderContentMode.Productions,
                                stringResource(Res.string.nuvio_enhanced_cinematic_header_content_productions),
                            ),
                            EnhancedChoiceOption(
                                CinematicHeaderContentMode.WhereToWatch,
                                stringResource(Res.string.nuvio_enhanced_cinematic_header_content_where_to_watch),
                            ),
                            EnhancedChoiceOption(
                                CinematicHeaderContentMode.Hidden,
                                stringResource(Res.string.nuvio_enhanced_cinematic_header_content_hidden),
                            ),
                        ),
                        isTablet = isTablet,
                        enabled = settings.cinematicDetailHeaderEnabled,
                        highlighted = false,
                        onSelected = NuvioEnhancedSettingsRepository::setCinematicHeaderContentMode,
                    )
                    SettingsGroupDivider(isTablet = isTablet)
                    SettingsSwitchRow(
                        title = stringResource(Res.string.nuvio_enhanced_nuvio_read_title),
                        description = stringResource(Res.string.nuvio_enhanced_nuvio_read_desc),
                        checked = settings.nuvioReadEnabled,
                        isTablet = isTablet,
                        highlighted = isNew(NuvioEnhancedFeature.NuvioRead),
                        onCheckedChange = {
                            markSeen(NuvioEnhancedFeature.NuvioRead)
                            NuvioEnhancedSettingsRepository.setNuvioReadEnabled(it)
                        },
                    )
                    SettingsGroupDivider(isTablet = isTablet)
                    EnhancedChoiceRow(
                        title = stringResource(Res.string.nuvio_enhanced_episode_cards_layout_title),
                        description = stringResource(Res.string.nuvio_enhanced_episode_cards_layout_desc),
                        selected = detailSettings.episodeCardStyle,
                        options = listOf(
                            EnhancedChoiceOption(
                                MetaEpisodeCardStyle.Horizontal,
                                stringResource(Res.string.settings_meta_episode_style_horizontal),
                            ),
                            EnhancedChoiceOption(
                                MetaEpisodeCardStyle.List,
                                stringResource(Res.string.nuvio_enhanced_episode_cards_layout_compact_list),
                            ),
                            EnhancedChoiceOption(
                                MetaEpisodeCardStyle.VerticalHorizontal,
                                stringResource(Res.string.nuvio_enhanced_episode_cards_layout_vertical),
                            ),
                        ),
                        isTablet = isTablet,
                        highlighted = isNew(NuvioEnhancedFeature.DetailPresentationControlsV2),
                        onSelected = {
                            markSeen(NuvioEnhancedFeature.DetailPresentationControlsV2)
                            MetaScreenSettingsRepository.setEpisodeCardStyle(it)
                        },
                    )
                    SettingsGroupDivider(isTablet = isTablet)
                    SettingsSwitchRow(
                        title = stringResource(Res.string.nuvio_enhanced_show_download_button_title),
                        description = stringResource(Res.string.nuvio_enhanced_show_download_button_desc),
                        checked = detailSettings.showDownloadAction,
                        isTablet = isTablet,
                        highlighted = isNew(NuvioEnhancedFeature.DetailPresentationControlsV2),
                        onCheckedChange = {
                            markSeen(NuvioEnhancedFeature.DetailPresentationControlsV2)
                            MetaScreenSettingsRepository.setShowDownloadAction(it)
                        },
                    )
                }
            }
        }

        if (section == MewSettingsSection.Home) {
        SettingsSection(
            title = stringResource(Res.string.nuvio_enhanced_section_release_radar),
            isTablet = isTablet,
        ) {
            SettingsGroup(isTablet = isTablet) {
                SettingsSwitchRow(
                    title = stringResource(Res.string.nuvio_enhanced_library_only_radar_title),
                    description = stringResource(Res.string.nuvio_enhanced_library_only_radar_desc),
                    checked = settings.releaseRadarLibraryOnly,
                    isTablet = isTablet,
                    highlighted = isNew(NuvioEnhancedFeature.ReleaseRadarFilters),
                    onCheckedChange = {
                        markSeen(NuvioEnhancedFeature.ReleaseRadarFilters)
                        NuvioEnhancedSettingsRepository.setReleaseRadarLibraryOnly(it)
                    },
                )
                SettingsGroupDivider(isTablet = isTablet)
                EnhancedChoiceRow(
                    title = stringResource(Res.string.nuvio_enhanced_radar_window_title),
                    description = stringResource(Res.string.nuvio_enhanced_radar_window_desc),
                    selected = settings.releaseRadarWindowDays,
                    options = listOf(
                        EnhancedChoiceOption(7, stringResource(Res.string.nuvio_enhanced_radar_window_7_days)),
                        EnhancedChoiceOption(30, stringResource(Res.string.nuvio_enhanced_radar_window_30_days)),
                    ),
                    isTablet = isTablet,
                    highlighted = isNew(NuvioEnhancedFeature.ReleaseRadarFilters),
                    onSelected = {
                        markSeen(NuvioEnhancedFeature.ReleaseRadarFilters)
                        NuvioEnhancedSettingsRepository.setReleaseRadarWindowDays(it)
                    },
                )
                SettingsGroupDivider(isTablet = isTablet)
                EnhancedChoiceRow(
                    title = stringResource(Res.string.nuvio_enhanced_radar_content_title),
                    description = stringResource(Res.string.nuvio_enhanced_radar_content_desc),
                    selected = settings.releaseRadarContentFilter,
                    options = listOf(
                        EnhancedChoiceOption(
                            NuvioReleaseRadarContentFilter.All,
                            stringResource(Res.string.nuvio_enhanced_filter_all),
                        ),
                        EnhancedChoiceOption(
                            NuvioReleaseRadarContentFilter.Episodes,
                            stringResource(Res.string.nuvio_enhanced_filter_episodes),
                        ),
                        EnhancedChoiceOption(
                            NuvioReleaseRadarContentFilter.Movies,
                            stringResource(Res.string.nuvio_enhanced_filter_movies),
                        ),
                    ),
                    isTablet = isTablet,
                    highlighted = isNew(NuvioEnhancedFeature.ReleaseRadarFilters),
                    onSelected = {
                        markSeen(NuvioEnhancedFeature.ReleaseRadarFilters)
                        NuvioEnhancedSettingsRepository.setReleaseRadarContentFilter(it)
                    },
                )
            }
        }
        }

        if (section == MewSettingsSection.Details) {
        SettingsSection(
            title = stringResource(Res.string.nuvio_enhanced_section_details_experience),
            isTablet = isTablet,
        ) {
            SettingsGroup(isTablet = isTablet) {
                if (detailHeroTrailerPlaybackSupported) {
                    SettingsSwitchRow(
                        title = stringResource(Res.string.settings_meta_hero_trailer_playback),
                        description = stringResource(Res.string.settings_meta_hero_trailer_playback_description),
                        checked = detailSettings.heroTrailerPlayback,
                        isTablet = isTablet,
                        highlighted = isNew(NuvioEnhancedFeature.DetailExperienceControls),
                        onCheckedChange = {
                            markSeen(NuvioEnhancedFeature.DetailExperienceControls)
                            MetaScreenSettingsRepository.setHeroTrailerPlayback(it)
                        },
                    )
                    SettingsGroupDivider(isTablet = isTablet)
                    SettingsSwitchRow(
                        title = stringResource(Res.string.settings_meta_hero_trailer_sound),
                        description = stringResource(Res.string.settings_meta_hero_trailer_sound_description),
                        checked = detailSettings.heroTrailerSoundEnabled,
                        enabled = detailSettings.heroTrailerPlayback,
                        isTablet = isTablet,
                        highlighted = isNew(NuvioEnhancedFeature.DetailExperienceControls),
                        onCheckedChange = {
                            markSeen(NuvioEnhancedFeature.DetailExperienceControls)
                            MetaScreenSettingsRepository.setHeroTrailerSoundEnabled(it)
                        },
                    )
                    SettingsGroupDivider(isTablet = isTablet)
                }
                SettingsSwitchRow(
                    title = stringResource(Res.string.settings_meta_show_episode_ratings),
                    description = stringResource(Res.string.settings_meta_show_episode_ratings_description),
                    checked = detailSettings.showEpisodeRatings,
                    isTablet = isTablet,
                    highlighted = isNew(NuvioEnhancedFeature.DetailExperienceControls),
                    onCheckedChange = {
                        markSeen(NuvioEnhancedFeature.DetailExperienceControls)
                        MetaScreenSettingsRepository.setShowEpisodeRatings(it)
                    },
                )
            }
        }
        }

        if (section == MewSettingsSection.App) {
            SettingsSection(
                title = stringResource(Res.string.nuvio_enhanced_section_network),
                isTablet = isTablet,
            ) {
                SettingsGroup(isTablet = isTablet) {
                    SettingsNavigationRow(
                        title = stringResource(Res.string.nuvio_enhanced_section_network),
                        description = dnsOverHttpsSettings.provider.label,
                        isTablet = isTablet,
                        highlighted = isNew(NuvioEnhancedFeature.NetworkControls),
                        onClick = {
                            markSeen(NuvioEnhancedFeature.NetworkControls)
                            showDnsDialog = true
                        },
                    )
                }
            }
        }

        if (section == MewSettingsSection.Backup) {
            SettingsSection(
                title = stringResource(Res.string.nuvio_enhanced_section_backup_import),
                isTablet = isTablet,
            ) {
                SettingsGroup(isTablet = isTablet) {
                    SettingsNavigationRow(
                        title = stringResource(Res.string.nuvio_enhanced_backup_download_title),
                        description = stringResource(Res.string.nuvio_enhanced_backup_download_desc),
                        icon = Icons.Rounded.Backup,
                        isTablet = isTablet,
                        highlighted = isNew(NuvioEnhancedFeature.BackupImport),
                        onClick = {
                            markSeen(NuvioEnhancedFeature.BackupImport)
                            val payload = ProfileSettingsSync.exportBackupJson()
                            NuvioEnhancedBackupFileBridge.exportBackup(
                                fileName = "nuvio-backup.json",
                                payload = payload,
                            ) { result ->
                                result
                                    .onSuccess { NuvioToastController.show(backupFileReadyMessage) }
                                    .onFailure { error -> NuvioToastController.show(error.message ?: backupExportFailedMessage) }
                            }
                        },
                    )
                    SettingsGroupDivider(isTablet = isTablet)
                    SettingsNavigationRow(
                        title = stringResource(Res.string.nuvio_enhanced_backup_import_file_title),
                        description = stringResource(Res.string.nuvio_enhanced_backup_import_file_desc),
                        icon = Icons.Rounded.Restore,
                        isTablet = isTablet,
                        highlighted = isNew(NuvioEnhancedFeature.BackupImport),
                        onClick = {
                            markSeen(NuvioEnhancedFeature.BackupImport)
                            importError = null
                            NuvioEnhancedBackupFileBridge.importBackup { result ->
                                result
                                    .onSuccess(::importBackupPayload)
                                    .onFailure { error -> NuvioToastController.show(error.message ?: backupImportFailedMessage) }
                            }
                        },
                    )
                    SettingsGroupDivider(isTablet = isTablet)
                    SettingsNavigationRow(
                        title = stringResource(Res.string.nuvio_enhanced_backup_copy_title),
                        description = stringResource(Res.string.nuvio_enhanced_backup_copy_desc),
                        icon = Icons.Rounded.ContentCopy,
                        isTablet = isTablet,
                        highlighted = isNew(NuvioEnhancedFeature.BackupImport),
                        onClick = {
                            markSeen(NuvioEnhancedFeature.BackupImport)
                            val payload = ProfileSettingsSync.exportBackupJson()
                            backupPayload = payload
                            clipboardManager.setText(AnnotatedString(payload))
                            NuvioToastController.show(backupCopiedMessage)
                        },
                    )
                    SettingsGroupDivider(isTablet = isTablet)
                    SettingsNavigationRow(
                        title = stringResource(Res.string.nuvio_enhanced_backup_paste_title),
                        description = stringResource(Res.string.nuvio_enhanced_backup_paste_desc),
                        icon = Icons.Rounded.Download,
                        isTablet = isTablet,
                        highlighted = isNew(NuvioEnhancedFeature.BackupImport),
                        onClick = {
                            markSeen(NuvioEnhancedFeature.BackupImport)
                            importError = null
                            showImportDialog = true
                        },
                    )
                }
            }
        }

        if (section == MewSettingsSection.Downloads) {
            SettingsSection(
                title = stringResource(Res.string.nuvio_enhanced_section_downloads),
                isTablet = isTablet,
            ) {
                SettingsGroup(isTablet = isTablet) {
                    SettingsSwitchRow(
                        title = stringResource(Res.string.nuvio_enhanced_downloads_wifi_only_title),
                        description = stringResource(Res.string.nuvio_enhanced_downloads_wifi_only_desc),
                        checked = settings.downloadsWifiOnly,
                        isTablet = isTablet,
                        onCheckedChange = { NuvioEnhancedSettingsRepository.setDownloadsWifiOnly(it) },
                    )
                    SettingsGroupDivider(isTablet = isTablet)
                    SettingsSwitchRow(
                        title = stringResource(Res.string.nuvio_enhanced_downloads_season_batch_title),
                        description = stringResource(Res.string.nuvio_enhanced_downloads_season_batch_desc),
                        checked = settings.seasonBatchDownloadEnabled,
                        isTablet = isTablet,
                        onCheckedChange = { NuvioEnhancedSettingsRepository.setSeasonBatchDownloadEnabled(it) },
                    )
                    if (!isIos) {
                        SettingsGroupDivider(isTablet = isTablet)
                        SettingsNavigationRow(
                            title = stringResource(
                                if (externalFolderState.uri.isNullOrBlank()) {
                                    Res.string.nuvio_enhanced_external_folder_choose_title
                                } else {
                                    Res.string.nuvio_enhanced_external_folder_change_title
                                },
                            ),
                            description = when {
                                externalFolderState.unavailable -> stringResource(
                                    Res.string.nuvio_enhanced_external_folder_unavailable_description,
                                )
                                !externalFolderState.displayName.isNullOrBlank() -> externalFolderState.displayName.orEmpty()
                                else -> stringResource(Res.string.nuvio_enhanced_external_folder_description)
                            },
                            icon = Icons.Rounded.Folder,
                            isTablet = isTablet,
                            onClick = {
                                DownloadsExternalFolderPlatform.chooseFolder { result ->
                                    result.onFailure { error ->
                                        NuvioToastController.show(error.message ?: externalFolderFailedMessage)
                                    }
                                }
                            },
                        )
                        if (!externalFolderState.uri.isNullOrBlank()) {
                            SettingsGroupDivider(isTablet = isTablet)
                            SettingsNavigationRow(
                                title = stringResource(Res.string.nuvio_enhanced_external_folder_remove_title),
                                description = stringResource(Res.string.nuvio_enhanced_external_folder_remove_description),
                                icon = Icons.Rounded.Delete,
                                isTablet = isTablet,
                                onClick = DownloadsExternalFolderPlatform::clearFolder,
                            )
                        }
                    }
                }
            }
        }

        if (section == MewSettingsSection.App) {
            SettingsSection(
                title = stringResource(Res.string.nuvio_enhanced_section_discovery),
                isTablet = isTablet,
            ) {
                SettingsGroup(isTablet = isTablet) {
                    SettingsSwitchRow(
                        title = stringResource(Res.string.nuvio_enhanced_highlight_title),
                        description = stringResource(Res.string.nuvio_enhanced_highlight_desc),
                        checked = settings.featureHighlightsEnabled,
                        isTablet = isTablet,
                        highlighted = isNew(NuvioEnhancedFeature.FeatureHighlights),
                        onCheckedChange = {
                            markSeen(NuvioEnhancedFeature.FeatureHighlights)
                            NuvioEnhancedSettingsRepository.setFeatureHighlightsEnabled(it)
                        },
                    )
                }
            }
        }

        if (section == MewSettingsSection.Hub) {
            SettingsSection(
                title = stringResource(Res.string.nuvio_enhanced_footer_community),
                isTablet = isTablet,
            ) {
                SettingsGroup(isTablet = isTablet) {
                    SettingsNavigationRow(
                        title = stringResource(Res.string.nuvio_enhanced_footer_github),
                        description = stringResource(Res.string.mew_settings_github_desc),
                        iconPainter = appIconPainter(AppIconResource.GithubMark),
                        isTablet = isTablet,
                        highlighted = isNew(NuvioEnhancedFeature.CommunityLinks),
                        onClick = {
                            markSeen(NuvioEnhancedFeature.CommunityLinks)
                            uriHandler.openUri(NuvioEnhancedGithubUrl)
                        },
                    )
                    SettingsGroupDivider(isTablet = isTablet)
                    SettingsNavigationRow(
                        title = stringResource(Res.string.nuvio_enhanced_footer_telegram),
                        description = stringResource(Res.string.mew_settings_telegram_desc),
                        icon = Icons.AutoMirrored.Rounded.Send,
                        isTablet = isTablet,
                        highlighted = isNew(NuvioEnhancedFeature.CommunityLinks),
                        onClick = {
                            markSeen(NuvioEnhancedFeature.CommunityLinks)
                            uriHandler.openUri(MewTelegramUrl)
                        },
                    )
                }
            }
            SettingsSection(
                title = stringResource(Res.string.support_nuvio_title),
                isTablet = isTablet,
            ) {
                SettingsGroup(isTablet = isTablet) {
                    SettingsNavigationRow(
                        title = stringResource(Res.string.mew_support_tyvm),
                        description = stringResource(Res.string.support_nuvio_description),
                        icon = Icons.Rounded.Favorite,
                        isTablet = isTablet,
                        onClick = { uriHandler.openUri(MewDonateUrl) },
                    )
                    SettingsGroupDivider(isTablet = isTablet)
                    SettingsNavigationRow(
                        title = stringResource(Res.string.mew_support_upi),
                        description = stringResource(Res.string.settings_nuvio_enhanced_donate_description),
                        icon = Icons.Rounded.Payments,
                        isTablet = isTablet,
                        onClick = { uriHandler.openUri(MewUpiUrl) },
                    )
                }
            }
        }

        if (section == MewSettingsSection.Diagnostics && CrashDiagnostics.reportsSupported) {
            SettingsSection(
                title = stringResource(Res.string.nuvio_enhanced_section_diagnostics),
                isTablet = isTablet,
            ) {
                SettingsGroup(isTablet = isTablet) {
                    SettingsNavigationRow(
                        title = stringResource(Res.string.nuvio_enhanced_copy_diagnostics_title),
                        description = stringResource(Res.string.nuvio_enhanced_copy_diagnostics_desc),
                        icon = Icons.Rounded.ContentCopy,
                        isTablet = isTablet,
                        onClick = {
                            clipboardManager.setText(AnnotatedString(CrashDiagnostics.currentReport()))
                            NuvioToastController.show(diagnosticsCopiedMessage)
                        },
                    )
                    SettingsGroupDivider(isTablet = isTablet)
                    SettingsNavigationRow(
                        title = stringResource(Res.string.nuvio_enhanced_copy_last_crash_title),
                        description = lastCrashReport?.let { report ->
                            listOf(report.contextSummary, report.summary)
                                .filter(String::isNotBlank)
                                .joinToString("\n")
                        } ?: stringResource(Res.string.nuvio_enhanced_copy_last_crash_empty_desc),
                        icon = Icons.Rounded.ContentCopy,
                        enabled = lastCrashReport != null,
                        isTablet = isTablet,
                        onClick = {
                            val report = lastCrashReport ?: return@SettingsNavigationRow
                            clipboardManager.setText(AnnotatedString(report.details))
                            NuvioToastController.show(crashCopiedMessage)
                        },
                    )
                }
            }
        }

    }

    backupPayload?.let { payload ->
        BackupPayloadDialog(
            payload = payload,
            onDismiss = { backupPayload = null },
            onCopy = {
                clipboardManager.setText(AnnotatedString(payload))
                NuvioToastController.show(backupCopiedMessage)
            },
        )
    }

    if (showDnsDialog) {
        DnsOverHttpsProviderDialog(
            selected = dnsOverHttpsSettings.provider,
            onSelected = { provider ->
                DnsOverHttpsSettingsRepository.setProvider(provider)
                showDnsDialog = false
            },
            onDismiss = { showDnsDialog = false },
        )
    }

    if (showImportDialog) {
        ImportBackupDialog(
            payload = importPayload,
            error = importError,
            onPayloadChange = {
                importPayload = it
                importError = null
            },
            onDismiss = {
                showImportDialog = false
                importPayload = ""
                importError = null
            },
            onImport = {
                importBackupPayload(importPayload)
            },
        )
    }
}

private data class EnhancedChoiceOption<T>(
    val value: T,
    val label: String,
)

/** Option chips in the standard settings style (same layout as SettingsChipRow). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T> EnhancedChoiceRow(
    title: String,
    description: String,
    selected: T,
    options: List<EnhancedChoiceOption<T>>,
    isTablet: Boolean,
    enabled: Boolean = true,
    highlighted: Boolean,
    onSelected: (T) -> Unit,
) {
    val tokens = MaterialTheme.nuvio
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = if (isTablet) 20.dp else 16.dp,
                vertical = if (isTablet) 16.dp else 14.dp,
            )
            .alpha(if (enabled) NuvioTokens.Opacity.visible else tokens.opacity.medium),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            SettingsRowTitle(title = title, highlighted = highlighted)
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = tokens.colors.textMuted,
            )
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            options.forEach { option ->
                Chip(
                    label = option.label,
                    selected = option.value == selected,
                    enabled = enabled,
                    onClick = { onSelected(option.value) },
                )
            }
        }
    }
}

@Composable
private fun DnsOverHttpsProviderDialog(
    selected: DnsOverHttpsProvider,
    onSelected: (DnsOverHttpsProvider) -> Unit,
    onDismiss: () -> Unit,
) {
    DialogSurface(
        onDismissRequest = onDismiss,
        title = stringResource(Res.string.nuvio_enhanced_section_network),
        message = stringResource(Res.string.settings_advanced_doh_description),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(NuvioTokens.Space.s8)) {
            DnsOverHttpsProvider.entries.forEach { provider ->
                DialogOption(
                    text = provider.label,
                    selected = provider == selected,
                    onClick = { onSelected(provider) },
                )
            }
        }
        DialogButtons {
            DialogButton(
                text = stringResource(Res.string.action_cancel),
                onClick = onDismiss,
            )
        }
    }
}

@Composable
private fun BackupPayloadDialog(
    payload: String,
    onDismiss: () -> Unit,
    onCopy: () -> Unit,
) {
    DialogSurface(
        onDismissRequest = onDismiss,
        title = stringResource(Res.string.nuvio_enhanced_backup_ready_title),
        message = stringResource(Res.string.nuvio_enhanced_backup_ready_desc),
    ) {
        MewBackupTextField(
            value = payload,
            onValueChange = {},
            readOnly = true,
            minHeight = 160.dp,
            maxLines = 8,
        )
        DialogButtons {
            DialogButton(
                text = stringResource(Res.string.nuvio_enhanced_close),
                onClick = onDismiss,
            )
            DialogButton(
                text = stringResource(Res.string.nuvio_enhanced_backup_copy_again),
                onClick = onCopy,
                style = DialogButtonStyle.Primary,
            )
        }
    }
}

@Composable
private fun ImportBackupDialog(
    payload: String,
    error: String?,
    onPayloadChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onImport: () -> Unit,
) {
    val tokens = MaterialTheme.nuvio
    DialogSurface(
        onDismissRequest = onDismiss,
        title = stringResource(Res.string.nuvio_enhanced_import_title),
        message = stringResource(Res.string.nuvio_enhanced_import_desc),
    ) {
        MewBackupTextField(
            value = payload,
            onValueChange = onPayloadChange,
            placeholder = stringResource(Res.string.nuvio_enhanced_import_placeholder),
            minHeight = 180.dp,
            maxLines = 10,
        )
        if (!error.isNullOrBlank()) {
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = tokens.colors.danger,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
        DialogButtons {
            DialogButton(
                text = stringResource(Res.string.nuvio_enhanced_cancel),
                onClick = onDismiss,
            )
            DialogButton(
                text = stringResource(Res.string.nuvio_enhanced_import_action),
                onClick = onImport,
                enabled = payload.isNotBlank(),
                style = DialogButtonStyle.Primary,
            )
        }
    }
}

/** Multi-line JSON field styled like NuvioInputField. */
@Composable
private fun MewBackupTextField(
    value: String,
    onValueChange: (String) -> Unit,
    minHeight: Dp,
    maxLines: Int,
    readOnly: Boolean = false,
    placeholder: String? = null,
) {
    val tokens = MaterialTheme.nuvio
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        readOnly = readOnly,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = minHeight),
        maxLines = maxLines,
        shape = tokens.shapes.compactCard,
        textStyle = MaterialTheme.typography.bodySmall.copy(color = tokens.colors.textPrimary),
        placeholder = placeholder?.let {
            { Text(text = it, color = tokens.colors.textMuted, style = MaterialTheme.typography.bodyMedium) }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = tokens.colors.borderFocus,
            unfocusedBorderColor = tokens.colors.borderDefault,
            focusedContainerColor = tokens.colors.surfaceCard,
            unfocusedContainerColor = tokens.colors.surfaceCard,
            cursorColor = tokens.colors.accent,
        ),
    )
}
