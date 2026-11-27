package com.nuvio.app

// Mew port of luq's AppLoadingContent (AppShellComponents.kt, 1.1.0-beta.3), the surface shown
// while Home paints its first catalog. Kept in its own Mew-prefixed file so future luq merges of
// AppShellComponents.kt don't collide: if luq's version is ever adopted wholesale, delete this
// file and point AppLaunchOverlay at AppLoadingContent instead.
//
// Matches luq's no-origin entrance exactly: the emblem (active profile avatar, or the Mew
// wordmark before a profile exists) drops in from 1.7x / -36px with a medium-bouncy spring, a
// soft accent halo fades up, and a thin accent ring pulses outward once.
// Not ported: luq's tap-origin glide and exit-toward-profile-tab (they need ProfileSelectionScreen
// to report tap positions, which Mew's screen doesn't), and member profile backgrounds.

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.nuvio.app.core.ui.nuvio
import com.nuvio.app.features.profiles.NuvioProfile
import com.nuvio.app.features.settings.AppBrandWordmark
import kotlinx.coroutines.launch
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.app_brand_name
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MewLaunchLoadingContent(
    modifier: Modifier = Modifier,
    profile: NuvioProfile? = null,
) {
    val tokens = MaterialTheme.nuvio
    val emblemScale = remember { Animatable(1.7f) }
    val emblemAlpha = remember { Animatable(0f) }
    val emblemOffsetY = remember { Animatable(-36f) }
    val haloAlpha = remember { Animatable(0f) }
    val ringScale = remember { Animatable(0.7f) }
    val ringAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch { emblemAlpha.animateTo(1f, animationSpec = tween(220, easing = FastOutSlowInEasing)) }
        launch { haloAlpha.animateTo(1f, animationSpec = tween(420, easing = FastOutSlowInEasing)) }
        launch {
            emblemOffsetY.animateTo(
                0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium,
                ),
            )
        }
        launch {
            emblemScale.animateTo(
                1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium,
                ),
            )
        }
        ringScale.snapTo(0.7f)
        ringAlpha.snapTo(0.45f)
        launch { ringScale.animateTo(1.9f, animationSpec = tween(900, easing = LinearOutSlowInEasing)) }
        ringAlpha.animateTo(0f, animationSpec = tween(900))
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(150.dp)
                .graphicsLayer { alpha = haloAlpha.value }
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            tokens.colors.accent.copy(alpha = 0.28f),
                            tokens.colors.accent.copy(alpha = 0f),
                        ),
                    ),
                    CircleShape,
                ),
        )
        Box(
            modifier = Modifier
                .size(110.dp)
                .graphicsLayer {
                    scaleX = ringScale.value
                    scaleY = ringScale.value
                    alpha = ringAlpha.value
                }
                .border(1.5.dp, tokens.colors.accent, CircleShape),
        )
        val emblemModifier = Modifier.graphicsLayer {
            scaleX = emblemScale.value
            scaleY = emblemScale.value
            alpha = emblemAlpha.value
            translationY = emblemOffsetY.value
        }
        if (profile != null) {
            AppLoadingProfileAvatar(profile = profile, modifier = emblemModifier.size(96.dp))
        } else {
            AppBrandWordmark(
                contentDescription = stringResource(Res.string.app_brand_name),
                modifier = emblemModifier.width(160.dp).height(44.dp),
            )
        }
    }
}
