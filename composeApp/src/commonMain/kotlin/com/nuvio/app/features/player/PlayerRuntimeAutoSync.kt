package com.nuvio.app.features.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import com.nuvio.app.features.autosync.AutoSyncPlayerController
import com.nuvio.app.features.autosync.AutoSyncPreferencesRepository
import com.nuvio.app.features.autosync.AutoSyncSubtitleCandidate
import com.nuvio.app.features.streams.StreamSubtitle

private fun List<AddonSubtitle>.toAutoSyncCandidates(): List<AutoSyncSubtitleCandidate> =
    map { subtitle ->
        AutoSyncSubtitleCandidate(
            url = subtitle.url,
            language = subtitle.language,
            name = subtitle.display,
        )
    }

internal fun buildAutoSyncCandidates(
    addonSubtitles: List<AddonSubtitle>,
    streamSubtitles: List<StreamSubtitle>,
): List<AutoSyncSubtitleCandidate> =
    (addonSubtitles.toAutoSyncCandidates() + streamSubtitles.map { subtitle ->
        AutoSyncSubtitleCandidate(
            url = subtitle.url,
            language = subtitle.language,
            name = subtitle.name,
        )
    }).distinctBy { it.url }

private fun PlayerScreenRuntime.currentAutoSyncCandidates(): List<AutoSyncSubtitleCandidate> =
    buildAutoSyncCandidates(addonSubtitles, activeExternalSubtitles)

internal fun shouldRetryPreferredSubtitleAfterAddonLoad(
    isAutoSyncController: Boolean, playbackLoading: Boolean, startupAutoSyncEnabled: Boolean,
    addonLoading: Boolean, addonSubtitleCount: Int, preferredSelectionApplied: Boolean,
    manualSelectionLocked: Boolean, selectedSubtitleIndex: Int, selectedAddonSubtitleId: String?,
): Boolean =
    isAutoSyncController && !playbackLoading && startupAutoSyncEnabled &&
        !addonLoading && addonSubtitleCount > 0 && preferredSelectionApplied &&
        !manualSelectionLocked && selectedSubtitleIndex < 0 && selectedAddonSubtitleId == null

internal fun PlayerScreenRuntime.configureAutoSyncController(controller: PlayerEngineController) {
    controller.setAutoSyncSubtitleCandidates(currentAutoSyncCandidates())
    controller.setAutoSyncAppliedListener { subtitleUrl, delayMs ->
        val appliedSubtitle = addonSubtitles.firstOrNull { it.url == subtitleUrl }
        selectedAddonSubtitleId = appliedSubtitle?.selectionKey ?: subtitleUrl
        selectedSubtitleIndex = -1
        useCustomSubtitles = true
        preferredSubtitleSelectionApplied = true
        if (appliedSubtitle != null && manualSubtitleSelectionLocked) {
            persistAddonSubtitlePreference(appliedSubtitle)
        }
        val appliedDelayMs = delayMs.coerceIn(SUBTITLE_DELAY_MIN_MS, SUBTITLE_DELAY_MAX_MS)
        subtitleDelayMs = appliedDelayMs
        PlayerTrackPreferenceStorage.saveSubtitleDelayMs(playbackSession.videoId, appliedDelayMs)
    }
}

@Composable
internal fun PlayerScreenRuntime.BindAutoSyncRuntimeEffects() {
    val activeController = playerController
    DisposableEffect(activeController) {
        activeController?.let { configureAutoSyncController(it) }
        onDispose { activeController?.setAutoSyncAppliedListener(null) }
    }
    LaunchedEffect(playerController, addonSubtitles, activeExternalSubtitles) {
        playerController?.setAutoSyncSubtitleCandidates(currentAutoSyncCandidates())
    }
    LaunchedEffect(playerController, playbackSnapshot.isLoading, isLoadingAddonSubtitles, addonSubtitles) {
        // Add-ons can finish after the initial player track scan found no subtitle.
        // Retry only for AutoSync and only while the user has not made an explicit choice.
        if (!shouldRetryPreferredSubtitleAfterAddonLoad(
                isAutoSyncController = playerController is AutoSyncPlayerController,
                playbackLoading = playbackSnapshot.isLoading,
                startupAutoSyncEnabled = AutoSyncPreferencesRepository.preferredSubtitleAutoSyncOnStart.value,
                addonLoading = isLoadingAddonSubtitles,
                addonSubtitleCount = addonSubtitles.size,
                preferredSelectionApplied = preferredSubtitleSelectionApplied,
                manualSelectionLocked = manualSubtitleSelectionLocked,
                selectedSubtitleIndex = selectedSubtitleIndex,
                selectedAddonSubtitleId = selectedAddonSubtitleId,
            )
        ) return@LaunchedEffect
        preferredSubtitleSelectionApplied = false
        refreshTracks()
    }
}

internal fun PlayerScreenRuntime.attachSelectedAddonSubtitleWithAutoSync(url: String) {
    subtitleAutoSyncState = SubtitleAutoSyncUiState()
    playerController?.setSubtitleUriWithSelectedAutoSync(url)
}

internal fun PlayerScreenRuntime.maybeAutoSyncPreferredSubtitleAtStart(subtitle: AddonSubtitle): Boolean {
    if (manualSubtitleSelectionLocked) return false
    val preferredLanguage = normalizeLanguageCode(playerSettingsUiState.preferredSubtitleLanguage) ?: return false
    if (
        preferredLanguage.isBlank() ||
        preferredLanguage == SubtitleLanguageOption.NONE ||
        preferredLanguage == SubtitleLanguageOption.FORCED
    ) return false
    val controller = playerController ?: return false
    if (controller !is AutoSyncPlayerController) return false
    val playbackKey = activeSourceIdentityKey ?: activeSourceUrl
    if (!AutoSyncPreferencesRepository.claimStartupRun(hashCode(), playbackKey)) return false
    controller.setSubtitleUriWithAutoSync(subtitle.url)
    return true
}
