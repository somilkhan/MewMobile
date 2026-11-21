package com.nuvio.app.features.settings

import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.nuvio.app.core.ui.AppTheme
import com.nuvio.app.core.ui.ThemeColors

/**
 * Product-facing Mew brand lockup.
 *
 * The old implementation appended the upstream "Supporter" membership badge to
 * the application wordmark on the profile selector. That was inherited UI, not
 * product branding, and made every user appear to be a supporter.
 */
@Composable
internal fun MemberBrandWordmark(
    height: Dp,
    modifier: Modifier = Modifier,
) {
    AppBrandWordmark(
        modifier = modifier.height(height),
        contentDescription = "Mew",
    )
}

internal fun memberBrandWordmarkColors(
    theme: AppTheme,
    animatedThemeColors: List<Color>? = null,
): List<Color> {
    val palette = ThemeColors.getColorPalette(theme)
    val themeColors = animatedThemeColors
        ?.takeIf { it.size >= 2 }
        ?: palette.accentGradient.takeIf { it.size >= 2 }
        ?: listOf(palette.secondaryVariant, palette.secondary, palette.focusRing)
    return themeColors + themeColors.first()
}
