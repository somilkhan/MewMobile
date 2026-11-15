package com.nuvio.app.core.auth

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AuthModelsTest {
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
