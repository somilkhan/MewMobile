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

/** Retry quickly at first, then keep checking periodically until the cached session is validated. */
internal fun remoteSessionValidationRetryDelayMs(attempt: Int): Long = when (attempt.coerceAtLeast(0)) {
    0 -> 2_000L
    1 -> 5_000L
    2 -> 10_000L
    3 -> 20_000L
    4 -> 40_000L
    else -> 60_000L
}

val AuthState.userId: String?
    get() = (this as? AuthState.Authenticated)?.userId

val AuthState.isAnonymous: Boolean
    get() = (this as? AuthState.Authenticated)?.isAnonymous == true
