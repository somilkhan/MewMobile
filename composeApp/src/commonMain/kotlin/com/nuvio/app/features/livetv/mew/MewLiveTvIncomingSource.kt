package com.nuvio.app.features.livetv

// Mew-only: links, M3U files and magnets shared into the app (Android share/view intents).
// Lives outside upstream Live TV files so luq Live TV updates merge cleanly.

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

sealed interface LiveTvIncomingSource {
    data class SourceUrl(val url: String) : LiveTvIncomingSource

    data class PlaylistData(
        val fileName: String,
        val data: String,
    ) : LiveTvIncomingSource

    data class DirectStream(
        val url: String,
        val title: String = "Shared stream",
        val headers: Map<String, String> = emptyMap(),
    ) : LiveTvIncomingSource

    data class Magnet(
        val magnetUri: String,
        val infoHash: String,
        val trackers: List<String> = emptyList(),
    ) : LiveTvIncomingSource
}

object LiveTvIncomingSourceRepository {
    private val requestChannel = Channel<LiveTvIncomingSource>(capacity = Channel.BUFFERED)
    val requests: Flow<LiveTvIncomingSource> = requestChannel.receiveAsFlow()

    fun submitText(text: String) {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return
        if (trimmed.startsWith("#EXTM3U", ignoreCase = true)) {
            requestChannel.trySend(LiveTvIncomingSource.PlaylistData(fileName = "Shared M3U playlist", data = trimmed))
            return
        }
        val url = firstSharedUrl(trimmed) ?: return
        when {
            url.startsWith("magnet:", ignoreCase = true) -> parseMagnet(url)?.let(requestChannel::trySend)
            url.looksLikePlaylistUrl() -> requestChannel.trySend(LiveTvIncomingSource.SourceUrl(url))
            url.startsWith("http://", ignoreCase = true) || url.startsWith("https://", ignoreCase = true) -> {
                requestChannel.trySend(
                    LiveTvIncomingSource.DirectStream(
                        url = url,
                        title = url.substringBefore('?').substringAfterLast('/').ifBlank { "Shared stream" },
                    ),
                )
            }
        }
    }

    fun submitPlaylistData(fileName: String, data: String) {
        val trimmed = data.trim()
        if (trimmed.isBlank()) return
        requestChannel.trySend(
            LiveTvIncomingSource.PlaylistData(
                fileName = fileName.trim().ifBlank { "Shared M3U playlist" },
                data = trimmed,
            ),
        )
    }
}

private val sharedUrlRegex = Regex("""(?i)(magnet:\?xt=urn:btih:[^\s"'<>]+|https?://[^\s"'<>]+)""")

private fun firstSharedUrl(value: String): String? =
    sharedUrlRegex.find(value)?.value?.trim()?.trimEnd(',', '.', ')', ']')

private fun String.looksLikePlaylistUrl(): Boolean {
    val normalized = substringBefore('#').substringBefore('?').lowercase()
    return normalized.endsWith(".m3u") || normalized.endsWith(".m3u8")
}

private fun parseMagnet(url: String): LiveTvIncomingSource.Magnet? {
    val infoHash = Regex("""(?i)(?:xt=urn:btih:)([a-f0-9]{32,40})""")
        .find(url)
        ?.groupValues
        ?.getOrNull(1)
        ?.lowercase()
        ?: return null
    val trackers = Regex("""(?:^|[?&])tr=([^&]+)""")
        .findAll(url)
        .map { it.groupValues[1].replace("+", "%20") }
        .toList()
    return LiveTvIncomingSource.Magnet(
        magnetUri = url,
        infoHash = infoHash,
        trackers = trackers,
    )
}
