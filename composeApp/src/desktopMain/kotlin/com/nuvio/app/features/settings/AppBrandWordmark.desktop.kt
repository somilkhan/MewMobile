package com.nuvio.app.features.settings

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.mew_brand_logo
import org.jetbrains.compose.resources.painterResource

@Composable
actual fun AppBrandWordmark(
    modifier: Modifier,
    contentDescription: String?,
    icon: AppIconOption?,
) {
    Image(
        painter = painterResource(Res.drawable.mew_brand_logo),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Fit,
    )
}
