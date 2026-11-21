package com.nuvio.app.features.player

import com.nuvio.app.features.addons.AddonResource
import com.nuvio.app.features.addons.ManagedAddon
import com.nuvio.app.features.addons.enabledAddons

internal fun buildAddonSubtitleFetchKey(
    addons: List<ManagedAddon>,
    type: String?,
    videoId: String?,
): String? {
    val normalizedType = type?.takeIf { it.isNotBlank() } ?: return null
    val normalizedVideoId = videoId?.takeIf { it.isNotBlank() } ?: return null
    val compatibleSubtitleAddons = addons.enabledAddons().mapNotNull { addon ->
        val manifest = addon.manifest ?: return@mapNotNull null
        val supportsSubtitles = manifest.resources.any { resource ->
            resource.isCompatibleSubtitleResource(
                type = normalizedType,
                videoId = normalizedVideoId,
            )
        }
        if (!supportsSubtitles) return@mapNotNull null
        "${manifest.id}:${manifest.transportUrl}"
    }

    if (compatibleSubtitleAddons.isEmpty()) return null
    return buildString {
        append(normalizedType)
        append('|')
        append(normalizedVideoId)
        append('|')
        append(compatibleSubtitleAddons.sorted().joinToString("|"))
    }
}

internal fun AddonResource.isCompatibleSubtitleResource(type: String, videoId: String): Boolean {
    val isSubtitleResource = name.equals("subtitles", ignoreCase = true) ||
        name.equals("subtitle", ignoreCase = true)
    if (!isSubtitleResource) return false

    val requestType = if (type.equals("tv", ignoreCase = true)) "series" else type
    val typeMatches = types.isEmpty() || types.any { it.equals(requestType, ignoreCase = true) }
    if (!typeMatches) return false

    return idPrefixes.isEmpty() || idPrefixes.any { prefix -> videoId.startsWith(prefix) }
}

internal enum class SubtitleAutoSelectionMode {
    FORCED_ONLY,
    NORMAL_ONLY,
}

internal data class SubtitleAutoSelectionPlan(
    val targets: List<String>,
    val mode: SubtitleAutoSelectionMode,
)

internal fun resolveAudioTrackLanguageTarget(track: AudioTrack?): String? {
    if (track == null) return null

    val directLanguage = normalizeLanguageCode(track.language)
        ?.takeUnless { it == "und" || it == "unknown" }
    if (directLanguage != null) return directLanguage

    val selectableLanguages = AvailableLanguageOptions
        .mapNotNull { option -> normalizeLanguageCode(option.code) }
        .toSet()
    return listOf(track.label, track.id).firstNotNullOfOrNull { value ->
        normalizeLanguageCode(value)?.takeIf(selectableLanguages::contains)
    }
}

internal fun resolveSubtitleAutoSelectionPlan(
    selectedAudioTrack: AudioTrack?,
    preferredAudioTargets: List<String>,
    preferredSubtitleTargets: List<String>,
    useForcedSubtitles: Boolean,
): SubtitleAutoSelectionPlan? {
    if (useForcedSubtitles && selectedAudioTrack == null) return null

    val subtitleTargets = preferredSubtitleTargets
        .map { target -> SubtitleLanguageMatching.normalizeLanguageCode(target) }
        .filter { target ->
            target.isNotBlank() &&
                target != SubtitleLanguageOption.NONE &&
                target != SubtitleLanguageOption.FORCED &&
                target != AudioLanguageOption.DEFAULT
        }
        .distinct()
    val primarySubtitleTarget = subtitleTargets.firstOrNull()
    val forcedTarget = when {
        !useForcedSubtitles -> null
        primarySubtitleTarget != null &&
            selectedAudioTrack != null &&
            audioMatchesSubtitleTargetForForced(selectedAudioTrack, primarySubtitleTarget) ->
            primarySubtitleTarget
        primarySubtitleTarget == null &&
            selectedAudioTrack != null &&
            preferredAudioTargets.any { target ->
                audioTrackMatchesLanguage(selectedAudioTrack, target)
            } -> selectedAudioLanguageTarget(selectedAudioTrack)
        else -> null
    }

    return SubtitleAutoSelectionPlan(
        targets = forcedTarget?.let(::listOf) ?: subtitleTargets,
        mode = if (forcedTarget != null) {
            SubtitleAutoSelectionMode.FORCED_ONLY
        } else {
            SubtitleAutoSelectionMode.NORMAL_ONLY
        },
    )
}

internal fun audioMatchesSubtitleTargetForForced(
    audioTrack: AudioTrack,
    target: String,
): Boolean {
    if (audioTrackMatchesLanguage(audioTrack, target)) return true

    val normalizedTarget = SubtitleLanguageMatching.normalizeLanguageCode(target)
    val baseTarget = normalizedTarget.substringBefore('-')
    if (baseTarget == normalizedTarget) return false

    val audioVariant = SubtitleLanguageMatching.detectTrackLanguageVariant(
        language = audioTrack.language,
        name = audioTrack.label,
        trackId = audioTrack.id,
    )
    return audioVariant == baseTarget || audioVariant == normalizedTarget
}

internal fun findPreferredSubtitleTrackIndex(
    tracks: List<SubtitleTrack>,
    targets: List<String>,
    mode: SubtitleAutoSelectionMode,
    selectedAudioTrack: AudioTrack? = null,
): Int = findBestInternalSubtitleTrackIndex(
    tracks = tracks,
    targets = targets,
    forcedOnly = mode == SubtitleAutoSelectionMode.FORCED_ONLY,
    normalOnly = mode == SubtitleAutoSelectionMode.NORMAL_ONLY,
    selectedAudioTrack = selectedAudioTrack,
)

internal fun findBestInternalSubtitleTrackIndex(
    tracks: List<SubtitleTrack>,
    targets: List<String>,
    forcedOnly: Boolean = false,
    normalOnly: Boolean = false,
    selectedAudioTrack: AudioTrack? = null,
): Int {
    for ((targetPosition, target) in targets.withIndex()) {
        if (forcedOnly) {
            val forcedIndex = findBestForcedSubtitleTrackIndex(tracks, target, selectedAudioTrack)
            if (forcedIndex >= 0) return forcedIndex
            if (targetPosition == 0) return -1
            continue
        }

        val normalizedTarget = SubtitleLanguageMatching.normalizeLanguageCode(target)
        val candidateIndexes = tracks.indices.filter { index ->
            val track = tracks[index]
            val matchesTarget = if (normalizedTarget == "pt-br" || normalizedTarget == "es-419") {
                SubtitleLanguageMatching.detectTrackLanguageVariant(
                    language = track.language,
                    name = track.label,
                    trackId = track.id,
                ) == normalizedTarget
            } else {
                subtitleTrackMatchesLanguage(track, target)
            }
            (!normalOnly || !track.isForced) && matchesTarget
        }
        if (candidateIndexes.isEmpty()) {
            if (normalizedTarget == "pt-br") {
                val brazilian = findBrazilianPortugueseInGenericPtTracks(tracks, normalOnly)
                if (brazilian >= 0) return brazilian
                if (targetPosition == 0) return -1
            }
            if (normalizedTarget == "es-419") {
                val latino = findLatinoSpanishInGenericEsTracks(tracks, normalOnly)
                if (latino >= 0) return latino
                if (targetPosition == 0) return -1
            }
            continue
        }

        val preferredCandidates = candidateIndexes.filter { index -> !tracks[index].isForced }
            .takeIf { it.isNotEmpty() }
            ?: if (normalOnly) continue else candidateIndexes
        if (preferredCandidates.size == 1) {
            if (normalizedTarget == "pt" || normalizedTarget == "es") {
                val track = tracks[preferredCandidates.first()]
                val variant = SubtitleLanguageMatching.detectTrackLanguageVariant(
                    language = track.language,
                    name = track.label,
                    trackId = track.id,
                )
                if (variant != normalizedTarget && variant != track.language?.lowercase()) continue
            }
            return preferredCandidates.first()
        }
        if (normalizedTarget == "pt" || normalizedTarget == "pt-br") {
            val tieBroken = breakPortugueseSubtitleTie(tracks, preferredCandidates, normalizedTarget)
            if (tieBroken >= 0) return tieBroken
        }
        if (normalizedTarget == "es" || normalizedTarget == "es-419") {
            val tieBroken = breakSpanishSubtitleTie(tracks, preferredCandidates, normalizedTarget)
            if (tieBroken >= 0) return tieBroken
        }
        return preferredCandidates.first()
    }
    return -1
}

internal fun findBestForcedSubtitleTrackIndex(
    tracks: List<SubtitleTrack>,
    target: String,
    selectedAudioTrack: AudioTrack?,
): Int {
    val directMatch = tracks.indexOfFirst { track ->
        track.isForced && subtitleTrackMatchesLanguage(track, target) &&
            selectedAudioTrack != null && subtitleTrackMatchesSelectedAudioLanguage(track, selectedAudioTrack)
    }
    if (directMatch >= 0) return directMatch

    val normalizedTarget = SubtitleLanguageMatching.normalizeLanguageCode(target)
    if (normalizedTarget == "pt-br" || normalizedTarget == "es-419") {
        return tracks.indexOfFirst { track ->
            track.isForced && selectedAudioTrack != null &&
                subtitleTrackMatchesSelectedAudioLanguage(track, selectedAudioTrack) &&
                SubtitleLanguageMatching.detectTrackLanguageVariant(
                    language = track.language,
                    name = track.label,
                    trackId = track.id,
                ) == normalizedTarget
        }
    }
    return -1
}

internal fun subtitleTrackMatchesLanguage(track: SubtitleTrack, target: String): Boolean =
    SubtitleLanguageMatching.trackMatchesLanguage(track.label, track.language, track.id, target)

internal fun audioTrackMatchesLanguage(track: AudioTrack, target: String): Boolean =
    SubtitleLanguageMatching.trackMatchesLanguage(track.label, track.language, track.id, target)

internal fun selectedAudioLanguageTarget(track: AudioTrack): String? {
    track.language?.takeIf { it.isNotBlank() && !it.equals("und", ignoreCase = true) }?.let { return it }
    val haystack = listOf(track.label, track.id).joinToString(" ").lowercase()
    return AvailableLanguageOptions.firstOrNull { language ->
        val code = language.code.lowercase()
        val name = SubtitleLanguageMatching.languageCodeToName(language.code)
        SubtitleLanguageMatching.languageCodeAppearsInHaystack(haystack, code) ||
            (name.isNotBlank() && haystack.contains(name))
    }?.code
}

internal fun subtitleTrackMatchesSelectedAudioLanguage(
    track: SubtitleTrack,
    selectedAudioTrack: AudioTrack,
): Boolean {
    selectedAudioLanguageTarget(selectedAudioTrack)?.let { if (subtitleTrackMatchesLanguage(track, it)) return true }
    val subtitleLanguageName = track.language
        ?.takeIf { it.isNotBlank() && !it.equals("und", ignoreCase = true) }
        ?.let(SubtitleLanguageMatching::languageCodeToName)
    val audioHaystack = listOfNotNull(
        selectedAudioTrack.label,
        selectedAudioTrack.language,
        selectedAudioTrack.id,
    ).joinToString(" ").lowercase()
    return !subtitleLanguageName.isNullOrBlank() && audioHaystack.contains(subtitleLanguageName)
}

internal fun addonSubtitleIsForced(subtitle: AddonSubtitle): Boolean =
    listOfNotNull(subtitle.id, subtitle.url, subtitle.addonName)
        .any { value -> value.contains("forced", ignoreCase = true) }

internal fun addonSubtitleMatchesLanguage(subtitle: AddonSubtitle, target: String): Boolean {
    if (SubtitleLanguageMatching.matchesLanguageCode(subtitle.language, target)) return true
    val normalizedTarget = SubtitleLanguageMatching.normalizeLanguageCode(target)
    val targetName = SubtitleLanguageMatching.languageCodeToName(target)
    val haystack = listOfNotNull(subtitle.language, subtitle.id, subtitle.url, subtitle.addonName)
        .joinToString(" ").lowercase()
    return SubtitleLanguageMatching.languageCodeAppearsInHaystack(haystack, normalizedTarget) ||
        (targetName.isNotBlank() && haystack.contains(targetName))
}

internal fun addonSubtitleMatchesSelectedAudioLanguage(
    subtitle: AddonSubtitle,
    selectedAudioTrack: AudioTrack,
): Boolean {
    selectedAudioLanguageTarget(selectedAudioTrack)?.let { if (addonSubtitleMatchesLanguage(subtitle, it)) return true }
    val subtitleLanguageName = subtitle.language
        .takeIf { it.isNotBlank() && !it.equals("und", ignoreCase = true) }
        ?.let(SubtitleLanguageMatching::languageCodeToName)
    val audioHaystack = listOfNotNull(
        selectedAudioTrack.label,
        selectedAudioTrack.language,
        selectedAudioTrack.id,
    ).joinToString(" ").lowercase()
    return !subtitleLanguageName.isNullOrBlank() && audioHaystack.contains(subtitleLanguageName)
}

internal fun findBrazilianPortugueseInGenericPtTracks(
    tracks: List<SubtitleTrack>,
    normalOnly: Boolean = false,
): Int {
    val candidates = tracks.indices.filter { index ->
        (!normalOnly || !tracks[index].isForced) &&
            SubtitleLanguageMatching.normalizeLanguageCode(tracks[index].language.orEmpty()) == "pt"
    }
    return candidates.firstOrNull { index ->
        !tracks[index].isForced && subtitleHasAnyTag(tracks[index], SubtitleLanguageMatching.BRAZILIAN_TAGS) &&
            !subtitleHasAnyTag(tracks[index], SubtitleLanguageMatching.EUROPEAN_PT_TAGS)
    } ?: candidates.firstOrNull { index ->
        subtitleHasAnyTag(tracks[index], SubtitleLanguageMatching.BRAZILIAN_TAGS) &&
            !subtitleHasAnyTag(tracks[index], SubtitleLanguageMatching.EUROPEAN_PT_TAGS)
    } ?: candidates.firstOrNull { index ->
        subtitleHasAnyTag(tracks[index], SubtitleLanguageMatching.BRAZILIAN_TAGS)
    } ?: -1
}

internal fun findLatinoSpanishInGenericEsTracks(
    tracks: List<SubtitleTrack>,
    normalOnly: Boolean = false,
): Int {
    val candidates = tracks.indices.filter { index ->
        (!normalOnly || !tracks[index].isForced) &&
            SubtitleLanguageMatching.normalizeLanguageCode(tracks[index].language.orEmpty()) == "es"
    }
    return candidates.firstOrNull { index ->
        !tracks[index].isForced && subtitleHasAnyTag(tracks[index], SubtitleLanguageMatching.LATINO_TAGS) &&
            !subtitleHasAnyTag(tracks[index], SubtitleLanguageMatching.CASTILIAN_TAGS)
    } ?: candidates.firstOrNull { index ->
        subtitleHasAnyTag(tracks[index], SubtitleLanguageMatching.LATINO_TAGS) &&
            !subtitleHasAnyTag(tracks[index], SubtitleLanguageMatching.CASTILIAN_TAGS)
    } ?: candidates.firstOrNull { index ->
        subtitleHasAnyTag(tracks[index], SubtitleLanguageMatching.LATINO_TAGS)
    } ?: -1
}

internal fun breakPortugueseSubtitleTie(
    tracks: List<SubtitleTrack>,
    candidateIndexes: List<Int>,
    normalizedTarget: String,
): Int = if (normalizedTarget == "pt-br") {
    candidateIndexes.firstOrNull {
        subtitleHasAnyTag(tracks[it], SubtitleLanguageMatching.BRAZILIAN_TAGS) &&
            !subtitleHasAnyTag(tracks[it], SubtitleLanguageMatching.EUROPEAN_PT_TAGS)
    } ?: candidateIndexes.firstOrNull { subtitleHasAnyTag(tracks[it], SubtitleLanguageMatching.BRAZILIAN_TAGS) }
        ?: candidateIndexes.first()
} else {
    candidateIndexes.firstOrNull {
        subtitleHasAnyTag(tracks[it], SubtitleLanguageMatching.EUROPEAN_PT_TAGS) &&
            !subtitleHasAnyTag(tracks[it], SubtitleLanguageMatching.BRAZILIAN_TAGS)
    } ?: candidateIndexes.firstOrNull { subtitleHasAnyTag(tracks[it], SubtitleLanguageMatching.EUROPEAN_PT_TAGS) }
        ?: candidateIndexes.firstOrNull { !subtitleHasAnyTag(tracks[it], SubtitleLanguageMatching.BRAZILIAN_TAGS) }
        ?: candidateIndexes.first()
}

internal fun breakSpanishSubtitleTie(
    tracks: List<SubtitleTrack>,
    candidateIndexes: List<Int>,
    normalizedTarget: String,
): Int = if (normalizedTarget == "es-419") {
    candidateIndexes.firstOrNull {
        subtitleHasAnyTag(tracks[it], SubtitleLanguageMatching.LATINO_TAGS) &&
            !subtitleHasAnyTag(tracks[it], SubtitleLanguageMatching.CASTILIAN_TAGS)
    } ?: candidateIndexes.firstOrNull { subtitleHasAnyTag(tracks[it], SubtitleLanguageMatching.LATINO_TAGS) }
        ?: candidateIndexes.first()
} else {
    candidateIndexes.firstOrNull {
        subtitleHasAnyTag(tracks[it], SubtitleLanguageMatching.CASTILIAN_TAGS) &&
            !subtitleHasAnyTag(tracks[it], SubtitleLanguageMatching.LATINO_TAGS)
    } ?: candidateIndexes.firstOrNull { subtitleHasAnyTag(tracks[it], SubtitleLanguageMatching.CASTILIAN_TAGS) }
        ?: candidateIndexes.firstOrNull { !subtitleHasAnyTag(tracks[it], SubtitleLanguageMatching.LATINO_TAGS) }
        ?: candidateIndexes.first()
}

private fun subtitleHasAnyTag(track: SubtitleTrack, tags: List<String>): Boolean =
    SubtitleLanguageMatching.subtitleHasAnyTag(track.label, track.language, track.id, tags)

internal fun filterAddonSubtitlesForSettings(
    subtitles: List<AddonSubtitle>,
    settings: PlayerSettingsUiState,
): List<AddonSubtitle> {
    val shouldFilter = settings.subtitleStyle.showOnlyPreferredLanguages
    if (!shouldFilter) return subtitles

    val targets = preferredSubtitleTargetsForSettings(settings)
        .mapNotNull(::normalizeLanguageCode)
        .distinct()
    if (targets.isEmpty()) return emptyList()

    return subtitles.filter { subtitle ->
        val normalizedLanguage = normalizeLanguageCode(subtitle.language) ?: return@filter false
        targets.any { target ->
            normalizedLanguage == target ||
                normalizedLanguage.substringBefore('-') == target.substringBefore('-')
        }
    }
}

internal fun preferredSubtitleTargetsForSettings(settings: PlayerSettingsUiState): List<String> {
    return resolvePreferredSubtitleLanguageTargets(
        preferredSubtitleLanguage = settings.preferredSubtitleLanguage,
        secondaryPreferredSubtitleLanguage = settings.secondaryPreferredSubtitleLanguage,
        deviceLanguages = DeviceLanguagePreferences.preferredLanguageCodes(),
    ).filterNot { it == SubtitleLanguageOption.FORCED }
}

internal fun findPersistedAudioTrackIndex(
    tracks: List<AudioTrack>,
    preference: PersistedPlayerTrackPreference,
): Int {
    val targetId = preference.audioTrackId?.trim()?.lowercase()?.takeIf { it.isNotBlank() }
    val targetName = preference.audioName?.trim()?.lowercase()?.takeIf { it.isNotBlank() }
    val targetLanguage = normalizeLanguageCode(preference.audioLanguage)
    val strictCandidates = tracks.filter {
        targetLanguage == null || normalizeLanguageCode(it.language) == targetLanguage
    }
    if (targetId != null) {
        strictCandidates.firstOrNull {
            it.id.trim().lowercase() == targetId &&
                (targetName == null || it.label.trim().lowercase().contains(targetName))
        }?.let { return it.index }
    }
    if (targetName != null) {
        strictCandidates.firstOrNull { it.label.trim().lowercase() == targetName }
            ?.let { return it.index }
        strictCandidates.firstOrNull { it.label.trim().lowercase().contains(targetName) }
            ?.let { return it.index }
    }
    if (targetLanguage == null) return -1
    val languageCandidates = tracks.filter { languageMatchesPreference(it.language, targetLanguage) }
    val targetVariant = SubtitleLanguageMatching.detectTrackLanguageVariant(
        language = preference.audioLanguage,
        name = preference.audioName,
        trackId = preference.audioTrackId,
    )
    return languageCandidates.firstOrNull {
        SubtitleLanguageMatching.detectTrackLanguageVariant(
            language = it.language,
            name = it.label,
            trackId = it.id,
        ) == targetVariant
    }?.index ?: languageCandidates.firstOrNull()?.index ?: -1
}

internal fun findPersistedAddonSubtitle(
    subtitles: List<AddonSubtitle>,
    preference: PersistedPlayerTrackPreference,
): AddonSubtitle? {
    preference.addonSubtitleUrl?.takeIf { it.isNotBlank() }?.let { url ->
        subtitles.firstOrNull { it.url == url }?.let { return it }
    }
    // URLs usually change between episodes, but an addon may keep its ids stable: when it
    // offers several subtitles in one language, the id tells the one the user picked.
    preference.addonSubtitleId?.takeIf { it.isNotBlank() }?.let { id ->
        subtitles.firstOrNull { subtitle ->
            subtitle.id == id && (
                preference.addonSubtitleAddonName.isNullOrBlank() ||
                    subtitle.addonName.equals(preference.addonSubtitleAddonName, ignoreCase = true)
                )
        }?.let { return it }
    }
    val language = preference.subtitleLanguage?.takeIf { it.isNotBlank() } ?: return null
    val candidates = subtitles.filter { addonSubtitleMatchesLanguage(it, language) }
    val providerCandidates = preference.addonSubtitleAddonName?.takeIf { it.isNotBlank() }?.let { name ->
        candidates.filter { it.addonName.equals(name, ignoreCase = true) }
    }.orEmpty()
    val preferredCandidates = providerCandidates.ifEmpty { candidates }
    return preferredCandidates.firstOrNull {
        it.display.equals(preference.subtitleName, ignoreCase = true)
    } ?: preferredCandidates.firstOrNull()
}

internal fun findPersistedSubtitleTrackIndex(
    tracks: List<SubtitleTrack>,
    preference: PersistedPlayerTrackPreference,
): Int {
    preference.subtitleTrackId?.takeIf { it.isNotBlank() }?.let { trackId ->
        tracks.firstOrNull { it.id == trackId }?.let { return it.index }
    }

    val languageCandidates = preference.subtitleLanguage?.takeIf { it.isNotBlank() }?.let { language ->
        tracks.indices.filter { index ->
            SubtitleLanguageMatching.matchesLanguageCode(tracks[index].language, language) ||
                subtitleTrackMatchesLanguage(tracks[index], language)
        }
    }.orEmpty()
    val forcedFiltered = if (preference.subtitleIsForced == true) {
        languageCandidates.filter { index -> tracks[index].isForced }
    } else {
        languageCandidates
    }
    if (forcedFiltered.size == 1) return tracks[forcedFiltered.first()].index
    if (forcedFiltered.size > 1) {
        val targetVariant = SubtitleLanguageMatching.detectTrackLanguageVariant(
            language = preference.subtitleLanguage,
            name = preference.subtitleName,
            trackId = preference.subtitleTrackId,
        )
        val variantMatch = forcedFiltered.firstOrNull { index ->
            SubtitleLanguageMatching.detectTrackLanguageVariant(
                language = tracks[index].language,
                name = tracks[index].label,
                trackId = tracks[index].id,
            ) == targetVariant
        }
        return tracks[variantMatch ?: forcedFiltered.first()].index
    }

    preference.subtitleName?.takeIf { it.isNotBlank() }?.let { name ->
        val nameMatches = tracks.filter { it.label.equals(name, ignoreCase = true) }
        val forcedNameMatches = if (preference.subtitleIsForced == true) {
            nameMatches.filter { it.isForced }
        } else {
            nameMatches
        }
        forcedNameMatches.firstOrNull()?.let { return it.index }
    }
    return -1
}

internal fun findPersistedAddonSubtitle(
    subtitles: List<AddonSubtitle>,
    preference: PersistedPlayerTrackPreference,
): AddonSubtitle? {
    val language = preference.subtitleLanguage?.takeIf { it.isNotBlank() } ?: return null
    val candidates = subtitles.filter { addonSubtitleMatchesLanguage(it, language) }
    val providerCandidates = preference.addonSubtitleAddonName?.takeIf { it.isNotBlank() }?.let { name ->
        candidates.filter { it.addonName.equals(name, ignoreCase = true) }
    }.orEmpty()
    val preferredCandidates = providerCandidates.ifEmpty { candidates }
    return preferredCandidates.firstOrNull {
        it.display.equals(preference.subtitleName, ignoreCase = true)
    } ?: preferredCandidates.firstOrNull()
}
