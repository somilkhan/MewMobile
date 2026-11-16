package com.nuvio.app.features.library

/**
 * Bounded automatic retry policy for durable library mutations.
 *
 * Explicit sync is deliberately not limited by this policy; exhausting this automatic
 * burst leaves the persisted mutation queue available for a later user-triggered sync.
 */
internal object LibraryPushRetryPolicy {
    const val maxAutomaticAttempts: Int = 6

    private const val initialDelayMs = 1_000L
    private const val maxDelayMs = 60_000L

    fun delayForAttempt(attempt: Int): Long? {
        if (attempt !in 1..maxAutomaticAttempts) return null
        return (initialDelayMs shl (attempt - 1)).coerceAtMost(maxDelayMs)
    }

    /**
     * Retry only HTTP responses that are normally transient. A null status represents
     * a transport failure and is handled by the caller as retryable.
     */
    fun isRetryableHttpStatus(statusCode: Int?): Boolean = when (statusCode) {
        null, 408, 425, 429 -> true
        in 500..599 -> true
        else -> false
    }
}
