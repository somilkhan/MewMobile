package com.nuvio.app.core.region

internal actual object PlatformRegionContext {
    actual fun initialize() = Unit

    actual fun currentCountryCode(): String? = null
}
