package com.nuvio.app.features.plugins

import com.nuvio.app.features.home.MetaPreview
import com.nuvio.app.features.home.PosterShape
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PluginContentIdsTest {
    @Test
    fun `series playback id strips season episode suffix`() {
        assertEquals("tt2575988", pluginContentId("tt2575988:5:8", 5, 8))
    }

    @Test
    fun `tmdb prefixed series playback id strips prefix and suffix`() {
        assertEquals("12345", pluginContentId("tmdb:12345:2:6", 2, 6))
    }

    @Test
    fun `movie id stays unchanged`() {
        assertEquals("tt0133093", pluginContentId("tt0133093", null, null))
    }

    @Test
    fun `slash prefixed tmdb id keeps base content id`() {
        assertEquals("999", pluginContentId("tmdb/999/1/2", 1, 2))
    }

    @Test
    fun `plugin search selection matches media type and exact title first`() {
        val movie = preview("tmdb:10", "movie", "Dune")
        val series = preview("tmdb:20", "series", "Dune")
        val otherMovie = preview("tmdb:11", "movie", "Dune: Part Two")

        assertEquals(movie, selectPluginTmdbSearchResult(listOf(series, otherMovie, movie), "Dune", "movie"))
        assertEquals(series, selectPluginTmdbSearchResult(listOf(movie, series), "Dune", "series"))
    }

    @Test
    fun `plugin search selection rejects wrong media type`() {
        assertNull(selectPluginTmdbSearchResult(listOf(preview("tmdb:10", "movie", "Title")), "Title", "series"))
    }

    private fun preview(id: String, type: String, name: String) = MetaPreview(
        id = id,
        type = type,
        name = name,
        poster = null,
        banner = null,
        posterShape = PosterShape.Poster,
    )
}
