package com.nuvio.app.features.addons

import co.touchlab.kermit.Logger
import com.nuvio.app.core.network.SupabaseProvider
import com.nuvio.app.core.sync.putSyncOriginClientId
import com.nuvio.app.features.profiles.ProfileRepository
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.put
import nuvio.composeapp.generated.resources.*
import org.jetbrains.compose.resources.getString

@Serializable
private data class AddonRow(
    val url: String,
    val name: String? = null,
    val enabled: Boolean = true,
    @SerialName("sort_order") val sortOrder: Int = 0,
)

@Serializable
private data class AddonPushItem(
    val url: String,
    val name: String = "",
    val enabled: Boolean = true,
    @SerialName("sort_order") val sortOrder: Int = 0,
)

object AddonRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val log = Logger.withTag("AddonRepository")
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val _uiState = MutableStateFlow(AddonsUiState())
    val uiState: StateFlow<AddonsUiState> = _uiState.asStateFlow()

    private var initialized = false
    private var pulledFromServer = false
    private var currentProfileId: Int = 1
    // Invalidates initialization work that finishes after a profile switch.
    private var profileGeneration: Long = 0L
    // This repository uses Dispatchers.Default, so UI calls and refresh-job completion can
    // access the registry concurrently. Keep reservation/removal/profile-switch atomic.
    private val refreshJobsLock = Any()
    private val activeRefreshJobs = mutableMapOf<String, Job>()
    private val versionStorageMutex = Mutex()

    fun initialize() {
        val effectiveProfileId = resolveEffectiveProfileId(ProfileRepository.activeProfileId)
        val generation = synchronized(refreshJobsLock) {
            if (initialized) return
            currentProfileId = effectiveProfileId
            initialized = true
            profileGeneration
        }
        log.d { "initialize() — loading local addons for profile $effectiveProfileId" }

        val storedUrls = dedupeManifestUrls(AddonStorage.loadInstalledAddonUrls(effectiveProfileId))
        val enabledByUrl = AddonStorage.loadAddonEnabledStates(effectiveProfileId)
            .mapKeys { (url, _) -> ensureManifestSuffix(url) }
        val pendingVersionChanges = AddonStorage.loadPendingAddonVersionChanges(effectiveProfileId)
        log.d { "initialize() — local addon count: ${storedUrls.size}" }
        if (storedUrls.isEmpty()) return

        val existingByUrl = synchronized(refreshJobsLock) {
            if (
                !initialized ||
                currentProfileId != effectiveProfileId ||
                profileGeneration != generation ||
                resolveEffectiveProfileId(ProfileRepository.activeProfileId) != effectiveProfileId
            ) {
                return
            }
            val existing = _uiState.value.addons.associateBy(ManagedAddon::manifestUrl)
            _uiState.value = AddonsUiState(
                addons = storedUrls.map { manifestUrl ->
                    existing[manifestUrl].toPendingAddon(
                        manifestUrl = manifestUrl,
                        enabled = enabledByUrl[manifestUrl],
                    ).copy(
                        versionChangedFrom = pendingVersionChanges[manifestUrl]
                            ?: existing[manifestUrl]?.versionChangedFrom,
                    )
                },
            )
            existing
        }

        storedUrls.forEach { manifestUrl ->
            val existing = existingByUrl[manifestUrl]
            val addon = _uiState.value.addons.firstOrNull { it.manifestUrl == manifestUrl }
            if (addon?.enabled == true && (existing == null || (addon.manifest == null && !addon.isRefreshing))) {
                refreshAddonForProfile(manifestUrl, effectiveProfileId, generation)
            }
        }
    }

    fun onProfileChanged(profileId: Int) {
        val effectiveProfileId = resolveEffectiveProfileId(profileId)
        val jobsToCancel = synchronized(refreshJobsLock) {
            if (effectiveProfileId == currentProfileId && initialized) return
            val jobs = activeRefreshJobs.values.toList()
            activeRefreshJobs.clear()
            profileGeneration += 1
            currentProfileId = effectiveProfileId
            initialized = false
            pulledFromServer = false
            _uiState.value = AddonsUiState()
            jobs
        }
        jobsToCancel.forEach(Job::cancel)
    }

    fun clearLocalState() {
        val jobsToCancel = synchronized(refreshJobsLock) {
            val jobs = activeRefreshJobs.values.toList()
            activeRefreshJobs.clear()
            profileGeneration += 1
            currentProfileId = 1
            initialized = false
            pulledFromServer = false
            _uiState.value = AddonsUiState()
            jobs
        }
        jobsToCancel.forEach(Job::cancel)
    }

    suspend fun pullFromServer(profileId: Int) {
        val effectiveProfileId = resolveEffectiveProfileId(profileId)
        onProfileChanged(effectiveProfileId)
        val requestSession = synchronized(refreshJobsLock) {
            currentProfileId to profileGeneration
        }
        val requestProfileId = requestSession.first
        val requestGeneration = requestSession.second
        log.i { "pullFromServer() — profileId=$profileId, initialized=$initialized, pulledFromServer=$pulledFromServer" }
        runCatching {
            val rows = SupabaseProvider.client.postgrest
                .from("addons")
                .select {
                    filter { eq("profile_id", requestProfileId) }
                    order("sort_order", Order.ASCENDING)
                }
                .decodeList<AddonRow>()
            if (!isCurrentProfile(requestProfileId, requestGeneration)) return


            val rowsByUrl = linkedMapOf<String, AddonRow>()
            rows.forEach { row ->
                val manifestUrl = ensureManifestSuffix(row.url)
                if (!rowsByUrl.containsKey(manifestUrl)) {
                    rowsByUrl[manifestUrl] = row.copy(url = manifestUrl)
                }
            }

            val urls = rowsByUrl.keys.toList()
            log.i { "pullFromServer() — server returned ${rows.size} addons" }
            urls.forEachIndexed { i, u -> log.d { "  server[$i]: $u" } }

            if (urls.isEmpty() && !pulledFromServer) {
                val localUrls = dedupeManifestUrls(AddonStorage.loadInstalledAddonUrls(requestProfileId))
                log.i { "pullFromServer() — server empty, local has ${localUrls.size} addons" }
                if (localUrls.isNotEmpty()) {
                    log.i { "pullFromServer() — migrating local addons to server for profile $requestProfileId" }
                    initialize()
                    pulledFromServer = true
                    val enabledByUrl = loadLocalEnabledStates()
                    val addons = localUrls.mapIndexed { index, addonUrl ->
                        val manifestUrl = ensureManifestSuffix(addonUrl)
                        AddonPushItem(
                            url = manifestUrl,
                            name = _uiState.value.addons
                                .find { it.manifestUrl == manifestUrl }?.manifest?.name ?: "",
                            enabled = enabledByUrl[manifestUrl]
                                ?: _uiState.value.addons.find { it.manifestUrl == manifestUrl }?.enabled
                                ?: true,
                            sortOrder = index,
                        )
                    }
                    val params = buildJsonObject {
                        put("p_profile_id", requestProfileId)
                        put("p_addons", json.encodeToJsonElement(addons))
                        putSyncOriginClientId()
                    }
                    SupabaseProvider.client.postgrest.rpc("sync_push_addons", params)
                    log.i { "pullFromServer() — migration push done (${addons.size} addons)" }
                    return
                }
            }

            if (urls.isEmpty()) {
                val localUrls = dedupeManifestUrls(AddonStorage.loadInstalledAddonUrls(requestProfileId))
                if (localUrls.isNotEmpty()) {
                    log.w { "pullFromServer() — remote empty while local has ${localUrls.size} addons; preserving local addons" }
                    val enabledByUrl = loadLocalEnabledStates()
                    val existingByUrl = _uiState.value.addons.associateBy(ManagedAddon::manifestUrl)
                    _uiState.value = AddonsUiState(
                        addons = localUrls.map { url ->
                            existingByUrl[url].toPendingAddon(
                                manifestUrl = url,
                                enabled = enabledByUrl[url],
                            )
                        },
                    )
                    persist()
                    localUrls.forEach { url ->
                        val existing = existingByUrl[url]
                        val addon = _uiState.value.addons.firstOrNull { it.manifestUrl == url }
                        if (addon?.enabled == true && (existing == null || (addon.manifest == null && !addon.isRefreshing))) {
                            refreshAddon(url)
                        }
                    }
                    pulledFromServer = true
                    initialized = true
                    return
                }
            }

            val existingByUrl = _uiState.value.addons.associateBy(ManagedAddon::manifestUrl)
            _uiState.value = AddonsUiState(
                addons = urls.map { url ->
                    val row = rowsByUrl[url]
                    existingByUrl[url].toPendingAddon(
                        manifestUrl = url,
                        userSetName = row?.name?.takeIf { it.isNotBlank() },
                        enabled = row?.enabled,
                    )
                },
            )
            persist()
            urls.forEach { url ->
                val existing = existingByUrl[url]
                val addon = _uiState.value.addons.firstOrNull { it.manifestUrl == url }
                if (addon?.enabled == true && (existing == null || (addon.manifest == null && !addon.isRefreshing))) {
                    refreshAddon(url)
                }
            }
            pulledFromServer = true
            initialized = true
            log.i { "pullFromServer() — applied ${urls.size} addons to state" }
        }.onFailure { e ->
            log.e(e) { "pullFromServer() — FAILED" }
        }
    }

    suspend fun awaitManifestsLoaded() {
        if (_uiState.value.addons.isEmpty()) return
        uiState.first { state ->
            state.addons.isEmpty() ||
                state.addons.any { it.manifest != null } ||
                state.addons.none { it.isRefreshing }
        }
    }

    suspend fun addAddon(rawUrl: String): AddAddonResult {
        if (isUsingPrimaryAddonsFromSecondaryProfile()) {
            return AddAddonResult.Error(getString(Res.string.profile_primary_addons_required))
        }
        log.i { "addAddon() — rawUrl=$rawUrl" }
        val manifestUrl = try {
            normalizeManifestUrl(rawUrl)
        } catch (error: IllegalArgumentException) {
            return AddAddonResult.Error(error.message ?: getString(Res.string.addon_invalid_url))
        }

        if (_uiState.value.addons.any { it.manifestUrl == manifestUrl }) {
            return AddAddonResult.Error(getString(Res.string.addon_already_installed))
        }

        val profileId = currentProfileId
        val manifest = try {
            withContext(Dispatchers.Default) {
                val payload = httpGetText(manifestUrl)
                AddonManifestParser.parse(
                    manifestUrl = manifestUrl,
                    payload = payload,
                )
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            return AddAddonResult.Error(error.message ?: getString(Res.string.addon_load_manifest_failed))
        }
        if (profileId != currentProfileId) {
            return AddAddonResult.Error("Active profile changed while adding the add-on")
        }

        _uiState.update { current ->
            current.copy(
                addons = current.addons + ManagedAddon(
                    manifestUrl = manifestUrl,
                    manifest = manifest,
                    isRefreshing = false,
                    errorMessage = null,
                ),
            )
        }
        saveKnownVersion(profileId, manifestUrl, manifest.version)
        persist()
        pushToServer()
        return AddAddonResult.Success(manifest)
    }

    fun removeAddon(manifestUrl: String) {
        if (isUsingPrimaryAddonsFromSecondaryProfile()) return
        log.i { "removeAddon() — $manifestUrl" }
        _uiState.update { current ->
            current.copy(
                addons = current.addons.filterNot { it.manifestUrl == manifestUrl },
            )
        }
        val refreshToCancel = synchronized(refreshJobsLock) {
            activeRefreshJobs.remove(manifestUrl)
        }
        refreshToCancel?.cancel()
        val profileId = currentProfileId
        scope.launch {
            versionStorageMutex.withLock {
                val knownVersions = AddonStorage.loadKnownAddonVersions(profileId).toMutableMap()
                knownVersions.remove(manifestUrl)
                AddonStorage.saveKnownAddonVersions(profileId, knownVersions)
                val pendingChanges = AddonStorage.loadPendingAddonVersionChanges(profileId).toMutableMap()
                pendingChanges.remove(manifestUrl)
                AddonStorage.savePendingAddonVersionChanges(profileId, pendingChanges)
            }
        }
        persist()
        pushToServer()
    }

    fun moveAddon(fromIndex: Int, toIndex: Int) {
        if (isUsingPrimaryAddonsFromSecondaryProfile()) return
        _uiState.update { current ->
            val addons = current.addons
            if (
                fromIndex !in addons.indices ||
                toIndex !in addons.indices ||
                fromIndex == toIndex
            ) {
                return@update current
            }

            val reordered = addons.toMutableList()
            val movingAddon = reordered.removeAt(fromIndex)
            reordered.add(toIndex, movingAddon)
            current.copy(addons = reordered)
        }
        persist()
        pushToServer()
    }

    fun setAddonEnabled(manifestUrl: String, enabled: Boolean) {
        if (isUsingPrimaryAddonsFromSecondaryProfile()) return
        var shouldRefresh = false
        _uiState.update { current ->
            current.copy(
                addons = current.addons.map { addon ->
                    if (addon.manifestUrl != manifestUrl || addon.enabled == enabled) {
                        addon
                    } else {
                        shouldRefresh = enabled && addon.manifest == null && !addon.isRefreshing
                        addon.copy(enabled = enabled)
                    }
                },
            )
        }
        persist()
        pushToServer()
        if (shouldRefresh) {
            refreshAddon(manifestUrl)
        }
    }

    fun refreshAll() {
        _uiState.value.addons.distinctBy { it.manifestUrl }.forEach { addon ->
            refreshAddon(addon.manifestUrl)
        }
    }

    fun refreshAddon(manifestUrl: String) {
        refreshAddonInternal(manifestUrl, expectedProfileId = null, expectedGeneration = null)
    }

    private fun refreshAddonForProfile(manifestUrl: String, profileId: Int, generation: Long) {
        refreshAddonInternal(manifestUrl, expectedProfileId = profileId, expectedGeneration = generation)
    }

    private fun refreshAddonInternal(
        manifestUrl: String,
        expectedProfileId: Int?,
        expectedGeneration: Long?,
    ) {
        lateinit var refreshJob: Job
        synchronized(refreshJobsLock) {
            if (expectedProfileId != null && (
                    currentProfileId != expectedProfileId ||
                        profileGeneration != expectedGeneration ||
                        !initialized ||
                        resolveEffectiveProfileId(ProfileRepository.activeProfileId) != expectedProfileId
                )
            ) return
            // A lazy job is not active until start(), so checking isActive alone permits two
            // callers to reserve the same URL before either starts.
            if (!canReserveAddonRefresh(activeRefreshJobs[manifestUrl])) return
            val profileId = currentProfileId
            markRefreshing(manifestUrl)
            refreshJob = scope.launch(start = CoroutineStart.LAZY) {
                try {
                    val result = runCatching {
                        val payload = httpGetText(manifestUrl)
                        AddonManifestParser.parse(manifestUrl = manifestUrl, payload = payload)
                    }.onFailure { error ->
                        if (error is CancellationException) throw error
                    }
                    if (profileId != currentProfileId) return@launch
                    val fallbackPreviousVersion = _uiState.value.addons
                        .firstOrNull { it.manifestUrl == manifestUrl }?.manifest?.version
                    val versionChangedFrom = result.getOrNull()?.let { manifest ->
                        recordManifestVersion(profileId, manifestUrl, manifest.version, fallbackPreviousVersion)
                    }
                    if (profileId != currentProfileId) return@launch
                    _uiState.update { current ->
                        if (profileId != currentProfileId) return@update current
                        current.copy(
                            addons = current.addons.map { addon ->
                                if (addon.manifestUrl != manifestUrl) addon
                                else result.fold(
                                    onSuccess = { manifest -> addon.copy(
                                        manifest = manifest,
                                        isRefreshing = false,
                                        errorMessage = null,
                                        versionChangedFrom = versionChangedFrom,
                                    ) },
                                    onFailure = { error -> addon.copy(
                                        isRefreshing = false,
                                        errorMessage = error.message ?: getString(Res.string.addon_load_manifest_failed),
                                    ) },
                                )
                            },
                        )
                    }
                } finally {
                    synchronized(refreshJobsLock) {
                        if (activeRefreshJobs[manifestUrl] === refreshJob) activeRefreshJobs.remove(manifestUrl)
                    }
                }
            }
            activeRefreshJobs[manifestUrl] = refreshJob
        }
        refreshJob.start()
    }

    fun dismissVersionChange(manifestUrl: String) {
        val profileId = currentProfileId
        _uiState.update { current ->
            if (profileId != currentProfileId) return@update current
            current.copy(addons = current.addons.map { addon ->
                if (addon.manifestUrl == manifestUrl) addon.copy(versionChangedFrom = null) else addon
            })
        }
        scope.launch {
            versionStorageMutex.withLock {
                val pending = AddonStorage.loadPendingAddonVersionChanges(profileId).toMutableMap()
                pending.remove(manifestUrl)
                AddonStorage.savePendingAddonVersionChanges(profileId, pending)
            }
        }
    }

    private fun pushToServer() {
        scope.launch {
            runCatching {
                if (isUsingPrimaryAddonsFromSecondaryProfile()) {
                    return@runCatching
                }
                val profileId = currentProfileId
                val addons = _uiState.value.addons
                    .distinctBy { it.manifestUrl }
                    .mapIndexed { index, addon ->
                        AddonPushItem(
                            url = addon.manifestUrl,
                            name = addon.userSetName?.takeIf { it.isNotBlank() } ?: addon.manifest?.name ?: "",
                            enabled = addon.enabled,
                            sortOrder = index,
                        )
                    }
                log.d { "pushToServer() — profileId=$profileId, pushing ${addons.size} addons" }
                val params = buildJsonObject {
                    put("p_profile_id", profileId)
                    put("p_addons", json.encodeToJsonElement(addons))
                    putSyncOriginClientId()
                }
                SupabaseProvider.client.postgrest.rpc("sync_push_addons", params)
                log.d { "pushToServer() — success" }
            }.onFailure { e ->
                log.e(e) { "pushToServer() — FAILED" }
            }
        }
    }

    private fun markRefreshing(manifestUrl: String) {
        _uiState.update { current ->
            current.copy(
                addons = current.addons.map { addon ->
                    if (addon.manifestUrl == manifestUrl) {
                        addon.copy(
                            isRefreshing = true,
                            errorMessage = null,
                        )
                    } else {
                        addon
                    }
                },
            )
        }
    }

    private fun persist() {
        val addons = _uiState.value.addons
        AddonStorage.saveInstalledAddonUrls(
            currentProfileId,
            dedupeManifestUrls(addons.map { it.manifestUrl }),
        )
        AddonStorage.saveAddonEnabledStates(
            currentProfileId,
            addons.associate { it.manifestUrl to it.enabled },
        )
    }

    private suspend fun saveKnownVersion(profileId: Int, manifestUrl: String, version: String) {
        if (version.isBlank()) return
        versionStorageMutex.withLock {
            val versions = AddonStorage.loadKnownAddonVersions(profileId).toMutableMap()
            versions[manifestUrl] = version
            AddonStorage.saveKnownAddonVersions(profileId, versions)
        }
    }

    private suspend fun recordManifestVersion(
        profileId: Int,
        manifestUrl: String,
        currentVersion: String,
        fallbackPreviousVersion: String?,
    ): String? {
        if (currentVersion.isBlank()) return versionStorageMutex.withLock {
            AddonStorage.loadPendingAddonVersionChanges(profileId)[manifestUrl]
        }
        return versionStorageMutex.withLock {
            val knownVersions = AddonStorage.loadKnownAddonVersions(profileId).toMutableMap()
            val pendingChanges = AddonStorage.loadPendingAddonVersionChanges(profileId).toMutableMap()
            val pendingVersion = addonVersionChangeFrom(
                previousVersion = knownVersions[manifestUrl] ?: fallbackPreviousVersion,
                currentVersion = currentVersion,
                pendingVersion = pendingChanges[manifestUrl],
            )
            knownVersions[manifestUrl] = currentVersion
            if (pendingVersion != null) pendingChanges[manifestUrl] = pendingVersion
            AddonStorage.saveKnownAddonVersions(profileId, knownVersions)
            AddonStorage.savePendingAddonVersionChanges(profileId, pendingChanges)
            pendingVersion
        }
    }

    private fun loadLocalEnabledStates(): Map<String, Boolean> =
        AddonStorage.loadAddonEnabledStates(currentProfileId)
            .mapKeys { (url, _) -> ensureManifestSuffix(url) }

    private fun isCurrentProfile(profileId: Int, generation: Long): Boolean =
        synchronized(refreshJobsLock) {
            currentProfileId == profileId &&
                profileGeneration == generation &&
                resolveEffectiveProfileId(ProfileRepository.activeProfileId) == profileId
        }

    private fun cancelActiveRefreshes() {
        val jobs = synchronized(refreshJobsLock) {
            activeRefreshJobs.values.toList().also { activeRefreshJobs.clear() }
        }
        jobs.forEach(Job::cancel)
    }

    private fun resolveEffectiveProfileId(profileId: Int): Int {
        val active = ProfileRepository.state.value.activeProfile
        return if (active != null && active.profileIndex != 1 && active.usesPrimaryAddons) 1 else profileId
    }

    private fun isUsingPrimaryAddonsFromSecondaryProfile(): Boolean {
        val active = ProfileRepository.state.value.activeProfile
        return active != null && active.profileIndex != 1 && active.usesPrimaryAddons
    }
}

private fun ManagedAddon?.toPendingAddon(
    manifestUrl: String,
    userSetName: String? = null,
    enabled: Boolean? = null,
): ManagedAddon =
    when {
        this == null -> ManagedAddon(
            manifestUrl = manifestUrl,
            isRefreshing = enabled ?: true,
            userSetName = userSetName,
            enabled = enabled ?: true,
        )
        manifest != null -> copy(
            manifestUrl = manifestUrl,
            isRefreshing = false,
            userSetName = userSetName ?: this.userSetName,
            enabled = enabled ?: this.enabled,
        )
        isRefreshing -> copy(
            manifestUrl = manifestUrl,
            userSetName = userSetName ?: this.userSetName,
            enabled = enabled ?: this.enabled,
        )
        else -> copy(
            manifestUrl = manifestUrl,
            isRefreshing = enabled ?: this.enabled,
            errorMessage = null,
            userSetName = userSetName ?: this.userSetName,
            enabled = enabled ?: this.enabled,
        )
    }

private fun dedupeManifestUrls(urls: List<String>): List<String> =
    urls.map(::ensureManifestSuffix).distinct()

private fun ensureManifestSuffix(url: String): String {
    val path = url.substringBefore("?").trimEnd('/')
    val query = url.substringAfter("?", "")
    val withSuffix = if (path.endsWith("/manifest.json")) path else "$path/manifest.json"
    return if (query.isEmpty()) withSuffix else "$withSuffix?$query"
}

private fun normalizeManifestUrl(rawUrl: String): String {
    val trimmed = rawUrl.trim()
    require(trimmed.isNotEmpty()) { runBlocking { getString(Res.string.addons_error_enter_url) } }

    val normalizedScheme = when {
        trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed
        trimmed.startsWith("stremio://") -> "https://${trimmed.removePrefix("stremio://")}"
        else -> "https://$trimmed"
    }

    val withoutFragment = normalizedScheme.substringBefore("#")
    val query = withoutFragment.substringAfter("?", "")
    val path = withoutFragment.substringBefore("?").trimEnd('/')
    val manifestPath = if (path.endsWith("/manifest.json")) {
        path
    } else {
        "$path/manifest.json"
    }

    return if (query.isEmpty()) manifestPath else "$manifestPath?$query"
}
