package com.nuvio.app.features.tmdb

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TmdbCredentialAuthTest {
    @Test
    fun v3ApiKeyUsesQueryParameterWithoutBearerHeader() {
        val key = "test-v3-key"
        val url = buildTmdbUrl(
            endpoint = "movie/550",
            apiKey = key,
            query = mapOf("language" to "en"),
        )

        assertTrue(url.contains("api_key=test-v3-key"))
        assertEquals(emptyMap(), tmdbAuthorizationHeaders(key))
    }

    @Test
    fun v3ApiKeyAndQueryValuesAreUrlEncoded() {
        val url = buildTmdbUrl(
            endpoint = "/search/movie",
            apiKey = " key+value ",
            query = mapOf("query" to "A & B"),
        )

        assertTrue(url.startsWith("https://api.themoviedb.org/3/search/movie?"))
        assertTrue(url.contains("api_key=key%2Bvalue"))
        assertTrue(url.contains("query=A%20%26%20B"))
    }

    @Test
    fun rejectedCredentialDisablesMetadataUntilItChanges() {
        val rejected = TmdbSettings(apiKey = "test-key", enabled = true, credentialRejected = true)
        assertFalse(rejected.canUseMetadata)
        assertTrue(rejected.copy(credentialRejected = false).canUseMetadata)
    }

    @Test
    fun readAccessTokenUsesBearerHeaderAndIsNotPlacedInUrl() {
        val token = "Bearer header.payload.signature"
        val url = buildTmdbUrl(
            endpoint = "movie/550",
            apiKey = token,
            query = mapOf("language" to "en"),
        )

        assertTrue(isTmdbReadAccessToken(token))
        assertFalse(url.contains("api_key="))
        assertFalse(url.contains(token))
        assertEquals("Bearer header.payload.signature", tmdbAuthorizationHeaders(token)["Authorization"])
    }

    @Test
    fun bearerPrefixNormalizationIsCaseInsensitiveAndTrimsWhitespace() {
        val token = "  bEaReR   header.payload.signature  "

        assertEquals("header.payload.signature", normalizeTmdbCredential(token))
        assertEquals("Bearer header.payload.signature", tmdbAuthorizationHeaders(token)["Authorization"])
        assertFalse(buildTmdbUrl("movie/550", token).contains("api_key="))
    }

    @Test
    fun tmdbIdNormalizationHandlesNestedPrefixesAndCase() {
        assertEquals("12345", normalizeTmdbVideoId("TMDB:movie:12345:4:2"))
        assertEquals("12345", normalizeTmdbVideoId("series:TMDB:12345/season/4"))
        assertEquals("tt1234567", normalizeTmdbVideoId("MOVIE:tt1234567:2:1"))
        assertEquals("12345", normalizeTmdbVideoId("  tmdb:12345  "))
    }

    @Test
    fun malformedTokenIsNotTreatedAsBearerCredential() {
        val malformed = "Bearer not-a-jwt"

        assertFalse(isTmdbReadAccessToken(malformed))
        assertEquals(emptyMap(), tmdbAuthorizationHeaders(malformed))
        assertTrue(buildTmdbUrl("movie/550", malformed).contains("api_key=not-a-jwt"))
    }
}
