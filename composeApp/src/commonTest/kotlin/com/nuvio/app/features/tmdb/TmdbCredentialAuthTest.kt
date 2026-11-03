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
}
