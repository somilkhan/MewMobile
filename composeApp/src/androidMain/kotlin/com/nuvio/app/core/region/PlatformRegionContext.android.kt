package com.nuvio.app.core.region

import android.content.Context
import android.telephony.TelephonyManager

internal actual object PlatformRegionContext {
    private var applicationContext: Context? = null

    fun initialize(context: Context) {
        applicationContext = context.applicationContext
    }

    actual fun currentCountryCode(): String? {
        val context = applicationContext ?: return null
        val telephonyManager = context.getSystemService(TelephonyManager::class.java) ?: return null

        return runCatching { telephonyManager.networkCountryIso }
            .getOrNull()
            ?.trim()
            ?.takeIf { it.length == 2 && it.all(Char::isLetter) }
            ?.uppercase()
    }
}
