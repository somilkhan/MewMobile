package com.nuvio.app.features.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import nuvio.composeapp.generated.resources.Res

/**
 * Renders the Mew wordmark through Coil instead of Compose Resources' painterResource.
 *
 * Compose Multiplatform's SVG painter is not supported on Android. The wordmark is
 * intentionally kept as the real Mew SVG asset, while Coil's SVG decoder handles
 * the file on Android and the other supported targets.
 */
@Composable
internal fun AppBrandWordmark(
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    icon: AppIconOption? = null,
) {
    AsyncImage(
        model = Res.getUri("drawable/mew_brand_logo.svg"),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Fit,
    )
}
