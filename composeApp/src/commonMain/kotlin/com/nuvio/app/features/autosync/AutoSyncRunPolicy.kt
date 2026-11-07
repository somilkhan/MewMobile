package com.nuvio.app.features.autosync

internal enum class AutoSyncCandidateScope {
    STARTUP_SEARCH,
    SELECTED_ONLY,
}

/**
 * The preference gates automatic startup selection only. An explicit user selection is an
 * intentional AutoSync request and must still run when startup AutoSync is disabled.
 */
internal fun AutoSyncCandidateScope.shouldRunAutoSync(
    preferredSubtitleAutoSyncOnStart: Boolean,
): Boolean = this == AutoSyncCandidateScope.SELECTED_ONLY || preferredSubtitleAutoSyncOnStart

internal fun AutoSyncCandidateScope.alternativeCandidates(
    candidates: List<AutoSyncSubtitleCandidate>,
): List<AutoSyncSubtitleCandidate> =
    if (this == AutoSyncCandidateScope.STARTUP_SEARCH) candidates else emptyList()

internal val AutoSyncCandidateScope.usesAlternativeProvider: Boolean
    get() = this == AutoSyncCandidateScope.STARTUP_SEARCH

internal fun shouldRestoreOriginalSubtitle(
    activeSidecarSubtitleKey: String?,
): Boolean = activeSidecarSubtitleKey == null
