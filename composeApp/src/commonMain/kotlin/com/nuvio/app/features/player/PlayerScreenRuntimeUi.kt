package com.nuvio.app.features.player

import com.nuvio.app.features.watching.application.WatchingState
import com.nuvio.app.features.home.MetaPreview
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import com.nuvio.app.core.ui.DialogButton
import com.nuvio.app.core.ui.DialogButtonStyle
import com.nuvio.app.core.ui.DialogButtons
import com.nuvio.app.core.ui.DialogSurface
import com.nuvio.app.core.ui.glass.LocalLiquidGlassBackdrop
import com.nuvio.app.features.p2p.P2pStreamingState
import com.nuvio.app.features.party.PartyRepository
import com.nuvio.app.features.p2p.formatP2pMegabytes
import com.nuvio.app.features.p2p.formatP2pSpeed
import com.nuvio.app.features.player.skip.internalSkipAction
import com.nuvio.app.features.servers.ServerStreams
import com.nuvio.app.features.streams.streamAddonInstanceId
import com.nuvio.app.isIos
import kotlinx.coroutines.launch
import com.nuvio.app.core.i18n.localizedSeasonEpisodeCode
import com.nuvio.app.features.ratings.UserRatingPlayerOverlay
import com.nuvio.app.features.ratings.rememberCanRate
import com.nuvio.app.features.ratings.rememberUserRating
import com.nuvio.app.features.ratings.toUserRatingTarget
import com.nuvio.app.features.tracking.TrackingRatingTarget
import com.nuvio.app.features.livetv.LiveTvRepository
import com.nuvio.app.features.livetv.currentProgram
import com.nuvio.app.features.livetv.rememberLiveTvEpgNowMillis
import nuvio.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PlayerScreenRuntime.RenderPlayerRuntimeUi(
    partyPanelWidth: Dp,
    partyPanelInset: Dp,
    dockPartyPanel: Boolean,
) {
    val runtime = this
    val displayedPositionMs = scrubbingPositionMs ?: playbackSnapshot.positionMs
    val isEpisode = activeSeasonNumber != null && activeEpisodeNumber != null
    val currentGestureFeedback = liveGestureFeedback ?: gestureFeedback
    val isP2pPlaybackActive = activeTorrentInfoHash != null
    val p2pConnecting = p2pStreamingState as? P2pStreamingState.Connecting
    val p2pStats = p2pStreamingState as? P2pStreamingState.Streaming
    val p2pPeerInfo = p2pStats?.let { stats ->
        org.jetbrains.compose.resources.stringResource(
            nuvio.composeapp.generated.resources.Res.string.player_torrent_peer_info,
            stats.seeds,
            stats.peers,
        )
    }
    val p2pDownloadSpeed = p2pStats?.let { formatP2pSpeed(it.downloadSpeed) }
    val p2pLoadingBytes = p2pStats?.let { maxOf(it.downloadedBytes, it.deliveredBytes) } ?: 0L
    val connectingPeerInfo = p2pConnecting?.let { state ->
        org.jetbrains.compose.resources.stringResource(
            nuvio.composeapp.generated.resources.Res.string.player_torrent_peer_info,
            state.seeds,
            state.peers,
        )
    }
    val p2pInitialLoadingMessage = when {
        !isP2pPlaybackActive || initialLoadCompleted -> null
        p2pConnecting != null -> {
            if (p2pSettingsUiState.hideTorrentStats) {
                p2pConnectingPhaseLabel(p2pConnecting.phase)
            } else {
                org.jetbrains.compose.resources.stringResource(
                    nuvio.composeapp.generated.resources.Res.string.player_torrent_connecting_status,
                    p2pConnectingPhaseLabel(p2pConnecting.phase),
                    connectingPeerInfo.orEmpty(),
                    formatP2pSpeed(p2pConnecting.downloadSpeed),
                )
            }
        }
        p2pStats != null -> {
            if (p2pSettingsUiState.hideTorrentStats) {
                null
            } else {
                org.jetbrains.compose.resources.stringResource(
                    nuvio.composeapp.generated.resources.Res.string.player_torrent_loading_status,
                    formatP2pMegabytes(p2pLoadingBytes),
                    p2pPeerInfo.orEmpty(),
                    p2pDownloadSpeed.orEmpty(),
                )
            }
        }
        else -> org.jetbrains.compose.resources.stringResource(
            nuvio.composeapp.generated.resources.Res.string.player_torrent_starting_engine,
        )
    }
    val p2pInitialLoadingProgress = when {
        !isP2pPlaybackActive || initialLoadCompleted || p2pStats == null -> null
        else -> {
            val bufferedAheadMs = (playbackSnapshot.bufferedPositionMs - playbackSnapshot.positionMs)
                .coerceAtLeast(0L)
            p2pInitialLoadingProgress(
                bufferedAheadMs = bufferedAheadMs,
                downloadedBytes = p2pStats.downloadedBytes,
                deliveredBytes = p2pStats.deliveredBytes,
            )
        }
    }
    val showP2pRebufferStats = isP2pPlaybackActive &&
        initialLoadCompleted &&
        playbackSnapshot.isLoading &&
        p2pStats != null &&
        !p2pSettingsUiState.hideTorrentStats
    val p2pRebufferMessage = when {
        !showP2pRebufferStats -> null
        else -> {
            val bufferedSeconds = ((playbackSnapshot.bufferedPositionMs - playbackSnapshot.positionMs) / 1000L)
                .coerceAtLeast(0L)
            "${bufferedSeconds}s buffered · ${p2pPeerInfo.orEmpty()} · ${p2pDownloadSpeed.orEmpty()}"
        }
    }
    val p2pRebufferProgress = when {
        !showP2pRebufferStats -> null
        else -> {
            val bufferedSeconds = ((playbackSnapshot.bufferedPositionMs - playbackSnapshot.positionMs) / 1000f)
                .coerceAtLeast(0f)
            (bufferedSeconds / 10f).coerceIn(0f, 1f)
        }
    }
    val gestureCallbacks = rememberSurfaceGestureCallbacks()
    val onPinchZoom = rememberUpdatedState<(Float) -> Unit> { factor -> pinchVideoZoom(factor) }
    val playbackGesturesEnabled = initialLoadCompleted && errorMessage == null
    LaunchedEffect(playerController) {
        if (videoZoom != 1f) playerController?.setVideoZoom(videoZoom)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(end = partyPanelInset)
            .clipToBounds()
            .onSizeChanged { layoutSize = it }
            .then(if (isMiniPresentation) Modifier else Modifier.playerSurfaceTapGestures(
                layoutSize = layoutSize,
                playbackGesturesEnabled = playbackGesturesEnabled,
                playerControlsLockedState = gestureCallbacks.playerControlsLocked,
                onSurfaceTap = gestureCallbacks.onSurfaceTap,
                onSurfaceDoubleTap = gestureCallbacks.onSurfaceDoubleTap,
                activateHoldToSpeedState = gestureCallbacks.activateHoldToSpeed,
                deactivateHoldToSpeedState = gestureCallbacks.deactivateHoldToSpeed,
                revealLockedOverlayState = gestureCallbacks.revealLockedOverlay,
            )
            .playerSurfaceDragGestures(
                gestureController = gestureController,
                playerController = playerController,
                layoutSize = layoutSize,
                playbackGesturesEnabled = playbackGesturesEnabled,
                sideGestureSystemEdgeExclusionPx = sideGestureSystemEdgeExclusionPx,
                horizontalSeekSystemEdgeExclusionPx = horizontalSeekSystemEdgeExclusionPx,
                playerControlsLockedState = gestureCallbacks.playerControlsLocked,
                touchGesturesEnabledState = gestureCallbacks.touchGesturesEnabled,
                swipeToSeekEnabledState = gestureCallbacks.swipeToSeekEnabled,
                isHoldToSpeedGestureActiveState = gestureCallbacks.isHoldToSpeedGestureActive,
                currentPositionMsState = gestureCallbacks.currentPositionMs,
                currentDurationMsState = gestureCallbacks.currentDurationMs,
                deactivateHoldToSpeedState = gestureCallbacks.deactivateHoldToSpeed,
                showHorizontalSeekPreviewState = gestureCallbacks.showHorizontalSeekPreview,
                showBrightnessFeedbackState = gestureCallbacks.showBrightnessFeedback,
                showVolumeFeedbackState = gestureCallbacks.showVolumeFeedback,
                currentVolumeBoostPercentState = gestureCallbacks.currentVolumeBoostPercent,
                applyVolumeBoostPercentState = gestureCallbacks.applyVolumeBoostPercent,
                clearLiveGestureFeedbackState = gestureCallbacks.clearLiveGestureFeedback,
                revealLockedOverlayState = gestureCallbacks.revealLockedOverlay,
                commitHorizontalSeekState = gestureCallbacks.commitHorizontalSeek,
            )
            .playerSurfacePinchGestures(
                playbackGesturesEnabled = playbackGesturesEnabled,
                playerControlsLockedState = gestureCallbacks.playerControlsLocked,
                touchGesturesEnabledState = gestureCallbacks.touchGesturesEnabled,
                onPinchZoomState = onPinchZoom,
            )),
    ) {
        val playerSurfaceSourceUrl = currentPlaybackSurfaceSourceUrl
        val initialPositionRequestKey = currentInitialPositionRequestKey()
        if (playerSurfaceSourceUrl != null) {
            PlatformPlayerSurface(
                sourceUrl = playerSurfaceSourceUrl,
                sourceAudioUrl = activeSourceAudioUrl,
                sourceHeaders = activeSourceHeaders,
                sourceResponseHeaders = activeSourceResponseHeaders,
                externalSubtitles = activeExternalSubtitles,
                streamType = activeStreamType,
                modifier = Modifier.fillMaxSize(),
                playWhenReady = shouldPlay,
                initialPositionMs = activeInitialPositionMs.takeIf { it > 0L },
                initialPositionRequestKey = initialPositionRequestKey,
                resizeMode = resizeMode,
                playbackEngine = playbackEngineOverride ?: playerSettingsUiState.androidPlaybackEngine,
                onInitialPositionHandled = { key, handled ->
                    if (key == currentInitialPositionRequestKey()) {
                        initialSeekApplied = handled
                    }
                },
                onControllerReady = { controller ->
                    playerController = controller
                    playerControllerSourceUrl = playerSurfaceSourceUrl
                    pendingPlaybackSpeedRestore?.let { speed ->
                        controller.setPlaybackSpeed(speed)
                        pendingPlaybackSpeedRestore = null
                    }
                },
                onSnapshot = { snapshot ->
                    playbackSnapshot = snapshot
                    if (!snapshot.isLoading) initialLoadCompleted = true
                    if (snapshot.isEnded) {
                        shouldPlay = false
                        controlsVisible = !playerControlsLocked
                    }
                },
                onError = { message ->
                    if (message != null && tryRefreshCredentialedSourceAfterError(message)) {
                        return@PlatformPlayerSurface
                    }
                    if (maybeFailoverPlaybackEngine(message)) return@PlatformPlayerSurface
                    errorMessage = message?.removePrefix("audio-route:")
                    if (message != null) {
                        controlsVisible = !playerControlsLocked
                        removeFailedStreamFromCache()
                    }
                },
            )
        }
        val resolvedAutoEngine = resolvedAutoEngineState.value

        if (!isMiniPresentation) {
        AnimatedVisibility(
            visible = playerSettingsUiState.pauseOverlayEnabled && pausedOverlayVisible && !controlsVisible && !playerControlsLocked,
            enter = fadeIn(animationSpec = tween(durationMillis = 220)),
            exit = fadeOut(animationSpec = tween(durationMillis = 180)),
        ) {
            PauseMetadataOverlay(
                title = if (isLiveTvPlayback) activeStreamTitle else title,
                logo = if (isLiveTvPlayback) activeLogo else logo,
                isEpisode = isEpisode,
                seasonNumber = activeSeasonNumber,
                episodeNumber = activeEpisodeNumber,
                episodeTitle = activeEpisodeTitle,
                pauseDescription = activePauseDescription ?: activeStreamSubtitle,
                providerName = activeProviderName,
                metrics = metrics,
                horizontalSafePadding = horizontalSafePadding,
                modifier = Modifier.fillMaxSize(),
            )
        }

        RenderPlayerControls(
            isEpisode = isEpisode,
            folded = fold != null,
            modifier = panelModifier,
        )
        RenderPartyPanel()
        if (showSubtitleSyncByEar) {
            SubtitleSyncByEarCard(
                visible = true,
                subtitleDelayMs = subtitleDelayMs,
                heardCaptured = subtitleSyncHeardPositionMs != null,
                sawCaptured = subtitleSyncSawPositionMs != null,
                onHeard = { captureSubtitleSyncHeard() },
                onSaw = { captureSubtitleSyncSaw() },
                onClose = { closeSubtitleSyncByEar() },
                modifier = Modifier.align(androidx.compose.ui.Alignment.TopCenter)
                    .padding(top = 16.dp, start = horizontalSafePadding, end = horizontalSafePadding),
            )
        }
        RenderPlayerModals(displayedPositionMs = displayedPositionMs)
        StreamInfoOverlay(visible=showStreamInfo,provider=activeProviderName,streamTitle=activeStreamTitle,streamSubtitle=activeStreamSubtitle,addonId=activeProviderAddonId,filename=currentStreamFilename,infoHash=currentStreamInfoHash,audioTrack=audioTracks.firstOrNull { it.index == selectedAudioIndex } ?: audioTracks.firstOrNull { it.isSelected },subtitleTrack=subtitleTracks.firstOrNull { it.index == selectedSubtitleIndex } ?: subtitleTracks.firstOrNull { it.isSelected }.takeUnless { useCustomSubtitles },addonSubtitle=addonSubtitles.firstOrNull { it.selectionKey == selectedAddonSubtitleId },engine=playerController?.playbackEngine,streamType=activeStreamType,resolution=if(playbackSnapshot.videoWidth!=null&&playbackSnapshot.videoHeight!=null) "${playbackSnapshot.videoWidth} × ${playbackSnapshot.videoHeight}" else null,videoCodec=playbackSnapshot.videoCodec,videoFrameRate=playbackSnapshot.videoFrameRate,videoBitrate=playbackSnapshot.videoBitrate,audioCodec=playbackSnapshot.audioCodec,audioChannels=playbackSnapshot.audioChannels,audioSampleRate=playbackSnapshot.audioSampleRate,hdrFormat=playbackSnapshot.hdrFormat,onDismiss={showStreamInfo=false})
        val switchEngineTarget = playerController?.playbackEngine?.switchTarget
        if (switchEngineTarget != null && showSwitchEngineConfirm) {
            DialogSurface(
                onDismissRequest = { showSwitchEngineConfirm = false },
                title = stringResource(Res.string.player_engine_switch_title, switchEngineTarget.label),
                message = stringResource(Res.string.player_engine_switch_message),
            ) {
                DialogButtons {
                    DialogButton(
                        text = stringResource(Res.string.action_cancel),
                        onClick = { showSwitchEngineConfirm = false },
                        style = DialogButtonStyle.Secondary,
                    )
                    DialogButton(
                        text = stringResource(Res.string.action_switch),
                        onClick = {
                            showSwitchEngineConfirm = false
                            switchPlaybackEngine()
                        },
                        style = DialogButtonStyle.Primary,
                    )
                }
            }
        }
        }
    }
}

@Composable
private fun p2pConnectingPhaseLabel(phase: String): String = when (phase) {
    "add_magnet" -> org.jetbrains.compose.resources.stringResource(
        nuvio.composeapp.generated.resources.Res.string.player_torrent_fetching_metadata,
    )
    "prepare_stream", "attach_route" -> org.jetbrains.compose.resources.stringResource(
        nuvio.composeapp.generated.resources.Res.string.player_torrent_preparing_stream,
    )
    else -> org.jetbrains.compose.resources.stringResource(
        nuvio.composeapp.generated.resources.Res.string.player_torrent_starting_engine,
    )
}

internal fun buildInitialPositionRequestKey(
    playbackIdentity: String,
    videoId: String?,
    seasonNumber: Int?,
    episodeNumber: Int?,
): String {
    return "$playbackIdentity:${videoId.orEmpty()}:$seasonNumber:$episodeNumber"
}

private fun PlayerScreenRuntime.currentInitialPositionRequestKey(): String? {
    return buildInitialPositionRequestKey(
        playbackIdentity = activePlaybackIdentity,
        videoId = activeVideoId,
        seasonNumber = activeSeasonNumber,
        episodeNumber = activeEpisodeNumber,
    )
}

@Composable
private fun PlayerScreenRuntime.RenderPlayerControls(
    isEpisode: Boolean,
    folded: Boolean,
    modifier: Modifier,
) {
    val isInPip = rememberIsInPictureInPicture()
    val orientationToggleSupported = rememberPlayerOrientationToggleSupported()
    val portraitPlayback by PlayerOrientationPreference.portrait.collectAsState()
    val playerVideoFrameBackdrop = rememberVideoFrameBackdrop(enabled = playerSettingsUiState.playerLiquidGlassEnabled)
    val showQuietDeviceStatusOverlay = nuvioEnhancedSettingsUiState.playerStatusOverlayEnabled &&
        !controlsVisible &&
        !showParentalGuide &&
        !playerControlsLocked &&
        !(playerSettingsUiState.pauseOverlayEnabled && pausedOverlayVisible)
    AnimatedVisibility(
        visible = shouldShowPlayerControlsShell(
            isInPip = isInPip,
            controlsVisible = controlsVisible,
            showParentalGuide = showParentalGuide,
            playerControlsLocked = playerControlsLocked,
            showQuietDeviceStatusOverlay = showQuietDeviceStatusOverlay,
        ),
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        CompositionLocalProvider(
            LocalLiquidGlassBackdrop provides playerVideoFrameBackdrop,
            LocalPlayerLiquidGlassEnabled provides playerSettingsUiState.playerLiquidGlassEnabled,
        ) {
            PlayerControlsShell(
                title = title,
                streamTitle = activeStreamTitle,
                providerName = activeProviderName,
                seasonNumber = activeSeasonNumber,
                episodeNumber = activeEpisodeNumber,
                episodeTitle = activeEpisodeTitle,
                playbackSnapshot = playbackSnapshot,
                displayedPositionMs = displayedPositionMs,
                metrics = metrics,
                resizeMode = resizeMode,
                isLocked = playerControlsLocked,
                skipBackwardSeconds = playerSettingsUiState.skipBackwardSeconds,
                skipForwardSeconds = playerSettingsUiState.skipForwardSeconds,
                showPlaybackControls = controlsVisible,
                showDeviceStatusOverlay = showQuietDeviceStatusOverlay,
                showClockEndTime = nuvioEnhancedSettingsUiState.enhancedHomeFeaturesEnabled &&
                    playerSettingsUiState.playerClockEndTimeEnabled &&
                    contentType != "live-tv",
                onLockToggle = {
                    if (playerControlsLocked) unlockPlayerControls() else lockPlayerControls()
                },
                onBack = {
                    flushWatchProgress()
                    handOffToMiniPlayerIfEnabled()
                    args.onBack()
                },
                onTogglePlayback = { togglePlayback() },
                onSeekBack = { seekBy(-playerSettingsUiState.skipBackwardSeconds * 1_000L) },
                onSeekForward = { seekBy(playerSettingsUiState.skipForwardSeconds * 1_000L) },
                onResizeModeClick = { cycleResizeMode() },
                onSpeedClick = { cyclePlaybackSpeed() },
                onSubtitleClick = if (isLiveTv) null else {
                    {
                        refreshTracks()
                        activeSubtitleTab = SubtitleTab.BuiltIn
                        showSubtitleModal = true
                    }
                },
                onSubtitleSyncClick = if (
                    nuvioEnhancedSettingsUiState.enhancedHomeFeaturesEnabled &&
                    playerSettingsUiState.subtitleSyncMenuEnabled
                ) {
                    {
                        activeSubtitleTab = SubtitleTab.Sync
                        loadSubtitleAutoSyncCues()
                        showSubtitleModal = true
                    }
                } else {
                    null
                },
                onAudioClick = {
                    refreshTracks()
                    showAudioModal = true
                },
                qualityLabel = playerQualityControlLabel(),
                onQualityClick = if (!isLiveTv && activeTorrentInfoHash == null) {
                    { openQualityPanel() }
                } else {
                    null
                },
                onChannelsClick = if (isLiveTv) {
                    { showLiveTvChannelsPanel = true }
                } else {
                    null
                },
                onStreamInfoClick = { showStreamInfo = true; controlsVisible = true },
                onVideoSettingsClick = if (isIos) {
                    {
                        showVideoSettingsModal = true
                        controlsVisible = true
                    }
                } else {
                    null
                },
                onOrientationToggleClick = if (orientationToggleSupported) {
                    { PlayerOrientationPreference.toggle() }
                } else null,
                onPartyClick = { showPartyPanel = true },
                isPortraitPlayback = portraitPlayback,
                onSourcesClick = if (activeVideoId != null) { { openSourcesPanel() } } else null,
                onEpisodesClick = if (isSeries) { { openEpisodesPanel() } } else null,
                onNextEpisodeClick = if (
                    nuvioEnhancedSettingsUiState.enhancedHomeFeaturesEnabled &&
                    nuvioEnhancedSettingsUiState.nextEpisodeButtonEnabled &&
                    isSeries &&
                    nextEpisodeInfo?.hasAired == true
                ) {
                    {
                        playPreparedNextEpisode()
                    }
                } else null,
                nextEpisodeSearching = nextEpisodeAutoPlaySearching,
                nextEpisodeReady = nextEpisodeAutoPlayReady,
                randomNextEpisodeMode = randomNextEpisodeMode,
                onRandomNextEpisodeModeToggle = if (
                    nuvioEnhancedSettingsUiState.enhancedHomeFeaturesEnabled &&
                    isSeries &&
                    playerSettingsUiState.randomNextEpisodeEnabled
                ) {
                    { randomNextEpisodeMode = !randomNextEpisodeMode }
                } else null,
                onSwitchEngineClick = if (!isIos && playerController?.playbackEngine != null) { { showSwitchEngineConfirm = true } } else null,
                onOpenInExternalPlayer = args.onOpenInExternalPlayer?.let { openExternal ->
                    {
                        val loadedSubtitles = addonSubtitles
                            .takeIf { it.isNotEmpty() }
                            ?.map { sub ->
                                SubtitleInput(
                                    url = sub.url,
                                    name = buildString {
                                        if (!sub.addonName.isNullOrBlank()) append("[${sub.addonName}] ")
                                        append(sub.display)
                                    },
                                    lang = sub.language,
                                )
                            }
                        openExternal(
                            ExternalPlayerPlaybackRequest(
                                sourceUrl = externalPlayerSourceUrl(),
                                title = title,
                                streamTitle = activeStreamTitle,
                                sourceHeaders = activeSourceHeaders,
                                resumePositionMs = playbackSnapshot.positionMs,
                                subtitles = loadedSubtitles,
                                season = activeSeasonNumber,
                                episode = activeEpisodeNumber,
                                episodeTitle = activeEpisodeTitle,
                            ),
                        )
                    }
                },
                onSubmitIntroClick = if (
                    isSeries &&
                    playerSettingsUiState.introSubmitEnabled &&
                    playerSettingsUiState.introDbApiKey.isNotBlank()
                ) {
                    { showSubmitIntroModal = true }
                } else {
                    null
                },
                parentalWarnings = parentalWarnings,
                showParentalGuide = showParentalGuide,
                onParentalGuideAnimationComplete = { showParentalGuide = false },
                onScrubChange = { positionMs ->
                    isScrubbingTimeline = true
                    scrubbingPositionMs = positionMs
                },
                onScrubFinished = { positionMs ->
                    isScrubbingTimeline = false
                    scrubbingPositionMs = null
                    seekToPosition(positionMs)
                },
                horizontalSafePadding = horizontalSafePadding,
                modifier = Modifier.fillMaxSize(),
            )

        }
    }
}

internal fun shouldShowPlayerControlsShell(
    isInPip: Boolean,
    controlsVisible: Boolean,
    showParentalGuide: Boolean,
    playerControlsLocked: Boolean,
    showQuietDeviceStatusOverlay: Boolean,
): Boolean = !isInPip &&
    (((controlsVisible || showParentalGuide) && !playerControlsLocked) || showQuietDeviceStatusOverlay)

@Composable
private fun BoxScope.RenderPlaybackOverlays(
    runtime: PlayerScreenRuntime,
    currentGestureFeedback: GestureFeedbackState?,
    p2pInitialLoadingMessage: String?,
    p2pInitialLoadingProgress: Float?,
    showP2pRebufferStats: Boolean,
    p2pRebufferMessage: String?,
    p2pRebufferProgress: Float?,
    folded: Boolean,
) {
    runtime.run {
        val overlaySnapshot = if (playerControlsLocked && lockedOverlayVisible) {
            playbackSnapshot
        } else {
            Snapshot.withoutReadObservation { playbackSnapshot }
        }
        val snapshotIsLoading by remember(runtime) { derivedStateOf { playbackSnapshot.isLoading } }
        val snapshotDurationMs by remember(runtime) { derivedStateOf { playbackSnapshot.durationMs } }
        val displayedPositionMs = scrubbingPositionMs ?: overlaySnapshot.positionMs
        val openingOverlayVisible = playerSettingsUiState.showLoadingOverlay && !initialLoadCompleted && errorMessage == null
        val showNetworkSpeed = openingOverlayVisible &&
            playerSettingsUiState.showPlayerLoadingStatus &&
            p2pInitialLoadingMessage == null
        val networkSpeedText by produceState<String?>(null, playerController, showNetworkSpeed) {
            value = null
            if (!showNetworkSpeed) return@produceState
            var seenTraffic = false
            while (true) {
                val bytesPerSecond = playerController?.networkSpeedBytesPerSecond()
                if (bytesPerSecond != null && bytesPerSecond > 0L) seenTraffic = true
                value = if (bytesPerSecond != null && seenTraffic) formatNetworkSpeed(bytesPerSecond) else null
                delay(1_000L)
            }
        }
        PlayerPlaybackOverlays(
            playerControlsLocked = playerControlsLocked,
            useLegacyLayout = playerSettingsUiState.useLegacyPlayerLayout,
            lockedOverlayVisible = lockedOverlayVisible && !folded,
            showRemainingTime = showRemainingTime,
            playbackSnapshot = overlaySnapshot,
        displayedPositionMs = displayedPositionMs,
        metrics = metrics,
        horizontalSafePadding = horizontalSafePadding,
        onUnlock = { unlockPlayerControls() },
        showOpeningOverlay = openingOverlayVisible,
        backdropArtwork = if (isLiveTvPlayback) activeLogo?.takeIf(String::isNotBlank) ?: background ?: poster else background ?: poster,
        logo = if (isLiveTvPlayback) activeLogo else logo,
        title = if (isLiveTvPlayback) activeStreamTitle else title,
        onBackWithProgress = {
            flushWatchProgress()
            handOffToMiniPlayerIfEnabled()
            args.onBack()
        },
        openingLoadingMessage = if (playerSettingsUiState.showPlayerLoadingStatus) {
            p2pInitialLoadingMessage ?: playerLoadingStatusMessage(
                showStatus = true,
                controllerReady = playerController != null,
                snapshot = playbackSnapshot,
            )
        } else null,
        openingNetworkSpeed = networkSpeedText,
        p2pInitialLoadingProgress = p2pInitialLoadingProgress,
        showP2pRebufferStats = showP2pRebufferStats,
        p2pRebufferMessage = p2pRebufferMessage,
        p2pRebufferProgress = p2pRebufferProgress,
        currentGestureFeedback = currentGestureFeedback,
        renderedGestureFeedback = renderedGestureFeedback,
        initialLoadCompleted = initialLoadCompleted,
        pausedOverlayVisible = playerSettingsUiState.pauseOverlayEnabled && pausedOverlayVisible,
        activeSkipInterval = activeSkipInterval,
        skipsToPostCredits = activeSkipInterval?.internalSkipAction(skipIntervals, snapshotDurationMs)?.skipsToPostCredits == true,
        skipIntervalDismissed = skipIntervalDismissed,
        controlsVisible = controlsVisible,
        onSkipInterval = { interval ->
            val rawMs = (interval.endTime * 1000.0).toLong()
            val durationMs = playbackSnapshot.durationMs
            val seekMs = if (durationMs > 0L) rawMs.coerceAtMost(durationMs - 1) else rawMs
            seekToPosition(seekMs)
            skipIntervalDismissed = true
        },
        sliderEdgePadding = sliderEdgePadding,
        overlayBottomPadding = overlayBottomPadding,
        isSeries = isSeries,
        nextEpisodeInfo = nextEpisodeInfo,
        showNextEpisodeCard = showNextEpisodeCard,
        nextEpisodeSwipeDismissEnabled = playerSettingsUiState.nextEpisodeSwipeDismissEnabled,
        nextEpisodeAutoPlaySearching = nextEpisodeAutoPlaySearching,
        nextEpisodeAutoPlayReady = nextEpisodeAutoPlayReady,
        nextEpisodeAutoPlaySourceName = nextEpisodeAutoPlaySourceName,
        nextEpisodeAutoPlayCountdown = nextEpisodeAutoPlayCountdown,
        blurUnwatchedEpisodes = metaScreenSettingsUiState.blurUnwatchedEpisodes,
        onPlayNextEpisode = {
            playPreparedNextEpisode()
        },
        onDismissNextEpisode = {
            cancelNextEpisodeAutoPlay()
            nextEpisodeCardDismissed = true
            showNextEpisodeCard = false
            nextEpisodeAutoPlaySearching = false
            nextEpisodeAutoPlayReady = false
            nextEpisodeAutoPlaySourceName = null
            nextEpisodeAutoPlayCountdown = null
            pendingNextEpisodeLaunch = false
            pendingNextEpisodeLaunchWithCountdown = false
        },
        errorMessage = errorMessage,
            onDismissError = {
                flushWatchProgress()
                args.onBack()
            },
            onRetryError = {
                playerController?.retry()
                errorMessage = null
                initialLoadCompleted = false
                controlsVisible = !playerControlsLocked
            },
        )
    }
}

private fun PlayerScreenRuntime.openQualityPanel() {
    showQualityPanel = true
    showSourcesPanel = false
    showEpisodesPanel = false
    showAudioModal = false
    showSubtitleModal = false
    showVideoSettingsModal = false
    showLiveTvChannelsPanel = false
    controlsVisible = false
}

private fun PlayerScreenRuntime.playerQualityControlLabel(): String {
    if (playerQualityState.isLoading) return playbackResolutionLabel(forButton = true) ?: "Quality"
    val label = playerQualityState.labelFor(selectedPlayerQualityId, forButton = true)
    if (!label.isNullOrBlank()) {
        return if (selectedPlayerQualityId == null && playerQualityState.hasSelectableQualities) {
            "Auto $label"
        } else {
            label
        }
    }
    return playbackResolutionLabel(forButton = true) ?: "Quality"
}

private fun PlayerScreenRuntime.currentQualityPanelResolutionLabel(): String? {
    playbackResolutionLabel(forButton = false)?.let { return it }
    return playerQualityState.labelFor(selectedPlayerQualityId, forButton = false)
}

private fun PlayerScreenRuntime.externalPlayerSourceUrl(): String {
    if (activeTorrentInfoHash != null) {
        return p2pResolvedSourceUrl ?: activeSourceUrl
    }
    val selectedVariantUrl = selectedPlayerQualityId
        ?.let { selectedId -> playerQualityState.variants.firstOrNull { it.id == selectedId } }
        ?.absoluteUri
    return selectedVariantUrl ?: activeSourceUrl
}

private fun PlayerScreenRuntime.playbackResolutionLabel(forButton: Boolean): String? =
    playerQualityNameForResolution(
        width = playbackSnapshot.videoWidth,
        height = playbackSnapshot.videoHeight,
        forButton = forButton,
    )

private fun PlayerScreenRuntime.selectPlayerQuality(qualityId: String?) {
    val playbackUrl = playerQualityState.playbackUrlFor(qualityId) ?: return
    if (activePlaybackSourceUrl == playbackUrl && selectedPlayerQualityId == qualityId) {
        showQualityPanel = false
        controlsVisible = true
        return
    }

    val resumePositionMs = playbackSnapshot.positionMs.coerceAtLeast(0L)
    audioTracks.firstOrNull { it.index == selectedAudioIndex || it.isSelected }?.let { currentAudio ->
        persistAudioPreference(currentAudio)
    }
    rememberPlaybackSpeedForSourceReload()
    selectedPlayerQualityId = qualityId
    activePlaybackSourceUrl = playbackUrl
    activeInitialPositionMs = resumePositionMs
    activeInitialProgressFraction = null
    initialSeekApplied = resumePositionMs <= 0L
    shouldPlay = true
    playerController = null
    playerControllerSourceUrl = null
    playbackSnapshot = PlayerPlaybackSnapshot()
    initialLoadCompleted = false
    trackPreferenceRestoreApplied = false
    preferredAudioSelectionApplied = false
    preferredSubtitleSelectionApplied = false
    showQualityPanel = false
    controlsVisible = true
}

private fun PlayerScreenRuntime.rememberPlaybackSpeedForSourceReload() {
    val stableSpeed = speedBoostRestoreSpeed ?: playbackSnapshot.playbackSpeed
    pendingPlaybackSpeedRestore = stableSpeed.takeIf { kotlin.math.abs(it - 1f) > 0.01f }
    speedBoostRestoreSpeed = null
    isHoldToSpeedGestureActive = false
}

@Composable
private fun PlayerScreenRuntime.RenderPlayerModals() {
    val displayedPositionMs = if (showChaptersPanel || showSubmitIntroModal) {
        scrubbingPositionMs ?: playbackSnapshot.positionMs
    } else {
        Snapshot.withoutReadObservation { scrubbingPositionMs ?: playbackSnapshot.positionMs }
    }
    val snapshotMediaInfoJson by remember(this) { derivedStateOf { playbackSnapshot.mediaInfoJson } }
    PlayerChaptersPanel(
        visible = showChaptersPanel && chapters.isNotEmpty(),
        chapters = chapters,
        positionMs = displayedPositionMs,
        durationMs = playbackSnapshot.durationMs,
        onChapterSelected = { selectChapter(it) },
        onDismiss = {
            showChaptersPanel = false
            controlsVisible = true
        },
    )
    PlayerDiscTitlesPanel(
        visible = showDiscTitlesPanel && playbackSnapshot.discTitles.size > 1,
        titles = playbackSnapshot.discTitles,
        activeIndex = playbackSnapshot.activeDiscTitleIndex,
        onTitleSelected = { selectDiscTitle(it) },
        onDismiss = {
            showDiscTitlesPanel = false
            controlsVisible = true
        },
    )
    PlayerScreenModalHosts(
        pendingP2pSwitch = pendingP2pSwitch,
        onPendingP2pSwitchChanged = { pendingP2pSwitch = it },
        onP2pEpisodeStreamSelected = { stream, episode, isAutoPlay ->
            switchToP2pEpisodeStream(stream, episode, isAutoPlay)
        },
        onP2pSourceStreamSelected = { stream -> switchToP2pSourceStream(stream) },
        onNextEpisodeAutoPlaySearchingChanged = { nextEpisodeAutoPlaySearching = it },
        onNextEpisodeAutoPlayCountdownChanged = { nextEpisodeAutoPlayCountdown = it },
        onNextEpisodeAutoPlaySourceNameChanged = { nextEpisodeAutoPlaySourceName = it },
        showAudioModal = showAudioModal,
        audioTracks = audioTracks,
        selectedAudioIndex = selectedAudioIndex,
        audioSelectorStyle = nuvioEnhancedSettingsUiState.audioSelectorStyle,
        onAudioTrackSelected = { index ->
            if (serverAudioTracks.isNotEmpty()) {
                selectServerAudioTrack(index)
            } else {
                selectedAudioIndex = index
                persistAudioPreference(audioTracks.firstOrNull { it.index == index })
                playerController?.selectAudioTrack(index)
            }
            scope.launch {
                kotlinx.coroutines.delay(200)
                showAudioModal = false
            }
        },
        onAudioModalDismissed = { showAudioModal = false },
        showAudioDelay = playerController?.supportsAudioDelay == true,
        audioDelayMs = audioDelayMs,
        onAudioDelayChanged = { delayMs -> setAudioDelay(delayMs) },
        showAudioAmplification = playerController?.supportsAudioAmplification == true,
        audioAmplificationDb = audioAmplificationDb,
        keepAudioAmplification = playerSettingsUiState.persistAudioAmplification,
        onAudioAmplificationChanged = { db -> setAudioAmplification(db) },
        onKeepAudioAmplificationChanged = { keep -> setKeepAudioAmplification(keep) },
        showCenterMix = centerMixAvailable,
        centerMixLevelDb = centerMixLevelDb,
        onCenterMixLevelChanged = { db -> setCenterMixLevel(db) },
        showSubtitleModal = showSubtitleModal,
        activeSubtitleTab = activeSubtitleTab,
        subtitleSelectorStyle = nuvioEnhancedSettingsUiState.subtitleSelectorStyle,
        subtitleTracks = subtitleTracks,
        selectedSubtitleIndex = selectedSubtitleIndex,
        addonSubtitles = visibleAddonSubtitles,
        selectedAddonSubtitleId = selectedAddonSubtitleId,
        isLoadingAddonSubtitles = isLoadingAddonSubtitles,
        subtitleStyle = subtitleStyle,
        subtitleDelayMs = subtitleDelayMs,
        selectedAddonSubtitle = selectedAddonSubtitle,
        subtitleAutoSyncState = subtitleAutoSyncState,
        onSubtitleTabSelected = { tab ->
            activeSubtitleTab = tab
            if (tab == SubtitleTab.Sync) {
                loadSubtitleAutoSyncCues()
            }
        },
        subtitleSyncEnabled = nuvioEnhancedSettingsUiState.enhancedHomeFeaturesEnabled &&
            playerSettingsUiState.subtitleSyncMenuEnabled,
        currentPlaybackPositionMs = playbackSnapshot.positionMs,
        isPlaying = playbackSnapshot.isPlaying,
         onBuiltInSubtitleTrackSelected = { index ->
             val wasCustom = useCustomSubtitles
              selectedSubtitleIndex = index
              selectedAddonSubtitleId = null
              useCustomSubtitles = false
              autoAddonFallbackPending = false
             manualSubtitleSelectionLocked = true
             // A manual choice must survive track refreshes caused by seeking.
             trackPreferenceRestoreApplied = true
             preferredSubtitleSelectionApplied = true
             persistInternalSubtitlePreference(subtitleTracks.firstOrNull { it.index == index })
             if (wasCustom) {
                 playerController?.clearExternalSubtitleAndSelect(index)
            } else {
                val wasCustom = useCustomSubtitles
                isUserExplicitSubtitleSelection = true
                preferredSubtitleSelectionApplied = true
                selectedSubtitleIndex = index
                selectedAddonSubtitleId = null
                useCustomSubtitles = false
                persistInternalSubtitlePreference(subtitleTracks.firstOrNull { it.index == index })
                if (wasCustom) {
                    playerController?.clearExternalSubtitleAndSelect(index)
                } else {
                    playerController?.selectSubtitleTrack(index)
                }
                if (hasBurnedInServerSubtitle) clearServerSubtitleTrack()
            }
        },
         onAddonSubtitleSelected = { addon ->
              selectedAddonSubtitleId = addon.selectionKey
              selectedSubtitleIndex = -1
              useCustomSubtitles = true
              autoAddonFallbackPending = false
             manualSubtitleSelectionLocked = true
             // Do not let the next player refresh replace a manual addon choice.
             trackPreferenceRestoreApplied = true
             preferredSubtitleSelectionApplied = true
             persistAddonSubtitlePreference(addon)
             attachSelectedAddonSubtitleWithAutoSync(addon.url)
        },
        onFetchAddonSubtitles = { fetchAddonSubtitlesForActiveItem() },
        onSubtitleStyleChanged = PlayerSettingsRepository::updateSubtitleStyle,
        onSubtitleDelayChanged = { delayMs -> setSubtitleDelay(delayMs) },
        onSubtitleDelayReset = { setSubtitleDelay(0) },
        onSyncByEarClick = { openSubtitleSyncByEar() },
        onAutoSyncCapture = { captureSubtitleAutoSyncTime() },
        onSyncByEarClick = { openSubtitleSyncByEar() },
        onAutoSyncCueSelected = { cue -> applySubtitleAutoSyncCue(cue) },
        onAutoSyncReload = { loadSubtitleAutoSyncCues(force = true) },
        onTogglePlayback = { togglePlayback() },
        onSubtitleModalDismissed = { showSubtitleModal = false },
        showVideoSettingsModal = showVideoSettingsModal,
        playerSettings = playerSettingsUiState,
        onVideoSettingsChanged = {
            playerController?.configureIosVideoOutput(PlayerSettingsRepository.uiState.value)
        },
        onVideoSettingsModalDismissed = { showVideoSettingsModal = false },
        showQualityPanel = showQualityPanel,
        playerQualityState = playerQualityState,
        selectedPlayerQualityId = selectedPlayerQualityId,
        currentQualityLabel = currentQualityPanelResolutionLabel(),
        onPlayerQualitySelected = { qualityId -> selectPlayerQuality(qualityId) },
        onQualityPanelDismissed = {
            showQualityPanel = false
            controlsVisible = true
        },
        showSourcesPanel = showSourcesPanel,
        sourceStreamsState = sourceStreamsState,
        activeSourceUrl = activeSourceUrl,
        activeStreamTitle = activeStreamTitle,
        onSourceFilterSelected = PlayerStreamsRepository::selectSourceFilter,
        onSourceStreamSelected = { stream -> switchToSource(stream) },
        onReloadSources = {
            val vid = activeVideoId
            if (vid != null) {
                PlayerStreamsRepository.loadSources(
                    type = contentType ?: parentMetaType,
                    videoId = vid,
                    parentMetaId = parentMetaId,
                    parentMetaType = parentMetaType,
                    season = activeSeasonNumber,
                    episode = activeEpisodeNumber,
                    forceRefresh = true,
                )
            }
        },
        onSourcesPanelDismissed = {
            showSourcesPanel = false
            PlayerStreamsRepository.stopSourcesLoading()
            controlsVisible = true
        },
        showLiveChannelsPanel = showLiveChannelsPanel,
        liveTvChannels = liveTvUiState.channels,
        activeLiveChannelId = activeVideoId,
        onLiveChannelSelected = { channel -> switchToLiveChannel(channel) },
        onLiveChannelsPanelDismissed = {
            showLiveChannelsPanel = false
            controlsVisible = true
        },
        isSeries = isSeries,
        showEpisodesPanel = showEpisodesPanel,
        allEpisodes = playerMetaVideos,
        parentMetaType = parentMetaType,
        parentMetaId = parentMetaId,
        activeSeasonNumber = activeSeasonNumber,
        activeEpisodeNumber = activeEpisodeNumber,
        watchProgressByVideoId = watchProgressUiState.byVideoId,
        watchedKeys = watchedUiState.watchedKeys,
        blurUnwatchedEpisodes = metaScreenSettingsUiState.blurUnwatchedEpisodes,
        episodeStreamsPanelState = episodeStreamsPanelState,
        episodeStreamsRepoState = episodeStreamsRepoState,
        onEpisodeSelectedForDownload = { episode ->
            selectDownloadedEpisodeForPlayback(
                parentMetaId = parentMetaId,
                episode = episode,
                onDownloadedEpisodeSelected = { item, video -> switchToDownloadedEpisode(item, video) },
            )
        },
        onEpisodeStreamsRequested = { episode ->
            PlayerStreamsRepository.loadEpisodeStreams(
                type = contentType ?: parentMetaType,
                videoId = episode.id,
                parentMetaId = parentMetaId,
                parentMetaType = parentMetaType,
                season = episode.season,
                episode = episode.episode,
            )
            episodeStreamsPanelState = EpisodeStreamsPanelState(showStreams = true, selectedEpisode = episode)
        },
        onEpisodeStreamFilterSelected = PlayerStreamsRepository::selectEpisodeStreamsFilter,
        onEpisodeStreamSelected = { stream, episode ->
            switchToEpisodeStream(
                stream,
                episode,
                saveForReuse = stream != PartyRepository.sharedStream.value?.toStreamItem(),
            )
        },
        onBackToEpisodes = {
            episodeStreamsPanelState = EpisodeStreamsPanelState()
            PlayerStreamsRepository.clearEpisodeStreams()
        },
        onReloadEpisodeStreams = {
            val episode = episodeStreamsPanelState.selectedEpisode
            if (episode != null) {
                PlayerStreamsRepository.loadEpisodeStreams(
                    type = contentType ?: parentMetaType,
                    videoId = episode.id,
                    parentMetaId = parentMetaId,
                    parentMetaType = parentMetaType,
                    season = episode.season,
                    episode = episode.episode,
                    forceRefresh = true,
                )
            }
        },
        onEpisodesPanelDismissed = {
            showEpisodesPanel = false
            episodeStreamsPanelState = EpisodeStreamsPanelState()
            PlayerStreamsRepository.clearEpisodeStreams()
            controlsVisible = true
        },
        showSubmitIntroModal = showSubmitIntroModal,
        activeVideoId = activeVideoId,
        metaUiState = metaUiState,
        displayedPositionMs = displayedPositionMs,
        submitIntroSegmentType = submitIntroSegmentType,
        onSubmitIntroSegmentTypeChanged = { submitIntroSegmentType = it },
        submitIntroStartTimeStr = submitIntroStartTimeStr,
        onSubmitIntroStartTimeChanged = { submitIntroStartTimeStr = it },
        submitIntroEndTimeStr = submitIntroEndTimeStr,
        onSubmitIntroEndTimeChanged = { submitIntroEndTimeStr = it },
        onSubmitIntroDismissed = { showSubmitIntroModal = false },
        onSubmitIntroSuccess = {
            InAppLogger.info(
                "Player/SkipIntro",
                "submit success videoId=${activeVideoId.orEmpty()} s=${activeSeasonNumber ?: 0} " +
                    "e=${activeEpisodeNumber ?: 0} type=$submitIntroSegmentType " +
                    "start=$submitIntroStartTimeStr end=$submitIntroEndTimeStr",
            )
            submitIntroStartTimeStr = "00:00"
            submitIntroEndTimeStr = "00:00"
            submitIntroSegmentType = "intro"
            showSubmitIntroModal = false
        },
        showStreamInfoModal = showStreamInfoModal,
        mediaInfoJson = snapshotMediaInfoJson,
        onStreamInfoModalDismissed = { showStreamInfoModal = false },
    )

    if (showUserRatingSheet) {
        currentUserRatingTarget()?.let { target ->
            UserRatingPlayerOverlay(
                target = target,
                title = activeEpisodeTitle?.takeIf { target.episode != null && it.isNotBlank() } ?: title,
                subtitle = if (target.episode != null) {
                    listOfNotNull(
                        title,
                        localizedSeasonEpisodeCode(target.season, target.episode),
                    ).joinToString(" · ")
                } else {
                    null
                },
                onDismiss = {
                    showUserRatingSheet = false
                    controlsVisible = true
                },
            )
        }
    }
    StreamInfoOverlay(
        visible = showStreamInfo,
        addonName = activeProviderName,
        addonLogo = activeAddonLogo,
        streamName = activeStreamTitle,
        streamDescription = activeStreamSubtitle,
        playbackEngine = playerController?.playbackEngine,
        serverPlayback = serverPlaybackSummary(),
        mediaInfo = streamMediaInfo,
        audioTrack = audioTracks.firstOrNull { it.index == selectedAudioIndex },
        subtitleTrack = subtitleTracks.firstOrNull { it.index == selectedSubtitleIndex }.takeIf { !useCustomSubtitles },
        addonSubtitle = selectedAddonSubtitle.takeIf { useCustomSubtitles },
        onDismiss = { showStreamInfo = false },
    )
    if (isLiveTv) {
        LiveTvChannelsPanel(
            visible = showLiveTvChannelsPanel,
            currentStreamUrl = activeSourceUrl,
            onChannelSelected = { channel -> switchToLiveTvChannel(channel) },
            onDismiss = {
                showLiveTvChannelsPanel = false
                controlsVisible = true
            },
        )
    }
}

/** The movie or episode currently playing, for in-player rating. */
@Composable
private fun PlayerScreenRuntime.currentUserRatingTarget(): TrackingRatingTarget? =
    remember(parentMetaId, contentType, parentMetaType, activeVideoId, activeSeasonNumber, activeEpisodeNumber) {
        if (isLiveTvPlayback) null else currentTrackingMedia().toUserRatingTarget()
    }

private const val MOVIE_RECOMMENDATION_LIMIT = 10

internal val PlayerScreenRuntime.movieRecommendationCandidates: List<MetaPreview>
    get() {
        val watchedKeys = watchedUiState.watchedKeys
        return (playerMoreLikeThis ?: playerMeta?.moreLikeThis).orEmpty()
            .asSequence()
            .filterNot { it.id == parentMetaId }
            .filterNot { WatchingState.isPosterWatched(watchedKeys = watchedKeys, item = it) }
            .take(MOVIE_RECOMMENDATION_LIMIT)
            .toList()
    }

