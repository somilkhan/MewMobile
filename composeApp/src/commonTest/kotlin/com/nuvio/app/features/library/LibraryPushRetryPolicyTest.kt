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
    fun `automatic retry policy rejects attempts outside its bounded window`() {
        assertNull(LibraryPushRetryPolicy.delayForAttempt(0))
        assertNull(LibraryPushRetryPolicy.delayForAttempt(7))
        assertNull(LibraryPushRetryPolicy.delayForAttempt(Int.MAX_VALUE))
    }
}
