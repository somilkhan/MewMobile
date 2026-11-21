package com.nuvio.app.features.downloads

import com.nuvio.app.features.details.MetaDetails
import com.nuvio.app.core.sync.AppForegroundMonitor
import com.nuvio.app.core.sync.AppVisibility
import com.nuvio.app.features.player.SubtitleRequestExtras
import com.nuvio.app.features.player.addonSubtitleRequests
import com.nuvio.app.features.streams.StreamItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import nuvio.composeapp.generated.resources.*
import org.jetbrains.compose.resources.getString

object DownloadsRepository {
    private val _uiState = MutableStateFlow(DownloadsUiState())
    val uiState: StateFlow<DownloadsUiState> = _uiState.asStateFlow()

    private val _hasUnseenCompleted = MutableStateFlow(false)
    val hasUnseenCompleted: StateFlow<Boolean> = _hasUnseenCompleted.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val activeHandles = mutableMapOf<String, DownloadsTaskHandle>()
    private var hasLoaded = false
    private var isObservingInputs = false
    private var nextDownloadOrdinal = 0L
    private val lastProgressPersistAt = mutableMapOf<String, Long>()

    fun ensureLoaded() {
        if (hasLoaded) return
        loadFromDisk()
        observeInputs()
    }

    /** Restarts queued downloads when limits or network transport allow them again. */
    private fun observeInputs() {
        if (isObservingInputs) return
        isObservingInputs = true
        var lastAllowMobile: Boolean? = null
        combine(DownloadsSettingsRepository.concurrencyLimits, DownloadsSettingsRepository.allowMobileDataDownloads, DownloadNetworkGuard.transportState) { limits, allowMobile, _ ->
            Triple(limits, allowMobile, isDownloadNetworkAllowed())
        }
            .distinctUntilChanged()
            .onEach { (_, allowMobile, networkAllowed) ->
                if (!hasLoaded) return@onEach
                // Running transfers keep the network rule they started with, so restart them when it
                // changes. Where a restart loses progress (iOS), only do so when the rule gets stricter.
                val ruleChanged = lastAllowMobile != null && lastAllowMobile != allowMobile
                val becameWifiOnly = lastAllowMobile == true && !allowMobile
                lastAllowMobile = allowMobile
                if (!networkAllowed || becameWifiOnly || (ruleChanged && DownloadsPlatformDownloader.restartKeepsProgress)) {
                    val runningIds = _uiState.value.items
                        .filter { it.status == DownloadStatus.Downloading }
                        .mapTo(mutableSetOf()) { it.id }
                    setStatus(runningIds, DownloadStatus.Queued)
                }
                advanceDownloadQueue()
            }
            .launchIn(scope)
        AppForegroundMonitor.events()
            .onEach { visibility -> if (visibility == AppVisibility.Foreground && hasLoaded) advanceDownloadQueue() }
            .launchIn(scope)
    }

    fun onProfileChanged() {
        synchronized(stateLock) { hasLoaded = true }
        loadFromDisk()
    }

    fun markCompletedSeen() {
        _hasUnseenCompleted.value = false
    }

    /**
     * Resets in-memory state on sign-out. Must not write storage: by the time this runs the
     * active profile has already been reset, so a write would land on another profile's key.
     * Unfinished records for every profile are removed by the platform storage wipe.
     */
    fun clearLocalState() {
        activeHandles.values.forEach(DownloadsTaskHandle::cancel)
        activeHandles.clear()
        lastProgressPersistAt.clear()
        hasLoaded = false
        _hasUnseenCompleted.value = false
        OfflineMetadataRepository.clear()
        _uiState.value = DownloadsUiState()
        notifyLiveStatusPlatform()
    }

    fun getOfflineMetadata(showId: String): StoredOfflineMeta? {
        ensureLoaded()
        return OfflineMetadataRepository[showId]
    }

    fun findPlayableDownloadByVideoId(videoId: String?): DownloadItem? {
        ensureLoaded()
        val normalizedVideoId = videoId?.trim().orEmpty()
        if (normalizedVideoId.isBlank()) return null
        val matching = _uiState.value.items.filter { item ->
            item.videoId == normalizedVideoId && item.hasPlayableLocalFile()
        }
        if (matching.isEmpty()) return null
        // Prefer the download that belongs to the same series/movie when known, so a
        // videoId collision across shows can never resolve to the wrong episode.
        if (parentMetaId != null) {
            matching.firstOrNull { it.parentMetaId.trim() == parentMetaId.trim() }?.let { return it }
        }
        return matching.first()
    }

    fun findPlayableDownload(
        parentMetaId: String,
        seasonNumber: Int? = null,
        episodeNumber: Int? = null,
        videoId: String? = null,
    ): DownloadItem? {
        ensureLoaded()
        val items = _uiState.value.items
        val normalizedParentMetaId = parentMetaId.trim()
        val log = Logger.withTag("Downloads")

        findPlayableDownloadByVideoId(videoId, parentMetaId = parentMetaId)?.let { matched ->
            log.d {
                "episode-index: videoId match id=${matched.id} videoId=${matched.videoId} " +
                    "S${matched.seasonNumber ?: "?"}E${matched.episodeNumber ?: "?"} parent=${matched.parentMetaId}"
            }
            return matched
        }

        val fallback = if (seasonNumber != null && episodeNumber != null) {
            items.firstOrNull { item ->
                item.parentMetaId == normalizedParentMetaId &&
                    item.seasonNumber == seasonNumber &&
                    item.episodeNumber == episodeNumber &&
                    item.hasPlayableLocalFile()
            }
        } else {
            items.firstOrNull { item ->
                item.parentMetaId == normalizedParentMetaId &&
                    item.seasonNumber == null &&
                    item.episodeNumber == null &&
                    item.hasPlayableLocalFile()
            }
        }
        if (fallback != null) {
            log.d {
                "episode-index: S/E match id=${fallback.id} " +
                    "S${fallback.seasonNumber ?: "?"}E${fallback.episodeNumber ?: "?"} " +
                    "lookup S${seasonNumber ?: "?"}E${episodeNumber ?: "?"}"
            }
        }
        return fallback
    }

    fun playableLocalFileUri(item: DownloadItem): String? {
        ensureLoaded()
        if (item.status != DownloadStatus.Completed) return null
        val resolvedUri = DownloadsPlatformDownloader.resolveLocalFileUri(
            localFileUri = item.localFileUri,
            destinationFileName = item.fileName,
        ) ?: return null

        if (resolvedUri != item.localFileUri) {
            mutateItem(item.id) { current ->
                if (current.fileName == item.fileName) {
                    current.copy(
                        localFileUri = resolvedUri,
                        updatedAtEpochMs = DownloadsClock.nowEpochMs(),
                    )
                } else {
                    current
                }
            }
        }

        return resolvedUri
    }

    fun enqueueFromStream(
        contentType: String,
        videoId: String,
        parentMetaId: String,
        parentMetaType: String,
        title: String,
        logo: String?,
        poster: String?,
        background: String?,
        seasonNumber: Int?,
        episodeNumber: Int?,
        episodeTitle: String?,
        episodeThumbnail: String?,
        episodeOverview: String? = null,
        stream: StreamItem,
        episodeOverview: String? = null,
        showMeta: MetaDetails? = null,
    ): DownloadEnqueueResult {
        ensureLoaded()
        val log = Logger.withTag("Downloads")

        val sourceUrl = stream.playableDirectUrl
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: return DownloadEnqueueResult.MissingUrl

        if (!sourceUrl.isSupportedDownloadUrl()) {
            return DownloadEnqueueResult.UnsupportedFormat
        }

        val now = DownloadsClock.nowEpochMs()
        val logicalKey = buildLogicalKey(
            parentMetaId = parentMetaId,
            seasonNumber = seasonNumber,
            episodeNumber = episodeNumber,
        )

        updateOfflineMetadata(
            meta = showMeta,
            parentMetaId = parentMetaId,
            parentMetaType = parentMetaType,
            title = title,
            logo = logo,
            poster = poster,
            background = background,
            videoId = videoId,
            episodeTitle = episodeTitle,
            episodeThumbnail = episodeThumbnail,
            episodeOverview = episodeOverview,
            seasonNumber = seasonNumber,
            episodeNumber = episodeNumber,
        )
        val resolvedEpisodeThumbnail = episodeThumbnail ?: showMeta?.videos
            ?.firstOrNull { it.season == seasonNumber && it.episode == episodeNumber }
            ?.thumbnail

        val replacement = replaceExistingDownload(logicalKey)
        val replacedExisting = replacement != null
        val currentItems = _uiState.value.items.toMutableList()
        replacement?.let { currentItems.removeAll { item -> item.id == it.id } }
        val downloadId = nextDownloadId(now)
        val fileName = buildFileName(
            title = title,
            seasonNumber = seasonNumber,
            episodeNumber = episodeNumber,
            episodeTitle = episodeTitle,
            fallbackTitle = stream.streamLabel,
            sourceUrl = sourceUrl,
            downloadId = downloadId,
        )
        val detailsSnapshot = snapshotCurrentDetails(
            contentType = contentType,
            parentMetaId = parentMetaId,
            parentMetaType = parentMetaType,
            videoId = videoId,
        )

        val queuedItem = DownloadItem(
            id = downloadId,
            contentType = contentType,
            parentMetaId = parentMetaId,
            parentMetaType = parentMetaType,
            videoId = videoId,
            title = title,
            logo = logo,
            poster = poster,
            background = background,
            seasonNumber = seasonNumber,
            episodeNumber = episodeNumber,
            episodeTitle = episodeTitle,
            episodeThumbnail = resolvedEpisodeThumbnail,
            streamTitle = stream.streamLabel,
            streamSubtitle = stream.streamSubtitle,
            providerName = stream.addonName,
            providerAddonId = stream.addonId,
            externalSubtitles = stream.externalSubtitles,
            sourceUrl = sourceUrl,
            sourceHeaders = sanitizeRequestHeaders(stream.behaviorHints.proxyHeaders?.request),
            sourceResponseHeaders = sanitizeResponseHeaders(stream.behaviorHints.proxyHeaders?.response),
            subtitleRequests = addonSubtitleRequests(
                type = contentType,
                videoId = videoId,
                extras = SubtitleRequestExtras.from(stream.behaviorHints, sourceUrl),
            ),
            sourceSubtitles = stream.externalSubtitles,
            localFileUri = null,
            fileName = fileName,
            status = DownloadStatus.Queued,
            downloadedBytes = 0L,
            totalBytes = null,
            errorMessage = null,
            createdAtEpochMs = now,
            updatedAtEpochMs = now,
        )
        currentItems.add(0, queuedItem)
        publish(currentItems)
        persist()
        advanceDownloadQueue()
        val item = _uiState.value.items.firstOrNull { it.id == downloadId } ?: queuedItem

        return if (item.status == DownloadStatus.Queued) {
            DownloadEnqueueResult.Queued
        } else if (replacedExisting) {
            DownloadEnqueueResult.Replaced
        } else {
            DownloadEnqueueResult.Started
        }
    }

    private fun updateOfflineMetadata(
        meta: MetaDetails?, parentMetaId: String, parentMetaType: String, title: String,
        logo: String?, poster: String?, background: String?, videoId: String, episodeTitle: String?,
        episodeThumbnail: String?, episodeOverview: String?, seasonNumber: Int?, episodeNumber: Int?,
    ) {
        if (meta != null) {
            OfflineMetadataRepository.merge(meta)
        } else {
            OfflineMetadataRepository.recordBasic(
                showId = parentMetaId, type = parentMetaType, title = title, logo = logo,
                poster = poster, background = background,
                episode = if (seasonNumber != null && episodeNumber != null) StoredDownloadedVideoMeta(
                    id = videoId, title = episodeTitle.orEmpty(), thumbnail = episodeThumbnail,
                    season = seasonNumber, episode = episodeNumber, overview = episodeOverview,
                ) else null,
            )
        }
    }

    private fun replaceExistingDownload(logicalKey: String): DownloadItem? {
        val existing = _uiState.value.items.firstOrNull { it.logicalContentKey == logicalKey } ?: return null
        dropHandle(existing.id)?.cancel()
        DownloadsPlatformDownloader.removeFile(playableLocalFileUri(existing) ?: existing.localFileUri)
        DownloadsPlatformDownloader.removePartialFile(existing.fileName)
        return existing
    }

    /** Starts queued downloads until the concurrency limits or network policy stop it. */
    fun advanceDownloadQueue() {
        ensureLoaded()
        val policy = DownloadQueuePolicy.current()
        val items = _uiState.value.items.toMutableList()
        val started = mutableListOf<DownloadItem>()
        while (true) {
            val next = policy.nextToStart(items) ?: break
            val replacement = next.copy(status = DownloadStatus.Downloading, updatedAtEpochMs = DownloadsClock.nowEpochMs(), errorMessage = null)
            val index = items.indexOfFirst { it.id == next.id }
            if (index < 0) break
            items[index] = replacement
            started += replacement
        }
        if (started.isNotEmpty()) {
            publish(items)
            persist()
            started.forEach(::startDownload)
        }
    }

    fun pauseDownload(downloadId: String) {
        pauseDownloads(listOf(downloadId))
    }

    fun pauseActiveDownloads() {
        pauseActiveDownloadsMatching { true }
    }

    /** Pauses the download if it is running or queued; otherwise resumes or retries it. */
    fun togglePause(downloadId: String) {
        ensureLoaded()
        val log = Logger.withTag("Downloads")
        val item = _uiState.value.items.firstOrNull { it.id == downloadId } ?: return
        if (item.isActive) pauseDownload(downloadId) else resumeDownload(downloadId)
    }

    /** Pauses every active episode of [showId], or resumes them all when none is active. */
    fun toggleShowPause(showId: String) {
        val isAnyActive = _uiState.value.items.any { it.parentMetaId == showId && it.isActive }
        if (isAnyActive) pauseActiveDownloadsMatching { it.parentMetaId == showId } else resumeOrRetrySeriesDownloads(showId)
    }

    /** Pauses every matching active item in one update before advancing the queue. */
    private fun pauseActiveDownloadsMatching(predicate: (DownloadItem) -> Boolean) {
        ensureLoaded()
        val current = _uiState.value.items
        val targets = current.filter { it.isActive && predicate(it) }
        if (targets.isEmpty()) return
        val ids = targets.mapTo(mutableSetOf()) { it.id }
        setStatus(ids, DownloadStatus.Paused)
        advanceDownloadQueue()
    }

    private fun setStatus(ids: Set<String>, status: DownloadStatus) {
        if (ids.isEmpty()) return
        val current = _uiState.value.items
        ids.forEach { dropHandle(it)?.cancel() }
        val now = DownloadsClock.nowEpochMs()
        publish(current.map { item ->
            if (item.id in ids) item.copy(status = status, updatedAtEpochMs = now, errorMessage = null) else item
        })
        persist()
    }

    /** Resumes paused episodes of [seriesId] and retries its failed ones. */
    private fun resumeOrRetrySeriesDownloads(seriesId: String) {
        ensureLoaded()
        _uiState.value.items
            .filter { it.parentMetaId == seriesId && it.isResumable }
            .map { it.id }
            .let(::resumeDownloads)
    }

    /** Resumes every paused download and retries every failed one. */
    fun resumeOrRetryAllDownloads() {
        ensureLoaded()
        _uiState.value.items
            .filter { it.isResumable }
            .map { it.id }
            .let(::resumeDownloads)
    }

    fun resumePausedDownloads() {
        ensureLoaded()
        _uiState.value.items
            .filter { it.status == DownloadStatus.Paused }
            .map { it.id }
            .forEach(::resumeDownload)
    }

    fun resumePausedDownloads() {
        ensureLoaded()
        _uiState.value.items
            .filter { it.status == DownloadStatus.Paused }
            .map { it.id }
            .forEach(::resumeDownload)
    }

    fun resumeDownload(downloadId: String) {
        resumeDownloads(listOf(downloadId))
    }

    /** Requeues paused and failed downloads; the queue starts items in persisted creation order. */
    fun resumeDownloads(ids: Collection<String>) {
        ensureLoaded()
        val current = _uiState.value.items
        val itemsById = current.associateBy { it.id }
        val toResume = ids.distinct().mapNotNull { itemsById[it] }.filter { it.isResumable }
        if (toResume.isEmpty()) return

        val now = DownloadsClock.nowEpochMs()
        val resetById = toResume.associate { item ->
            val reset = item.copy(
                status = DownloadStatus.Queued,
                errorMessage = null,
                localFileUri = null,
                updatedAtEpochMs = now,
            )
            item.id to reset
        }

        publish(current.map { resetById[it.id] ?: it })
        persist()
        advanceDownloadQueue()
    }

    fun pauseDownloads(downloadIds: Collection<String>) {
        pauseActiveDownloadsMatching { it.id in downloadIds }
    }

    internal fun reattachBackgroundDownload(downloadId: String) {
        if (!hasLoaded) return
        val item = _uiState.value.items.firstOrNull { it.id == downloadId } ?: return
        dropHandle(downloadId)?.cancel()
        val restored = DownloadsPlatformDownloader.restoreItem(item)
        replaceItem(restored)
        persist()
        if (restored.status == DownloadStatus.Downloading) startDownload(restored)
    }

    fun cancelDownload(downloadId: String) {
        cancelDownloads(listOf(downloadId))
    }

    fun cancelDownloads(downloadIds: Collection<String>) {
        ensureLoaded()
        val ids = downloadIds.toSet()
        val current = _uiState.value.items
        val targets = current.filter { it.id in ids }
        if (targets.isEmpty()) return
        targets.forEach { item ->
            dropHandle(item.id)?.cancel()
            val uri = DownloadsPlatformDownloader.resolveLocalFileUri(item.localFileUri, item.fileName) ?: item.localFileUri
            DownloadsPlatformDownloader.removeFile(uri)
            DownloadsPlatformDownloader.removePartialFile(item.fileName)
        }
        val remainingItems = current.filterNot { it.id in ids }
        targets.map { it.parentMetaId }.distinct().forEach { parentId ->
            if (remainingItems.none { it.parentMetaId == parentId }) OfflineMetadataRepository.remove(parentId)
        }
        val wasDownloading = targets.any { it.status == DownloadStatus.Downloading }
        publish(remainingItems)
        persist()
        if (wasDownloading) {
            advanceDownloadQueue()
        }
    }

    private fun loadFromDisk() {
        OfflineMetadataRepository.reload()
        _hasUnseenCompleted.value = false
        hasLoaded = true
        val payload = DownloadsStorage.loadPayload().orEmpty().trim()
        if (payload.isEmpty()) {
            OfflineMetadataRepository.ensureLoaded()
            _uiState.value = DownloadsUiState()
            OfflineMetadataRepository.setDownloads(emptyList())
            notifyLiveStatusPlatform()
            return
        }

        val decoded = DownloadsCodec.decodePayload(payload)
        OfflineMetadataRepository.migrateLegacy(decoded.showMetadata)

        var shouldPersistNormalized = false
        val normalized = decoded.items
            .map { item ->
                val statusNormalized = DownloadsPlatformDownloader.restoreItem(item)

                val localUriNormalized = normalizeCompletedLocalFileUri(statusNormalized)
                if (localUriNormalized != item) {
                    shouldPersistNormalized = true
                }
                localUriNormalized
            }
            .filter { item ->
                item.status != DownloadStatus.Completed ||
                    DownloadsPlatformDownloader.resolveLocalFileUri(
                        localFileUri = item.localFileUri,
                        destinationFileName = item.fileName,
                    ) != null
            }
        if (normalized.size != decodedItems.size) {
            shouldPersistNormalized = true
        }
        val activePendingSearches = decoded.pendingSourceSearches.filter { search ->
            search.status == PendingSourceSearchStatus.Searching &&
                now < search.expiresAtEpochMs &&
                search.attemptCount < MAX_SOURCE_SEARCH_ATTEMPTS
        }
        if (activePendingSearches.size != decoded.pendingSourceSearches.size) {
            shouldPersistNormalized = true
        }

        val restorePolicy = DownloadQueuePolicy.current()
        val keptDownloading = mutableListOf<DownloadItem>()

        val queuedNormalized = normalized.map { item ->
            if (item.status == DownloadStatus.Downloading) {
                if (restorePolicy.canStart(item.showKey, keptDownloading)) {
                    keptDownloading += item
                    item
                } else {
                    shouldPersistNormalized = true
                    DownloadsPlatformDownloader.pauseRestoredItem(item)
                    item.copy(status = DownloadStatus.Queued)
                }
            } else {
                item
            }
        }

        _uiState.value = DownloadsUiState(queuedNormalized)
        OfflineMetadataRepository.setDownloads(queuedNormalized)
        notifyLiveStatusPlatform()
        if (normalized.none { it.status == DownloadStatus.Downloading }) {
            promoteWaitingDownload()
        }
        if (shouldPersistNormalized) {
            persist()
        }
        queuedNormalized
            .filter { it.status == DownloadStatus.Downloading && it.id !in activeHandles }
            .forEach(::startDownload)
        advanceDownloadQueue()
    }

    private fun startDownload(item: DownloadItem) {
        val request = DownloadPlatformRequest(item)

        val handle = DownloadsPlatformDownloader.start(
            request = request,
            onProgress = { downloadedBytes, totalBytes ->
                scope.launch {
                    mutateItem(item.id) { current ->
                        if (current.status != DownloadStatus.Downloading) {
                            current
                        } else {
                            current.copy(
                                downloadedBytes = downloadedBytes.coerceAtLeast(0L),
                                totalBytes = totalBytes?.takeIf { it > 0L },
                                updatedAtEpochMs = DownloadsClock.nowEpochMs(),
                                errorMessage = null,
                            )
                        }
                    }
                }
            },
            onSuccess = { localFileUri, totalBytes ->
                scope.launch {
                    dropHandle(item.id)
                    mutateItem(item.id) { current ->
                        if (current.status != DownloadStatus.Downloading) return@mutateItem current
                        current.copy(
                            status = DownloadStatus.Completed,
                            localFileUri = localFileUri,
                            downloadedBytes = if (totalBytes != null && totalBytes > 0L) {
                                totalBytes
                            } else {
                                current.downloadedBytes
                            },
                            totalBytes = totalBytes?.takeIf { it > 0L } ?: current.totalBytes,
                            errorMessage = null,
                            updatedAtEpochMs = DownloadsClock.nowEpochMs(),
                        )
                    }
                    advanceDownloadQueue()
                }
                log.d {
                    "progress: id=${item.id} videoId=${item.videoId} " +
                        "S${item.seasonNumber ?: "?"}E${item.episodeNumber ?: "?"} " +
                        "$nextDownloadedBytes/${totalBytes ?: "?"} bytes"
                }
            },
            onFailure = { message ->
                scope.launch {
                    dropHandle(item.id)
                    mutateItem(item.id) { current ->
                        if (current.status != DownloadStatus.Downloading) {
                            current
                        } else {
                            current.copy(
                                status = DownloadStatus.Failed,
                                errorMessage = message.ifBlank { runBlocking { getString(Res.string.download_failed) } },
                                updatedAtEpochMs = DownloadsClock.nowEpochMs(),
                            )
                        }
                    }
                    advanceDownloadQueue()
                }
                log.d {
                    "completed: id=${item.id} videoId=${item.videoId} " +
                        "S${item.seasonNumber ?: "?"}E${item.episodeNumber ?: "?"} " +
                        "parent=${item.parentMetaId} file=${item.fileName} uri=$localFileUri"
                }
                promoteWaitingDownload()
            },
            onPaused = {
                scope.launch {
                    dropHandle(item.id)
                    mutateItem(item.id) { current ->
                        if (current.status != DownloadStatus.Downloading) return@mutateItem current
                        current.copy(status = DownloadStatus.Paused, errorMessage = null)
                    }
                    advanceDownloadQueue()
                }
            },
            onQueued = {
                scope.launch {
                    dropHandle(item.id)
                    mutateItem(item.id) { current ->
                        if (current.status == DownloadStatus.Downloading) current.copy(status = DownloadStatus.Queued, errorMessage = null) else current
                    }
                }
                log.w {
                    "failed: id=${item.id} videoId=${item.videoId} " +
                        "S${item.seasonNumber ?: "?"}E${item.episodeNumber ?: "?"} error=$message"
                }
                promoteWaitingDownload()
            },
        )

        registerActiveHandle(item.id, handle)
    }

    /**
     * In one-at-a-time mode, starts the oldest waiting download once no other download
     * is active. Selection is delegated to [DownloadQueuePolicy.oneAtATime].
     */
    private fun promoteWaitingDownload() {
        val promoted = synchronized(stateLock) {
            val state = _uiState.value
            if (state.items.any { it.status == DownloadStatus.Downloading }) return@synchronized null
            val policy = DownloadQueuePolicy.oneAtATime.copy(isNetworkAllowed = isDownloadNetworkAllowed())
            val next = policy.nextToStart(state.items)
                ?: return@synchronized null
            val candidate = next.copy(
                status = DownloadStatus.Downloading,
                updatedAtEpochMs = DownloadsClock.nowEpochMs(),
                errorMessage = null,
            )
            _uiState.value = state.copy(
                items = state.items.map { item -> if (item.id == candidate.id) candidate else item },
            )
            candidate
        } ?: return
        notifyLiveStatusPlatform()
        persist()
        Logger.withTag("Downloads").d {
            "queue: promote id=${promoted.id} videoId=${promoted.videoId} " +
                "S${promoted.seasonNumber ?: "?"}E${promoted.episodeNumber ?: "?"} -> downloading"
        }
        startDownload(promoted)
    }

    private fun mutateItem(
        downloadId: String,
        persist: Boolean = true,
        transform: (DownloadItem) -> DownloadItem,
    ) {
        var changed = false
        synchronized(stateLock) {
            _uiState.update { state ->
                val items = state.items.map { item ->
                    if (item.id == downloadId) {
                        changed = true
                        transform(item)
                    } else {
                        item
                    }
                }
                if (changed) state.copy(items = items) else state
            }
        }

        if (changed) {
            publish(updated)
            val item = updated.firstOrNull { it.id == downloadId }
            if (item == null || item.status != DownloadStatus.Downloading || shouldPersistProgress(item)) persist()
        }
    }

    /** Forgets a transfer's handle and its progress-save timestamp. */
    private fun dropHandle(downloadId: String): DownloadsTaskHandle? {
        lastProgressPersistAt.remove(downloadId)
        return activeHandles.remove(downloadId)
    }

    private fun shouldPersistProgress(item: DownloadItem): Boolean {
        val now = DownloadsClock.nowEpochMs()
        val previous = lastProgressPersistAt[item.id] ?: 0L
        if (now - previous < PROGRESS_PERSIST_INTERVAL_MS) return false
        lastProgressPersistAt[item.id] = now
        return true
    }

    private fun replaceItem(item: DownloadItem) {
        synchronized(stateLock) {
            _uiState.update { state ->
                state.copy(
                    items = state.items.map { existing ->
                        if (existing.id == item.id) item else existing
                    },
                )
            }
        }
        notifyLiveStatusPlatform()
    }

    private fun publish(items: List<DownloadItem>) {
        val previousStatuses = _uiState.value.items.associate { it.id to it.status }
        if (items.any { it.status == DownloadStatus.Completed && previousStatuses[it.id].let { status -> status != null && status != DownloadStatus.Completed } }) {
            _hasUnseenCompleted.value = true
        }
        _uiState.value = DownloadsUiState(
            items = items,
        )
        OfflineMetadataRepository.setDownloads(items)
        notifyLiveStatusPlatform()
    }

    private fun notifyLiveStatusPlatform() {
        runCatching {
            DownloadsLiveStatusPlatform.onItemsChanged(_uiState.value.items)
        }
        val pendingToPrompt = _uiState.value.pendingSourceSearches.filter { search ->
            search.status == PendingSourceSearchStatus.Searching && !search.manualChoicePrompted
        }
        if (_uiState.value.items.none { it.status == DownloadStatus.Downloading } && pendingToPrompt.isNotEmpty()) {
            PendingDownloadSourceSearchPlatform.notifyManualChoice(pendingToPrompt)
        }
    }

    private fun snapshotCurrentDetails(
        contentType: String,
        parentMetaId: String,
        parentMetaType: String,
        videoId: String,
    ): DownloadDetailsSnapshot? {
        val candidates = listOf(
            parentMetaType to parentMetaId,
            contentType to parentMetaId,
            parentMetaType to videoId,
            contentType to videoId,
        )

        return candidates
            .asSequence()
            .mapNotNull { (type, id) ->
                val normalizedType = type.trim()
                val normalizedId = id.trim()
                if (normalizedType.isBlank() || normalizedId.isBlank()) {
                    null
                } else {
                    MetaDetailsRepository.peek(normalizedType, normalizedId)
                }
            }
            .firstOrNull()
            ?.toDownloadDetailsSnapshot()
    }

    private fun persist() {
        DownloadsStorage.savePayload(DownloadsCodec.encodePayload(_uiState.value.items))
    }

    private fun nextDownloadId(nowEpochMs: Long): String {
        val ordinal = synchronized(stateLock) {
            nextDownloadOrdinal += 1L
            nextDownloadOrdinal
        }
        return buildString {
            append(nowEpochMs.toString(36))
            append('_')
            append(ordinal.toString(36))
        }
    }

    private fun removeActiveHandle(downloadId: String): DownloadsTaskHandle? =
        synchronized(stateLock) { activeHandles.remove(downloadId) }

    private fun registerActiveHandle(downloadId: String, handle: DownloadsTaskHandle) {
        synchronized(stateLock) { activeHandles[downloadId] = handle }
    }

    private fun shouldPersistProgress(): Boolean = synchronized(stateLock) {
        val now = DownloadsClock.nowEpochMs()
        if (now - lastProgressPersistAtEpochMs < DOWNLOAD_PROGRESS_PERSIST_INTERVAL_MS) {
            false
        } else {
            lastProgressPersistAtEpochMs = now
            true
        }
    }

    private fun normalizeCompletedLocalFileUri(item: DownloadItem): DownloadItem {
        if (item.status != DownloadStatus.Completed) return item
        val resolvedUri = DownloadsPlatformDownloader.resolveLocalFileUri(
            localFileUri = item.localFileUri,
            destinationFileName = item.fileName,
        ) ?: return item
        return if (resolvedUri != item.localFileUri) {
            item.copy(localFileUri = resolvedUri)
        } else {
            item
        }
    }
}

internal fun DownloadItem.hasPlayableLocalFile(): Boolean =
    status == DownloadStatus.Completed &&
        DownloadsPlatformDownloader.resolveLocalFileUri(
            localFileUri = localFileUri,
            destinationFileName = fileName,
        ) != null

@Serializable
internal data class StoredDownloadsPayload(
    val items: List<DownloadItem> = emptyList(),
    val showMetadata: Map<String, StoredOfflineMeta> = emptyMap(),
)

@Serializable
private data class StoredDownloadItemsPayload(val items: List<DownloadItem> = emptyList())

internal object DownloadsCodec {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun decodePayload(payload: String): StoredDownloadsPayload = runCatching {
        val root = json.parseToJsonElement(payload) as? kotlinx.serialization.json.JsonObject
            ?: return StoredDownloadsPayload()
        val items = (root["items"] as? kotlinx.serialization.json.JsonArray).orEmpty()
            .mapNotNull { element -> runCatching { json.decodeFromJsonElement<DownloadItem>(element) }.getOrNull() }
        val metadata = runCatching {
            root["showMetadata"]?.let { json.decodeFromJsonElement<Map<String, StoredOfflineMeta>>(it) }.orEmpty()
        }.getOrDefault(emptyMap())
        StoredDownloadsPayload(items = items, showMetadata = metadata)
    }.getOrDefault(StoredDownloadsPayload())

    fun encodePayload(items: Collection<DownloadItem>): String =
        json.encodeToString(StoredDownloadItemsPayload(items = items.toList()))
}

private const val PROGRESS_PERSIST_INTERVAL_MS = 5_000L

private fun sanitizeRequestHeaders(headers: Map<String, String>?): Map<String, String> =
    headers
        .orEmpty()
        .mapNotNull { (key, value) ->
            val normalizedKey = key.trim()
            val normalizedValue = value.trim()
            if (
                normalizedKey.isBlank() ||
                normalizedValue.isBlank() ||
                normalizedKey.equals("Accept-Encoding", ignoreCase = true) ||
                normalizedKey.equals("Range", ignoreCase = true)
            ) {
                null
            } else {
                normalizedKey to normalizedValue
            }
        }
        .toMap()

private fun sanitizeResponseHeaders(headers: Map<String, String>?): Map<String, String> =
    headers
        .orEmpty()
        .mapNotNull { (key, value) ->
            val normalizedKey = key.trim()
            val normalizedValue = value.trim()
            if (normalizedKey.isBlank() || normalizedValue.isBlank()) {
                null
            } else {
                normalizedKey to normalizedValue
            }
        }
        .toMap()

internal fun buildLogicalKey(
    parentMetaId: String,
    seasonNumber: Int?,
    episodeNumber: Int?,
): String = downloadLogicalContentKey(parentMetaId, seasonNumber, episodeNumber)

private fun buildFileName(
    title: String,
    seasonNumber: Int?,
    episodeNumber: Int?,
    episodeTitle: String?,
    fallbackTitle: String,
    sourceUrl: String,
    downloadId: String,
): String {
    val baseTitle = if (seasonNumber != null && episodeNumber != null) {
        buildString {
            append(title)
            append(" S")
            append(seasonNumber.toString().padStart(2, '0'))
            append('E')
            append(episodeNumber.toString().padStart(2, '0'))
            if (!episodeTitle.isNullOrBlank()) {
                append(' ')
                append(episodeTitle)
            }
        }
    } else {
        title.ifBlank { fallbackTitle }
    }

    val extension = sourceUrl.fileExtensionFromUrl()
    return buildString {
        append(baseTitle.sanitizeFileName().ifBlank { "download" }.take(92))
        append('_')
        append(downloadId)
        append('.')
        append(extension)
    }
}

private fun String.sanitizeFileName(): String =
    trim().replace(Regex("[^A-Za-z0-9._ -]"), "_")

private fun String.fileExtensionFromUrl(): String {
    val withoutQuery = substringBefore('?').substringBefore('#')
    val suffix = withoutQuery.substringAfterLast('.', missingDelimiterValue = "")
        .lowercase()
        .trim()

    return if (suffix.length in 2..5 && suffix.all { it.isLetterOrDigit() }) {
        suffix
    } else {
        "mp4"
    }
}

internal fun String.isSupportedDownloadUrl(): Boolean {
    val normalized = trim().lowercase()
    if (normalized.startsWith("magnet:")) return false
    if (normalized.endsWith(".m3u8") || normalized.contains(".m3u8?")) return false
    if (normalized.endsWith(".mpd") || normalized.contains(".mpd?")) return false
    if (normalized.endsWith(".torrent") || normalized.contains(".torrent?")) return false
    return normalized.startsWith("http://") || normalized.startsWith("https://")
}

private const val SOURCE_SEARCH_MAX_AGE_MS = 24L * 60L * 60L * 1_000L
private val SOURCE_SEARCH_RETRY_DELAYS_MS = listOf(
    5L * 60L * 1_000L,
    15L * 60L * 1_000L,
    30L * 60L * 1_000L,
    60L * 60L * 1_000L,
)
private const val MAX_SOURCE_SEARCH_ATTEMPTS = 10
private const val DOWNLOAD_PROGRESS_PERSIST_INTERVAL_MS = 2_000L
