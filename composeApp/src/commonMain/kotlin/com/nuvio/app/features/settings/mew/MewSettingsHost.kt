package com.nuvio.app.features.settings

// Mew-only settings pages and host callbacks.
//
// The upstream (Luqman) SettingsScreen stays as close to upstream as possible. Everything
// Mew needs inside it is reached through:
//   * `LocalMewSettingsHost` – callbacks/state supplied by Mew's App shell, read once per screen.
//   * `mewSettingsPageContent(...)` – renders the Mew-only SettingsPage entries, one `when` branch.
//   * `mewSettingsHeaderTitleStyle(page)` – Mew header styling overrides.
// Keep new Mew pages here so upstream merges only ever touch the single hook lines.

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import com.nuvio.app.core.build.AppFeaturePolicy
import com.nuvio.app.features.ai.AiAssistantSettings
import com.nuvio.app.features.ai.AiAssistantSettingsRepository
import com.nuvio.app.features.home.HomeCatalogSettingsRepository
import com.nuvio.app.features.details.FavoritePerson
import com.nuvio.app.features.watchprogress.ContinueWatchingItem

@Immutable
internal data class MewSettingsHost(
    val onDownloadsClick: (() -> Unit)? = null,
    val onContinueWatchingItemClick: ((ContinueWatchingItem) -> Unit)? = null,
    val onFavoritePersonClick: ((FavoritePerson) -> Unit)? = null,
    val homescreenHideCatalogUnderline: Boolean = false,
    val aiAssistantSettings: AiAssistantSettings = AiAssistantSettings(),
)

internal val LocalMewSettingsHost = staticCompositionLocalOf { MewSettingsHost() }

@Composable
internal fun ProvideMewSettingsHost(
    onDownloadsClick: (() -> Unit)?,
    onContinueWatchingItemClick: ((ContinueWatchingItem) -> Unit)?,
    onFavoritePersonClick: ((FavoritePerson) -> Unit)?,
    content: @Composable () -> Unit,
) {
    val aiAssistantSettings by remember {
        AiAssistantSettingsRepository.ensureLoaded()
        AiAssistantSettingsRepository.uiState
    }.collectAsState()
    val homeCatalogSettings by HomeCatalogSettingsRepository.uiState.collectAsState()
    val host = MewSettingsHost(
        onDownloadsClick = onDownloadsClick,
        onContinueWatchingItemClick = onContinueWatchingItemClick,
        onFavoritePersonClick = onFavoritePersonClick,
        homescreenHideCatalogUnderline = homeCatalogSettings.hideCatalogUnderline,
        aiAssistantSettings = aiAssistantSettings,
    )
    CompositionLocalProvider(LocalMewSettingsHost provides host, content = content)
}

internal val SettingsPage.isMewPage: Boolean
    get() = this == SettingsPage.FavoriteActors ||
        this == SettingsPage.NuvioEnhanced ||
        this == SettingsPage.CloudStream ||
        this == SettingsPage.AiAssistant

/** Renders the Mew-only settings pages. Called from one `when` branch per layout. */
internal fun LazyListScope.mewSettingsPageContent(
    page: SettingsPage,
    isTablet: Boolean,
    host: MewSettingsHost,
) {
    when (page) {
        SettingsPage.FavoriteActors -> favoritePeopleContent(
            isTablet = isTablet,
            onFavoritePersonClick = host.onFavoritePersonClick,
        )
        SettingsPage.NuvioEnhanced -> nuvioEnhancedSettingsContent(isTablet = isTablet)
        SettingsPage.CloudStream -> if (AppFeaturePolicy.pluginsEnabled) {
            cloudStreamSettingsContent()
        } else {
            addonsSettingsContent()
        }
        SettingsPage.AiAssistant -> aiAssistantSettingsContent(
            isTablet = isTablet,
            settings = host.aiAssistantSettings,
        )
        else -> Unit
    }
}

@Composable
internal fun mewSettingsHeaderTitleStyle(page: SettingsPage): TextStyle =
    if (page == SettingsPage.FavoriteActors) {
        MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold)
    } else {
        MaterialTheme.typography.displayLarge
    }
