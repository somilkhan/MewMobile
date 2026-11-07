package com.nuvio.app.features.home

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import com.nuvio.app.features.cloudstream.CloudStreamSearchItem
import com.nuvio.app.features.cloudstream.CloudStreamTvType

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
    @Test
    fun mapsEveryNonEmptyRawCatalogCategoryForProvider() {
        val providerId = "cs-provider"
        val sections = buildCloudStreamHomeSections(
            providerStorageKey = "provider-storage-key",
            providerId = providerId,
            providerName = "Example Extension",
            categories = listOf(
                "Movies" to listOf(CloudStreamSearchItem(providerId, "movie-1", "Movie", CloudStreamTvType.Movie)),
                "Series" to listOf(CloudStreamSearchItem(providerId, "series-1", "Series", CloudStreamTvType.TvSeries)),
                "Empty" to emptyList(),
            ),
        )

        assertEquals(2, sections.size)
        assertEquals(listOf("Movies", "Series"), sections.map { it.title })
        assertEquals(listOf("cloudstream:provider-storage-key:Movies", "cloudstream:provider-storage-key:Series"), sections.map { it.key })
        assertTrue(sections.all { (it.target as com.nuvio.app.features.catalog.CatalogTarget.CloudStream).providerId == providerId })
    }
}
