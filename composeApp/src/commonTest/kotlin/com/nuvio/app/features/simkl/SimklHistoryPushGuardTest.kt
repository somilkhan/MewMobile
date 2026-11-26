package com.nuvio.app.features.simkl

import com.nuvio.app.features.watched.WatchedItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SimklHistoryPushGuardTest {
    private fun mark(
        type: String,
        season: Int? = null,
        episode: Int? = null,
        id: String = "show-1",
    ) = WatchedItem(
        id = id,
        type = type,
        name = "Test title",
        season = season,
        episode = episode,
        markedAtEpochMs = 1L,
    )

    @Test
    fun `a mark without an episode never reaches Simkl for a series`() {
        assertTrue(simklHistoryPushItems(listOf(mark("series"))).isEmpty())
        assertTrue(simklHistoryPushItems(listOf(mark("show"))).isEmpty())
        assertTrue(simklHistoryPushItems(listOf(mark("tv"))).isEmpty())
        assertTrue(simklHistoryPushItems(listOf(mark("anime"))).isEmpty())
        assertTrue(simklHistoryPushItems(listOf(mark("documentary-series"))).isEmpty())
    }

    @Test
    fun `episode marks always reach Simkl`() {
        val pushed = simklHistoryPushItems(listOf(mark("series", season = 1, episode = 1)))
        assertEquals(1, pushed.size)
        assertEquals(1, pushed.single().season)
        assertEquals(1, pushed.single().episode)
    }

    @Test
    fun `films need no episode`() {
        assertEquals(1, simklHistoryPushItems(listOf(mark("movie"))).size)
        assertEquals(1, simklHistoryPushItems(listOf(mark("film"))).size)
        assertEquals(1, simklHistoryPushItems(listOf(mark("Movie"))).size)
    }

    @Test
    fun `the episodes of a whole-series action still travel`() {
        val pushed = simklHistoryPushItems(
            listOf(
                mark("series"),
                mark("series", season = 1, episode = 1),
                mark("movie", id = "movie-1"),
            ),
        )

        assertEquals(2, pushed.size)
        assertTrue(pushed.any { it.type == "series" && it.episode == 1 })
        assertTrue(pushed.any { it.type == "movie" })
    }

    @Test
    fun `a series marked together with its episodes is moved to Completed`() {
        val completed = simklExplicitlyCompletedSeries(
            listOf(
                mark("series"),
                mark("series", season = 1, episode = 1),
                mark("series", season = 1, episode = 2),
            ),
        )

        assertEquals(1, completed.size)
        assertEquals(null, completed.single().episode)
    }

    @Test
    fun `an automatic series mark alone never moves Simkl to Completed`() {
        assertTrue(simklExplicitlyCompletedSeries(listOf(mark("series"))).isEmpty())
        assertTrue(
            simklExplicitlyCompletedSeries(
                listOf(mark("series", season = 3, episode = 4)),
            ).isEmpty(),
        )
        assertTrue(simklExplicitlyCompletedSeries(listOf(mark("movie", id = "movie-1"))).isEmpty())
    }

    @Test
    fun `a series marked together with its episodes is an explicit completion`() {
        val series = simklExplicitlyCompletedSeries(
            listOf(
                mark("series"),
                mark("series", season = 1, episode = 1),
                mark("series", season = 1, episode = 2),
            ),
        )

        assertEquals(1, series.size)
        assertEquals(null, series.single().episode)
    }

    @Test
    fun `an automatic series mark on its own never moves Simkl to Completed`() {
        assertTrue(simklExplicitlyCompletedSeries(listOf(mark("series"))).isEmpty())
        assertTrue(
            simklExplicitlyCompletedSeries(listOf(mark("series", season = 3, episode = 4))).isEmpty(),
        )
        assertTrue(simklExplicitlyCompletedSeries(listOf(mark("movie"))).isEmpty())
    }
}
