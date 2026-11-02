package com.nuvio.app.features.tmdb

import com.nuvio.app.features.addons.RawHttpResponse
import io.ktor.http.encodeURLParameter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

private const val TMDB_RETRY_BASE_DELAY_MS = 250L

internal fun normalizeTmdbCredential(value: String): String =
    value.trim().removePrefix("Bearer ").trim()

internal fun isTmdbReadAccessToken(value: String): Boolean {
    val segments = normalizeTmdbCredential(value).split('.')
    return segments.size == 3 && segments.all { it.isNotBlank() }
}

internal fun tmdbAuthorizationHeaders(value: String): Map<String, String> =
    if (isTmdbReadAccessToken(value)) {
        mapOf("Authorization" to "Bearer ${normalizeTmdbCredential(value)}")
    } else {
        emptyMap()
    }

internal fun buildTmdbUrl(
    endpoint: String,
    apiKey: String,
    query: Map<String, String> = emptyMap(),
): String {
    val params = linkedMapOf<String, String>()
    if (!isTmdbReadAccessToken(apiKey)) {
        params["api_key"] = normalizeTmdbCredential(apiKey)
    }
    query.forEach { (key, value) ->
        if (value.isNotBlank()) {
            params[key] = value
        }
    }
    return buildString {
        append("https://api.themoviedb.org/3/")
        append(endpoint.removePrefix("/"))
        if (params.isNotEmpty()) {
            append("?")
            append(params.entries.joinToString("&") { (key, value) ->
                "${key.encodeURLParameter()}=${value.encodeURLParameter()}"
            })
        }
    }
}

internal suspend fun requestTmdbText(
    maxAttempts: Int = 3,
    pause: suspend (Long) -> Unit = { delay(it) },
    request: suspend () -> RawHttpResponse,
): String? = requestTmdbTextInternal(
    maxAttempts = maxAttempts,
    pause = pause,
    onUnauthorized = null,
    request = request,
)

internal suspend fun requestTmdbText(
    maxAttempts: Int = 3,
    pause: suspend (Long) -> Unit = { delay(it) },
    onUnauthorized: (() -> Unit)?,
    request: suspend () -> RawHttpResponse,
): String? = requestTmdbTextInternal(
    maxAttempts = maxAttempts,
    pause = pause,
    onUnauthorized = onUnauthorized,
    request = request,
)

private suspend fun requestTmdbTextInternal(
    maxAttempts: Int,
    pause: suspend (Long) -> Unit,
    onUnauthorized: (() -> Unit)?,
    request: suspend () -> RawHttpResponse,
): String? {
    val attempts = maxAttempts.coerceAtLeast(1)
    repeat(attempts) { attempt ->
        val response = try {
            request()
        } catch (error: Throwable) {
            if (error is CancellationException) throw error
            null
        }
        if (response != null && response.status in 200..299) return response.body
        if (response?.status == 401) onUnauthorized?.invoke()
        val transient = response == null || response.status == 429 || response.status in 500..599
        if (!transient || attempt == attempts - 1) return null
        pause(TMDB_RETRY_BASE_DELAY_MS * (attempt + 1))
    }
    return null
}
