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
        trimmed.startsWith("tmdb:") -> trimmed.removePrefix("tmdb:")
        trimmed.startsWith("tmdb/") -> trimmed.removePrefix("tmdb/")
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
 * Plugin getStreams() contracts use a TMDB numeric id, not an IMDb id or display title.
 * Resolve external identities before crossing the plugin bridge; unresolved values are
 * intentionally omitted instead of triggering malformed /movie/{title} or /tv/tt... calls.
 */
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
        return TmdbService.ensureTmdbId(normalized, mediaType)
            ?.takeIf { it.toLongOrNull()?.let { id -> id > 0 } == true }
    }

    val query = searchTitle?.trim()?.takeIf(String::isNotBlank) ?: normalized
    val results = TmdbService.search(query, limit = 12)
    val selected = selectPluginTmdbSearchResult(results, query, mediaType) ?: return null
    return selected.id.removePrefix("tmdb:")
        .takeIf { it.toLongOrNull()?.let { id -> id > 0 } == true }
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
