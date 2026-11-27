package com.nuvio.app.features.mdblist

import com.nuvio.app.core.logging.InAppLogger
import com.nuvio.app.features.details.MetaDetails
import com.nuvio.app.features.details.MetaExternalRating
import com.nuvio.app.features.tmdb.TmdbService
import com.nuvio.app.features.tmdb.TmdbSettingsRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object MdbListMetadataService {
    const val PROVIDER_IMDB = "imdb"
    const val PROVIDER_TMDB = "tmdb"
    const val PROVIDER_TOMATOES = "tomatoes"
    const val PROVIDER_METACRITIC = "metacritic"
    const val PROVIDER_TRAKT = "trakt"
    const val PROVIDER_LETTERBOXD = "letterboxd"
    const val PROVIDER_AUDIENCE = "audience"
    const val PROVIDER_MAL = "mal"

    val PROVIDER_PRIORITY_ORDER = listOf(
        PROVIDER_IMDB,
        PROVIDER_TMDB,
        PROVIDER_TOMATOES,
        PROVIDER_METACRITIC,
        PROVIDER_TRAKT,
        PROVIDER_LETTERBOXD,
        PROVIDER_AUDIENCE,
        PROVIDER_MAL,
    )

    private val ratingsRepository = lazy { MdbListRatingsRepository(MdbListTracker.ratings) }
    private val repository by ratingsRepository
    private val imdbRegex = Regex("tt\\d+")
    private val tmdbRegex = Regex("(?:tmdb[:/])?(\\d+)")
    private const val TmdbToImdbTimeoutMs = 4_000L

    fun shouldFetchForMeta(
        meta: MetaDetails,
        fallbackItemId: String,
        settings: MdbListSettings,
    ): Boolean {
        if (!settings.isActive) return false
        if (settings.enabledProvidersInPriorityOrder().isEmpty()) return false
        if (extractImdbId(meta.id) != null || extractImdbId(fallbackItemId) != null) return true
        if (extractTmdbId(meta.id) == null && extractTmdbId(fallbackItemId) == null) return false
        TmdbSettingsRepository.ensureLoaded()
        return TmdbSettingsRepository.snapshot().hasApiKey
    }

    suspend fun enrichMeta(
        meta: MetaDetails,
        fallbackItemId: String,
        settings: MdbListSettings,
    ): MetaDetails {
        if (!shouldFetchForMeta(meta, fallbackItemId, settings)) {
            InAppLogger.debug(
                "Metadata/MDBList",
                "skip metaId=${meta.id} fallback=$fallbackItemId enabled=${settings.enabled} " +
                    "providers=${settings.enabledProvidersInPriorityOrder().joinToString(",")}",
            )
            return meta
        }
        val credential = settings.credential ?: return meta.copy(externalRatings = emptyList())

        val imdbId = resolveImdbId(meta = meta, fallbackItemId = fallbackItemId)
            ?: return meta
        val mediaType = toMdbListMediaType(meta.type)
        val enabledProviders = settings.enabledProvidersInPriorityOrder()

        InAppLogger.info(
            "Metadata/MDBList",
            "fetch ratings imdb=$imdbId mediaType=$mediaType providers=${enabledProviders.joinToString(",")}",
        )

        val ratings = repository.getRatings(
            imdbId = imdbId,
            mediaType = mediaType,
            credential = credential,
            providers = enabledProviders,
        )

        InAppLogger.info(
            "Metadata/MDBList",
            "ratings result imdb=$imdbId mediaType=$mediaType count=${ratings.size}",
        )
        return meta.copy(externalRatings = mergeExternalRatings(meta.externalRatings, ratings))
    }

    fun clearCache() {
        if (ratingsRepository.isInitialized()) repository.clearCache()
    }

    private fun extractImdbId(value: String?): String? {
        if (value.isNullOrBlank()) return null
        return imdbRegex.find(value)?.value
    }

    private fun extractTmdbId(value: String?): Int? {
        if (value.isNullOrBlank()) return null
        val normalized = value
            .removePrefix("tmdb:")
            .removePrefix("movie:")
            .removePrefix("series:")
            .substringBefore(':')
            .substringBefore('/')
            .trim()
        normalized.toIntOrNull()?.takeIf { it > 0 }?.let { return it }
        return tmdbRegex.matchEntire(value.trim())
            ?.groupValues
            ?.getOrNull(1)
            ?.toIntOrNull()
            ?.takeIf { it > 0 }
    }

    private suspend fun resolveImdbId(meta: MetaDetails, fallbackItemId: String): String? {
        extractImdbId(meta.id)?.let { return it }
        extractImdbId(fallbackItemId)?.let { return it }

        val tmdbId = extractTmdbId(meta.id) ?: extractTmdbId(fallbackItemId) ?: return null
        TmdbSettingsRepository.ensureLoaded()
        if (!TmdbSettingsRepository.snapshot().hasApiKey) return null

        return withTimeoutOrNull(TmdbToImdbTimeoutMs) {
            TmdbService.tmdbToImdb(tmdbId = tmdbId, mediaType = meta.type)
        }?.takeIf { it.isNotBlank() }
    }

    private fun mergeExternalRatings(
        current: List<MetaExternalRating>,
        mdbListRatings: List<MetaExternalRating>,
    ): List<MetaExternalRating> {
        if (mdbListRatings.isEmpty()) return current
        val bySource = linkedMapOf<String, MetaExternalRating>()
        current.forEach { rating ->
            bySource[rating.source.trim().lowercase()] = rating
        }
        mdbListRatings.forEach { rating ->
            bySource[rating.source.trim().lowercase()] = rating
        }
        return bySource.values.toList()
    }

    private fun toMdbListMediaType(metaType: String): String {
        val normalized = metaType.trim().lowercase()
        return if (normalized == "movie") "movie" else "show"
    }
}
