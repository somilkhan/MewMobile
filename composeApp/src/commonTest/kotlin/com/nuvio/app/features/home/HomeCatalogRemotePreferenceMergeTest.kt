package com.nuvio.app.features.home

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HomeCatalogRemotePreferenceMergeTest {
    @Test
    fun `preserves local choices when remote sync arrives before catalogs load`() {
        assertTrue(
            HomeCatalogSettingsRepository.shouldPreservePreferenceAfterRemoteSync(
                key = "addon:movie:popular",
                remoteKeys = emptySet(),
                knownKeys = emptySet(),
            ),
        )
    }

    @Test
    fun `remote values win for keys included in the incoming payload`() {
        assertFalse(
            HomeCatalogSettingsRepository.shouldPreservePreferenceAfterRemoteSync(
                key = "addon:movie:popular",
                remoteKeys = setOf("addon:movie:popular"),
                knownKeys = emptySet(),
            ),
        )
    }

    @Test
    fun `known keys and explicit sync keys remain preserved after catalogs load`() {
        assertTrue(
            HomeCatalogSettingsRepository.shouldPreservePreferenceAfterRemoteSync(
                key = "known-key",
                remoteKeys = emptySet(),
                knownKeys = setOf("known-key"),
            ),
        )
        assertTrue(
            HomeCatalogSettingsRepository.shouldPreservePreferenceAfterRemoteSync(
                key = "addon:type:id:profile",
                remoteKeys = emptySet(),
                knownKeys = setOf("known-key"),
            ),
        )
        assertFalse(
            HomeCatalogSettingsRepository.shouldPreservePreferenceAfterRemoteSync(
                key = "legacy-key",
                remoteKeys = emptySet(),
                knownKeys = setOf("known-key"),
            ),
        )
    }
}
