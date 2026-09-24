package com.nuvio.app.features.onboarding

import com.nuvio.app.features.settings.NuvioEnhancedSettingsRepository

internal object MewOnboardingRepository {
    fun ensureLoaded() {
        NuvioEnhancedSettingsRepository.ensureLoaded()
    }

    fun complete() {
        NuvioEnhancedSettingsRepository.markOnboardingCompleted()
    }
}
