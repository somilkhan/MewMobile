package com.nuvio.app.features.downloads

import com.nuvio.app.features.streams.StreamSubtitle

/**
 * Mew hook: luq player code asks `DownloadSubtitles.localSubtitles(uri)` for subtitles saved
 * next to a downloaded file. Mew downloads keep the source's subtitle list on the item itself,
 * so this resolves them from [DownloadsRepository] instead of luq's sidecar files.
 */
internal object DownloadSubtitles {
    fun localSubtitles(localVideoUri: String): List<StreamSubtitle> {
        if (localVideoUri.isBlank()) return emptyList()
        return DownloadsRepository.uiState.value.items
            .firstOrNull { it.localFileUri == localVideoUri }
            ?.externalSubtitles
            .orEmpty()
    }

    /** Mew stores subtitle URLs on the item; nothing to bundle beside the file (iOS downloader hook). */
    @Suppress("UNUSED_PARAMETER")
    suspend fun prepare(item: DownloadItem, localVideoUri: String) = Unit
}
