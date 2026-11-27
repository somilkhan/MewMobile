package com.nuvio.app.features.updater

/*
 * Mew hook: luq's What's New screen expects luq's GitHub constants and what's-new cache.
 * Mew keeps its own updater (AppUpdater.kt), so this file points those at the Mew release
 * repository. UpdateChannel, UpdatePreferences and ReleaseSelector live in luq's files
 * (UpdateChannel.kt, UpdatePreferences.kt, ReleaseSelector.kt), copied verbatim.
 */

internal const val GITHUB_API_BASE = "https://api.github.com"
internal const val GITHUB_OWNER = AppUpdateConfig.GITHUB_OWNER
internal const val GITHUB_REPO = AppUpdateConfig.GITHUB_REPO

private object MewWhatsNewCacheHolder {
    var payload: String? = null
}

/** In-memory What's New cache (Mew's platform updater has no persisted slot for it). */
internal fun AppUpdaterPlatform.getWhatsNewCache(): String? = MewWhatsNewCacheHolder.payload

internal fun AppUpdaterPlatform.setWhatsNewCache(payload: String?) {
    MewWhatsNewCacheHolder.payload = payload
}
