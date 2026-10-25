package com.nuvio.app.features.tmdb

data class TmdbSettings(
    val enabled: Boolean = false,
    val apiKey: String = "",
    val language: String = "en",
    val useTrailers: Boolean = true,
    val useArtwork: Boolean = true,
    val useBasicInfo: Boolean = true,
    val useDetails: Boolean = true,
    val useReleaseDates: Boolean = false,
    val useCredits: Boolean = true,
    val useProductions: Boolean = true,
    val useNetworks: Boolean = true,
    val useEpisodes: Boolean = true,
    val useSeasonPosters: Boolean = true,
    val useMoreLikeThis: Boolean = true,
    val useCollections: Boolean = true,
    val titleLanguage: TmdbTitleLanguage = TmdbTitleLanguage.APP,
) {
    val hasApiKey: Boolean
        get() = apiKey.isNotBlank()

    val canUseMetadata: Boolean
        get() = enabled && hasApiKey
}


enum class TmdbTitleLanguage(val storageId: String) {
    APP("app"),
    ORIGINAL("original");

    companion object {
        fun fromStorageId(value: String?): TmdbTitleLanguage =
            entries.firstOrNull { it.storageId == value } ?: APP
    }
}

internal fun selectTmdbDisplayTitle(
    titleLanguage: TmdbTitleLanguage,
    localizedTitle: String?,
    originalTitle: String?,
): String? {
    val localized = localizedTitle?.trim()?.takeIf(String::isNotBlank)
    val original = originalTitle?.trim()?.takeIf(String::isNotBlank)
    return if (titleLanguage == TmdbTitleLanguage.ORIGINAL) original ?: localized else localized ?: original
}
