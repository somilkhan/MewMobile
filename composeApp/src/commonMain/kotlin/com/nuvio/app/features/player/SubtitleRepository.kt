package com.nuvio.app.features.player

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.compose_player_no_subtitles_found
import org.jetbrains.compose.resources.getString

object SubtitleRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _addonSubtitles = MutableStateFlow<List<AddonSubtitle>>(emptyList())
    val addonSubtitles: StateFlow<List<AddonSubtitle>> = _addonSubtitles.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _loadingProgress = MutableStateFlow<SubtitleLoadingProgress?>(null)
    internal val loadingProgress: StateFlow<SubtitleLoadingProgress?> = _loadingProgress.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var activeFetchJob: Job? = null
    private var fetchGeneration = 0L

    fun fetchAddonSubtitles(
        type: String,
        videoId: String,
        extras: SubtitleRequestExtras? = null,
    ) {
        activeFetchJob?.cancel()
        val generation = ++fetchGeneration
        _loadingProgress.value = null
        activeFetchJob = scope.launch {
            executeSubtitleFetch(type, videoId, extras, generation)
        }
    }

    /**
     * Like [fetchAddonSubtitles], but first computes the OpenSubtitles video hash
     * when the stream extras do not already contain one.
     */
    fun fetchAddonSubtitlesWithHashComputation(
        type: String,
        videoId: String,
        extras: SubtitleRequestExtras?,
        sourceUrl: String,
        sourceHeaders: Map<String, String>,
        onExtrasUpdated: (SubtitleRequestExtras) -> Unit,
    ) {
        activeFetchJob?.cancel()
        val generation = ++fetchGeneration
        _loadingProgress.value = null
        activeFetchJob = scope.launch {
            _isLoading.value = true
            _error.value = null
            _addonSubtitles.value = emptyList()

            val enrichedExtras = try {
                val hashResult = computeVideoHash(sourceUrl, sourceHeaders)
                if (hashResult != null) {
                    val (hash, fileSize) = hashResult
                    val updated = (extras ?: SubtitleRequestExtras()).copy(
                        videoHash = hash,
                        videoSize = extras?.videoSize ?: fileSize,
                    )
                    if (generation == fetchGeneration) onExtrasUpdated(updated)
                    updated
                } else extras
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                extras
            }

            if (generation != fetchGeneration) return@launch
            executeSubtitleLoad(type, videoId, enrichedExtras, generation)
        }
    }

    fun clear() {
        activeFetchJob?.cancel()
        fetchGeneration += 1L
        _loadingProgress.value = null
        _addonSubtitles.value = emptyList()
        _isLoading.value = false
        _error.value = null
    }

    private suspend fun executeSubtitleFetch(
        type: String,
        videoId: String,
        extras: SubtitleRequestExtras?,
        generation: Long,
    ) {
        if (generation != fetchGeneration) return
        _isLoading.value = true
        _error.value = null
        _addonSubtitles.value = emptyList()
        executeSubtitleLoad(type, videoId, extras, generation)
    }

    private suspend fun executeSubtitleLoad(
        type: String,
        videoId: String,
        extras: SubtitleRequestExtras?,
        generation: Long,
    ) {
        val requests = addonSubtitleRequests(type, videoId, extras)
        if (requests.isEmpty()) {
            _isLoading.value = false
            return
        }

        _loadingProgress.value = SubtitleLoadingProgress(total = requests.size)
        // Mew: keep add-on order stable while results arrive, and drop stale generations.
        val completedBlocks = arrayOfNulls<List<AddonSubtitle>>(requests.size)
        loadAddonSubtitles(requests) { request, subtitles ->
            if (generation != fetchGeneration) return@loadAddonSubtitles
            val index = requests.indexOf(request)
            if (index >= 0) completedBlocks[index] = subtitles
            _addonSubtitles.value = aggregateOrderedSubtitleBlocks(
                blocks = completedBlocks.toList(),
                generation = generation,
                activeGeneration = fetchGeneration,
            ) ?: return@loadAddonSubtitles
            _loadingProgress.update { progress ->
                progress?.copy(completed = progress.completed + 1, addonName = request.addonName)
            }
        }

        if (generation != fetchGeneration) return
        if (_addonSubtitles.value.isEmpty()) {
            _error.value = getString(Res.string.compose_player_no_subtitles_found)
        }
        _isLoading.value = false
    }
}

internal data class SubtitleLoadingProgress(
    val total: Int,
    val completed: Int = 0,
    val addonName: String? = null,
)

internal fun <T> aggregateOrderedSubtitleBlocks(
    blocks: List<List<T>?>,
    generation: Long,
    activeGeneration: Long,
): List<T>? {
    if (generation != activeGeneration) return null
    return blocks.filterNotNull().flatten()
}

/**
 * Add-on subtitle URLs are untrusted input and are forwarded to external players.
 * Accept only absolute HTTP(S) URLs; file/content/data schemes could expose local data.
 */
internal fun isSupportedAddonSubtitleUrl(url: String): Boolean {
    if (url != url.trim()) return false
    val schemeSeparator = url.indexOf("://")
    if (schemeSeparator <= 0) return false
    return when (url.substring(0, schemeSeparator).lowercase()) {
        "http", "https" -> true
        else -> false
    }
}
