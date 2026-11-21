package com.nuvio.app.features.downloads

/**
 * Decides which downloads may run, given concurrency limits and network policy.
 *
 * Ported from luqmanfadlli 1b9f21f0b. Adapted for Mew: waiting downloads use
 * [DownloadStatus.Waiting] (Mew has no Queued status), and the limits and network
 * inputs are plain values supplied by the caller.
 */
internal data class DownloadQueuePolicy(
    val maxDownloads: Int,
    val maxEpisodesPerShow: Int,
    val maxShows: Int,
    val isNetworkAllowed: Boolean,
) {
    /** Whether a download for [showKey] can start next to [downloading] without exceeding the limits. */
    fun canStart(showKey: String?, downloading: List<DownloadItem>): Boolean {
        if (!isNetworkAllowed) return false
        if (downloading.size >= maxDownloads) return false
        if (showKey == null) return true
        val activeShows = downloading.mapNotNullTo(mutableSetOf()) { it.showKey }
        if (showKey !in activeShows && activeShows.size >= maxShows) return false
        return downloading.count { it.showKey == showKey } < maxEpisodesPerShow
    }

    /** The oldest waiting download that may start now, or null when none can. */
    fun nextToStart(items: List<DownloadItem>): DownloadItem? {
        val downloading = items.filter { it.status == DownloadStatus.Downloading }
        return items
            .filter { it.status == DownloadStatus.Waiting }
            .sortedWith(queueOrder)
            .firstOrNull { canStart(it.showKey, downloading) }
    }

    companion object {
        private val queueOrder = compareBy<DownloadItem> { it.createdAtEpochMs }
            .thenBy { it.seasonNumber ?: 0 }
            .thenBy { it.episodeNumber ?: 0 }
            .thenBy { it.id }

        /** Policy for Mew's one-at-a-time queue: a single active download at a time. */
        val oneAtATime: DownloadQueuePolicy = DownloadQueuePolicy(
            maxDownloads = 1,
            maxEpisodesPerShow = 1,
            maxShows = 1,
            isNetworkAllowed = true,
        )
    }
}

/** The show an episode download counts against for per-show limits; null for movies. */
internal val DownloadItem.showKey: String?
    get() = parentMetaId.takeIf { isEpisode && it.isNotBlank() }
