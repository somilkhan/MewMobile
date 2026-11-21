package com.nuvio.app.features.streams

import co.touchlab.kermit.Logger
import com.nuvio.app.core.logging.InAppLogger
import com.nuvio.app.core.build.AppFeaturePolicy
import com.nuvio.app.core.diagnostics.DiagnosticEvent
import com.nuvio.app.core.diagnostics.RuntimeDiagnostics
import com.nuvio.app.features.addons.AddonRepository
import com.nuvio.app.features.addons.enabledAddons
import com.nuvio.app.features.addons.httpGetText
import com.nuvio.app.features.cloudstream.CloudStreamRepository
import com.nuvio.app.features.cloudstream.isCloudStreamDiscoveryAllowed
import com.nuvio.app.features.cloudstream.parseCloudStreamRouteId
import com.nuvio.app.features.cloudstream.toStreamItem
import com.nuvio.app.features.debrid.DirectDebridStreamPreparer
import com.nuvio.app.features.debrid.DebridSettingsRepository
import com.nuvio.app.features.debrid.DebridStreamPresentation
import com.nuvio.app.features.details.MetaDetailsRepository
import com.nuvio.app.features.downloads.DownloadsRepository
import com.nuvio.app.features.downloads.DownloadSubtitles
import com.nuvio.app.features.downloads.displayTitle
import com.nuvio.app.features.player.PlayerSettingsRepository
import com.nuvio.app.features.tmdb.TmdbSettingsRepository
import com.nuvio.app.features.plugins.PluginRepository
import com.nuvio.app.features.plugins.resolvePluginTmdbId
import com.nuvio.app.features.plugins.PluginsUiState
import com.nuvio.app.features.plugins.isExcludedByPluginQualityFilter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import nuvio.composeapp.generated.resources.*
import org.jetbrains.compose.resources.getString
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withTimeoutOrNull

object StreamsRepository {
    private val log = Logger.withTag("StreamsRepo")
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _uiState = MutableStateFlow(StreamsUiState())
    val uiState: StateFlow<StreamsUiState> = _uiState.asStateFlow()

    private var activeJob: Job? = null
    private var activeRequestKey: String? = null
    private var activeContentRequestKey: String? = null
    private var activeRequestGeneration = 0L

    fun requestToken(
        type: String,
        videoId: String,
        season: Int? = null,
        episode: Int? = null,
        manualSelection: Boolean = false,
    ): String =
        "$type::$videoId::$season::$episode::$manualSelection"

    fun load(
        type: String,
        videoId: String,
        parentMetaId: String? = null,
        parentMetaType: String? = null,
        season: Int? = null,
        episode: Int? = null,
        manualSelection: Boolean = false,
        searchTitle: String? = null,
    ) {
        load(
            type = type,
            videoId = videoId,
            parentMetaId = parentMetaId,
            parentMetaType = parentMetaType,
            season = season,
            episode = episode,
            manualSelection = manualSelection,
            searchTitle = searchTitle,
            forceRefresh = false,
        )
    }

    fun reload(
        type: String,
        videoId: String,
        parentMetaId: String? = null,
        parentMetaType: String? = null,
        season: Int? = null,
        episode: Int? = null,
        manualSelection: Boolean = false,
        searchTitle: String? = null,
    ) {
        load(
            type = type,
            videoId = videoId,
            parentMetaId = parentMetaId,
            parentMetaType = parentMetaType,
            season = season,
            episode = episode,
            manualSelection = manualSelection,
            searchTitle = searchTitle,
            forceRefresh = true,
        )
    }

    private fun load(
        type: String,
        videoId: String,
        parentMetaId: String?,
        parentMetaType: String?,
        season: Int?,
        episode: Int?,
        manualSelection: Boolean,
        searchTitle: String?,
        forceRefresh: Boolean,
    ) {
        TmdbSettingsRepository.ensureLoaded()
        val tmdbEnabled = TmdbSettingsRepository.snapshot().enabled
        val pluginUiState = if (AppFeaturePolicy.pluginsEnabled) {
            PluginRepository.initialize()
            PluginRepository.uiState.value
        } else {
            PluginsUiState(pluginsEnabled = false)
        }
        val cloudStreamSearchRequest = buildCloudStreamSearchRequest(
            type = type,
            videoId = videoId,
            parentMetaId = parentMetaId,
            parentMetaType = parentMetaType,
            season = season,
            episode = episode,
            searchTitle = searchTitle,
        )
        val cloudStreamRoute = parseCloudStreamRouteId(videoId)
        // CloudStream is independent of the Nuvio plugin feature switch.
        val cloudStreamProviderGroups = cloudStreamProviderGroupsForRequest(
            type = type,
            request = cloudStreamSearchRequest,
            directRoute = cloudStreamRoute,
            groupByRepository = pluginUiState.groupStreamsByRepository,
        )
        // Keep repository grouping as a presentation choice only. Fetch each provider
        // independently so a slow sibling cannot hold back every result in its repository.
        val cloudStreamProviderTasks = flattenCloudStreamProviderTasks(cloudStreamProviderGroups)
        val cloudStreamRegistryRevision = if (isCloudStreamDiscoveryAllowed(tmdbEnabled)) {
            CloudStreamRepository.uiState.value.registryRevision
        } else {
            0L
        }
        val requestToken = requestToken(
            type = type,
            videoId = videoId,
            season = season,
            episode = episode,
            manualSelection = manualSelection,
        )
        val pluginQualityKey = pluginUiState.excludedQualities.sorted().joinToString(",")
        val stableContentRequestKey = "$type::$videoId::$season::$episode" +
            "::pluginsGrouped=${pluginUiState.groupStreamsByRepository}" +
            "::pluginQuality=$pluginQualityKey" +
            "::cloudTarget=${cloudStreamSearchRequest?.cacheKey.orEmpty()}" +
            "::tmdbEnabled=$tmdbEnabled"
        val contentRequestKey = "$stableContentRequestKey::cloudstream=$cloudStreamRegistryRevision"
        val requestKey = "$contentRequestKey::manualSelection=$manualSelection"
        val currentState = _uiState.value
        if (
            !forceRefresh &&
            shouldReuseStreamRequest(
                sameRequest = activeRequestKey == requestKey,
                hasResult = currentState.groups.isNotEmpty() || currentState.emptyStateReason != null,
                isLoading = currentState.isAnyLoading,
                jobActive = activeJob?.isActive == true,
            )
        ) {
            log.d { "Skipping stream reload for unchanged request type=$type id=$videoId" }
            return
        }
        if (
            !forceRefresh &&
            manualSelection &&
            shouldReuseStreamRequest(
                sameRequest = activeContentRequestKey == stableContentRequestKey,
                hasResult = currentState.groups.isNotEmpty() || currentState.emptyStateReason != null,
                isLoading = currentState.isAnyLoading,
                jobActive = activeJob?.isActive == true,
            )
        ) {
            log.d { "Reusing prefetched streams for manual picker type=$type id=$videoId" }
            activeRequestKey = requestKey
            _uiState.update {
                it.copy(
                    requestToken = requestToken,
                    autoPlayStream = null,
                    autoPlayCandidates = emptyList(),
                    isDirectAutoPlayFlow = false,
                    showDirectAutoPlayOverlay = false,
                    overlayMessage = null,
                )
            }
            return
        }

        val requestGeneration = ++activeRequestGeneration
        RuntimeDiagnostics.record(DiagnosticEvent.StreamLoadStarted)
        RuntimeDiagnostics.updateStreams(groups = 0, loadingGroups = 0, providerTasks = 0)
        activeRequestKey = requestKey
        activeContentRequestKey = contentRequestKey
        activeJob?.cancel()
        _uiState.value = StreamsUiState(requestToken = requestToken)
        fun isCurrentRequest(): Boolean =
            activeRequestGeneration == requestGeneration

        PlayerSettingsRepository.ensureLoaded()
        val playerSettings = PlayerSettingsRepository.uiState.value
        val debridSettings = DebridSettingsRepository.snapshot()
        val streamBadgeRules = StreamBadgeSettingsRepository.snapshot()
        InAppLogger.info(
            "Streams/StreamsRepository",
            "Loading streams type=$type id=$videoId season=${season ?: -1} episode=${episode ?: -1} " +
                "manual=$manualSelection force=$forceRefresh",
        )
        val autoPlayMode = playerSettings.streamAutoPlayMode
        val smartSettings = SmartStreamSettingsRepository.state.value
        val smartAutoPlay = !manualSelection && smartSettings.autoPlay
        val smartDevice = if (smartAutoPlay) currentSmartDeviceProfile() else null
        val isAutoPlayEnabled = smartAutoPlay || (
            !manualSelection && autoPlayMode != StreamAutoPlayMode.MANUAL &&
                !(autoPlayMode == StreamAutoPlayMode.REGEX_MATCH &&
                    !StreamAutoPlayPolicy.isRegexSelectionConfigured(playerSettings.streamAutoPlayRegex))
            )

        // Persisted binge groups should only rank streams once auto-play is already active.
        // Manual mode must keep the picker visible and never enter the direct overlay flow.
        val persistedBingeGroup = if (
            playerSettings.streamAutoPlayPreferBingeGroup &&
            playerSettings.streamAutoPlayReuseBingeGroup
        ) {
            parentMetaId?.let { BingeGroupCacheRepository.get(it) }
        } else null
        val persistedFilename = if (persistedBingeGroup != null) parentMetaId?.let(BingeGroupCacheRepository::getFilename) else null
        val persistedInfoHash = if (persistedBingeGroup != null) parentMetaId?.let(BingeGroupCacheRepository::getInfoHash) else null
        val persistedNextFilename = persistedFilename?.let { filename ->
            val extracted = FilenameEpisodeMatcher.extractSeasonEpisode(filename)
            if (extracted != null && episode != null && episode > 0) {
                FilenameEpisodeMatcher.buildNextFilename(filename, extracted.first, extracted.second, season, episode) ?: filename
            } else filename
        }
        val isDirectAutoPlayFlow = isAutoPlayEnabled

        if (isDirectAutoPlayFlow) {
            _uiState.value = StreamsUiState(
                requestToken = requestToken,
                isDirectAutoPlayFlow = true,
                autoPlayDecided = true,
                showDirectAutoPlayOverlay = true,
            )
        }

        val downloadedGroup = findDownloadedStreamGroup(type, videoId, parentMetaId, season, episode)

        val embeddedStreams = MetaDetailsRepository.findEmbeddedStreams(videoId)
        if (embeddedStreams.isNotEmpty()) {
            log.d { "Using ${embeddedStreams.size} embedded streams for type=$type id=$videoId" }
            InAppLogger.info("Streams/StreamsRepository", "Using embedded streams count=${embeddedStreams.size} type=$type id=$videoId")
            val group = AddonStreamGroup(
                addonName = embeddedStreams.first().addonName,
                addonId = "embedded",
                streams = embeddedStreams,
                isLoading = false,
            )
            val presentedGroup = StreamBadgePresentation.apply(
                groups = listOf(group),
                rules = streamBadgeRules,
            ).firstOrNull() ?: group
            val finalGroups = listOfNotNull(downloadedGroup, presentedGroup)
            _uiState.value = StreamsUiState(
                requestToken = requestToken,
                groups = finalGroups,
                autoPlayDecided = true,
                activeAddonIds = finalGroups.map { it.addonId }.toSet(),
                isAnyLoading = false,
            )
            RuntimeDiagnostics.updateStreams(groups = 1, loadingGroups = 0, providerTasks = 0)
            RuntimeDiagnostics.record(DiagnosticEvent.StreamLoadFinished)
            return
        }

        // Addon stream endpoints generally require an external ID. Never send an
        // opaque CloudStream route to unrelated addons; plugin scrapers may still try
        // title-based lookup when no external ID is available.
        val streamLookupId = if (cloudStreamRoute != null) {
            cloudStreamSearchRequest?.externalId ?: cloudStreamSearchRequest?.title.orEmpty()
        } else {
            videoId
        }

        val installedAddons = AddonRepository.uiState.value.addons.enabledAddons()
        val pluginScrapers = if (AppFeaturePolicy.pluginsEnabled) {
            PluginRepository.getEnabledScrapersForType(type)
        } else {
            emptyList()
        }
        val pluginBatchCount = (pluginScrapers.size + PLUGIN_SCRAPER_CONCURRENCY - 1) / PLUGIN_SCRAPER_CONCURRENCY
        val totalStreamTimeoutMs = maxOf(
            STREAM_TOTAL_TIMEOUT_MS,
            pluginBatchCount * PLUGIN_STREAM_PROVIDER_TIMEOUT_MS + 5_000L,
        )
        val pluginProviderGroups = pluginScrapers.toPluginProviderGroups(
            repositories = pluginUiState.repositories,
            groupByRepository = pluginUiState.groupStreamsByRepository,
        )

        if (installedAddons.isEmpty() && pluginProviderGroups.isEmpty() && cloudStreamProviderGroups.isEmpty()) {
            _uiState.value = StreamsUiState(
                requestToken = requestToken,
                isAnyLoading = false,
                emptyStateReason = StreamsEmptyStateReason.NoAddonsInstalled,
            )
            RuntimeDiagnostics.record(DiagnosticEvent.StreamLoadFinished)
            return
        }

        val streamAddons = installedAddons
            .mapNotNull { addon ->
                val manifest = addon.manifest ?: return@mapNotNull null
                if (cloudStreamRoute != null && cloudStreamSearchRequest?.externalId == null) {
                    return@mapNotNull null
                }
                val supportsRequestedStream = manifest.resources.any { resource ->
                    resource.name == "stream" &&
                        resource.types.contains(type) &&
                        (resource.idPrefixes.isEmpty() ||
                            resource.idPrefixes.any { streamLookupId.startsWith(it) })
                }
                if (!supportsRequestedStream) return@mapNotNull null

                InstalledStreamAddonTarget(
                    addonName = addon.displayTitle.ifBlank { manifest.name },
                    addonId = addon.streamAddonInstanceId(manifest.id),
                    manifest = manifest,
                )
            }

        log.d {
            "Found ${streamAddons.size} addons and ${cloudStreamProviderTasks.size} compatible CloudStream provider tasks " +
                "across ${cloudStreamProviderGroups.size} groups for stream type=$type id=$videoId"
        }

        if (streamAddons.isEmpty() && pluginProviderGroups.isEmpty() && cloudStreamProviderGroups.isEmpty()) {
            _uiState.value = StreamsUiState(
                requestToken = requestToken,
                isAnyLoading = false,
                emptyStateReason = StreamsEmptyStateReason.NoCompatibleAddons,
            )
            RuntimeDiagnostics.record(DiagnosticEvent.StreamLoadFinished)
            return
        }

        // Initialise loading placeholders
        val searchTargets = StreamSearchTargets(
            type = type,
            videoId = videoId,
            season = season,
            episode = episode,
            streamAddons = streamAddons,
            serverSources = serverSources,
            pluginProviderGroups = pluginProviderGroups,
        )
        val installedAddonOrder = streamAddons.map { it.addonName }
        val placeholderGroups = StreamAutoPlaySelector.orderAddonStreams(streamAddons.map { addon ->
            AddonStreamGroup(
                addonName = addon.addonName,
                addonId = addon.addonId,
                streams = emptyList(),
                isLoading = true,
            )
        } + pluginProviderGroups.map { providerGroup ->
            AddonStreamGroup(
                addonName = providerGroup.addonName,
                addonId = providerGroup.addonId,
                streams = emptyList(),
                isLoading = true,
            )
        } + cloudStreamProviderGroups.map { providerGroup ->
            AddonStreamGroup(
                addonName = providerGroup.addonName,
                addonId = providerGroup.addonId,
                streams = emptyList(),
                isLoading = true,
            )
        }, installedAddonOrder)
        val isInitiallyLoading = initialGroups.any { it.isLoading }
        _uiState.value = StreamsUiState(
            requestToken = requestToken,
            groups = initialGroups,
            activeAddonIds = initialGroups.map { it.addonId }.toSet(),
            isAnyLoading = isInitiallyLoading,
            emptyStateReason = null,
            isDirectAutoPlayFlow = isDirectAutoPlayFlow,
            autoPlayDecided = true,
            showDirectAutoPlayOverlay = isDirectAutoPlayFlow,
        )

        activeJob = scope.launch {
            val pluginRemainingByAddonId = pluginProviderGroups
                .associate { it.addonId to it.scrapers.size }
                .toMutableMap()
            val pluginFirstErrorByAddonId = mutableMapOf<String, String>()
            val cloudStreamRemainingByAddonId = cloudStreamProviderTasks
                .groupingBy { it.addonId }
                .eachCount()
                .toMutableMap()
            val cloudStreamFirstErrorByAddonId = mutableMapOf<String, String>()
            val totalTasks = streamAddons.size +
                pluginProviderGroups.sumOf { it.scrapers.size } +
                cloudStreamProviderTasks.size
            RuntimeDiagnostics.updateStreams(
                groups = initialGroups.size,
                loadingGroups = initialGroups.count { it.isLoading },
                providerTasks = totalTasks,
            )
            val cloudStreamSemaphore = Semaphore(CLOUDSTREAM_STREAM_PROVIDER_CONCURRENCY)
            // Plugin repositories can expose dozens of scrapers. Launching every scraper at once
            // creates a large burst of network/DEX work and competes with the UI thread during
            // stream-screen entry. Keep the full provider set available, but bound execution.
            val pluginScraperSemaphore = Semaphore(PLUGIN_SCRAPER_CONCURRENCY)

            val installedAddonNames = installedAddonOrder.toSet()
            val installedAddonIds = streamAddons.map { it.addonId }.toSet()
            var autoSelectTriggered = false
            var timeoutElapsed = false
            fun smartRanking(): SmartAutoPlayRanking? = smartDevice?.let { device ->
                SmartStreamRanking.rankForAutoPlay(
                    streams = _uiState.value.groups.flatMap { it.streams },
                    settings = smartSettings,
                    device = device,
                    debridEnabled = debridSettings.canResolvePlayableLinks,
                    isEpisode = season != null && episode != null,
                    preferredBingeGroup = persistedBingeGroup,
                )
            }

            fun smartEvaluation(ranking: SmartAutoPlayRanking): StreamAutoPlayEvaluation {
                val ready = ranking.ranked.map { it.stream }
                return StreamAutoPlayEvaluation(stream = ready.firstOrNull(), readyStreams = ready)
            }

            fun evaluateAutoPlay(bingeGroupOnly: Boolean = false): StreamAutoPlayEvaluation =
                if (smartAutoPlay && !bingeGroupOnly) {
                    smartRanking()?.let(::smartEvaluation) ?: StreamAutoPlayEvaluation()
                } else StreamAutoPlaySelector.evaluateAutoPlayStream(
                    streams = _uiState.value.groups.flatMap { it.streams },
                    mode = autoPlayMode,
                    regexPattern = playerSettings.streamAutoPlayRegex,
                    source = playerSettings.streamAutoPlaySource,
                    installedAddonNames = installedAddonNames,
                    selectedAddons = playerSettings.streamAutoPlaySelectedAddons,
                    selectedPlugins = playerSettings.streamAutoPlaySelectedPlugins,
                    preferredBingeGroup = persistedBingeGroup,
                    preferBingeGroupInSelection = persistedBingeGroup != null,
                    bingeGroupOnly = bingeGroupOnly,
                    debridEnabled = debridSettings.canResolvePlayableLinks,
                    activeResolverProviderId = debridSettings.activeResolverProviderId,
                )

            fun settleAutoPlay(evaluation: StreamAutoPlayEvaluation) {
                if (autoSelectTriggered) return
                autoSelectTriggered = true
                _uiState.update { current ->
                    if (evaluation.stream == null) {
                        current.copy(
                            autoPlayStream = null,
                            autoPlayCandidates = emptyList(),
                            isDirectAutoPlayFlow = false,
                            showDirectAutoPlayOverlay = false,
                        )
                    } else {
                        current.copy(
                            autoPlayStream = evaluation.stream,
                            autoPlayCandidates = evaluation.readyStreams,
                        )
                    }
                }
            }

            fun updateAutoPlayAfterStreamsChanged() {
                if (!isDirectAutoPlayFlow || autoSelectTriggered) return

                // Try binge group + filename first (most precise), then infoHash
                if (persistedBingeGroup != null && !timeoutElapsed) {
                    val allStreams = _uiState.value.groups.flatMap { it.streams }
                    // Filename match
                    if (persistedFilename != null) {
                        val nextFilename = run {
                            val extracted = FilenameEpisodeMatcher.extractSeasonEpisode(persistedFilename)
                            if (extracted != null) {
                                FilenameEpisodeMatcher.buildNextFilename(
                                    persistedFilename, extracted.first, extracted.second, season, episode ?: 0
                                ) ?: persistedFilename
                            } else persistedFilename
                        }
                        val bingeAndFilename = allStreams.firstOrNull { stream ->
                            stream.behaviorHints.bingeGroup == persistedBingeGroup &&
                                FilenameEpisodeMatcher.matches(stream.behaviorHints.filename, nextFilename)
                        }
                        if (bingeAndFilename != null) {
                            settleAutoPlay(StreamAutoPlayEvaluation(bingeAndFilename, allStreams))
                            return
                        }
                    }
                    // InfoHash match (fallback when filename didn't match or was null)
                    if (persistedInfoHash != null) {
                        val bingeAndInfoHash = allStreams.firstOrNull { stream ->
                            stream.behaviorHints.bingeGroup == persistedBingeGroup &&
                                stream.infoHash?.equals(persistedInfoHash, ignoreCase = true) == true
                        }
                        if (bingeAndInfoHash != null) {
                            settleAutoPlay(StreamAutoPlayEvaluation(bingeAndInfoHash, allStreams))
                            return
                        }
                    }
                }

                if (smartAutoPlay) {
                    val ranking = smartRanking() ?: return
                    val groups = _uiState.value.groups.filterNot { it.isLocalDownload }
                    _uiState.update { current ->
                        current.copy(
                            smartAutoPlaySearch = SmartAutoPlaySearch(
                                loadedSources = groups.count { !it.isLoading },
                                totalSources = groups.size,
                                candidateSummary = ranking.ranked.firstOrNull()?.info?.summary(instantLabel = null),
                            ),
                        )
                    }
                    val allLoaded = groups.none { it.isLoading }
                    when {
                        allLoaded -> settleAutoPlay(smartEvaluation(ranking))
                        timeoutElapsed && ranking.ranked.isNotEmpty() -> settleAutoPlay(smartEvaluation(ranking))
                    }
                    return
                }

                val earlyEvaluation = when {
                    timeoutElapsed -> evaluateAutoPlay()
                    persistedBingeGroup != null -> evaluateAutoPlay(bingeGroupOnly = true)
                    else -> null
                }
                if (earlyEvaluation?.stream != null) {
                    settleAutoPlay(earlyEvaluation)
                    return
                }

                if (
                    _uiState.value.groups.areAutoPlaySourcesLoaded(
                        source = playerSettings.streamAutoPlaySource,
                        installedAddonIds = installedAddonIds,
                    )
                ) {
                    settleAutoPlay(evaluateAutoPlay())
                }
            }

            fun presentStreamGroup(group: AddonStreamGroup): AddonStreamGroup {
                val badgeGroup = StreamBadgePresentation.apply(
                    groups = listOf(group),
                    rules = streamBadgeRules,
                ).firstOrNull() ?: group
                return DebridStreamPresentation.apply(
                    groups = listOf(badgeGroup),
                    settings = debridSettings,
                ).firstOrNull() ?: badgeGroup
            }

            fun tryAutoSelectAvailableStream() {
                if (
                    activeRequestGeneration != requestGeneration
                ) return
                if (!isDirectAutoPlayFlow || autoSelectTriggered) return
                val bingeGroupOnly = !timeoutElapsed
                if (bingeGroupOnly && persistedBingeGroup == null) return
                val allStreams = _uiState.value.groups.flatMap { it.streams }
                if (allStreams.isEmpty()) return

                val selected = StreamAutoPlaySelector.selectAutoPlayStream(
                    streams = allStreams,
                    mode = autoPlayMode,
                    regexPattern = playerSettings.streamAutoPlayRegex,
                    source = playerSettings.streamAutoPlaySource,
                    installedAddonNames = installedAddonNames,
                    selectedAddons = playerSettings.streamAutoPlaySelectedAddons,
                    selectedPlugins = playerSettings.streamAutoPlaySelectedPlugins,
                    preferredBingeGroup = persistedBingeGroup,
                    preferredFilename = persistedNextFilename,
                    preferredInfoHash = persistedInfoHash,
                    preferBingeGroupInSelection = persistedBingeGroup != null,
                    bingeGroupOnly = bingeGroupOnly,
                    debridEnabled = debridSettings.canResolvePlayableLinks,
                    activeResolverProviderId = debridSettings.activeResolverProviderId,
                ) ?: return
                autoSelectTriggered = true
                _uiState.update { it.copy(autoPlayStream = selected) }
            }

            fun publishAddonGroup(group: AddonStreamGroup) {
                _uiState.update { current ->
                    if (
                        activeRequestGeneration != requestGeneration
                    ) return@update current
                    val updated = StreamAutoPlaySelector.orderAddonStreams(
                        groups = current.groups.map { currentGroup ->
                            if (currentGroup.addonId == group.addonId) group else currentGroup
                        },
                        installedOrder = installedAddonOrder,
                        preferredGroupIds = preferredGroupIds,
                    )
                    val anyLoading = updated.any { it.isLoading }
                    current.copy(
                        groups = updated,
                        isAnyLoading = anyLoading,
                        emptyStateReason = updated.toEmptyStateReason(anyLoading),
                    )
                }
                updateAutoPlayAfterStreamsChanged()
            }

            fun publishAddonGroupAfterCacheCheck(group: AddonStreamGroup) {
                if (group.addonId !in installedAddonIds || group.streams.isEmpty()) {
                    publishAddonGroup(presentStreamGroup(group))
                    tryAutoSelectAvailableStream()
                    return
                }

                val eligibleGroupIds = setOf(group.addonId)
                val shouldWaitForCacheCheck = LocalDebridAvailabilityService.hasPendingCacheCheck(
                    groups = listOf(group),
                    eligibleGroupIds = eligibleGroupIds,
                )
                if (!shouldWaitForCacheCheck) {
                    publishAddonGroup(presentStreamGroup(group))
                    tryAutoSelectAvailableStream()
                    return
                }

                val checkingGroup = LocalDebridAvailabilityService.markChecking(
                    groups = listOf(group),
                    eligibleGroupIds = eligibleGroupIds,
                ).firstOrNull() ?: group

                // Show network results immediately; cache availability is an enrichment step.
                publishAddonGroup(presentStreamGroup(checkingGroup))
                tryAutoSelectAvailableStream()

                val availabilityJob = launch {
                    val availabilityGroup = withTimeoutOrNull(DEBRID_AVAILABILITY_TIMEOUT_MS) {
                        LocalDebridAvailabilityService.annotateCachedAvailability(
                            groups = listOf(checkingGroup),
                            eligibleGroupIds = eligibleGroupIds,
                        ).firstOrNull()
                    } ?: checkingGroup
                    publishAddonGroup(presentStreamGroup(availabilityGroup))
                    tryAutoSelectAvailableStream()

                    // Early binge-group match right after this addon's availability is resolved
                    if (isDirectAutoPlayFlow && !autoSelectTriggered && persistedBingeGroup != null && !timeoutElapsed) {
                        val allStreams = _uiState.value.groups.flatMap { it.streams }
                        if (allStreams.isNotEmpty()) {
                            val earlyMatch = StreamAutoPlaySelector.selectAutoPlayStream(
                                streams = allStreams,
                                mode = autoPlayMode,
                                regexPattern = playerSettings.streamAutoPlayRegex,
                                source = playerSettings.streamAutoPlaySource,
                                installedAddonNames = installedAddonNames,
                                selectedAddons = playerSettings.streamAutoPlaySelectedAddons,
                                selectedPlugins = playerSettings.streamAutoPlaySelectedPlugins,
                                preferredBingeGroup = persistedBingeGroup,
                    preferredFilename = persistedNextFilename,
                    preferredInfoHash = persistedInfoHash,
                                preferBingeGroupInSelection = true,
                                bingeGroupOnly = true,
                                debridEnabled = debridSettings.canResolvePlayableLinks,
                                activeResolverProviderId = debridSettings.activeResolverProviderId,
                            )
                            if (earlyMatch != null) {
                                autoSelectTriggered = true
                                _uiState.update { it.copy(autoPlayStream = earlyMatch) }
                            }
                        }
                    }
                }
                debridAvailabilityJobs += availabilityJob
            }

            fun stopPendingGroups(errorMessage: String? = null) {
                _uiState.update { current ->
                    if (
                        activeRequestGeneration != requestGeneration
                    ) return@update current
                    val updatedGroups = current.groups.map { group ->
                        if (group.isLoading) {
                            group.copy(
                                isLoading = false,
                                error = group.error ?: errorMessage,
                            )
                        } else {
                            group
                        }
                    }
                    current.copy(
                        groups = updatedGroups,
                        isAnyLoading = false,
                        emptyStateReason = updatedGroups.toEmptyStateReason(anyLoading = false),
                    )
                }
            }

            val timeoutJob = if (isDirectAutoPlayFlow) {
                val timeoutSeconds = playerSettings.streamAutoPlayTimeoutSeconds
                val isUnlimitedTimeout = timeoutSeconds == Int.MAX_VALUE
                // Timeout semantics:
                // - 0 (instant): timeoutElapsed immediately, full select on each response
                // - 1-30 (bounded): wait the configured delay, then full select
                // - unlimited (Int.MAX_VALUE): timeoutElapsed immediately, full select on each response,
                //   with 60s hard fallback to stream picker
                if (timeoutSeconds <= 0 || isUnlimitedTimeout) {
                    timeoutElapsed = true
                    // For unlimited: launch a hard 60s fallback to dismiss overlay
                    if (isUnlimitedTimeout) {
                        launch {
                            delay(60_000L)
                            if (!isCurrentRequest()) return@launch
                            if (!autoSelectTriggered) {
                                autoSelectTriggered = true
                                val allStreams = _uiState.value.groups.flatMap { it.streams }
                                if (allStreams.isNotEmpty()) {
                                    val selected = StreamAutoPlaySelector.selectAutoPlayStream(
                                        streams = allStreams,
                                        mode = autoPlayMode,
                                        regexPattern = playerSettings.streamAutoPlayRegex,
                                        source = playerSettings.streamAutoPlaySource,
                                        installedAddonNames = installedAddonNames,
                                        selectedAddons = playerSettings.streamAutoPlaySelectedAddons,
                                        selectedPlugins = playerSettings.streamAutoPlaySelectedPlugins,
                                        preferredBingeGroup = persistedBingeGroup,
                    preferredFilename = persistedNextFilename,
                    preferredInfoHash = persistedInfoHash,
                                        preferBingeGroupInSelection = persistedBingeGroup != null,
                                        bingeGroupOnly = false,
                                        debridEnabled = debridSettings.canResolvePlayableLinks,
                                        activeResolverProviderId = debridSettings.activeResolverProviderId,
                                    )
                                    _uiState.update { it.copy(autoPlayStream = selected) }
                                }
                                if (_uiState.value.autoPlayStream == null) {
                                    _uiState.update {
                                        it.copy(
                                            isDirectAutoPlayFlow = false,
                                            showDirectAutoPlayOverlay = false,
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        null
                    }
                } else {
                    // Bounded timeout (1-30s)
                    launch {
                        delay(timeoutSeconds * 1_000L)
                        if (!isCurrentRequest()) return@launch
                        timeoutElapsed = true
                        if (!autoSelectTriggered) {
                            val allStreams = _uiState.value.groups.flatMap { it.streams }
                            if (allStreams.isNotEmpty()) {
                                val evaluation = StreamAutoPlaySelector.evaluateAutoPlayStream(
                                    streams = allStreams,
                                    mode = autoPlayMode,
                                    regexPattern = playerSettings.streamAutoPlayRegex,
                                    source = playerSettings.streamAutoPlaySource,
                                    installedAddonNames = installedAddonNames,
                                    selectedAddons = playerSettings.streamAutoPlaySelectedAddons,
                                    selectedPlugins = playerSettings.streamAutoPlaySelectedPlugins,
                                    preferredBingeGroup = persistedBingeGroup,
                    preferredFilename = persistedNextFilename,
                    preferredInfoHash = persistedInfoHash,
                                    preferBingeGroupInSelection = persistedBingeGroup != null,
                                    bingeGroupOnly = false,
                                    debridEnabled = debridSettings.canResolvePlayableLinks,
                                    activeResolverProviderId = debridSettings.activeResolverProviderId,
                                )
                                if (evaluation.stream != null || !evaluation.hasPendingDebridCandidate) {
                                    settleAutoPlay(evaluation)
                                }
                            }
                        }
                    }
                }
            } else {
                null
            }

            streamAddons.forEach { addon ->
                launch {
                    val url = buildAddonResourceUrl(
                        manifestUrl = addon.manifest.transportUrl,
                        resource = "stream",
                        type = type,
                        id = streamLookupId,
                    )
                    log.d { "Fetching streams from: $url" }

                    val displayName = addon.addonName
                    val group = runCatchingUnlessCancelled {
                        val payload = withTimeoutOrNull(STREAM_PROVIDER_TIMEOUT_MS) {
                            httpGetText(url)
                        } ?: error("$displayName timed out")
                        StreamParser.parse(
                            payload = payload,
                            addonName = displayName,
                            addonId = addon.addonId,
                            addonLogo = addon.manifest.logoUrl,
                        )
                    }.fold(
                        onSuccess = { streams ->
                            log.d { "Got ${streams.size} streams from ${displayName}" }
                            AddonStreamGroup(
                                addonName = displayName,
                                addonId = addon.addonId,
                                streams = streams,
                                isLoading = false,
                            )
                        },
                        onFailure = { err ->
                            log.w(err) { "Failed to fetch streams from ${displayName}" }
                            AddonStreamGroup(
                                addonName = displayName,
                                addonId = addon.addonId,
                                streams = emptyList(),
                                isLoading = false,
                                error = err.message,
                            )
                        },
                    )
                    publishCompletion(StreamLoadCompletion.Addon(group))
                }
            }

            // TMDB identity resolution is a network operation too. It must be bounded
            // independently; previously it ran before the completion timeout loop, so a
            // stalled TMDB request could leave every plugin group spinning forever.
            val canonicalPluginTmdbId = withTimeoutOrNull(PLUGIN_ID_RESOLUTION_TIMEOUT_MS) {
                runCatchingUnlessCancelled {
                    resolvePluginTmdbId(
                        videoId = streamLookupId,
                        mediaType = type,
                        searchTitle = searchTitle,
                        season = season,
                        episode = episode,
                    )
                }.getOrNull()
            }
            val pluginIdentityError = if (canonicalPluginTmdbId.isNullOrBlank()) {
                "Could not resolve a TMDB ID for plugin lookup"
            } else {
                null
            }
            if (pluginIdentityError != null) {
                log.w { "$pluginIdentityError (type=$type, id=$streamLookupId, title=$searchTitle)" }
            }

            pluginProviderGroups.forEach { providerGroup ->
                val includeScraperNameInSubtitle = false
                providerGroup.scrapers.forEach { scraper ->
                    launch {
                        val completion = if (pluginIdentityError != null) {
                            StreamLoadCompletion.PluginScraper(
                                addonId = providerGroup.addonId,
                                streams = emptyList(),
                                error = pluginIdentityError,
                            )
                        } else {
                            val scraperResult = pluginScraperSemaphore.withPermit {
                                withTimeoutOrNull(PLUGIN_STREAM_PROVIDER_TIMEOUT_MS) {
                                    PluginRepository.executeScraper(
                                        scraper = scraper,
                                        tmdbId = canonicalPluginTmdbId.orEmpty(),
                                        mediaType = type,
                                        season = season,
                                        episode = episode,
                                    )
                                }
                            }
                            (scraperResult ?: Result.failure(Throwable("${scraper.name} timed out"))).fold(
                                onSuccess = { results ->
                                    val filteredResults = results.filterNot { result ->
                                        result.isExcludedByPluginQualityFilter(pluginUiState.excludedQualities)
                                    }
                                    StreamLoadCompletion.PluginScraper(
                                        addonId = providerGroup.addonId,
                                        streams = filteredResults.map { result ->
                                            result.toStreamItem(
                                                scraper = scraper,
                                                addonName = providerGroup.addonName,
                                                addonId = providerGroup.addonId,
                                                includeScraperNameInSubtitle = includeScraperNameInSubtitle,
                                            )
                                        },
                                        error = null,
                                    )
                                },
                                onFailure = { error ->
                                    StreamLoadCompletion.PluginScraper(
                                        addonId = providerGroup.addonId,
                                        streams = emptyList(),
                                        error = error.message ?: getString(Res.string.streams_failed_to_load_scraper, scraper.name),
                                    )
                                },
                            )
                        }
                        publishCompletion(completion)
                    }
                }
            }

            cloudStreamProviderTasks.forEach { providerTask ->
                launch {
                    val group = withTimeoutOrNull(STREAM_PROVIDER_TIMEOUT_MS) {
                        cloudStreamSemaphore.withPermit {
                            resolveCloudStreamProviderStreams(
                                providerGroup = providerTask,
                                request = cloudStreamSearchRequest,
                            )
                        }
                    } ?: AddonStreamGroup(
                        addonName = providerTask.addonName,
                        addonId = providerTask.addonId,
                        streams = emptyList(),
                        isLoading = false,
                        error = "CloudStream provider ${providerTask.providerId} timed out",
                    )
                    publishCompletion(StreamLoadCompletion.CloudStreamProvider(group))
                }
            }

            var receivedTasks = 0
            val allTasksCompleted = withTimeoutOrNull(totalStreamTimeoutMs) {
                while (receivedTasks < totalTasks) {
                    val completion = completions.receive()
                    receivedTasks++
                    when (completion) {
                    is StreamLoadCompletion.Addon -> {
                        val result = completion.group
                        publishAddonGroup(presentStreamGroup(result))
                    }

                    is StreamLoadCompletion.CloudStreamProvider -> {
                        val group = completion.group
                        val remaining = (cloudStreamRemainingByAddonId[group.addonId] ?: 1) - 1
                        cloudStreamRemainingByAddonId[group.addonId] = remaining.coerceAtLeast(0)
                        if (!group.error.isNullOrBlank() && cloudStreamFirstErrorByAddonId[group.addonId].isNullOrBlank()) {
                            cloudStreamFirstErrorByAddonId[group.addonId] = group.error
                        }
                        _uiState.update { current ->
                            if (activeRequestGeneration != requestGeneration) return@update current
                            val updated = StreamAutoPlaySelector.orderAddonStreams(
                                groups = current.groups.map { currentGroup ->
                                    if (currentGroup.addonId != group.addonId) {
                                        currentGroup
                                    } else {
                                        val mergedStreams = (currentGroup.streams + group.streams)
                                            .distinctBy { stream -> "${stream.addonId}:${stream.url}" }
                                            .sortedForGroupedDisplay()
                                        val stillLoading = remaining > 0
                                        val finalError = if (mergedStreams.isEmpty() && !stillLoading) {
                                            cloudStreamFirstErrorByAddonId[group.addonId]
                                        } else {
                                            null
                                        }
                                        presentStreamGroup(currentGroup.copy(
                                            streams = mergedStreams,
                                            isLoading = stillLoading,
                                            error = finalError,
                                        ))
                                    }
                                },
                                installedOrder = installedAddonOrder,
                            )
                            val anyLoading = updated.any { it.isLoading }
                            current.copy(
                                groups = updated,
                                isAnyLoading = anyLoading,
                                emptyStateReason = updated.toEmptyStateReason(anyLoading),
                            )
                        }
                        tryAutoSelectAvailableStream()
                    }

                    is StreamLoadCompletion.PluginScraper -> {
                        val remaining = (pluginRemainingByAddonId[completion.addonId] ?: 1) - 1
                        pluginRemainingByAddonId[completion.addonId] = remaining.coerceAtLeast(0)
                        if (!completion.error.isNullOrBlank() && pluginFirstErrorByAddonId[completion.addonId].isNullOrBlank()) {
                            pluginFirstErrorByAddonId[completion.addonId] = completion.error
                        }

                        _uiState.update { current ->
                            if (
                                activeRequestGeneration != requestGeneration
                            ) return@update current
                            val updated = StreamAutoPlaySelector.orderAddonStreams(
                                groups = current.groups.map { group ->
                                    if (group.addonId != completion.addonId) {
                                        group
                                    } else {
                                        val mergedStreams = if (completion.streams.isEmpty()) {
                                            group.streams
                                        } else {
                                            (group.streams + StreamBadgePresentation.applyToStreams(completion.streams, streamBadgeRules))
                                                .sortedForGroupedDisplay()
                                        }
                                        val stillLoading = remaining > 0
                                        val finalError = if (mergedStreams.isEmpty() && !stillLoading) {
                                            pluginFirstErrorByAddonId[completion.addonId]
                                        } else {
                                            null
                                        }
                                        presentStreamGroup(group.copy(
                                            streams = mergedStreams,
                                            isLoading = stillLoading,
                                            error = finalError,
                                        ))
                                    }
                                },
                                installedOrder = installedAddonOrder,
                                preferredGroupIds = preferredGroupIds,
                            )
                            val anyLoading = updated.any { it.isLoading }
                            current.copy(
                                groups = updated,
                                isAnyLoading = anyLoading,
                                emptyStateReason = updated.toEmptyStateReason(anyLoading),
                            )
                        }
                        tryAutoSelectAvailableStream()
                    }

                    }
                }
                true
            } ?: false

            if (!allTasksCompleted) {
                RuntimeDiagnostics.record(DiagnosticEvent.StreamLoadTimedOut)
                log.w { "Stream loading timed out after $receivedTasks / $totalTasks provider tasks" }
                stopPendingGroups(errorMessage = "Timed out")
            }

            for (availabilityJob in debridAvailabilityJobs) {
                availabilityJob.join()
                if (!isCurrentRequest()) break

                // Early binge-group match after each availability job completes
                if (isDirectAutoPlayFlow && !autoSelectTriggered && persistedBingeGroup != null) {
                    val allStreams = _uiState.value.groups.flatMap { it.streams }
                    if (allStreams.isNotEmpty()) {
                        val earlyMatch = StreamAutoPlaySelector.selectAutoPlayStream(
                            streams = allStreams,
                            mode = autoPlayMode,
                            regexPattern = playerSettings.streamAutoPlayRegex,
                            source = playerSettings.streamAutoPlaySource,
                            installedAddonNames = installedAddonNames,
                            selectedAddons = playerSettings.streamAutoPlaySelectedAddons,
                            selectedPlugins = playerSettings.streamAutoPlaySelectedPlugins,
                            preferredBingeGroup = persistedBingeGroup,
                    preferredFilename = persistedNextFilename,
                    preferredInfoHash = persistedInfoHash,
                            preferBingeGroupInSelection = true,
                            bingeGroupOnly = !timeoutElapsed,
                            debridEnabled = debridSettings.canResolvePlayableLinks,
                            activeResolverProviderId = debridSettings.activeResolverProviderId,
                        )
                        if (earlyMatch != null) {
                            autoSelectTriggered = true
                            _uiState.update { it.copy(autoPlayStream = earlyMatch) }
                            break
                        }
                    }
                }
            }

            launch {
                DirectDebridStreamPreparer.prepare(
                    streams = _uiState.value.groups
                        .filter { it.addonId in installedAddonIds }
                        .flatMap { it.streams },
                    season = season,
                    episode = episode,
                    playerSettings = playerSettings,
                    installedAddonNames = installedAddonNames,
                ) { original, prepared ->
                    _uiState.update { current ->
                        if (!isCurrentRequest()) return@update current
                        current.copy(
                            groups = DirectDebridStreamPreparer.replacePreparedStream(
                                groups = current.groups,
                                original = original,
                                prepared = prepared,
                                eligibleGroupIds = installedAddonIds,
                            ),
                        )
                    }
                    tryAutoSelectAvailableStream()

                    // Early binge-group match after each debrid-prepared stream
                    if (isDirectAutoPlayFlow && !autoSelectTriggered && persistedBingeGroup != null) {
                        val allStreams = _uiState.value.groups.flatMap { it.streams }
                        if (allStreams.isNotEmpty()) {
                            val earlyMatch = StreamAutoPlaySelector.selectAutoPlayStream(
                                streams = allStreams,
                                mode = autoPlayMode,
                                regexPattern = playerSettings.streamAutoPlayRegex,
                                source = playerSettings.streamAutoPlaySource,
                                installedAddonNames = installedAddonNames,
                                selectedAddons = playerSettings.streamAutoPlaySelectedAddons,
                                selectedPlugins = playerSettings.streamAutoPlaySelectedPlugins,
                                preferredBingeGroup = persistedBingeGroup,
                    preferredFilename = persistedNextFilename,
                    preferredInfoHash = persistedInfoHash,
                                preferBingeGroupInSelection = true,
                                bingeGroupOnly = !timeoutElapsed,
                                debridEnabled = debridSettings.canResolvePlayableLinks,
                                activeResolverProviderId = debridSettings.activeResolverProviderId,
                            )
                            if (earlyMatch != null) {
                                autoSelectTriggered = true
                                _uiState.update { current ->
                                    if (!isCurrentRequest()) current else current.copy(autoPlayStream = earlyMatch)
                                }
                            }
                        }
                    }
                }

                // Early match / timeout-elapsed auto-select on each addon response
                if (isDirectAutoPlayFlow && !autoSelectTriggered) {
                    val allStreams = _uiState.value.groups.flatMap { it.streams }
                    if (allStreams.isNotEmpty()) {
                        if (timeoutElapsed) {
                            // After timeout: full fallback (bingeGroupOnly = false)
                            val selected = StreamAutoPlaySelector.selectAutoPlayStream(
                                streams = allStreams,
                                mode = autoPlayMode,
                                regexPattern = playerSettings.streamAutoPlayRegex,
                                source = playerSettings.streamAutoPlaySource,
                                installedAddonNames = installedAddonNames,
                                selectedAddons = playerSettings.streamAutoPlaySelectedAddons,
                                selectedPlugins = playerSettings.streamAutoPlaySelectedPlugins,
                                preferredBingeGroup = persistedBingeGroup,
                    preferredFilename = persistedNextFilename,
                    preferredInfoHash = persistedInfoHash,
                                preferBingeGroupInSelection = persistedBingeGroup != null,
                                bingeGroupOnly = false,
                                debridEnabled = debridSettings.canResolvePlayableLinks,
                                activeResolverProviderId = debridSettings.activeResolverProviderId,
                            )
                            if (selected != null) {
                                autoSelectTriggered = true
                                _uiState.update { current ->
                                    if (!isCurrentRequest()) current else current.copy(autoPlayStream = selected)
                                }
                            }
                        } else if (persistedBingeGroup != null) {
                            // Before timeout: try binge-group-only early match
                            val earlyMatch = StreamAutoPlaySelector.selectAutoPlayStream(
                                streams = allStreams,
                                mode = autoPlayMode,
                                regexPattern = playerSettings.streamAutoPlayRegex,
                                source = playerSettings.streamAutoPlaySource,
                                installedAddonNames = installedAddonNames,
                                selectedAddons = playerSettings.streamAutoPlaySelectedAddons,
                                selectedPlugins = playerSettings.streamAutoPlaySelectedPlugins,
                                preferredBingeGroup = persistedBingeGroup,
                    preferredFilename = persistedNextFilename,
                    preferredInfoHash = persistedInfoHash,
                                preferBingeGroupInSelection = true,
                                bingeGroupOnly = true,
                                debridEnabled = debridSettings.canResolvePlayableLinks,
                                activeResolverProviderId = debridSettings.activeResolverProviderId,
                            )
                            if (earlyMatch != null) {
                                autoSelectTriggered = true
                                _uiState.update { current ->
                                    if (!isCurrentRequest()) current else current.copy(autoPlayStream = earlyMatch)
                                }
                            }
                        }
                    }
                }
            }

            if (isDirectAutoPlayFlow && !autoSelectTriggered) {
                autoSelectTriggered = true
                val allStreams = _uiState.value.groups.flatMap { it.streams }
                val evaluation = StreamAutoPlaySelector.evaluateAutoPlayStream(
                    streams = allStreams,
                    mode = autoPlayMode,
                    regexPattern = playerSettings.streamAutoPlayRegex,
                    source = playerSettings.streamAutoPlaySource,
                    installedAddonNames = installedAddonNames,
                    selectedAddons = playerSettings.streamAutoPlaySelectedAddons,
                    selectedPlugins = playerSettings.streamAutoPlaySelectedPlugins,
                    preferredBingeGroup = persistedBingeGroup,
                    preferredFilename = persistedNextFilename,
                    preferredInfoHash = persistedInfoHash,
                    preferBingeGroupInSelection = persistedBingeGroup != null,
                    bingeGroupOnly = false,
                    debridEnabled = debridSettings.canResolvePlayableLinks,
                    activeResolverProviderId = debridSettings.activeResolverProviderId,
                )
                _uiState.update {
                    if (!isCurrentRequest()) it else it.copy(
                        autoPlayStream = evaluation.stream,
                        autoPlayCandidates = evaluation.readyStreams,
                    )
                }
            }
            if (isDirectAutoPlayFlow && _uiState.value.autoPlayStream == null) {
                _uiState.update {
                    if (!isCurrentRequest()) it else it.copy(
                        isDirectAutoPlayFlow = false,
                        showDirectAutoPlayOverlay = false,
                    )
                }
            }
            InAppLogger.info(
                "Streams/StreamsRepository",
                "Finished loading streams type=$type id=$videoId groups=${_uiState.value.groups.size} streams=${_uiState.value.groups.sumOf { it.streams.size }}",
            )
            timeoutJob?.cancel()
        }
        activeJob?.let { job -> finalizeJob(job, requestGeneration) }
    }

    fun selectFilter(addonId: String?) {
        _uiState.update { it.copy(selectedFilter = addonId) }
    }

    fun consumeAutoPlay() {
        activeRequestKey = null
        _uiState.update {
            it.copy(
                autoPlayStream = null,
                autoPlayCandidates = emptyList(),
                isDirectAutoPlayFlow = false,
                showDirectAutoPlayOverlay = false,
                overlayMessage = null,
            )
        }
    }

    fun skipAutoPlayStream(stream: StreamItem): Boolean {
        var hasNext = false
        _uiState.update { current ->
            val failedIndex = current.autoPlayCandidates.indexOf(stream)
            val remaining = if (failedIndex >= 0) {
                current.autoPlayCandidates.drop(failedIndex + 1)
            } else {
                current.autoPlayCandidates.drop(1)
            }
            hasNext = remaining.isNotEmpty()
            current.copy(
                autoPlayStream = remaining.firstOrNull(),
                autoPlayCandidates = remaining,
                isDirectAutoPlayFlow = remaining.isNotEmpty(),
                showDirectAutoPlayOverlay = remaining.isNotEmpty(),
                overlayMessage = if (remaining.isNotEmpty()) current.overlayMessage else null,
            )
        }
        return hasNext
    }

    fun cancelLoading() {
        val wasLoading = activeJob?.isActive == true ||
            _uiState.value.isAnyLoading ||
            _uiState.value.groups.any { it.isLoading }
        activeRequestGeneration += 1
        activeRequestKey = null
        activeContentRequestKey = null
        activeJob?.cancel()
        activeJob = null
        RuntimeDiagnostics.updateStreamLoading(
            groups = _uiState.value.groups.size,
            loadingGroups = 0,
        )
        if (wasLoading) RuntimeDiagnostics.record(DiagnosticEvent.StreamLoadCancelled)
        _uiState.update { current ->
            fun StreamsUiState.withStoppedOverlay() = copy(
                autoPlayStream = null,
                autoPlayCandidates = emptyList(),
                isDirectAutoPlayFlow = false,
                showDirectAutoPlayOverlay = false,
                overlayMessage = null,
            )
            if (!current.isAnyLoading && current.groups.none { it.isLoading }) {
                current.withStoppedOverlay()
            } else {
                val updatedGroups = current.groups.map { group ->
                    if (group.isLoading) group.copy(isLoading = false) else group
                }
                current.copy(
                    groups = updatedGroups,
                    isAnyLoading = false,
                    emptyStateReason = if (updatedGroups.isEmpty()) {
                        current.emptyStateReason
                    } else {
                        updatedGroups.toEmptyStateReason(anyLoading = false)
                    },
                ).withStoppedOverlay()
            }
        }
    }

    fun clear() {
        activeRequestGeneration += 1
        activeJob?.cancel()
        activeJob = null
        activeRequestKey = null
        activeContentRequestKey = null
        _uiState.value = StreamsUiState()
    }

    fun setOverlayVisible(visible: Boolean, message: String? = null) {
        _uiState.update {
            if (visible) {
                it.copy(showDirectAutoPlayOverlay = true, overlayMessage = message)
            } else {
                it.copy(showDirectAutoPlayOverlay = false, overlayMessage = null)
            }
        }
    }

    private fun finalizeJob(job: Job, requestGeneration: Long) {
        job.invokeOnCompletion { cause ->
            if (
                activeJob !== job ||
                activeRequestGeneration != requestGeneration
            ) return@invokeOnCompletion
            _uiState.update { current ->
                val interrupted = cause != null || current.isAnyLoading || current.groups.any { it.isLoading }
                if (!interrupted) return@update current
                val updatedGroups = current.groups.map { group ->
                    if (group.isLoading) group.copy(isLoading = false, error = group.error ?: "Loading interrupted") else group
                }
                current.copy(
                    groups = updatedGroups,
                    isAnyLoading = false,
                    emptyStateReason = updatedGroups.toEmptyStateReason(anyLoading = false),
                    isDirectAutoPlayFlow = false,
                    showDirectAutoPlayOverlay = false,
                )
            }
            RuntimeDiagnostics.updateStreamLoading(
                groups = _uiState.value.groups.size,
                loadingGroups = 0,
            )
            RuntimeDiagnostics.record(DiagnosticEvent.StreamLoadFinished)
            activeJob = null
        }
    }

    private fun findDownloadedStreamGroup(
        type: String,
        videoId: String,
        parentMetaId: String?,
        season: Int?,
        episode: Int?,
    ): AddonStreamGroup? {
        DownloadsRepository.ensureLoaded()
        val downloadedItem = DownloadsRepository.findPlayableDownload(
            parentMetaId = parentMetaId ?: videoId,
            seasonNumber = season,
            episodeNumber = episode,
            videoId = videoId,
        ) ?: return null
        val localUri = DownloadsRepository.playableLocalFileUri(downloadedItem) ?: return null

        val streamTitle = downloadedItem.streamTitle.ifBlank { downloadedItem.displayTitle() }
        val streamSubtitle = downloadedItem.streamSubtitle
            ?: downloadedItem.providerName.takeIf { it.isNotBlank() }
        val streamItem = StreamItem(
            name = streamTitle,
            title = downloadedItem.episodeTitle ?: downloadedItem.title,
            description = streamSubtitle,
            url = localUri,
            // StreamDestination forwards this name to the player as its provider label.
            addonName = runBlocking { getString(Res.string.provider_downloaded) },
            addonId = DOWNLOADED_ADDON_ID,
            streamType = type,
            behaviorHints = StreamBehaviorHints(
                videoSize = downloadedItem.totalBytes?.takeIf { it > 0L } ?: downloadedItem.downloadedBytes,
            ),
            externalSubtitles = DownloadSubtitles.localSubtitles(localUri),
        )
        return AddonStreamGroup(
            addonName = DOWNLOADED_ADDON_ID,
            addonId = DOWNLOADED_ADDON_ID,
            streams = listOf(streamItem),
            isLoading = false,
        )
    }
}

// Provider count must not be capped: a repository can contain many narrowly scoped
// providers, and an alphabetical cap silently skipped otherwise valid sources.
// Keep network and DEX work bounded with a semaphore instead.
private const val CLOUDSTREAM_STREAM_PROVIDER_CONCURRENCY = 8
// Keep stream scheduling aligned with the four dedicated QuickJS workers.
private const val PLUGIN_SCRAPER_CONCURRENCY = 4
private const val STREAM_PROVIDER_TIMEOUT_MS = 30_000L
// Nuvio plugin execution has its own 60s QuickJS deadline; keep the outer budget above it.
private const val PLUGIN_STREAM_PROVIDER_TIMEOUT_MS = 65_000L
private const val PLUGIN_ID_RESOLUTION_TIMEOUT_MS = 10_000L
private const val STREAM_TOTAL_TIMEOUT_MS = 70_000L
private const val DEBRID_AVAILABILITY_TIMEOUT_MS = 15_000L

internal fun shouldReuseStreamRequest(
    sameRequest: Boolean,
    hasResult: Boolean,
    isLoading: Boolean,
    jobActive: Boolean,
): Boolean = sameRequest && if (isLoading) jobActive else hasResult
