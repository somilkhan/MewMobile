package com.nuvio.app.features.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.nuvio.app.core.sync.decodeSyncBoolean
import com.nuvio.app.core.sync.decodeSyncString
import com.nuvio.app.core.sync.encodeSyncBoolean
import com.nuvio.app.core.sync.encodeSyncString
import com.nuvio.app.core.storage.ProfileScopedKey
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

actual object ThemeSettingsStorage {
    private const val preferencesName = "nuvio_theme_settings"
    private const val selectedThemeKey = "selected_theme"
    private const val customThemeFirstColorKey = "custom_theme_first_color"
    private const val customThemeSecondColorKey = "custom_theme_second_color"
    private const val amoledEnabledKey = "amoled_enabled"
    private const val liquidGlassNativeTabBarEnabledKey = "liquid_glass_native_tab_bar_enabled"
    private const val liquidGlassAutoHideOnScrollEnabledKey = "liquid_glass_auto_hide_on_scroll_enabled"
    private const val selectedAppLanguageKey = "selected_app_language"
    private const val NAV_BAR_STYLE_KEY = "nav_bar_style"
    private const val NAV_BAR_POSITION_KEY = "nav_bar_position"

    private const val localNavBarStyleKey = "enhanced_local_nav_bar_style"
    private const val localNavBarPositionKey = "enhanced_local_nav_bar_position"
    private const val localNavBarGlassKey = "enhanced_local_nav_bar_glass"
    private const val localLiquidGlassEffectsKey = "enhanced_local_liquid_glass_effects"
    private const val navBarGlowEnabledKey = "nav_bar_glow_enabled"
    private const val navBarDefaultIconColorEnabledKey = "enhanced_local_nav_bar_default_icon_color_enabled"
    private const val navBarDefaultIconColorKey = "enhanced_local_nav_bar_default_icon_color"
    private const val navBarLiquidGlassBarEffectsKey = "enhanced_local_nav_bar_liquid_glass_bar_effects"
    private const val navBarLiquidGlassSelectionEffectsKey = "enhanced_local_nav_bar_liquid_glass_selection_effects"
    private val profileScopedSyncKeys = listOf(
        selectedThemeKey,
        customThemeFirstColorKey,
        customThemeSecondColorKey,
        amoledEnabledKey,
        navBarGlowEnabledKey,
        liquidGlassNativeTabBarEnabledKey,
        liquidGlassAutoHideOnScrollEnabledKey,
    )

    private var preferences: SharedPreferences? = null

    fun initialize(context: Context) {
        preferences = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)
        applySelectedAppLanguage(loadSelectedAppLanguage() ?: AppLanguage.DEVICE.code)
    }

    actual fun loadSelectedTheme(): String? =
        preferences?.getString(ProfileScopedKey.of(selectedThemeKey), null)

    actual fun saveSelectedTheme(themeName: String) {
        preferences
            ?.edit()
            ?.putString(ProfileScopedKey.of(selectedThemeKey), themeName)
            ?.apply()
    }

    actual fun loadCustomThemeFirstColor(): String? =
        preferences?.getString(ProfileScopedKey.of(customThemeFirstColorKey), null)

    actual fun saveCustomThemeFirstColor(colorName: String) {
        preferences?.edit()?.putString(ProfileScopedKey.of(customThemeFirstColorKey), colorName)?.apply()
    }

    actual fun loadCustomThemeSecondColor(): String? =
        preferences?.getString(ProfileScopedKey.of(customThemeSecondColorKey), null)

    actual fun saveCustomThemeSecondColor(colorName: String) {
        preferences?.edit()?.putString(ProfileScopedKey.of(customThemeSecondColorKey), colorName)?.apply()
    }

    actual fun loadAmoledEnabled(): Boolean? =
        preferences?.let { prefs ->
            val key = ProfileScopedKey.of(amoledEnabledKey)
            if (prefs.contains(key)) prefs.getBoolean(key, false) else null
        }

    actual fun saveAmoledEnabled(enabled: Boolean) {
        preferences
            ?.edit()
            ?.putBoolean(ProfileScopedKey.of(amoledEnabledKey), enabled)
            ?.apply()
    }

    actual fun loadNavBarGlowEnabled(): Boolean? =
        preferences?.let { prefs ->
            val key = ProfileScopedKey.of(navBarGlowEnabledKey)
            if (prefs.contains(key)) prefs.getBoolean(key, false) else null
        }

    actual fun saveNavBarGlowEnabled(enabled: Boolean) {
        preferences
            ?.edit()
            ?.putBoolean(ProfileScopedKey.of(navBarGlowEnabledKey), enabled)
            ?.apply()
    }

    actual fun loadNavBarDefaultIconColorEnabled(): Boolean? =
        preferences?.let { prefs ->
            val key = ProfileScopedKey.of(navBarDefaultIconColorEnabledKey)
            if (prefs.contains(key)) prefs.getBoolean(key, false) else null
        }

    actual fun saveNavBarDefaultIconColorEnabled(enabled: Boolean) {
        preferences
            ?.edit()
            ?.putBoolean(ProfileScopedKey.of(navBarDefaultIconColorEnabledKey), enabled)
            ?.apply()
    }

    actual fun loadNavBarDefaultIconColor(): String? = loadRawString(navBarDefaultIconColorKey)

    actual fun saveNavBarDefaultIconColor(color: String) {
        saveRawString(navBarDefaultIconColorKey, color)
    }


    actual fun loadNavBarLiquidGlassBarEffects(): String? = loadRawString(navBarLiquidGlassBarEffectsKey)

    actual fun saveNavBarLiquidGlassBarEffects(settings: String) {
        saveRawString(navBarLiquidGlassBarEffectsKey, settings)
    }

    actual fun loadNavBarLiquidGlassSelectionEffects(): String? =
        loadRawString(navBarLiquidGlassSelectionEffectsKey)

    actual fun saveNavBarLiquidGlassSelectionEffects(settings: String) {
        saveRawString(navBarLiquidGlassSelectionEffectsKey, settings)
    }

    actual fun loadLiquidGlassNativeTabBarEnabled(): Boolean? =
        preferences?.let { prefs ->
            val key = ProfileScopedKey.of(liquidGlassNativeTabBarEnabledKey)
            if (prefs.contains(key)) prefs.getBoolean(key, false) else null
        }

    actual fun saveLiquidGlassNativeTabBarEnabled(enabled: Boolean) {
        preferences
            ?.edit()
            ?.putBoolean(ProfileScopedKey.of(liquidGlassNativeTabBarEnabledKey), enabled)
            ?.apply()
    }

    actual fun loadLiquidGlassAutoHideOnScrollEnabled(): Boolean? =
        preferences?.let { prefs ->
            val key = ProfileScopedKey.of(liquidGlassAutoHideOnScrollEnabledKey)
            if (prefs.contains(key)) prefs.getBoolean(key, false) else null
        }

    actual fun saveLiquidGlassAutoHideOnScrollEnabled(enabled: Boolean) {
        preferences
            ?.edit()
            ?.putBoolean(ProfileScopedKey.of(liquidGlassAutoHideOnScrollEnabledKey), enabled)
            ?.apply()
    }

    actual fun loadSelectedAppLanguage(): String? {
        val value = preferences?.getString(selectedAppLanguageKey, null)
        if (value != null) return value
        val legacy = preferences?.getString(ProfileScopedKey.of(selectedAppLanguageKey), null)
        if (legacy != null) saveSelectedAppLanguage(legacy)
        return legacy
    }

    actual fun saveSelectedAppLanguage(languageCode: String) {
        preferences
            ?.edit()
            ?.putString(selectedAppLanguageKey, languageCode)
            ?.apply()
    }

    actual fun applySelectedAppLanguage(languageCode: String) {
        if (languageCode.equals("device", ignoreCase = true)) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
        } else {
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(languageCode),
            )
        }
    }

    actual fun loadNavBarStyle(): String? =
        loadRawString(localNavBarStyleKey) ?: loadRawString(NAV_BAR_STYLE_KEY)

    actual fun saveNavBarStyle(styleKey: String) {
        saveRawString(localNavBarStyleKey, styleKey)
    }

    actual fun loadNavBarPosition(): String? =
        loadRawString(localNavBarPositionKey) ?: loadRawString(NAV_BAR_POSITION_KEY)

    actual fun saveNavBarPosition(positionKey: String) {
        saveRawString(localNavBarPositionKey, positionKey)
    }

    actual fun loadNavBarGlass(): String? =
        loadRawString(localNavBarGlassKey)

    actual fun saveNavBarGlass(value: String) {
        saveRawString(localNavBarGlassKey, value)
    }

    actual fun loadLiquidGlassEffectsEnabled(): Boolean? =
        preferences?.let { prefs ->
            val key = ProfileScopedKey.of(localLiquidGlassEffectsKey)
            if (prefs.contains(key)) prefs.getBoolean(key, true) else null
        }

    actual fun saveLiquidGlassEffectsEnabled(enabled: Boolean) {
        preferences
            ?.edit()
            ?.putBoolean(ProfileScopedKey.of(localLiquidGlassEffectsKey), enabled)
            ?.apply()
    }

    private fun loadRawString(key: String): String? =
        preferences?.getString(ProfileScopedKey.of(key), null)

    private fun saveRawString(key: String, value: String) {
        preferences
            ?.edit()
            ?.putString(ProfileScopedKey.of(key), value)
            ?.apply()
    }

    actual fun exportToSyncPayload(): JsonObject = buildJsonObject {
        loadSelectedTheme()?.let { put(selectedThemeKey, encodeSyncString(it)) }
        loadCustomThemeFirstColor()?.let { put(customThemeFirstColorKey, encodeSyncString(it)) }
        loadCustomThemeSecondColor()?.let { put(customThemeSecondColorKey, encodeSyncString(it)) }
        loadAmoledEnabled()?.let { put(amoledEnabledKey, encodeSyncBoolean(it)) }
        loadLiquidGlassNativeTabBarEnabled()?.let { put(liquidGlassNativeTabBarEnabledKey, encodeSyncBoolean(it)) }
        loadLiquidGlassAutoHideOnScrollEnabled()?.let { put(liquidGlassAutoHideOnScrollEnabledKey, encodeSyncBoolean(it)) }
        loadNavBarStyle()?.let { put(NAV_BAR_STYLE_KEY, encodeSyncString(it)) }
    }

    actual fun replaceFromSyncPayload(payload: JsonObject) {
        preferences?.edit()?.apply {
            profileScopedSyncKeys.forEach { remove(ProfileScopedKey.of(it)) }
        }?.apply()

        payload.decodeSyncString(selectedThemeKey)?.let(::saveSelectedTheme)
        payload.decodeSyncString(customThemeFirstColorKey)?.let(::saveCustomThemeFirstColor)
        payload.decodeSyncString(customThemeSecondColorKey)?.let(::saveCustomThemeSecondColor)
        payload.decodeSyncBoolean(amoledEnabledKey)?.let(::saveAmoledEnabled)
        payload.decodeSyncBoolean(liquidGlassNativeTabBarEnabledKey)?.let(::saveLiquidGlassNativeTabBarEnabled)
        payload.decodeSyncBoolean(liquidGlassAutoHideOnScrollEnabledKey)?.let(::saveLiquidGlassAutoHideOnScrollEnabled)
        payload.decodeSyncString(NAV_BAR_STYLE_KEY)?.let(::saveNavBarStyle)
        applySelectedAppLanguage(loadSelectedAppLanguage() ?: AppLanguage.DEVICE.code)
    }
}
