package com.nuvio.app.features.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nuvio.app.core.ui.nuvio
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.mewmobile_onboarding_logo
import org.jetbrains.compose.resources.painterResource

private const val TmdbManifest = "https://tmdb.elfhosted.com/manifest.json"
private const val ProviderManifest = "https://raw.githubusercontent.com/phisher98/phisher-nuvio-providers/refs/heads/main/manifest.json"

@Composable
internal fun MewOnboardingScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var page by rememberSaveable { mutableIntStateOf(0) }
    val uriHandler = LocalUriHandler.current
    val clipboard = LocalClipboardManager.current
    val pages = 3

    Surface(
        modifier = modifier.fillMaxSize(),
        color = androidx.compose.material3.MaterialTheme.nuvio.colors.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Image(
                painter = painterResource(Res.drawable.mewmobile_onboarding_logo),
                contentDescription = "MewMobile",
                modifier = Modifier.fillMaxWidth().height(96.dp),
            )
            Text(
                "MewMobile",
                style = androidx.compose.material3.MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
            )
            Text(
                when (page) {
                    0 -> "Your media client. Connect the sources and integrations you want."
                    1 -> "MewMobile keeps integrations external. CloudStream provides providers; addons provide catalogs and metadata."
                    else -> "Get started"
                },
                style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )

            when (page) {
                0 -> InfoCard(
                    title = "How MewMobile works",
                    body = "Nothing is bundled into an all-in-one source system. You choose which external addons, CloudStream plugins and repositories to use.",
                )
                1 -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SetupCard(
                        title = "Metadata",
                        subtitle = "TMDB is optional metadata and catalog enrichment.",
                        button = "Open TMDB",
                        onAction = { uriHandler.openUri(TmdbManifest) },
                        onCopy = { clipboard.setText(AnnotatedString(TmdbManifest)) },
                    )
                    SetupCard(
                        title = "Providers",
                        subtitle = "CloudStream repositories provide provider and source integrations. TMDB is not required.",
                        button = "Open repository",
                        onAction = { uriHandler.openUri(ProviderManifest) },
                        onCopy = { clipboard.setText(AnnotatedString(ProviderManifest)) },
                    )
                }
                else -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    InfoCard(
                        title = "Where to manage sources",
                        body = "Open Discovery to manage Addons, Plugins and CloudStream repositories. Install only the integrations you want.",
                    )
                    InfoCard(
                        title = "One important distinction",
                        body = "TMDB = optional metadata enrichment. CloudStream = provider/content compatibility. Stremio/Nuvio addons remain external.",
                    )
                }
            }

            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (page > 0) {
                    OutlinedButton(
                        onClick = { page-- },
                        modifier = Modifier.weight(1f),
                    ) { Text("Back") }
                }
                Button(
                    onClick = {
                        if (page == pages - 1) onComplete() else page++
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(if (page == pages - 1) "Start using MewMobile" else "Continue")
                }
            }
            if (page == 2) {
                OutlinedButton(
                    onClick = onComplete,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Skip guide") }
            }
        }
    }
}

@Composable
private fun InfoCard(title: String, body: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        tonalElevation = 3.dp,
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, fontWeight = FontWeight.Bold)
            Text(body)
        }
    }
}

@Composable
private fun SetupCard(
    title: String,
    subtitle: String,
    button: String,
    onAction: () -> Unit,
    onCopy: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        tonalElevation = 3.dp,
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, fontWeight = FontWeight.Bold)
            Text(subtitle)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onAction) {
                    Icon(Icons.Default.OpenInNew, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(button)
                }
                OutlinedButton(onClick = onCopy) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Copy")
                }
            }
        }
    }
}
