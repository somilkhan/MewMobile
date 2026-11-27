package com.nuvio.app.features.details

import co.touchlab.kermit.Logger
import com.nuvio.app.core.logging.InAppLogger
import com.nuvio.app.core.diagnostics.MetadataLoadOutcome
import com.nuvio.app.core.diagnostics.MetadataLoadPath
import com.nuvio.app.core.diagnostics.MetadataLoadTrigger
import com.nuvio.app.core.diagnostics.MetadataPublicationStage
import com.nuvio.app.core.diagnostics.RuntimeDiagnostics
import com.nuvio.app.features.addons.AddonManifest
import com.nuvio.app.features.addons.AddonRepository
import com.nuvio.app.features.addons.buildAddonResourceUrl
import com.nuvio.app.features.addons.enabledAddons
import com.nuvio.app.features.addons.fetchAddonResponseText
import com.nuvio.app.features.addons.httpRequestRaw
import com.nuvio.app.features.addons.supportsResource
import com.nuvio.app.core.poster.withCustomPosterUrls
import com.nuvio.app.features.servers.ServerCatalog
import com.nuvio.app.features.servers.ServerItemRef
import com.nuvio.app.features.servers.ServerUserStateProjection
import com.nuvio.app.features.servers.message
import com.nuvio.app.features.servers.serverFailure
import com.nuvio.app.features.tmdb.TMDB_RECOMMENDATIONS_PAGE_SIZE
import com.nuvio.app.features.trakt.MoreLikeThisSourcePreference
import com.nuvio.app.features.watched.WatchedClock
import com.nuvio.app.features.cloudstream.CloudStreamRouteData
import com.nuvio.app.features.cloudstream.parseCloudStreamRouteId
import com.nuvio.app.features.home.HomeCatalogSettingsRepository
import com.nuvio.app.features.home.filterReleasedItems
import com.nuvio.app.features.mdblist.MdbListMetadataService
import com.nuvio.app.features.mdblist.MdbListSettingsRepository
import com.nuvio.app.features.tmdb.TmdbMetadataService
import com.nuvio.app.features.tmdb.TmdbService
import com.nuvio.app.features.tmdb.TmdbSettings
import com.nuvio.app.features.tmdb.TmdbSettingsRepository
import com.nuvio.app.features.trakt.TraktAuthRepository
import com.nuvio.app.features.trakt.TraktConnectionMode
import com.nuvio.app.features.trakt.TraktRelatedRepository
import com.nuvio.app.features.tracking.TrackingSettingsRepository
import com.nuvio.app.features.trakt.shouldUseTraktMoreLikeThis
import com.nuvio.app.features.watchprogress.CurrentDateProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlin.time.TimeSource
import nuvio.composeapp.generated.resources.*
import org.jetbrains.compose.resources.getString

object MetaDetailsRepository {
    private data class CachedMetaEntry(
        val baseMeta: MetaDetails,
        val metaScreenMeta: MetaDetails? = null,
        val metaScreenSettingsFingerprint: String? = null,
    )

    private enum class BaseMetadataSource {
        Addon,
        TmdbFallback,
        CloudStream,
        Server,
    }

    private data class BaseMetadataResult(
        val meta: MetaDetails,
        val source: BaseMetadataSource,
        val fallbackItemId: String,
    )

    private val log = Logger.withTag("MetaDetailsRepo")
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _uiState = MutableStateFlow(MetaDetailsUiState())
    val uiState: StateFlow<MetaDetailsUiState> = _uiState.asStateFlow()
    private var activeJob: Job? = null
    private var activeRequestKey: String? = null
    private var activeSettingsFingerprint: String? = null
    private var activeRequestGeneration = 0L
    private var cacheGeneration = 0L
    private val cacheLock = SynchronizedObject()
    private val cachedMetaByRequestKey = mutableMapOf<String, CachedMetaEntry>()
    private val baseRequestCoordinator = MetadataRequestCoordinator<BaseMetadataResult?>(scope)

    fun load(type: String, id: String, trigger: MetadataLoadTrigger = MetadataLoadTrigger.Initial) {
        log.d { "load() called — type=$type id=$id" }
        val requestKey = metaDetailsRequestKey(type, id)
        ServerItemRef.parse(id)?.let { ref ->
            loadServerMeta(requestKey, ref)
            return
        }
        val cloudStreamRoute = parseCloudStreamRouteId(id)
        if (cloudStreamRoute != null) {
            if (!isCloudStreamRouteAllowed(id)) {
                activeRequestGeneration += 1
                activeJob?.cancel()
                activeJob = null
                activeRequestKey = requestKey
                activeSettingsFingerprint = null
                _uiState.value = MetaDetailsUiState(
                    requestKey = requestKey,
                    errorMessage = "CloudStream detail could not be loaded",
                )
                RuntimeDiagnostics.updateMetadataLoading(false)
                return
            }
            loadCloudStream(type = type, id = id, requestKey = requestKey, route = cloudStreamRoute, trigger = trigger)
            return
        }
        val currentState = _uiState.value
        val mdbListSettings = MdbListSettingsRepository.snapshot()
        val metaScreenSettingsFingerprint = buildMetaScreenSettingsFingerprint(mdbListSettings)

        cachedEntry(requestKey)?.let { cachedEntry ->
            val cachedScreenMeta = cachedEntry.metaScreenMeta
                ?.takeIf { cachedEntry.metaScreenSettingsFingerprint == metaScreenSettingsFingerprint }

            val cachedBaseMeta = cachedEntry.baseMeta
            if (
                currentState.isLoading &&
                activeJob?.isActive == true &&
                activeRequestKey == requestKey &&
                activeSettingsFingerprint == metaScreenSettingsFingerprint
            ) {
                log.d { "Meta screen enrichment already in flight — type=$type id=$id" }
                RuntimeDiagnostics.recordMetadataCoalesced(trigger, additionalCall = true)
                return
            }

            val requestGeneration = ++activeRequestGeneration
            activeRequestKey = requestKey
            activeSettingsFingerprint = metaScreenSettingsFingerprint
            activeJob?.cancel()

            if (cachedScreenMeta != null) {
                activeJob = null
                val nextState = MetaDetailsUiState(
                    requestKey = requestKey,
                    meta = cachedScreenMeta.withUnreleasedFilter(),
                )
                if (_uiState.value != nextState) _uiState.value = nextState
                RuntimeDiagnostics.updateMetadataLoading(false)
                RuntimeDiagnostics.recordMetadataCacheHit(trigger)
                return
            }

            val diagnosticOperation = RuntimeDiagnostics.startMetadataLoad(
                trigger = trigger,
                path = MetadataLoadPath.CachedBase,
            )
            val diagnosticStart = TimeSource.Monotonic.markNow()
            RuntimeDiagnostics.updateMetadataLoading(true)
            val currentVisibleMeta = currentState.meta
                ?.takeIf { currentState.requestKey == requestKey }
                ?: cachedBaseMeta.withUnreleasedFilter()
            _uiState.value = MetaDetailsUiState(
                requestKey = requestKey,
                isLoading = true,
                meta = currentVisibleMeta,
            )

            activeJob = scope.launch {
                var outcome = MetadataLoadOutcome.Completed
                try {
                    val enrichedMeta = withContext(Dispatchers.Default) {
                        enrichForMetaScreen(
                            meta = cachedBaseMeta,
                            fallbackItemId = id,
                            fallbackItemType = type,
                            settings = mdbListSettings,
                        )
                    }
                    if (!isCurrentRequest(requestKey, metaScreenSettingsFingerprint, requestGeneration)) return@launch
                    putCachedEntry(requestKey, cachedEntry.copy(
                        metaScreenMeta = enrichedMeta,
                        metaScreenSettingsFingerprint = metaScreenSettingsFingerprint,
                    ))
                    val nextState = MetaDetailsUiState(
                        requestKey = requestKey,
                        meta = enrichedMeta.withUnreleasedFilter(),
                    )
                    if (_uiState.value != nextState) {
                        _uiState.value = nextState
                        RuntimeDiagnostics.recordMetadataPublication(MetadataPublicationStage.Final)
                    }
                } catch (error: Throwable) {
                    if (error is CancellationException) {
                        outcome = MetadataLoadOutcome.Cancelled
                        throw error
                    }
                    outcome = MetadataLoadOutcome.Failed
                    log.e(error) { "Meta screen enrichment failed — type=$type id=$id" }
                } finally {
                    RuntimeDiagnostics.finishMetadataLoad(
                        operation = diagnosticOperation,
                        outcome = outcome,
                        durationMs = diagnosticStart.elapsedNow().inWholeMilliseconds,
                    )
                    finishCurrentRequest(requestKey, metaScreenSettingsFingerprint, requestGeneration)
                }
            }
            return
        }

        if (currentState.meta?.type == type && currentState.meta.id == id && !currentState.isLoading) {
            log.d { "Skipping reload for cached meta — type=$type id=$id" }
            activeRequestKey = requestKey
            RuntimeDiagnostics.recordMetadataCacheHit(trigger)
            return
        }

        if (
            currentState.isLoading &&
            activeJob?.isActive == true &&
            activeRequestKey == requestKey &&
            activeSettingsFingerprint == metaScreenSettingsFingerprint
        ) {
            log.d { "Request already in flight — type=$type id=$id" }
            RuntimeDiagnostics.recordMetadataCoalesced(trigger, additionalCall = true)
            return
        }

        val requestGeneration = ++activeRequestGeneration
        activeRequestKey = requestKey
        activeSettingsFingerprint = metaScreenSettingsFingerprint
        activeJob?.cancel()
        val diagnosticOperation = RuntimeDiagnostics.startMetadataLoad(
            trigger = trigger,
            path = MetadataLoadPath.Network,
        )
        val diagnosticStart = TimeSource.Monotonic.markNow()
        RuntimeDiagnostics.updateMetadataLoading(true)
        _uiState.value = MetaDetailsUiState(requestKey = requestKey, isLoading = true)

        activeJob = scope.launch {
            var outcome = MetadataLoadOutcome.Completed
            try {
                val baseResult = fetchSharedBase(type = type, id = id, requestKey = requestKey)
                if (baseResult.coalesced) RuntimeDiagnostics.recordMetadataCoalesced(trigger)
                val loaded = baseResult.value
                if (loaded != null) {
                    publishLoadedMeta(
                        requestKey = requestKey,
                        meta = loaded.meta,
                        fallbackItemId = loaded.fallbackItemId,
                        fallbackItemType = type,
                        mdbListSettings = mdbListSettings,
                        metaScreenSettingsFingerprint = metaScreenSettingsFingerprint,
                        requestGeneration = requestGeneration,
                    )
                    return@launch
                }

                if (!isCurrentRequest(requestKey, metaScreenSettingsFingerprint, requestGeneration)) return@launch
                log.w { "No metadata source returned content for type=$type id=$id" }
                _uiState.value = MetaDetailsUiState(
                    requestKey = requestKey,
                    errorMessage = getString(Res.string.details_no_addon_meta),
                )
                outcome = MetadataLoadOutcome.Failed
            } catch (error: Throwable) {
                if (error is CancellationException) {
                    outcome = MetadataLoadOutcome.Cancelled
                    throw error
                }
                outcome = MetadataLoadOutcome.Failed
                log.e(error) { "Unexpected metadata load failure — type=$type id=$id" }
                if (isCurrentRequest(requestKey, metaScreenSettingsFingerprint, requestGeneration)) {
                    _uiState.value = MetaDetailsUiState(
                        requestKey = requestKey,
                        errorMessage = getString(Res.string.details_load_failed_all_addons),
                    )
                }
            } finally {
                RuntimeDiagnostics.finishMetadataLoad(
                    operation = diagnosticOperation,
                    outcome = outcome,
                    durationMs = diagnosticStart.elapsedNow().inWholeMilliseconds,
                )
                finishCurrentRequest(requestKey, metaScreenSettingsFingerprint, requestGeneration)
            }
        }
    }

    fun peek(type: String, id: String): MetaDetails? {
        val requestKey = metaDetailsRequestKey(type, id)
        val metaScreenSettingsFingerprint = buildMetaScreenSettingsFingerprint(MdbListSettingsRepository.snapshot())
        val currentMeta = _uiState.value.meta?.takeIf {
            it.type == type && it.id == id && activeSettingsFingerprint == metaScreenSettingsFingerprint
        }
        if (currentMeta != null) return currentMeta

        val cachedEntry = cachedEntry(requestKey) ?: return null
        return cachedEntry.metaScreenMeta
            ?.takeIf { cachedEntry.metaScreenSettingsFingerprint == metaScreenSettingsFingerprint }
            ?: cachedEntry.baseMeta
    }

    fun invalidateTmdbEnrichment() {
        val currentState = _uiState.value
        val requestKey = currentState.requestKey
        val tmdbEnabled = TmdbSettingsRepository.snapshot().enabled
        val currentRequestIsCloudStream =
            currentState.meta?.id?.let(::parseCloudStreamRouteId) != null ||
                requestKey?.substringAfter(':')?.let(::parseCloudStreamRouteId) != null
        activeJob?.cancel()
        activeJob = null
        activeRequestGeneration += 1
        activeSettingsFingerprint = null

        synchronized(cacheLock) {
            cacheGeneration += 1
            val iterator = cachedMetaByRequestKey.entries.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                if (entry.value.baseMeta.id.startsWith("tmdb:", ignoreCase = true)) {
                    iterator.remove()
                } else {
                    entry.setValue(
                        entry.value.copy(
                            metaScreenMeta = null,
                            metaScreenSettingsFingerprint = null,
                        ),
                    )
                }
            }
        }

        val cachedBase = requestKey?.let(::cachedEntry)
        val currentCloudStreamRouteIsDisabled = tmdbEnabled && currentRequestIsCloudStream
        val shouldReloadCloudStreamRoute = !tmdbEnabled && currentRequestIsCloudStream && cachedBase == null
        activeRequestKey = requestKey?.takeIf { cachedBase != null && !currentCloudStreamRouteIsDisabled }
        if (currentCloudStreamRouteIsDisabled) {
            _uiState.value = MetaDetailsUiState(
                requestKey = requestKey,
                errorMessage = "CloudStream detail could not be loaded",
            )
        } else if (cachedBase != null) {
            _uiState.value = MetaDetailsUiState(
                requestKey = requestKey,
                meta = cachedBase.baseMeta.withUnreleasedFilter(),
            )
        } else if (requestKey != null && currentState.meta?.id?.startsWith("tmdb:", ignoreCase = true) == true) {
            // Remove the stale standalone TMDB result without launching network work from settings.
            // The detail route will fetch again when it is next loaded with the new settings.
            _uiState.value = MetaDetailsUiState(requestKey = requestKey)
        } else if (shouldReloadCloudStreamRoute) {
            _uiState.value = MetaDetailsUiState(requestKey = requestKey)
        } else if (currentState.isLoading) {
            _uiState.value = currentState.copy(isLoading = false)
        }
        RuntimeDiagnostics.updateMetadataLoading(false)
        if (shouldReloadCloudStreamRoute && requestKey != null) {
            load(
                type = requestKey.substringBefore(':'),
                id = requestKey.substringAfter(':'),
                trigger = MetadataLoadTrigger.Initial,
            )
        }
    }

    fun clear() {
        activeJob?.cancel()
        activeJob = null
        activeRequestKey = null
        activeSettingsFingerprint = null
        activeRequestGeneration += 1
        synchronized(cacheLock) {
            cacheGeneration += 1
            cachedMetaByRequestKey.clear()
        }
        _uiState.value = MetaDetailsUiState()
        RuntimeDiagnostics.updateMetadataLoading(false)
    }

    fun retry(type: String, id: String) {
        val requestKey = "$type:$id"
        synchronized(cacheLock) { cachedMetaByRequestKey.remove(requestKey) }
        if (activeRequestKey == requestKey) {
            activeRequestKey = null
            activeSettingsFingerprint = null
            _uiState.value = MetaDetailsUiState()
        }
        load(type, id, trigger = MetadataLoadTrigger.Retry)
    }

    suspend fun withPreferredMoreLikeThis(
        meta: MetaDetails,
        fallbackItemId: String,
        fallbackItemType: String,
    ): MetaDetails {
        if (meta.moreLikeThisSource == MoreLikeThisSource.SIMKL || meta.moreLikeThisSource == MoreLikeThisSource.TRAKT) {
            return meta
        }
        return withContext(Dispatchers.Default) {
            applyMoreLikeThisSource(
                meta = meta,
                fallbackItemId = fallbackItemId,
                fallbackItemType = fallbackItemType,
            )
        }.withUnreleasedFilter()
    }

    fun clearCachedMetadata() {
        synchronized(cacheLock) {
            cacheGeneration += 1
            cachedMetaByRequestKey.clear()
        }
    }

    suspend fun fetch(
        type: String,
        id: String,
        cacheResult: Boolean = true,
        useCache: Boolean = true,
    ): MetaDetails? {
        if (!useCache) synchronized(cacheLock) { cachedMetaByRequestKey.remove(metaDetailsRequestKey(type, id)) }
        return fetchInternal(type = type, id = id, cacheResult = cacheResult, includeOptionalEnrichment = true)
    }

    suspend fun fetchBase(type: String, id: String, cacheResult: Boolean = true): MetaDetails? =
        fetchInternal(type = type, id = id, cacheResult = cacheResult, includeOptionalEnrichment = false)

    private suspend fun fetchInternal(
        type: String,
        id: String,
        cacheResult: Boolean,
        includeOptionalEnrichment: Boolean,
    ): MetaDetails? {
        if (!isCloudStreamRouteAllowed(id)) return null
        val requestKey = metaDetailsRequestKey(type, id)
        cachedEntry(requestKey)?.let {
            RuntimeDiagnostics.recordMetadataCacheHit(MetadataLoadTrigger.Background)
            return if (includeOptionalEnrichment) {
                enrichWithTmdb(meta = it.baseMeta, fallbackItemId = id)
            } else {
                it.baseMeta
            }
        }

        val diagnosticPath = if (parseCloudStreamRouteId(id) != null) {
            MetadataLoadPath.CloudStream
        } else {
            MetadataLoadPath.Network
        }
        val diagnosticOperation = RuntimeDiagnostics.startMetadataLoad(MetadataLoadTrigger.Background, diagnosticPath)
        val diagnosticStart = TimeSource.Monotonic.markNow()
        var outcome = MetadataLoadOutcome.Completed
        return try {
            val fetched = if (cacheResult) {
                fetchSharedBase(type = type, id = id, requestKey = requestKey).also {
                    if (it.coalesced) RuntimeDiagnostics.recordMetadataCoalesced(MetadataLoadTrigger.Background)
                }.value
            } else {
                fetchBaseUncached(type = type, id = id)
            } ?: run {
                outcome = MetadataLoadOutcome.Failed
                return null
            }
            if (includeOptionalEnrichment && fetched.source == BaseMetadataSource.Addon) {
                enrichWithTmdb(meta = fetched.meta, fallbackItemId = fetched.fallbackItemId)
            } else {
                fetched.meta
            }
        } catch (error: Throwable) {
            outcome = if (error is CancellationException) MetadataLoadOutcome.Cancelled else MetadataLoadOutcome.Failed
            throw error
        } finally {
            RuntimeDiagnostics.finishMetadataLoad(
                operation = diagnosticOperation,
                outcome = outcome,
                durationMs = diagnosticStart.elapsedNow().inWholeMilliseconds,
            )
        }
    }

    // Upstream (luq): lightweight background lookups and media-server details.
    private val backgroundBackoffUntilByHost = mutableMapOf<String, Long>()

    internal suspend fun fetchLightweight(type: String, id: String): MetaLookupOutcome {
        val metaLookupId = resolveMetaLookupId(itemId = id, itemType = type)
        val manifests = findReadyMetaManifests(type = type, id = metaLookupId)
        if (manifests.isEmpty()) {
            return tryFetchTmdbFallbackMeta(type = type, id = id)
                ?.let { meta -> MetaLookupOutcome.Loaded(meta) }
                ?: MetaLookupOutcome.Unavailable
        }

        var throttledUntil: Long? = null
        for (manifest in manifests) {
            val host = manifest.transportUrl.addonHost()
            val now = WatchedClock.nowEpochMs()
            val blockedUntil = backgroundBackoffUntilByHost[host]
            if (blockedUntil != null && blockedUntil > now) {
                throttledUntil = minOf(throttledUntil ?: blockedUntil, blockedUntil)
                continue
            }
            val url = buildAddonResourceUrl(
                manifestUrl = manifest.transportUrl,
                resource = "meta",
                type = type,
                id = metaLookupId,
            )
            val response = try {
                withTimeoutOrNull(FETCH_TIMEOUT_MS) {
                    httpRequestRaw(
                        method = "GET",
                        url = url,
                        headers = mapOf("Accept" to "application/json"),
                        body = "",
                        maxResponseBodyBytes = LIGHTWEIGHT_META_MAX_BYTES,
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                log.w(error) { "Lightweight meta request failed url=$url" }
                null
            }
            if (response == null) continue

            if (response.status == 429 || response.status == 503) {
                val until = now + response.retryAfterMs()
                backgroundBackoffUntilByHost[host] = until
                InAppLogger.warn(
                    "Metadata/AddonFetch",
                    "Background lookups paused host=$host status=${response.status} for ${(until - now) / 1000}s",
                )
                throttledUntil = minOf(throttledUntil ?: until, until)
                continue
            }
            if (response.status !in 200..299 || response.body.isBlank()) continue

            val meta = try {
                withContext(Dispatchers.Default) { MetaDetailsParser.parse(response.body) }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                log.w(error) { "Failed to parse lightweight meta url=$url" }
                null
            }
            if (meta != null) return MetaLookupOutcome.Loaded(meta)
        }

        return throttledUntil?.let { until -> MetaLookupOutcome.Throttled(until) } ?: MetaLookupOutcome.Failed
    }

    private fun String.addonHost(): String =
        substringAfter("://").substringBefore('/').substringBefore('?').lowercase()

    private fun com.nuvio.app.features.addons.RawHttpResponse.retryAfterMs(): Long {
        val seconds = headers["retry-after"]?.trim()?.toLongOrNull()
        val delayMs = seconds?.times(1_000L) ?: DEFAULT_BACKGROUND_BACKOFF_MS
        return delayMs.coerceIn(MIN_BACKGROUND_BACKOFF_MS, MAX_BACKGROUND_BACKOFF_MS)
    }

    private const val LIGHTWEIGHT_META_MAX_BYTES = 8 * 1024 * 1024
    private const val DEFAULT_BACKGROUND_BACKOFF_MS = 2L * 60_000L
    private const val MIN_BACKGROUND_BACKOFF_MS = 30_000L
    private const val MAX_BACKGROUND_BACKOFF_MS = 15L * 60_000L

    private fun loadServerMeta(requestKey: String, ref: ServerItemRef) {
        if (_uiState.value.isLoading && activeRequestKey == requestKey) return
        activeRequestKey = requestKey
        val requestGeneration = ++activeRequestGeneration
        activeSettingsFingerprint = null
        activeJob?.cancel()
        _uiState.value = MetaDetailsUiState(
            requestKey = requestKey,
            isLoading = true,
            meta = cachedEntry(requestKey)?.baseMeta,
        )
        activeJob = scope.launch {
            val result = runCatching { ServerCatalog.details(ref) }
            if (activeRequestKey != requestKey || activeRequestGeneration != requestGeneration) return@launch
            result.fold(
                onSuccess = { details ->
                    val meta = details.meta
                    ServerUserStateProjection.apply(details)
                    putCachedEntry(requestKey, CachedMetaEntry(baseMeta = meta))
                    _uiState.value = MetaDetailsUiState(requestKey = requestKey, meta = meta)
                },
                onFailure = { error ->
                    if (error is CancellationException) throw error
                    log.w { "Server details failed: ${error.serverFailure()}" }
                    val cached = cachedEntry(requestKey)?.baseMeta
                    _uiState.value = MetaDetailsUiState(
                        requestKey = requestKey,
                        meta = cached,
                        errorMessage = if (cached == null) getString(error.serverFailure().message()) else null,
                    )
                    if (cached == null) activeRequestKey = null
                },
            )
        }
    }

    private const val FETCH_TIMEOUT_MS = 5_000L
    private const val METADATA_BASE_TOTAL_TIMEOUT_MS = 5_000L
    private const val FIRST_PAINT_ENRICHMENT_BUDGET_MS = 2_000L
    private const val METADATA_PROVIDER_READY_TIMEOUT_MS = 10_000L
    private const val TMDB_FALLBACK_TIMEOUT_MS = 10_000L
    private const val MDBLIST_ENRICH_TIMEOUT_MS = 5_000L
    private const val TRAKT_RELATED_TIMEOUT_MS = 10_000L

    private fun loadCloudStream(
        type: String,
        id: String,
        requestKey: String,
        route: CloudStreamRouteData,
        trigger: MetadataLoadTrigger,
    ) {
        cachedEntry(requestKey)?.let { cached ->
            activeRequestGeneration += 1
            activeJob?.cancel()
            activeJob = null
            _uiState.value = MetaDetailsUiState(requestKey = requestKey, meta = cached.baseMeta)
            activeRequestKey = requestKey
            activeSettingsFingerprint = null
            RuntimeDiagnostics.updateMetadataLoading(false)
            RuntimeDiagnostics.recordMetadataCacheHit(trigger)
            return
        }
        if (_uiState.value.isLoading && activeJob?.isActive == true && activeRequestKey == requestKey) {
            RuntimeDiagnostics.recordMetadataCoalesced(trigger, additionalCall = true)
            return
        }
        val requestGeneration = ++activeRequestGeneration
        activeRequestKey = requestKey
        activeSettingsFingerprint = null
        activeJob?.cancel()
        val diagnosticOperation = RuntimeDiagnostics.startMetadataLoad(trigger, MetadataLoadPath.CloudStream)
        val diagnosticStart = TimeSource.Monotonic.markNow()
        RuntimeDiagnostics.updateMetadataLoading(true)
        _uiState.value = MetaDetailsUiState(requestKey = requestKey, isLoading = true)
        activeJob = scope.launch {
            var outcome = MetadataLoadOutcome.Completed
            try {
                val result = fetchSharedBase(type = type, id = id, requestKey = requestKey)
                if (result.coalesced) RuntimeDiagnostics.recordMetadataCoalesced(trigger)
                if (activeRequestKey != requestKey || activeRequestGeneration != requestGeneration) return@launch
                val meta = result.value?.meta
                if (meta != null) {
                    _uiState.value = MetaDetailsUiState(requestKey = requestKey, meta = meta)
                    activeRequestKey = requestKey
                } else {
                    outcome = MetadataLoadOutcome.Failed
                    _uiState.value = MetaDetailsUiState(
                        requestKey = requestKey,
                        errorMessage = "CloudStream detail could not be loaded",
                    )
                }
            } catch (error: Throwable) {
                if (error is CancellationException) {
                    outcome = MetadataLoadOutcome.Cancelled
                    throw error
                }
                outcome = MetadataLoadOutcome.Failed
                log.e(error) { "Unexpected CloudStream detail failure provider=${route.providerId}" }
                if (activeRequestKey == requestKey && activeRequestGeneration == requestGeneration) {
                    _uiState.value = MetaDetailsUiState(
                        requestKey = requestKey,
                        errorMessage = error.message ?: "CloudStream detail could not be loaded",
                    )
                }
            } finally {
                RuntimeDiagnostics.finishMetadataLoad(
                    operation = diagnosticOperation,
                    outcome = outcome,
                    durationMs = diagnosticStart.elapsedNow().inWholeMilliseconds,
                )
                if (activeRequestKey == requestKey && activeRequestGeneration == requestGeneration) {
                    if (_uiState.value.isLoading) {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
                    RuntimeDiagnostics.updateMetadataLoading(false)
                    activeJob = null
                }
            }
        }
    }

    private suspend fun fetchSharedBase(
        type: String,
        id: String,
        requestKey: String,
    ): CoordinatedMetadataResult<BaseMetadataResult?> {
        val generation = synchronized(cacheLock) { cacheGeneration }
        val totalTimeoutMs = if (parseCloudStreamRouteId(id) != null) {
            CLOUDSTREAM_DETAIL_TIMEOUT_MS
        } else {
            METADATA_BASE_TOTAL_TIMEOUT_MS
        }
        return baseRequestCoordinator.execute(key = "$generation:$requestKey") {
            withTimeoutOrNull(totalTimeoutMs) {
                cachedEntry(requestKey, generation)?.let { cached ->
                    BaseMetadataResult(
                        meta = cached.baseMeta,
                        source = BaseMetadataSource.Addon,
                        fallbackItemId = id,
                    )
                } ?: fetchBaseUncached(type = type, id = id)?.also { result ->
                    putBaseMetaIfGenerationMatches(requestKey, result.meta, generation)
                }
            }
        }
    }

    private suspend fun fetchBaseUncached(type: String, id: String): BaseMetadataResult? {
        ServerItemRef.parse(id)?.let { ref ->
            return runCatching { ServerCatalog.details(ref).meta }
                .onFailure { if (it is CancellationException) throw it }
                .getOrNull()
                ?.let { meta ->
                    BaseMetadataResult(meta = meta, source = BaseMetadataSource.Server, fallbackItemId = id)
                }
        }
        parseCloudStreamRouteId(id)?.let { route ->
            return loadCloudStreamMetaDetails(route) // Mew hook
                ?.let { meta ->
                    BaseMetadataResult(
                        meta = meta,
                        source = BaseMetadataSource.CloudStream,
                        fallbackItemId = id,
                    )
                }
        }

        val metaLookupId = resolveMetaLookupId(itemId = id, itemType = type)
        val manifests = findReadyMetaManifests(type = type, id = metaLookupId)
        for (manifest in manifests) {
            val meta = withTimeoutOrNull(FETCH_TIMEOUT_MS) {
                withContext(Dispatchers.Default) {
                    tryFetchMeta(manifest, type, metaLookupId, includeMdbList = false)
                }
            }
            if (meta != null) {
                return BaseMetadataResult(
                    meta = meta,
                    source = BaseMetadataSource.Addon,
                    fallbackItemId = metaLookupId,
                )
            }
        }

        return tryFetchTmdbFallbackMeta(type = type, id = id)?.let { meta ->
            BaseMetadataResult(
                meta = meta,
                source = BaseMetadataSource.TmdbFallback,
                fallbackItemId = id,
            )
        }
    }

    private fun cachedEntry(requestKey: String): CachedMetaEntry? =
        synchronized(cacheLock) { cachedMetaByRequestKey[requestKey] }

    private fun cachedEntry(requestKey: String, generation: Long): CachedMetaEntry? =
        synchronized(cacheLock) {
            cachedMetaByRequestKey[requestKey].takeIf { cacheGeneration == generation }
        }

    private fun putCachedEntry(requestKey: String, entry: CachedMetaEntry) {
        synchronized(cacheLock) { cachedMetaByRequestKey[requestKey] = entry }
    }

    private fun putBaseMetaIfGenerationMatches(requestKey: String, meta: MetaDetails, generation: Long) {
        synchronized(cacheLock) {
            if (cacheGeneration == generation && cachedMetaByRequestKey[requestKey] == null) {
                cachedMetaByRequestKey[requestKey] = CachedMetaEntry(baseMeta = meta)
            }
        }
    }

    private fun cacheEnrichedMeta(
        requestKey: String,
        baseMeta: MetaDetails,
        enrichedMeta: MetaDetails,
        settingsFingerprint: String,
    ) {
        val cached = synchronized(cacheLock) {
            val current = cachedMetaByRequestKey[requestKey] ?: return@synchronized false
            if (current.baseMeta != baseMeta) return@synchronized false
            if (
                current.metaScreenSettingsFingerprint != null &&
                current.metaScreenSettingsFingerprint != settingsFingerprint
            ) return@synchronized false
            cachedMetaByRequestKey[requestKey] = current.copy(
                metaScreenMeta = enrichedMeta,
                metaScreenSettingsFingerprint = settingsFingerprint,
            )
            true
        }
        if (cached) RuntimeDiagnostics.recordMetadataPublication(MetadataPublicationStage.CacheOnly)
    }

    private suspend fun tryFetchMeta(
        manifest: AddonManifest,
        type: String,
        id: String,
        includeMdbList: Boolean,
    ): MetaDetails? {
        val url = buildAddonResourceUrl(
            manifestUrl = manifest.transportUrl,
            resource = "meta",
            type = type,
            id = id,
        )

        return try {
            TmdbSettingsRepository.ensureLoaded()
            log.d { "Fetching meta from: $url" }
            val payload = fetchAddonResponseText(url)
            log.d { "Raw payload length=${payload.length}, first 500 chars: ${payload.take(500)}" }
            val result = MetaDetailsParser.parse(payload)
            val enriched = if (includeMdbList) {
                MdbListSettingsRepository.ensureLoaded()
                withTimeoutOrNull(MDBLIST_ENRICH_TIMEOUT_MS) {
                    MdbListMetadataService.enrichMeta(
                        meta = result,
                        fallbackItemId = id,
                        settings = MdbListSettingsRepository.snapshot(),
                    )
                } ?: result
            } else {
                result
            }
            log.d { "Parsed meta: type=${enriched.type}, name=${enriched.name}, videos=${enriched.videos.size}" }
            if (enriched.videos.isNotEmpty()) {
                val first = enriched.videos.first()
                log.d { "First video: id=${first.id} title=${first.title} s=${first.season} e=${first.episode} embeddedStreams=${first.streams.size}" }
            }
            enriched
        } catch (e: Throwable) {
            if (e is CancellationException) throw e
            log.e(e) { "Failed to fetch/parse meta from $url (manifest=${manifest.transportUrl})" }
            null
        }
    }

    private suspend fun findReadyMetaManifests(type: String, id: String): List<AddonManifest> {
        AddonRepository.initialize()

        findMetaManifests(AddonRepository.uiState.value, type, id).takeIf { it.isNotEmpty() }?.let { return it }

        if (!AddonRepository.uiState.value.hasPendingEnabledAddonManifests()) {
            return emptyList()
        }

        val readyState = withTimeoutOrNull(METADATA_PROVIDER_READY_TIMEOUT_MS) {
            AddonRepository.uiState.first { state ->
                findMetaManifests(state, type, id).isNotEmpty() ||
                    !state.hasPendingEnabledAddonManifests()
            }
        } ?: AddonRepository.uiState.value

        return findMetaManifests(readyState, type, id)
    }

    private fun findMetaManifests(state: com.nuvio.app.features.addons.AddonsUiState, type: String, id: String): List<AddonManifest> =
        state.addons
            .enabledAddons()
            .mapNotNull { it.manifest }
            .filter { manifest -> manifest.supportsResource("meta", type, id) }

    private fun com.nuvio.app.features.addons.AddonsUiState.hasPendingEnabledAddonManifests(): Boolean =
        addons.enabledAddons().any { addon -> addon.manifest == null && addon.isRefreshing }

    private suspend fun resolveMetaLookupId(itemId: String, itemType: String): String {
        val tmdbId = itemId
            .takeIf { it.startsWith("tmdb:", ignoreCase = true) }
            ?.substringAfter(':')
            ?.substringBefore(':')
            ?.toIntOrNull()
            ?: return itemId

        return withTimeoutOrNull(FETCH_TIMEOUT_MS) {
            TmdbService.tmdbToImdb(tmdbId = tmdbId, mediaType = itemType)
        }
            ?.takeIf { it.isNotBlank() }
            ?: itemId
    }

    private suspend fun tryFetchTmdbFallbackMeta(type: String, id: String): MetaDetails? =
        withTimeoutOrNull(TMDB_FALLBACK_TIMEOUT_MS) {
            TmdbMetadataService.fetchStandaloneMeta(
                type = type,
                id = id,
                settings = TmdbSettingsRepository.snapshot(),
            )
        }

    private suspend fun publishLoadedMeta(
        requestKey: String,
        meta: MetaDetails,
        fallbackItemId: String,
        fallbackItemType: String,
        mdbListSettings: com.nuvio.app.features.mdblist.MdbListSettings,
        metaScreenSettingsFingerprint: String,
        requestGeneration: Long,
    ) {
        if (!isCurrentRequest(requestKey, metaScreenSettingsFingerprint, requestGeneration)) return
        val cachedEntry = CachedMetaEntry(baseMeta = meta)
        putCachedEntry(requestKey, cachedEntry)

        val enrichment = scope.async(Dispatchers.Default) {
            try {
                enrichForMetaScreen(
                    meta = meta,
                    fallbackItemId = fallbackItemId,
                    fallbackItemType = fallbackItemType,
                    settings = mdbListSettings,
                )
            } catch (error: Throwable) {
                if (error is CancellationException || error is OutOfMemoryError) throw error
                log.w(error) { "Deferred metadata enrichment failed" }
                null
            }
        }
        val enrichedMeta = withTimeoutOrNull(FIRST_PAINT_ENRICHMENT_BUDGET_MS) {
            enrichment.await()
        }
        if (
            activeRequestKey != requestKey ||
            activeSettingsFingerprint != metaScreenSettingsFingerprint ||
            activeRequestGeneration != requestGeneration
        ) return

        if (enrichedMeta != null) {
            putCachedEntry(requestKey, cachedEntry.copy(
                metaScreenMeta = enrichedMeta,
                metaScreenSettingsFingerprint = metaScreenSettingsFingerprint,
            ))
            val nextState = MetaDetailsUiState(
                requestKey = requestKey,
                meta = enrichedMeta.withUnreleasedFilter(),
            )
            if (_uiState.value != nextState) {
                _uiState.value = nextState
                RuntimeDiagnostics.recordMetadataPublication(MetadataPublicationStage.Final)
            }
        } else {
            val baseState = MetaDetailsUiState(
                requestKey = requestKey,
                meta = meta.withUnreleasedFilter(),
            )
            if (_uiState.value != baseState) {
                _uiState.value = baseState
                RuntimeDiagnostics.recordMetadataPublication(MetadataPublicationStage.Base)
            }
            scope.launch {
                val deferredMeta = enrichment.await() ?: return@launch
                cacheEnrichedMeta(
                    requestKey = requestKey,
                    baseMeta = meta,
                    enrichedMeta = deferredMeta,
                    settingsFingerprint = metaScreenSettingsFingerprint,
                )
            }
        }
        activeRequestKey = requestKey
    }

    private suspend fun enrichForMetaScreen(
        meta: MetaDetails,
        fallbackItemId: String,
        fallbackItemType: String,
        settings: com.nuvio.app.features.mdblist.MdbListSettings,
    ): MetaDetails {
        val tmdbEnrichedMeta = enrichWithTmdb(
            meta = meta,
            fallbackItemId = fallbackItemId,
        )
        val mdbListEnrichedMeta = withTimeoutOrNull(MDBLIST_ENRICH_TIMEOUT_MS) {
            MdbListMetadataService.enrichMeta(
                meta = tmdbEnrichedMeta,
                fallbackItemId = fallbackItemId,
                settings = settings,
            )
        } ?: tmdbEnrichedMeta
        val imdbRatedMeta = OmdbEpisodeRatingsService.applyTo(
            meta = mdbListEnrichedMeta,
            fallbackItemId = fallbackItemId,
            settings = TmdbSettingsRepository.snapshot(),
        )
        val enrichedMeta = applyMoreLikeThisSource(
            meta = imdbRatedMeta,
            fallbackItemId = fallbackItemId,
            fallbackItemType = fallbackItemType,
        )

        return enrichedMeta
    }

    private suspend fun enrichWithTmdb(
        meta: MetaDetails,
        fallbackItemId: String,
        onProgress: (suspend (MetaDetails) -> Unit)? = null,
    ): MetaDetails {
        TmdbSettingsRepository.ensureLoaded()
        val settings = TmdbSettingsRepository.snapshot()
        if (!settings.enabled) {
            RuntimeDiagnostics.recordLog("tmdb-enrichment-miss reason=disabled")
            return meta
        }
        RuntimeDiagnostics.recordLog("tmdb-enrichment-start")
        val seasonCount = meta.videos.mapNotNull { it.season }.distinct().size
        var latestProgress = meta
        val result = runCatching {
            withTimeoutOrNull(tmdbEnrichmentTimeoutMs(seasonCount)) {
                TmdbMetadataService.enrichMeta(
                    meta = meta,
                    fallbackItemId = fallbackItemId,
                    settings = settings,
                    onEpisodeProgress = { progress ->
                        latestProgress = progress
                        onProgress?.invoke(progress)
                    },
                )
            }
        }.getOrElse { failure ->
            RuntimeDiagnostics.recordLog("tmdb-enrichment-failure error=${failure.message?.take(160)}")
            return latestProgress
        }
        if (result == null) {
            RuntimeDiagnostics.recordLog("tmdb-enrichment-miss reason=timeout-or-no-match")
            return latestProgress
        }
        RuntimeDiagnostics.recordLog("tmdb-enrichment-success")
        return result
    }

    private suspend fun applyMoreLikeThisSource(
        meta: MetaDetails,
        fallbackItemId: String,
        fallbackItemType: String,
    ): MetaDetails {
        TrackingSettingsRepository.ensureLoaded()
        TraktAuthRepository.ensureLoaded()
        TmdbSettingsRepository.ensureLoaded()

        val trackingSettings = TrackingSettingsRepository.uiState.value
        val isTraktAuthenticated = TraktAuthRepository.uiState.value.mode == TraktConnectionMode.CONNECTED
        val tmdbSettings = TmdbSettingsRepository.snapshot()

        log.d {
            "More like this source=${trackingSettings.moreLikeThisSource} simkl=${shouldUseSimklMoreLikeThis(trackingSettings.moreLikeThisSource)} " +
                "type=${meta.type} fallbackType=$fallbackItemType id=${meta.id}"
        }
        // Simkl source
        if (shouldUseSimklMoreLikeThis(trackingSettings.moreLikeThisSource) &&
            supportsMoreLikeThis(meta, fallbackItemType)
        ) {
            val items = runCatching {
                com.nuvio.app.features.simkl.SimklRelatedRepository.getRelated(
                    meta = meta,
                    fallbackItemId = fallbackItemId,
                    fallbackItemType = fallbackItemType,
                )
            }.onFailure { error ->
                log.w { "Failed to load Simkl related titles for ${meta.id}: ${error.message}" }
            }.getOrDefault(emptyList())
            InAppLogger.info("Metadata/Simkl", "Related titles id=${meta.id} count=${items.size}")

            if (items.isNotEmpty()) {
                return meta.copy(
                    moreLikeThis = items,
                    moreLikeThisSource = MoreLikeThisSource.SIMKL,
                    moreLikeThisHasMore = false,
                )
            }
        }

        // Trakt source
        val shouldUseTrakt = shouldUseTraktMoreLikeThis(
            isAuthenticated = isTraktAuthenticated,
            source = trackingSettings.moreLikeThisSource,
        ) && supportsMoreLikeThis(meta, fallbackItemType)

        if (shouldUseTrakt) {
            val page = runCatching {
                TraktRelatedRepository.getRelated(
                    itemId = meta.id,
                    itemType = meta.type,
                    fallbackItemId = fallbackItemId,
                    fallbackItemType = fallbackItemType,
                )
            }.onFailure { error ->
                log.w { "Failed to load Trakt related titles for ${meta.id}: ${error.message}" }
                InAppLogger.warn("Metadata/Trakt", "Failed related titles id=${meta.id} error=${InAppLogger.throwableSummary(error)}")
            }.getOrDefault(MoreLikeThisPage())
            InAppLogger.info("Metadata/Trakt", "Related titles id=${meta.id} count=${page.items.size} hasMore=${page.hasMore}")

            if (page.items.isNotEmpty()) {
                return meta.copy(
                    moreLikeThis = page.items,
                    moreLikeThisSource = MoreLikeThisSource.TRAKT,
                    moreLikeThisHasMore = page.hasMore,
                )
            }
        }

        if (!tmdbSettings.enabled || !tmdbSettings.useMoreLikeThis) {
            return meta.copy(
                moreLikeThis = emptyList(),
                moreLikeThisSource = null,
                moreLikeThisHasMore = false,
            )
        }

        InAppLogger.info(
            "Metadata/TMDB",
            "More Like This fallback id=${meta.id} count=${meta.moreLikeThis.size}",
        )
        return meta.copy(
            moreLikeThisSource = MoreLikeThisSource.TMDB.takeIf { meta.moreLikeThis.isNotEmpty() },
            moreLikeThisHasMore = meta.moreLikeThis.size >= TMDB_RECOMMENDATIONS_PAGE_SIZE,
        )
    }

    private fun shouldFetchMdbListOnMetaScreen(
        meta: MetaDetails,
        fallbackItemId: String,
        settings: com.nuvio.app.features.mdblist.MdbListSettings,
    ): Boolean = MdbListMetadataService.shouldFetchForMeta(
        meta = meta,
        fallbackItemId = fallbackItemId,
        settings = settings,
    )

    private fun shouldEnrichForMetaScreen(
        meta: MetaDetails,
        fallbackItemId: String,
        settings: com.nuvio.app.features.mdblist.MdbListSettings,
    ): Boolean {
        if (shouldFetchMdbListOnMetaScreen(meta, fallbackItemId, settings)) return true
        TmdbSettingsRepository.ensureLoaded()
        if (OmdbEpisodeRatingsService.shouldApply(meta, TmdbSettingsRepository.snapshot())) return true
        return shouldApplyMoreLikeThisSource(meta)
    }

    private fun shouldApplyMoreLikeThisSource(meta: MetaDetails): Boolean {
        TrackingSettingsRepository.ensureLoaded()
        TraktAuthRepository.ensureLoaded()
        TmdbSettingsRepository.ensureLoaded()

        val trackingSettings = TrackingSettingsRepository.uiState.value
        val isTraktAuthenticated = TraktAuthRepository.uiState.value.mode == TraktConnectionMode.CONNECTED
        val tmdbSettings = TmdbSettingsRepository.snapshot()
        return shouldUseSimklMoreLikeThis(trackingSettings.moreLikeThisSource) || shouldUseTraktMoreLikeThis(
            isAuthenticated = isTraktAuthenticated,
            source = trackingSettings.moreLikeThisSource,
        ) || !tmdbSettings.enabled || !tmdbSettings.useMoreLikeThis || meta.moreLikeThisSource == null && meta.moreLikeThis.isNotEmpty()
    }

    private fun buildMetaScreenSettingsFingerprint(
        settings: com.nuvio.app.features.mdblist.MdbListSettings,
    ): String {
        TrackingSettingsRepository.ensureLoaded()
        TraktAuthRepository.ensureLoaded()
        TmdbSettingsRepository.ensureLoaded()
        val providers = settings.enabledProvidersInPriorityOrder().joinToString(",")
        val trackingSettings = TrackingSettingsRepository.uiState.value
        val traktAuthMode = TraktAuthRepository.uiState.value.mode
        val tmdbSettings = TmdbSettingsRepository.snapshot()
        return buildString {
            append("${settings.enabled}:${settings.apiKey.trim()}:$providers")
            append("|mdblist_account=${settings.accountScope.takeUnless { settings.hasApiKey }}")
            append("|more_like=${trackingSettings.moreLikeThisSource}:$traktAuthMode:${shouldUseSimklMoreLikeThis(trackingSettings.moreLikeThisSource)}")
            append("|tmdb=${tmdbEnrichmentSettingsFingerprint(tmdbSettings)}")
            append("|omdb=${OmdbEpisodeRatingsService.hasApiKey}:${tmdbSettings.useEpisodeRatings}")
        }
    }

    private fun supportsMoreLikeThis(meta: MetaDetails, fallbackItemType: String): Boolean =
        normalizeMoreLikeThisType(meta.type) != null || normalizeMoreLikeThisType(fallbackItemType) != null

    private fun shouldUseSimklMoreLikeThis(source: MoreLikeThisSourcePreference): Boolean {
        if (source != MoreLikeThisSourcePreference.SIMKL) return false
        com.nuvio.app.features.simkl.SimklAuthRepository.ensureLoaded()
        return com.nuvio.app.features.simkl.SimklAuthRepository.isAuthenticated.value
    }

    private fun normalizeMoreLikeThisType(value: String?): String? =
        when (value?.trim()?.lowercase()) {
            "movie", "film" -> "movie"
            "series", "show", "tv", "tvshow" -> "series"
            else -> null
        }

    private fun isCurrentRequest(
        requestKey: String,
        settingsFingerprint: String,
        requestGeneration: Long,
    ): Boolean =
        activeRequestKey == requestKey &&
            activeSettingsFingerprint == settingsFingerprint &&
            activeRequestGeneration == requestGeneration

    private fun finishCurrentRequest(
        requestKey: String,
        settingsFingerprint: String,
        requestGeneration: Long,
    ) {
        if (!isCurrentRequest(requestKey, settingsFingerprint, requestGeneration)) return
        if (_uiState.value.isLoading) {
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
        RuntimeDiagnostics.updateMetadataLoading(false)
        activeJob = null
    }

    private fun MetaDetails.withUnreleasedFilter(): MetaDetails {
        val posterPattern = com.nuvio.app.core.poster.CustomPosterUrlRepository.let {
            it.ensureLoaded()
            it.patternForScreen(com.nuvio.app.core.poster.CustomPosterScreen.DETAILS)
        }
        val base = withCustomPosterUrls(posterPattern)
        if (!HomeCatalogSettingsRepository.snapshot().hideUnreleasedContent) return base
        val todayIsoDate = CurrentDateProvider.todayIsoDate()
        val releasedMoreLikeThis = base.moreLikeThis.filterReleasedItems(todayIsoDate)
        return base.copy(
            moreLikeThis = releasedMoreLikeThis,
            moreLikeThisSource = base.moreLikeThisSource.takeIf { releasedMoreLikeThis.isNotEmpty() },
            collectionItems = base.collectionItems.filterReleasedItems(todayIsoDate),
        )
    }

   
    fun findEmbeddedStreams(videoId: String): List<com.nuvio.app.features.streams.StreamItem> =
        _uiState.value.meta?.findEmbeddedStreams(videoId).orEmpty()
}

/** Streams the addon embedded in this title's metadata for [videoId], if any. */
internal fun MetaDetails.findEmbeddedStreams(videoId: String): List<com.nuvio.app.features.streams.StreamItem> {
    val videosWithStreams = videos.filter { it.streams.isNotEmpty() }
    if (videosWithStreams.isEmpty()) return emptyList()

    val directMatch = videosWithStreams.firstOrNull { it.id == videoId }
    if (directMatch != null) return directMatch.streams

    val parts = videoId.split(":")
    if (parts.size >= 3) {
        val season = parts[parts.size - 2].toIntOrNull()
        val episode = parts[parts.size - 1].toIntOrNull()
        if (season != null && episode != null) {
            val episodeMatch = videosWithStreams.firstOrNull { it.season == season && it.episode == episode }
            if (episodeMatch != null) return episodeMatch.streams
        }
    }

    val prefixMatch = videosWithStreams.firstOrNull { it.id.startsWith("$videoId:") }
    if (prefixMatch != null) return prefixMatch.streams

    if (videoId == id && videosWithStreams.size == 1) {
        return videosWithStreams.first().streams
    }

    if (videoId == id && videosWithStreams.isNotEmpty()) {
        return videosWithStreams.flatMap { it.streams }
    }

    return emptyList()
}

internal sealed interface MetaLookupOutcome {
    class Loaded(val meta: MetaDetails) : MetaLookupOutcome

    class Throttled(val retryAtEpochMs: Long) : MetaLookupOutcome

    object Unavailable : MetaLookupOutcome

    object Failed : MetaLookupOutcome
}

internal fun tmdbEnrichmentSettingsFingerprint(settings: TmdbSettings): String = with(settings) {
    listOf(
        enabled,
        hasApiKey,
        language,
        useTrailers,
        useArtwork,
        useBasicInfo,
        useDetails,
        useReleaseDates,
        useCredits,
        useProductions,
        useNetworks,
        useEpisodes,
        useEpisodeRatings,
        useSeasonPosters,
        useMoreLikeThis,
        useCollections,
    ).joinToString(":")
}

internal fun metaDetailsRequestKey(type: String, id: String): String = "$type:$id"

internal fun tmdbEnrichmentTimeoutMs(seasonCount: Int): Long =
    (10_000L + seasonCount.coerceAtLeast(0) * 2_500L).coerceAtMost(60_000L)
