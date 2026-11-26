package com.nuvio.app.features.streams

// Mew-only stream search additions, kept out of the upstream StreamsRepository,
// PlayerStreamsRepository and StreamSearchSessions so upstream merges stay small.
// Upstream files only call into this file from lines marked `// Mew hook`.
//
// Adds:
//  - CloudStream provider search (title search and direct `cloudstream:` routes)
//  - plugin quality filter (Plugins settings -> excluded qualities)
//  - per-scraper timeout and concurrency gate for plugin scrapers
//  - RuntimeDiagnostics stream events

import co.touchlab.kermit.Logger
import com.nuvio.app.core.diagnostics.DiagnosticEvent
import com.nuvio.app.core.diagnostics.RuntimeDiagnostics
import com.nuvio.app.features.addons.AddonManifest
import com.nuvio.app.features.cloudstream.CloudStreamRepository
import com.nuvio.app.features.cloudstream.isCloudStreamDiscoveryAllowed
import com.nuvio.app.features.cloudstream.parseCloudStreamRouteId
import com.nuvio.app.features.plugins.PluginRuntimeResult
import com.nuvio.app.features.plugins.isExcludedByPluginQualityFilter
import com.nuvio.app.features.tmdb.TmdbSettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withTimeoutOrNull

private const val MEW_CLOUDSTREAM_PROVIDER_CONCURRENCY = 8
private const val MEW_CLOUDSTREAM_PROVIDER_TIMEOUT_MS = 25_000L
private const val MEW_PLUGIN_SCRAPER_CONCURRENCY = 4
private const val MEW_PLUGIN_SCRAPER_TIMEOUT_MS = 65_000L

private val mewStreamLog = Logger.withTag("MewStreamSearch")
private val mewPluginScraperGate = Semaphore(MEW_PLUGIN_SCRAPER_CONCURRENCY)

internal class MewStreamSearchExtras(
    val cloudStreamRequest: CloudStreamSearchRequest? = null,
    val cloudStreamGroups: List<CloudStreamProviderGroup> = emptyList(),
    /** True when the requested id is an opaque `cloudstream:` route. */
    val isCloudStreamRoute: Boolean = false,
    val cloudStreamRegistryRevision: Long = 0L,
    val excludedPluginQualities: Set<String> = emptySet(),
) {
    val isEmpty: Boolean get() = cloudStreamGroups.isEmpty()

    val taskCount: Int get() = cloudStreamGroups.size

    /** Added to the upstream request/session keys so a changed CloudStream or quality setup re-searches. */
    val configurationKey: String
        get() = buildList {
            cloudStreamGroups.forEach { add("cloudstream:${it.addonId}") }
            if (cloudStreamGroups.isNotEmpty()) add("cloudstreamRev:$cloudStreamRegistryRevision")
            cloudStreamRequest?.let { add("cloudTarget:${it.cacheKey}") }
            if (excludedPluginQualities.isNotEmpty()) add("pluginQuality:${excludedPluginQualities.sorted().joinToString(",")}")
        }.joinToString("|")

    fun loadingGroups(): List<AddonStreamGroup> = cloudStreamGroups.map { group ->
        AddonStreamGroup(
            addonName = group.addonName,
            addonId = group.addonId,
            streams = emptyList(),
            isLoading = true,
        )
    }

    /**
     * Addon endpoints need an external id. For a CloudStream route use the resolved external id
     * (or skip addons when none is known) instead of sending the opaque route to unrelated addons.
     */
    fun addonLookupId(videoId: String): String? = if (isCloudStreamRoute) {
        cloudStreamRequest?.externalId
    } else {
        videoId
    }

    fun supportsAddon(manifest: AddonManifest, type: String, videoId: String): Boolean {
        val lookupId = addonLookupId(videoId) ?: return false
        return manifest.supportsStream(type, lookupId)
    }

    fun filterPluginResults(results: List<PluginRuntimeResult>): List<PluginRuntimeResult> =
        if (excludedPluginQualities.isEmpty()) {
            results
        } else {
            results.filterNot { it.isExcludedByPluginQualityFilter(excludedPluginQualities) }
        }

    companion object {
        val None = MewStreamSearchExtras()
    }
}

internal fun mewStreamSearchExtras(
    type: String,
    videoId: String,
    parentMetaId: String?,
    parentMetaType: String?,
    season: Int?,
    episode: Int?,
    searchTitle: String?,
    groupByRepository: Boolean,
    excludedPluginQualities: Set<String> = emptySet(),
): MewStreamSearchExtras {
    TmdbSettingsRepository.ensureLoaded()
    val tmdbEnabled = TmdbSettingsRepository.snapshot().enabled
    val route = parseCloudStreamRouteId(videoId)
    val request = runCatching {
        buildCloudStreamSearchRequest(
            type = type,
            videoId = videoId,
            parentMetaId = parentMetaId,
            parentMetaType = parentMetaType,
            season = season,
            episode = episode,
            searchTitle = searchTitle,
        )
    }.getOrNull()
    val groups = runCatching {
        cloudStreamProviderGroupsForRequest(
            type = type,
            request = request,
            directRoute = route,
            groupByRepository = groupByRepository,
        )
    }.onFailure { mewStreamLog.w(it) { "CloudStream provider lookup failed" } }
        .getOrDefault(emptyList())
    val revision = if (isCloudStreamDiscoveryAllowed(tmdbEnabled)) {
        CloudStreamRepository.uiState.value.registryRevision
    } else {
        0L
    }
    return MewStreamSearchExtras(
        cloudStreamRequest = request,
        cloudStreamGroups = groups,
        isCloudStreamRoute = route != null,
        cloudStreamRegistryRevision = revision,
        excludedPluginQualities = excludedPluginQualities,
    )
}

/** Launches the Mew-only search tasks next to the upstream addon/server/plugin tasks. */
internal fun CoroutineScope.launchMewStreamSearchTasks(
    extras: MewStreamSearchExtras,
    onCompletion: (StreamLoadCompletion) -> Unit,
) {
    if (extras.cloudStreamGroups.isEmpty()) return
    val gate = Semaphore(MEW_CLOUDSTREAM_PROVIDER_CONCURRENCY)
    extras.cloudStreamGroups.forEach { providerGroup ->
        launch {
            val group = gate.withPermit {
                withTimeoutOrNull(MEW_CLOUDSTREAM_PROVIDER_TIMEOUT_MS) {
                    runCatchingUnlessCancelled {
                        resolveCloudStreamProviderStreams(providerGroup, extras.cloudStreamRequest)
                    }.getOrElse { error ->
                        mewStreamLog.w(error) { "CloudStream provider failed: ${providerGroup.addonName}" }
                        AddonStreamGroup(
                            addonName = providerGroup.addonName,
                            addonId = providerGroup.addonId,
                            streams = emptyList(),
                            isLoading = false,
                            error = error.message,
                        )
                    }
                }
            } ?: AddonStreamGroup(
                addonName = providerGroup.addonName,
                addonId = providerGroup.addonId,
                streams = emptyList(),
                isLoading = false,
                error = "${providerGroup.addonName} timed out",
            )
            onCompletion(StreamLoadCompletion.Addon(group))
        }
    }
}

/** Bounds plugin scraper concurrency and run time so one slow scraper cannot keep the search spinning. */
internal suspend fun <T> mewPluginScraperGate(
    scraperName: String,
    block: suspend () -> Result<T>,
): Result<T> = mewPluginScraperGate.withPermit {
    withTimeoutOrNull(MEW_PLUGIN_SCRAPER_TIMEOUT_MS) { block() }
        ?: Result.failure(Throwable("$scraperName timed out"))
}

internal fun mewRecordStreamLoadStarted() {
    RuntimeDiagnostics.record(DiagnosticEvent.StreamLoadStarted)
    RuntimeDiagnostics.updateStreams(groups = 0, loadingGroups = 0, providerTasks = 0)
}

internal fun mewRecordStreamLoadProgress(state: StreamsUiState, providerTasks: Int) {
    RuntimeDiagnostics.updateStreams(
        groups = state.groups.size,
        loadingGroups = state.groups.count { it.isLoading },
        providerTasks = providerTasks,
    )
}

internal fun mewRecordStreamLoadFinished() {
    RuntimeDiagnostics.record(DiagnosticEvent.StreamLoadFinished)
}
