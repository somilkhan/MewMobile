package com.nuvio.app.features.cloudstream

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CloudStreamDiscoveryPolicyTest {
    @Test
    fun tmdbEnabledDisablesCloudStreamDiscovery() {
        assertFalse(isCloudStreamDiscoveryAllowed(tmdbEnabled = true))
    }

    @Test
    fun tmdbDisabledAllowsCloudStreamDiscovery() {
        assertTrue(isCloudStreamDiscoveryAllowed(tmdbEnabled = false))
    }
}
