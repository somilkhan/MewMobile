package com.nuvio.app.features.profiles

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import com.nuvio.app.core.diagnostics.ProfileBackgroundStatus
import com.nuvio.app.core.diagnostics.RuntimeDiagnostics
import com.nuvio.app.core.sync.AppForegroundMonitor
import kotlinx.coroutines.flow.collect

@Composable
fun ProfileRemoteBackgroundImage(
    imageUrl: String?,
    profileIndex: Int?,
    modifier: Modifier = Modifier,
    alpha: Float = 1f,
    contentScale: ContentScale = ContentScale.Crop,
    targetWidth: Dp? = null,
    targetHeight: Dp? = null,
) {
    if (imageUrl == null) {
        LaunchedEffect(Unit) {
            RuntimeDiagnostics.updateProfileBackgroundStatus(ProfileBackgroundStatus.Preset)
        }
        return
    }

    var foregroundGeneration by remember(imageUrl) { mutableIntStateOf(0) }
    val context = LocalPlatformContext.current
    val density = LocalDensity.current
    val targetWidthPx = targetWidth?.let { with(density) { it.roundToPx() } }?.coerceAtLeast(1)
    val targetHeightPx = targetHeight?.let { with(density) { it.roundToPx() } }?.coerceAtLeast(1)
    val memoryCacheKey = "profile-background:${profileIndex ?: 0}:$imageUrl:${targetWidthPx ?: 0}x${targetHeightPx ?: 0}:${foregroundGeneration % 2}"
    val previousMemoryCacheKey = "profile-background:${profileIndex ?: 0}:$imageUrl:${targetWidthPx ?: 0}x${targetHeightPx ?: 0}:${(foregroundGeneration + 1) % 2}"
    val request = remember(context, imageUrl, foregroundGeneration) {
        ImageRequest.Builder(context)
            .data(imageUrl)
            .memoryCacheKey(memoryCacheKey)
            .diskCacheKey("profile-background:$imageUrl")
            .apply {
                if (targetWidthPx != null && targetHeightPx != null) {
                    size(targetWidthPx, targetHeightPx)
                }
            }
            .apply {
                if (foregroundGeneration > 0) placeholderMemoryCacheKey(previousMemoryCacheKey)
            }
            .build()
    }

    LaunchedEffect(imageUrl) {
        RuntimeDiagnostics.updateProfileBackgroundStatus(ProfileBackgroundStatus.Loading)
        AppForegroundMonitor.events().collect {
            foregroundGeneration += 1
            RuntimeDiagnostics.recordProfileBackgroundRetry()
        }
    }

    AsyncImage(
        model = request,
        contentDescription = null,
        modifier = modifier.alpha(alpha.coerceIn(0f, 1f)),
        contentScale = contentScale,
        onSuccess = {
            RuntimeDiagnostics.updateProfileBackgroundStatus(ProfileBackgroundStatus.Loaded)
        },
        onError = {
            RuntimeDiagnostics.updateProfileBackgroundStatus(ProfileBackgroundStatus.Failed)
        },
    )
}