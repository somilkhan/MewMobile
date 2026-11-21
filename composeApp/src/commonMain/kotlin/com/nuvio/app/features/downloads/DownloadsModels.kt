package com.nuvio.app.features.downloads

import com.nuvio.app.features.player.SubtitleAddonRequest
import com.nuvio.app.features.streams.StreamSubtitle
import kotlinx.serialization.Serializable
import kotlinx.coroutines.runBlocking
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.downloads_enqueue_missing_url
import nuvio.composeapp.generated.resources.downloads_enqueue_replaced
import nuvio.composeapp.generated.resources.downloads_enqueue_queued
import nuvio.composeapp.generated.resources.downloads_enqueue_started
import nuvio.composeapp.generated.resources.downloads_enqueue_unsupported_format
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.StringResource
import nuvio.composeapp.generated.resources.downloads_resolution_720p
import nuvio.composeapp.generated.resources.downloads_resolution_1080p
import nuvio.composeapp.generated.resources.downloads_resolution_2160p

@Serializable
enum class DownloadStatus {
    Queued,
    Downloading,
    Waiting,
    Paused,
    Completed,
    Failed,
}

enum class DownloadPreferredResolution(
    val resolution: Int,
    val label: StringResource,
) {
    P720(720, Res.string.downloads_resolution_720p),
    P1080(1080, Res.string.downloads_resolution_1080p),
    P2160(2160, Res.string.downloads_resolution_2160p);

    companion object {
        val DEFAULT = P1080

        fun fromStorage(value: String?): DownloadPreferredResolution =
            entries.firstOrNull { it.name == value?.trim() } ?: DEFAULT
    }
}

/** A user-adjustable cap on how many downloads may run at once. */
enum class DownloadConcurrencyLimit(
    val storageKey: String,
    val defaultValue: Int,
) {
    TotalDownloads("max_download_concurrency", 3),
    EpisodesPerShow("max_series_episode_concurrency", 1),
    ConcurrentShows("max_concurrent_shows", 1);

    fun clamp(value: Int): Int = value.coerceIn(1, MAX_VALUE)

    companion object {
        const val MAX_VALUE = 5
    }
}

@Serializable
data class DownloadItem(
    val id: String,
    val contentType: String,
    val parentMetaId: String,
    val parentMetaType: String,
    val videoId: String,
    val title: String,
    val logo: String? = null,
    val poster: String? = null,
    val background: String? = null,
    val seasonNumber: Int? = null,
    val episodeNumber: Int? = null,
    val episodeTitle: String? = null,
    val episodeThumbnail: String? = null,
    val episodeOverview: String? = null,
    val detailsSnapshot: DownloadDetailsSnapshot? = null,
    val streamTitle: String,
    val streamSubtitle: String? = null,
    val providerName: String,
    val providerAddonId: String? = null,
    val externalSubtitles: List<StreamSubtitle> = emptyList(),
    val sourceUrl: String,
    val sourceHeaders: Map<String, String> = emptyMap(),
    val sourceResponseHeaders: Map<String, String> = emptyMap(),
    val subtitleRequests: List<SubtitleAddonRequest> = emptyList(),
    val sourceSubtitles: List<StreamSubtitle> = emptyList(),
    val localFileUri: String? = null,
    val fileName: String,
    val status: DownloadStatus,
    val queueMode: DownloadQueueMode = DownloadQueueMode.AllAtOnce,
    val downloadedBytes: Long = 0L,
    val totalBytes: Long? = null,
    val downloadSpeedBytesPerSecond: Long = 0L,
    val errorMessage: String? = null,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long,
) {
    val contentId: String
        get() = parentMetaId.trim().ifBlank { id }

    val isEpisode: Boolean
        get() = seasonNumber != null && episodeNumber != null

    val isActive: Boolean
        get() = status == DownloadStatus.Downloading || status == DownloadStatus.Queued

    /** Paused or failed: resuming continues a paused download and retries a failed one. */
    val isResumable: Boolean
        get() = status == DownloadStatus.Paused || status == DownloadStatus.Failed

    val isPlayable: Boolean
        get() = status == DownloadStatus.Completed && !localFileUri.isNullOrBlank()

    val displaySubtitle: String
        get() = episodeTitle.orEmpty()

    val progressFraction: Float
        get() {
            val total = totalBytes?.takeIf { it > 0L } ?: return 0f
            return (downloadedBytes.toDouble() / total.toDouble())
                .toFloat()
                .coerceIn(0f, 1f)
        }

    val estimatedRemainingSeconds: Long?
        get() {
            if (status != DownloadStatus.Downloading) return null
            val total = totalBytes?.takeIf { it > 0L } ?: return null
            val speed = downloadSpeedBytesPerSecond.takeIf { it > 0L } ?: return null
            val remainingBytes = (total - downloadedBytes).coerceAtLeast(0L)
            if (remainingBytes <= 0L) return 0L
            return (remainingBytes + speed - 1L) / speed
        }

    val logicalContentKey: String
        get() = if (seasonNumber != null || episodeNumber != null) {
            "${parentMetaId.trim()}|${seasonNumber ?: -1}|${episodeNumber ?: -1}"
        } else {
            "${parentMetaId.trim()}|movie"
        }
}

@Serializable
data class DownloadDetailsSnapshot(
    val id: String,
    val type: String,
    val name: String,
    val aliases: List<String> = emptyList(),
    val poster: String? = null,
    val background: String? = null,
    val logo: String? = null,
    val description: String? = null,
    val releaseInfo: String? = null,
    val lastAirDate: String? = null,
    val status: String? = null,
    val imdbRating: String? = null,
    val ageRating: String? = null,
    val runtime: String? = null,
    val externalRatings: List<DownloadExternalRatingSnapshot> = emptyList(),
    val genres: List<String> = emptyList(),
    val director: List<String> = emptyList(),
    val writer: List<String> = emptyList(),
    val cast: List<DownloadPersonSnapshot> = emptyList(),
    val productionCompanies: List<DownloadCompanySnapshot> = emptyList(),
    val networks: List<DownloadCompanySnapshot> = emptyList(),
    val country: String? = null,
    val awards: String? = null,
    val language: String? = null,
    val website: String? = null,
    val hasScheduledVideos: Boolean = false,
    val videos: List<DownloadVideoSnapshot> = emptyList(),
)

@Serializable
data class DownloadExternalRatingSnapshot(
    val source: String,
    val value: Double,
)

@Serializable
data class DownloadPersonSnapshot(
    val name: String,
    val role: String? = null,
    val photo: String? = null,
    val tmdbId: Int? = null,
)

@Serializable
data class DownloadCompanySnapshot(
    val name: String,
    val logo: String? = null,
    val tmdbId: Int? = null,
)

@Serializable
data class DownloadVideoSnapshot(
    val id: String,
    val title: String,
    val released: String? = null,
    val available: Boolean = true,
    val thumbnail: String? = null,
    val seasonPoster: String? = null,
    val season: Int? = null,
    val episode: Int? = null,
    val overview: String? = null,
    val runtime: Int? = null,
)

data class DownloadsUiState(
    val items: List<DownloadItem> = emptyList(),
    val pendingSourceSearches: List<PendingEpisodeDownload> = emptyList(),
) {
    val activeItems: List<DownloadItem>
        get() = items.filter { it.status != DownloadStatus.Completed }

    val completedItems: List<DownloadItem>
        get() = items.filter { it.status == DownloadStatus.Completed }

    val activeSourceSearches: List<PendingEpisodeDownload>
        get() = pendingSourceSearches.filter { it.status == PendingSourceSearchStatus.Searching }
}

data class PendingShowGroup(
    val parentMetaId: String,
    val parentMetaType: String,
    val title: String,
    val logo: String? = null,
    val poster: String? = null,
    val background: String? = null,
    val searches: List<PendingEpisodeDownload>,
) {
    val contentType: String
        get() = searches.firstOrNull()?.contentType ?: "movie"
}

fun List<PendingEpisodeDownload>.groupByShow(): List<PendingShowGroup> =
    groupBy { it.parentMetaId }
        .map { (showId, searches) ->
            val first = searches.first()
            PendingShowGroup(
                parentMetaId = showId,
                parentMetaType = first.parentMetaType,
                title = first.title,
                logo = first.logo,
                poster = first.poster,
                background = first.background,
                searches = searches.sortedWith(pendingEpisodeComparator),
            )
        }
        .sortedBy { it.title.lowercase() }

internal val pendingEpisodeComparator: Comparator<PendingEpisodeDownload> =
    compareBy<PendingEpisodeDownload> { it.seasonNumber ?: Int.MAX_VALUE }
        .thenBy { it.episodeNumber ?: Int.MAX_VALUE }
        .thenBy { it.episodeTitle?.trim().orEmpty().lowercase() }
        .thenBy { it.title.trim().lowercase() }
        .thenBy { it.id }

internal fun downloadLogicalContentKey(
    parentMetaId: String,
    seasonNumber: Int?,
    episodeNumber: Int?,
): String {
    val season = seasonNumber?.takeIf { it > 0 }
    val episode = episodeNumber?.takeIf { it > 0 }
    return if (season != null || episode != null) {
    "${parentMetaId.trim()}|${season ?: -1}|${episode ?: -1}"
} else {
    "${parentMetaId.trim()}|movie"
}
}

enum class DownloadEnqueueResult {
    Started,
    Replaced,
    Queued,
    MissingUrl,
    UnsupportedFormat;

    val isSuccess: Boolean
        get() = this == Started || this == Replaced || this == Queued

    fun toastMessage(): String = runBlocking {
        when (this@DownloadEnqueueResult) {
            Started -> getString(Res.string.downloads_enqueue_started)
            Replaced -> getString(Res.string.downloads_enqueue_replaced)
            Queued -> getString(Res.string.downloads_enqueue_queued)
            MissingUrl -> getString(Res.string.downloads_enqueue_missing_url)
            UnsupportedFormat -> getString(Res.string.downloads_enqueue_unsupported_format)
        }
    }
}

/** How many downloads are in each unfinished state. */
internal data class DownloadStatusCounts(
    val downloading: Int = 0,
    val queued: Int = 0,
    val paused: Int = 0,
    val failed: Int = 0,
) {
    val hasActive: Boolean get() = downloading > 0 || queued > 0
}

internal fun List<DownloadItem>.statusCounts(): DownloadStatusCounts {
    val byStatus = groupingBy { it.status }.eachCount()
    return DownloadStatusCounts(
        downloading = byStatus[DownloadStatus.Downloading] ?: 0,
        queued = byStatus[DownloadStatus.Queued] ?: 0,
        paused = byStatus[DownloadStatus.Paused] ?: 0,
        failed = byStatus[DownloadStatus.Failed] ?: 0,
    )
}

internal fun List<DownloadItem>.sortedForSeriesDownloads(): List<DownloadItem> =
    sortedWith(downloadSeriesEpisodeComparator)

internal val downloadSeriesEpisodeComparator: Comparator<DownloadItem> =
    compareBy<DownloadItem> { it.seasonNumber ?: Int.MAX_VALUE }
        .thenBy { it.episodeNumber ?: Int.MAX_VALUE }
        .thenBy { it.episodeTitle?.trim().orEmpty().lowercase() }
        .thenBy { it.title.trim().lowercase() }
        .thenBy { it.id }

internal fun DownloadItem.displayTitle(): String =
    if (isEpisode) {
        episodeTitle?.trim()?.takeIf { it.isNotBlank() } ?: title
    } else {
        title
    }

internal fun isDownloadableDirectUrl(url: String?): Boolean =
    url?.trim()?.takeIf { it.isNotBlank() }?.isSupportedDownloadUrl() == true
