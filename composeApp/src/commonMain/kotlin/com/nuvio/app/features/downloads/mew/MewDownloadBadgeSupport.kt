package com.nuvio.app.features.downloads

// Mew adapter: upstream (luq) episode download badges on the details screen
// are wired to Mew's downloads model. Mew keeps its own downloads module, so the
// small upstream helpers those badges need live here instead of upstream files.

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** One title's downloads, indexed for per-episode lookup. */
class EpisodeDownloadIndex private constructor(
    private val byVideoId: Map<String, List<DownloadItem>>,
    private val byEpisode: Map<Pair<Int, Int>, List<DownloadItem>>,
    private val movieItems: List<DownloadItem>,
) {
    fun find(seasonNumber: Int?, episodeNumber: Int?, videoId: String? = null): DownloadItem? {
        val byId = videoId?.trim()?.takeIf { it.isNotEmpty() }?.let { byVideoId[it] }.orEmpty()
        val byNumber = when {
            seasonNumber != null && episodeNumber != null -> byEpisode[seasonNumber to episodeNumber].orEmpty()
            seasonNumber == null && episodeNumber == null -> movieItems
            else -> emptyList()
        }
        return preferred((byId + byNumber).distinctBy { it.id })
    }

    companion object {
        fun build(items: List<DownloadItem>, parentMetaId: String): EpisodeDownloadIndex {
            val normalizedParentMetaId = parentMetaId.trim()
            val titleItems = items.filter { it.parentMetaId.trim() == normalizedParentMetaId }
            return EpisodeDownloadIndex(
                byVideoId = titleItems.filter { it.videoId.isNotBlank() }.groupBy { it.videoId.trim() },
                byEpisode = titleItems
                    .filter { it.isEpisode }
                    .groupBy { checkNotNull(it.seasonNumber) to checkNotNull(it.episodeNumber) },
                movieItems = titleItems.filter { it.seasonNumber == null && it.episodeNumber == null },
            )
        }

        private fun preferred(matches: List<DownloadItem>): DownloadItem? =
            matches.firstOrNull { it.status == DownloadStatus.Downloading }
                ?: matches.firstOrNull { it.status == DownloadStatus.Waiting }
                ?: matches.firstOrNull { it.status == DownloadStatus.Completed }
                ?: matches.firstOrNull()
    }
}

@Composable
internal fun animatedDownloadProgress(progress: Float): Float {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 400),
        label = "downloadProgress",
    )
    return animated
}

@Composable
internal fun DownloadProgressSpinner(
    progress: Float?,
    color: Color,
    size: Dp = 10.dp,
    strokeWidth: Dp = 2.dp,
) {
    if (progress != null) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.size(size),
            strokeWidth = strokeWidth,
            color = color,
            trackColor = color.copy(alpha = 0.3f),
        )
    } else {
        CircularProgressIndicator(
            modifier = Modifier.size(size),
            strokeWidth = strokeWidth,
            color = color,
        )
    }
}

internal val DownloadItem.knownProgress: Float?
    get() = progressFraction.takeIf { totalBytes != null && totalBytes > 0L }

internal fun formatDownloadBadgeProgress(downloadedBytes: Long, totalBytes: Long?): String {
    if (totalBytes == null || totalBytes <= 0L) return formatBytes(downloadedBytes)
    return "${formatBytes(downloadedBytes)}/${formatBytes(totalBytes)}"
}
