package com.nuvio.app.features.library

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LibraryPushRetryPolicyTest {
    @Test
    fun `automatic retry delays are bounded exponential backoff`() {
        assertEquals(
            listOf(1_000L, 2_000L, 4_000L, 8_000L, 16_000L, 32_000L),
            (1..LibraryPushRetryPolicy.maxAutomaticAttempts)
                .mapNotNull(LibraryPushRetryPolicy::delayForAttempt),
        )
    }

    @Test
    fun `retryable HTTP statuses include throttling timeouts and server errors`() {
        listOf(null, 408, 425, 429, 500, 503, 599).forEach { status ->
            assertEquals(true, LibraryPushRetryPolicy.isRetryableHttpStatus(status), "status=$status")
        }
    }

    @Test
    fun `permanent HTTP statuses are not retried`() {
        listOf(400, 401, 403, 404, 409, 422, 499, 600).forEach { status ->
            assertEquals(false, LibraryPushRetryPolicy.isRetryableHttpStatus(status), "status=$status")
        }
    }

    @Test
    fun `automatic retry policy rejects attempts outside its bounded window`() {
        assertNull(LibraryPushRetryPolicy.delayForAttempt(0))
        assertNull(LibraryPushRetryPolicy.delayForAttempt(7))
        assertNull(LibraryPushRetryPolicy.delayForAttempt(Int.MAX_VALUE))
    }
}
