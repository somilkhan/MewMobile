package com.nuvio.app.features.streams

// Mew-only StreamsScreen additions (called from `// Mew hook` lines in StreamsScreen.kt):
//  - direct auto-play overlay (Mew keeps its own app shell, so upstream StreamLoadingScreen is not used)
//  - episode synopsis panel under the episode hero

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.nuvio.app.core.ui.NuvioLoadingIndicator
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.details_show_less
import nuvio.composeapp.generated.resources.details_show_more
import nuvio.composeapp.generated.resources.streams_finding_source
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MewDirectAutoPlayOverlay(
    uiState: StreamsUiState,
    type: String,
    videoId: String,
    seasonNumber: Int?,
    episodeNumber: Int?,
    manualSelection: Boolean,
    title: String,
    logo: String?,
) {
    val expectedRequestToken = remember(type, videoId, seasonNumber, episodeNumber, manualSelection) {
        StreamsRepository.requestToken(
            type = type,
            videoId = videoId,
            season = seasonNumber,
            episode = episodeNumber,
            manualSelection = manualSelection,
        )
    }
    val showDirectAutoPlayOverlay =
        !manualSelection &&
            uiState.requestToken == expectedRequestToken &&
            uiState.showDirectAutoPlayOverlay
    var autoPlayOverlayLogoLoadError by remember(logo) { mutableStateOf(false) }
    val autoPlayOverlayLogoUrl = logo?.takeIf { it.isNotBlank() }

    AnimatedVisibility(
        visible = showDirectAutoPlayOverlay,
        enter = fadeIn(animationSpec = tween(250)),
        exit = fadeOut(animationSpec = tween(200)),
        modifier = Modifier.fillMaxSize(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f)),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (autoPlayOverlayLogoUrl != null && !autoPlayOverlayLogoLoadError) {
                    AsyncImage(
                        model = autoPlayOverlayLogoUrl,
                        contentDescription = title,
                        modifier = Modifier
                            .height(48.dp),
                        contentScale = ContentScale.Fit,
                        onError = { autoPlayOverlayLogoLoadError = true },
                    )
                } else if (title.isNotBlank()) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 24.dp),
                    )
                }
                NuvioLoadingIndicator(
                    modifier = Modifier.size(32.dp),
                    color = Color.White,
                )
                Text(
                    text = uiState.overlayMessage
                        ?: stringResource(Res.string.streams_finding_source),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f),
                )
            }
        }
    }
}


@Composable
internal fun MewEpisodeSynopsisPanel(
    overview: String?,
    modifier: Modifier = Modifier,
) {
    val summary = overview?.trim()?.takeIf { it.isNotBlank() } ?: return
    var expanded by remember(summary) { mutableStateOf(false) }
    val canExpand = summary.length > 180
    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.78f),
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.56f),
                    ),
                ),
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                shape = shape,
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(
                modifier = Modifier
                    .width(30.dp)
                    .height(2.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.78f)),
            )
            Text(
                text = summary,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    fontWeight = FontWeight.Medium,
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.92f),
                maxLines = if (expanded) Int.MAX_VALUE else 4,
                overflow = TextOverflow.Ellipsis,
            )
            if (canExpand) {
                Text(
                    text = stringResource(if (expanded) Res.string.details_show_less else Res.string.details_show_more),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.End)
                        .clip(RoundedCornerShape(999.dp))
                        .clickable { expanded = !expanded }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
        }
    }
}
