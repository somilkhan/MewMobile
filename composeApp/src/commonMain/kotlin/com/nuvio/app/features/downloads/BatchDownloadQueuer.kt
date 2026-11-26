package com.nuvio.app.features.downloads

import co.touchlab.kermit.Logger
import com.nuvio.app.features.details.MetaDetails
import com.nuvio.app.features.details.MetaVideo
import com.nuvio.app.features.profiles.ProfileRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** One planned episode (or a movie) with the source chosen for it. */
internal data class BatchQueueItem(
    val season: Int,
    val episode: MetaVideo,
    val option: DownloadSourceOption,
)

/**
 * Turns the sources chosen in the batch download dialog into downloads, one episode at a time,
 * through the same enqueue path as the episode download sheet.
 */
internal object BatchDownloadQueuer {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    /** Parent of every running queue job; replaced when queued work must stop. */
    private var queueJob = SupervisorJob()

    fun queue(meta: MetaDetails, items: List<BatchQueueItem>) {
        val log = Logger.withTag("Downloads/Batch")
        val startingProfileId = ProfileRepository.activeProfileId
        scope.launch(queueJob) {
            for (item in items) {
                if (ProfileRepository.activeProfileId != startingProfileId) break
                val target = meta.episodeDownloadTargetFor(item.episode, item.season)
                val result = try {
                    EpisodeDownloadCoordinator.enqueue(
                        contentType = meta.type,
                        parentMetaId = meta.id,
                        parentMetaType = meta.type,
                        title = meta.name,
                        logo = meta.logo,
                        poster = meta.poster,
                        background = meta.background,
                        targets = listOf(target),
                        selectedOption = item.option,
                        queueMode = DownloadQueueMode.OneAtATime,
                    )
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Throwable) {
                    log.w(error) { "enqueue failed for S${target.seasonNumber}E${target.episodeNumber}" }
                    null
                }
                if (result == null || result.started + result.replaced + result.awaitingSource == 0) {
                    log.d { "enqueue returned nothing for S${target.seasonNumber}E${target.episodeNumber}" }
                }
            }
        }
    }

    /** Stops queue work that hasn't finished, e.g. when the profile changes. */
    fun clearLocalState() {
        queueJob.cancel()
        queueJob = SupervisorJob()
    }
}
