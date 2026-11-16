package com.nuvio.app.core.network

import okhttp3.OkHttpClient

/**
 * Shared Android OkHttp base so long-lived network clients use the currently selected DNS resolver.
 * Callers that need custom timeouts or interceptors should derive clients with [newBuilder].
 */
internal object SharedOkHttpClient {
    val base: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .dns(ConfiguredDnsOverHttps)
            .build()
    }
}
