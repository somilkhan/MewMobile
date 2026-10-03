package com.nuvio.app.core.region

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class RegionSelectionMode {
    AUTO,
    MANUAL,
}

data class RegionSettingsUiState(
    val mode: RegionSelectionMode = RegionSelectionMode.AUTO,
    val manualCountryCode: String? = null,
    val effectiveContext: RegionContext = RegionContext(
        countryCode = null,
        languageTag = "en",
        source = RegionContextSource.GLOBAL,
    ),
)

object RegionSettingsRepository {
    private val _uiState = MutableStateFlow(RegionSettingsUiState())
    val uiState: StateFlow<RegionSettingsUiState> = _uiState.asStateFlow()

    private var loaded = false
    private var manualCountryCode: String? = null

    fun ensureLoaded() {
        if (loaded) return
        loaded = true
        manualCountryCode = normalizeCountryCode(RegionSettingsStorage.loadManualCountryCode())
        publish()
    }

    fun setAutomatic() {
        ensureLoaded()
        if (manualCountryCode == null) return
        manualCountryCode = null
        RegionSettingsStorage.saveManualCountryCode(null)
        publish()
    }

    fun setManualCountryCode(value: String): Boolean {
        ensureLoaded()
        val normalized = normalizeCountryCode(value) ?: return false
        if (manualCountryCode == normalized) return true
        manualCountryCode = normalized
        RegionSettingsStorage.saveManualCountryCode(normalized)
        publish()
        return true
    }

    internal fun manualCountryCode(): String? {
        ensureLoaded()
        return manualCountryCode
    }

    private fun publish() {
        val effective = RegionContext.resolve(manualCountryCode)
        _uiState.value = RegionSettingsUiState(
            mode = if (manualCountryCode == null) RegionSelectionMode.AUTO else RegionSelectionMode.MANUAL,
            manualCountryCode = manualCountryCode,
            effectiveContext = effective,
        )
    }

    private fun normalizeCountryCode(value: String?): String? =
        value
            ?.trim()
            ?.uppercase()
            ?.takeIf { it.length == 2 && it.all(Char::isLetter) }
}
