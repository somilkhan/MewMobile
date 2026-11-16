package com.nuvio.app.core.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ServerCapabilities(
    val emailPasswordAuth: Boolean,
    val tvLogin: Boolean,
    val watchParty: Boolean = false,
)

data class ServerConfiguration(
    val backendUrl: String,
    val publishableKey: String,
    val capabilities: ServerCapabilities,
    val isCustom: Boolean = false,
    val discoveryUrl: String? = null,
    val fallbackBackendUrl: String? = null,
)

object ServerConfigurationRepository {
    private val official = ServerConfiguration(
        backendUrl = SupabaseConfig.URL.trim().trimEnd('/'),
        publishableKey = SupabaseConfig.ANON_KEY.trim(),
        capabilities = ServerCapabilities(
            emailPasswordAuth = true,
            tvLogin = true,
            watchParty = true,
        ),
        fallbackBackendUrl = SupabaseConfig.FALLBACK_URL.trim().trimEnd('/').takeIf(String::isNotBlank),
    )
    private val _active = MutableStateFlow(official)
    val active: StateFlow<ServerConfiguration> = _active.asStateFlow()
}
