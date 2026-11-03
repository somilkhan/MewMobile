package com.nuvio.app.features.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
internal fun StreamInfoOverlay(
    visible: Boolean,
    provider: String,
    streamTitle: String,
    streamSubtitle: String?,
    engine: AndroidPlaybackEngine?,
    streamType: String?,
    resolution: String?,
    onDismiss: () -> Unit,
) {
    PlayerOverlayScaffold(
        visible = visible,
        onDismiss = onDismiss,
        contentPadding = PaddingValues(horizontal = 32.dp, vertical = 24.dp),
    ) {
        Column(
            modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth(.72f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text("STREAM INFORMATION", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = .6f), fontWeight = FontWeight.Bold)
            InfoLine("Provider", provider)
            InfoLine("Stream", streamTitle)
            InfoLine("Description", streamSubtitle)
            InfoLine("Playback engine", engine?.label)
            InfoLine("Source type", streamType?.takeIf(String::isNotBlank))
            InfoLine("Video resolution", resolution)
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String?) {
    if (value.isNullOrBlank()) return
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        Text(label, color = Color.White.copy(alpha = .58f), style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(1.dp))
        Text(value, color = Color.White, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
    }
}
