package com.nuvio.app.features.plugins

import com.nuvio.app.features.home.MetaPreview
import com.nuvio.app.features.tmdb.TmdbService

internal fun pluginContentId(
    videoId: String,
    season: Int?,
    episode: Int?,
): String {
    val trimmed = videoId.trim()
    if (trimmed.isBlank()) return videoId

    val withoutPrefix = when {
        trimmed.startsWith("tmdb:", ignoreCase = true) -> trimmed.substringAfter(':')
        trimmed.startsWith("tmdb/", ignoreCase = true) -> trimmed.substringAfter('/')
        trimmed.startsWith("movie:", ignoreCase = true) -> trimmed.substringAfter(':')
        trimmed.startsWith("series:", ignoreCase = true) -> trimmed.substringAfter(':')
        else -> trimmed
    }

    val withoutEpisodeSuffix = if (season != null && episode != null) {
        withoutPrefix.removeSuffix(":$season:$episode")
    } else {
        withoutPrefix
    }

    return withoutEpisodeSuffix.substringBefore('/').ifBlank { trimmed }
}

/**
 * Prefer a canonical TMDB numeric ID when it can be resolved. Keep the normalized source
 * identity as a compatibility fallback, matching Nuvio Enhanced: some plugin repositories
 * resolve IMDb IDs or titles themselves, and a disabled/missing TMDB key must not suppress
 * every scraper before its own code gets a chance to run.
 */
internal fun pluginTmdbIdOrSourceIdentity(
    normalizedId: String,
    resolvedTmdbId: String?,
): String = resolvedTmdbId
    ?.takeIf { it.toLongOrNull()?.let { id -> id > 0 } == true }
    ?: normalizedId

internal suspend fun resolvePluginTmdbId(
    videoId: String,
    mediaType: String,
    searchTitle: String? = null,
    season: Int? = null,
    episode: Int? = null,
): String? {
    val normalized = pluginContentId(videoId, season, episode).trim()
    if (normalized.isBlank()) return null
    if (normalized.toLongOrNull()?.let { it > 0 } == true) return normalized
    if (normalized.startsWith("tt", ignoreCase = true)) {
        return pluginTmdbIdOrSourceIdentity(
            normalizedId = normalized,
            resolvedTmdbId = TmdbService.ensureTmdbId(normalized, mediaType),
        )
    }

    val query = searchTitle?.trim()?.takeIf(String::isNotBlank) ?: normalized
    val results = TmdbService.search(query, limit = 12)
    val selectedTmdbId = selectPluginTmdbSearchResult(results, query, mediaType)
        ?.id
        ?.removePrefix("tmdb:")
    return pluginTmdbIdOrSourceIdentity(
        normalizedId = normalized,
        resolvedTmdbId = selectedTmdbId,
    )
}

internal fun selectPluginTmdbSearchResult(
    results: List<MetaPreview>,
    query: String,
    mediaType: String,
): MetaPreview? {
    val expectedType = when (mediaType.trim().lowercase()) {
        "movie", "film" -> "movie"
        "tv", "series", "show", "tvshow", "dizi" -> "series"
        else -> mediaType.trim().lowercase()
    }
    val candidates = results.filter { it.type.equals(expectedType, ignoreCase = true) }
    if (candidates.isEmpty()) return null
    val normalizedQuery = query.trim().lowercase()
    // Never silently choose an unrelated title (e.g. a remake or similarly named show).
    return candidates.firstOrNull { it.name.trim().equals(normalizedQuery, ignoreCase = true) }
}
