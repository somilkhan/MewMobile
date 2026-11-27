package com.nuvio.app.features.settings

// Mew-only tracking cards: MyAnimeList (+ legacy Mew AniList while connected) and the
// "unavailable" support panel. Upstream TrackingProviderCards.kt calls these via `// Mew hook`.

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.nuvio.app.core.ui.NuvioLoadingIndicator
import com.nuvio.app.core.ui.NuvioTokens
import com.nuvio.app.core.ui.nuvio
import com.nuvio.app.features.profiles.ProfileRepository
import com.nuvio.app.features.anime.AnimeTrackingAuthUiState
import com.nuvio.app.features.anime.AniListTrackingRepository
import com.nuvio.app.features.anime.MyAnimeListTrackingRepository
import com.nuvio.app.features.anime.AnimeTrackingProvider
import com.nuvio.app.features.anime.AnimeTrackingProviderPreferences
import com.nuvio.app.features.anime.AnimeTrackingSettingsRepository
import com.nuvio.app.features.simkl.SimklAuthError
import com.nuvio.app.features.simkl.SimklAuthRepository
import com.nuvio.app.features.simkl.SimklAuthUiState
import com.nuvio.app.features.simkl.SimklBrandAsset
import com.nuvio.app.features.simkl.SimklConnectionMode
import com.nuvio.app.features.simkl.SimklSyncRepository
import com.nuvio.app.features.simkl.simklBrandPainter
import com.nuvio.app.features.tracking.TrackingProviderId
import com.nuvio.app.features.tracking.TrackingRefreshIntent
import com.nuvio.app.features.trakt.TraktAuthRepository
import com.nuvio.app.features.trakt.TraktAuthUiState
import com.nuvio.app.features.trakt.TraktBrandAsset
import com.nuvio.app.features.trakt.TraktConnectionMode
import com.nuvio.app.features.trakt.traktBrandPainter
import com.nuvio.app.features.watchprogress.WatchProgressSourceCoordinator
import kotlinx.coroutines.launch
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.action_donate
import nuvio.composeapp.generated.resources.action_cancel
import nuvio.composeapp.generated.resources.community_donation_progress_fee_notice
import nuvio.composeapp.generated.resources.logo_anilist
import nuvio.composeapp.generated.resources.logo_mal
import nuvio.composeapp.generated.resources.settings_anime_approval_description
import nuvio.composeapp.generated.resources.settings_anime_connect
import nuvio.composeapp.generated.resources.settings_anime_connected_as
import nuvio.composeapp.generated.resources.settings_anime_connected_description
import nuvio.composeapp.generated.resources.settings_anime_disconnect
import nuvio.composeapp.generated.resources.settings_anime_finish_sign_in
import nuvio.composeapp.generated.resources.settings_anime_missing_client_id
import nuvio.composeapp.generated.resources.settings_anime_open_login
import nuvio.composeapp.generated.resources.settings_anime_automatic_scrobble
import nuvio.composeapp.generated.resources.settings_anime_explicit_watched_sync
import nuvio.composeapp.generated.resources.settings_anime_sign_in_description
import nuvio.composeapp.generated.resources.settings_anime_visit
import nuvio.composeapp.generated.resources.settings_simkl_authorization_expired
import nuvio.composeapp.generated.resources.settings_simkl_authorization_revoked
import nuvio.composeapp.generated.resources.settings_simkl_connect
import nuvio.composeapp.generated.resources.settings_simkl_connected_as
import nuvio.composeapp.generated.resources.settings_simkl_connected_description
import nuvio.composeapp.generated.resources.settings_simkl_default_user
import nuvio.composeapp.generated.resources.settings_simkl_disconnect
import nuvio.composeapp.generated.resources.settings_simkl_disconnect_description
import nuvio.composeapp.generated.resources.settings_simkl_finish_sign_in
import nuvio.composeapp.generated.resources.settings_simkl_invalid_callback
import nuvio.composeapp.generated.resources.settings_simkl_missing_credentials
import nuvio.composeapp.generated.resources.settings_simkl_open_login
import nuvio.composeapp.generated.resources.settings_simkl_sign_in_description
import nuvio.composeapp.generated.resources.settings_simkl_sign_in_failed
import nuvio.composeapp.generated.resources.settings_simkl_sync_info_action
import nuvio.composeapp.generated.resources.settings_simkl_sync_now
import nuvio.composeapp.generated.resources.settings_simkl_visit
import nuvio.composeapp.generated.resources.settings_tracking_approval_redirect
import nuvio.composeapp.generated.resources.settings_tracking_disconnect_description
import nuvio.composeapp.generated.resources.settings_tracking_disconnect_title
import nuvio.composeapp.generated.resources.settings_trakt_approval_redirect
import nuvio.composeapp.generated.resources.settings_trakt_connect
import nuvio.composeapp.generated.resources.settings_trakt_connected_as
import nuvio.composeapp.generated.resources.settings_trakt_default_user
import nuvio.composeapp.generated.resources.settings_trakt_disconnect
import nuvio.composeapp.generated.resources.settings_trakt_disconnect_description
import nuvio.composeapp.generated.resources.settings_trakt_failed_open_browser
import nuvio.composeapp.generated.resources.settings_trakt_finish_sign_in
import nuvio.composeapp.generated.resources.settings_trakt_missing_credentials
import nuvio.composeapp.generated.resources.settings_trakt_open_login
import nuvio.composeapp.generated.resources.settings_trakt_save_actions_description
import nuvio.composeapp.generated.resources.settings_trakt_sign_in_description
import nuvio.composeapp.generated.resources.settings_trakt_vip_unavailable_description
import nuvio.composeapp.generated.resources.settings_trakt_vip_unavailable_title
import nuvio.composeapp.generated.resources.settings_trakt_supporters_count
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun MewTrackingProviderCards(modifier: Modifier = Modifier) {
    val aniListUiState by remember {
        AniListTrackingRepository.ensureLoaded()
        AniListTrackingRepository.uiState
    }.collectAsStateWithLifecycle()
    val malUiState by remember {
        MyAnimeListTrackingRepository.ensureLoaded()
        MyAnimeListTrackingRepository.uiState
    }.collectAsStateWithLifecycle()
    val animeSettings by remember {
        AnimeTrackingSettingsRepository.ensureLoaded()
        AnimeTrackingSettingsRepository.state
    }.collectAsStateWithLifecycle()
    // Upstream now ships its own AniList card; Mew's legacy AniList card is only shown while a
    // Mew AniList session is still connected so the user can manage/disconnect it.
    if (aniListUiState.connected) {
        AnimeTrackingProviderCard(TrackingBrand.ANILIST, aniListUiState, animeSettings.aniList, modifier)
    }
    AnimeTrackingProviderCard(TrackingBrand.MY_ANIME_LIST, malUiState, animeSettings.myAnimeList, modifier)
}

@Composable
internal fun MewTrackingMissingCredentials(
    unavailableTitle: String?,
    unavailableDescription: String?,
    missingCredentialsMessage: String,
) {
    if (unavailableTitle != null && unavailableDescription != null) {
        TrackingUnavailableSupport(title = unavailableTitle, description = unavailableDescription)
    } else {
        TrackingBrandMessage(text = missingCredentialsMessage, isError = true)
    }
}

@Composable
internal fun MewTrackingBrandGlyph(
    brand: TrackingBrand,
    modifier: Modifier,
    contentDescription: String?,
) {
    Image(
        painter = painterResource(Res.drawable.logo_mal),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Fit,
    )
}

internal fun mewTrackingCardBrush(brand: TrackingBrand): Brush = Brush.linearGradient(
    colors = listOf(Color(0xFF182238), Color(0xFF2E51A2), Color(0xFF4B73D1)),
)

@Composable
private fun AnimeTrackingProviderCard(
    brand: TrackingBrand,
    uiState: AnimeTrackingAuthUiState,
    preferences: AnimeTrackingProviderPreferences,
    modifier: Modifier,
) {
    val provider = if (brand == TrackingBrand.ANILIST) {
        AnimeTrackingProvider.ANILIST
    } else {
        AnimeTrackingProvider.MY_ANIME_LIST
    }
    val repository = if (provider == AnimeTrackingProvider.ANILIST) {
        AniListTrackingRepository
    } else {
        MyAnimeListTrackingRepository
    }
    TrackingProviderCard(
        brand = brand,
        mode = if (uiState.connected) TrackingConnectionCardMode.CONNECTED else TrackingConnectionCardMode.DISCONNECTED,
        credentialsConfigured = uiState.credentialsConfigured,
        isLoading = uiState.isLoading,
        connectedLabel = stringResource(
            Res.string.settings_anime_connected_as,
            brand.displayName,
            uiState.username ?: brand.displayName,
        ),
        connectedDescription = stringResource(Res.string.settings_anime_connected_description),
        signInDescription = stringResource(Res.string.settings_anime_sign_in_description),
        finishSignInLabel = stringResource(Res.string.settings_anime_finish_sign_in),
        approvalDescription = stringResource(Res.string.settings_anime_approval_description),
        connectLabel = stringResource(Res.string.settings_anime_connect, brand.displayName),
        openLoginLabel = stringResource(Res.string.settings_anime_open_login),
        disconnectLabel = stringResource(Res.string.settings_anime_disconnect),
        missingCredentialsMessage = stringResource(Res.string.settings_anime_missing_client_id, brand.displayName),
        errorMessage = uiState.errorMessage,
        websiteLabel = stringResource(Res.string.settings_anime_visit, brand.displayName),
        websiteUrl = if (brand == TrackingBrand.ANILIST) "https://anilist.co" else "https://myanimelist.net",
        onConnectRequested = { repository.onConnectRequested() },
        onResumeAuthorization = { repository.onConnectRequested() },
        onCancelAuthorization = {},
        onDisconnect = repository::onDisconnectRequested,
        connectedContent = {
            AnimeTrackingPreferences(
                preferences = preferences,
                onAutomaticScrobbleChanged = {
                    AnimeTrackingSettingsRepository.setAutomaticScrobble(provider, it)
                },
                onExplicitWatchedSyncChanged = {
                    AnimeTrackingSettingsRepository.setExplicitWatchedSync(provider, it)
                },
            )
        },
        modifier = modifier,
    )
}

@Composable
private fun AnimeTrackingPreferences(
    preferences: AnimeTrackingProviderPreferences,
    onAutomaticScrobbleChanged: (Boolean) -> Unit,
    onExplicitWatchedSyncChanged: (Boolean) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        AnimeTrackingPreferenceRow(
            label = stringResource(Res.string.settings_anime_automatic_scrobble),
            checked = preferences.automaticScrobble,
            onCheckedChange = onAutomaticScrobbleChanged,
        )
        AnimeTrackingPreferenceRow(
            label = stringResource(Res.string.settings_anime_explicit_watched_sync),
            checked = preferences.explicitWatchedSync,
            onCheckedChange = onExplicitWatchedSyncChanged,
        )
    }
}

@Composable
private fun AnimeTrackingPreferenceRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(NuvioTokens.Radius.md))
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.9f),
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Composable
private fun TrackingUnavailableSupport(
    title: String,
    description: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.11f),
        shape = RoundedCornerShape(NuvioTokens.Radius.lg),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f),
            )
        }
    }
}
