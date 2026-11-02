package com.nuvio.app.features.home

import kotlin.test.Test
import kotlin.test.assertEquals

class HomeCloudStreamProviderSelectionTest {
    @Test
    fun keepsSelectedProviderWhenCloudStreamIsTheOnlyDiscoverySource() {
        assertEquals(
            "provider-a",
            effectiveHomeCloudStreamProviderId(
                selectedProviderId = "provider-a",
                hasAddonCatalogs = false,
            ),
        )
    }

    @Test
    fun ignoresPersistedProviderFilterWhenAddonCatalogsAreAvailable() {
        assertEquals(
            null,
            effectiveHomeCloudStreamProviderId(
                selectedProviderId = "provider-a",
                hasAddonCatalogs = true,
            ),
        )
    }

    @Test
    fun leavesAllProvidersSelectedWhenNoProviderWasChosen() {
        assertEquals(
            null,
            effectiveHomeCloudStreamProviderId(
                selectedProviderId = null,
                hasAddonCatalogs = false,
            ),
        )
    }
    @Test
    fun cloudStreamSectionKeyKeepsProviderAndFullCategoryIdentity() {
        val provider = "provider-storage-key"
        assertEquals("cloudstream:provider-storage-key:Movies", cloudStreamSectionKey(provider, "Movies"))
        assertNotEquals(cloudStreamSectionKey(provider, "Aa"), cloudStreamSectionKey(provider, "BB"))
        assertNotEquals(cloudStreamSectionKey("provider-a", "Movies"), cloudStreamSectionKey("provider-b", "Movies"))
    }
}
