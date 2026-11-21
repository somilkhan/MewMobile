package com.nuvio.app.features.updater

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.nuvio.app.core.build.AppFeaturePolicy
import com.nuvio.app.core.build.AppVersionConfig
import com.nuvio.app.core.i18n.localizedByteUnit
import com.nuvio.app.core.ui.NuvioToastController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import nuvio.composeapp.generated.resources.*
import org.jetbrains.compose.resources.getString

private const val gitHubApiBase = "https://api.github.com"
private val releaseChannelBranches = listOf("enhanced", "cmp-rewrite")

data class AppUpdate(
    val tag: String,
    val title: String,
    val notes: String,
    val releaseUrl: String?,
    val assetName: String,
    val assetUrl: String,
    val assetSizeBytes: Long?,
    val buildCode: Int?,
)

data class AppUpdaterUiState(
    val updateChannel: UpdateChannel = UpdateChannel.STABLE,
    val isChecking: Boolean = false,
    val update: AppUpdate? = null,
    val isUpdateAvailable: Boolean = false,
    val isDownloading: Boolean = false,
    val downloadProgress: Float? = null,
    val downloadedApkPath: String? = null,
    val showDialog: Boolean = false,
    val showUnknownSourcesDialog: Boolean = false,
    val errorMessage: String? = null,
    val isDebugTest: Boolean = false,
)

@Serializable
private data class GitHubReleaseDto(
    @SerialName("tag_name") val tagName: String? = null,
    val name: String? = null,
    val body: String? = null,
    val draft: Boolean = false,
    val prerelease: Boolean = false,
    @SerialName("html_url") val htmlUrl: String? = null,
    @SerialName("target_commitish") val targetCommitish: String? = null,
    val assets: List<GitHubAssetDto> = emptyList(),
)

@Serializable
private data class GitHubAssetDto(
    val name: String,
    @SerialName("browser_download_url") val browserDownloadUrl: String,
    val size: Long? = null,
    @SerialName("content_type") val contentType: String? = null,
)

private val appUpdaterJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

private class NoChannelReleaseException : IllegalStateException(
    runBlocking { getString(Res.string.updates_no_channel_release) },
)

internal object VersionUtils {
    private val semanticVersionRegex = Regex("""(?i)(?:^|[^0-9])v?(\d+(?:\.\d+){1,3})""")
    private val buildCodeRegex = Regex("""(?i)(?:^|[^a-z0-9])build[-_ ]?(\d+)(?:\D|$)""")
    private val trailingBuildCodeRegex = Regex("""(?:^|[-_])(\d+)$""")

    fun normalize(raw: String?): String {
        if (raw.isNullOrBlank()) return ""
        return raw.trim().removePrefix("v").removePrefix("V")
    }

    fun parseVersionParts(raw: String?): List<Int>? {
        val normalized = normalize(raw)
        if (normalized.isBlank()) return null

        val versionToken = semanticVersionRegex.find(normalized)
            ?.groups
            ?.get(1)
            ?.value
            ?: normalized.takeIf { it.firstOrNull()?.isDigit() == true }
            ?: return null

        val parts = versionToken.split('.', '-', '_')
            .filter { it.isNotBlank() }
            .mapNotNull { token -> token.takeWhile { it.isDigit() }.toIntOrNull() }

        return parts.takeIf { it.isNotEmpty() }
    }

    fun parseBuildCode(raw: String?): Int? {
        val normalized = normalize(raw)
        if (normalized.isBlank()) return null

        buildCodeRegex.find(normalized)
            ?.groups
            ?.get(1)
            ?.value
            ?.toIntOrNull()
            ?.let { return it }

        return trailingBuildCodeRegex.find(normalized)
            ?.groups
            ?.get(1)
            ?.value
            ?.toIntOrNull()
    }

    fun firstBuildCode(vararg values: String?): Int? =
        values.firstNotNullOfOrNull(::parseBuildCode)

    fun isRemoteNewer(
        remote: String?,
        local: String?,
        localBuildCode: Int? = null,
        remoteBuildCode: Int? = null,
    ): Boolean {
        val remoteParts = parseVersionParts(remote)
        val localParts = parseVersionParts(local)
        val resolvedRemoteBuildCode = remoteBuildCode ?: parseBuildCode(remote)

        if (remoteParts == null || localParts == null) {
            return if (resolvedRemoteBuildCode != null && localBuildCode != null) {
                resolvedRemoteBuildCode > localBuildCode
            } else {
                false
            }
        }

        val maxSize = maxOf(remoteParts.size, localParts.size)
        for (index in 0 until maxSize) {
            val remoteValue = remoteParts.getOrElse(index) { 0 }
            val localValue = localParts.getOrElse(index) { 0 }
            if (remoteValue != localValue) return remoteValue > localValue
        }

        return if (resolvedRemoteBuildCode != null && localBuildCode != null) {
            resolvedRemoteBuildCode > localBuildCode
        } else {
            false
        }
    }
}

private object AppUpdaterRepository {
    suspend fun getLatestChannelUpdate(): Result<AppUpdate> = runCatching {
        val response = httpRequestRaw(
            method = "GET",
            url = "$gitHubApiBase/repos/${AppUpdateConfig.GITHUB_OWNER}/${AppUpdateConfig.GITHUB_REPO}/releases?per_page=20",
            headers = mapOf(
                "Accept" to "application/vnd.github+json",
                "User-Agent" to "MewMobile",
            ),
            body = "",
        )
        if (response.status !in 200..299) {
            error(getString(Res.string.updates_github_api_error, response.status))
        }

        val releases = appUpdaterJson.decodeFromString<List<GitHubReleaseDto>>(response.body)
        val release = releases.firstOrNull { it.isInstallableRelease() && it.matchesRequestedChannel() }
            ?: releases.firstOrNull { it.isInstallableRelease() }
            ?: throw NoChannelReleaseException()

        val tag = release.tagName?.takeIf { it.isNotBlank() }
            ?: release.name?.takeIf { it.isNotBlank() }
            ?: error(getString(Res.string.updates_release_missing_title))

        val asset = chooseBestApkAsset(release.assets)
            ?: error(getString(Res.string.updates_apk_asset_missing))

        AppUpdate(
            tag = tag,
            title = release.name?.takeIf { it.isNotBlank() } ?: tag,
            notes = release.body.orEmpty(),
            releaseUrl = release.htmlUrl,
            assetName = asset.name,
            assetUrl = asset.browserDownloadUrl,
            assetSizeBytes = asset.size,
            buildCode = VersionUtils.firstBuildCode(asset.name, release.tagName, release.name, release.body),
        )
    }

    private fun GitHubReleaseDto.isInstallableRelease(): Boolean =
        !draft && !prerelease && chooseBestApkAsset(assets) != null

    private fun GitHubReleaseDto.matchesRequestedChannel(): Boolean {
        if (releaseChannelBranches.any { channel ->
                targetCommitish?.trim()?.equals(channel, ignoreCase = true) == true
            }
        ) {
            return true
        }

        return listOf(tagName, name)
            .filterNotNull()
            .any { value ->
                releaseChannelBranches.any { channel -> value.contains(channel, ignoreCase = true) }
            }
    }

    private fun chooseBestApkAsset(assets: List<GitHubAssetDto>): GitHubAssetDto? {
        val apkAssets = assets.filter { asset ->
            val name = asset.name.lowercase()
            (name.endsWith(".apk") || asset.contentType == "application/vnd.android.package-archive") &&
                !name.contains("debug") &&
                !name.contains("unsigned")
        }
        if (apkAssets.isEmpty()) return null
        if (apkAssets.size == 1) return apkAssets.first()

        val supportedAbis = AppUpdaterPlatform.getSupportedAbis()
        for (abi in supportedAbis) {
            val candidate = apkAssets.firstOrNull { asset ->
                asset.name.contains(abi, ignoreCase = true)
            }
            if (candidate != null) return candidate
        }

        return apkAssets.firstOrNull { asset ->
            val name = asset.name.lowercase()
            name.contains("universal") || name.contains("all")
        } ?: apkAssets.firstOrNull { asset ->
            val name = asset.name.lowercase()
            name.contains("full") && (name.contains("release") || name.contains("signed"))
        } ?: apkAssets.first()
    }
}

class AppUpdaterController internal constructor(
    private val scope: CoroutineScope,
    private val preferences: UpdatePreferences = UpdatePreferences.shared,
    private val fetchUpdate: suspend (UpdateChannel) -> Result<AppUpdate> = AppUpdaterRepository::getLatestChannelUpdate,
) {
    private val _uiState = MutableStateFlow(AppUpdaterUiState(updateChannel = preferences.channel.value))
    val uiState: StateFlow<AppUpdaterUiState> = _uiState.asStateFlow()

    private var autoCheckStarted = false
    private var updateCheckJob: Job? = null
    private var downloadJob: Job? = null

    init {
        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            preferences.channel.collect { channel ->
                if (channel != _uiState.value.updateChannel) {
                    updateCheckJob?.cancel()
                    downloadJob?.cancel()
                    _uiState.value.downloadedApkPath?.let(AppUpdaterPlatform::deleteDownloadedApk)
                    _uiState.value = AppUpdaterUiState(updateChannel = channel)
                    if (!AppUpdaterPlatform.isDebugBuild) {
                        checkForUpdates(force = true, showNoUpdateFeedback = false)
                    }
                }
            }
        }
    }

    fun ensureAutoCheckStarted() {
        if (autoCheckStarted || !AppFeaturePolicy.inAppUpdaterEnabled ||
            !AppUpdaterPlatform.isSupported || AppUpdaterPlatform.isDebugBuild
        ) {
            return
        }
        autoCheckStarted = true
        checkForUpdates(force = false, showNoUpdateFeedback = false)
    }

    fun checkForUpdates(force: Boolean, showNoUpdateFeedback: Boolean) {
        if (!AppFeaturePolicy.inAppUpdaterEnabled || !AppUpdaterPlatform.isSupported) {
            if (showNoUpdateFeedback) {
                scope.launch {
                    NuvioToastController.show(getString(Res.string.updates_not_available))
                }
            }
            return
        }

        if (_uiState.value.isDownloading) return
        updateCheckJob?.cancel()
        val channel = preferences.channel.value
        updateCheckJob = scope.launch {
            _uiState.update { state ->
                state.copy(
                    isChecking = true,
                    errorMessage = null,
                    showUnknownSourcesDialog = false,
                    isDebugTest = false,
                )
            }

            val ignoredTag = AppUpdaterPlatform.getIgnoredTag()
            val result = fetchUpdate(channel)
            currentCoroutineContext().ensureActive()
            if (channel != preferences.channel.value) return@launch

            result.onSuccess { update ->
                val remoteNewer = VersionUtils.isRemoteNewer(
                    remote = update.tag,
                    local = AppVersionConfig.VERSION_NAME,
                    localBuildCode = AppVersionConfig.VERSION_CODE,
                    remoteBuildCode = update.buildCode,
                )
                val ignored = ignoredTag != null && ignoredTag == update.tag
                val shouldShowDialog = remoteNewer && (force || !ignored)

                _uiState.update { state ->
                    state.copy(
                        isChecking = false,
                        update = update.takeIf { remoteNewer },
                        isUpdateAvailable = remoteNewer,
                        isDownloading = false,
                        downloadProgress = null,
                        downloadedApkPath = state.downloadedApkPath.takeIf {
                            remoteNewer && state.update?.tag == update.tag
                        },
                        showDialog = shouldShowDialog,
                        showUnknownSourcesDialog = false,
                        errorMessage = null,
                    )
                }

                if (showNoUpdateFeedback && !remoteNewer) {
                    NuvioToastController.show(noUpdateMessage(channel))
                }
            }.onFailure { error ->
                _uiState.update { state ->
                    state.copy(
                        isChecking = false,
                        isDownloading = false,
                        downloadProgress = null,
                        downloadedApkPath = null,
                        update = null,
                        isUpdateAvailable = false,
                        showDialog = force && error !is NoChannelReleaseException,
                        showUnknownSourcesDialog = false,
                        errorMessage = if (force && error !is NoChannelReleaseException) {
                            error.message ?: getString(Res.string.updates_check_failed)
                        } else {
                            null
                        },
                    )
                }

                if (showNoUpdateFeedback) {
                    NuvioToastController.show(
                        if (error is NoChannelReleaseException) noUpdateMessage(channel)
                        else error.message ?: getString(Res.string.updates_check_failed),
                    )
                }
            }
        }
    }

    private suspend fun noUpdateMessage(channel: UpdateChannel): String = getString(
        if (channel == UpdateChannel.STABLE && VersionUtils.isPrerelease(AppVersionConfig.VERSION_NAME)) {
            Res.string.updates_waiting_for_stable
        } else {
            Res.string.updates_latest_version
        },
    )

    fun dismissDialog() {
        _uiState.update { state ->
            state.copy(
                showDialog = false,
                showUnknownSourcesDialog = false,
                errorMessage = null,
            )
        }
    }

    fun ignoreThisVersion() {
        val tag = _uiState.value.update?.tag ?: return
        AppUpdaterPlatform.setIgnoredTag(tag)
        dismissDialog()
    }

    fun downloadUpdate() {
        if (_uiState.value.isDownloading) return
        val update = _uiState.value.update ?: return
        if (_uiState.value.isDebugTest) {
            runDebugDownloadTest()
            return
        }

        val previousDownload = downloadJob
        previousDownload?.cancel()
        val channel = preferences.channel.value
        downloadJob = scope.launch {
            previousDownload?.join()
            val downloadContext = currentCoroutineContext()
            _uiState.update { state ->
                state.copy(
                    isDownloading = true,
                    downloadProgress = 0f,
                    errorMessage = null,
                )
            }

            val result = AppUpdaterPlatform.downloadApk(
                assetUrl = update.assetUrl,
                assetName = update.assetName,
            ) { downloadedBytes, totalBytes ->
                downloadContext.ensureActive()
                val progress = if (totalBytes != null && totalBytes > 0L) {
                    (downloadedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
                } else {
                    null
                }
                _uiState.update { state ->
                    if (state.updateChannel == channel) state.copy(downloadProgress = progress) else state
                }
            }
            currentCoroutineContext().ensureActive()
            if (channel != preferences.channel.value) return@launch
            result.onSuccess { path ->
                _uiState.update { state ->
                    state.copy(
                        isDownloading = false,
                        downloadProgress = null,
                        downloadedApkPath = path,
                        errorMessage = null,
                    )
                }
                installDownloadedUpdate()
            }.onFailure { error ->
                _uiState.update { state ->
                    state.copy(
                        isDownloading = false,
                        downloadProgress = null,
                        downloadedApkPath = null,
                        errorMessage = error.message ?: getString(Res.string.updates_download_failed),
                        showDialog = true,
                    )
                }
            }
        }
    }

    fun installDownloadedUpdate() {
        val apkPath = _uiState.value.downloadedApkPath ?: return
        if (!AppUpdaterPlatform.canRequestPackageInstalls()) {
            _uiState.update { state -> state.copy(showUnknownSourcesDialog = true, showDialog = true) }
            return
        }

        AppUpdaterPlatform.installDownloadedApk(apkPath).onSuccess {
            _uiState.update { state -> state.copy(showUnknownSourcesDialog = false) }
        }.onFailure { error ->
            scope.launch {
                val fallbackMessage = error.message ?: getString(Res.string.updates_install_failed)
                _uiState.update { state ->
                    state.copy(
                        errorMessage = fallbackMessage,
                        showDialog = true,
                    )
                }
            }
        }
    }

    fun resumeInstallation() {
        if (AppUpdaterPlatform.canRequestPackageInstalls()) {
            installDownloadedUpdate()
        } else {
            AppUpdaterPlatform.openUnknownSourcesSettings()
        }
    }

    fun showDebugTestUpdate() {
        if (!AppUpdaterPlatform.isDebugBuild || !AppUpdaterPlatform.isSupported) return

        updateCheckJob?.cancel()
        downloadJob?.cancel()
        _uiState.value = AppUpdaterUiState(
            updateChannel = preferences.channel.value,
            update = AppUpdate(
                tag = "9.9.9",
                title = "Mew 9.9.9",
                notes = """
                    A local preview of the new update experience.

                    - The banner pushes the app content down.
                    - Download progress fills the banner with the primary accent.
                    - Release notes live behind the info button.
                """.trimIndent(),
                releaseUrl = null,
                assetName = "Mew-debug-preview.apk",
                assetUrl = "debug://update-preview",
                assetSizeBytes = 185L * 1024L * 1024L,
                buildCode = 999,
            ),
            isUpdateAvailable = true,
            showDialog = true,
            isDebugTest = true,
        )
    }

    private fun runDebugDownloadTest() {
        downloadJob = scope.launch {
            _uiState.update { state ->
                state.copy(
                    isDownloading = true,
                    downloadProgress = 0f,
                    errorMessage = null,
                )
            }

            for (step in 1..100) {
                delay(35)
                _uiState.update { state -> state.copy(downloadProgress = step / 100f) }
            }

            _uiState.update { state ->
                state.copy(
                    isDownloading = false,
                    isUpdateAvailable = false,
                    downloadProgress = 1f,
                )
            }
        }
    }
}

@Composable
fun rememberAppUpdaterController(): AppUpdaterController {
    val scope = rememberCoroutineScope()
    return remember(scope) { AppUpdaterController(scope) }
}

internal fun formatFileSize(sizeBytes: Long): String {
    if (sizeBytes <= 0L) return "0 ${localizedByteUnit("B")}"
    val units = listOf("B", "KB", "MB", "GB")
    var value = sizeBytes.toDouble()
    var unitIndex = 0
    while (value >= 1024.0 && unitIndex < units.lastIndex) {
        value /= 1024.0
        unitIndex += 1
    }
    val roundedValue = if (value >= 10 || unitIndex == 0) {
        value.toInt().toString()
    } else {
        ((value * 10).toInt() / 10.0).toString()
    }
    return "$roundedValue ${localizedByteUnit(units[unitIndex])}"
}
