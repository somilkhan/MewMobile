package com.nuvio.app.features.settings

import com.nuvio.app.core.ui.AppTheme
import com.nuvio.app.core.ui.CustomThemeColors
import com.nuvio.app.core.ui.NativeTabBridge
import com.nuvio.app.core.ui.ThemeColors
import androidx.compose.ui.graphics.Color
import com.nuvio.app.core.ui.ThemeAccentColor
import com.nuvio.app.core.ui.formatHexColor
import com.nuvio.app.core.ui.glass.LiquidGlassEffectDefaults
import com.nuvio.app.core.ui.glass.LiquidGlassEffectSettings
import com.nuvio.app.core.ui.glass.LiquidGlassSettings
import com.nuvio.app.core.ui.glass.LiquidGlassStyle
import com.nuvio.app.core.ui.glass.decodeLiquidGlassEffectSettings
import com.nuvio.app.core.ui.glass.encodeLiquidGlassEffectSettings
import com.nuvio.app.core.ui.glass.migrateLegacyNavBarGlassEffects
import com.nuvio.app.core.ui.nativeAccentGradientHex
import com.nuvio.app.core.ui.parseHexColor
import com.nuvio.app.core.ui.toThemeColor
import com.nuvio.app.core.ui.toThemeHex
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal const val DEFAULT_NAV_BAR_ICON_COLOR = 0x969CA3

object ThemeSettingsRepository {
    private val _selectedTheme = MutableStateFlow(AppTheme.MEW)
    private val _selectedThemePreference = MutableStateFlow<AppTheme?>(null)
    val selectedThemePreference: StateFlow<AppTheme?> = _selectedThemePreference.asStateFlow()
    val selectedTheme: StateFlow<AppTheme> = _selectedTheme.asStateFlow()

    private val _customThemeFirstColor = MutableStateFlow(ThemeAccentColor.PINK.color)
    val customThemeFirstColor: StateFlow<Color> = _customThemeFirstColor.asStateFlow()

    private val _customThemeSecondColor = MutableStateFlow(ThemeAccentColor.CYAN.color)
    val customThemeSecondColor: StateFlow<Color> = _customThemeSecondColor.asStateFlow()

    /** luq-compatible view of Mew's two custom colors (consumed by app background / layout pages). */
    private val _customThemeColors = MutableStateFlow(mewCustomThemeColors(ThemeAccentColor.PINK.color, ThemeAccentColor.CYAN.color))
    val customThemeColors: StateFlow<CustomThemeColors> = _customThemeColors.asStateFlow()
    val customThemePreference: StateFlow<CustomThemeColors> = customThemeColors
    private val _amoledEnabled = MutableStateFlow(false)
    val amoledEnabled: StateFlow<Boolean> = _amoledEnabled.asStateFlow()

    private val _tabBarBehavior = MutableStateFlow(NuvioTabBarBehavior.Default)
    val tabBarBehavior: StateFlow<NuvioTabBarBehavior> = _tabBarBehavior.asStateFlow()

    private val _liquidGlassNativeTabBarEnabled = MutableStateFlow(NuvioTabBarBehavior.Default.isEnabled)
    val liquidGlassNativeTabBarEnabled: StateFlow<Boolean> = _liquidGlassNativeTabBarEnabled.asStateFlow()

    private val _liquidGlassAutoHideOnScrollEnabled = MutableStateFlow(false)
    val liquidGlassAutoHideOnScrollEnabled: StateFlow<Boolean> = _liquidGlassAutoHideOnScrollEnabled.asStateFlow()

    private val _dynamicArtworkBackgroundEnabled = MutableStateFlow(false)
    val dynamicArtworkBackgroundEnabled: StateFlow<Boolean> = _dynamicArtworkBackgroundEnabled.asStateFlow()

    private val _showCatalogAccentEnabled = MutableStateFlow(false)
    val showCatalogAccentEnabled: StateFlow<Boolean> = _showCatalogAccentEnabled.asStateFlow()

    private val _selectedAppLanguage = MutableStateFlow(AppLanguage.DEVICE)
    val selectedAppLanguage: StateFlow<AppLanguage> = _selectedAppLanguage.asStateFlow()

    private val _navBarStyle = MutableStateFlow(NavBarStyle.ADAPTIVE)
    val navBarStyle: StateFlow<NavBarStyle> = _navBarStyle.asStateFlow()

    private val _navBarPosition = MutableStateFlow(NavBarPosition.BOTTOM)
    val navBarPosition: StateFlow<NavBarPosition> = _navBarPosition.asStateFlow()

    private val _navBarGlowEnabled = MutableStateFlow(true)
    val navBarGlowEnabled: StateFlow<Boolean> = _navBarGlowEnabled.asStateFlow()

    private val _navBarDefaultIconColorEnabled = MutableStateFlow(false)
    val navBarDefaultIconColorEnabled: StateFlow<Boolean> = _navBarDefaultIconColorEnabled.asStateFlow()

    private val _navBarDefaultIconColor = MutableStateFlow(DEFAULT_NAV_BAR_ICON_COLOR)
    val navBarDefaultIconColor: StateFlow<Int> = _navBarDefaultIconColor.asStateFlow()

    private val _navBarDefaultIconColorRevision = MutableStateFlow(0L)
    internal val navBarDefaultIconColorRevision: StateFlow<Long> = _navBarDefaultIconColorRevision.asStateFlow()
    private var navBarDefaultIconColorDraftDirty = false

    private val _navBarLiquidGlassBarEffects = MutableStateFlow(LiquidGlassEffectDefaults.floatingBar)
    internal val navBarLiquidGlassBarEffects: StateFlow<LiquidGlassEffectSettings> = _navBarLiquidGlassBarEffects.asStateFlow()

    private val _navBarLiquidGlassSelectionEffects = MutableStateFlow(LiquidGlassEffectDefaults.selectionLens)
    internal val navBarLiquidGlassSelectionEffects: StateFlow<LiquidGlassEffectSettings> = _navBarLiquidGlassSelectionEffects.asStateFlow()

    private val _navBarLiquidGlassEffectsRevision = MutableStateFlow(0L)
    internal val navBarLiquidGlassEffectsRevision: StateFlow<Long> = _navBarLiquidGlassEffectsRevision.asStateFlow()

    private val _navBarGlass = MutableStateFlow(LiquidGlassStyle.Default)
    val navBarGlass: StateFlow<LiquidGlassStyle> = _navBarGlass.asStateFlow()

    private val _liquidGlassEffectsEnabled = MutableStateFlow(true)
    val liquidGlassEffectsEnabled: StateFlow<Boolean> = _liquidGlassEffectsEnabled.asStateFlow()

    private var hasLoaded = false

    fun ensureLoaded() {
        if (hasLoaded) return
        loadFromDisk()
    }

    fun onProfileChanged() {
        loadFromDisk()
        com.nuvio.app.core.ui.background.AppBackgroundRepository.reload()
    }

    fun clearLocalState() {
        hasLoaded = false
        _selectedTheme.value = AppTheme.MEW
        _selectedThemePreference.value = null
        _customThemeFirstColor.value = ThemeAccentColor.PINK.color
        _customThemeSecondColor.value = ThemeAccentColor.CYAN.color
        syncCustomThemeColors()
        _amoledEnabled.value = false
        _liquidGlassNativeTabBarEnabled.value = false
        _liquidGlassAutoHideOnScrollEnabled.value = false
        _tabBarBehavior.value = NuvioTabBarBehavior.OFF
        _dynamicArtworkBackgroundEnabled.value = false
        _showCatalogAccentEnabled.value = false
        NativeTabBridge.publishAccentColor(AppTheme.MEW.nativeTabAccentHex())
        NativeTabBridge.publishTabBarBehavior(NuvioTabBarBehavior.OFF)
        NativeTabBridge.publishLiquidGlassEnabled(false)
        _selectedAppLanguage.value = AppLanguage.DEVICE
        _navBarGlowEnabled.value = true
        _navBarDefaultIconColorEnabled.value = false
        _navBarDefaultIconColor.value = DEFAULT_NAV_BAR_ICON_COLOR
        navBarDefaultIconColorDraftDirty = false
        _navBarLiquidGlassBarEffects.value = LiquidGlassEffectDefaults.floatingBar
        _navBarLiquidGlassSelectionEffects.value = LiquidGlassEffectDefaults.selectionLens
        _navBarStyle.value = NavBarStyle.ADAPTIVE
        _navBarPosition.value = NavBarPosition.BOTTOM
        _navBarGlass.value = LiquidGlassStyle.Default
        LiquidGlassSettings.userChoice = null
        _liquidGlassEffectsEnabled.value = LiquidGlassSettings.enabled
    }

    private fun loadFromDisk() {
        hasLoaded = true
        val stored = ThemeSettingsStorage.loadSelectedTheme()
        val theme = stored.toAppTheme()
        _selectedTheme.value = theme
        _selectedThemePreference.value = stored?.let { theme }
        _customThemeFirstColor.value = ThemeSettingsStorage.loadCustomThemeFirstColor()
            .toThemeColor(ThemeAccentColor.PINK.color)
        _customThemeSecondColor.value = ThemeSettingsStorage.loadCustomThemeSecondColor()
            .toThemeColor(ThemeAccentColor.CYAN.color)
        syncCustomThemeColors()
        NativeTabBridge.publishAccentColor(theme.nativeTabAccentHex(_customThemeFirstColor.value))
        _amoledEnabled.value = ThemeSettingsStorage.loadAmoledEnabled() ?: false
        val liquidGlassEnabled = ThemeSettingsStorage.loadLiquidGlassNativeTabBarEnabled() ?: false
        _liquidGlassNativeTabBarEnabled.value = liquidGlassEnabled
        _liquidGlassAutoHideOnScrollEnabled.value =
            ThemeSettingsStorage.loadLiquidGlassAutoHideOnScrollEnabled() ?: false
        NativeTabBridge.publishLiquidGlassEnabled(liquidGlassEnabled)
        val behavior = NuvioTabBarBehavior.fromKey(ThemeSettingsStorage.loadTabBarBehavior())
            ?: legacyTabBarBehavior()
        _tabBarBehavior.value = behavior
        NativeTabBridge.publishTabBarBehavior(behavior)
        _dynamicArtworkBackgroundEnabled.value =
            ThemeSettingsStorage.loadDynamicArtworkBackgroundEnabled() ?: false
        _showCatalogAccentEnabled.value =
            ThemeSettingsStorage.loadShowCatalogAccentEnabled() ?: false
        val appLanguage = AppLanguage.fromCode(ThemeSettingsStorage.loadSelectedAppLanguage())
        ThemeSettingsStorage.applySelectedAppLanguage(appLanguage.code)
        _selectedAppLanguage.value = appLanguage
        _navBarGlowEnabled.value = ThemeSettingsStorage.loadNavBarGlowEnabled() ?: true
        _navBarStyle.value = NavBarStyle.fromKey(ThemeSettingsStorage.loadNavBarStyle())
        _navBarPosition.value = NavBarPosition.fromKey(ThemeSettingsStorage.loadNavBarPosition())
        _navBarDefaultIconColorEnabled.value = ThemeSettingsStorage.loadNavBarDefaultIconColorEnabled() ?: false
        _navBarDefaultIconColor.value = ThemeSettingsStorage.loadNavBarDefaultIconColor()
            ?.let(::parseHexColor) ?: DEFAULT_NAV_BAR_ICON_COLOR
        navBarDefaultIconColorDraftDirty = false
        _navBarLiquidGlassBarEffects.value = decodeLiquidGlassEffectSettings(
            ThemeSettingsStorage.loadNavBarLiquidGlassBarEffects(),
            LiquidGlassEffectDefaults.floatingBar,
            LiquidGlassEffectDefaults.floatingBarLimits,
        )
        _navBarLiquidGlassSelectionEffects.value = decodeLiquidGlassEffectSettings(
            ThemeSettingsStorage.loadNavBarLiquidGlassSelectionEffects(),
            LiquidGlassEffectDefaults.selectionLens,
            LiquidGlassEffectDefaults.selectionLensLimits,
        )
        val storedNavBarGlass = ThemeSettingsStorage.loadNavBarGlass()
        _navBarGlass.value = LiquidGlassStyle.decode(storedNavBarGlass)
        migrateLegacyNavBarGlass(storedNavBarGlass)
        LiquidGlassSettings.userChoice = ThemeSettingsStorage.loadLiquidGlassEffectsEnabled()
        _liquidGlassEffectsEnabled.value = LiquidGlassSettings.enabled
    }

    /** luq API: maps the three-stop luq custom theme onto Mew's two custom accent colors. */
    fun setCustomTheme(colors: CustomThemeColors) {
        ensureLoaded()
        setCustomThemeFirstColor(Color(0xFF000000.toInt() or colors.first))
        setCustomThemeSecondColor(Color(0xFF000000.toInt() or colors.second))
        setTheme(AppTheme.CUSTOM)
    }

    fun setTheme(theme: AppTheme) {
        ensureLoaded()
        if (_selectedTheme.value == theme) return
        _selectedTheme.value = theme
        _selectedThemePreference.value = theme
        ThemeSettingsStorage.saveSelectedTheme(theme.name)
        NativeTabBridge.publishAccentColor(theme.nativeTabAccentHex(_customThemeFirstColor.value))
    }

    fun setCustomThemeFirstColor(color: Color) {
        ensureLoaded()
        if (_customThemeFirstColor.value == color) return
        _customThemeFirstColor.value = color
        ThemeSettingsStorage.saveCustomThemeFirstColor(color.toThemeHex())
        syncCustomThemeColors()
        if (_selectedTheme.value == AppTheme.CUSTOM) {
            NativeTabBridge.publishAccentColor(AppTheme.CUSTOM.nativeTabAccentHex(color))
        }
    }

    fun setCustomThemeSecondColor(color: Color) {
        ensureLoaded()
        if (_customThemeSecondColor.value == color) return
        _customThemeSecondColor.value = color
        ThemeSettingsStorage.saveCustomThemeSecondColor(color.toThemeHex())
        syncCustomThemeColors()
    }

    fun setAmoled(enabled: Boolean) {
        ensureLoaded()
        if (_amoledEnabled.value == enabled) return
        _amoledEnabled.value = enabled
        ThemeSettingsStorage.saveAmoledEnabled(enabled)
    }

    fun setTabBarBehavior(behavior: NuvioTabBarBehavior) {
        ensureLoaded()
        if (_tabBarBehavior.value == behavior) return
        applyTabBarBehavior(behavior)
        ThemeSettingsStorage.saveTabBarBehavior(behavior.key)
        if (!ThemeSettingsStorage.isTabBarBehaviorDeviceLocal()) {
            ThemeSettingsStorage.saveLiquidGlassNativeTabBarEnabled(behavior.isEnabled)
        }
    }

    private fun applyTabBarBehavior(behavior: NuvioTabBarBehavior) {
        _tabBarBehavior.value = behavior
        _liquidGlassNativeTabBarEnabled.value = behavior.isEnabled
        val autoHide = behavior.respondsToScroll
        if (_liquidGlassAutoHideOnScrollEnabled.value != autoHide) {
            _liquidGlassAutoHideOnScrollEnabled.value = autoHide
            ThemeSettingsStorage.saveLiquidGlassAutoHideOnScrollEnabled(autoHide)
        }
        NativeTabBridge.publishTabBarBehavior(behavior)
        NativeTabBridge.publishLiquidGlassEnabled(behavior.isEnabled)
    }

    /** Mew toggle: Liquid Glass native tab bar on/off. */
    fun setLiquidGlassNativeTabBar(enabled: Boolean) {
        ensureLoaded()
        if (_liquidGlassNativeTabBarEnabled.value == enabled) return
        _liquidGlassNativeTabBarEnabled.value = enabled
        ThemeSettingsStorage.saveLiquidGlassNativeTabBarEnabled(enabled)
        NativeTabBridge.publishLiquidGlassEnabled(enabled)
        val behavior = legacyTabBarBehavior()
        _tabBarBehavior.value = behavior
        ThemeSettingsStorage.saveTabBarBehavior(behavior.key)
        NativeTabBridge.publishTabBarBehavior(behavior)
    }

    private fun legacyTabBarBehavior(): NuvioTabBarBehavior = when {
        !_liquidGlassNativeTabBarEnabled.value -> NuvioTabBarBehavior.OFF
        _liquidGlassAutoHideOnScrollEnabled.value -> NuvioTabBarBehavior.AUTO_HIDE
        else -> NuvioTabBarBehavior.STATIC
    }

    private fun syncCustomThemeColors() {
        _customThemeColors.value = mewCustomThemeColors(
            _customThemeFirstColor.value,
            _customThemeSecondColor.value,
        )
    }

    fun setDynamicArtworkBackground(enabled: Boolean) {
        ensureLoaded()
        if (_dynamicArtworkBackgroundEnabled.value == enabled) return
        _dynamicArtworkBackgroundEnabled.value = enabled
        ThemeSettingsStorage.saveDynamicArtworkBackgroundEnabled(enabled)
    }

    fun setShowCatalogAccent(enabled: Boolean) {
        ensureLoaded()
        if (_showCatalogAccentEnabled.value == enabled) return
        _showCatalogAccentEnabled.value = enabled
        ThemeSettingsStorage.saveShowCatalogAccentEnabled(enabled)
    }

    fun setLiquidGlassAutoHideOnScroll(enabled: Boolean) {
        ensureLoaded()
        if (_liquidGlassAutoHideOnScrollEnabled.value == enabled) return
        _liquidGlassAutoHideOnScrollEnabled.value = enabled
        ThemeSettingsStorage.saveLiquidGlassAutoHideOnScrollEnabled(enabled)
        if (_liquidGlassNativeTabBarEnabled.value) {
            val behavior = legacyTabBarBehavior()
            _tabBarBehavior.value = behavior
            ThemeSettingsStorage.saveTabBarBehavior(behavior.key)
            NativeTabBridge.publishTabBarBehavior(behavior)
        }
    }

    fun setAppLanguage(language: AppLanguage) {
        ensureLoaded()
        if (_selectedAppLanguage.value == language) return
        ThemeSettingsStorage.saveSelectedAppLanguage(language.code)
        ThemeSettingsStorage.applySelectedAppLanguage(language.code)
        _selectedAppLanguage.value = language
    }

    fun setNavBarStyle(style: NavBarStyle) {
        ensureLoaded()
        if (_navBarStyle.value == style) return
        _navBarStyle.value = style
        ThemeSettingsStorage.saveNavBarStyle(style.key)
    }

    fun setNavBarPosition(position: NavBarPosition) {
        ensureLoaded()
        if (_navBarPosition.value == position) return
        _navBarPosition.value = position
        ThemeSettingsStorage.saveNavBarPosition(position.key)
    }

    fun setNavBarGlowEnabled(enabled: Boolean) {
        ensureLoaded()
        if (_navBarGlowEnabled.value == enabled) return
        _navBarGlowEnabled.value = enabled
        ThemeSettingsStorage.saveNavBarGlowEnabled(enabled)
    }

    fun setNavBarDefaultIconColorEnabled(enabled: Boolean) {
        ensureLoaded()
        if (_navBarDefaultIconColorEnabled.value == enabled) return
        ThemeSettingsStorage.saveNavBarDefaultIconColorEnabled(enabled)
        _navBarDefaultIconColorEnabled.value = enabled
    }

    internal fun updateNavBarDefaultIconColor(color: Int) {
        ensureLoaded()
        val safeColor = color.coerceIn(0, 0xFFFFFF)
        if (_navBarDefaultIconColor.value == safeColor) return
        _navBarDefaultIconColor.value = safeColor
        navBarDefaultIconColorDraftDirty = true
    }

    internal fun saveNavBarDefaultIconColorSettings() {
        ensureLoaded()
        if (!navBarDefaultIconColorDraftDirty) return
        ThemeSettingsStorage.saveNavBarDefaultIconColor(formatHexColor(_navBarDefaultIconColor.value))
        navBarDefaultIconColorDraftDirty = false
        _navBarDefaultIconColorRevision.value += 1L
    }

    private fun migrateLegacyNavBarGlass(raw: String?) {
        val (bar, lens) = migrateLegacyNavBarGlassEffects(raw) ?: return
        if (ThemeSettingsStorage.loadNavBarLiquidGlassBarEffects() == null) {
            _navBarLiquidGlassBarEffects.value = bar
            ThemeSettingsStorage.saveNavBarLiquidGlassBarEffects(
                encodeLiquidGlassEffectSettings(bar, LiquidGlassEffectDefaults.floatingBar, LiquidGlassEffectDefaults.floatingBarLimits),
            )
        }
        if (ThemeSettingsStorage.loadNavBarLiquidGlassSelectionEffects() == null) {
            _navBarLiquidGlassSelectionEffects.value = lens
            ThemeSettingsStorage.saveNavBarLiquidGlassSelectionEffects(
                encodeLiquidGlassEffectSettings(lens, LiquidGlassEffectDefaults.selectionLens, LiquidGlassEffectDefaults.selectionLensLimits),
            )
        }
        ThemeSettingsStorage.saveNavBarGlass(_navBarGlass.value.encode())
    }

    internal fun updateNavBarLiquidGlassBarEffects(settings: LiquidGlassEffectSettings) {
        ensureLoaded()
        _navBarLiquidGlassBarEffects.value = settings.sanitized(
            LiquidGlassEffectDefaults.floatingBar,
            LiquidGlassEffectDefaults.floatingBarLimits,
        )
    }

    internal fun updateNavBarLiquidGlassSelectionEffects(settings: LiquidGlassEffectSettings) {
        ensureLoaded()
        _navBarLiquidGlassSelectionEffects.value = settings.sanitized(
            LiquidGlassEffectDefaults.selectionLens,
            LiquidGlassEffectDefaults.selectionLensLimits,
        )
    }

    internal fun saveNavBarLiquidGlassEffectSettings() {
        ensureLoaded()
        val barEffects = _navBarLiquidGlassBarEffects.value.sanitized(
            LiquidGlassEffectDefaults.floatingBar,
            LiquidGlassEffectDefaults.floatingBarLimits,
        )
        val selectionEffects = _navBarLiquidGlassSelectionEffects.value.sanitized(
            LiquidGlassEffectDefaults.selectionLens,
            LiquidGlassEffectDefaults.selectionLensLimits,
        )
        val persistedBarEffects = decodeLiquidGlassEffectSettings(
            ThemeSettingsStorage.loadNavBarLiquidGlassBarEffects(),
            LiquidGlassEffectDefaults.floatingBar,
            LiquidGlassEffectDefaults.floatingBarLimits,
        )
        val persistedSelectionEffects = decodeLiquidGlassEffectSettings(
            ThemeSettingsStorage.loadNavBarLiquidGlassSelectionEffects(),
            LiquidGlassEffectDefaults.selectionLens,
            LiquidGlassEffectDefaults.selectionLensLimits,
        )
        var changed = false

        if (barEffects != persistedBarEffects) {
            ThemeSettingsStorage.saveNavBarLiquidGlassBarEffects(
                encodeLiquidGlassEffectSettings(
                    barEffects,
                    LiquidGlassEffectDefaults.floatingBar,
                    LiquidGlassEffectDefaults.floatingBarLimits,
                ),
            )
            changed = true
        }
        if (selectionEffects != persistedSelectionEffects) {
            ThemeSettingsStorage.saveNavBarLiquidGlassSelectionEffects(
                encodeLiquidGlassEffectSettings(
                    selectionEffects,
                    LiquidGlassEffectDefaults.selectionLens,
                    LiquidGlassEffectDefaults.selectionLensLimits,
                ),
            )
            changed = true
        }

        if (changed) _navBarLiquidGlassEffectsRevision.value += 1L
    }

    fun setNavBarGlass(style: LiquidGlassStyle) {
        ensureLoaded()
        if (_navBarGlass.value == style) return
        _navBarGlass.value = style
        ThemeSettingsStorage.saveNavBarGlass(style.encode())
    }

    fun setLiquidGlassEffectsEnabled(enabled: Boolean) {
        ensureLoaded()
        if (_liquidGlassEffectsEnabled.value == enabled) return
        applyLiquidGlassEffectsEnabled(enabled)
        ThemeSettingsStorage.saveLiquidGlassEffectsEnabled(enabled)
    }

    private fun applyLiquidGlassEffectsEnabled(enabled: Boolean) {
        LiquidGlassSettings.userChoice = enabled
        _liquidGlassEffectsEnabled.value = LiquidGlassSettings.enabled
    }

    fun onGlassUiActiveChanged(active: Boolean) {
        if (LiquidGlassSettings.glassUiActive == active) return
        LiquidGlassSettings.glassUiActive = active
        _liquidGlassEffectsEnabled.value = LiquidGlassSettings.enabled
    }

}

private fun AppTheme.nativeTabAccentHex(customFirst: Color = ThemeAccentColor.PINK.color): String =
    ThemeColors.getColorPalette(this, customFirst = customFirst).nativeAccentHex

private fun String?.toAppTheme(): AppTheme = when (this) {
    "AURORA", "LAVENDER" -> AppTheme.MESSENGER
    "PRISM" -> AppTheme.AMETHYST
    "NEBULA", "ORCHID" -> AppTheme.BLOSSOM
    "OPAL", "TWILIGHT" -> AppTheme.LAGOON
    "ULTRAVIOLET" -> AppTheme.SUNSET
    else -> this?.let { stored -> AppTheme.entries.firstOrNull { it.name == stored } } ?: AppTheme.MEW
}

private fun Color.toRgbInt(): Int =
    ((red * 255f).toInt().coerceIn(0, 255) shl 16) or
        ((green * 255f).toInt().coerceIn(0, 255) shl 8) or
        (blue * 255f).toInt().coerceIn(0, 255)

private fun mewCustomThemeColors(first: Color, second: Color): CustomThemeColors =
    CustomThemeColors(first.toRgbInt(), second.toRgbInt(), second.toRgbInt())
