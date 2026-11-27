package com.nuvio.app.features.settings

// Mew settings hub.
//
// Settings > Mew used to be one very long page with a category chip bar whose filters
// overlapped. It is now a hub: one navigation row per area, each opening its own sub-page.
// The rows reuse the existing NuvioEnhancedSettingsPage blocks, gated by MewSettingsSection,
// so no setting moved storage or changed behaviour.

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.nuvio.app.core.diagnostics.CrashDiagnostics
import com.nuvio.app.core.ui.NuvioActionLabel
import com.nuvio.app.core.ui.nuvio
import nuvio.composeapp.generated.resources.*
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

internal enum class MewSettingsSection(
    val page: SettingsPage,
    val descriptionRes: StringResource?,
    /** Features whose "New" highlight lives on this sub-page; drives the hub badge. */
    val features: Set<NuvioEnhancedFeature>,
) {
    Hub(SettingsPage.NuvioEnhanced, null, emptySet()),
    Home(
        SettingsPage.MewHome,
        Res.string.mew_settings_page_home_desc,
        setOf(
            NuvioEnhancedFeature.HomeExperienceControls,
            NuvioEnhancedFeature.SmartResume2,
            NuvioEnhancedFeature.SmartShelfComposer,
            NuvioEnhancedFeature.ReleaseRadarDigest,
            NuvioEnhancedFeature.ReleaseRadarFilters,
            NuvioEnhancedFeature.QuietHomeMode,
            NuvioEnhancedFeature.LibraryHealth,
        ),
    ),
    Hero(
        SettingsPage.MewHero,
        Res.string.mew_settings_page_hero_desc,
        setOf(NuvioEnhancedFeature.HeroControlsV2, NuvioEnhancedFeature.HeroExperienceControls),
    ),
    Details(
        SettingsPage.MewDetails,
        Res.string.mew_settings_page_details_desc,
        setOf(
            NuvioEnhancedFeature.CinematicDetailHeader,
            NuvioEnhancedFeature.DetailPresentationControlsV2,
            NuvioEnhancedFeature.DetailExperienceControls,
            NuvioEnhancedFeature.NuvioRead,
        ),
    ),
    Player(
        SettingsPage.MewPlayer,
        Res.string.mew_settings_page_player_desc,
        setOf(
            NuvioEnhancedFeature.SubtitleSyncMenu,
            NuvioEnhancedFeature.SubtitleSelectorStyle,
            NuvioEnhancedFeature.AudioSelectorStyle,
            NuvioEnhancedFeature.NextEpisodeButton,
            NuvioEnhancedFeature.PlayerTimeOverlay,
            NuvioEnhancedFeature.PersistentEpisodeShuffle,
            NuvioEnhancedFeature.PlayerStatusOverlay,
            NuvioEnhancedFeature.BackgroundStreamPrefetch,
            NuvioEnhancedFeature.ContentWarnings,
        ),
    ),
    App(
        SettingsPage.MewApp,
        Res.string.mew_settings_page_app_desc,
        setOf(
            NuvioEnhancedFeature.StatusBarVisibility,
            NuvioEnhancedFeature.LiveTvControls,
            NuvioEnhancedFeature.FeatureHighlights,
        ),
    ),
    Downloads(SettingsPage.MewDownloads, Res.string.mew_settings_page_downloads_desc, emptySet()),
    Backup(SettingsPage.MewBackup, Res.string.mew_settings_page_backup_desc, setOf(NuvioEnhancedFeature.BackupImport)),
    Diagnostics(SettingsPage.MewDiagnostics, Res.string.mew_settings_page_diagnostics_desc, emptySet());

    val isAvailable: Boolean
        get() = this != Diagnostics || CrashDiagnostics.reportsSupported

    companion object {
        fun forPage(page: SettingsPage): MewSettingsSection? = entries.firstOrNull { it.page == page }
        val customize = listOf(Home, Hero, Details, Player)
        val appAndData = listOf(App, Downloads, Backup, Diagnostics)
    }
}

/** Navigation rows shown at the top of the Mew hub. */
@Composable
internal fun MewSettingsHubRows(
    isTablet: Boolean,
    settings: NuvioEnhancedSettingsUiState,
    onPageChange: (SettingsPage) -> Unit,
    onMarkAllSeen: () -> Unit = {},
) {
    Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.nuvio.spacing.listGap)) {
        MewSettingsHubGroup(
            title = stringResource(Res.string.mew_settings_hub_group_customize),
            sections = MewSettingsSection.customize,
            isTablet = isTablet,
            settings = settings,
            onPageChange = onPageChange,
            onMarkAllSeen = onMarkAllSeen.takeIf { settings.hasNewFeatures },
        )
        MewSettingsHubGroup(
            title = stringResource(Res.string.mew_settings_hub_group_app_data),
            sections = MewSettingsSection.appAndData,
            isTablet = isTablet,
            settings = settings,
            onPageChange = onPageChange,
        )
    }
}

@Composable
private fun MewSettingsHubGroup(
    title: String,
    sections: List<MewSettingsSection>,
    isTablet: Boolean,
    settings: NuvioEnhancedSettingsUiState,
    onPageChange: (SettingsPage) -> Unit,
    onMarkAllSeen: (() -> Unit)? = null,
) {
    val visible = sections.filter { it.isAvailable }
    if (visible.isEmpty()) return
    SettingsSection(
        title = title,
        isTablet = isTablet,
        actions = {
            if (onMarkAllSeen != null) {
                NuvioActionLabel(
                    text = stringResource(Res.string.nuvio_enhanced_mark_all_seen),
                    onClick = onMarkAllSeen,
                )
            }
        },
    ) {
        SettingsGroup(isTablet = isTablet) {
            visible.forEachIndexed { index, section ->
                if (index > 0) SettingsGroupDivider(isTablet = isTablet)
                SettingsNavigationRow(
                    title = stringResource(section.page.titleRes),
                    description = section.descriptionRes?.let { stringResource(it) },
                    icon = section.page.icon,
                    isTablet = isTablet,
                    highlighted = section.features.any(settings::isNew),
                    onClick = { onPageChange(section.page) },
                )
            }
        }
    }
}
