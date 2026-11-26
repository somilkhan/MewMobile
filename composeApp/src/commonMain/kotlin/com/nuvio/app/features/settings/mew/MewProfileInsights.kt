package com.nuvio.app.features.settings

// Mew-only profile page additions (favorite people, concierge, profile background).
// Upstream's ProfileInsightsSettingsPage.kt reaches these via `// Mew hook` lines only.

import co.touchlab.kermit.Logger
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CollectionsBookmark
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.nuvio.app.core.ui.NuvioPrimaryButton
import com.nuvio.app.core.ui.NuvioSurfaceCard
import com.nuvio.app.core.ui.NuvioModalBottomSheet
import com.nuvio.app.core.ui.nuvio
import com.nuvio.app.features.home.MetaPreview
import com.nuvio.app.features.home.buildHomeConciergeState
import com.nuvio.app.features.home.buildHomeReleaseRadarItems
import com.nuvio.app.features.home.components.CollectionCardRemoteImage
import com.nuvio.app.features.home.components.HomeConciergeSection
import com.nuvio.app.features.details.MetaDetailsRepository
import com.nuvio.app.features.details.MetaDetails
import com.nuvio.app.features.details.FavoritePerson
import com.nuvio.app.features.details.FavoritePeopleRepository
import com.nuvio.app.features.settings.filteredByNuvioEnhancedReleaseRadar
import com.nuvio.app.features.library.LibraryItem
import com.nuvio.app.features.library.LibraryRepository
import com.nuvio.app.features.library.LibraryUiState
import com.nuvio.app.features.profiles.AvatarCatalogItem
import com.nuvio.app.features.profiles.AvatarRepository
import com.nuvio.app.features.profiles.NuvioProfile
import com.nuvio.app.features.profiles.ProfileRepository
import com.nuvio.app.features.profiles.ProfileRemoteBackgroundImage
import com.nuvio.app.features.profiles.profileBackgroundImageUrl
import com.nuvio.app.features.profiles.parseHexColor
import com.nuvio.app.features.profiles.profileAvatarImageUrl
import com.nuvio.app.features.watched.WatchedClock
import com.nuvio.app.features.watched.WatchedItem
import com.nuvio.app.features.watched.WatchedRepository
import com.nuvio.app.features.watched.WatchedUiState
import com.nuvio.app.features.watched.watchedItemKey
import com.nuvio.app.features.watchprogress.CurrentDateProvider
import com.nuvio.app.features.watchprogress.ContinueWatchingItem
import com.nuvio.app.features.watchprogress.WatchProgressEntry
import com.nuvio.app.features.watchprogress.WatchProgressRepository
import com.nuvio.app.features.watchprogress.WatchProgressUiState
import com.nuvio.app.features.watchprogress.isLiveTvProgressEntry
import com.nuvio.app.features.watchprogress.profileContinueWatchingEntries
import com.nuvio.app.features.watchprogress.toContinueWatchingItem
import kotlin.math.roundToInt
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.*
import nuvio.composeapp.generated.resources.compose_nav_profile
import nuvio.composeapp.generated.resources.home_hero_type_movie
import nuvio.composeapp.generated.resources.home_hero_type_series
import nuvio.composeapp.generated.resources.profile_insights_edit_profile
import nuvio.composeapp.generated.resources.profile_insights_hours
import nuvio.composeapp.generated.resources.profile_insights_minutes
import nuvio.composeapp.generated.resources.profile_insights_resume_progress
import nuvio.composeapp.generated.resources.profile_insights_section_overview
import nuvio.composeapp.generated.resources.profile_insights_section_taste
import nuvio.composeapp.generated.resources.profile_insights_smart_resume_empty_subtitle
import nuvio.composeapp.generated.resources.profile_insights_smart_resume_empty_title
import nuvio.composeapp.generated.resources.profile_insights_stat_completed
import nuvio.composeapp.generated.resources.profile_insights_stat_completed_caption
import nuvio.composeapp.generated.resources.profile_insights_stat_continue
import nuvio.composeapp.generated.resources.profile_insights_stat_continue_caption
import nuvio.composeapp.generated.resources.profile_insights_stat_library
import nuvio.composeapp.generated.resources.profile_insights_stat_library_caption
import nuvio.composeapp.generated.resources.profile_insights_stat_recent
import nuvio.composeapp.generated.resources.profile_insights_stat_recent_caption
import nuvio.composeapp.generated.resources.profile_insights_stat_time
import nuvio.composeapp.generated.resources.profile_insights_stat_time_caption
import nuvio.composeapp.generated.resources.profile_insights_stat_upcoming
import nuvio.composeapp.generated.resources.profile_insights_stat_upcoming_caption
import nuvio.composeapp.generated.resources.profile_insights_subtitle
import nuvio.composeapp.generated.resources.profile_insights_switch_profile
import nuvio.composeapp.generated.resources.profile_insights_taste_empty
import nuvio.composeapp.generated.resources.profile_insights_taste_subtitle
import nuvio.composeapp.generated.resources.profile_insights_taste_title
import nuvio.composeapp.generated.resources.profile_insights_title
import org.jetbrains.compose.resources.stringResource

internal fun LazyListScope.favoritePeopleContent(
    isTablet: Boolean,
    onFavoritePersonClick: ((FavoritePerson) -> Unit)? = null,
) {
    item {
        ProfileFavoritePeopleFullPage(
            isTablet = isTablet,
            onFavoritePersonClick = onFavoritePersonClick,
        )
    }
}


private const val ProfileFavoritePeopleInlineLimit = 3
private const val ProfileConciergeContinueWatchingLimit = 36
private val mewProfileLog = Logger.withTag("MewProfileInsights")

/** Mew hero background: the profile's remote background image, drawn under upstream's hero. */
@Composable
internal fun MewProfileHeroBackground(
    profile: NuvioProfile?,
    modifier: Modifier = Modifier,
) {
    if (profile == null) return
    ProfileRemoteBackgroundImage(
        imageUrl = profileBackgroundImageUrl(profile),
        profileIndex = profile.profileIndex,
        modifier = modifier,
        alpha = 0.22f,
    )
}

/**
 * Mew-only sections of the profile page (favorite people rail + Nuvio Concierge).
 * Rendered by one hook line inside upstream's ProfileInsightsBody column.
 */
@Composable
internal fun MewProfileInsightsExtras(
    isTablet: Boolean,
    profileName: String,
    onPosterClick: ((MetaPreview) -> Unit)?,
    onContinueWatchingClick: ((ContinueWatchingItem) -> Unit)?,
    onFavoritePersonClick: ((FavoritePerson) -> Unit)?,
    onFavoritePeopleViewAllClick: (() -> Unit)?,
) {
    val watchProgressState by remember {
        WatchProgressRepository.ensureLoaded()
        WatchProgressRepository.uiState
    }.collectAsStateWithLifecycle()
    val libraryState by remember {
        LibraryRepository.ensureLoaded()
        LibraryRepository.uiState
    }.collectAsStateWithLifecycle()
    val favoritePeopleState by remember {
        FavoritePeopleRepository.ensureLoaded()
        FavoritePeopleRepository.uiState
    }.collectAsStateWithLifecycle()
    val nuvioEnhancedSettings by remember {
        NuvioEnhancedSettingsRepository.ensureLoaded()
        NuvioEnhancedSettingsRepository.uiState
    }.collectAsStateWithLifecycle()
    val todayIsoDate = remember { CurrentDateProvider.todayIsoDate() }
    val activeProfileIndex = ProfileRepository.activeProfileId

    val continueWatchingItems = remember(activeProfileIndex, watchProgressState.entries) {
        runCatching {
            watchProgressState.entries
                .profileContinueWatchingEntries(limit = ProfileConciergeContinueWatchingLimit)
                .map(WatchProgressEntry::toContinueWatchingItem)
        }.onFailure { error ->
            mewProfileLog.e(error) { "Failed to build profile continue watching items profile=$activeProfileIndex" }
        }.getOrElse { emptyList() }
    }
    val profileRadarItems = remember(activeProfileIndex, todayIsoDate, continueWatchingItems, libraryState.items, nuvioEnhancedSettings) {
        if (!nuvioEnhancedSettings.releaseRadarHomeSignalsEnabled) {
            emptyList()
        } else {
            runCatching {
                buildHomeReleaseRadarItems(
                    todayIsoDate = todayIsoDate,
                    continueWatchingItems = continueWatchingItems,
                    libraryItems = libraryState.items,
                    catalogSections = emptyList(),
                    resolvedLibraryDetails = emptyMap(),
                ).filteredByNuvioEnhancedReleaseRadar(nuvioEnhancedSettings)
            }.onFailure { error ->
                mewProfileLog.e(error) { "Failed to build profile radar items profile=$activeProfileIndex" }
            }.getOrElse { emptyList() }
        }
    }
    val conciergeState = remember(activeProfileIndex, profileName, continueWatchingItems, profileRadarItems, libraryState.items, nuvioEnhancedSettings) {
        if (!nuvioEnhancedSettings.enhancedHomeFeaturesEnabled || !nuvioEnhancedSettings.nuvioConciergeEnabled) {
            null
        } else {
            runCatching {
                buildHomeConciergeState(
                    profileName = profileName,
                    continueWatchingItems = continueWatchingItems,
                    releaseRadarItems = profileRadarItems,
                    libraryItems = libraryState.items,
                    catalogSections = emptyList(),
                    smartResumeEnabled = nuvioEnhancedSettings.smartResumeEnabled,
                    releaseRadarEnabled = nuvioEnhancedSettings.releaseRadarHomeSignalsEnabled,
                    profileStatsEnabled = nuvioEnhancedSettings.profileStatsEnabled,
                )
            }.onFailure { error ->
                mewProfileLog.e(error) { "Failed to build profile concierge state profile=$activeProfileIndex" }
            }.getOrNull()
        }
    }

    if (favoritePeopleState.people.isNotEmpty() && onFavoritePersonClick != null) {
        SettingsSection(
            title = stringResource(Res.string.profile_insights_favorite_people_title),
            isTablet = isTablet,
            actions = {
                if (
                    favoritePeopleState.displayPeople.size > ProfileFavoritePeopleInlineLimit &&
                    onFavoritePeopleViewAllClick != null
                ) {
                    ProfileFavoritePeopleSeeAllButton(
                        isTablet = isTablet,
                        onClick = onFavoritePeopleViewAllClick,
                    )
                }
            },
        ) {
            ProfileFavoritePeopleRail(
                people = favoritePeopleState.displayPeople,
                isTablet = isTablet,
                onPersonClick = onFavoritePersonClick,
            )
        }
    }
    if (conciergeState != null) {
        SettingsSection(
            title = "Mew Concierge",
            isTablet = isTablet,
        ) {
            HomeConciergeSection(
                state = conciergeState,
                sectionPadding = 0.dp,
                onPosterClick = onPosterClick,
                onContinueWatchingClick = onContinueWatchingClick,
            )
        }
    }
}

@Composable
private fun ProfileFavoritePeopleFullPage(
    isTablet: Boolean,
    onFavoritePersonClick: ((FavoritePerson) -> Unit)?,
) {
    val favoritePeopleState by remember {
        FavoritePeopleRepository.ensureLoaded()
        FavoritePeopleRepository.uiState
    }.collectAsStateWithLifecycle()
    val columns = if (isTablet) 5 else 3
    val rows = remember(favoritePeopleState.people, favoritePeopleState.pinnedPersonIds, columns) {
        favoritePeopleState.displayPeople.chunked(columns)
    }
    val pinnedIds = remember(favoritePeopleState.pinnedPersonIds) {
        favoritePeopleState.pinnedPersonIds.toSet()
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(if (isTablet) 24.dp else 20.dp),
    ) {
        rows.forEach { rowPeople ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(if (isTablet) 16.dp else 12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                rowPeople.forEach { person ->
                    ProfileFavoritePersonCompactItem(
                        person = person,
                        isTablet = isTablet,
                        onClick = { onFavoritePersonClick?.invoke(person) },
                        isPinned = person.tmdbId in pinnedIds,
                        onTogglePinned = { FavoritePeopleRepository.togglePinned(person.tmdbId) },
                        modifier = Modifier.weight(1f),
                    )
                }
                repeat(columns - rowPeople.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ProfileFavoritePeopleRail(
    people: List<FavoritePerson>,
    isTablet: Boolean,
    onPersonClick: (FavoritePerson) -> Unit,
) {
    val tokens = MaterialTheme.nuvio
    val shape = RoundedCornerShape(if (isTablet) 28.dp else 24.dp)
    val visiblePeople = people.take(ProfileFavoritePeopleInlineLimit)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = tokens.colors.surfaceElevated.copy(alpha = 0.62f),
        shape = shape,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.07f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.035f),
                            tokens.colors.accent.copy(alpha = 0.045f),
                            Color.Transparent,
                        ),
                    ),
                )
                .padding(horizontal = if (isTablet) 20.dp else 14.dp, vertical = if (isTablet) 18.dp else 15.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(if (isTablet) 16.dp else 10.dp),
                verticalAlignment = Alignment.Top,
            ) {
                visiblePeople.forEach { person ->
                    ProfileFavoritePersonCompactItem(
                        person = person,
                        isTablet = isTablet,
                        onClick = { onPersonClick(person) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileFavoritePeopleSeeAllButton(
    isTablet: Boolean,
    onClick: () -> Unit,
) {
    val tokens = MaterialTheme.nuvio
    Surface(
        modifier = Modifier
            .size(if (isTablet) 42.dp else 38.dp)
            .clickable(onClick = onClick),
        color = tokens.colors.surfaceElevated.copy(alpha = 0.88f),
        shape = CircleShape,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(if (isTablet) 24.dp else 22.dp),
                tint = tokens.colors.textSecondary,
            )
        }
    }
}

@Composable
private fun ProfileFavoritePersonCompactItem(
    person: FavoritePerson,
    isTablet: Boolean,
    onClick: () -> Unit,
    isPinned: Boolean = false,
    onTogglePinned: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val tokens = MaterialTheme.nuvio
    val avatarSize = if (isTablet) 82.dp else 70.dp
    val heartSize = if (isTablet) 27.dp else 24.dp
    val pinSize = if (isTablet) 27.dp else 24.dp

    Column(
        modifier = modifier
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(if (isTablet) 9.dp else 8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(avatarSize + 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(avatarSize)
                    .clip(CircleShape)
                    .background(tokens.colors.surfaceElevated)
                    .border(1.dp, Color.White.copy(alpha = 0.13f), CircleShape),
            ) {
                if (!person.photo.isNullOrBlank()) {
                    AsyncImage(
                        model = person.photo,
                        contentDescription = person.name,
                        modifier = Modifier.matchParentSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Box(
                        modifier = Modifier.matchParentSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = person.name.take(1).uppercase(),
                            style = MaterialTheme.typography.titleLarge,
                            color = tokens.colors.textSecondary,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.20f),
                                ),
                            ),
                        ),
                )
            }
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(heartSize),
                color = tokens.colors.surface.copy(alpha = 0.90f),
                shape = CircleShape,
                border = BorderStroke(1.dp, tokens.colors.accent.copy(alpha = 0.48f)),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Favorite,
                        contentDescription = null,
                        modifier = Modifier.size(if (isTablet) 15.dp else 13.dp),
                        tint = tokens.colors.accent,
                    )
                }
            }
            if (onTogglePinned != null) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .size(pinSize)
                        .clickable(onClick = onTogglePinned),
                    color = tokens.colors.surface.copy(alpha = 0.92f),
                    shape = CircleShape,
                    border = BorderStroke(
                        1.dp,
                        if (isPinned) {
                            tokens.colors.accent.copy(alpha = 0.62f)
                        } else {
                            Color.White.copy(alpha = 0.16f)
                        },
                    ),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isPinned) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                            contentDescription = null,
                            modifier = Modifier.size(if (isTablet) 15.dp else 13.dp),
                            tint = if (isPinned) tokens.colors.accent else tokens.colors.textSecondary,
                        )
                    }
                }
            }
        }
        Text(
            text = person.name,
            style = if (isTablet) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
            color = tokens.colors.textPrimary,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
        )
        person.knownFor?.takeIf { it.isNotBlank() }?.let { knownFor ->
            Text(
                text = knownFor,
                style = MaterialTheme.typography.bodySmall,
                color = tokens.colors.textSecondary,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
