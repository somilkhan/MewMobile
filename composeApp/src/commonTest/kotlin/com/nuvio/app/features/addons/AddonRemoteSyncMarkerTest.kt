package com.nuvio.app.features.addons

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AddonRemoteSyncMarkerTest {
    @Test
    fun pendingMarkerPreservesVersionChangesAndCanBeCleared() {
        val versions = mapOf("https://provider.example/manifest.json" to "1.2.3")

        val pending = versions.withAddonRemoteSyncPending(true)
        assertTrue(pending.hasAddonRemoteSyncPending())
        assertEquals("1.2.3", pending["https://provider.example/manifest.json"])

        val cleared = pending.withAddonRemoteSyncPending(false)
        assertFalse(cleared.hasAddonRemoteSyncPending())
        assertEquals(versions, cleared)
    }
}
