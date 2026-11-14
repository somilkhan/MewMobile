package com.nuvio.app.features.cloudstream

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CloudStreamDiscoveryPolicyTest {
    @Test
    fun tmdbEnabledDoesNotDisableCloudStreamDiscovery() {
        assertTrue(isCloudStreamDiscoveryAllowed(tmdbEnabled = true))
    }

    @Test
    fun tmdbDisabledAlsoAllowsCloudStreamDiscovery() {
        assertTrue(isCloudStreamDiscoveryAllowed(tmdbEnabled = false))
    }
}
