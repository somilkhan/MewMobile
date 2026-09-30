package com.nuvio.app.core.region

import android.content.Context
import android.telephony.TelephonyManager

internal actual object PlatformRegionContext {
    private var applicationContext: Context? = null

    actual fun initialize() {
        // MainActivity initializes this before repositories and TMDB settings are created.
        // The Android application context is supplied through RegionContextAndroid.initialize.
    }

    fun initialize(context: Context) {
        applicationContext = context.applicationContext
    }

    actual fun currentCountryCode(): String? {
        val context = applicationContext ?: return null
        val telephonyManager = context.getSystemService(TelephonyManager::class.java) ?: return null

        val networkCountry = runCatching { telephonyManager.networkCountryIso }
            .getOrNull()
            ?.trim()
            ?.takeIf { it.length == 2 && it.all(Char::isLetter) }
            ?.uppercase()

        return networkCountry
    }
}
