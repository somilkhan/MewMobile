package com.nuvio.app.core.region

import android.content.Context

actual object RegionSettingsStorage {
    private const val preferencesName = "nuvio_region_settings"
    private const val manualCountryCodeKey = "manual_country_code"
    private var preferences: android.content.SharedPreferences? = null

    fun initialize(context: Context) {
        preferences = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)
    }

    actual fun loadManualCountryCode(): String? =
        preferences?.getString(manualCountryCodeKey, null)

    actual fun saveManualCountryCode(countryCode: String?) {
        preferences?.edit()?.apply {
            if (countryCode == null) remove(manualCountryCodeKey)
            else putString(manualCountryCodeKey, countryCode)
        }?.apply()
    }
}
