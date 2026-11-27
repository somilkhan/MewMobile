package com.nuvio.app.core.network

import android.content.Context

internal actual object DnsPlatform {
    // The Advanced page used to keep its own provider here, separate from Mew's resolver.
    private const val legacyPreferencesName = "nuvio_network"
    private const val legacyProviderKey = "dns_provider"

    /**
     * Called from Application.onCreate so the DNS choice is readable before any network
     * client is built, including background work that starts without an Activity.
     */
    fun initialize(context: Context) {
        val appContext = context.applicationContext
        DnsOverHttpsSettingsStorage.initialize(appContext)
        migrateLegacyProvider(appContext)
    }

    actual val isSupported: Boolean = true

    /** Keeps a provider chosen on the old Advanced row unless Mew's own setting is already set. */
    private fun migrateLegacyProvider(context: Context) {
        val legacy = context.getSharedPreferences(legacyPreferencesName, Context.MODE_PRIVATE)
        val legacyId = legacy.getString(legacyProviderKey, null) ?: return
        val current = DnsOverHttpsProvider.fromId(DnsOverHttpsSettingsStorage.loadProviderId())
        val migrated = DnsOverHttpsProvider.fromId(legacyId)
        if (current == DnsOverHttpsProvider.None && migrated != DnsOverHttpsProvider.None) {
            DnsOverHttpsSettingsStorage.saveProviderId(migrated.id)
        }
        legacy.edit().remove(legacyProviderKey).apply()
    }
}

internal actual fun platformHttpEngine(): io.ktor.client.engine.HttpClientEngine? =
    io.ktor.client.engine.okhttp.OkHttp.create { preconfigured = SharedOkHttpClient.base }
