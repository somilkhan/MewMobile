package com.nuvio.app.features.streams

/**
 * Builds a target filename string by replacing episode/season numbers in the
 * current filename with the next episode's numbers. Used for precise stream
 * matching when binge group alone is ambiguous.
 */
object FilenameEpisodeMatcher {

    fun buildNextFilename(
        filename: String,
        currentSeason: Int?,
        currentEpisode: Int,
        nextSeason: Int?,
        nextEpisode: Int
    ): String? {
        if (filename.isBlank()) return null
        for (pattern in patterns) {
            val result = pattern.replace(filename, currentSeason, currentEpisode, nextSeason, nextEpisode)
            if (result != null) return result
        }
        return null
    }

    fun matches(candidateFilename: String?, expectedFilename: String?): Boolean {
        if (candidateFilename.isNullOrBlank() || expectedFilename.isNullOrBlank()) return false
        return candidateFilename.equals(expectedFilename, ignoreCase = true)
    }

    fun matchesEpisode(candidateFilename: String?, expectedFilename: String?): Boolean {
        if (candidateFilename.isNullOrBlank() || expectedFilename.isNullOrBlank()) return false
        val candidate = extractEpisodeIdentity(candidateFilename) ?: return false
        val expected = extractEpisodeIdentity(expectedFilename) ?: return false
        // A seasonless EP## marker is not interchangeable with SxxE##: treating an
        // unknown season as a wildcard can select the wrong season during autoplay.
        return candidate.season == expected.season &&
            candidate.firstEpisode <= expected.firstEpisode &&
            candidate.lastEpisode >= expected.lastEpisode
    }

    fun extractSeasonEpisode(filename: String): Pair<Int?, Int>? {
        if (filename.isBlank()) return null
        Regex("""(?i)S\s*(\d{1,3})\s*E\s*(\d{1,4})""").find(filename)?.let { match ->
            val season = match.groupValues[1].toIntOrNull()
            val episode = match.groupValues[2].toIntOrNull() ?: return null
            return season to episode
        }
        Regex("""(?<!\d)(\d{1,2})[xX](\d{1,4})""").find(filename)?.let { match ->
            val season = match.groupValues[1].toIntOrNull()
            val episode = match.groupValues[2].toIntOrNull() ?: return null
            return season to episode
        }
        Regex("""(?i)\bE[Pp]?[.\s]?(\d{1,4})\b""").find(filename)?.let { match ->
            val episode = match.groupValues[1].toIntOrNull() ?: return null
            return null to episode
        }
        return null
    }

    private data class EpisodeIdentity(
        val season: Int?,
        val firstEpisode: Int,
        val lastEpisode: Int,
    )

    private fun extractEpisodeIdentity(filename: String): EpisodeIdentity? {
        if (filename.isBlank()) return null
        val seasonEpisode = Regex(
            """(?i)S\s*(\d{1,3})\s*E\s*(\d{1,4})(?:(?:\s*-\s*E?|\s*E)\s*(\d{1,4}))?""",
        ).find(filename)
        if (seasonEpisode != null) {
            val season = seasonEpisode.groupValues[1].toIntOrNull() ?: return null
            val first = seasonEpisode.groupValues[2].toIntOrNull() ?: return null
            val last = seasonEpisode.groupValues[3].takeIf(String::isNotBlank)?.toIntOrNull() ?: first
            if (last < first) return null
            return EpisodeIdentity(season, first, last)
        }
        val episodeOnly = Regex("""(?i)\bE[Pp]?[.\s]?(\d{1,4})\b""").find(filename)
            ?: return null
        val episode = episodeOnly.groupValues[1].toIntOrNull() ?: return null
        return EpisodeIdentity(null, episode, episode)
    }

    private interface EpisodePattern {
        fun replace(filename: String, currentSeason: Int?, currentEpisode: Int, nextSeason: Int?, nextEpisode: Int): String?
    }

    private val sXeY = object : EpisodePattern {
        private val regex = Regex("""(?i)(S)\s*(\d{1,3})\s*(E)\s*(\d{1,4})""")
        override fun replace(filename: String, currentSeason: Int?, currentEpisode: Int, nextSeason: Int?, nextEpisode: Int): String? {
            if (currentSeason == null) return null
            val match = regex.find(filename) ?: return null
            if (match.groupValues[2].toIntOrNull() != currentSeason || match.groupValues[4].toIntOrNull() != currentEpisode) return null
            val seasonWidth = match.groupValues[2].length
            val episodeWidth = match.groupValues[4].length
            val replacement = "${match.groupValues[1]}${(nextSeason ?: currentSeason).toString().padStart(seasonWidth, '0')}${match.groupValues[3]}${nextEpisode.toString().padStart(episodeWidth, '0')}"
            return filename.replaceRange(match.range, replacement)
        }
    }

    // 01x03, 1x03 (negative lookbehind for digit avoids matching 1920x1080)
    private val seasonXepisode = object : EpisodePattern {
        private val regex = Regex("""(?<!\d)(\d{1,2})[xX](\d{1,4})""")
        override fun replace(filename: String, currentSeason: Int?, currentEpisode: Int, nextSeason: Int?, nextEpisode: Int): String? {
            if (currentSeason == null) return null
            val match = regex.find(filename) ?: return null
            if (match.groupValues[1].toIntOrNull() != currentSeason || match.groupValues[2].toIntOrNull() != currentEpisode) return null
            val seasonWidth = match.groupValues[1].length
            val episodeWidth = match.groupValues[2].length
            val sep = filename[match.range.first + match.groupValues[1].length]
            val replacement = "${(nextSeason ?: currentSeason).toString().padStart(seasonWidth, '0')}${sep}${nextEpisode.toString().padStart(episodeWidth, '0')}"
            return filename.replaceRange(match.range, replacement)
        }
    }

    private val episodeOnly = object : EpisodePattern {
        private val regex = Regex("""(?i)\b(E[Pp]?)[.\s]?(\d{1,4})\b""")
        override fun replace(filename: String, currentSeason: Int?, currentEpisode: Int, nextSeason: Int?, nextEpisode: Int): String? {
            val match = regex.find(filename) ?: return null
            if (match.groupValues[2].toIntOrNull() != currentEpisode) return null
            val episodeWidth = match.groupValues[2].length
            val prefix = match.groupValues[1]
            val separator = filename.substring(
                match.range.first + prefix.length,
                match.range.first + prefix.length + (match.value.length - prefix.length - match.groupValues[2].length)
            )
            val replacement = "$prefix$separator${nextEpisode.toString().padStart(episodeWidth, '0')}"
            return filename.replaceRange(match.range, replacement)
        }
    }

    private val patterns = listOf(sXeY, seasonXepisode, episodeOnly)
}
