package com.nuvio.app.features.player

import com.nuvio.app.features.player.skip.selectActiveSkipInterval

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.nuvio.app.core.ui.AppSystemUiController
import com.nuvio.app.features.details.MetaDetailsRepository
import com.nuvio.app.features.p2p.P2pSettingsRepository
import com.nuvio.app.features.p2p.P2pStreamRequest
import com.nuvio.app.features.p2p.P2pStreamingEngine
import com.nuvio.app.features.p2p.P2pStreamingState
import com.nuvio.app.features.player.skip.NextEpisodeInfo
import com.nuvio.app.features.player.skip.PlayerNextEpisodeRules
import com.nuvio.app.features.player.skip.SkipIntroRepository
import com.nuvio.app.features.player.skip.categoryKey
import com.nuvio.app.core.network.NetworkCondition
import com.nuvio.app.core.network.NetworkStatusRepository
import com.nuvio.app.core.network.NetworkStatusUiState
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.withTimeoutOrNull
import com.nuvio.app.features.streams.BingeGroupCacheRepository
import com.nuvio.app.features.streams.StreamLinkCacheRepository
import com.nuvio.app.features.debrid.DirectDebridPlayableResult
import com.nuvio.app.features.debrid.DirectDebridPlaybackResolver
import com.nuvio.app.features.streams.StreamItem
import com.nuvio.app.features.streams.hasLikelyExpiringPlaybackCredentials
import com.nuvio.app.features.tracking.TrackingScrobbleAction
import com.nuvio.app.features.watchprogress.WatchProgressRepository
import com.nuvio.app.features.watchprogress.buildPlaybackVideoId
import com.nuvio.app.features.watching.application.WatchingState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import nuvio.composeapp.generated.resources.*
import org.jetbrains.compose.resources.getString

private const val SKIP_INTERVAL_RESOLVE_TIMEOUT_MS = 4_000L

@Composable
internal fun PlayerScreenRuntime.BindPlayerRuntimeEffects() {
    val networkStatus by NetworkStatusRepository.uiState.collectAsStateWithLifecycle()
    val networkAvailable = networkStatus.condition == NetworkCondition.Online
    LaunchedEffect(Unit) {
        NetworkStatusRepository.ensureStarted()
    }
    BindAutoSyncRuntimeEffects()
    val currentFeedback = liveGestureFeedback ?: gestureFeedback
    LaunchedEffect(currentFeedback) {
        if (currentFeedback != null) {
            renderedGestureFeedback = currentFeedback
        }
    }

    LaunchedEffect(playerController, playerSettingsUiState.volumeBoostPercent) {
        playerController?.setVolumeBoost(playerSettingsUiState.volumeBoostPercent / 100f)
    }

    LaunchedEffect(parentMetaType, parentMetaId) {
        playerMeta = MetaDetailsRepository.peek(parentMetaType, parentMetaId)
        playerMetaVideos = playerMeta?.videos.orEmpty()
        if (playerMetaVideos.isEmpty()) {
            MetaDetailsRepository.fetch(parentMetaType, parentMetaId)?.let { meta ->
                playerMeta = meta
                playerMetaVideos = meta.videos
            }
        }
    }

    LaunchedEffect(metaUiState.meta, parentMetaType, parentMetaId) {
        val currentMeta = metaUiState.meta ?: return@LaunchedEffect
        if (currentMeta.type == parentMetaType && currentMeta.id == parentMetaId) {
            playerMeta = currentMeta
            playerMetaVideos = currentMeta.videos
        }
    }

    LaunchedEffect(currentStreamBingeGroup, currentStreamFilename, currentStreamInfoHash, parentMetaId) {
        val bg = currentStreamBingeGroup
        if (bg != null && parentMetaId.isNotBlank()) {
            BingeGroupCacheRepository.save(parentMetaId, bg, currentStreamFilename, currentStreamInfoHash)
        }
    }

    LaunchedEffect(activePlaybackKey, activeSourceUrl, activeSourceAudioUrl, activeSourceHeaders, activeSourceResponseHeaders) {
        errorMessage = null
        playerController = null
        playerControllerSourceUrl = null
        playbackSnapshot = PlayerPlaybackSnapshot()
        playbackSnapshotKey = null
        cancelNextEpisodeAutoPlay()
        isScrubbingTimeline = false
        scrubbingPositionMs = null
        liveGestureFeedback = null
        renderedGestureFeedback = null
        lockedOverlayVisible = false
        credentialRefreshJob?.cancel()
        credentialRefreshJob = null
        credentialRefreshAttemptedSourceUrl = null
        initialLoadCompleted = false
        lastProgressPersistEpochMs = 0L
        lastProgressRemoteSyncEpochMs = 0L
        lastProgressRemoteSyncPositionMs = 0L
        previousIsPlaying = false
        pendingSeekScrobbleRestart = false
        seekProgressSyncJob?.cancel()
        seekProgressSyncJob = null
        accumulatedSeekResetJob?.cancel()
        accumulatedSeekResetJob = null
        accumulatedSeekState = null
        speedBoostRestoreSpeed = null
        trackPreferenceRestoreApplied = false
        preferredAudioSelectionApplied = false
        appliedAudioPreferences = null
        preferredSubtitleSelectionApplied = false
        manualSubtitleSelectionLocked = false
        showSourcesPanel = false
        showEpisodesPanel = false
        showQualityPanel = false
        showLiveTvChannelsPanel = false
        chaptersLoadedForSourceUrl = null
        clearChapterSkipState()
        episodeStreamsPanelState = EpisodeStreamsPanelState()
        preloadedNextEpisodeVideoId = null
        nextEpisodePreparationJob?.cancel()
        nextEpisodePreparationJob = null
        preloadedNextEpisodeStream = null
        pendingNextEpisodeLaunch = false
        pendingNextEpisodeLaunchWithCountdown = false
        nextEpisodeAutoPlayReady = false
        PlayerStreamsRepository.clearEpisodeStreams()
        resetAddonSubtitlesIfSourceChanged()
        WatchProgressRepository.ensureLoaded()

        // A source that does not finish loading within the configured time counts as failed when
        // falling back to the next source. It has an effect only where that fallback applies.
        val startupTimeoutSeconds = playerSettingsUiState.streamAutoPlayFallbackStartupTimeoutSeconds
        delay(startupTimeoutSeconds * 1_000L)
        if (!initialLoadCompleted && errorMessage == null) {
            tryFallbackToNextSourceAfterError(
                getString(Res.string.player_error_source_startup_timeout, startupTimeoutSeconds),
            )
        }
    }

    LaunchedEffect(activeSourceUrl, activeSourceAudioUrl, activeSourceHeaders, activeStreamType, activeTorrentInfoHash) {
        val resolvingSourceUrl = activeSourceUrl
        val resolvingAudioUrl = activeSourceAudioUrl
        val resolvingHeaders = activeSourceHeaders
        val resolvingStreamType = activeStreamType
        val resolvingTorrentInfoHash = activeTorrentInfoHash
        val shouldInspectHls = resolvingTorrentInfoHash == null &&
            resolvingAudioUrl == null &&
            (resolvingStreamType.equals("hls", ignoreCase = true) || resolvingSourceUrl.contains(".m3u8", ignoreCase = true))

        selectedPlayerQualityId = null
        showQualityPanel = false
        activePlaybackSourceUrl = if (shouldInspectHls) null else resolvingSourceUrl
        playerQualityState = PlayerQualitySelectionState(
            isLoading = shouldInspectHls,
            sourceUrl = resolvingSourceUrl,
        )

        if (!shouldInspectHls) {
            return@LaunchedEffect
        }

        val resolved = runCatching {
            PlayerQualityResolver.resolve(
                sourceUrl = resolvingSourceUrl,
                requestHeaders = resolvingHeaders,
                forceHls = resolvingStreamType.equals("hls", ignoreCase = true),
            )
        }.getOrElse { error ->
            PlayerQualitySelectionState(
                sourceUrl = resolvingSourceUrl,
                errorMessage = error.message ?: "Unable to inspect HLS quality variants.",
            )
        }

        if (
            activeSourceUrl != resolvingSourceUrl ||
            activeSourceAudioUrl != resolvingAudioUrl ||
            activeStreamType != resolvingStreamType ||
            activeTorrentInfoHash != resolvingTorrentInfoHash
        ) {
            return@LaunchedEffect
        }

        playerQualityState = resolved
        activePlaybackSourceUrl = resolved.playbackUrlFor(null) ?: resolvingSourceUrl
    }

    LaunchedEffect(
        activeTorrentInfoHash,
        activeTorrentFileIdx,
        activeTorrentFilename,
        activeTorrentTrackers,
        p2pSettingsUiState.p2pEnabled,
    ) {
        val infoHash = activeTorrentInfoHash
        if (infoHash == null) {
            p2pResolvedSourceUrl = null
            P2pStreamingEngine.stopStream()
            return@LaunchedEffect
        }
        if (!P2pSettingsRepository.isVisible || !p2pSettingsUiState.p2pEnabled) {
            p2pResolvedSourceUrl = null
            P2pStreamingEngine.stopStream()
            return@LaunchedEffect
        }

        p2pResolvedSourceUrl = null
        val requestedFileIdx = activeTorrentFileIdx
        val requestedFilename = activeTorrentFilename
        val requestedTrackers = activeTorrentTrackers
        errorMessage = null
        playerController = null
        playerControllerSourceUrl = null
        playbackSnapshot = PlayerPlaybackSnapshot()
        initialLoadCompleted = false

        try {
            val localUrl = P2pStreamingEngine.startStream(
                P2pStreamRequest(
                    infoHash = infoHash,
                    fileIdx = requestedFileIdx,
                    filename = requestedFilename,
                    trackers = requestedTrackers,
                ),
            )
            if (activeTorrentInfoHash == infoHash && activeTorrentFileIdx == requestedFileIdx) {
                activeSourceAudioUrl = null
                activeSourceHeaders = emptyMap()
                activeSourceResponseHeaders = emptyMap()
                p2pResolvedSourceUrl = localUrl
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            errorMessage = getString(
                Res.string.player_error_failed_start_torrent,
                error.message ?: genericUnknownLabel,
            )
            controlsVisible = !playerControlsLocked
            initialLoadCompleted = true
        }
    }

    LaunchedEffect(p2pStreamingState, activeTorrentInfoHash) {
        val state = p2pStreamingState
        if (activeTorrentInfoHash != null && state is P2pStreamingState.Error) {
            p2pResolvedSourceUrl = null
            playerController = null
            playerControllerSourceUrl = null
            playbackSnapshot = PlayerPlaybackSnapshot()
            initialLoadCompleted = true
            errorMessage = getString(Res.string.player_error_torrent, state.message)
            controlsVisible = !playerControlsLocked
        }
    }

    LaunchedEffect(playbackSession.videoId) {
        subtitleDelayMs = PlayerTrackPreferenceStorage.loadSubtitleDelayMs(playbackSession.videoId) ?: 0
        subtitleAutoSyncState = SubtitleAutoSyncUiState()
    }

    LaunchedEffect(activeVideoId) {
        // External add-on tracks are tied to one episode and must not leak into the next
        // one, even when a provider reuses the same playback URL.
        if (useCustomSubtitles) playerController?.clearExternalSubtitle()
        selectedAddonSubtitleId = null
        selectedSubtitleIndex = -1
        useCustomSubtitles = false
        manualSubtitleSelectionLocked = false
        autoAddonFallbackPending = false
    }

    LaunchedEffect(playerController, subtitleDelayMs) {
        playerController?.setSubtitleDelayMs(subtitleDelayMs)
    }

    BindAudioDelayEffects()
    BindAudioAmplificationEffects()

    LaunchedEffect(selectedAddonSubtitleId, useCustomSubtitles, activeSourceUrl) {
        subtitleAutoSyncState = SubtitleAutoSyncUiState()
    }

    LaunchedEffect(playerController, subtitleStyle) {
        playerController?.applySubtitleStyle(subtitleStyle)
    }

    // iOS recognises hardware keys in the mpv view controller, because its UIKit view can hold
    // first responder at moments when the Compose surface does not. It only reports which
    // shortcut was pressed — the action itself still runs through the shared runtime dispatcher.
    DisposableEffect(playerController) {
        playerController?.setKeyboardShortcutHandler(::handleKeyboardShortcut)
        onDispose { playerController?.setKeyboardShortcutHandler(null) }
    }

    val platformKeyboardShortcutsEnabled = !isAnyOverlayVisible && !playerControlsLocked
    LaunchedEffect(playerController, platformKeyboardShortcutsEnabled) {
        playerController?.setKeyboardShortcutsEnabled(platformKeyboardShortcutsEnabled)
    }

    val subtitlePreferenceKey = listOf(
        playerSettingsUiState.preferredSubtitleLanguage,
        playerSettingsUiState.secondaryPreferredSubtitleLanguage.orEmpty(),
        subtitleStyle.useForcedSubtitles,
    ).joinToString("|")
    var lastSubtitlePreferenceKey by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(
        playerController,
        subtitlePreferenceKey,
        preferredSubtitleSelectionApplied,
        selectedSubtitleIndex,
        selectedAddonSubtitleId,
        useCustomSubtitles,
    ) {
        val controller = playerController ?: return@LaunchedEffect
        val preferenceChanged = lastSubtitlePreferenceKey != null &&
            lastSubtitlePreferenceKey != subtitlePreferenceKey
        lastSubtitlePreferenceKey = subtitlePreferenceKey

        controller.applySubtitlePreferences(
            preferredLanguage = playerSettingsUiState.preferredSubtitleLanguage,
            secondaryPreferredLanguage = playerSettingsUiState.secondaryPreferredSubtitleLanguage,
            useForcedSubtitles = subtitleStyle.useForcedSubtitles,
            autoSelectionApplied = preferredSubtitleSelectionApplied,
            hasActiveSubtitle = selectedSubtitleIndex >= 0 || selectedAddonSubtitleId != null,
            useCustomSubtitles = useCustomSubtitles,
        )

        if (preferenceChanged) {
            preferredSubtitleSelectionApplied = false
            refreshTracks()
        }
    }

    LaunchedEffect(
        playerController,
        playerSettingsUiState.preferredSubtitleLanguage,
        playerSettingsUiState.secondaryPreferredSubtitleLanguage,
        subtitleStyle.useForcedSubtitles,
    ) {
        val controller = playerController ?: return@LaunchedEffect
        val preferenceKey = listOf(
            playerSettingsUiState.preferredSubtitleLanguage,
            playerSettingsUiState.secondaryPreferredSubtitleLanguage.orEmpty(),
            subtitleStyle.useForcedSubtitles,
        ).joinToString("|")
        val preferenceChanged = lastAppliedSubtitlePreferenceKey != null &&
            lastAppliedSubtitlePreferenceKey != preferenceKey
        lastAppliedSubtitlePreferenceKey = preferenceKey
        controller.applySubtitlePreferences(
            preferredLanguage = playerSettingsUiState.preferredSubtitleLanguage,
            secondaryPreferredLanguage = playerSettingsUiState.secondaryPreferredSubtitleLanguage,
            useForcedSubtitles = subtitleStyle.useForcedSubtitles,
            autoSelectionApplied = preferredSubtitleSelectionApplied,
            hasActiveSubtitle = manualSubtitleSelectionLocked &&
                (selectedSubtitleIndex >= 0 || selectedAddonSubtitleId != null),
            useCustomSubtitles = useCustomSubtitles,
        )
        if (preferenceChanged && !manualSubtitleSelectionLocked) {
            preferredSubtitleSelectionApplied = false
            refreshTracks()
        }
    }

    LaunchedEffect(
        playerController,
        playerControllerSourceUrl,
        activeSourceUrl,
        activePlaybackSourceUrl,
        p2pResolvedSourceUrl,
        title,
        activeStreamTitle,
        activeLogo,
        activeSeasonNumber,
        activeEpisodeNumber,
        activeEpisodeTitle,
        poster,
        background,
    ) {
        val controller = playerController ?: return@LaunchedEffect
        if (playerControllerSourceUrl != currentPlaybackSurfaceSourceUrl) return@LaunchedEffect
        controller.updateNowPlayingMetadata(buildNowPlayingInfo())
    }

    LaunchedEffect(
        activeSourceUrl,
        p2pResolvedSourceUrl,
        addonSubtitleFetchKey,
        playerSettingsUiState.addonSubtitleStartupMode,
        playerController,
        playerControllerSourceUrl,
        activeExternalSubtitles,
        networkAvailable,
    ) {
        if (activeSourceUrl.startsWith("file:") && externalSubtitles.isNotEmpty()) return@LaunchedEffect
        val fetchKey = addonSubtitleFetchKey ?: return@LaunchedEffect
        val playbackSurfaceUrl = currentPlaybackSurfaceSourceUrl
        val playerInitialized = playerController != null && (
            playerControllerSourceUrl == activeSourceUrl ||
                playerControllerSourceUrl == playbackSurfaceUrl
            )
        val canFetch = canAutomaticallyFetchAddonSubtitles(
            mode = playerSettingsUiState.addonSubtitleStartupMode,
            playerInitialized = playerInitialized,
        )
        if (!canFetch) return@LaunchedEffect
        if (activeSourceUrl.startsWith("file:", ignoreCase = true) && activeExternalSubtitles.isNotEmpty()) {
            return@LaunchedEffect
        }
        if (autoFetchedAddonSubtitlesForKey == fetchKey) return@LaunchedEffect
        // Keep the key unset while offline so the effect retries when connectivity returns.
        if (!networkAvailable) return@LaunchedEffect
        autoFetchedAddonSubtitlesForKey = fetchKey
        fetchAddonSubtitlesForActiveItem()
    }

    LaunchedEffect(autoAddonFallbackPending, visibleAddonSubtitles, activeVideoId) {
        if (!autoAddonFallbackPending || useCustomSubtitles || manualSubtitleSelectionLocked) {
            return@LaunchedEffect
        }
        val selectedAudioTrack = audioTracks.firstOrNull { it.index == selectedAudioIndex }
            ?: audioTracks.firstOrNull { it.isSelected }
        val configuredTargets = preferredSubtitleTargetsForSettings(playerSettingsUiState)
        val targets = configuredTargets.ifEmpty {
            if (subtitleStyle.useForcedSubtitles) {
                listOfNotNull(selectedAudioTrack?.let(::selectedAudioLanguageTarget))
            } else {
                emptyList()
            }
        }
        val forcedOnly = subtitleStyle.useForcedSubtitles && selectedAudioTrack != null &&
            targets.firstOrNull()?.let { target ->
                audioMatchesSubtitleTargetForForced(selectedAudioTrack, target)
            } == true
        val currentEpisodeSubtitle = targets.firstNotNullOfOrNull { target ->
            visibleAddonSubtitles.firstOrNull { subtitle ->
                if (forcedOnly) {
                    addonSubtitleIsForced(subtitle) &&
                        addonSubtitleMatchesLanguage(subtitle, target) &&
                        addonSubtitleMatchesSelectedAudioLanguage(subtitle, selectedAudioTrack)
                } else {
                    !addonSubtitleIsForced(subtitle) && addonSubtitleMatchesLanguage(subtitle, target)
                }
            }
        } ?: return@LaunchedEffect

        // visibleAddonSubtitles preserves the installed add-on order. A matching add-on
        // is selected only after the built-in pass has failed for this episode.
        selectedAddonSubtitleId = currentEpisodeSubtitle.selectionKey
        selectedSubtitleIndex = -1
        useCustomSubtitles = true
        autoAddonFallbackPending = false
        if (!maybeAutoSyncPreferredSubtitleAtStart(currentEpisodeSubtitle)) {
            playerController?.setSubtitleUri(currentEpisodeSubtitle.url)
        }
    }

    LaunchedEffect(playbackSnapshot.isLoading, playerController) {
        if (!playbackSnapshot.isLoading && playerController != null) {
            refreshTracks()
        }
    }

    LaunchedEffect(addonSubtitles, isLoadingAddonSubtitles, playerController, activeVideoId) {
        if (isLoadingAddonSubtitles || playerController == null || manualSubtitleSelectionLocked) {
            return@LaunchedEffect
        }
        if (selectedAddonSubtitleId != null) return@LaunchedEffect
        val preference = PlayerTrackPreferenceStorage.load(parentMetaId) ?: return@LaunchedEffect
        if (preference.subtitleType != PersistedSubtitleSelectionType.ADDON) return@LaunchedEffect
        // Persisted add-on choices are resolved only after this episode's add-on list
        // settles; the stored URL is intentionally not reused across episodes.
        trackPreferenceRestoreApplied = false
        refreshTracks()
    }

    LaunchedEffect(
        playbackSnapshot.isLoading,
        playerController,
        playerControllerSourceUrl,
        activeSourceUrl,
        playerSettingsUiState.androidExoChaptersEnabled,
    ) {
        clearChaptersIfDisabled()
        if (!playbackSnapshot.isLoading) loadChaptersIfNeeded()
    }

    LaunchedEffect(playbackSnapshot.chapters, chapters, playerSettingsUiState.androidExoChaptersEnabled) {
        adoptSnapshotChaptersIfNeeded()
    }

    LaunchedEffect(
        playerController,
        playbackSnapshot.isLoading,
        preferredAudioSelectionApplied,
        preferredSubtitleSelectionApplied,
        trackPreferenceRestoreApplied,
        addonSubtitles,
        isLoadingAddonSubtitles,
    ) {
        if (playerController == null || playbackSnapshot.isLoading) {
            return@LaunchedEffect
        }
        if (trackPreferenceRestoreApplied && preferredAudioSelectionApplied && preferredSubtitleSelectionApplied) {
            return@LaunchedEffect
        }

        repeat(10) {
            refreshTracks()
            if (trackPreferenceRestoreApplied && preferredAudioSelectionApplied && preferredSubtitleSelectionApplied) {
                return@LaunchedEffect
            }
            delay(300)
        }
    }

    // A pending carry-over is re-applied as soon as the new engine exposes its tracks; poll
    // briefly, then give up.
    LaunchedEffect(playerController, pendingEngineSwitchTrackPreference != null) {
        if (playerController == null) return@LaunchedEffect
        if (pendingEngineSwitchTrackPreference == null) return@LaunchedEffect

        repeat(30) {
            refreshTracks()
            if (pendingEngineSwitchTrackPreference == null) return@LaunchedEffect
            delay(300)
        }

        val pending = pendingEngineSwitchTrackPreference ?: return@LaunchedEffect
        if (playerController?.playbackEngine == pending.targetEngine) {
            // The new engine never exposed a matching track: drop the carry-over.
            clearPendingEngineSwitchTrackPreference()
        }    }

    LaunchedEffect(
        playerController,
        playerControllerSourceUrl,
        activePlaybackSourceUrl,
        p2pResolvedSourceUrl,
        playbackSnapshot.isLoading,
        playbackSnapshot.durationMs,
        activeInitialPositionMs,
        activeInitialProgressFraction,
        initialSeekApplied,
    ) {
        val controller = playerController ?: return@LaunchedEffect
        if (playerControllerSourceUrl != currentPlaybackSurfaceSourceUrl) return@LaunchedEffect
        if (initialSeekApplied || playbackSnapshot.isLoading) return@LaunchedEffect
        if (isPartySynced && PartyRepository.playback.value != null) {
            initialSeekApplied = true
            return@LaunchedEffect
        }

        val progressFraction = activeInitialProgressFraction
            ?.takeIf { it > 0f }
            ?.coerceIn(0f, 1f)
        val targetPositionMs = when {
            activeInitialPositionMs > 0L -> activeInitialPositionMs
            progressFraction != null && playbackSnapshot.durationMs > 0L -> {
                (playbackSnapshot.durationMs.toDouble() * progressFraction.toDouble()).toLong()
            }
            progressFraction != null -> return@LaunchedEffect
            else -> 0L
        }
        if (targetPositionMs <= 0L) {
            initialSeekApplied = true
            return@LaunchedEffect
        }

        controller.seekTo(targetPositionMs)
        initialSeekApplied = true
    }

    BindPartyEffects()
    BindPlayerUiVisibilityEffects()
    BindPlayerMetadataAndSkipEffects(networkStatus)

    DisposableEffect(playbackSession.videoId, activeSourceUrl, activeSourceAudioUrl) {
        val effectVideoId = playbackSession.videoId
        val effectSourceUrl = activeSourceUrl
        val effectSourceAudioUrl = activeSourceAudioUrl
        onDispose {
            if (
                playbackSession.videoId == effectVideoId &&
                activeSourceUrl == effectSourceUrl &&
                activeSourceAudioUrl == effectSourceAudioUrl
            ) {
                flushWatchProgress()
            }
        }
    }

    DisposableEffect(Unit) {
        PlayerStreamsRepository.pauseSearchForPlayback()
        onDispose {
            args.launchId?.let { launchId ->
                PlayerLaunchStore.update(launchId) { currentLaunch(it) }
                // The player is gone: release the launch payload once the restore window has
                // passed (a configuration change recreates the composition and the player
                // re-reads its launch from the store).
                PlayerLaunchStore.markExited(launchId)
            }
            playerController?.clearNowPlayingInfo()
            P2pStreamingEngine.shutdown()
            cancelNextEpisodePreload()
            PlayerStreamsRepository.clearAll()
        }
    }
}

@Composable
private fun PlayerScreenRuntime.BindPlayerUiVisibilityEffects() {
    val statusBarVisiblePreference = nuvioEnhancedSettingsUiState.statusBarVisible
    DisposableEffect(statusBarVisiblePreference) {
        onDispose {
            AppSystemUiController.setStatusBarVisible(statusBarVisiblePreference)
        }
    }

    LaunchedEffect(
        statusBarVisiblePreference,
        controlsVisible,
        playerControlsLocked,
        playbackSnapshot.isPlaying,
        playbackSnapshot.isLoading,
        showParentalGuide,
        errorMessage,
    ) {
        if (!statusBarVisiblePreference) {
            AppSystemUiController.setStatusBarVisible(false)
            return@LaunchedEffect
        }

        val isActivelyWatching = playbackSnapshot.isPlaying &&
            !playbackSnapshot.isLoading &&
            errorMessage == null &&
            !showParentalGuide

        if (!isActivelyWatching) {
            AppSystemUiController.setStatusBarVisible(true)
            return@LaunchedEffect
        }

        if (!controlsVisible || playerControlsLocked) {
            AppSystemUiController.setStatusBarVisible(false)
            return@LaunchedEffect
        }

        AppSystemUiController.setStatusBarVisible(true)
        delay(2_000)
        AppSystemUiController.setStatusBarVisible(false)
    }

    LaunchedEffect(
        folded,
        controlsVisible,
        controlsActivityTick,
        isScrubbingTimeline,
        isTouchingTimeline,
        snapshotSignals.isPlaying,
        snapshotSignals.isLoading,
        showParentalGuide,
        showSpeedPanel,
        errorMessage,
    ) {
        if (
            !controlsVisible ||
            isScrubbingTimeline ||
            isTouchingTimeline ||
            !playbackSnapshot.isPlaying ||
            playbackSnapshot.isLoading ||
            showParentalGuide ||
            showSpeedPanel ||
            errorMessage != null
        ) {
            return@LaunchedEffect
        }
        delay(if (folded) 5000 else 3500)
        controlsVisible = false
    }

    LaunchedEffect(playerControlsLocked, lockedOverlayVisible) {
        if (!playerControlsLocked || !lockedOverlayVisible) return@LaunchedEffect
        delay(PlayerLockedOverlayDurationMs)
        lockedOverlayVisible = false
    }

    LaunchedEffect(
        playerSettingsUiState.pauseOverlayEnabled,
        snapshotSignals.isPlaying,
        snapshotSignals.isLoading,
        snapshotSignals.durationMs,
        errorMessage,
    ) {
        pausedOverlayVisible = false
        if (!playerSettingsUiState.pauseOverlayEnabled || playbackSnapshot.isPlaying || playbackSnapshot.isLoading || playbackSnapshot.durationMs <= 0L || errorMessage != null) {
            return@LaunchedEffect
        }
        delay(5000)
        pausedOverlayVisible = true
    }

    // Keyed on the position in whole seconds: the work below (server progress reporting, the
    // scrobble state machine and the 60s persist tick) does not need the 250ms resolution of
    // the engine snapshot, and restarting it that often steals time from the decoder.
    LaunchedEffect(
        snapshotSignals.positionSeconds,
        snapshotSignals.isPlaying,
        snapshotSignals.isLoading,
        snapshotSignals.isEnded,
        snapshotSignals.durationMs,
    ) {
        ServerPlayback.onPlaybackSnapshot(
            url = activeSourceUrl,
            positionMs = playbackSnapshot.positionMs,
            isPlaying = playbackSnapshot.isPlaying,
            isLoading = playbackSnapshot.isLoading,
            isEnded = playbackSnapshot.isEnded,
        )
        if (playbackSnapshot.isEnded) {
            flushWatchProgress(TrackingScrobbleAction.STOP)
            previousIsPlaying = false
            pendingSeekScrobbleRestart = false
            return@LaunchedEffect
        }

        if (previousIsPlaying && !playbackSnapshot.isPlaying && !playbackSnapshot.isLoading) {
            pendingSeekScrobbleRestart = false
            flushWatchProgress(TrackingScrobbleAction.PAUSE)
        }

        if (!previousIsPlaying && playbackSnapshot.isPlaying) {
            emitAddonPlayerEvent(AddonPlayerAction.START)
        }

        if (playbackSnapshot.isPlaying && pendingSeekScrobbleRestart) {
            pendingSeekScrobbleRestart = false
            if (hasRequestedScrobbleStartForCurrentItem) {
                emitTrackingSeekScrobbleStart()
            } else {
                emitTrackingScrobbleStart()
            }
        } else if (!previousIsPlaying && playbackSnapshot.isPlaying) {
            emitTrackingScrobbleStart()
        }

        if (!playbackSnapshot.isLoading) {
            previousIsPlaying = playbackSnapshot.isPlaying
        }
        if (playbackSnapshot.isPlaying) {
            persistPlaybackProgressTick()
            syncPlaybackProgressTick()
        }
    }
}

@Composable
private fun PlayerScreenRuntime.BindPlayerMetadataAndSkipEffects(networkStatus: NetworkStatusUiState) {
    val networkCondition = networkStatus.condition
    val networkAvailable = networkStatus.condition == NetworkCondition.Online
    LaunchedEffect(
        activeVideoId,
        activeSeasonNumber,
        activeEpisodeNumber,
        parentMetaId,
        parentMetaType,
        playerSettingsUiState.showParentalGuide,
        networkCondition,
    ) {
        parentalWarnings = emptyList()
        showParentalGuide = false
        parentalGuideHasShown = false
        playbackStartedForParentalGuide = false

        if (!playerSettingsUiState.showParentalGuide) return@LaunchedEffect
        if (networkCondition != NetworkCondition.Online) return@LaunchedEffect
        val imdbId = resolveParentalGuideImdbId() ?: return@LaunchedEffect
        val guide = ParentalGuideRepository.getParentalGuide(imdbId) ?: return@LaunchedEffect
        parentalWarnings = buildParentalWarnings(guide, parentalGuideLabels)

        if (playbackSnapshot.isPlaying) {
            tryShowParentalGuide()
        }
    }

    LaunchedEffect(playbackSnapshot.isPlaying, parentalWarnings) {
        if (playbackSnapshot.isPlaying) {
            tryShowParentalGuide()
        }
    }

    LaunchedEffect(activeVideoId, activeSeasonNumber, activeEpisodeNumber, networkAvailable) {
        skipIntervals = emptyList()
        apiSkipIntervals = emptyList()
        clearChapterSkipState()
        activeSkipInterval = null
        skipIntervalDismissed = false
        showNextEpisodeCard = false
        nextEpisodeAutoPlayJob?.cancel()
        nextEpisodePreparationJob?.cancel()
        nextEpisodeAutoPlaySearching = false
        nextEpisodeAutoPlayReady = false
        nextEpisodeAutoPlaySourceName = null
        nextEpisodeAutoPlayCountdown = null
        preloadedNextEpisodeVideoId = null
        preloadedNextEpisodeStream = null
        pendingNextEpisodeLaunch = false

        val season = activeSeasonNumber
        val episode = activeEpisodeNumber
        val vid = activeVideoId
        if (season == null || episode == null || vid == null || !networkAvailable) {
            return@LaunchedEffect
        }

        launch {
            val intervals = withTimeoutOrNull(SKIP_INTERVAL_RESOLVE_TIMEOUT_MS) {
                when {
                    vid.startsWith("mal:") -> {
                        val malId = vid.removePrefix("mal:").substringBefore(':')
                        SkipIntroRepository.getSkipIntervalsForMal(malId = malId, episode = episode)
                    }
                    vid.startsWith("kitsu:") -> {
                        val kitsuId = vid.removePrefix("kitsu:").substringBefore(':')
                        SkipIntroRepository.getSkipIntervalsForKitsu(kitsuId = kitsuId, episode = episode)
                    }
                    else -> SkipIntroRepository.getSkipIntervals(
                        imdbId = vid.substringBefore(':').takeIf { it.startsWith("tt") },
                        season = season,
                        episode = episode,
                    )
                }
            } ?: return@launch
            apiSkipIntervals = intervals
            recomputeMergedSkipIntervals()
        }
    }

    LaunchedEffect(
        playbackSnapshot.isLoading,
        playerController,
        playerControllerSourceUrl,
        activeSourceUrl,
    ) {
        if (!playbackSnapshot.isLoading) loadChaptersIfNeeded()
    }

    LaunchedEffect(playbackSnapshot.chapters) {
        adoptSnapshotChaptersIfNeeded()
    }

    LaunchedEffect(playbackSnapshot.positionMs, playbackSnapshot.durationMs, skipIntervals) {
        recomputeChapterSkipIntervalsIfNeeded()
        if (skipIntervals.isEmpty()) {
            activeSkipInterval = null
            return@LaunchedEffect
        }
        val positionSec = playbackSnapshot.positionMs / 1000.0
        val current = selectActiveSkipInterval(skipIntervals, positionSec)
        val previous = activeSkipInterval
        if (current != previous) {
            if (current != null) {
                // Keep dismissal when a provider replaces the interval within the same segment category.
                val keepDismissed = previous?.categoryKey() == current.categoryKey()
                activeSkipInterval = current
                if (!keepDismissed) skipIntervalDismissed = false
            } else if (previous != null) {
                // A replacement provider may start a little later; avoid flicker across a short gap.
                val nearby = skipIntervals.firstOrNull {
                    it.categoryKey() == previous.categoryKey() &&
                        it.startTime.isFinite() &&
                        it.endTime.isFinite() &&
                        it.endTime > it.startTime &&
                        positionSec < it.startTime &&
                        it.startTime - positionSec <= 2.0
                }
                if (nearby != null) {
                    activeSkipInterval = nearby
                } else {
                    activeSkipInterval = null
                    skipIntervalDismissed = false
                }
            }
        }
    }

    LaunchedEffect(
        nextEpisodeInfo,
        nuvioEnhancedSettingsUiState.enhancedHomeFeaturesEnabled,
        nuvioEnhancedSettingsUiState.nextEpisodeButtonEnabled,
        playerSettingsUiState.streamAutoPlayMode,
        playerSettingsUiState.streamAutoPlaySource,
        playerSettingsUiState.streamAutoPlaySelectedAddons,
        playerSettingsUiState.streamAutoPlaySelectedPlugins,
        playerSettingsUiState.streamAutoPlayRegex,
        playerSettingsUiState.streamAutoPlayTimeoutSeconds,
        playerSettingsUiState.streamAutoPlayPreferBingeGroup,
        playerSettingsUiState.streamAutoPlayNextEpisodeFallbackEnabled,
        activeProviderAddonId,
        activeProviderName,
        currentStreamBingeGroup,
    ) {
        if (
            !nuvioEnhancedSettingsUiState.enhancedHomeFeaturesEnabled ||
            !nuvioEnhancedSettingsUiState.nextEpisodeButtonEnabled ||
            nextEpisodeInfo?.hasAired != true
        ) {
            nextEpisodePreparationJob?.cancel()
            nextEpisodePreparationJob = null
            nextEpisodeAutoPlaySearching = false
            nextEpisodeAutoPlayReady = false
            nextEpisodeAutoPlaySourceName = null
            preloadedNextEpisodeStream = null
            return@LaunchedEffect
        }
        prepareNextEpisodeStream(force = true)
    }

    LaunchedEffect(
        playerMetaVideos,
        activeSeasonNumber,
        activeEpisodeNumber,
        randomNextEpisodeMode,
        playerSettingsUiState.randomNextEpisodeEnabled,
        nuvioEnhancedSettingsUiState.enhancedHomeFeaturesEnabled,
    ) {
        if (!isSeries || playerMetaVideos.isEmpty()) {
            nextEpisodeInfo = null
            return@LaunchedEffect
        }
        val curSeason = activeSeasonNumber ?: return@LaunchedEffect
        val curEpisode = activeEpisodeNumber ?: return@LaunchedEffect
        val nextVideo = PlayerNextEpisodeRules.resolveNextEpisode(
            videos = playerMetaVideos,
            currentSeason = curSeason,
            currentEpisode = curEpisode,
            randomMode = randomNextEpisodeMode &&
                playerSettingsUiState.randomNextEpisodeEnabled &&
                nuvioEnhancedSettingsUiState.enhancedHomeFeaturesEnabled,
            randomHistoryKey = parentMetaId,
        )
        val nextSeason = nextVideo?.season
        val nextEpisode = nextVideo?.episode
        nextEpisodeInfo = if (nextVideo != null && nextSeason != null && nextEpisode != null) {
            val playbackVideoId = buildPlaybackVideoId(
                parentMetaId = parentMetaId,
                seasonNumber = nextSeason,
                episodeNumber = nextEpisode,
                fallbackVideoId = nextVideo.id,
            )
            val isWatched = watchProgressUiState.progressForVideo(
                videoId = playbackVideoId,
                parentMetaId = parentMetaId,
                seasonNumber = nextSeason,
                episodeNumber = nextEpisode,
            )?.isEffectivelyCompleted == true || WatchingState.isEpisodeWatched(
                watchedKeys = watchedUiState.watchedKeys,
                metaType = parentMetaType,
                metaId = parentMetaId,
                episode = nextVideo,
            )
            NextEpisodeInfo(
                videoId = nextVideo.id,
                season = nextSeason,
                episode = nextEpisode,
                title = nextVideo.title,
                thumbnail = nextVideo.thumbnail,
                overview = nextVideo.overview,
                released = nextVideo.released,
                hasAired = PlayerNextEpisodeRules.hasEpisodeAired(nextVideo.released),
                isWatched = isWatched,
                unairedMessage = if (!PlayerNextEpisodeRules.hasEpisodeAired(nextVideo.released)) {
                    "$airsPrefix ${nextVideo.released ?: tbaLabel}"
                } else null,
            )
        } else null
    }

    LaunchedEffect(playbackSnapshot.isEnded) {
        if (playbackSnapshot.isEnded && nextEpisodeCardDismissed &&
            playerSettingsUiState.streamAutoPlayNextEpisodeEnabled &&
            nextEpisodeInfo?.hasAired == true
        ) {
            nextEpisodeCardDismissed = false
        }
    }

    LaunchedEffect(
        activePlaybackKey,
        playbackSnapshot,
        playbackSnapshotKey,
        initialSeekApplied,
        isScrubbingTimeline,
        errorMessage,
        nextEpisodeInfo,
        skipIntervals,
        playerSettingsUiState.nextEpisodeThresholdMode,
        playerSettingsUiState.nextEpisodeThresholdPercent,
        playerSettingsUiState.nextEpisodeThresholdMinutesBeforeEnd,
        playerSettingsUiState.streamAutoPlayNextEpisodeEnabled,
        nextEpisodeCardDismissed,
    ) {
        if (nextEpisodeInfo == null || (
                playbackSnapshot.durationMs <= 0L &&
                    !nuvioEnhancedSettingsUiState.nextEpisodeButtonEnabled
            )
        ) {
            showNextEpisodeCard = false
            return@LaunchedEffect
        }
        if (shouldShow && !showNextEpisodeCard) {
            preloadNextEpisodeStreams()
            showNextEpisodeCard = true
            if (playerSettingsUiState.streamAutoPlayNextEpisodeEnabled && nextEpisodeInfo?.hasAired == true) {
                playPreparedNextEpisode(withCountdown = true)
            }
        } else if (!shouldShow) {
            showNextEpisodeCard = false
        }
    }

    LaunchedEffect(playbackSnapshot.isEnded, nextEpisodeInfo) {
        if (playbackSnapshot.isEnded && nextEpisodeInfo != null && !showNextEpisodeCard) {
            preloadNextEpisodeStreams()
            showNextEpisodeCard = true
            if (playerSettingsUiState.streamAutoPlayNextEpisodeEnabled && nextEpisodeInfo?.hasAired == true) {
                playPreparedNextEpisode(withCountdown = true)
            }
        }
        movieRecommendationStage = stage
        showMovieRecommendationCard = stage > movieRecommendationDismissedStage
    }
}

private fun PlayerScreenRuntime.buildNowPlayingInfo(): PlayerNowPlayingInfo {
    val isEpisode = activeSeasonNumber != null && activeEpisodeNumber != null
    return PlayerNowPlayingInfo(
        title = title.ifBlank { activeStreamTitle },
        subtitle = buildNowPlayingSubtitle(
            isEpisode = isEpisode,
            seasonNumber = activeSeasonNumber,
            episodeNumber = activeEpisodeNumber,
            episodeTitle = activeEpisodeTitle,
        ),
        artworkUrl = if (isLiveTvPlayback) {
            firstNonBlankUrl(activeLogo, poster, background)
        } else {
            firstNonBlankUrl(poster, background)
        },
    )
}

private fun buildNowPlayingSubtitle(
    isEpisode: Boolean,
    seasonNumber: Int?,
    episodeNumber: Int?,
    episodeTitle: String?,
): String? {
    if (!isEpisode) return null

    val episodeParts = buildList {
        localizedSeasonEpisodeCode(seasonNumber, episodeNumber)?.let { add(it) }
        episodeTitle?.takeIf { it.isNotBlank() }?.let { add(it) }
    }

    return when (episodeParts.size) {
        0 -> null
        1 -> episodeParts.first()
        else -> "${episodeParts[0]} - ${episodeParts[1]}"
    }
}

private fun firstNonBlankUrl(vararg values: String?): String? =
    values.firstOrNull { !it.isNullOrBlank() }?.trim()

internal fun PlayerScreenRuntime.removeFailedStreamFromCache() {
    val currentVideoId = activeVideoId ?: return
    val cacheKey = StreamLinkCacheRepository.contentKey(
        type = contentType ?: parentMetaType,
        videoId = currentVideoId,
        parentMetaId = parentMetaId,
        season = activeSeasonNumber,
        episode = activeEpisodeNumber,
    )
    StreamLinkCacheRepository.remove(cacheKey)
}

/**
 * Fetches the playing source again from its add-on (re-resolving debrid) and continues from the
 * same position when its link has likely expired:
 * - a link reopened from the reuse-last-link cache, which may have expired since it was saved;
 * - a link with expiring credentials in its URL, which may already be stale when picked from
 *   warmed-up results or may expire during playback.
 * Each failed URL is refreshed at most once.
 */
internal fun PlayerScreenRuntime.tryRefreshExpiredLinkAfterError(message: String?): Boolean {
    if (ServerStreams.isServerSourceId(activeProviderAddonId)) return retryServerSourceAfterError(message)
    if (credentialRefreshJob?.isActive == true) return true
    val failedUrl = activeSourceUrl
    if (failedUrl.isBlank()) return false
    val fromLinkCache = failedUrl == linkCacheSourceUrl
    if (!fromLinkCache && !failedUrl.hasLikelyExpiringPlaybackCredentials()) return false
    if (credentialRefreshAttemptedSourceUrl == failedUrl) return false

    val currentVideoId = activeVideoId ?: return false
    credentialRefreshAttemptedSourceUrl = failedUrl
    linkCacheSourceUrl = null
    removeFailedStreamFromCache()

    val expectedProviderAddonId = activeProviderAddonId
    val expectedProviderName = activeProviderName
    val expectedStreamTitle = activeStreamTitle
    val expectedBingeGroup = currentStreamBingeGroup
    val expectedInfoHash = activeStream?.p2pInfoHash ?: currentStreamInfoHash
    val expectedFilename = activeStream?.behaviorHints?.filename ?: currentStreamFilename
    val type = contentType ?: parentMetaType
    val season = activeSeasonNumber
    val episode = activeEpisodeNumber

    errorMessage = null
    controlsVisible = !playerControlsLocked

    credentialRefreshJob = scope.launch {
        PlayerStreamsRepository.loadSources(
            type = type,
            videoId = currentVideoId,
            parentMetaId = parentMetaId,
            parentMetaType = parentMetaType,
            season = season,
            episode = episode,
            forceRefresh = true,
        )

        var refreshedStream: StreamItem? = null
        var pollCount = 0
        while (pollCount < CREDENTIAL_REFRESH_POLL_COUNT && refreshedStream == null) {
            val state = PlayerStreamsRepository.sourceState.value
            refreshedStream = findCredentialRefreshCandidate(
                streams = state.groups.flatMap { it.streams },
                failedUrl = failedUrl,
                expectedProviderAddonId = expectedProviderAddonId,
                expectedProviderName = expectedProviderName,
                expectedStreamTitle = expectedStreamTitle,
                expectedBingeGroup = expectedBingeGroup,
            )

            var refreshedStream: StreamItem? = null
            var pollCount = 0
            while (pollCount < CREDENTIAL_REFRESH_POLL_COUNT && refreshedStream == null) {
                val state = PlayerStreamsRepository.sourceState.value
                refreshedStream = findCredentialRefreshCandidate(
                    streams = state.groups.flatMap { it.streams },
                    failedUrl = failedUrl,
                    expectedProviderAddonId = expectedProviderAddonId,
                    expectedProviderName = expectedProviderName,
                    expectedStreamTitle = expectedStreamTitle,
                    expectedBingeGroup = expectedBingeGroup,
                    expectedInfoHash = expectedInfoHash,
                    expectedFilename = expectedFilename,
                )
                if (
                    refreshedStream != null ||
                    state.emptyStateReason != null ||
                    (!state.isAnyLoading && state.groups.isNotEmpty())
                ) {
                    break
                }
                delay(CREDENTIAL_REFRESH_POLL_INTERVAL_MS)
                pollCount++
            }

            val candidate = refreshedStream
            if (candidate == null) {
                showErrorUnlessSourceFallback(message)
                return@launch
            }
            // Debrid links are always requested anew; a prepared or in-memory one may be the expired link.
            val unresolved = if (candidate.debridResolved) candidate.copy(url = null, debridResolved = false) else candidate
            val stream = when (
                val resolved = DirectDebridPlaybackResolver.resolveToPlayableStream(
                    stream = unresolved,
                    season = season,
                    episode = episode,
                    forceRefresh = true,
                )
            ) {
                is DirectDebridPlayableResult.Success -> resolved.stream
                else -> {
                    showErrorUnlessSourceFallback(message)
                    return@launch
                }
            }

            val refreshedUrl = stream.playableDirectUrl
            if (refreshedUrl.isNullOrBlank() || refreshedUrl == failedUrl) {
                showErrorUnlessSourceFallback(message)
                return@launch
            }

            playReplacementSource(stream, refreshedUrl, currentVideoId)
        } finally {
            PlayerStreamsRepository.stopSourcesLoading()
        }
    }
    return true
}

private fun PlayerScreenRuntime.retryServerSourceAfterError(message: String?): Boolean {
    if (credentialRefreshJob?.isActive == true) return true
    val failedUrl = activeSourceUrl
    if (credentialRefreshAttemptedSourceUrl == failedUrl) return false
    credentialRefreshAttemptedSourceUrl = failedUrl
    val savedPositionMs = playbackSnapshot.positionMs.coerceAtLeast(0L)
    errorMessage = null
    credentialRefreshJob = scope.launch {
        val playback = ServerPlayback.fallback(failedUrl)
        if (playback == null) {
            errorMessage = message
            controlsVisible = !playerControlsLocked
            return@launch
        }

        flushWatchProgress()
        stopActiveP2pStream()
        activeSourceUrl = refreshedUrl
        activeSourceAudioUrl = null
        activeSourceHeaders = sanitizePlaybackHeaders(stream.behaviorHints.proxyHeaders?.request)
        activeSourceResponseHeaders = sanitizePlaybackResponseHeaders(stream.behaviorHints.proxyHeaders?.response)
        activeExternalSubtitles = stream.externalSubtitles
        activeStreamType = stream.streamType
        activeStreamTitle = stream.streamLabel
        activeStreamSubtitle = stream.streamSubtitle
        activeProviderName = stream.addonName
        activeProviderAddonId = stream.addonId
        currentStreamBingeGroup = stream.behaviorHints.bingeGroup
        currentStreamFilename = stream.behaviorHints.filename
        currentStreamInfoHash = stream.infoHash ?: stream.p2pInfoHash
        activeInitialPositionMs = savedPositionMs
        activeInitialProgressFraction = null
    }
    return true
}

internal fun findCredentialRefreshCandidate(
    streams: List<StreamItem>,
    failedUrl: String,
    expectedProviderAddonId: String?,
    expectedProviderName: String,
    expectedStreamTitle: String,
    expectedBingeGroup: String?,
    expectedInfoHash: String? = null,
    expectedFilename: String? = null,
    canResolveDebrid: (StreamItem) -> Boolean = DirectDebridPlaybackResolver::shouldResolveToPlayableStream,
): StreamItem? =
    streams
        .asSequence()
        .mapNotNull { stream ->
            val hasFreshUrl = stream.playableDirectUrl?.let { it.isNotBlank() && it != failedUrl } == true
            if (!hasFreshUrl && !stream.debridResolved && !canResolveDebrid(stream)) return@mapNotNull null
            val providerMatches = if (!expectedProviderAddonId.isNullOrBlank()) {
                stream.addonId == expectedProviderAddonId
            } else {
                stream.addonName == expectedProviderName
            }
            if (!providerMatches) return@mapNotNull null

            var score = 100
            if (!expectedInfoHash.isNullOrBlank() && stream.p2pInfoHash.equals(expectedInfoHash, ignoreCase = true)) {
                score += 60
            }
            if (stream.streamLabel == expectedStreamTitle) score += 40
            if (!expectedFilename.isNullOrBlank() && stream.behaviorHints.filename == expectedFilename) score += 30
            if (!expectedBingeGroup.isNullOrBlank() && stream.behaviorHints.bingeGroup == expectedBingeGroup) {
                score += 20
            }
            if (stream.playableDirectUrl?.hasLikelyExpiringPlaybackCredentials() == true) score += 5
            score to stream
        }
        .maxByOrNull { (score, _) -> score }
        ?.second

internal fun PlayerScreenRuntime.isControllerOnActivePlayback(
    controllerSourceUrl: String? = playerControllerSourceUrl,
): Boolean =
    controllerSourceUrl != null && (
        controllerSourceUrl == activeSourceUrl ||
            controllerSourceUrl == activePlaybackSourceUrl ||
            controllerSourceUrl == p2pResolvedSourceUrl
        )

private fun SkipInterval.isEligibleForSkipButton(positionSec: Double, offsetSeconds: Int): Boolean =
    positionSec >= skipButtonStartTime(offsetSeconds) && positionSec < endTime

private fun SkipInterval.skipButtonStartTime(offsetSeconds: Int): Double =
    if (isIntroLikeSkipInterval() && offsetSeconds != 0) {
        (startTime + offsetSeconds).coerceIn(0.0, maxOf(startTime, endTime - 1.0))
    } else {
        startTime
    }

private fun SkipInterval.isIntroLikeSkipInterval(): Boolean =
    when (type.lowercase()) {
        "intro", "op", "mixed-op" -> true
        else -> false
    }

private const val CREDENTIAL_REFRESH_POLL_COUNT = 30
private const val CREDENTIAL_REFRESH_POLL_INTERVAL_MS = 500L

internal fun PlayerScreenRuntime.isControllerOnActivePlayback(
    controllerSourceUrl: String? = playerControllerSourceUrl,
): Boolean =
    controllerSourceUrl != null && (
        controllerSourceUrl == activeSourceUrl ||
            controllerSourceUrl == activePlaybackSourceUrl ||
            controllerSourceUrl == p2pResolvedSourceUrl
        )
