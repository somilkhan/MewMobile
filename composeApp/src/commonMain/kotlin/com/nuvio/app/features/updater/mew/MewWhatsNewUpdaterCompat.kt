package com.nuvio.app.features.updater

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/*
 * Mew hook: luq's "What's new" screen expects luq's split updater API (channels, release
 * selector, GitHub constants, what's-new cache). Mew keeps its own single-file updater
 * (AppUpdater.kt), so this file supplies just the pieces What's New needs, pointed at the
 * Mew release repository from AppUpdateConfig.
 */

internal const val GITHUB_API_BASE = "https://api.github.com"
internal const val GITHUB_OWNER = AppUpdateConfig.GITHUB_OWNER
internal const val GITHUB_REPO = AppUpdateConfig.GITHUB_REPO

enum class UpdateChannel(val storedValue: String) {
    STABLE("stable"),
    BETA("beta");

    companion object {
        fun fromStoredValue(value: String?): UpdateChannel? = entries.firstOrNull {
            it.storedValue.equals(value, ignoreCase = true)
        }
    }
}

/** Mew ships one release channel; What's New always reads stable notes. */
internal class UpdatePreferences {
    private val _channel = MutableStateFlow(UpdateChannel.STABLE)
    val channel: StateFlow<UpdateChannel> = _channel.asStateFlow()

    fun setChannel(channel: UpdateChannel) {
        _channel.value = channel
    }

    companion object {
        val shared by lazy { UpdatePreferences() }
    }
}

internal object ReleaseSelector {
    private val prereleaseNamePattern = Regex(
        "(?:^|[\\s._-])(alpha|beta|rc|preview)(?:[\\s._-]|$)",
        RegexOption.IGNORE_CASE,
    )

    fun eligibleReleases(
        releases: List<GitHubReleaseDto>,
        channel: UpdateChannel,
    ): List<GitHubReleaseDto> = releases
        .asSequence()
        .filterNot(GitHubReleaseDto::draft)
        .mapNotNull { release ->
            val version = VersionUtils.parse(release.tagName) ?: VersionUtils.parse(release.name)
                ?: return@mapNotNull null
            val prerelease = release.prerelease ||
                version.prerelease.isNotEmpty() ||
                prereleaseNamePattern.containsMatchIn(release.name.orEmpty())
            Triple(release, version, prerelease)
        }
        .filter { (_, _, prerelease) -> channel == UpdateChannel.BETA || !prerelease }
        .sortedByDescending { it.second }
        .map { it.first }
        .toList()
}

private object MewWhatsNewCacheHolder {
    var payload: String? = null
}

/** In-memory What's New cache (Mew's platform updater has no persisted slot for it). */
internal fun AppUpdaterPlatform.getWhatsNewCache(): String? = MewWhatsNewCacheHolder.payload

internal fun AppUpdaterPlatform.setWhatsNewCache(payload: String?) {
    MewWhatsNewCacheHolder.payload = payload
}
