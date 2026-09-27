package com.nuvio.app.features.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal expect fun AppBrandWordmark(
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    icon: AppIconOption? = null,
)
