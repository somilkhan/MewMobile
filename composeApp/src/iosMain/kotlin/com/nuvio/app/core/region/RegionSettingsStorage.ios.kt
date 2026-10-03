package com.nuvio.app.core.region

import platform.Foundation.NSUserDefaults

actual object RegionSettingsStorage {
    private const val manualCountryCodeKey = "nuvio_region_manual_country_code"

    actual fun loadManualCountryCode(): String? =
        NSUserDefaults.standardUserDefaults.stringForKey(manualCountryCodeKey)

    actual fun saveManualCountryCode(countryCode: String?) {
        if (countryCode == null) {
            NSUserDefaults.standardUserDefaults.removeObjectForKey(manualCountryCodeKey)
        } else {
            NSUserDefaults.standardUserDefaults.setObject(countryCode, forKey = manualCountryCodeKey)
        }
    }
}
