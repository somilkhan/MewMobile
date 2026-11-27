// Mew-only next-episode helpers. Upstream effects call these through `// Mew hook` lines.
package com.nuvio.app.features.player

import com.nuvio.app.features.downloads.DownloadsRepository

/**
 * Mew: true when the next episode can start from an offline download or a stream that Mew
 * already pre-resolved for the Next button. In that case the Mew prepared flow is used for
 * auto-play; otherwise upstream's automatic next-episode search runs.
 */
internal fun PlayerScreenRuntime.hasPreparedNextEpisodeSource(): Boolean {
    val next = nextEpisodeInfo ?: return false
    if (preloadedNextEpisodeStream != null && preloadedNextEpisodeVideoId == next.videoId) return true
    val nextVideo = playerMetaVideos.firstOrNull { video -> video.id == next.videoId } ?: return false
    return DownloadsRepository.findPlayableDownload(
        parentMetaId = parentMetaId,
        seasonNumber = nextVideo.season,
        episodeNumber = nextVideo.episode,
        videoId = nextVideo.id,
    ) != null
}
