package com.nuvio.app.core.auth

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AuthModelsTest {
    @Test
    fun `remote session validation backs off then continues at a safe interval`() {
        assertEquals(2_000L, remoteSessionValidationRetryDelayMs(0))
        assertEquals(5_000L, remoteSessionValidationRetryDelayMs(1))
        assertEquals(10_000L, remoteSessionValidationRetryDelayMs(2))
        assertEquals(20_000L, remoteSessionValidationRetryDelayMs(3))
        assertEquals(40_000L, remoteSessionValidationRetryDelayMs(4))
        assertEquals(60_000L, remoteSessionValidationRetryDelayMs(5))
        assertEquals(60_000L, remoteSessionValidationRetryDelayMs(100))
        assertEquals(2_000L, remoteSessionValidationRetryDelayMs(-1))
    }

    @Test
    fun `remote sync requires a validated non anonymous session`() {
        assertFalse(AuthState.Loading.canSyncRemotely)
        assertFalse(AuthState.Unauthenticated.canSyncRemotely)
        assertFalse(
            AuthState.Authenticated(
                userId = "anonymous-user",
                email = null,
                isAnonymous = true,
                isRemoteSessionValidated = true,
            ).canSyncRemotely,
        )
        assertFalse(
            AuthState.Authenticated(
                userId = "cached-user",
                email = "cached@example.com",
                isAnonymous = false,
            ).canSyncRemotely,
        )
        assertFalse(
            AuthState.Authenticated(
                userId = " ",
                email = "user@example.com",
                isAnonymous = false,
                isRemoteSessionValidated = true,
            ).canSyncRemotely,
        )
        assertTrue(
            AuthState.Authenticated(
                userId = "validated-user",
                email = "user@example.com",
                isAnonymous = false,
                isRemoteSessionValidated = true,
            ).canSyncRemotely,
        )
    }
}
