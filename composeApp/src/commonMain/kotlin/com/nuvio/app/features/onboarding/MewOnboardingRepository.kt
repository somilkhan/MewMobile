package com.nuvio.app.features.onboarding

import com.nuvio.app.features.settings.NuvioEnhancedSettingsRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.distinctUntilChanged

internal object MewOnboardingRepository {
    val visible: StateFlow<Boolean>
        get() = NuvioEnhancedSettingsRepository.uiState
            .map { !it.onboardingCompleted }
            .distinctUntilChanged() as StateFlow<Boolean>

    fun ensureLoaded() {
        NuvioEnhancedSettingsRepository.ensureLoaded()
    }

    fun complete() {
        NuvioEnhancedSettingsRepository.markOnboardingCompleted()
    }
}
