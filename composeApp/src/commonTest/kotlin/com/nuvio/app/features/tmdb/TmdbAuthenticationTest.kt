package com.nuvio.app.features.tmdb

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TmdbAuthenticationTest {
    @Test
    fun `v3 API keys remain query parameters`() {
        assertEquals(
            "https://api.themoviedb.org/3/movie/603?api_key=v3-key&language=en",
            buildTmdbUrl("movie/603", "v3-key", mapOf("language" to "en")),
        )
    }

    @Test
    fun `bearer token requests omit API key query parameter`() {
        assertEquals(
            "https://api.themoviedb.org/3/movie/603",
            buildTmdbUrl("movie/603", null),
        )
    }

    @Test
    fun `v4 read access token format is detected without treating v3 keys as bearer tokens`() {
        assertTrue(isTmdbV4ReadAccessToken("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyIn0.signature"))
        assertFalse(isTmdbV4ReadAccessToken("0123456789abcdef0123456789abcdef"))
        assertFalse(isTmdbV4ReadAccessToken("eyJ-not-a-jwt"))
    }
}
