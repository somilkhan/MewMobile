package com.nuvio.app.core.auth

sealed interface AuthState {
    data object Loading : AuthState
    data object Unauthenticated : AuthState
    data class Authenticated(
        val userId: String,
        val email: String?,
        val isAnonymous: Boolean,
        val isRemoteSessionValidated: Boolean = false,
    ) : AuthState
}

val AuthState.isLoggedIn: Boolean
    get() = this is AuthState.Authenticated

/** Remote profile sync requires a validated, non-anonymous account session. */
val AuthState.canSyncRemotely: Boolean
    get() = this is AuthState.Authenticated && !isAnonymous && isRemoteSessionValidated && userId.isNotBlank()

val AuthState.userId: String?
    get() = (this as? AuthState.Authenticated)?.userId

val AuthState.isAnonymous: Boolean
    get() = (this as? AuthState.Authenticated)?.isAnonymous == true
