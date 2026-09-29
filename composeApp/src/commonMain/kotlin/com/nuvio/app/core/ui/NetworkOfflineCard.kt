package com.nuvio.app.core.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.nuvio.app.core.network.NetworkCondition
import com.nuvio.app.core.network.messageForEmptyState
import com.nuvio.app.core.network.titleForEmptyState
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.action_retry
import nuvio.composeapp.generated.resources.mew_mascot_dizzy_cat
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun NuvioNetworkOfflineCard(
    condition: NetworkCondition,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
) {
    val tokens = MaterialTheme.nuvio
    NuvioSurfaceCard(modifier = modifier) {
        Image(
            painter = painterResource(Res.drawable.mew_mascot_dizzy_cat),
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            contentScale = ContentScale.Fit,
        )
        Spacer(modifier = Modifier.height(tokens.spacing.controlGap))
        Text(
            text = condition.titleForEmptyState(),
            style = MaterialTheme.typography.titleLarge,
            color = tokens.colors.textPrimary,
        )
        Spacer(modifier = Modifier.height(tokens.spacing.controlGap))
        Text(
            text = condition.messageForEmptyState(),
            style = MaterialTheme.typography.bodyLarge,
            color = tokens.colors.textMuted,
        )
        if (onRetry != null) {
            Spacer(modifier = Modifier.height(tokens.spacing.screenHorizontal))
            NuvioPrimaryButton(
                text = stringResource(Res.string.action_retry),
                onClick = onRetry,
            )
        }
    }
}
