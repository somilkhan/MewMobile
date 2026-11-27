package com.nuvio.app.features.settings

// Mew-only settings-search entries. Upstream SettingsSearch.kt appends these with one line.

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.nuvio.app.isIos
import nuvio.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun mewSettingsSearchEntries(): List<SettingsSearchEntry> {
    val entries = mutableListOf<SettingsSearchEntry>()
    val enhancedCategory = stringResource(SettingsCategory.System.labelRes)
    val nuvioEnhancedPage = stringResource(Res.string.settings_nuvio_enhanced_title)

    fun addPage(
        page: SettingsPage,
        key: String,
        title: String,
        description: String,
        category: String = enhancedCategory,
        icon: ImageVector,
    ) {
        entries += SettingsSearchEntry(
            key = "mew-$key",
            title = title,
            description = description,
            page = title,
            section = "",
            category = category,
            icon = icon,
            target = SettingsSearchTarget.Page(page),
        )
    }

    fun addRow(
        page: SettingsPage,
        key: String,
        title: String,
        description: String = "",
        pageLabel: String,
        section: String,
        category: String = enhancedCategory,
        icon: ImageVector,
    ) {
        entries += SettingsSearchEntry(
            key = "mew-$key",
            title = title,
            description = description,
            page = pageLabel,
            section = section,
            category = category,
            icon = icon,
            target = SettingsSearchTarget.Page(page),
        )
    }

    addPage(
        page = SettingsPage.CloudStream,
        key = "cloudstream",
        title = stringResource(Res.string.compose_settings_page_cloudstream),
        description = stringResource(Res.string.compose_settings_page_cloudstream),
        category = stringResource(SettingsCategory.Watch.labelRes),
        icon = SettingsPage.CloudStream.icon,
    )
    addPage(
        page = SettingsPage.AiAssistant,
        key = "ai-assistant",
        title = stringResource(Res.string.compose_settings_page_ai_assistant),
        description = stringResource(Res.string.compose_settings_page_ai_assistant),
        category = stringResource(SettingsCategory.Services.labelRes),
        icon = SettingsPage.AiAssistant.icon,
    )
    addPage(
        page = SettingsPage.NuvioEnhanced,
        key = "nuvio-enhanced",
        title = nuvioEnhancedPage,
        description = stringResource(Res.string.settings_nuvio_enhanced_description),
        category = enhancedCategory,
        icon = Icons.Rounded.AutoAwesome,
    )
    addRow(
        page = SettingsPage.MewDetails,
        key = "cinematic-detail-header",
        title = stringResource(Res.string.nuvio_enhanced_cinematic_detail_header_title),
        description = stringResource(Res.string.nuvio_enhanced_cinematic_detail_header_desc),
        pageLabel = stringResource(SettingsPage.MewDetails.titleRes),
        section = stringResource(Res.string.nuvio_enhanced_detail_presentation_title),
        category = enhancedCategory,
        icon = Icons.Rounded.AutoAwesome,
    )
    addRow(
        page = SettingsPage.MewDetails,
        key = "cinematic-header-content",
        title = stringResource(Res.string.nuvio_enhanced_cinematic_header_content_title),
        description = stringResource(Res.string.nuvio_enhanced_cinematic_header_content_desc),
        pageLabel = stringResource(SettingsPage.MewDetails.titleRes),
        section = stringResource(Res.string.nuvio_enhanced_detail_presentation_title),
        category = enhancedCategory,
        icon = Icons.Rounded.AutoAwesome,
    )
    addRow(
        page = SettingsPage.NuvioEnhanced,
        key = "support-nuvio-enhanced",
        title = stringResource(Res.string.support_nuvio_title),
        description = stringResource(Res.string.support_nuvio_description),
        pageLabel = nuvioEnhancedPage,
        section = enhancedCategory,
        category = enhancedCategory,
        icon = Icons.Rounded.Favorite,
    )
    addRow(
        page = SettingsPage.MewHero,
        key = "hero-auto-scroll",
        title = stringResource(Res.string.settings_advanced_hero_auto_scroll),
        description = stringResource(Res.string.settings_advanced_hero_auto_scroll_description),
        pageLabel = stringResource(SettingsPage.MewHero.titleRes),
        section = stringResource(Res.string.nuvio_enhanced_section_hero_experience),
        category = enhancedCategory,
        icon = Icons.Rounded.AutoAwesome,
    )
    addRow(
        page = SettingsPage.MewHero,
        key = "hero-motion-preview",
        title = stringResource(Res.string.settings_advanced_hero_motion_preview),
        description = stringResource(Res.string.settings_advanced_hero_motion_preview_description),
        pageLabel = stringResource(SettingsPage.MewHero.titleRes),
        section = stringResource(Res.string.nuvio_enhanced_section_hero_experience),
        category = enhancedCategory,
        icon = Icons.Rounded.AutoAwesome,
    )
    addRow(
        page = SettingsPage.MewHero,
        key = "streaming-showcase-hero",
        title = stringResource(Res.string.nuvio_enhanced_showcase_hero_title),
        description = stringResource(Res.string.nuvio_enhanced_showcase_hero_desc),
        pageLabel = stringResource(SettingsPage.MewHero.titleRes),
        section = stringResource(Res.string.nuvio_enhanced_section_hero_experience),
        category = enhancedCategory,
        icon = Icons.Rounded.AutoAwesome,
    )
    addRow(
        page = SettingsPage.MewHero,
        key = "streaming-showcase-video-preview",
        title = stringResource(Res.string.nuvio_enhanced_showcase_video_preview_title),
        description = stringResource(Res.string.nuvio_enhanced_showcase_video_preview_desc),
        pageLabel = stringResource(SettingsPage.MewHero.titleRes),
        section = stringResource(Res.string.nuvio_enhanced_section_hero_experience),
        category = enhancedCategory,
        icon = Icons.Rounded.AutoAwesome,
    )
    addRow(
        page = SettingsPage.MewHero,
        key = "streaming-showcase-video-preview-sound",
        title = stringResource(Res.string.nuvio_enhanced_showcase_video_preview_sound_title),
        description = stringResource(Res.string.nuvio_enhanced_showcase_video_preview_sound_desc),
        pageLabel = stringResource(SettingsPage.MewHero.titleRes),
        section = stringResource(Res.string.nuvio_enhanced_section_hero_experience),
        category = enhancedCategory,
        icon = Icons.Rounded.AutoAwesome,
    )
    addRow(
        page = SettingsPage.MewDetails,
        key = "hero-trailer-playback",
        title = stringResource(Res.string.settings_meta_hero_trailer_playback),
        description = stringResource(Res.string.settings_meta_hero_trailer_playback_description),
        pageLabel = stringResource(SettingsPage.MewDetails.titleRes),
        section = stringResource(Res.string.nuvio_enhanced_section_details_experience),
        category = enhancedCategory,
        icon = Icons.Rounded.PlayArrow,
    )
    addRow(
        page = SettingsPage.MewDetails,
        key = "hero-trailer-sound",
        title = stringResource(Res.string.settings_meta_hero_trailer_sound),
        description = stringResource(Res.string.settings_meta_hero_trailer_sound_description),
        pageLabel = stringResource(SettingsPage.MewDetails.titleRes),
        section = stringResource(Res.string.nuvio_enhanced_section_details_experience),
        category = enhancedCategory,
        icon = Icons.Rounded.PlayArrow,
    )
    addRow(
        page = SettingsPage.MewPlayer,
        key = "random-next-episode",
        title = stringResource(Res.string.settings_playback_random_next_episode),
        description = stringResource(Res.string.settings_playback_random_next_episode_description),
        pageLabel = stringResource(SettingsPage.MewPlayer.titleRes),
        section = stringResource(Res.string.nuvio_enhanced_section_player_tools),
        category = enhancedCategory,
        icon = Icons.Rounded.PlayArrow,
    )
    addRow(
        page = SettingsPage.MewDetails,
        key = "show-episode-ratings",
        title = stringResource(Res.string.settings_meta_show_episode_ratings),
        description = stringResource(Res.string.settings_meta_show_episode_ratings_description),
        pageLabel = stringResource(SettingsPage.MewDetails.titleRes),
        section = stringResource(Res.string.nuvio_enhanced_section_details_experience),
        category = enhancedCategory,
        icon = Icons.Rounded.PlayArrow,
    )
    addRow(
        page = SettingsPage.MewPlayer,
        key = "content-warnings",
        title = stringResource(Res.string.settings_playback_parental_guide),
        description = stringResource(Res.string.settings_playback_parental_guide_description),
        pageLabel = stringResource(SettingsPage.MewPlayer.titleRes),
        section = stringResource(Res.string.nuvio_enhanced_section_player_tools),
        category = enhancedCategory,
        icon = Icons.Rounded.PlayArrow,
    )
    addRow(
        page = SettingsPage.MewPlayer,
        key = "background-stream-prefetch",
        title = stringResource(Res.string.nuvio_enhanced_background_stream_prefetch_title),
        description = stringResource(Res.string.nuvio_enhanced_background_stream_prefetch_desc),
        pageLabel = stringResource(SettingsPage.MewPlayer.titleRes),
        section = stringResource(Res.string.nuvio_enhanced_section_player_tools),
        category = enhancedCategory,
        icon = Icons.Rounded.PlayArrow,
    )
    addRow(
        page = SettingsPage.MewHome,
        key = "enhanced-ready-badge",
        title = stringResource(Res.string.settings_continue_watching_ready_badge_title),
        description = stringResource(Res.string.settings_continue_watching_ready_badge_description),
        pageLabel = stringResource(SettingsPage.MewHome.titleRes),
        section = stringResource(Res.string.nuvio_enhanced_section_nuvio_experience),
        category = enhancedCategory,
        icon = Icons.Rounded.AutoAwesome,
    )
    if (!isIos) {
        addRow(
            page = SettingsPage.MewPlayer,
            key = "memory-safe-buffer",
            title = stringResource(Res.string.settings_playback_android_memory_safe_buffer),
            description = stringResource(Res.string.settings_playback_android_memory_safe_buffer_description),
            pageLabel = stringResource(SettingsPage.MewPlayer.titleRes),
            section = stringResource(Res.string.nuvio_enhanced_section_player_tools),
            category = enhancedCategory,
            icon = Icons.Rounded.PlayArrow,
        )
    }
    MewSettingsSection.entries
        .filter { it != MewSettingsSection.Hub && it.isAvailable }
        .forEach { section ->
            addPage(
                page = section.page,
                key = "page-${section.name.lowercase()}",
                title = stringResource(section.page.titleRes),
                description = section.descriptionRes?.let { stringResource(it) }.orEmpty(),
                icon = section.page.icon,
            )
        }
    return entries
}
