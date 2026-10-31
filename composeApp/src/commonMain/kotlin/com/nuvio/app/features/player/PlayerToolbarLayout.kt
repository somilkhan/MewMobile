package com.nuvio.app.features.player

enum class PlayerToolbarButton(val storageId: String) {
    RESIZE("resize"),
    SPEED("speed"),
    SUBTITLES("subtitles"),
    SUBTITLE_SYNC("subtitle_sync"),
    AUDIO("audio"),
    QUALITY("quality"),
    CHANNELS("channels"),
    SOURCES("sources"),
    EPISODES("episodes"),
    NEXT_EPISODE("next_episode");

    companion object {
        fun fromStorageId(value: String): PlayerToolbarButton? =
            entries.firstOrNull { it.storageId == value }
    }
}

data class PlayerToolbarEntry(
    val button: PlayerToolbarButton,
    val visible: Boolean,
)

data class PlayerToolbarLayout(val entries: List<PlayerToolbarEntry>) {
    val visibleButtons: List<PlayerToolbarButton>
        get() = entries.filter(PlayerToolbarEntry::visible).map(PlayerToolbarEntry::button)

    fun withVisibility(button: PlayerToolbarButton, visible: Boolean): PlayerToolbarLayout =
        PlayerToolbarLayout(entries.map { if (it.button == button) it.copy(visible = visible) else it })

    fun moved(fromIndex: Int, toIndex: Int): PlayerToolbarLayout {
        if (fromIndex !in entries.indices || toIndex !in entries.indices || fromIndex == toIndex) return this
        val updated = entries.toMutableList()
        updated.add(toIndex, updated.removeAt(fromIndex))
        return PlayerToolbarLayout(updated)
    }

    fun encode(): String = entries.joinToString(",") { entry ->
        "${entry.button.storageId}:${if (entry.visible) "v" else "h"}"
    }

    companion object {
        // Preserve Mew's current action-pill order and visibility as the migration default.
        val Default = PlayerToolbarLayout(PlayerToolbarButton.entries.map { PlayerToolbarEntry(it, true) })

        fun decode(value: String?): PlayerToolbarLayout {
            if (value.isNullOrBlank()) return Default
            val parsed = value.split(',').mapNotNull { token ->
                val button = PlayerToolbarButton.fromStorageId(token.substringBefore(':').trim())
                    ?: return@mapNotNull null
                PlayerToolbarEntry(button, token.substringAfter(':', "v").trim() != "h")
            }.distinctBy(PlayerToolbarEntry::button)
            val missing = Default.entries.filter { default -> parsed.none { it.button == default.button } }
            return PlayerToolbarLayout(parsed + missing)
        }
    }
}
