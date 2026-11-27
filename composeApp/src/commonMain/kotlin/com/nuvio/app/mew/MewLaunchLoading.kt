package com.nuvio.app

// Mew port of luq's AppLoadingContent (AppShellComponents.kt, 1.1.0-beta.3), the surface shown
// while Home paints its first catalog. Kept in its own Mew-prefixed file so future luq merges of
// AppShellComponents.kt don't collide: if luq's version is ever adopted wholesale, delete this
// file and point AppLaunchOverlay at AppLoadingContent instead.
//
// Matches luq's AppLoadingContent behaviour:
// - No origin (cold start, auto-picked profile): the emblem (profile avatar, or the Mew wordmark
//   before a profile exists) drops in from 1.7x / -36px with a medium-bouncy spring, a soft accent
//   halo fades up, and a thin accent ring pulses outward once.
// - Tap origin (picked on "Who's watching"): the emblem starts exactly where the tapped avatar was
//   and glides to the centre over 550ms, then the ring pulses.
// - Exit toward the profile tab: once Home is ready the emblem shrinks into the real profile tab
//   avatar (Compose tab bars report it via mewReportProfileTabCenter; iOS via NativeTabBridge),
//   falling back to the bottom-right corner like luq.

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nuvio.app.core.ui.NativeTabBridge
import com.nuvio.app.core.ui.nuvio
import com.nuvio.app.features.profiles.NuvioProfile
import com.nuvio.app.features.settings.AppBrandWordmark
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.app_brand_name
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MewLaunchLoadingContent(
    modifier: Modifier = Modifier,
    profile: NuvioProfile? = null,
    entryOrigin: Offset? = null,
    exitTowardProfileTab: Boolean = false,
    onExitFinished: () -> Unit = {},
) {
    val tokens = MaterialTheme.nuvio
    val emblemScale = remember { Animatable(1.7f) }
    val emblemAlpha = remember { Animatable(0f) }
    val emblemOffsetX = remember { Animatable(0f) }
    val emblemOffsetY = remember { Animatable(-36f) }
    val haloAlpha = remember { Animatable(0f) }
    val ringScale = remember { Animatable(0.7f) }
    val ringAlpha = remember { Animatable(0f) }
    val density = LocalDensity.current
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    var containerCenterInWindow by remember { mutableStateOf<Offset?>(null) }
    val nativeProfileTabFrame by NativeTabBridge.profileTabIconFrame.collectAsStateWithLifecycle()
    val composeProfileTabCenter by MewProfileTransition.profileTabCenterInWindow

    suspend fun CoroutineScope.pulseRing() {
        ringScale.snapTo(0.7f)
        ringAlpha.snapTo(0.45f)
        launch { ringScale.animateTo(1.9f, animationSpec = tween(900, easing = LinearOutSlowInEasing)) }
        ringAlpha.animateTo(0f, animationSpec = tween(900))
    }

    LaunchedEffect(entryOrigin) {
        if (entryOrigin != null) {
            // The emblem *is* the avatar that was just tapped: fully visible at that spot, then
            // it glides straight to the centre (luq's tap-to-centre transition).
            val center = snapshotFlow { containerCenterInWindow }.first { it != null }!!
            emblemAlpha.snapTo(1f)
            emblemScale.snapTo(1f)
            emblemOffsetX.snapTo(entryOrigin.x - center.x)
            emblemOffsetY.snapTo(entryOrigin.y - center.y)
            val travelDuration = 550
            launch { emblemOffsetX.animateTo(0f, animationSpec = tween(travelDuration, easing = FastOutSlowInEasing)) }
            launch { haloAlpha.animateTo(1f, animationSpec = tween(260, delayMillis = travelDuration - 160, easing = FastOutSlowInEasing)) }
            emblemOffsetY.animateTo(0f, animationSpec = tween(travelDuration, easing = FastOutSlowInEasing))
            pulseRing()
        } else {
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
            pulseRing()
        }
    }

    LaunchedEffect(exitTowardProfileTab) {
        if (!exitTowardProfileTab) return@LaunchedEffect
        val center = containerCenterInWindow
        val composeTarget = composeProfileTabCenter
        val nativeFrame = nativeProfileTabFrame
        val targetX: Float
        val targetY: Float
        when {
            center != null && composeTarget != null -> {
                targetX = composeTarget.x - center.x
                targetY = composeTarget.y - center.y
            }
            nativeFrame != null -> {
                val iconCenterXPx = with(density) { (nativeFrame.xDp + nativeFrame.widthDp / 2f).dp.toPx() }
                val iconCenterYPx = with(density) { (nativeFrame.yDp + nativeFrame.heightDp / 2f).dp.toPx() }
                targetX = iconCenterXPx - containerSize.width / 2f
                targetY = iconCenterYPx - containerSize.height / 2f
            }
            else -> {
                val marginPx = with(density) { 24.dp.toPx() }
                targetX = (containerSize.width / 2f - marginPx).coerceAtLeast(0f)
                targetY = (containerSize.height / 2f - marginPx).coerceAtLeast(0f)
            }
        }
        // Stays opaque for almost the whole trip and only dissolves at the very end, so it reads
        // as the avatar shrinking into the real tab icon rather than fading away.
        val travelDuration = 550
        launch { haloAlpha.animateTo(0f, animationSpec = tween(220)) }
        launch { ringAlpha.animateTo(0f, animationSpec = tween(200)) }
        launch { emblemOffsetX.animateTo(targetX, animationSpec = tween(travelDuration, easing = FastOutSlowInEasing)) }
        launch { emblemScale.animateTo(0.22f, animationSpec = tween(travelDuration, easing = FastOutSlowInEasing)) }
        launch { emblemAlpha.animateTo(0f, animationSpec = tween(180, delayMillis = travelDuration - 180)) }
        emblemOffsetY.animateTo(targetY, animationSpec = tween(travelDuration, easing = FastOutSlowInEasing))
        onExitFinished()
    }

    Box(
        modifier = modifier.onGloballyPositioned { coordinates ->
            containerSize = coordinates.size
            containerCenterInWindow = coordinates.positionInWindow() + Offset(
                coordinates.size.width / 2f,
                coordinates.size.height / 2f,
            )
        },
        contentAlignment = Alignment.Center,
    ) {
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
            translationX = emblemOffsetX.value
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
