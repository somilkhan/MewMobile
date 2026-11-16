package com.nuvio.app.features.home

import com.nuvio.app.features.cloudstream.CLOUDSTREAM_DISCOVERY_TIMEOUT_MS
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HomeProviderTimeoutPolicyTest {
    @Test
    fun homeDeadlineAllowsRuntimeDiscoveryToFinish() {
        assertEquals(15_000L, CLOUDSTREAM_DISCOVERY_TIMEOUT_MS)
        assertEquals(20_000L, HOME_CLOUDSTREAM_PROVIDER_TIMEOUT_MS)
        assertTrue(HOME_CLOUDSTREAM_PROVIDER_TIMEOUT_MS > CLOUDSTREAM_DISCOVERY_TIMEOUT_MS)
    }
}
