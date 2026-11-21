package com.nuvio.app.features.details

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.StarBorder
import com.nuvio.app.features.ratings.UserRatingSheet
import com.nuvio.app.features.ratings.UserRatingSheetRequest
import com.nuvio.app.features.ratings.episodeUserRatingTarget
import com.nuvio.app.features.ratings.rememberCanRate
import com.nuvio.app.features.ratings.rememberUserRating
import com.nuvio.app.features.ratings.seasonUserRatingTarget
import com.nuvio.app.features.ratings.userRatingTarget
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import com.nuvio.app.core.ui.NuvioLoadingIndicator
import com.nuvio.app.core.ui.NuvioToastController
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.touchlab.kermit.Logger
import coil3.compose.AsyncImage
import com.nuvio.app.core.build.AppFeaturePolicy
import com.nuvio.app.core.build.TrailerPlaybackMode
import com.nuvio.app.core.diagnostics.MetadataLoadTrigger
import com.nuvio.app.core.network.NetworkCondition
import com.nuvio.app.core.network.NetworkStatusRepository
import com.nuvio.app.core.ui.NuvioBackButton
import com.nuvio.app.core.ui.TrackingListPickerDialog
import com.nuvio.app.core.ui.nuvioSafeBottomPadding
import com.nuvio.app.core.ui.nuvioKeyboardFocusIndicator
import com.nuvio.app.core.ui.rememberHeroStretchState
import com.nuvio.app.features.ai.AiAssistantSettingsRepository
import com.nuvio.app.features.anime.AnimeTrackingEditorSheet
import com.nuvio.app.features.anime.AniListTrackingRepository
import com.nuvio.app.features.anime.MyAnimeListTrackingRepository
import com.nuvio.app.features.details.components.AiAssistantSheet
import com.nuvio.app.features.details.components.DetailActionButtons
import com.nuvio.app.features.details.components.CinematicDetailHeader
import com.nuvio.app.features.details.components.DetailSecondaryAction
import com.nuvio.app.features.details.components.CommentDetailSheet
import com.nuvio.app.features.details.components.DetailAdditionalInfoSection
import com.nuvio.app.features.details.components.DetailCastSection
import com.nuvio.app.features.details.components.DetailCommentsSection
import com.nuvio.app.features.details.components.DetailEpisodeRatingMap
import com.nuvio.app.features.details.components.hasEpisodeRatingMap
import com.nuvio.app.features.details.components.CinematicHeaderBackdrop
import com.nuvio.app.features.details.components.DetailFloatingHeader
import com.nuvio.app.features.details.components.DetailHero
import com.nuvio.app.features.details.components.DetailMetaInfo
import com.nuvio.app.features.details.components.DetailPosterRailSection
import com.nuvio.app.features.details.components.DetailProductionSection
import com.nuvio.app.features.details.components.DetailSeriesContent
import com.nuvio.app.features.details.components.EpisodeNavigatorState
import com.nuvio.app.features.details.components.detailEpisodeListItems
import com.nuvio.app.features.details.components.rememberEpisodeNavigatorState
import com.nuvio.app.features.details.components.rememberEpisodeDownloadLookup
import com.nuvio.app.features.downloads.DownloadItem
import com.nuvio.app.features.details.components.DetailTrailersSection
import com.nuvio.app.features.details.components.readableDetailContentColor
import com.nuvio.app.features.details.components.EpisodeDownloadFlowSheet
import com.nuvio.app.features.details.components.EpisodeWatchedActionSheet
import com.nuvio.app.features.details.components.SeasonWatchedActionSheet
import com.nuvio.app.features.details.components.TabletArtworkBackdropAlignment
import com.nuvio.app.features.details.components.TabletDetailBackdrop
import com.nuvio.app.features.details.components.TabletDetailHero
import com.nuvio.app.features.details.components.TabletDetailHeroActions
import com.nuvio.app.features.details.components.TrailerPlayerPopup
import com.nuvio.app.features.downloads.DownloadQueueMode
import com.nuvio.app.features.downloads.DownloadsRepository
import com.nuvio.app.features.downloads.EpisodeDownloadCoordinator
import com.nuvio.app.features.downloads.EpisodeDownloadSettingsStorage
import com.nuvio.app.features.downloads.EpisodeDownloadTarget
import com.nuvio.app.features.home.MetaPreview
import com.nuvio.app.features.mdblist.MdbListSettingsRepository
import com.nuvio.app.features.library.LibraryRepository
import com.nuvio.app.features.library.toLibraryItem
import com.nuvio.app.features.player.PlayerSettingsRepository
import com.nuvio.app.features.settings.CinematicHeaderContentMode
import com.nuvio.app.features.settings.NuvioEnhancedSettingsRepository
import com.nuvio.app.features.simkl.SimklAuthRepository
import com.nuvio.app.features.simkl.SimklConnectionMode
import com.nuvio.app.features.streams.StreamAutoPlayPolicy
import com.nuvio.app.features.streams.StreamsRepository
import com.nuvio.app.features.tmdb.TmdbSettingsRepository
import com.nuvio.app.features.tmdb.TmdbMetadataService
import com.nuvio.app.features.tmdb.TmdbService
import com.nuvio.app.features.trakt.TraktAuthRepository
import com.nuvio.app.features.trakt.TraktCommentReview
import com.nuvio.app.features.trakt.TraktCommentsRepository
import com.nuvio.app.features.trakt.TraktCommentsSettings
import com.nuvio.app.features.trakt.TraktConnectionMode
import com.nuvio.app.features.trakt.TraktSettingsRepository
import com.nuvio.app.features.tracking.TrackingLibraryTab
import com.nuvio.app.features.tracking.toggleTrackingLibraryMembership
import com.nuvio.app.features.trailer.TrailerPlaybackResolver
import com.nuvio.app.features.trailer.TrailerPlaybackSource
import com.nuvio.app.features.watched.WatchedRepository
import com.nuvio.app.features.watched.previousReleasedEpisodesBefore
import com.nuvio.app.features.watched.releasedPlayableEpisodes
import com.nuvio.app.features.watched.releasedEpisodesForSeason
import com.nuvio.app.features.watchprogress.CurrentDateProvider
import com.nuvio.app.features.watchprogress.WatchProgressEntry
import com.nuvio.app.features.watchprogress.WatchProgressClock
import com.nuvio.app.features.watchprogress.WatchProgressRepository
import com.nuvio.app.features.watchprogress.buildPlaybackVideoId
import com.nuvio.app.features.watchprogress.ContinueWatchingPreferencesRepository
import com.nuvio.app.features.watching.application.WatchingActions
import com.nuvio.app.features.watching.application.WatchingState
import nuvio.composeapp.generated.resources.anime_tracking_action
import com.kmpalette.rememberDominantColorState
import com.kmpalette.extensions.painter.rememberPainterDominantColorState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nuvio.composeapp.generated.resources.*
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

private const val RANDOM_EPISODE_HISTORY_LIMIT = 8

private data class EpisodeDownloadRequest(
    val initialEpisodes: List<MetaVideo>,
    val showStartPrompt: Boolean,
)

private data class AnimeTrackingEditorRequest(
    val season: Int? = null,
    val episode: Int? = null,
)

@Composable
@OptIn(ExperimentalSharedTransitionApi::class)
fun MetaDetailsScreen(
    type: String,
    id: String,
    offlineMode: Boolean = false,
    onBack: () -> Unit,
    onPlay: ((type: String, videoId: String, parentMetaId: String, parentMetaType: String, title: String, logo: String?, poster: String?, background: String?, seasonNumber: Int?, episodeNumber: Int?, episodeTitle: String?, episodeThumbnail: String?, pauseDescription: String?, resumePositionMs: Long?) -> Unit)? = null,
    onPlayManually: ((type: String, videoId: String, parentMetaId: String, parentMetaType: String, title: String, logo: String?, poster: String?, background: String?, seasonNumber: Int?, episodeNumber: Int?, episodeTitle: String?, episodeThumbnail: String?, pauseDescription: String?, resumePositionMs: Long?) -> Unit)? = null,
    onRandomPlay: ((type: String, videoId: String, parentMetaId: String, parentMetaType: String, title: String, logo: String?, poster: String?, background: String?, seasonNumber: Int?, episodeNumber: Int?, episodeTitle: String?, episodeThumbnail: String?, pauseDescription: String?, resumePositionMs: Long?) -> Unit)? = null,
    onRandomPlayManually: ((type: String, videoId: String, parentMetaId: String, parentMetaType: String, title: String, logo: String?, poster: String?, background: String?, seasonNumber: Int?, episodeNumber: Int?, episodeTitle: String?, episodeThumbnail: String?, pauseDescription: String?, resumePositionMs: Long?) -> Unit)? = null,
    onOpenMeta: ((MetaPreview) -> Unit)? = null,
    onOpenMoreLikeThis: ((MetaDetails) -> Unit)? = null,
    onCastClick: ((MetaPerson, String?) -> Unit)? = null,
    onCompanyClick: ((MetaCompany, String) -> Unit)? = null,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    modifier: Modifier = Modifier,
) {
    val playbackAvailability = rememberPlaybackAvailability()
    val shuffleProfile by remember {
        EpisodeShuffleRepository.ensureLoaded()
        EpisodeShuffleRepository.uiState
    }.collectAsStateWithLifecycle()
    val shuffleProfileId = ProfileRepository.activeProfileId
    var shuffleVisit by remember(type, id, shuffleProfileId) { mutableStateOf(WatchProgressClock.nowEpochMs()) }
    var shuffleWasInactive by remember(type, id, shuffleProfileId) { mutableStateOf(false) }
    var showShuffle by remember(type, id, shuffleProfileId) { mutableStateOf(false) }
    ScreenActivityEffect(type, id, shuffleProfileId) { active ->
        if (active && shuffleWasInactive) shuffleVisit += 1
        shuffleWasInactive = !active
    }
    val uiState by MetaDetailsRepository.uiState.collectAsStateWithLifecycle()
    val enhancedSettings by NuvioEnhancedSettingsRepository.uiState.collectAsStateWithLifecycle()
    val downloadsUiState by remember {
        DownloadsRepository.ensureLoaded()
        DownloadsRepository.uiState
    }.collectAsStateWithLifecycle()
    val downloadedMeta = remember(downloadsUiState.items, type, id) {
        DownloadsRepository.findOfflineMetaDetails(type, id)
    }
    val offlineDetailsAvailable = offlineMode && downloadedMeta != null
    val displayedMeta = if (offlineDetailsAvailable) {
        downloadedMeta
    } else {
        uiState.meta?.takeIf { it.type == type && it.id == id }
            ?: MetaDetailsRepository.peek(type, id)
            ?: downloadedMeta
    }
    val metaScreenSettingsUiState by remember {
        MetaScreenSettingsRepository.ensureLoaded()
        MetaScreenSettingsRepository.uiState
    }.collectAsStateWithLifecycle()
    val mdbListSettings by remember {
        MdbListSettingsRepository.ensureLoaded()
        MdbListSettingsRepository.uiState
    }.collectAsStateWithLifecycle()
    val traktAuthUiState by remember {
        TraktAuthRepository.ensureLoaded()
        TraktAuthRepository.uiState
    }.collectAsStateWithLifecycle()
    val traktSettingsUiState by remember {
        TraktSettingsRepository.ensureLoaded()
        TraktSettingsRepository.uiState
    }.collectAsStateWithLifecycle()
    val tmdbSettingsUiState by remember {
        TmdbSettingsRepository.ensureLoaded()
        TmdbSettingsRepository.uiState
    }.collectAsStateWithLifecycle()
    val aiAssistantSettings by remember {
        AiAssistantSettingsRepository.ensureLoaded()
        AiAssistantSettingsRepository.uiState
    }.collectAsStateWithLifecycle()
    val libraryUiState by remember {
        LibraryRepository.ensureLoaded()
        LibraryRepository.uiState
    }.collectAsStateWithLifecycle()
    val watchedUiState by remember {
        WatchedRepository.ensureLoaded()
        WatchedRepository.uiState
    }.collectAsStateWithLifecycle()
    val fullyWatchedSeriesKeys by WatchedRepository.fullyWatchedSeriesKeys.collectAsStateWithLifecycle()
    val watchProgressUiState by remember {
        WatchProgressRepository.ensureLoaded()
        WatchProgressRepository.uiState
    }.collectAsStateWithLifecycle()
    val playerSettingsUiState by remember {
        PlayerSettingsRepository.ensureLoaded()
        PlayerSettingsRepository.uiState
    }.collectAsStateWithLifecycle()
    val nuvioEnhancedSettings by remember {
        NuvioEnhancedSettingsRepository.ensureLoaded()
        NuvioEnhancedSettingsRepository.uiState
    }.collectAsStateWithLifecycle()
    val networkStatusUiState by NetworkStatusRepository.uiState.collectAsStateWithLifecycle()
    var autoLoadAttempted by remember(type, id) { mutableStateOf(false) }
    val currentRequestKey = remember(type, id) { metaDetailsRequestKey(type, id) }
    val isCurrentRequestLoading = uiState.requestKey == currentRequestKey && uiState.isLoading
    val currentRequestError = uiState.errorMessage.takeIf { uiState.requestKey == currentRequestKey }
    var observedOfflineState by remember(type, id) { mutableStateOf(false) }
    var selectedEpisodeForActions by remember(type, id) { mutableStateOf<MetaVideo?>(null) }
    var selectedSeasonForActions by remember(type, id) { mutableStateOf<Int?>(null) }
    var episodeDownloadRequest by remember(type, id) { mutableStateOf<EpisodeDownloadRequest?>(null) }
    val commentsEnabled by remember {
        TraktCommentsSettings.ensureLoaded()
        TraktCommentsSettings.enabled
    }.collectAsStateWithLifecycle()
    var comments by remember(type, id) { mutableStateOf<List<TraktCommentReview>>(emptyList()) }
    var commentsCurrentPage by remember(type, id) { mutableIntStateOf(0) }
    var commentsPageCount by remember(type, id) { mutableIntStateOf(0) }
    var isCommentsLoading by remember(type, id) { mutableStateOf(false) }
    var isCommentsLoadingMore by remember(type, id) { mutableStateOf(false) }
    var commentsError by remember(type, id) { mutableStateOf<String?>(null) }
    var selectedComment by remember(type, id) { mutableStateOf<TraktCommentReview?>(null) }
    val detailsScope = rememberCoroutineScope()
    var showLibraryListPicker by remember(type, id) { mutableStateOf(false) }
    var pickerTabs by remember(type, id) { mutableStateOf<List<TrackingLibraryTab>>(emptyList()) }
    var pickerMembership by remember(type, id) { mutableStateOf<Map<String, Boolean>>(emptyMap()) }
    var pickerPending by remember(type, id) { mutableStateOf(false) }
    var pickerError by remember(type, id) { mutableStateOf<String?>(null) }
    var episodeImdbRatings by remember(type, id) { mutableStateOf<Map<Pair<Int, Int>, Double>>(emptyMap()) }
    var episodeTmdbRatings by remember(type, id) { mutableStateOf<Map<Pair<Int, Int>, Double>>(emptyMap()) }
    var deferredMetaWorkAllowed by remember(type, id) { mutableStateOf(false) }
    var showAiAssistant by remember(type, id) { mutableStateOf(false) }
    var animeTrackingEditorRequest by remember(type, id) { mutableStateOf<AnimeTrackingEditorRequest?>(null) }
    val aniListConnected by remember {
        AniListTrackingRepository.ensureLoaded()
        AniListTrackingRepository.isAuthenticated
    }.collectAsStateWithLifecycle()
    val malConnected by remember {
        MyAnimeListTrackingRepository.ensureLoaded()
        MyAnimeListTrackingRepository.isAuthenticated
    }.collectAsStateWithLifecycle()
    val simklAuthUiState by remember {
        SimklAuthRepository.ensureLoaded()
        SimklAuthRepository.uiState
    }.collectAsStateWithLifecycle()

    val shouldShowComments = !offlineDetailsAvailable &&
        commentsEnabled &&
        traktAuthUiState.mode == TraktConnectionMode.CONNECTED &&
        displayedMeta != null &&
        displayedMeta.type.lowercase().let { it == "movie" || it == "series" || it == "show" || it == "tv" }
    val supportsAiAssistant = displayedMeta?.type
        ?.lowercase()
        ?.let { it in setOf("movie", "film", "series", "show", "tv", "tvshow") } == true
    val supportsAnimeTracking =
        aniListConnected || malConnected || simklAuthUiState.mode == SimklConnectionMode.CONNECTED

    LaunchedEffect(displayedMeta?.id) {
        deferredMetaWorkAllowed = false
        if (displayedMeta != null) {
            delay(250)
            deferredMetaWorkAllowed = true
        }
    }

    LaunchedEffect(displayedMeta?.id, shouldShowComments, deferredMetaWorkAllowed) {
        if (displayedMeta == null || !shouldShowComments) {
            comments = emptyList()
            commentsCurrentPage = 0
            commentsPageCount = 0
            commentsError = null
            return@LaunchedEffect
        }
        if (!deferredMetaWorkAllowed) return@LaunchedEffect
        delay(150)
        isCommentsLoading = true
        commentsError = null
        try {
            val result = TraktCommentsRepository.getCommentsPage(displayedMeta, page = 1)
            comments = result.items
            commentsCurrentPage = result.currentPage
            commentsPageCount = result.pageCount
        } catch (e: Exception) {
            commentsError = e.message ?: getString(Res.string.details_comments_load_failed)
        }
        isCommentsLoading = false
    }

    LaunchedEffect(displayedMeta?.id, displayedMeta?.videos, deferredMetaWorkAllowed, offlineDetailsAvailable) {
        val metaForRatings = displayedMeta
        if (offlineDetailsAvailable) {
            episodeImdbRatings = emptyMap()
            episodeTmdbRatings = emptyMap()
            return@LaunchedEffect
        }
        if (!deferredMetaWorkAllowed) return@LaunchedEffect
        delay(300)
        if (metaForRatings == null || !metaForRatings.isSeriesLikeForEpisodeRatings()) {
            episodeImdbRatings = emptyMap()
            episodeTmdbRatings = emptyMap()
            return@LaunchedEffect
        }

        val imdbId = extractImdbId(metaForRatings.id) ?: extractImdbId(id)
        val tmdbId = extractTmdbId(metaForRatings.id)
            ?: extractTmdbId(id)
            ?: TmdbService.ensureTmdbId(metaForRatings.id, metaForRatings.type)?.toIntOrNull()
            ?: TmdbService.ensureTmdbId(id, type)?.toIntOrNull()

        if (imdbId == null && tmdbId == null) {
            episodeImdbRatings = emptyMap()
            episodeTmdbRatings = emptyMap()
            return@LaunchedEffect
        }

        episodeImdbRatings = ImdbEpisodeRatingsRepository.getEpisodeRatings(
            imdbId = imdbId,
            tmdbId = tmdbId,
        )
        episodeTmdbRatings = tmdbId?.let { resolvedTmdbId ->
            TmdbService.getEpisodeRatings(
                tmdbId = resolvedTmdbId,
                seasons = metaForRatings.videos.mapNotNull { it.season },
            )
        }.orEmpty()
    }

    LaunchedEffect(type, id, offlineDetailsAvailable, displayedMeta, autoLoadAttempted) {
        if (offlineDetailsAvailable) {
            autoLoadAttempted = true
            return@LaunchedEffect
        }
        if (!autoLoadAttempted && displayedMeta == null) {
            autoLoadAttempted = true
            MetaDetailsRepository.load(type, id, trigger = MetadataLoadTrigger.Initial)
        }
    }

    LaunchedEffect(
        type,
        id,
        traktSettingsUiState.moreLikeThisSource,
        traktAuthUiState.mode,
        tmdbSettingsUiState,
        offlineDetailsAvailable,
    ) {
        if (offlineDetailsAvailable) return@LaunchedEffect
        if (displayedMeta != null && !isCurrentRequestLoading) {
            MetaDetailsRepository.load(type, id, trigger = MetadataLoadTrigger.SettingsChanged)
        }
    }

    LaunchedEffect(networkStatusUiState.condition, displayedMeta, isCurrentRequestLoading, type, id, offlineDetailsAvailable) {
        if (offlineDetailsAvailable) return@LaunchedEffect
        when (networkStatusUiState.condition) {
            NetworkCondition.NoInternet,
            NetworkCondition.ServersUnreachable,
            -> {
                observedOfflineState = true
            }

            NetworkCondition.Online -> {
                if (!observedOfflineState) return@LaunchedEffect
                observedOfflineState = false
                if (displayedMeta == null && !isCurrentRequestLoading) {
                    MetaDetailsRepository.load(type, id, trigger = MetadataLoadTrigger.Reconnected)
                }
            }

            NetworkCondition.Unknown,
            NetworkCondition.Checking,
            -> Unit
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        when {
            displayedMeta == null && (isCurrentRequestLoading || !autoLoadAttempted) -> {
                NuvioLoadingIndicator(
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            displayedMeta == null && currentRequestError != null -> {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.details_failed_to_load),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = when (networkStatusUiState.condition) {
                            NetworkCondition.NoInternet -> stringResource(Res.string.details_check_connection)
                            NetworkCondition.ServersUnreachable -> stringResource(Res.string.details_servers_unreachable)
                            else -> currentRequestError.orEmpty()
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            NetworkStatusRepository.requestRefresh(force = true)
                            MetaDetailsRepository.retry(type, id)
                        },
                    ) {
                        Text(stringResource(Res.string.action_retry))
                    }
                }
            }

            displayedMeta != null -> {
                val meta = displayedMeta
                val offlineDetailsMode = offlineMode && downloadedMeta != null && meta.id == downloadedMeta.id
                val metaPreview = remember(meta) { meta.toMetaPreview() }
                val todayIsoDate = CurrentDateProvider.todayIsoDate()
                val isSaved = remember(
                    libraryUiState.items,
                    libraryUiState.sections,
                    libraryUiState.sourceMode,
                    meta.id,
                    meta.type,
                ) {
                    LibraryRepository.isSaved(meta.id, meta.type)
                }
                val isWatched = remember(watchedUiState.watchedKeys, fullyWatchedSeriesKeys, metaPreview) {
                    WatchingState.isPosterWatched(
                        watchedKeys = watchedUiState.watchedKeys,
                        item = metaPreview,
                        fullyWatchedSeriesKeys = fullyWatchedSeriesKeys,
                    )
                }
                val openLibraryListPicker = remember(meta) {
                    {
                        val libraryItem = meta.toLibraryItem(savedAtEpochMs = 0L)
                        pickerTabs = LibraryRepository.libraryListTabs(libraryItem)
                        pickerMembership = pickerTabs.associate { it.key to false }
                        pickerPending = true
                        pickerError = null
                        showLibraryListPicker = true
                        detailsScope.launch {
                            runCatching {
                                val snapshot = LibraryRepository.getMembershipSnapshot(libraryItem)
                                val tabs = LibraryRepository.libraryListTabs(libraryItem)
                                pickerTabs = tabs
                                pickerMembership = tabs.associate { tab ->
                                    tab.key to (snapshot[tab.key] == true)
                                }
                            }.onFailure { error ->
                                pickerError = error.message ?: getString(Res.string.trakt_lists_load_failed)
                            }
                            pickerPending = false
                        }
                        Unit
                    }
                }
                val toggleSaved = remember(meta) {
                    {
                        detailsScope.launch {
                            LibraryRepository.toggleSaved(meta.toLibraryItem(savedAtEpochMs = 0L))
                        }
                        Unit
                    }
                }
                val toggleWatched = remember(metaPreview) {
                    {
                        detailsScope.launch {
                            WatchingActions.togglePosterWatched(metaPreview)
                        }
                        Unit
                    }
                }
                val progressByVideoId = remember(watchProgressUiState.entries) {
                    watchProgressUiState.byVideoId
                }
                LaunchedEffect(meta.id, meta.type, watchProgressUiState.hasLoadedRemoteProgress) {
                    if (meta.type.lowercase() in setOf("series", "show", "tv", "tvshow")) {
                        WatchProgressRepository.refreshEpisodeProgress(meta.id)
                    }
                }
                LaunchedEffect(
                    meta.id,
                    meta.type,
                    todayIsoDate,
                    watchedUiState.isLoaded,
                    watchProgressUiState.hasLoadedRemoteProgress,
                    watchedUiState.watchedKeys,
                    watchProgressUiState.entries,
                ) {
                    if (watchedUiState.isLoaded && watchProgressUiState.hasLoadedRemoteProgress) {
                        WatchingActions.reconcileSeriesWatchedState(
                            meta = meta,
                            todayIsoDate = todayIsoDate,
                        )
                    }
                }
                val movieProgress = progressByVideoId[meta.id]
                    ?.takeUnless { it.isCompleted }
                val cwPrefs by ContinueWatchingPreferencesRepository.uiState.collectAsStateWithLifecycle()
                val seriesAction = remember(watchProgressUiState.entries, watchedUiState.items, meta, todayIsoDate, cwPrefs.upNextFromFurthestEpisode) {
                    meta.seriesPrimaryAction(
                        entries = watchProgressUiState.entries,
                        watchedItems = watchedUiState.items,
                        todayIsoDate = todayIsoDate,
                        preferFurthestEpisode = cwPrefs.upNextFromFurthestEpisode,
                        allowRewatch = true,
                    )
                }
                val seriesActionVideo = remember(seriesAction, meta.id, meta.videos) {
                    val action = seriesAction ?: return@remember null
                    meta.videos.firstOrNull { video ->
                        if (action.seasonNumber != null && action.episodeNumber != null) {
                            video.season == action.seasonNumber &&
                                video.episode == action.episodeNumber
                        } else {
                            buildPlaybackVideoId(
                                parentMetaId = meta.id,
                                seasonNumber = video.season,
                                episodeNumber = video.episode,
                                fallbackVideoId = video.id,
                            ) == action.videoId || video.id == action.videoId
                        }
                    }
                }
                val seriesPauseDescription = remember(seriesActionVideo) {
                    seriesActionVideo?.overview
                }
                val seriesStreamVideoId = remember(seriesAction, seriesActionVideo) {
                    val action = seriesAction ?: return@remember null
                    seriesActionVideo?.id?.takeIf { it.isNotBlank() } ?: action.videoId
                }
                LaunchedEffect(
                    nuvioEnhancedSettings.backgroundStreamPrefetchEnabled,
                    offlineDetailsMode,
                    meta.id,
                    meta.type,
                    meta.name,
                    seriesStreamVideoId,
                    seriesAction?.seasonNumber,
                    seriesAction?.episodeNumber,
                    seriesAction?.episodeTitle,
                    deferredMetaWorkAllowed,
                ) {
                    if (!nuvioEnhancedSettings.backgroundStreamPrefetchEnabled) return@LaunchedEffect
                    if (offlineDetailsMode || !deferredMetaWorkAllowed) return@LaunchedEffect
                    // Keep first-paint work isolated from stream-provider fan-out. Prefetch only
                    // after the detail UI has had a short idle window to establish a stable frame cadence.
                    delay(750)
                    val hasEpisodesForPrefetch = meta.type == "series" || meta.videos.any { it.season != null || it.episode != null }
                    val targetVideoId = if (hasEpisodesForPrefetch && seriesAction != null) {
                        seriesStreamVideoId ?: seriesAction.videoId
                    } else {
                        meta.id
                    }.takeIf { it.isNotBlank() } ?: return@LaunchedEffect
                    StreamsRepository.load(
                        type = meta.type,
                        videoId = targetVideoId,
                        parentMetaId = meta.id,
                        parentMetaType = meta.type,
                        season = seriesAction?.seasonNumber.takeIf { hasEpisodesForPrefetch },
                        episode = seriesAction?.episodeNumber.takeIf { hasEpisodesForPrefetch },
                        manualSelection = false,
                        searchTitle = if (hasEpisodesForPrefetch) seriesAction?.episodeTitle ?: meta.name else meta.name,
                    )
                }
                val showRandomEpisodeButton = nuvioEnhancedSettings.enhancedHomeFeaturesEnabled &&
                    playerSettingsUiState.randomNextEpisodeEnabled &&
                    meta.isSeriesLikeForEpisodeRatings()
                val randomEpisodeLabel = stringResource(Res.string.action_random_episode)
                var randomEpisodeHistory by remember(
                    meta.id,
                    seriesActionVideo?.season,
                    seriesActionVideo?.episode,
                    todayIsoDate,
                ) {
                    mutableStateOf<List<String>>(emptyList())
                }

                fun MetaVideo.randomEpisodePlaybackId(): String =
                    buildPlaybackVideoId(
                        parentMetaId = meta.id,
                        seasonNumber = season,
                        episodeNumber = episode,
                        fallbackVideoId = id,
                    )

                fun pickRandomEpisode(): MetaVideo? {
                    if (!showRandomEpisodeButton) return null
                    val releasedEpisodes = meta.releasedPlayableEpisodes(todayIsoDate)
                        .filter { video -> normalizeSeasonNumber(video.season) > 0 }
                    if (releasedEpisodes.isEmpty()) return null

                    val currentPlaybackId = seriesActionVideo?.randomEpisodePlaybackId()

                    val basePool = releasedEpisodes.filterNot { episode ->
                        currentPlaybackId != null && episode.randomEpisodePlaybackId() == currentPlaybackId
                    }
                    val nonRepeatingPool = basePool.filterNot { episode ->
                        basePool.size > 1 &&
                            episode.randomEpisodePlaybackId() in randomEpisodeHistory
                    }

                    val candidates = when {
                        nonRepeatingPool.isNotEmpty() -> nonRepeatingPool
                        basePool.isNotEmpty() -> basePool
                        else -> releasedEpisodes
                    }

                    val selected = candidates.random()
                    randomEpisodeHistory = (randomEpisodeHistory + selected.randomEpisodePlaybackId())
                        .takeLast(RANDOM_EPISODE_HISTORY_LIMIT)
                    return selected
                }

                val randomEpisodeAction = if (
                    showRandomEpisodeButton &&
                    (onRandomPlay != null || onRandomPlayManually != null || onPlay != null || onPlayManually != null)
                ) {
                    val clickHandler = onRandomPlay ?: onPlay ?: onRandomPlayManually ?: onPlayManually
                    val longClickHandler = onRandomPlayManually ?: onPlayManually ?: onRandomPlay ?: onPlay
                    DetailSecondaryAction(
                        label = randomEpisodeLabel,
                        icon = Icons.Default.Shuffle,
                        onClick = {
                            pickRandomEpisode()?.let { video ->
                                val season = video.season
                                val episode = video.episode
                                val playbackVideoId = video.randomEpisodePlaybackId()
                                val streamVideoId = video.id.takeIf { it.isNotBlank() } ?: playbackVideoId
                                val savedProgress = watchProgressUiState.byVideoId[streamVideoId]
                                    ?.takeUnless { it.isCompleted }
                                clickHandler?.invoke(
                                    meta.type,
                                    streamVideoId,
                                    meta.id,
                                    meta.type,
                                    meta.name,
                                    meta.logo,
                                    meta.poster,
                                    meta.background,
                                    season,
                                    episode,
                                    video.title,
                                    video.thumbnail,
                                    video.overview,
                                    savedProgress?.lastPositionMs,
                                )
                            }
                        },
                        onLongClick = longClickHandler?.let { handler ->
                            {
                                pickRandomEpisode()?.let { video ->
                                    val season = video.season
                                    val episode = video.episode
                                    val playbackVideoId = video.randomEpisodePlaybackId()
                                    val streamVideoId = video.id.takeIf { it.isNotBlank() } ?: playbackVideoId
                                    val savedProgress = watchProgressUiState.byVideoId[streamVideoId]
                                        ?.takeUnless { it.isCompleted }
                                    handler(
                                        meta.type,
                                        streamVideoId,
                                        meta.id,
                                        meta.type,
                                        meta.name,
                                        meta.logo,
                                        meta.poster,
                                        meta.background,
                                        season,
                                        episode,
                                        video.title,
                                        video.thumbnail,
                                        video.overview,
                                        savedProgress?.lastPositionMs,
                                    )
                                }
                            }
                        },
                    )
                } else {
                    null
                }
                val hasEpisodes = meta.videos.any { it.season != null || it.episode != null }
                val downloadAction = if (metaScreenSettingsUiState.showDownloadAction && hasEpisodes) {
                    DetailSecondaryAction(
                        label = stringResource(Res.string.streams_download_file),
                        icon = Icons.Default.Download,
                        onClick = {
                            episodeDownloadRequest = EpisodeDownloadRequest(
                                initialEpisodes = emptyList(),
                                showStartPrompt = true,
                            )
                        },
                    )
                } else if (metaScreenSettingsUiState.showDownloadAction && meta.type == "movie") {
                    DetailSecondaryAction(
                        label = stringResource(Res.string.streams_download_file),
                        icon = Icons.Default.Download,
                        onClick = {
                            episodeDownloadRequest = EpisodeDownloadRequest(
                                initialEpisodes = emptyList(),
                                showStartPrompt = false,
                            )
                        },
                    )
                } else {
                    null
                }
                val cinematicDetailHeaderEnabled = nuvioEnhancedSettings.cinematicDetailHeaderEnabled
                val cinematicHeaderContentMode = nuvioEnhancedSettings.cinematicHeaderContentMode
                val hasProductionSection = remember(meta, cinematicDetailHeaderEnabled) {
                    detailProductionSectionVisible(
                        hasProductionData = meta.productionCompanies.isNotEmpty() || meta.networks.isNotEmpty(),
                        cinematicDetailHeaderEnabled = cinematicDetailHeaderEnabled,
                    )
                }
                val hasAdditionalInfoSection = remember(meta) {
                    meta.status != null ||
                        meta.releaseInfo != null ||
                        meta.runtime != null ||
                        meta.ageRating != null ||
                        meta.country != null ||
                        meta.language != null
                }
                val hasCollectionSection = remember(meta) {
                    meta.collectionName != null && meta.collectionItems.isNotEmpty()
                }
                val hasMoreLikeThisSection = remember(meta, offlineDetailsMode) {
                    !offlineDetailsMode && meta.moreLikeThis.isNotEmpty()
                }
                val hasTrailersSection = remember(meta, offlineDetailsMode) {
                    !offlineDetailsMode && meta.trailers.isNotEmpty()
                }
                var cinematicProductionCompanies by remember(meta.id) {
                    mutableStateOf(emptyList<MetaCompany>())
                }
                var cinematicNetworks by remember(meta.id) {
                    mutableStateOf(emptyList<MetaCompany>())
                }
                LaunchedEffect(
                    cinematicDetailHeaderEnabled,
                    cinematicHeaderContentMode,
                    deferredMetaWorkAllowed,
                    meta.id,
                    meta.productionCompanies,
                    meta.networks,
                ) {
                    cinematicProductionCompanies = emptyList()
                    cinematicNetworks = emptyList()
                    if (
                        !cinematicDetailHeaderEnabled ||
                        cinematicHeaderContentMode != CinematicHeaderContentMode.Productions ||
                        !deferredMetaWorkAllowed ||
                        (meta.productionCompanies.isNotEmpty() && meta.networks.isNotEmpty())
                    ) {
                        return@LaunchedEffect
                    }
                    delay(450)
                    TmdbMetadataService.fetchCompanyBranding(
                        meta = meta,
                        fallbackItemId = id,
                    )?.let { branding ->
                        cinematicProductionCompanies = branding.productionCompanies
                        cinematicNetworks = branding.networks
                    }
                }
                var cinematicWatchProviders by remember(meta.id) {
                    mutableStateOf<com.nuvio.app.features.tmdb.TmdbWatchProviderAvailability?>(null)
                }
                LaunchedEffect(
                    cinematicDetailHeaderEnabled,
                    cinematicHeaderContentMode,
                    deferredMetaWorkAllowed,
                    meta.id,
                    tmdbSettingsUiState.enabled,
                    tmdbSettingsUiState.hasApiKey,
                    tmdbSettingsUiState.language,
                ) {
                    cinematicWatchProviders = null
                    if (
                        !cinematicDetailHeaderEnabled ||
                        cinematicHeaderContentMode != CinematicHeaderContentMode.WhereToWatch ||
                        !deferredMetaWorkAllowed ||
                        !tmdbSettingsUiState.enabled ||
                        !tmdbSettingsUiState.hasApiKey
                    ) {
                        return@LaunchedEffect
                    }
                    delay(450)
                    cinematicWatchProviders = TmdbMetadataService.fetchWatchProviders(
                        meta = meta,
                        fallbackItemId = id,
                    )
                }
                val cinematicHeaderMeta = remember(
                    meta,
                    cinematicProductionCompanies,
                    cinematicNetworks,
                ) {
                    meta.copy(
                        productionCompanies = meta.productionCompanies.ifEmpty { cinematicProductionCompanies },
                        networks = meta.networks.ifEmpty { cinematicNetworks },
                    )
                }
                val uriHandler = LocalUriHandler.current
                val inAppTrailerPlaybackEnabled = AppFeaturePolicy.trailerPlaybackMode == TrailerPlaybackMode.IN_APP
                val trailerScope = rememberCoroutineScope()
                var selectedTrailer by remember(meta.id) { mutableStateOf<MetaTrailer?>(null) }
                var trailerPlaybackSource by remember(meta.id) { mutableStateOf<TrailerPlaybackSource?>(null) }
                var trailerLoading by remember(meta.id) { mutableStateOf(false) }
                var trailerErrorMessage by remember(meta.id) { mutableStateOf<String?>(null) }
                var trailerRequestToken by remember(meta.id) { mutableIntStateOf(0) }
                var isLeavingDetails by remember(meta.id) { mutableStateOf(false) }
                val heroTrailerCandidate = remember(meta.trailers, offlineDetailsMode) {
                    if (offlineDetailsMode) null else selectHeroTrailer(meta.trailers)
                }
                val heroTrailerPlaybackEnabled = AppFeaturePolicy.heroTrailerPlaybackSupported &&
                    inAppTrailerPlaybackEnabled &&
                    (metaScreenSettingsUiState.heroTrailerPlayback || cinematicDetailHeaderEnabled) &&
                    !offlineDetailsMode
                var heroTrailerPlaybackSource by remember(meta.id, heroTrailerCandidate?.id) { mutableStateOf<TrailerPlaybackSource?>(null) }
                var heroTrailerReady by remember(meta.id, heroTrailerCandidate?.id) { mutableStateOf(false) }
                var heroTrailerFinished by remember(meta.id, heroTrailerCandidate?.id) { mutableStateOf(false) }
                val heroTrailerMuted by HeroTrailerAudioState.muted.collectAsStateWithLifecycle()
                var cinematicTrailerMuted by remember(meta.id, metaScreenSettingsUiState.heroTrailerSoundEnabled) {
                    mutableStateOf(!metaScreenSettingsUiState.heroTrailerSoundEnabled)
                }
                LaunchedEffect(
                    heroTrailerPlaybackEnabled,
                    heroTrailerCandidate?.id,
                    heroTrailerCandidate?.key,
                    deferredMetaWorkAllowed,
                    heroTrailerStartDelaySeconds,
                ) {
                    heroTrailerPlaybackSource = null
                    heroTrailerReady = false
                    heroTrailerFinished = false
                    if (!deferredMetaWorkAllowed || !heroTrailerPlaybackEnabled || heroTrailerCandidate == null) {
                        return@LaunchedEffect
                    }
                    val resolvedSource = async {
                        runCatching {
                            TrailerPlaybackResolver.resolveFromYouTubeUrl(heroTrailerCandidate.youtubePlaybackUrl())
                        }.getOrNull()
                    }
                    if (heroTrailerStartDelaySeconds > 0) {
                        delay(heroTrailerStartDelaySeconds.toLong() * 1000L)
                    }
                    val source = resolvedSource.await()
                    if (source == null) {
                        heroTrailerFinished = true
                    } else {
                        heroTrailerPlaybackSource = source
                    }
                }
                val stopHeroTrailerForNavigation: () -> Unit = {
                    isLeavingDetails = true
                    heroTrailerReady = false
                    heroTrailerFinished = true
                }
                val onBackFromDetails: () -> Unit = {
                    stopHeroTrailerForNavigation()
                    onBack()
                }
                val resolveTrailer: (MetaTrailer) -> Unit = remember(meta.id, inAppTrailerPlaybackEnabled, uriHandler) {
                    { trailer ->
                        val youtubeUrl = trailer.youtubePlaybackUrl()
                        if (!inAppTrailerPlaybackEnabled) {
                            runCatching { uriHandler.openUri(youtubeUrl) }
                        } else {
                            selectedTrailer = trailer
                            trailerPlaybackSource = null
                            trailerErrorMessage = null
                            trailerLoading = true
                            trailerRequestToken += 1
                            val currentRequestToken = trailerRequestToken
                            trailerScope.launch {
                                val resolvedSource = runCatching {
                                    TrailerPlaybackResolver.resolveFromYouTubeUrl(youtubeUrl)
                                }.getOrNull()
                                if (currentRequestToken != trailerRequestToken) {
                                    return@launch
                                }
                                trailerPlaybackSource = resolvedSource
                                trailerErrorMessage = if (resolvedSource == null) {
                                    getString(Res.string.trailer_no_playable_stream)
                                } else {
                                    null
                                }
                                trailerLoading = false
                            }
                        }
                    }
                }
                val primaryVideoId = seriesStreamVideoId ?: seriesAction?.videoId ?: meta.id
                val shufflePoolEmpty = shuffleSettings.enabled && seriesAction == null
                val isPrimaryPlayEnabled = shufflePoolEmpty || playbackAvailability.canPlay(
                    type = meta.type,
                    videoId = primaryVideoId,
                    parentMetaId = meta.id,
                    seasonNumber = seriesAction?.seasonNumber,
                    episodeNumber = seriesAction?.episodeNumber,
                )
                val playText = stringResource(Res.string.action_play)
                val resumeText = stringResource(Res.string.action_resume)
                val watchAgainText = stringResource(Res.string.action_watch_again)
                val isMovieWatched = remember(meta.id, meta.type, hasEpisodes, watchedUiState.watchedKeys) {
                    meta.type != "series" && !hasEpisodes && WatchedRepository.isWatched(
                        id = meta.id,
                        type = meta.type,
                    )
                }
                val chooseEpisodesText = stringResource(Res.string.shuffle_change_selection)
                val playButtonLabel = remember(movieProgress, isMovieWatched, seriesAction, meta.type, hasEpisodes, playText, resumeText, watchAgainText, shufflePoolEmpty, chooseEpisodesText) {
                    when {
                        shufflePoolEmpty -> chooseEpisodesText
                        (meta.type == "series" || hasEpisodes) && seriesAction != null ->
                            seriesAction.label
                        meta.type != "series" && !hasEpisodes && movieProgress != null ->
                            resumeText
                        isMovieWatched ->
                            watchAgainText
                        else -> playText
                    }
                }
                val onPrimaryPlayClick: () -> Unit = {
                    stopHeroTrailerForNavigation()
                    when {
                        shufflePoolEmpty -> showShuffle = true
                        (meta.type == "series" || hasEpisodes) && seriesAction != null -> {
                            onPlay?.invoke(
                                meta.type,
                                seriesStreamVideoId ?: seriesAction.videoId,
                                meta.id,
                                meta.type,
                                meta.name,
                                meta.logo,
                                meta.poster,
                                meta.background,
                                seriesAction.seasonNumber,
                                seriesAction.episodeNumber,
                                seriesAction.episodeTitle,
                                seriesAction.episodeThumbnail,
                                seriesPauseDescription,
                                seriesAction.resumePositionMs,
                            )
                        }

                        else -> {
                            onPlay?.invoke(
                                meta.type,
                                meta.id,
                                meta.id,
                                meta.type,
                                meta.name,
                                meta.logo,
                                meta.poster,
                                meta.background,
                                null,
                                null,
                                null,
                                null,
                                meta.description,
                                movieProgress?.lastPositionMs,
                            )
                        }
                    }
                }
                fun runPlayAction(
                    handler: ((type: String, videoId: String, parentMetaId: String, parentMetaType: String, title: String, logo: String?, poster: String?, background: String?, seasonNumber: Int?, episodeNumber: Int?, episodeTitle: String?, episodeThumbnail: String?, pauseDescription: String?, resumePositionMs: Long?) -> Unit)?,
                    resumePositionMs: Long?,
                ) {
                    handler ?: return
                    stopHeroTrailerForNavigation()
                    if ((meta.type == "series" || hasEpisodes) && seriesAction != null) {
                        handler(
                            meta.type,
                            seriesStreamVideoId ?: seriesAction.videoId,
                            meta.id,
                            meta.type,
                            meta.name,
                            meta.logo,
                            meta.poster,
                            meta.background,
                            seriesAction.seasonNumber,
                            seriesAction.episodeNumber,
                            seriesAction.episodeTitle,
                            seriesAction.episodeThumbnail,
                            seriesPauseDescription,
                            resumePositionMs,
                        )
                    } else {
                        handler(
                            meta.type,
                            meta.id,
                            meta.id,
                            meta.type,
                            meta.name,
                            meta.logo,
                            meta.poster,
                            meta.background,
                            null,
                            null,
                            null,
                            null,
                            meta.description,
                            resumePositionMs,
                        )
                    }
                }

                val activeResumePositionMs = if ((meta.type == "series" || hasEpisodes) && seriesAction != null) {
                    seriesAction.resumePositionMs
                } else {
                    movieProgress?.lastPositionMs
                }
                val onPlayFromStartClick: (() -> Unit)? = onPlayFromStart
                    ?.takeIf { (activeResumePositionMs ?: 0L) > 0L }
                    ?.let { handler -> { runPlayAction(handler, 0L) } }
                val onPlayExternallyClick: (() -> Unit)? = onPlayExternally
                    ?.let { handler -> { runPlayAction(handler, activeResumePositionMs) } }

                val episodeNavigator = rememberEpisodeNavigatorState(meta.id)
                val episodeDownloadFor = rememberEpisodeDownloadLookup(meta.id)
                val showDownloadButton by remember {
                    DownloadsSettingsRepository.ensureLoaded()
                    DownloadsSettingsRepository.showDownloadButton
                }.collectAsStateWithLifecycle()
                val onDownloadClick: (() -> Unit)? = if (showDownloadButton) {
                    {
                        stopHeroTrailerForNavigation()
                        val targetSeason = episodeNavigator.selectedSeason
                            ?: seriesAction?.seasonNumber
                            ?: initialSeasonNumber
                            ?: meta.videos.mapNotNull { it.season }.firstOrNull { it > 0 }
                            ?: 1
                        BatchDownloadCoordinator.openPlan(
                            meta = meta,
                            season = targetSeason,
                        )
                    }
                } else {
                    null
                }
                val manualPlayHandler = onPlayManually
                val showManualPlayOption = manualPlayHandler != null && StreamAutoPlayPolicy.isEffectivelyEnabled(playerSettingsUiState)
                val onPrimaryPlayLongClick: (() -> Unit)? = manualPlayHandler
                    ?.takeIf { !shufflePoolEmpty && showManualPlayOption && playbackAvailability.canStream(meta.type, primaryVideoId) }
                    ?.let { manualPlay ->
                        {
                            stopHeroTrailerForNavigation()
                            when {
                                (meta.type == "series" || hasEpisodes) && seriesAction != null -> {
                                    manualPlay(
                                        meta.type,
                                        seriesStreamVideoId ?: seriesAction.videoId,
                                        meta.id,
                                        meta.type,
                                        meta.name,
                                        meta.logo,
                                        meta.poster,
                                        meta.background,
                                        seriesAction.seasonNumber,
                                        seriesAction.episodeNumber,
                                        seriesAction.episodeTitle,
                                        seriesAction.episodeThumbnail,
                                        seriesPauseDescription,
                                        seriesAction.resumePositionMs,
                                    )
                                }

                                else -> {
                                    manualPlay(
                                        meta.type,
                                        meta.id,
                                        meta.id,
                                        meta.type,
                                        meta.name,
                                        meta.logo,
                                        meta.poster,
                                        meta.background,
                                        null,
                                        null,
                                        null,
                                        null,
                                        meta.description,
                                        movieProgress?.lastPositionMs,
                                    )
                                }
                            }
                        }
                    }
                val onEpisodePlayClick: (MetaVideo) -> Unit = { video ->
                    val season = video.season
                    val episode = video.episode
                    val playbackVideoId = buildPlaybackVideoId(
                        parentMetaId = meta.id,
                        seasonNumber = season,
                        episodeNumber = episode,
                        fallbackVideoId = video.id,
                    )
                    val streamVideoId = video.id.takeIf { it.isNotBlank() } ?: playbackVideoId
                    val savedProgress = watchProgressUiState.byVideoId[streamVideoId]
                        ?.takeUnless { it.isCompleted }
                    stopHeroTrailerForNavigation()
                    onPlay?.invoke(
                        meta.type,
                        streamVideoId,
                        meta.id,
                        meta.type,
                        meta.name,
                        meta.logo,
                        meta.poster,
                        meta.background,
                        season,
                        episode,
                        video.title,
                        video.thumbnail,
                        video.overview,
                        savedProgress?.lastPositionMs,
                    )
                }
                val onEpisodeManualPlayClick: (MetaVideo) -> Unit = { video ->
                    val season = video.season
                    val episode = video.episode
                    val playbackVideoId = buildPlaybackVideoId(
                        parentMetaId = meta.id,
                        seasonNumber = season,
                        episodeNumber = episode,
                        fallbackVideoId = video.id,
                    )
                    val streamVideoId = video.id.takeIf { it.isNotBlank() } ?: playbackVideoId
                    val savedProgress = watchProgressUiState.byVideoId[streamVideoId]
                        ?.takeUnless { it.isCompleted }
                    stopHeroTrailerForNavigation()
                    onPlayManually?.invoke(
                        meta.type,
                        streamVideoId,
                        meta.id,
                        meta.type,
                        meta.name,
                        meta.logo,
                        meta.poster,
                        meta.background,
                        season,
                        episode,
                        video.title,
                        video.thumbnail,
                        video.overview,
                        savedProgress?.lastPositionMs,
                    )
                }
                if (showShuffle && showShuffleButton) {
                    EpisodeShuffleSheet(
                        meta = meta,
                        settings = shuffleSettings,
                        onSave = saveShuffle,
                        watchedKeys = watchedUiState.watchedKeys,
                        progressEntries = watchProgressUiState.entries,
                        blurUnwatchedEpisodes = metaScreenSettingsUiState.blurUnwatchedEpisodes,
                        onDismiss = { showShuffle = false },
                        onPlay = onEpisodePlayClick,
                        onPlayManually = onEpisodeManualPlayClick.takeIf { showManualPlayOption },
                        onStartFromBeginning = { video ->
                            onPlay?.invoke(meta.type, video.id, meta.id, meta.type, meta.name, meta.logo,
                                meta.poster, meta.background, video.season, video.episode, video.title,
                                video.thumbnail, video.overview, 0L)
                        },
                    )
                }
                val listState = rememberLazyListState()
                val playFocusRequester = remember(meta.id) { FocusRequester() }
                val actionSectionListIndex = detailActionSectionLazyListIndex(metaScreenSettingsUiState) { key ->
                    metaSectionHasContent(
                        key = key,
                        meta = meta,
                        hasProductionSection = hasProductionSection,
                        hasTrailersSection = hasTrailersSection,
                        hasEpisodes = hasEpisodes,
                        hasAdditionalInfoSection = hasAdditionalInfoSection,
                        hasCollectionSection = hasCollectionSection,
                        hasMoreLikeThisSection = hasMoreLikeThisSection,
                        shouldShowComments = shouldShowComments,
                        comments = comments,
                        isCommentsLoading = isCommentsLoading,
                        commentsError = commentsError,
                    )
                }
                val heroStretchState = rememberHeroStretchState(listState)
                val density = LocalDensity.current
                val safeAreaTopPx = with(density) {
                    WindowInsets.statusBars
                        .asPaddingValues()
                        .calculateTopPadding()
                        .toPx()
                }
                val heroHeightPxState = remember(meta.id) { mutableIntStateOf(0) }
                val heroHeightPx = heroHeightPxState.intValue
                val detailScrollOffsetProvider = remember(listState, heroHeightPxState) {
                    {
                        if (listState.firstVisibleItemIndex == 0) {
                            listState.firstVisibleItemScrollOffset.toFloat()
                        } else {
                            heroHeightPxState.intValue.toFloat() + listState.firstVisibleItemScrollOffset
                        }
                    }
                }
                val isScrolledPastHeroHeaderThreshold by remember(
                    listState,
                    heroHeightPxState,
                    safeAreaTopPx,
                    detailScrollOffsetProvider,
                ) {
                    derivedStateOf {
                        val measuredHeroHeightPx = heroHeightPxState.intValue
                        val thresholdPx = (measuredHeroHeightPx - safeAreaTopPx).coerceAtLeast(0f)
                        measuredHeroHeightPx > 0 &&
                            (listState.firstVisibleItemIndex > 0 || detailScrollOffsetProvider() > thresholdPx)
                    }
                }
                val isHeroTrailerWithinPlayThreshold by remember(
                    listState,
                    heroHeightPxState,
                    safeAreaTopPx,
                    detailScrollOffsetProvider,
                ) {
                    derivedStateOf {
                        val measuredHeroHeightPx = heroHeightPxState.intValue
                        val thresholdPx = (measuredHeroHeightPx - safeAreaTopPx).coerceAtLeast(0f)
                        measuredHeroHeightPx == 0 || detailScrollOffsetProvider() <= thresholdPx
                    }
                }
                val headerTarget = if (isScrolledPastHeroHeaderThreshold) 1f else 0f
                val heroTrailerSourceUrl = heroTrailerPlaybackSource
                    ?.videoUrl
                    ?.takeIf { it.isNotBlank() && heroTrailerPlaybackEnabled && !heroTrailerFinished && !isLeavingDetails }
                val heroTrailerSourceAudioUrl = heroTrailerPlaybackSource
                    ?.audioUrl
                    ?.takeIf { heroTrailerSourceUrl != null && it.isNotBlank() }
                val heroTrailerPlayWhenReady = heroTrailerSourceUrl != null &&
                    !isLeavingDetails &&
                    isHeroTrailerWithinPlayThreshold
                val headerProgressState = animateFloatAsState(
                    targetValue = headerTarget,
                    animationSpec = tween(
                        durationMillis = if (headerTarget > 0f) 150 else 100,
                        easing = LinearOutSlowInEasing,
                    ),
                    label = "detail_floating_header_progress",
                )
                val headerProgressProvider = remember(headerProgressState) {
                    { headerProgressState.value }
                }
                val showHeroBackButton by remember(headerProgressState) {
                    derivedStateOf { headerProgressState.value <= 0.05f }
                }
                val headerInteractive by remember(headerProgressState) {
                    derivedStateOf { headerProgressState.value > 0.05f }
                }

                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val colorScheme = MaterialTheme.colorScheme
                    val useTabletLayout = minOf(maxWidth, maxHeight) >= 600.dp
                    val screenHeight = maxHeight
                    val isTablet = useTabletLayout || maxWidth >= 720.dp
                    val tabletViewportHeight = maxHeight
                    val isTabletLandscape = maxWidth > maxHeight
                    val contentHorizontalPadding = if (isTablet) 32.dp else 18.dp
                    val contentMaxWidth = detailTabletContentMaxWidth(maxWidth, isTablet)
                    val backdropUrl = meta.background ?: meta.poster
                    var heroBackdropPainter by remember(backdropUrl) { mutableStateOf<Painter?>(null) }
                    val backgroundMode = metaScreenSettingsUiState.backgroundMode
                    val cinematicBackground = backgroundMode == MetaScreenBackgroundMode.Cinematic &&
                        !backdropUrl.isNullOrBlank()
                    val cinematicBackdrop = rememberArtworkBackdrop(
                        imageUrl = backdropUrl.takeIf { cinematicBackground },
                        enabled = deferredMetaWorkAllowed,
                    )
                    val dominantColorEnabled = backgroundMode == MetaScreenBackgroundMode.DominantColor &&
                        !backdropUrl.isNullOrBlank()
                    val extractedDominantColor = rememberArtworkDominantColor(
                        url = backdropUrl,
                        enabled = dominantColorEnabled,
                    )
                    val dominantBackdropTargetColor = if (dominantColorEnabled && extractedDominantColor != null) {
                        dominantBackdropBlendColor(extractedDominantColor, colorScheme.background)
                    } else {
                        colorScheme.background
                    }
                    val dominantBackdropColor by animateColorAsState(
                        targetValue = dominantBackdropTargetColor,
                        animationSpec = tween(
                            durationMillis = 320,
                            easing = LinearOutSlowInEasing,
                        ),
                        label = "detail_dominant_backdrop_color",
                    )
                    val cinematicHeaderContainerColor = if (backgroundMode == MetaScreenBackgroundMode.Normal) {
                        colorScheme.background
                    } else {
                        Color.Transparent
                    }
                    val cinematicHeaderTopChromeColor = if (backgroundMode == MetaScreenBackgroundMode.Normal) {
                        Color.Black
                    } else {
                        Color.Transparent
                    }
                    val cinematicHeaderEffectiveBackgroundColor = when (backgroundMode) {
                        MetaScreenBackgroundMode.Normal,
                        MetaScreenBackgroundMode.Cinematic -> colorScheme.background
                        MetaScreenBackgroundMode.DominantColor -> dominantBackdropColor
                    }
                    val cinematicHeaderContentColor = readableDetailContentColor(
                        cinematicHeaderEffectiveBackgroundColor,
                    )

                    val cinematicGlassEnabled = backgroundMode == MetaScreenBackgroundMode.Cinematic
                    Box(modifier = Modifier.fillMaxSize()) {
                        when (backgroundMode) {
                            MetaScreenBackgroundMode.Normal -> Unit
                            MetaScreenBackgroundMode.Cinematic -> if (backdropUrl != null) {
                                val cinematicModifier = Modifier.fillMaxSize()
                                val heroPainter = heroBackdropPainter
                                if (heroPainter != null) {
                                    Image(
                                        painter = heroPainter,
                                        contentDescription = null,
                                        modifier = cinematicModifier,
                                        contentScale = ContentScale.Crop,
                                    )
                                } else if (deferredMetaWorkAllowed) {
                                    AsyncImage(
                                        model = backdropUrl,
                                        contentDescription = null,
                                        modifier = cinematicModifier,
                                        contentScale = ContentScale.Crop,
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(colorScheme.background.copy(alpha = 0.92f)),
                                )
                            }
                            MetaScreenBackgroundMode.DominantColor -> if (deferredMetaWorkAllowed) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(dominantBackdropColor),
                                )
                            }
                        }
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .detailsContentReveal(metaScreenSettingsUiState.posterTransitionEnabled)
                                .then(if (cinematicGlassEnabled) Modifier.nuvioBackdropSource(cinematicContentBackdrop) else Modifier),
                        ) {
                            item(
                                key = "detail-hero",
                                contentType = "detail-hero",
                            ) {
                                if (cinematicDetailHeaderEnabled) {
                                    CinematicDetailHeader(
                                        meta = cinematicHeaderMeta,
                                        isTablet = isTablet,
                                        contentMaxWidth = contentMaxWidth,
                                        scrollOffsetProvider = detailScrollOffsetProvider,
                                        stretchPx = { heroStretchState.stretchPx },
                                        onHeightChanged = { heroHeightPxState.intValue = it },
                                        onBackdropLoaded = { painter, imageBitmap ->
                                            heroBackdropPainter = painter
                                            dominantBackdropPainter = painter
                                            dominantBackdropImageBitmap = imageBitmap
                                        },
                                        onCompanyClick = onCompanyClick,
                                        trailerSourceUrl = heroTrailerSourceUrl,
                                        trailerSourceAudioUrl = heroTrailerSourceAudioUrl,
                                        trailerAutoPlay = metaScreenSettingsUiState.heroTrailerPlayback,
                                        trailerPlaybackAllowed = !isLeavingDetails && isHeroTrailerWithinPlayThreshold,
                                        trailerMuted = cinematicTrailerMuted,
                                        containerColor = cinematicHeaderContainerColor,
                                        effectiveBackgroundColor = cinematicHeaderEffectiveBackgroundColor,
                                        contentColor = cinematicHeaderContentColor,
                                        topChromeColor = cinematicHeaderTopChromeColor,
                                        contentMode = cinematicHeaderContentMode,
                                        watchProviderAvailability = cinematicWatchProviders,
                                        tmdbAvailable = tmdbSettingsUiState.enabled && tmdbSettingsUiState.hasApiKey,
                                        onContentModeChange = NuvioEnhancedSettingsRepository::setCinematicHeaderContentMode,
                                        onTrailerMuteToggle = {
                                            cinematicTrailerMuted = !cinematicTrailerMuted
                                        },
                                    )
                                } else {
                                    DetailHero(
                                        meta = meta,
                                        isTablet = isTablet,
                                        contentMaxWidth = contentMaxWidth,
                                        scrollOffsetProvider = detailScrollOffsetProvider,
                                        stretchPx = { heroStretchState.stretchPx },
                                        onHeightChanged = { heroHeightPxState.intValue = it },
                                        heroTrailerSourceUrl = heroTrailerSourceUrl,
                                        heroTrailerSourceAudioUrl = heroTrailerSourceAudioUrl,
                                        heroTrailerReady = heroTrailerReady,
                                        heroTrailerPlayWhenReady = heroTrailerPlayWhenReady,
                                        heroTrailerMuted = heroTrailerMuted,
                                        heroGradientColor = dominantBackdropColor.takeIf { dominantColorEnabled },
                                        onBackdropLoaded = { painter, imageBitmap ->
                                            heroBackdropPainter = painter
                                            dominantBackdropPainter = painter
                                            dominantBackdropImageBitmap = imageBitmap
                                        },
                                        onHeroTrailerMuteToggle = {
                                            HeroTrailerAudioState.toggleMuted()
                                        },
                                        onHeroTrailerReady = {
                                            if (!heroTrailerFinished) {
                                                heroTrailerReady = true
                                            }
                                        },
                                        onHeroTrailerEnded = {
                                            heroTrailerReady = false
                                            heroTrailerFinished = true
                                        },
                                        onHeroTrailerError = {
                                            heroTrailerReady = false
                                            heroTrailerFinished = true
                                        },
                                    )
                                }
                            }

                            configuredMetaSectionItems(
                                settings = metaScreenSettingsUiState,
                                meta = meta,
                                isTablet = isTablet,
                                contentHorizontalPadding = contentHorizontalPadding,
                                contentMaxWidth = if (isTablet) contentMaxWidth else Dp.Unspecified,
                                 playButtonLabel = playButtonLabel,
                                 playFocusRequester = playFocusRequester,
                                isSaved = isSaved,
                                isWatched = isWatched,
                                onPrimaryPlayClick = onPrimaryPlayClick,
                                onPrimaryPlayLongClick = onPrimaryPlayLongClick,
                                featuredAction = randomEpisodeAction,
                                downloadAction = downloadAction,
                                onSaveClick = toggleSaved,
                                onSaveLongClick = openLibraryListPicker,
                                 onWatchedClick = toggleWatched,
                                 onOpenAnimeTracking = if (supportsAnimeTracking) {
                                     { animeTrackingEditorRequest = AnimeTrackingEditorRequest() }
                                 } else {
                                     null
                                 },
                                showManualPlayOption = showManualPlayOption,
                                preferredEpisodeSeasonNumber = seriesAction?.seasonNumber,
                                preferredEpisodeNumber = seriesAction?.episodeNumber,
                                hasProductionSection = hasProductionSection,
                                hasTrailersSection = hasTrailersSection,
                                hasEpisodes = hasEpisodes,
                                hasAdditionalInfoSection = hasAdditionalInfoSection,
                                hasCollectionSection = hasCollectionSection,
                                hasMoreLikeThisSection = hasMoreLikeThisSection,
                                shouldShowComments = shouldShowComments,
                                comments = comments,
                                isCommentsLoading = isCommentsLoading,
                                isCommentsLoadingMore = isCommentsLoadingMore,
                                commentsCurrentPage = commentsCurrentPage,
                                commentsPageCount = commentsPageCount,
                                commentsError = commentsError,
                                episodeImdbRatings = episodeImdbRatings,
                                episodeTmdbRatings = episodeTmdbRatings,
                                showNuvioRead = shouldEnableDetailNuvioRead(
                                    nuvioReadEnabled = nuvioEnhancedSettings.nuvioReadEnabled,
                                    cinematicDetailHeaderEnabled = cinematicDetailHeaderEnabled,
                                ),
                                descriptionOnlyOverview = shouldUseDescriptionOnlyDetailOverview(
                                    cinematicDetailHeaderEnabled,
                                ),
                                onRetryComments = {
                                    detailsScope.launch {
                                        isCommentsLoading = true
                                        commentsError = null
                                        try {
                                            val result = TraktCommentsRepository.getCommentsPage(meta, page = 1, forceRefresh = true)
                                            comments = result.items
                                            commentsCurrentPage = result.currentPage
                                            commentsPageCount = result.pageCount
                                        } catch (e: Exception) {
                                            commentsError = e.message ?: getString(Res.string.details_comments_load_failed)
                                        }
                                    }
                                },
                                onLoadMoreComments = {
                                    detailsScope.launch {
                                        isCommentsLoadingMore = true
                                        try {
                                            val nextPage = commentsCurrentPage + 1
                                            val result = TraktCommentsRepository.getCommentsPage(meta, page = nextPage)
                                            val existingIds = comments.map { it.id }.toSet()
                                            val newComments = result.items.filter { it.id !in existingIds }
                                            comments = comments + newComments
                                            commentsCurrentPage = result.currentPage
                                            commentsPageCount = result.pageCount
                                        } catch (_: Exception) { }
                                        isCommentsLoadingMore = false
                                    }
                                },
                                onCommentClick = { review -> selectedComment = review },
                                onTrailerClick = resolveTrailer,
                                progressByVideoId = progressByVideoId,
                                watchedKeys = watchedUiState.watchedKeys,
                                blurUnwatchedEpisodes = metaScreenSettingsUiState.blurUnwatchedEpisodes,
                                showEpisodeRatings = metaScreenSettingsUiState.showEpisodeRatings,
                                onEpisodeClick = onEpisodePlayClick,
                                onEpisodeLongPress = { video -> selectedEpisodeForActions = video },
                                onSeasonLongPress = { season -> selectedSeasonForActions = season },
                                onOpenMeta = onOpenMeta,
                                onCastClick = onCastClick,
                                onCompanyClick = onCompanyClick,
                                sharedTransitionScope = sharedTransitionScope,
                                animatedVisibilityScope = animatedVisibilityScope,
                            )

                            item(
                                key = "detail-bottom-spacer",
                                contentType = "detail-spacer",
                            ) {
                                Spacer(modifier = Modifier.height(nuvioSafeBottomPadding(32.dp)))
                            }
                        }

                        if (
                            shouldRenderDetailBackdropBridge(
                                backgroundMode = backgroundMode,
                                cinematicDetailHeaderEnabled = cinematicDetailHeaderEnabled,
                            ) && deferredMetaWorkAllowed && heroHeightPx > 0
                        ) {
                            val blendColor = dominantBackdropColor.takeIf { dominantColorEnabled }
                                ?: colorScheme.background
                            Box(
                                modifier = Modifier
                                    .zIndex(0.5f)
                                    .fillMaxWidth()
                                    .height(132.dp)
                                    .graphicsLayer {
                                        translationY = heroHeightPx.toFloat() - detailScrollOffsetProvider()
                                    }
                                    Box(
                                        modifier = Modifier
                                            .zIndex(0.5f)
                                            .fillMaxWidth()
                                            .height(blendBandHeight)
                                            .graphicsLayer {
                                                translationY = heroHeightPx.intValue.toFloat() -
                                                    blendFadeInPx -
                                                    detailScrollOffsetPx()
                                            }
                                            .background(blendBrush),
                                    )
                                }
                            }
                        }

                        if (showHeroBackButton) {
                            NuvioBackButton(
                                onClick = onBackFromDetails,
                                modifier = Modifier
                                    .padding(
                                        start = 12.dp,
                                        top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 8.dp,
                                    )
                                    .zIndex(2f)
                                    .onPreviewKeyEvent { event ->
                                        if (
                                            event.type == KeyEventType.KeyDown &&
                                            event.key == Key.DirectionDown
                                        ) {
                                            detailsScope.launch {
                                                actionSectionListIndex?.let { index ->
                                                    listState.animateScrollToItem(index)
                                                    withFrameNanos { }
                                                }
                                                playFocusRequester.requestFocus()
                                            }
                                            true
                                        } else {
                                            false
                                        }
                                    },
                                containerColor = Color.Transparent,
                                contentColor = if (cinematicDetailHeaderEnabled) {
                                    cinematicHeaderContentColor
                                } else {
                                    MaterialTheme.colorScheme.onBackground
                                },
                            )
                        }

                        DetailFloatingHeader(
                            meta = meta,
                            isSaved = isSaved,
                            progressProvider = headerProgressProvider,
                            interactive = headerInteractive,
                            backgroundColor = dominantBackdropColor.takeIf { dominantColorEnabled },
                            headerBackground = null,
                            onBack = onBackFromDetails,
                            onToggleSaved = toggleSaved,
                            modifier = Modifier.zIndex(2f),
                        )

                        if (aiAssistantSettings.enabled && supportsAiAssistant) {
                            SmallFloatingActionButton(
                                onClick = { showAiAssistant = true },
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(
                                        end = 18.dp,
                                        bottom = nuvioSafeBottomPadding(18.dp),
                                    )
                                    .zIndex(3f),
                                shape = androidx.compose.foundation.shape.CircleShape,
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AutoAwesome,
                                    contentDescription = stringResource(Res.string.ai_chat_title),
                                )
                            }
                        }

                        if (showAiAssistant) {
                            AiAssistantSheet(
                                meta = meta,
                                settings = aiAssistantSettings,
                                onDismiss = { showAiAssistant = false },
                            )
                        }

                        selectedEpisodeForActions?.let { selectedEpisode ->
                            val isSelectedEpisodeWatched = remember(meta, selectedEpisode, watchedUiState.watchedKeys, progressByVideoId) {
                                isEpisodeWatchedForActions(
                                    meta = meta,
                                    episode = selectedEpisode,
                                    watchedKeys = watchedUiState.watchedKeys,
                                    progressByVideoId = progressByVideoId,
                                )
                            }
                            val previousEpisodes = remember(meta, selectedEpisode, todayIsoDate) {
                                meta.previousReleasedEpisodesBefore(
                                    target = selectedEpisode,
                                    todayIsoDate = todayIsoDate,
                                )
                            }
                            val seasonEpisodes = remember(meta, selectedEpisode, todayIsoDate) {
                                meta.releasedEpisodesForSeason(
                                    seasonNumber = selectedEpisode.season,
                                    todayIsoDate = todayIsoDate,
                                )
                            }
                            val arePreviousEpisodesWatched = remember(previousEpisodes, watchedUiState.watchedKeys, progressByVideoId) {
                                areEpisodesWatchedForActions(
                                    meta = meta,
                                    episodes = previousEpisodes,
                                    watchedKeys = watchedUiState.watchedKeys,
                                    progressByVideoId = progressByVideoId,
                                )
                            }
                            val isSeasonWatched = remember(seasonEpisodes, watchedUiState.watchedKeys, progressByVideoId) {
                                areEpisodesWatchedForActions(
                                    meta = meta,
                                    episodes = seasonEpisodes,
                                    watchedKeys = watchedUiState.watchedKeys,
                                    progressByVideoId = progressByVideoId,
                                )
                            }
                            val episodeRatingTarget = remember(meta, selectedEpisode) {
                                meta.episodeUserRatingTarget(selectedEpisode)
                            }
                            val canRateEpisode = rememberCanRate(episodeRatingTarget)
                            val episodeUserRating = rememberUserRating(episodeRatingTarget)
                            EpisodeWatchedActionSheet(
                                episode = selectedEpisode,
                                seasonLabel = seasonLabel(selectedEpisode.season ?: 0),
                                isEpisodeWatched = isSelectedEpisodeWatched,
                                canMarkPreviousEpisodes = previousEpisodes.isNotEmpty(),
                                arePreviousEpisodesWatched = arePreviousEpisodesWatched,
                                isSeasonWatched = isSeasonWatched,
                                onDismiss = { selectedEpisodeForActions = null },
                                onToggleWatched = {
                                    WatchingActions.toggleEpisodeWatched(
                                        meta = meta,
                                        episode = selectedEpisode,
                                        isCurrentlyWatched = isSelectedEpisodeWatched,
                                    )
                                },
                                onTogglePreviousWatched = {
                                    WatchingActions.togglePreviousEpisodesWatched(
                                        meta = meta,
                                        episodes = previousEpisodes,
                                        areCurrentlyWatched = arePreviousEpisodesWatched,
                                    )
                                },
                                onToggleSeasonWatched = {
                                    WatchingActions.toggleSeasonWatched(
                                        meta = meta,
                                        episodes = seasonEpisodes,
                                        areCurrentlyWatched = isSeasonWatched,
                                    )
                                },
                                onDownload = {
                                    episodeDownloadRequest = EpisodeDownloadRequest(
                                        initialEpisodes = listOf(selectedEpisode),
                                        showStartPrompt = false,
                                    )
                                },
                                showPlayManually = showManualPlayOption,
                                onPlayManually = {
                                    onEpisodeManualPlayClick(selectedEpisode)
                                },
                                onTracking = if (supportsAnimeTracking) {
                                    {
                                        animeTrackingEditorRequest = AnimeTrackingEditorRequest(
                                            season = selectedEpisode.season,
                                            episode = selectedEpisode.episode,
                                        )
                                    }
                                } else {
                                    null
                                },
                            )
                        }

                        animeTrackingEditorRequest?.let { request ->
                            AnimeTrackingEditorSheet(
                                meta = meta,
                                initialSeason = request.season,
                                initialEpisode = request.episode,
                                onDismiss = { animeTrackingEditorRequest = null },
                            )
                        }

                        episodeDownloadRequest?.let { request ->
                            EpisodeDownloadFlowSheet(
                                meta = meta,
                                defaultEpisode = seriesActionVideo,
                                initialEpisodes = request.initialEpisodes,
                                showStartPrompt = request.showStartPrompt,
                                onDismiss = { episodeDownloadRequest = null },
                            )
                        }

                        selectedSeasonForActions?.let { selectedSeason ->
                            val seasonLabel = seasonLabel(selectedSeason)
                            val seasonEpisodes = remember(meta, selectedSeason, todayIsoDate) {
                                meta.releasedEpisodesForSeason(
                                    seasonNumber = selectedSeason,
                                    todayIsoDate = todayIsoDate,
                                )
                            }
                            val previousSeasonEpisodes = remember(meta, selectedSeason, todayIsoDate) {
                                val normalizedSelectedSeason = selectedSeason.coerceAtLeast(0)
                                meta.releasedPlayableEpisodes(todayIsoDate)
                                    .filter { episode ->
                                        val season = episode.season?.coerceAtLeast(0) ?: 0
                                        season > 0 && season < normalizedSelectedSeason
                                    }
                            }
                            val isSeasonWatched = remember(seasonEpisodes, watchedUiState.watchedKeys, progressByVideoId) {
                                areEpisodesWatchedForActions(
                                    meta = meta,
                                    episodes = seasonEpisodes,
                                    watchedKeys = watchedUiState.watchedKeys,
                                    progressByVideoId = progressByVideoId,
                                )
                            }
                            val canMarkPreviousSeasons = remember(previousSeasonEpisodes, watchedUiState.watchedKeys, progressByVideoId) {
                                previousSeasonEpisodes.any { episode ->
                                    !isEpisodeWatchedForActions(
                                        meta = meta,
                                        episode = episode,
                                        watchedKeys = watchedUiState.watchedKeys,
                                        progressByVideoId = progressByVideoId,
                                    )
                                }
                            }
                            val seasonRatingTarget = remember(meta, selectedSeason) {
                                meta.seasonUserRatingTarget(selectedSeason)
                            }
                            val canRateSeason = rememberCanRate(seasonRatingTarget)
                            val seasonUserRating = rememberUserRating(seasonRatingTarget)
                            SeasonWatchedActionSheet(
                                seasonLabel = seasonLabel,
                                isSeasonWatched = isSeasonWatched,
                                canMarkPreviousSeasons = canMarkPreviousSeasons,
                                onDismiss = { selectedSeasonForActions = null },
                                onToggleSeasonWatched = {
                                    WatchingActions.toggleSeasonWatched(
                                        meta = meta,
                                        episodes = seasonEpisodes,
                                        areCurrentlyWatched = isSeasonWatched,
                                    )
                                },
                                onDownloadSeason = if (enhancedSettings.seasonBatchDownloadEnabled) {
                                    ({ enqueueSeasonBatch(detailsScope, meta, seasonEpisodes) })
                                } else {
                                    null
                                },
                                onMarkPreviousSeasonsWatched = {
                                    WatchingActions.togglePreviousEpisodesWatched(
                                        meta = meta,
                                        episodes = previousSeasonEpisodes,
                                        areCurrentlyWatched = false,
                                    )
                                },
                                userRating = seasonUserRating,
                                onRate = if (canRateSeason) {
                                    {
                                        val request = UserRatingSheetRequest(
                                            target = seasonRatingTarget,
                                            title = seasonLabel,
                                            subtitle = meta.name,
                                        )
                                        detailsScope.launch {
                                            delay(RATING_SHEET_OPEN_DELAY_MS)
                                            userRatingSheetRequest = request
                                        }
                                    }
                                } else {
                                    null
                                },
                            )
                        }

                        val activeBatchPlan by BatchDownloadCoordinator.activePlan.collectAsStateWithLifecycle()
                        activeBatchPlan?.let { plan ->
                            BatchDownloadDialog(
                                plan = plan,
                                isEpisodeWatched = { ep ->
                                    meta.isEpisodeWatchedForNavigator(ep, progressByVideoId, watchedUiState.watchedKeys)
                                },
                                onDismissRequest = {
                                    BatchDownloadCoordinator.closePlan()
                                },
                                onConfirm = {
                                    val message = when (BatchDownloadCoordinator.queuePlan()) {
                                        BatchQueueResult.Queued -> Res.string.downloads_enqueue_queued_multiple
                                        BatchQueueResult.NothingToQueue -> Res.string.season_download_no_source_found
                                    }
                                    detailsScope.launch { NuvioToastController.show(getString(message)) }
                                },
                            )
                        }

                        userRatingSheetRequest?.let { request ->
                            UserRatingSheet(
                                target = request.target,
                                title = request.title,
                                subtitle = request.subtitle,
                                onDismiss = { userRatingSheetRequest = null },
                            )
                        }

                        if (inAppTrailerPlaybackEnabled) {
                            TrailerPlayerPopup(
                                visible = selectedTrailer != null,
                                trailerTitle = selectedTrailer?.displayName ?: selectedTrailer?.name.orEmpty(),
                                trailerType = selectedTrailer?.type.orEmpty(),
                                contentTitle = meta.name,
                                playbackSource = trailerPlaybackSource,
                                isLoading = trailerLoading,
                                errorMessage = trailerErrorMessage,
                                onDismiss = {
                                    trailerRequestToken += 1
                                    trailerLoading = false
                                    trailerPlaybackSource = null
                                    trailerErrorMessage = null
                                    selectedTrailer = null
                                },
                                onRetry = selectedTrailer?.let { trailer ->
                                    { resolveTrailer(trailer) }
                                },
                            )
                        }

                        TrackingListPickerDialog(
                            visible = showLibraryListPicker,
                            title = meta.name,
                            tabs = pickerTabs,
                            membership = pickerMembership,
                            isPending = pickerPending,
                            errorMessage = pickerError,
                            onToggle = { listKey ->
                                pickerMembership = toggleTrackingLibraryMembership(
                                    tabs = pickerTabs,
                                    membership = pickerMembership,
                                    key = listKey,
                                )
                            },
                            onDismiss = {
                                if (!pickerPending) {
                                    showLibraryListPicker = false
                                }
                            },
                            onSave = {
                                detailsScope.launch {
                                    pickerPending = true
                                    pickerError = null
                                    runCatching {
                                        LibraryRepository.applyMembershipChanges(
                                            item = meta.toLibraryItem(savedAtEpochMs = 0L),
                                            desiredMembership = pickerMembership,
                                        )
                                    }.onSuccess {
                                        showLibraryListPicker = false
                                    }.onFailure { error ->
                                        pickerError = error.message ?: getString(Res.string.trakt_lists_update_failed)
                                    }
                                    pickerPending = false
                                }
                            },
                        )

                        selectedComment?.let { comment ->
                            val commentIndex = comments.indexOfFirst { it.id == comment.id }.coerceAtLeast(0)
                            CommentDetailSheet(
                                comment = comment,
                                currentIndex = commentIndex,
                                totalCount = comments.size,
                                canGoBack = commentIndex > 0,
                                canGoForward = commentIndex < comments.size - 1,
                                onPrevious = {
                                    if (commentIndex > 0) {
                                        selectedComment = comments[commentIndex - 1]
                                    }
                                },
                                onNext = {
                                    val nextIndex = commentIndex + 1
                                    if (nextIndex < comments.size) {
                                        selectedComment = comments[nextIndex]
                                    }
                                    if (nextIndex >= comments.size - 3 && commentsCurrentPage < commentsPageCount) {
                                        detailsScope.launch {
                                            isCommentsLoadingMore = true
                                            try {
                                                val nextPage = commentsCurrentPage + 1
                                                val result = TraktCommentsRepository.getCommentsPage(meta, page = nextPage)
                                                val existingIds = comments.map { it.id }.toSet()
                                                val newComments = result.items.filter { it.id !in existingIds }
                                                comments = comments + newComments
                                                commentsCurrentPage = result.currentPage
                                                commentsPageCount = result.pageCount
                                            } catch (_: Exception) { }
                                            isCommentsLoadingMore = false
                                        }
                                    }
                                },
                                onDismiss = { selectedComment = null },
                            )
                        }
                    }
                }
            }
        }

        if (displayedMeta == null) {
            NuvioBackButton(
                onClick = onBack,
                modifier = Modifier.padding(
                    start = 12.dp,
                    top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 8.dp,
                ),
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}

    }
}

private fun enqueueSeasonBatch(
    scope: CoroutineScope,
    meta: MetaDetails,
    episodes: List<MetaVideo>,
) {
    val targets = episodes
        .filter { it.available }
        .map { video ->
            EpisodeDownloadTarget(
                videoId = video.id,
                seasonNumber = video.season,
                episodeNumber = video.episode,
                title = video.title,
                thumbnail = video.thumbnail,
                overview = video.overview,
                embeddedStreams = video.streams,
                parentMetaId = meta.id,
                parentMetaType = meta.type,
            )
        }
    if (targets.isEmpty()) return
    val settings = EpisodeDownloadSettingsStorage.load()
        .copy(downloadMode = DownloadQueueMode.OneAtATime)
    scope.launch {
        try {
            EpisodeDownloadCoordinator.enqueueAuto(
                contentType = meta.type,
                parentMetaId = meta.id,
                parentMetaType = meta.type,
                title = meta.name,
                logo = meta.logo,
                poster = meta.poster,
                background = meta.background,
                targets = targets,
                settings = settings,
                onProgress = {},
            )
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            Logger.withTag("SeasonBatchDownload").w(error) { "enqueueSeasonBatch failed" }
        }
    }
}

private const val RATING_SHEET_OPEN_DELAY_MS = 350L


private fun isEpisodeWatchedForActions(
    meta: MetaDetails,
    episode: MetaVideo,
    watchedKeys: Set<String>,
    progressByVideoId: Map<String, WatchProgressEntry>,
): Boolean {
    val episodeVideoId = buildPlaybackVideoId(
        parentMetaId = meta.id,
        seasonNumber = episode.season,
        episodeNumber = episode.episode,
        fallbackVideoId = episode.id,
    )
    return progressByVideoId[episodeVideoId]?.isEffectivelyCompleted == true ||
        WatchingState.isEpisodeWatched(
            watchedKeys = watchedKeys,
            metaType = meta.type,
            metaId = meta.id,
            episode = episode,
        )
}

private fun areEpisodesWatchedForActions(
    meta: MetaDetails,
    episodes: Collection<MetaVideo>,
    watchedKeys: Set<String>,
    progressByVideoId: Map<String, WatchProgressEntry>,
): Boolean = episodes.isNotEmpty() && episodes.all { episode ->
    isEpisodeWatchedForActions(
        meta = meta,
        episode = episode,
        watchedKeys = watchedKeys,
        progressByVideoId = progressByVideoId,
    )
}

private fun MetaDetails.toMetaPreview(): MetaPreview =
    MetaPreview(
        id = id,
        type = type,
        name = name,
        poster = poster,
        banner = background,
        logo = logo,
        description = description,
        releaseInfo = releaseInfo,
        imdbRating = imdbRating,
        genres = genres,
    )

private fun LazyListScope.configuredMetaSectionItems(
    settings: MetaScreenSettingsUiState,
    isMdbListActive: Boolean,
    meta: MetaDetails,
    isTablet: Boolean,
    contentHorizontalPadding: Dp,
    contentMaxWidth: Dp,
    playButtonLabel: String,
    playFocusRequester: FocusRequester,
    isSaved: Boolean,
    isWatched: Boolean,
    onPrimaryPlayClick: () -> Unit,
    onDownloadClick: (() -> Unit)?,
    onPlayFromStartClick: (() -> Unit)?,
    onPlayExternallyClick: (() -> Unit)?,
    onShuffleClick: (() -> Unit)?,
    shuffleEnabled: Boolean,
    onPrimaryPlayLongClick: (() -> Unit)?,
    featuredAction: DetailSecondaryAction? = null,
    downloadAction: DetailSecondaryAction? = null,
    onSaveClick: () -> Unit,
    onSaveLongClick: (() -> Unit)?,
    onWatchedClick: () -> Unit,
    onOpenAnimeTracking: (() -> Unit)?,
    showManualPlayOption: Boolean,
    preferredEpisodeSeasonNumber: Int?,
    preferredEpisodeNumber: Int?,
    hasProductionSection: Boolean,
    hasTrailersSection: Boolean,
    hasEpisodes: Boolean,
    hasAdditionalInfoSection: Boolean,
    hasCollectionSection: Boolean,
    hasMoreLikeThisSection: Boolean,
    shouldShowComments: Boolean,
    comments: List<TraktCommentReview>,
    isCommentsLoading: Boolean,
    isCommentsLoadingMore: Boolean,
    commentsCurrentPage: Int,
    commentsPageCount: Int,
    commentsError: String?,
    episodeImdbRatings: Map<Pair<Int, Int>, Double>,
    episodeTmdbRatings: Map<Pair<Int, Int>, Double>,
    showNuvioRead: Boolean,
    descriptionOnlyOverview: Boolean,
    onRetryComments: () -> Unit,
    onLoadMoreComments: () -> Unit,
    onCommentClick: (TraktCommentReview) -> Unit,
    onTrailerClick: (MetaTrailer) -> Unit,
    progressByVideoId: Map<String, WatchProgressEntry>,
    watchedKeys: Set<String>,
    fullyWatchedSeriesKeys: Set<String> = emptySet(),
    blurUnwatchedEpisodes: Boolean,
    showEpisodeRatings: Boolean,
    onEpisodeClick: (MetaVideo) -> Unit,
    onEpisodeLongPress: (MetaVideo) -> Unit,
    onSeasonLongPress: (Int) -> Unit,
    onOpenMeta: ((MetaPreview) -> Unit)?,
    onOpenMoreLikeThis: ((MetaDetails) -> Unit)?,
    onCastClick: ((MetaPerson, String?) -> Unit)?,
    onCompanyClick: ((MetaCompany, String) -> Unit)?,
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?,
    episodeNavigator: EpisodeNavigatorState? = null,
    episodeDownloadFor: (MetaVideo) -> DownloadItem? = { null },
    listState: LazyListState? = null,
    leadingItemCount: Int = 0,
    stickyBackground: Color = Color.Unspecified,
    selectedSeason: Int?,
    onSeasonSelected: (Int) -> Unit,
) {
    val enabledItems = settings.items.filter { it.enabled }
    var emittedItemCount = 0
    fun sectionHasContent(key: MetaScreenSectionKey): Boolean =
        metaSectionHasContent(
            key = key,
            meta = meta,
            hasProductionSection = hasProductionSection,
            hasTrailersSection = hasTrailersSection,
            hasEpisodes = hasEpisodes,
            hasAdditionalInfoSection = hasAdditionalInfoSection,
            hasCollectionSection = hasCollectionSection,
            hasMoreLikeThisSection = hasMoreLikeThisSection,
            shouldShowComments = shouldShowComments,
            comments = comments,
            isCommentsLoading = isCommentsLoading,
            commentsError = commentsError,
        )

    fun addSectionItem(
        key: String,
        sectionItems: List<MetaScreenSectionItem>,
        forceTabLayout: Boolean = settings.tabLayout,
    ) {
        val contentType = if (sectionItems.size == 1) {
            "detail-section-${sectionItems.first().key.name}"
        } else {
            "detail-section-tab-group"
        }
        item(
            key = key,
            contentType = contentType,
        ) {
            DetailSectionContainer(
                horizontalPadding = contentHorizontalPadding,
                contentMaxWidth = contentMaxWidth,
            ) {
                ConfiguredMetaSections(
                    settings = settings.copy(
                        items = sectionItems,
                        tabLayout = forceTabLayout,
                    ),
                    isMdbListActive = isMdbListActive,
                    meta = meta,
                    episodeDownloadFor = episodeDownloadFor,
                    isTablet = isTablet,
                    playButtonLabel = playButtonLabel,
                    playFocusRequester = playFocusRequester,
                    isSaved = isSaved,
                    isWatched = isWatched,
                    onPrimaryPlayClick = onPrimaryPlayClick,
                    onDownloadClick = onDownloadClick,
                    onPlayFromStartClick = onPlayFromStartClick,
                    onPlayExternallyClick = onPlayExternallyClick,
                    onShuffleClick = onShuffleClick,
                    shuffleEnabled = shuffleEnabled,
                    onPrimaryPlayLongClick = onPrimaryPlayLongClick,
                    featuredAction = featuredAction,
                    downloadAction = downloadAction,
                    onSaveClick = onSaveClick,
                    onSaveLongClick = onSaveLongClick,
                    onWatchedClick = onWatchedClick,
                    onOpenAnimeTracking = onOpenAnimeTracking,
                    showManualPlayOption = showManualPlayOption,
                    preferredEpisodeSeasonNumber = preferredEpisodeSeasonNumber,
                    preferredEpisodeNumber = preferredEpisodeNumber,
                    hasProductionSection = hasProductionSection,
                    hasTrailersSection = hasTrailersSection,
                    hasEpisodes = hasEpisodes,
                    hasAdditionalInfoSection = hasAdditionalInfoSection,
                    hasCollectionSection = hasCollectionSection,
                    hasMoreLikeThisSection = hasMoreLikeThisSection,
                    shouldShowComments = shouldShowComments,
                    comments = comments,
                    isCommentsLoading = isCommentsLoading,
                    isCommentsLoadingMore = isCommentsLoadingMore,
                    commentsCurrentPage = commentsCurrentPage,
                    commentsPageCount = commentsPageCount,
                    commentsError = commentsError,
                    episodeImdbRatings = episodeImdbRatings,
                    episodeTmdbRatings = episodeTmdbRatings,
                    showNuvioRead = showNuvioRead,
                    descriptionOnlyOverview = descriptionOnlyOverview,
                    onRetryComments = onRetryComments,
                    onLoadMoreComments = onLoadMoreComments,
                    onCommentClick = onCommentClick,
                    onTrailerClick = onTrailerClick,
                    progressByVideoId = progressByVideoId,
                    watchedKeys = watchedKeys,
                    fullyWatchedSeriesKeys = fullyWatchedSeriesKeys,
                    blurUnwatchedEpisodes = blurUnwatchedEpisodes,
                    showEpisodeRatings = showEpisodeRatings,
                    onEpisodeClick = onEpisodeClick,
                    onEpisodeLongPress = onEpisodeLongPress,
                    onSeasonLongPress = onSeasonLongPress,
                    onOpenMeta = onOpenMeta,
                    onOpenMoreLikeThis = onOpenMoreLikeThis,
                    onCastClick = onCastClick,
                    onCompanyClick = onCompanyClick,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope,
                    selectedSeason = selectedSeason,
                    onSeasonSelected = onSeasonSelected,
                )
            }
        }
    }

    fun addLazyEpisodeItems(): Boolean {
        if (!hasEpisodes || settings.episodeCardStyle != MetaEpisodeCardStyle.List) return false
        val navigator = episodeNavigator ?: return false
        val state = listState ?: return false
        val emitted = detailEpisodeListItems(
            meta = meta,
            navigator = navigator,
            listState = state,
            firstItemIndex = leadingItemCount + emittedItemCount,
            horizontalPadding = contentHorizontalPadding,
            contentMaxWidth = contentMaxWidth,
            stickyBackground = stickyBackground,
            preferredSeasonNumber = preferredEpisodeSeasonNumber,
            progressByVideoId = progressByVideoId,
            watchedKeys = watchedKeys,
            episodeRatingsVisibility = settings.episodeRatingsVisibility,
            blurUnwatchedEpisodes = blurUnwatchedEpisodes,
            onEpisodeClick = onEpisodeClick,
            onEpisodeLongPress = onEpisodeLongPress,
            onSeasonLongPress = onSeasonLongPress,
            episodeDownloadFor = episodeDownloadFor,
        )
        emittedItemCount += emitted
        return emitted > 0
    }

    if (!settings.tabLayout) {
        enabledItems
            .filter { sectionHasContent(it.key) }
            .forEach { section ->
                if (section.key == MetaScreenSectionKey.EPISODES && addLazyEpisodeItems()) return@forEach
                addSectionItem(
                    key = "detail-section-${section.key.name}",
                    sectionItems = listOf(section),
                    forceTabLayout = false,
                )
            }
        return
    }

    val processedGroups = mutableSetOf<Int>()
    enabledItems.forEach { section ->
        val groupId = section.tabGroupForRendering(settings.episodeCardStyle)
        if (groupId == null) {
            if (section.key == MetaScreenSectionKey.EPISODES && sectionHasContent(section.key) && addLazyEpisodeItems()) {
            } else if (sectionHasContent(section.key)) {
                addSectionItem(
                    key = "detail-section-${section.key.name}",
                    sectionItems = listOf(section),
                    forceTabLayout = true,
                )
            }
        } else if (groupId !in processedGroups) {
            processedGroups.add(groupId)
            val groupMembers = enabledItems.filter { item ->
                item.tabGroupForRendering(settings.episodeCardStyle) == groupId && sectionHasContent(item.key)
            }
            if (groupMembers.isNotEmpty()) {
                addSectionItem(
                    key = "detail-section-group-$groupId",
                    sectionItems = groupMembers,
                    forceTabLayout = groupMembers.size > 1,
                )
            }
        }
    }
}

@Composable
private fun DetailSectionContainer(
    horizontalPadding: Dp,
    contentMaxWidth: Dp,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding)
            .padding(bottom = 20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (contentMaxWidth == Dp.Unspecified) {
                        Modifier
                    } else {
                        Modifier.widthIn(max = contentMaxWidth)
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            CompositionLocalProvider(LocalVerticalBarPadding provides PaddingValues(), content)
        }
    }
}

private fun metaSectionHasContent(
    key: MetaScreenSectionKey,
    meta: MetaDetails,
    hasProductionSection: Boolean,
    hasTrailersSection: Boolean,
    hasEpisodes: Boolean,
    hasAdditionalInfoSection: Boolean,
    hasCollectionSection: Boolean,
    hasMoreLikeThisSection: Boolean,
    shouldShowComments: Boolean,
    comments: List<TraktCommentReview>,
    isCommentsLoading: Boolean,
    commentsError: String?,
): Boolean =
    when (key) {
        MetaScreenSectionKey.ACTIONS -> true
        MetaScreenSectionKey.OVERVIEW -> true
        MetaScreenSectionKey.PRODUCTION -> hasProductionSection
        MetaScreenSectionKey.CAST -> meta.cast.isNotEmpty()
        MetaScreenSectionKey.COMMENTS -> shouldShowComments && (isCommentsLoading || comments.isNotEmpty() || !commentsError.isNullOrBlank())
        MetaScreenSectionKey.TRAILERS -> hasTrailersSection
        MetaScreenSectionKey.EPISODES -> hasEpisodes
        MetaScreenSectionKey.DETAILS -> hasAdditionalInfoSection
        MetaScreenSectionKey.COLLECTION -> !hasEpisodes && hasCollectionSection
        MetaScreenSectionKey.MORE_LIKE_THIS -> hasMoreLikeThisSection
    }

internal fun detailActionSectionLazyListIndex(
    settings: MetaScreenSettingsUiState,
    sectionHasContent: (MetaScreenSectionKey) -> Boolean,
): Int? {
    val enabledItems = settings.items.filter { it.enabled }
    var lazyListIndex = 1 // The hero is always the first item.

    if (!settings.tabLayout) {
        enabledItems.forEach { section ->
            if (sectionHasContent(section.key)) {
                if (section.key == MetaScreenSectionKey.ACTIONS) return lazyListIndex
                lazyListIndex++
            }
        }
        return null
    }

    val processedGroups = mutableSetOf<Int>()
    enabledItems.forEach { section ->
        val groupId = section.tabGroup
        if (groupId == null) {
            if (sectionHasContent(section.key)) {
                if (section.key == MetaScreenSectionKey.ACTIONS) return lazyListIndex
                lazyListIndex++
            }
        } else if (groupId !in processedGroups) {
            processedGroups.add(groupId)
            val groupMembers = enabledItems.filter { item ->
                item.tabGroup == groupId && sectionHasContent(item.key)
            }
            if (groupMembers.isNotEmpty()) {
                if (groupMembers.any { it.key == MetaScreenSectionKey.ACTIONS }) return lazyListIndex
                lazyListIndex++
            }
        }
    }
    return null
}

@Composable
@OptIn(ExperimentalSharedTransitionApi::class)
private fun ConfiguredMetaSections(
    settings: MetaScreenSettingsUiState,
    isMdbListActive: Boolean,
    meta: MetaDetails,
    episodeDownloadFor: (MetaVideo) -> DownloadItem?,
    isTablet: Boolean,
    playButtonLabel: String,
    playFocusRequester: FocusRequester,
    isSaved: Boolean,
    isWatched: Boolean,
    onPrimaryPlayClick: () -> Unit,
    onDownloadClick: (() -> Unit)?,
    onPlayFromStartClick: (() -> Unit)?,
    onPlayExternallyClick: (() -> Unit)?,
    onShuffleClick: (() -> Unit)?,
    shuffleEnabled: Boolean,
    onPrimaryPlayLongClick: (() -> Unit)?,
    featuredAction: DetailSecondaryAction? = null,
    downloadAction: DetailSecondaryAction? = null,
    onSaveClick: () -> Unit,
    onSaveLongClick: (() -> Unit)?,
    onWatchedClick: () -> Unit,
    onOpenAnimeTracking: (() -> Unit)?,
    showManualPlayOption: Boolean,
    preferredEpisodeSeasonNumber: Int?,
    preferredEpisodeNumber: Int?,
    hasProductionSection: Boolean,
    hasTrailersSection: Boolean,
    hasEpisodes: Boolean,
    hasAdditionalInfoSection: Boolean,
    hasCollectionSection: Boolean,
    hasMoreLikeThisSection: Boolean,
    shouldShowComments: Boolean,
    comments: List<TraktCommentReview>,
    isCommentsLoading: Boolean,
    isCommentsLoadingMore: Boolean,
    commentsCurrentPage: Int,
    commentsPageCount: Int,
    commentsError: String?,
    episodeImdbRatings: Map<Pair<Int, Int>, Double>,
    episodeTmdbRatings: Map<Pair<Int, Int>, Double>,
    showNuvioRead: Boolean,
    descriptionOnlyOverview: Boolean,
    onRetryComments: () -> Unit,
    onLoadMoreComments: () -> Unit,
    onCommentClick: (TraktCommentReview) -> Unit,
    onTrailerClick: (MetaTrailer) -> Unit,
    progressByVideoId: Map<String, WatchProgressEntry>,
    watchedKeys: Set<String>,
    fullyWatchedSeriesKeys: Set<String> = emptySet(),
    blurUnwatchedEpisodes: Boolean,
    showEpisodeRatings: Boolean,
    onEpisodeClick: (MetaVideo) -> Unit,
    onEpisodeLongPress: (MetaVideo) -> Unit,
    onSeasonLongPress: (Int) -> Unit,
    onOpenMeta: ((MetaPreview) -> Unit)?,
    onOpenMoreLikeThis: ((MetaDetails) -> Unit)?,
    onCastClick: ((MetaPerson, String?) -> Unit)?,
    onCompanyClick: ((MetaCompany, String) -> Unit)?,
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?,
    selectedSeason: Int?,
    onSeasonSelected: (Int) -> Unit,
) {
    val enabledItems = settings.items.filter { it.enabled }

    val sectionHasContent: (MetaScreenSectionKey) -> Boolean = { key ->
        when (key) {
            MetaScreenSectionKey.ACTIONS -> true
            MetaScreenSectionKey.OVERVIEW -> true
            MetaScreenSectionKey.PRODUCTION -> hasProductionSection
            MetaScreenSectionKey.CAST -> meta.cast.isNotEmpty()
            MetaScreenSectionKey.COMMENTS -> shouldShowComments && (isCommentsLoading || comments.isNotEmpty() || !commentsError.isNullOrBlank())
            MetaScreenSectionKey.TRAILERS -> hasTrailersSection
            MetaScreenSectionKey.EPISODES -> hasEpisodes
            MetaScreenSectionKey.DETAILS -> hasAdditionalInfoSection
            MetaScreenSectionKey.COLLECTION -> !hasEpisodes && hasCollectionSection
            MetaScreenSectionKey.MORE_LIKE_THIS -> hasMoreLikeThisSection
        }
    }

    @Composable
    fun RenderSection(key: MetaScreenSectionKey, showHeader: Boolean = true) {
        when (key) {
            MetaScreenSectionKey.ACTIONS -> {
                DetailActions(
                    playLabel = playButtonLabel,
                    playFocusRequester = playFocusRequester,
                    downloadAction = downloadAction,
                    playSideAction = featuredAction,
                    featuredAction = onOpenAnimeTracking
                        ?.takeIf { settings.showAnimeTrackingPencil }
                        ?.let { onOpen ->
                            DetailSecondaryAction(
                                label = stringResource(Res.string.anime_tracking_action),
                                icon = Icons.Default.Edit,
                                onClick = onOpen,
                            )
                        },
                    secondaryActions = listOf(
                        DetailSecondaryAction(
                            label = if (isWatched) {
                                stringResource(Res.string.hero_mark_unwatched)
                            } else {
                                stringResource(Res.string.hero_mark_watched)
                            },
                            icon = if (isWatched) {
                                Icons.Default.CheckCircle
                            } else {
                                Icons.Default.CheckCircleOutline
                            },
                            isActive = isWatched,
                            onClick = onWatchedClick,
                        ),
                        DetailSecondaryAction(
                            label = stringResource(Res.string.details_nuvio_library_action),
                            icon = if (isSaved) {
                                Icons.Default.Check
                            } else {
                                Icons.Default.Add
                            },
                            isActive = isSaved,
                            onClick = onSaveClick,
                            onLongClick = onSaveLongClick,
                        ),
                    ),
                    isTablet = isTablet,
                    shuffleEnabled = shuffleEnabled,
                    onPlayClick = onPrimaryPlayClick,
                    onDownloadClick = onDownloadClick,
                    onPlayLongClick = if (showManualPlayOption) onPrimaryPlayLongClick else null,
                    onShuffleClick = onShuffleClick,
                    onWatchedClick = onWatchedClick,
                    onSaveClick = onSaveClick,
                    onSaveLongClick = onSaveLongClick,
                    onPlayFromStartClick = onPlayFromStartClick,
                    onPlayExternallyClick = onPlayExternallyClick,
                    iconActionRow = settings.iconActionRow,
                    userRating = userRating,
                    onRateClick = onRateClick,
                )
            }
            MetaScreenSectionKey.OVERVIEW -> {
                DetailMetaInfo(
                    meta = meta,
                    episodeImdbRatings = episodeImdbRatings,
                    episodeTmdbRatings = episodeTmdbRatings,
                    showNuvioRead = showNuvioRead,
                    descriptionOnly = descriptionOnlyOverview,
                )
            }
            MetaScreenSectionKey.PRODUCTION -> {
                if (hasProductionSection) {
                    DetailProductionSection(meta = meta, showHeader = showHeader, onCompanyClick = onCompanyClick)
                }
            }
            MetaScreenSectionKey.CAST -> {
                DetailCastSection(
                    cast = meta.cast,
                    showHeader = showHeader,
                    onCastClick = onCastClick,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope,
                )
            }
            MetaScreenSectionKey.COMMENTS -> {
                if (shouldShowComments && (isCommentsLoading || comments.isNotEmpty() || !commentsError.isNullOrBlank())) {
                    DetailCommentsSection(
                        comments = comments,
                        isLoading = isCommentsLoading,
                        isLoadingMore = isCommentsLoadingMore,
                        canLoadMore = commentsCurrentPage < commentsPageCount,
                        error = commentsError,
                        onRetry = onRetryComments,
                        onLoadMore = onLoadMoreComments,
                        onCommentClick = onCommentClick,
                        showHeader = showHeader,
                    )
                }
            }
            MetaScreenSectionKey.TRAILERS -> {
                if (hasTrailersSection) {
                    DetailTrailersSection(trailers = meta.trailers, onTrailerClick = onTrailerClick, showHeader = showHeader)
                }
            }
            MetaScreenSectionKey.EPISODES -> {
                if (hasEpisodes) {
                    DetailSeriesContent(
                        meta = meta,
                        episodeDownloadFor = episodeDownloadFor,
                        showHeader = showHeader,
                        preferredSeasonNumber = preferredEpisodeSeasonNumber,
                        preferredEpisodeNumber = preferredEpisodeNumber,
                        episodeCardStyle = settings.episodeCardStyle,
                        progressByVideoId = progressByVideoId,
                        watchedKeys = watchedKeys,
                        episodeRatings = emptyMap(),
                        episodeRatingsVisibility = settings.episodeRatingsVisibility,
                        blurUnwatchedEpisodes = blurUnwatchedEpisodes,
                        showEpisodeRatings = showEpisodeRatings,
                        onEpisodeClick = onEpisodeClick,
                        onEpisodeLongPress = onEpisodeLongPress,
                        onSeasonLongPress = onSeasonLongPress,
                        selectedSeason = selectedSeason,
                        onSeasonSelected = onSeasonSelected,
                    )
                }
            }
            MetaScreenSectionKey.DETAILS -> {
                if (hasAdditionalInfoSection) {
                    DetailAdditionalInfoSection(meta = meta, showHeader = showHeader)
                }
            }
            MetaScreenSectionKey.COLLECTION -> {
                if (!hasEpisodes && hasCollectionSection) {
                    DetailPosterRailSection(
                        title = meta.collectionName.orEmpty(),
                        items = meta.collectionItems,
                        watchedKeys = watchedKeys,
                        fullyWatchedSeriesKeys = fullyWatchedSeriesKeys,
                        showHeader = showHeader,
                        onPosterClick = onOpenMeta,
                    )
                }
            }
            MetaScreenSectionKey.MORE_LIKE_THIS -> {
                if (hasMoreLikeThisSection) {
                    val sourceLabel = when (meta.moreLikeThisSource) {
                        MoreLikeThisSource.TMDB -> stringResource(Res.string.detail_more_like_this_powered_by_tmdb)
                        MoreLikeThisSource.TRAKT -> stringResource(Res.string.detail_more_like_this_powered_by_trakt)
                        MoreLikeThisSource.SIMKL -> stringResource(Res.string.detail_more_like_this_powered_by_simkl)
                        null -> null
                    }
                    DetailPosterRailSection(
                        title = stringResource(Res.string.details_more_like_this),
                        items = meta.moreLikeThis,
                        watchedKeys = watchedKeys,
                        fullyWatchedSeriesKeys = fullyWatchedSeriesKeys,
                        showHeader = showHeader,
                        sourceLabel = sourceLabel,
                        onViewAllClick = onOpenMoreLikeThis
                            ?.takeIf { meta.moreLikeThisHasMore && meta.moreLikeThisSource != null }
                            ?.let { open -> { open(meta) } },
                        onPosterClick = onOpenMeta,
                    )
                }
            }
        }
    }

    if (!settings.tabLayout) {
        enabledItems.forEach { section -> RenderSection(section.key) }
    } else {
        val processedGroups = mutableSetOf<Int>()

        enabledItems.forEach { section ->
            val groupId = section.tabGroup
            if (groupId == null) {
                RenderSection(section.key)
            } else if (groupId !in processedGroups) {
                processedGroups.add(groupId)
                val groupMembers = enabledItems
                    .filter { it.tabGroup == groupId && sectionHasContent(it.key) }
                if (groupMembers.isEmpty()) return@forEach
                if (groupMembers.size == 1) {
                    RenderSection(groupMembers.first().key)
                } else {
                    TabbedSectionGroup(
                        tabs = groupMembers.map { it.key to it.title },
                    ) { activeKey ->
                        RenderSection(activeKey, showHeader = false)
                    }
                }
            }
        }
    }
}

@Composable
private fun TabbedSectionGroup(
    tabs: List<Pair<MetaScreenSectionKey, String>>,
    content: @Composable (MetaScreenSectionKey) -> Unit,
) {
    if (tabs.isEmpty()) return

    var selectedIndex by remember { mutableIntStateOf(0) }
    val clampedIndex = selectedIndex.coerceIn(0, tabs.lastIndex)
    if (clampedIndex != selectedIndex) selectedIndex = clampedIndex

    val headerColor = MaterialTheme.colorScheme.onBackground

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val titleSize = if (maxWidth >= 720.dp) 22.sp else 20.sp
            val headerStyle = MaterialTheme.typography.titleLarge.copy(
                fontSize = titleSize,
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                tabs.forEachIndexed { index, (_, title) ->
                    if (index > 0) {
                        Text(
                            text = "|",
                            style = headerStyle,
                            color = headerColor.copy(alpha = 0.45f),
                            modifier = Modifier.padding(horizontal = 10.dp),
                        )
                    }

                    Text(
                        text = title,
                        style = headerStyle,
                        color = if (index == selectedIndex) {
                            headerColor
                        } else {
                            headerColor.copy(alpha = 0.55f)
                        },
                        maxLines = 1,
                        modifier = Modifier
                            .nuvioKeyboardFocusIndicator(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) { selectedIndex = index },
                    )
                }
            }
        }

        Crossfade(
            targetState = tabs[selectedIndex].first,
            animationSpec = tween(durationMillis = 200),
            label = "tabbedSectionCrossfade",
        ) { activeKey ->
            content(activeKey)
        }
    }
}

private const val DETAIL_HERO_BLEND_FADE_IN_FRACTION = 0.42f
private val DETAIL_HERO_BLEND_FADE_OUT = 200.dp
private const val DETAIL_HERO_BLEND_PEAK_ALPHA = 0.96f

/**
 * A single smooth bump: transparent at the top, [DETAIL_HERO_BLEND_PEAK_ALPHA] at [peakFraction]
 * (the hero's bottom edge) and transparent again at the bottom, using smootherstep easing on both
 * sides so neither end of the band shows an edge.
 */
private fun detailHeroBlendBrush(color: Color, peakFraction: Float): Brush {
    val peak = peakFraction.coerceIn(0.01f, 0.99f)
    val steps = 12
    fun ease(t: Float) = t * t * t * (t * (t * 6f - 15f) + 10f)
    val rampIn = Array(steps) { index ->
        val t = index / steps.toFloat()
        (t * peak) to color.copy(alpha = DETAIL_HERO_BLEND_PEAK_ALPHA * ease(t))
    }
    val rampOut = Array(steps + 1) { index ->
        val t = index / steps.toFloat()
        (peak + t * (1f - peak)) to color.copy(alpha = DETAIL_HERO_BLEND_PEAK_ALPHA * (1f - ease(t)))
    }
    return Brush.verticalGradient(colorStops = rampIn + rampOut)
}

private val tabletHeroSectionKeys = setOf(
    MetaScreenSectionKey.ACTIONS,
    MetaScreenSectionKey.OVERVIEW,
)

private fun detailTabletContentMaxWidth(maxWidth: Dp, isTablet: Boolean): Dp =
    if (!isTablet) {
        maxWidth
    } else {
        (maxWidth * 0.6f).coerceIn(520.dp, 680.dp)
    }

private fun dominantBackdropBlendColor(dominantColor: Color, backgroundColor: Color): Color =
    backgroundColor.blendTowards(dominantColor, fraction = 0.42f)

internal fun detailProductionSectionVisible(
    hasProductionData: Boolean,
    cinematicDetailHeaderEnabled: Boolean,
): Boolean = hasProductionData && !cinematicDetailHeaderEnabled

internal fun shouldRenderDetailBackdropBridge(
    backgroundMode: MetaScreenBackgroundMode,
    cinematicDetailHeaderEnabled: Boolean,
): Boolean = backgroundMode.usesBackdropBackground && !cinematicDetailHeaderEnabled

internal fun shouldUseDescriptionOnlyDetailOverview(cinematicDetailHeaderEnabled: Boolean): Boolean =
    cinematicDetailHeaderEnabled

internal fun shouldEnableDetailNuvioRead(
    nuvioReadEnabled: Boolean,
    cinematicDetailHeaderEnabled: Boolean,
): Boolean = nuvioReadEnabled && !cinematicDetailHeaderEnabled

private fun Color.blendTowards(target: Color, fraction: Float): Color {
    val clamped = fraction.coerceIn(0f, 1f)
    return Color(
        red = red + (target.red - red) * clamped,
        green = green + (target.green - green) * clamped,
        blue = blue + (target.blue - blue) * clamped,
        alpha = alpha + (target.alpha - alpha) * clamped,
    )
}
