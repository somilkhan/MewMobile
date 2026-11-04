package com.nuvio.app.features.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.BoxScope
import com.nuvio.app.features.party.PartyMedia
import com.nuvio.app.features.party.PartyPlayback
import com.nuvio.app.features.party.PartyStream
import com.nuvio.app.features.party.isShareableStreamUrl
import com.nuvio.app.features.party.PartyRepository
import com.nuvio.app.features.party.PartySource
import com.nuvio.app.features.party.PartyPanel
import com.nuvio.app.features.party.PartySync
import com.nuvio.app.features.party.PartyMemberStatus
import com.nuvio.app.features.party.PartyCorrection
import com.nuvio.app.features.party.PartyEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

internal fun PlayerScreenRuntime.partyMedia(): PartyMedia = PartyMedia(
    type = contentType ?: parentMetaType,
    metaId = parentMetaId,
    metaType = parentMetaType,
    videoId = activeVideoId,
    season = activeSeasonNumber,
    episode = activeEpisodeNumber,
    title = title,
    episodeTitle = activeEpisodeTitle,
    description = pauseDescription,
    poster = poster,
    background = background,
    logo = logo,
    source = PartySource(
        infoHash = activeTorrentInfoHash,
        fileIdx = activeTorrentFileIdx,
        filename = activeTorrentFilename,
        bingeGroup = currentStreamBingeGroup,
        addonId = activeProviderAddonId,
        name = activeStreamTitle,
    ),
)

internal fun PlayerScreenRuntime.partyStream(): PartyStream? {
    if (activeSourceAudioUrl != null || !isShareableStreamUrl(activeSourceUrl)) return null
    return PartyStream(
        mediaKey = partyMedia().key,
        url = activeSourceUrl,
        headers = activeSourceHeaders,
        responseHeaders = activeSourceResponseHeaders,
        name = activeStreamTitle,
        addonName = activeProviderName,
        addonId = activeProviderAddonId.orEmpty(),
        filename = activeTorrentFilename,
        bingeGroup = currentStreamBingeGroup,
        streamType = activeStreamType,
    )
}

internal fun PlayerScreenRuntime.partyControlBlocked(): Boolean {
    val party = PartyRepository.state.value.party ?: return false
    if (party.media.key != partyMedia().key || party.canControl) return false
    scope.launch { showGestureMessage("Host controls playback") }
    return true
}

@Composable
internal fun PlayerScreenRuntime.BindPartyEffects() {
    val state by PartyRepository.state.collectAsState()
    val party = state.party
    val sharing = party?.let { it.isHost && it.sharing } == true
    val sharedStream = if (sharing) partyStream() else null
    LaunchedEffect(sharing, sharedStream) { if (sharing) PartyRepository.shareStream(sharedStream) }
    LaunchedEffect(party?.id, party?.isHost) {
        if (party == null || party.isHost) return@LaunchedEffect
        PartyRepository.events.collect { event ->
            if (event is PartyEvent.MediaChanged && event.media.key != partyMedia().key) {
                showPartyPanel = false
                args.onOpenPartyMedia?.invoke(event.media)
            }
        }
    }
    LaunchedEffect(party?.id, partyMedia().key) {
        partyBehind = false
        var lastSeq = -1L
        var lastStatus: PartyMemberStatus? = null
        var hostPublishElapsed = 0L
        var seekCooldownTicks = 0
        while (true) {
            delay(400)
            val active = PartyRepository.state.value.party ?: break
            val matchingPlayback = PartyRepository.playback.value?.takeIf {
                it.mediaKey == partyMedia().key && !it.waiting
            }
            val expectedForPresence = matchingPlayback?.let {
                PartySync.expectedPositionMs(it, PartyRepository.nowMs(), playbackSnapshot.durationMs)
            }
            val behindMs = expectedForPresence?.minus(playbackSnapshot.positionMs)
                ?.takeIf { !active.isHost && it > PartySync.SEEK_THRESHOLD_MS }
            partyBehindMs = behindMs
            partyBehind = behindMs != null
            val localStatus = if (active.media.key != partyMedia().key) PartyMemberStatus.Browsing else when {
                errorMessage != null -> PartyMemberStatus.Browsing
                !initialLoadCompleted -> PartyMemberStatus.Loading
                playbackSnapshot.isLoading -> PartyMemberStatus.Buffering
                behindMs != null -> PartyMemberStatus.Behind
                else -> PartyMemberStatus.Ready
            }
            if (localStatus != lastStatus) {
                lastStatus = localStatus
                PartyRepository.setLocalStatus(localStatus, partyMedia().key.takeIf { localStatus != PartyMemberStatus.Browsing })
            }
            if (active.media.key != partyMedia().key || playerController == null || errorMessage != null) continue
            val remote = PartyRepository.playback.value
            if (remote == null) {
                if (active.isHost && initialLoadCompleted) {
                    hostPublishElapsed += 400L
                    if (hostPublishElapsed >= 1_000L) {
                        PartyRepository.publishPlayback(!shouldPlay, playbackSnapshot.positionMs, playbackSnapshot.playbackSpeed)
                        hostPublishElapsed = 0L
                    }
                }
                continue
            }
            if (active.isHost && remote.memberId == active.memberId) {
                hostPublishElapsed += 400L
                if (hostPublishElapsed >= 1_000L) {
                    PartyRepository.publishPlayback(!shouldPlay, playbackSnapshot.positionMs, playbackSnapshot.playbackSpeed)
                    hostPublishElapsed = 0L
                }
                continue
            }
            val expected = PartySync.expectedPositionMs(remote, PartyRepository.nowMs(), playbackSnapshot.durationMs)
            val drift = playbackSnapshot.positionMs - expected
            if (remote.seq != lastSeq) {
                lastSeq = remote.seq
                shouldPlay = !remote.paused
                if (remote.paused) playerController?.pause() else playerController?.play()
                if (abs(drift) > PartySync.PAUSED_TOLERANCE_MS || remote.paused && abs(drift) > 500L) playerController?.seekTo(expected)
                playerController?.setPlaybackSpeed(remote.rate)
                partyRateNudge = false
                seekCooldownTicks = if (abs(drift) > PartySync.PAUSED_TOLERANCE_MS) 6 else 0
            } else if (!isScrubbingTimeline && !playbackSnapshot.isLoading && !playbackSnapshot.isEnded) {
                if (seekCooldownTicks > 0) {
                    seekCooldownTicks--
                    continue
                }
                when (val correction = PartySync.correction(playbackSnapshot.positionMs, expected, remote, partyRateNudge)) {
                    is PartyCorrection.Seek -> {
                        playerController?.seekTo(correction.positionMs)
                        playerController?.setPlaybackSpeed(remote.rate)
                        partyRateNudge = false
                        seekCooldownTicks = 6
                    }
                    is PartyCorrection.Nudge -> {
                        partyRateNudge = true
                        playerController?.setPlaybackSpeed(correction.rate)
                    }
                    PartyCorrection.InSync -> {
                        if (partyRateNudge || abs(playbackSnapshot.playbackSpeed - remote.rate) > 0.02f) playerController?.setPlaybackSpeed(remote.rate)
                        partyRateNudge = false
                    }
                }
            }
        }
    }
}

@Composable
internal fun BoxScope.PlayerScreenRuntime.RenderPartyPanel() {
    val state by PartyRepository.state.collectAsState()
    PartyPanel(
        visible = showPartyPanel,
        state = state,
        showChat = showPartyChat,
        onShowChatChange = { showPartyChat = it },
        playerMedia = partyMedia(),
        isSupported = PartyRepository.available.collectAsState().value,
        canStart = PartyRepository.canUse,
        onStart = { startParty() },
        onSwitchParty = { PartyRepository.publishMedia(partyMedia(), playbackSnapshot.positionMs) },
        onWatchWithParty = {
            val media = PartyRepository.state.value.party?.media
            showPartyPanel = false
            if (media != null) args.onOpenPartyMedia?.invoke(media)
        },
        onLeave = { showPartyPanel = false; PartyRepository.leave() },
        onEnd = { showPartyPanel = false; PartyRepository.end() },
        onRemove = { PartyRepository.removeMember(it.id) },
        onHostOnlyChange = { PartyRepository.setHostOnly(it) },
        canShareStream = partyStream() != null,
        onSharingChange = { PartyRepository.setSharing(it) },
        behindMs = partyBehindMs,
        onCatchUp = {
            val playback = PartyRepository.playback.value
            val target = playback?.takeIf { it.mediaKey == partyMedia().key && !it.waiting }
                ?.let { PartySync.expectedPositionMs(it, PartyRepository.nowMs(), playbackSnapshot.durationMs) }
            if (target != null && playerController != null) {
                playerController?.seekTo(target)
                partyBehind = false
                partyBehindMs = null
                PartyRepository.setLocalStatus(PartyMemberStatus.Ready, partyMedia().key)
                showPartyPanel = false
            }
        },
        onDismiss = { showPartyPanel = false },
    )
}

internal fun PlayerScreenRuntime.startParty() {
    scope.launch {
        val error = PartyRepository.create(partyMedia())
        if (error != null) showGestureMessage(error.name)
        else showPartyPanel = true
    }
}
