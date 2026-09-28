package com.nuvio.app.features.settings

import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

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
