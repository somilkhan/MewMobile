package com.nuvio.app.features.simkl

import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals

class SimklPlaybackMergeTest {
    @Test
    fun `keeps newer local position when server returns stale copy of same episode`() {
        val fetchedStale = session(
            id = 1L,
            progress = 83.0,
            pausedAt = "2026-10-20T10:00:00Z",
            episode = SimklPlaybackEpisode(season = 2, number = 3),
        )
        val unrelated = session(
            id = 2L,
            progress = 21.0,
            pausedAt = "2026-10-20T10:05:00Z",
            episode = SimklPlaybackEpisode(season = 2, number = 4),
        )
        val localNewer = session(
            id = 3L,
            progress = 88.0,
            pausedAt = "2026-10-20T10:02:00Z",
            episode = SimklPlaybackEpisode(season = 2, number = 3),
        )

        val merged = mergeFetchedPlayback(
            fetched = listOf(fetchedStale, unrelated),
            held = listOf(localNewer),
        )

        assertEquals(listOf(unrelated, localNewer), merged)
    }

    @Test
    fun `does not restore local playback absent from account response`() {
        val removedOnServer = session(
            id = 1L,
            progress = 88.0,
            pausedAt = "2026-10-20T10:02:00Z",
            episode = SimklPlaybackEpisode(season = 2, number = 3),
        )

        assertEquals(emptyList(), mergeFetchedPlayback(fetched = emptyList(), held = listOf(removedOnServer)))
    }

    private fun session(
        id: Long,
        progress: Double,
        pausedAt: String,
        episode: SimklPlaybackEpisode,
    ) = SimklPlaybackSession(
        id = id,
        progress = progress,
        pausedAt = pausedAt,
        type = "show",
        episode = episode,
        show = SimklMedia(
            title = "Black Clover",
            ids = mapOf("simkl" to JsonPrimitive("1234")),
        ),
    )
}
