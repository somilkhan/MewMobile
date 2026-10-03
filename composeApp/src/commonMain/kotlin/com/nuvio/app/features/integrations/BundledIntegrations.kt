package com.nuvio.app.features.integrations

/**
 * Community integrations shipped as default remote registrations.
 *
 * These are not compiled into Mew. Their manifests/code remain remotely hosted
 * so the app can receive upstream metadata and provider updates without an APK release.
 */
object BundledIntegrations {
    const val AIOMETADATA_MANIFEST_URL =
        "https://aiometadata.elfhosted.com/stremio/73e81465-c507-423f-ae59-adf0f73f28dd/manifest.json"

    const val PHISHER_PLUGIN_REPOSITORY_URL =
        "https://raw.githubusercontent.com/phisher98/phisher-nuvio-providers/refs/heads/main/manifest.json"

    const val D3ADLYROCKET_PLUGIN_REPOSITORY_URL =
        "https://raw.githubusercontent.com/D3adlyRocket/All-in-One-Nuvio/refs/heads/main/manifest.json"

    val bundledStremioAddonUrls: List<String> = listOf(
        AIOMETADATA_MANIFEST_URL,
    )

    val bundledPluginRepositoryUrls: List<String> = listOf(
        PHISHER_PLUGIN_REPOSITORY_URL,
        D3ADLYROCKET_PLUGIN_REPOSITORY_URL,
    )

    fun isBundledStremioAddon(url: String): Boolean =
        bundledStremioAddonUrls.any { it.equals(url, ignoreCase = true) }

    fun isBundledPluginRepository(url: String): Boolean =
        bundledPluginRepositoryUrls.any { it.equals(url, ignoreCase = true) }
}
