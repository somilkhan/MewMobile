package com.nuvio.app.core.region

internal expect object RegionSettingsStorage {
    fun loadManualCountryCode(): String?
    fun saveManualCountryCode(countryCode: String?)
}
