package com.nuvio.app.core.network

import com.nuvio.app.core.build.AppVersionConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.websocket.KtorRealtimeWebsocketFactory
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.github.jan.supabase.supabaseJson
import kotlin.time.Duration.Companion.seconds
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.http.HttpHeaders
import io.ktor.http.takeFrom
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlin.time.Duration.Companion.seconds

object SupabaseProvider {
    private val clientLock = SynchronizedObject()
    private var cachedClient: SupabaseClient? = null
    private val rateLimitCoordinator = BackendRateLimitCoordinator()

    @OptIn(SupabaseInternal::class)
    val client: SupabaseClient by lazy {
        val userAgent = "NuvioMobile/${AppVersionConfig.VERSION_NAME.ifBlank { "dev" }}"
        lateinit var client: SupabaseClient
        client = createSupabaseClient(
            supabaseUrl = configuration.backendUrl,
            supabaseKey = configuration.publishableKey,
        ) {
            platformHttpEngine()?.let { httpEngine = it }
            httpConfig {
                install(HttpTimeout) {
                    requestTimeoutMillis = 60_000
                    connectTimeoutMillis = 30_000
                    socketTimeoutMillis = 60_000
                }
                install(WebSockets) { contentConverter = KotlinxWebsocketSerializationConverter(supabaseJson) }
                if (SupabaseEndpointConfig.hasFallback) {
                    install(HttpRequestRetry) {
                        retryOnExceptionIf(maxRetries = 1) { request, cause ->
                            SupabaseEndpointConfig.shouldRetryWithFallback(
                                requestUrl = request.url.buildString(),
                                statusCode = retryResponse.status.value,
                            )
                            retryCause != null -> SupabaseEndpointConfig.shouldRetryWithFallback(
                                requestUrl = request.url.buildString(),
                                cause = retryCause,
                            )
                            else -> false
                        }
                        if (shouldUseFallback) {
                            SupabaseEndpointConfig.fallbackUrlFor(request.url.buildString())?.let { fallbackUrl ->
                                request.url.takeFrom(fallbackUrl)
                            }
                        }
                    }
                    delayMillis(respectRetryAfterHeader = false) { retryCount ->
                        val retryResponse = response
                        if (retryResponse != null && isRetryableBackendResponse(retryResponse.status.value)) {
                            backendRetryDelayMillis(
                                retryCount = retryCount,
                                retryAfterHeader = retryResponse.headers[HttpHeaders.RetryAfter],
                            )
                        } else {
                            100L
                        }
                    }
                }
                defaultRequest {
                    headers.append(HttpHeaders.UserAgent, userAgent)
                }
                install(HttpTimeout) {
                    requestTimeoutMillis = 30_000L
                    connectTimeoutMillis = 15_000L
                }
                install(WebSockets) {
                    contentConverter = KotlinxWebsocketSerializationConverter(supabaseJson)
                }
            }
            install(Auth)
            install(Postgrest)
            install(Functions)
            install(Realtime) {
                customUrl = SupabaseEndpointConfig.realtimeUrl(SupabaseConfig.URL)
                reconnectDelay = 3.seconds
                websocketFactory = RealtimeSocketFactory { url -> KtorRealtimeWebsocketFactory(client.httpClient.httpClient).create(url) }
            }
        }
        previous?.close()
    }
}
