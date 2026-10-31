package com.nuvio.app.features.simkl

/**
 * Simkl may briefly return a stale playback position immediately after a pause or stop write.
 * Keep a locally newer row only when the next server response still contains that same playback;
 * the account remains authoritative for rows it no longer returns.
 */
internal fun mergeFetchedPlayback(
    fetched: List<SimklPlaybackSession>,
    held: List<SimklPlaybackSession>,
): List<SimklPlaybackSession> {
    if (held.isEmpty()) return fetched

    val fetchedByKey = fetched.mapNotNull { session ->
        session.playbackKey()?.let { key -> key to session }
    }.toMap()
    val kept = held.filter { local ->
        val key = local.playbackKey() ?: return@filter false
        val incoming = fetchedByKey[key] ?: return@filter false
        (local.playedAtEpochMs() ?: 0L) > (incoming.playedAtEpochMs() ?: 0L)
    }
    if (kept.isEmpty()) return fetched

    val keptKeys = kept.mapNotNullTo(mutableSetOf()) { it.playbackKey() }
    return fetched.filterNot { it.playbackKey() in keptKeys } + kept
}

private fun SimklPlaybackSession.playbackKey(): String? {
    val media = media ?: return null
    val contentId = media.ids.simklIdValue()
        ?: media.ids.idValue("imdb")
        ?: media.ids.idValue("tvdb")
        ?: media.title
        ?: return null
    val season = episode?.tvdbSeason ?: episode?.season ?: -1
    val number = episode?.tvdbNumber ?: episode?.number ?: -1
    return "$contentId|$season|$number"
}

private fun SimklPlaybackSession.playedAtEpochMs(): Long? =
    (pausedAt ?: watchedAt)?.let(::parseSimklUtcEpochMs)
