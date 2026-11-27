package com.nuvio.app.core.network

internal actual object DnsPlatform {
    actual val isSupported: Boolean = false
}

internal actual fun platformHttpEngine(): io.ktor.client.engine.HttpClientEngine? = null
