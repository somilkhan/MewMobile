package com.nuvio.app.features.tracking

import com.nuvio.app.features.player.skip.SimklIdResolver
import com.nuvio.app.features.watched.WatchedItem
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal data class TrackingAnimeVideoRef(
    val source: String,
    val id: Long,
    val episode: Int,
)

internal fun parseTrackingAnimeVideoId(videoId: String?): TrackingAnimeVideoRef? {
    val parts = videoId?.trim()?.split(':') ?: return null
    if (parts.size < 3) return null
    val source = parts[0].lowercase()
    if (source !in setOf("kitsu", "mal", "anilist", "anidb")) return null
    val id = parts[1].toLongOrNull()?.takeIf { it > 0L } ?: return null
    val episode = parts[2].toIntOrNull()?.takeIf { it > 0 } ?: return null
    return TrackingAnimeVideoRef(source, id, episode)
}

/**
 * Converts anime-specific IDs and episode numbering to IMDb/TV episode references for trackers
 * that expect conventional TV identifiers. Failed lookups leave the original item untouched.
 */
internal object TrackingAnimeIdMapper {
    private data class Mapped(
        val imdb: String,
        val isMovie: Boolean,
        val season: Int? = null,
        val episode: Int? = null,
    )

    private val lock = Mutex()
    private val cache = mutableMapOf<String, Mapped>()
    private const val MAX_CACHE_ENTRIES = 512
    private val animePrefixes = setOf("kitsu", "mal", "anilist", "anidb")

    suspend fun toImdbWatchedItems(items: Collection<WatchedItem>): List<WatchedItem> =
        items.map { toImdbWatchedItem(it) }

    private suspend fun toImdbWatchedItem(item: WatchedItem): WatchedItem {
        val ids = parseTrackingExternalIds(item.id).mergeMissing(parseTrackingExternalIds(item.videoId))
        val mapped = map(
            videoId = item.videoId,
            contentType = item.type,
            ids = ids,
            episode = item.episode,
        ) ?: return item
        if (mapped.isMovie) return item.copy(id = mapped.imdb, videoId = mapped.imdb)
        val season = mapped.season ?: return item
        val number = mapped.episode ?: return item
        return item.copy(
            id = mapped.imdb,
            season = season,
            episode = number,
            videoId = "${mapped.imdb}:$season:$number",
        )
    }

    private suspend fun map(
        videoId: String?,
        contentType: String?,
        ids: TrackingExternalIds,
        episode: Int?,
    ): Mapped? {
        val type = contentType?.trim()?.lowercase()
        val isMovie = type == "movie" || type == "film"
        val video = if (isMovie) null else parseTrackingAnimeVideoId(videoId)
        val hasStandardIds = !ids.imdb.isNullOrBlank() || ids.tmdb != null ||
            !ids.tvdb.isNullOrBlank() || ids.trakt != null
        val (source, id) = when {
            video != null -> video.source to video.id
            hasStandardIds -> return null
            ids.kitsu != null -> "kitsu" to ids.kitsu
            ids.mal != null -> "mal" to ids.mal
            ids.anilist != null -> "anilist" to ids.anilist
            ids.anidb != null -> "anidb" to ids.anidb
            else -> return null
        }
        val animeEpisode = if (isMovie) null else (video?.episode ?: episode ?: return null)
        val key = "$source:$id:${animeEpisode ?: "movie"}"
        return lock.withLock {
            cache[key]?.let { return@withLock it }
            val result = lookup(source, id, isMovie, animeEpisode) ?: return@withLock null
            if (cache.size >= MAX_CACHE_ENTRIES) cache.clear()
            cache[key] = result
            result
        }
    }

    private suspend fun lookup(source: String, id: Long, isMovie: Boolean, animeEpisode: Int?): Mapped? {
        val resolved = try {
            SimklIdResolver.resolveIds(source, id.toString(), "anime")
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            null
        } ?: return null
        val imdb = resolved.imdb?.trim()?.takeIf { it.startsWith("tt", ignoreCase = true) } ?: return null
        if (isMovie) return Mapped(imdb = imdb, isMovie = true)
        val animeEpisodeNumber = animeEpisode ?: return null
        val episodes = try {
            SimklIdResolver.getEpisodeMapping(resolved.simklId, resolved.type)
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            emptyList()
        }
        episodes.firstOrNull { it.animeEpisode == animeEpisodeNumber }?.let {
            return Mapped(imdb = imdb, isMovie = false, season = it.tvdbSeason, episode = it.tvdbEpisode)
        }
        episodes.filter { it.animeEpisode < animeEpisodeNumber }.maxByOrNull { it.animeEpisode }?.let { previous ->
            return Mapped(
                imdb = imdb,
                isMovie = false,
                season = previous.tvdbSeason,
                episode = previous.tvdbEpisode + (animeEpisodeNumber - previous.animeEpisode),
            )
        }
        if (episodes.isNotEmpty()) return null
        val season = resolved.tvdbSeason ?: return null
        return Mapped(imdb = imdb, isMovie = false, season = season, episode = animeEpisodeNumber)
    }
}
