package com.nuvio.app.features.addons

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AddonModelsTest {

    @Test
    fun `first version observation is not an update`() {
        assertFalse(addonVersionChanged(null, "1.0.0"))
        assertFalse(addonVersionChanged("", "1.0.0"))
    }

    @Test
    fun `same version is not an update`() {
        assertFalse(addonVersionChanged("1.0.0", "1.0.0"))
    }

    @Test
    fun `changed version is detected`() {
        assertTrue(addonVersionChanged("1.0.0", "1.1.0"))
    }

    @Test
    fun `pending version notice survives a newer refresh version`() {
        assertEquals("1.0.0", addonVersionChangeFrom("1.1.0", "1.2.0", "1.0.0"))
    }

    @Test
    fun `version change helper does not flag first observation`() {
        assertNull(addonVersionChangeFrom(null, "1.0.0", null))
    }

    @Test
    fun `disabled addon is installed but not active`() {
        val addon = ManagedAddon(
            manifestUrl = "https://example.test/manifest.json",
            manifest = manifest(),
            enabled = false,
        )

        assertFalse(addon.isActive)
        assertEquals(0, listOf(addon).toOverview().activeAddons)
        assertEquals(0, listOf(addon).toOverview().totalCatalogs)
    }

    @Test
    fun `enabled addons helper filters disabled addons`() {
        val enabled = ManagedAddon(
            manifestUrl = "https://enabled.example/manifest.json",
            manifest = manifest(id = "enabled"),
            enabled = true,
        )
        val disabled = ManagedAddon(
            manifestUrl = "https://disabled.example/manifest.json",
            manifest = manifest(id = "disabled"),
            enabled = false,
        )

        assertEquals(listOf(enabled), listOf(enabled, disabled).enabledAddons())
        assertTrue(enabled.isActive)
    }
}

private fun manifest(id: String = "addon") = AddonManifest(
    id = id,
    name = id,
    description = "",
    version = "1.0.0",
    resources = listOf(AddonResource(name = "catalog", types = listOf("movie"))),
    types = listOf("movie"),
    catalogs = listOf(AddonCatalog(type = "movie", id = "popular", name = "Popular")),
    transportUrl = "https://$id.example/manifest.json",
)
