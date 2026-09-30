package com.nuvio.app.core.region

import com.nuvio.app.features.player.DeviceLanguagePreferences

enum class RegionContextSource {
    NETWORK,
    DEVICE_LOCALE,
    GLOBAL,
}

data class RegionContext(
    val countryCode: String?,
    val languageTag: String,
    val source: RegionContextSource,
) {
    companion object {
        fun current(): RegionContext {
            val languageTag = DeviceLanguagePreferences.preferredLanguageCodes()
                .firstOrNull()
                ?.trim()
                ?.replace('_', '-')
                ?.takeIf { it.isNotBlank() }
                ?.let(::normalizeLanguageTag)
                ?: "en"

            val networkCountryCode = PlatformRegionContext.currentCountryCode()
            val localeCountryCode = languageTag
                .split('-')
                .drop(1)
                .firstOrNull { it.length == 2 && it.all(Char::isLetter) }
                ?.uppercase()

            return when {
                networkCountryCode != null -> RegionContext(
                    countryCode = networkCountryCode,
                    languageTag = languageTag,
                    source = RegionContextSource.NETWORK,
                )
                localeCountryCode != null -> RegionContext(
                    countryCode = localeCountryCode,
                    languageTag = languageTag,
                    source = RegionContextSource.DEVICE_LOCALE,
                )
                else -> RegionContext(
                    countryCode = null,
                    languageTag = languageTag,
                    source = RegionContextSource.GLOBAL,
                )
            }
        }
    }
}

private fun normalizeLanguageTag(value: String): String {
    val parts = value.split('-').filter { it.isNotBlank() }
    if (parts.isEmpty()) return "en"
    return buildString {
        append(parts.first().lowercase())
        parts.drop(1).forEach { part ->
            append('-')
            append(if (part.length == 2) part.uppercase() else part)
        }
    }
}

internal expect object PlatformRegionContext {
    fun currentCountryCode(): String?
}
