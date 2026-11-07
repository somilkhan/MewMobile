package com.nuvio.app.features.player

internal const val SUBTITLE_FONT_IMPORT_MAX_BYTES: Long = 30L * 1024L * 1024L

internal expect object SubtitleFontFileBridge {
    fun importFont(onResult: (Result<SubtitleFontImportResult>) -> Unit)
}
