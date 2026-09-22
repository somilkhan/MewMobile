package com.lagradost.cloudstream3.network

import android.content.Context
import com.lagradost.cloudstream3.USER_AGENT
import okhttp3.Cache
import okhttp3.Headers
import okhttp3.Headers.Companion.toHeaders
import okhttp3.OkHttpClient
import com.lagradost.nicehttp.Requests
import java.io.File

private val defaultHeaders = mapOf("user-agent" to USER_AGENT)

/**
 * Initializes the NiceHttp global client expected by CloudStream extensions.
 *
 * The host app does not run CloudStream's Application class, so this must be
 * performed explicitly before third-party plugins call `app.get()`.
 */
fun Requests.initClient(context: Context) {
    baseClient = OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .cache(
            Cache(
                directory = File(context.cacheDir, "cloudstream_http_cache"),
                maxSize = 50L * 1024L * 1024L,
            ),
        )
        .build()
}

fun getHeaders(headers: Map<String, String>, cookie: Map<String, String>): Headers {
    val cookieHeader = if (cookie.isEmpty()) {
        emptyMap()
    } else {
        mapOf("Cookie" to cookie.entries.joinToString(" ") { "${it.key}=${it.value};" })
    }
    return (defaultHeaders + headers + cookieHeader).toHeaders()
}
