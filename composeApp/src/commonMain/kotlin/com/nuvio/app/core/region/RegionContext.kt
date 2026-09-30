package com.nuvio.app.core.region

import com.nuvio.app.features.player.DeviceLanguagePreferences

enum class RegionContextSource {
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
            val countryCode = languageTag
                .split('-')
                .drop(1)
                .firstOrNull { it.length == 2 && it.all(Char::isLetter) }
                ?.uppercase()

            return RegionContext(
                countryCode = countryCode,
                languageTag = languageTag,
                source = if (countryCode != null) RegionContextSource.DEVICE_LOCALE else RegionContextSource.GLOBAL,
            )
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
