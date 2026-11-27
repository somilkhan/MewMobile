package com.nuvio.app.features.details.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nuvio.app.core.ui.NuvioTokens
import com.nuvio.app.core.ui.ThemeColors
import com.nuvio.app.core.ui.accentBrush
import com.nuvio.app.core.ui.appTheme
import com.nuvio.app.core.ui.glass.ambientLiquidGlass
import com.nuvio.app.core.ui.gradientMask
import com.nuvio.app.core.ui.nuvio
import com.nuvio.app.core.ui.nuvioKeyboardFocusIndicator
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.ai_chat_title
import org.jetbrains.compose.resources.stringResource

/** Height of the AI assistant button; the details list reserves this much space below its last row. */
internal val MewAiAssistantFabHeight: Dp = 48.dp

/** Space the details list must leave at the bottom so the assistant button never covers content. */
internal val MewAiAssistantFabClearance: Dp = MewAiAssistantFabHeight + 18.dp

/**
 * Mew: "Ask Mew" entry point on the details screen.
 *
 * It uses the same liquid-glass surface and accent-gradient icon as the floating
 * navigation bar and the player's skip button, instead of a stock Material FAB with
 * its own elevation and solid primary fill. At the top of the page it shows the label;
 * once the user scrolls into the content it collapses to a round icon button, so it
 * stays out of the way of rows behind it.
 */
@Composable
internal fun MewAiAssistantFab(
    listState: LazyListState,
    visible: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val expanded by remember(listState) {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset < 48
        }
    }
    val label = stringResource(Res.string.ai_chat_title)
    val tokens = MaterialTheme.nuvio
    val palette = ThemeColors.getColorPalette(MaterialTheme.appTheme)
    val useGradient = palette.accentGradient.size >= 2
    val shape = RoundedCornerShape(NuvioTokens.Radius.full)

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(220)) + scaleIn(tween(220), initialScale = 0.85f),
        exit = fadeOut(tween(160)) + scaleOut(tween(160), targetScale = 0.85f),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = MewAiAssistantFabHeight)
                .clip(shape)
                .ambientLiquidGlass(
                    shape = shape,
                    tint = Color(0xFF1C1C1E).copy(alpha = 0.55f),
                    fallback = Color(0xFF1C1C1E).copy(alpha = 0.85f),
                    blurRadius = 18.dp,
                    refractionHeight = 10.dp,
                    refractionAmount = 14.dp,
                )
                .border(NuvioTokens.Space.hairline, Color.White.copy(alpha = 0.14f), shape)
                .nuvioKeyboardFocusIndicator(shape, rememberAsContentFocus = false)
                .clickable(role = Role.Button, onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                // Collapsed: the icon carries the label. Expanded: the visible text does.
                contentDescription = if (expanded) null else label,
                tint = if (useGradient) Color.White else tokens.colors.accent,
                modifier = Modifier
                    .size(22.dp)
                    .then(if (useGradient) Modifier.gradientMask(palette.accentBrush()) else Modifier),
            )
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(tween(180)) + expandHorizontally(tween(220)),
                exit = fadeOut(tween(120)) + shrinkHorizontally(tween(200)),
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = tokens.colors.textPrimary,
                    maxLines = 1,
                    modifier = Modifier.padding(start = 8.dp, end = 4.dp),
                )
            }
        }
    }
}
