package com.nuvio.app.features.home

// Mew: CloudStream Home provider picker (the "CS3" button shown when no add-on
// catalogs are enabled). Kept out of the upstream HomeScreen.kt (same package) to
// keep luq merges conflict-free. See MEW_INTEGRATION.md.

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nuvio.app.features.cloudstream.CloudStreamPluginItem

@Composable
internal fun BoxScope.CloudStreamHomeProviderPicker(
    runnableCloudStreamProviders: List<CloudStreamPluginItem>,
    selectedCloudStreamProviderId: String?,
    cloudStreamProviderMenuExpanded: Boolean,
    onCloudStreamProviderMenuExpandedChange: (Boolean) -> Unit,
    nativeBottomNavigationOverlayHeight: Dp,
) {
    Box(
        modifier = Modifier
            .align(androidx.compose.ui.Alignment.BottomEnd)
            .padding(
                end = 16.dp,
                bottom = nativeBottomNavigationOverlayHeight + 16.dp,
            ),
    ) {
        DropdownMenu(
            expanded = cloudStreamProviderMenuExpanded,
            onDismissRequest = { onCloudStreamProviderMenuExpandedChange(false) },
        ) {
            DropdownMenuItem(
                text = { Text("All extensions") },
                onClick = {
                    onCloudStreamProviderMenuExpandedChange(false)
                    HomeRepository.setSelectedCloudStreamProvider(null)
                },
            )
            runnableCloudStreamProviders.forEach { provider ->
                DropdownMenuItem(
                    text = { Text(provider.metadata.name) },
                    onClick = {
                        onCloudStreamProviderMenuExpandedChange(false)
                        HomeRepository.setSelectedCloudStreamProvider(provider.metadata.id.value)
                    },
                )
            }
        }
        val selectedCloudStreamProvider = runnableCloudStreamProviders.firstOrNull {
            it.metadata.id.value == selectedCloudStreamProviderId
        }
        val sourceLabel = selectedCloudStreamProvider?.metadata?.name ?: "All sources"

        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
                )
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.30f),
                    shape = CircleShape,
                )
                .clickable { onCloudStreamProviderMenuExpandedChange(true) },
            contentAlignment = androidx.compose.ui.Alignment.Center,
        ) {
            Text(
                text = "CS3",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.94f),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
            )
        }
    }
}
