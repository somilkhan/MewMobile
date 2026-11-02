package com.nuvio.app.features.watchprogress

import kotlin.test.Test
import kotlin.test.assertEquals

class WatchProgressPendingDeletesTest {
    private fun entry(
        key: String,
        updatedAt: Long,
    ) = WatchProgressEntry(
        contentType = "movie",
        parentMetaId = key,
        parentMetaType = "movie",
        videoId = key,
        title = key,
        lastPositionMs = 0L,
        durationMs = 0L,
        lastUpdatedEpochMs = updatedAt,
        progressKey = key,
    )

    private val keyOf: (WatchProgressEntry) -> String = { it.progressKey.orEmpty() }

    @Test
    fun mergeKeepsTheNewestDeleteForEachProgressKey() {
        val older = entry("movie-1", 100L)
        val newer = entry("movie-1", 200L)
        val other = entry("movie-2", 150L)

        val merged = mergePendingWatchProgressDeletes(
            existing = listOf(older, other),
            deleted = listOf(newer),
            keyOf = keyOf,
        )

        assertEquals(mapOf("movie-1" to 200L, "movie-2" to 150L), merged.associate { keyOf(it) to it.lastUpdatedEpochMs })
    }

    @Test
    fun acknowledgingDeleteRetainsNewerAndUnrelatedTombstones() {
        val pushed = entry("movie-1", 100L)
        val newerPending = entry("movie-1", 200L)
        val other = entry("movie-2", 150L)

        val remaining = acknowledgePendingWatchProgressDeletes(
            pending = listOf(newerPending, other),
            pushed = listOf(pushed),
            keyOf = keyOf,
        )

        assertEquals(setOf("movie-1", "movie-2"), remaining.map(keyOf).toSet())
        assertEquals(200L, remaining.single { keyOf(it) == "movie-1" }.lastUpdatedEpochMs)
    }
}
