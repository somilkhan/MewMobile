package com.nuvio.app.features.library

import com.nuvio.app.features.details.MetaDetailsRepository
import com.nuvio.app.features.watchprogress.CurrentDateProvider
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/*
 * Mew hook: luq settings pages (Profile insights "Upcoming", Advanced "clear cache") call the
 * release-schedule API from luq's LibraryScreen. Mew keeps its own LibraryScreen, so this file
 * provides the same entry points with a lightweight in-memory schedule.
 */

internal data class LibraryUpcomingEpisode(
    val key: String,
    val item: LibraryItem,
    val dateIso: String,
    val subtitle: String?,
    val imageUrl: String?,
    val seasonNumber: Int?,
    val episodeNumber: Int?,
)

private object MewLibraryReleaseSchedule {
    val episodes = MutableStateFlow<List<LibraryUpcomingEpisode>>(emptyList())
    val mutex = Mutex()
    var lastSignature: String? = null
    var lastBuiltAtMs: Long = 0L
}

private const val MEW_RELEASE_SCHEDULE_CONCURRENCY = 4
private const val MEW_RELEASE_SCHEDULE_ITEM_LIMIT = 80

private fun LibraryItem.isMewScheduleSeries(): Boolean =
    type.equals("series", ignoreCase = true) ||
        type.equals("tv", ignoreCase = true) ||
        type.equals("show", ignoreCase = true) ||
        type.equals("anime", ignoreCase = true)

private suspend fun buildMewReleaseSchedule(items: List<LibraryItem>): List<LibraryUpcomingEpisode> = coroutineScope {
    items.filter { it.isMewScheduleSeries() }
        .take(MEW_RELEASE_SCHEDULE_ITEM_LIMIT)
        .chunked(MEW_RELEASE_SCHEDULE_CONCURRENCY)
        .flatMap { chunk ->
            chunk.map { item ->
                async {
                    val details = runCatching { MetaDetailsRepository.fetch(item.type, item.id) }.getOrNull()
                        ?: return@async emptyList()
                    details.videos.mapNotNull { video ->
                        val date = video.released?.trim()?.take(10)?.takeIf { it.length == 10 } ?: return@mapNotNull null
                        val season = video.season?.takeIf { it > 0 }
                        val episode = video.episode?.takeIf { it > 0 }
                        val label = when {
                            season != null && episode != null -> "S${season}E$episode"
                            episode != null -> "E$episode"
                            else -> null
                        }
                        LibraryUpcomingEpisode(
                            key = "episode:${item.type}:${item.id}:${season ?: 0}:${episode ?: video.id}:$date",
                            item = item,
                            dateIso = date,
                            subtitle = listOfNotNull(label, video.title.takeIf { it.isNotBlank() })
                                .joinToString(" - ")
                                .takeIf { it.isNotBlank() },
                            imageUrl = video.thumbnail ?: item.banner ?: item.poster,
                            seasonNumber = season,
                            episodeNumber = episode,
                        )
                    }
                }
            }.awaitAll().flatten()
        }
        .distinctBy { it.key }
        .sortedWith(compareBy<LibraryUpcomingEpisode> { it.dateIso }.thenBy { it.item.name.lowercase() })
}

private suspend fun rebuildMewReleaseSchedule(items: List<LibraryItem>, force: Boolean) {
    MewLibraryReleaseSchedule.mutex.withLock {
        val signature = items.joinToString("|") { "${it.type}:${it.id}" }
        if (!force && signature == MewLibraryReleaseSchedule.lastSignature) return
        MewLibraryReleaseSchedule.episodes.value = buildMewReleaseSchedule(items)
        MewLibraryReleaseSchedule.lastSignature = signature
    }
}

internal suspend fun warmLibraryReleaseSchedule(items: List<LibraryItem>) {
    if (items.isNotEmpty()) rebuildMewReleaseSchedule(items, force = false)
}

internal suspend fun forceRefreshLibraryReleaseSchedule(items: List<LibraryItem>) {
    rebuildMewReleaseSchedule(items, force = true)
}

internal suspend fun clearLibraryReleaseScheduleCache() {
    MewLibraryReleaseSchedule.mutex.withLock {
        MewLibraryReleaseSchedule.episodes.value = emptyList()
        MewLibraryReleaseSchedule.lastSignature = null
    }
    LibraryReleaseScheduleStorage.savePayload("")
}

internal fun libraryUpcomingEpisodesFlow(days: Int = 7): Flow<List<LibraryUpcomingEpisode>> =
    MewLibraryReleaseSchedule.episodes.map { episodes ->
        val today = CurrentDateProvider.todayIsoDate().take(10)
        val end = isoPlusDays(today, days)
        episodes.filter { it.dateIso >= today && it.dateIso < end }
    }

private fun isoPlusDays(iso: String, days: Int): String {
    val parts = iso.split('-').mapNotNull { it.toIntOrNull() }
    if (parts.size != 3) return iso
    var (y, m, d) = parts
    repeat(days) {
        d += 1
        val dim = when (m) {
            2 -> if ((y % 4 == 0 && y % 100 != 0) || y % 400 == 0) 29 else 28
            4, 6, 9, 11 -> 30
            else -> 31
        }
        if (d > dim) { d = 1; m += 1; if (m > 12) { m = 1; y += 1 } }
    }
    return "${y.toString().padStart(4, '0')}-${m.toString().padStart(2, '0')}-${d.toString().padStart(2, '0')}"
}
