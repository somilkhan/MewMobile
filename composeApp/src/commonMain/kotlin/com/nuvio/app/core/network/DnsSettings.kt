package com.nuvio.app.core.network

/**
 * Mew: Mew has one DNS setting. The selected provider is stored by
 * [DnsOverHttpsSettingsRepository] and applied to every OkHttp client through
 * [ConfiguredDnsOverHttps]. This file only reports whether the platform can apply it.
 */
internal expect object DnsPlatform {
    val isSupported: Boolean
}

object DnsSettingsRepository {
    val isSupported: Boolean get() = DnsPlatform.isSupported
}

internal expect fun platformHttpEngine(): io.ktor.client.engine.HttpClientEngine?
