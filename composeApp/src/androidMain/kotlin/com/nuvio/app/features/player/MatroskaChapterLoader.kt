package com.nuvio.app.features.player

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * Reads the chapters of a remote Matroska file with HTTP range requests, for the ExoPlayer path
 * (ExoPlayer's Matroska extractor skips chapters). Only a few small byte ranges are fetched.
 */
internal object MatroskaChapterLoader {
    private const val HEAD_BYTES = 256 * 1024
    private const val SEEK_HEAD_BYTES = 64 * 1024
    private const val CHAPTERS_BYTES = 512 * 1024
    private const val CONNECT_TIMEOUT_MS = 8_000
    private const val READ_TIMEOUT_MS = 10_000

    /** Chapters of the file at [url], or empty when it is not Matroska, has none, or cannot be read. */
    fun load(url: String, headers: Map<String, String>): List<PlayerChapter> = runCatching {
        if (!url.startsWith("http://") && !url.startsWith("https://")) return@runCatching emptyList()
        val head = readRange(url, headers, offset = 0L, length = HEAD_BYTES)
            ?: return@runCatching emptyList()
        val layout = MatroskaChapterParser.topLevelLayout(head)
            ?: return@runCatching emptyList()
        val chaptersOffset = layout.chaptersPosition
            ?: layout.seekHeadPosition?.let { seekHeadOffset ->
                val seekHead = readRange(url, headers, seekHeadOffset, SEEK_HEAD_BYTES)
                    ?: return@runCatching emptyList()
                MatroskaChapterParser.seekHeadPositions(seekHead)[MatroskaChapterParser.ID_CHAPTERS]
                    ?.let { layout.segmentDataStart + it }
            }
            ?: return@runCatching emptyList()
        val chapters = readRange(url, headers, chaptersOffset, CHAPTERS_BYTES)
            ?: return@runCatching emptyList()
        PlayerChapters.normalize(MatroskaChapterParser.parseChapters(chapters))
    }.getOrDefault(emptyList())

    /** Bytes [offset, offset + length) of [url], or null when the server does not answer with them. */
    private fun readRange(url: String, headers: Map<String, String>, offset: Long, length: Int): ByteArray? {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
            instanceFollowRedirects = true
            requestMethod = "GET"
            headers.forEach { (name, value) -> setRequestProperty(name, value) }
            setRequestProperty("Range", "bytes=$offset-${offset + length - 1}")
        }
        try {
            val code = connection.responseCode
            if (code != HttpURLConnection.HTTP_PARTIAL) return null
            return connection.inputStream.use { input -> input.readUpTo(length) }
        } catch (error: IOException) {
            return null
        } finally {
            connection.disconnect()
        }
    }

    private fun java.io.InputStream.readUpTo(limit: Int): ByteArray {
        val output = java.io.ByteArrayOutputStream(minOf(limit, 64 * 1024))
        val buffer = ByteArray(8 * 1024)
        while (output.size() < limit) {
            val read = read(buffer, 0, minOf(buffer.size, limit - output.size()))
            if (read < 0) break
            output.write(buffer, 0, read)
        }
        return output.toByteArray()
    }
}
