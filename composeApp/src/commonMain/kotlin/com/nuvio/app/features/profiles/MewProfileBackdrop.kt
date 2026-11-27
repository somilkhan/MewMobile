package com.nuvio.app.features.profiles

// Mew counterpart of luq's ProfileBackgroundBackdrop. luq's version resolves members-only
// catalog backgrounds from Nuvio's supporter backend; Mew's backgrounds (theme presets, bundled
// presets and custom image URLs) are free for every profile, so this resolves those instead.
// Shared by the "Who's watching" screen and the launch overlay so the profile transition keeps
// the exact same backdrop on both sides of the gate.

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import com.nuvio.app.core.ui.appTheme
import nuvio.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalResourceApi::class)
@Composable
fun MewProfileBackdrop(
    profile: NuvioProfile?,
    modifier: Modifier = Modifier,
) {
    val appTheme = MaterialTheme.appTheme
    val effectiveBackground = remember(profile?.backgroundUrl, appTheme) {
        effectiveProfileBackground(profile, appTheme)
    }

    BoxWithConstraints(modifier = modifier) {
        if (effectiveBackground.preset != null) {
            AsyncImage(
                model = Res.getUri(effectiveBackground.preset.resourcePath),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Image(
                painter = painterResource(DefaultProfileBackgroundResource),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
        ProfileRemoteBackgroundImage(
            imageUrl = effectiveBackground.customImageUrl,
            profileIndex = profile?.profileIndex,
            modifier = Modifier.fillMaxSize(),
            targetWidth = maxWidth,
            targetHeight = maxHeight,
        )
        if (effectiveBackground.customImageUrl != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.28f)),
            )
        }
    }
}
