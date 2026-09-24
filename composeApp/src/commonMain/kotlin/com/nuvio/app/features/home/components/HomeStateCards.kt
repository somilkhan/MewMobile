package com.nuvio.app.features.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nuvio.app.core.ui.NuvioPrimaryButton
import com.nuvio.app.core.ui.NuvioSurfaceCard

@Composable
fun HomeEmptyStateCard(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
) {
    NuvioSurfaceCard(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (actionLabel != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(16.dp))
            NuvioPrimaryButton(
                text = actionLabel,
                onClick = onActionClick,
            )
        }
    }
}

@Composable
fun HomeSourceSetupCard(
    modifier: Modifier = Modifier,
    onOpenAddons: () -> Unit,
    onOpenPlugins: () -> Unit,
) {
    NuvioSurfaceCard(modifier = modifier) {
        Text(
            text = "Get MewMobile ready",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Add metadata first, then add CloudStream providers. These sources stay optional and user-controlled.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(16.dp))
        SourceSetupRow(
            step = "01",
            title = "Metadata",
            detail = "TMDB addon manifest",
            url = "https://tmdb.elfhosted.com/manifest.json",
            actionLabel = "Open Addons",
            onClick = onOpenAddons,
        )
        Spacer(modifier = Modifier.height(12.dp))
        SourceSetupRow(
            step = "02",
            title = "CloudStream providers",
            detail = "Phisher98 provider repository",
            url = "https://raw.githubusercontent.com/phisher98/phisher-nuvio-providers/refs/heads/main/manifest.json",
            actionLabel = "Open Plugins",
            onClick = onOpenPlugins,
        )
    }
}

@Composable
private fun SourceSetupRow(
    step: String,
    title: String,
    detail: String,
    url: String,
    actionLabel: String,
    onClick: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = step,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        NuvioPrimaryButton(
            text = actionLabel,
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
        )
        }
        Text(
            text = url,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
