package com.nuvio.app.features.settings

// Mew-only additions to the upstream (Luqman) settings root and About page.
// Upstream files only call MewSettingsRootExtras(...) and mewSettingsFooterContent(...).

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.People
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nuvio.app.core.build.AppVersionConfig
import nuvio.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

internal const val MewRepositoryUrl = "https://github.com/somilkhan/MewMobile"

/** Mew rows appended to an upstream settings-root category group. */
@Composable
internal fun MewSettingsRootExtras(
    group: SettingsCategory,
    isTablet: Boolean,
    onPageClick: (SettingsPage) -> Unit,
    onDownloadsClick: (() -> Unit)?,
) {
    val uriHandler = LocalUriHandler.current
    when (group) {
        SettingsCategory.Watch -> if (onDownloadsClick != null) {
            SettingsGroupDivider(isTablet = isTablet)
            SettingsNavigationRow(
                title = stringResource(Res.string.compose_settings_root_downloads_title),
                description = stringResource(Res.string.compose_settings_root_downloads_description),
                icon = Icons.Rounded.Download,
                isTablet = isTablet,
                onClick = onDownloadsClick,
            )
        }
        SettingsCategory.System -> {
            val enhancedSettings by remember {
                NuvioEnhancedSettingsRepository.ensureLoaded()
                NuvioEnhancedSettingsRepository.uiState
            }.collectAsStateWithLifecycle()
            SettingsGroupDivider(isTablet = isTablet)
            SettingsNavigationRow(
                title = stringResource(Res.string.settings_nuvio_enhanced_title),
                description = stringResource(Res.string.settings_nuvio_enhanced_description),
                icon = Icons.Rounded.AutoAwesome,
                isTablet = isTablet,
                highlighted = enhancedSettings.hasNewFeatures,
                onClick = { onPageClick(SettingsPage.NuvioEnhanced) },
            )
            SettingsGroupDivider(isTablet = isTablet)
            SettingsNavigationRow(
                title = stringResource(Res.string.nuvio_enhanced_footer_telegram),
                description = stringResource(Res.string.settings_nuvio_enhanced_telegram_description),
                icon = Icons.Rounded.People,
                isTablet = isTablet,
                onClick = { uriHandler.openUri(MewTelegramUrl) },
            )
        }
        else -> Unit
    }
}

/** Mew footer: wordmark, repository link, Mew version and the upstream Nuvio version it tracks. */
internal fun LazyListScope.mewSettingsFooterContent(isTablet: Boolean) {
    item {
        val uriHandler = LocalUriHandler.current
        androidx.compose.foundation.layout.Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = if (isTablet) 20.dp else 16.dp),
        ) {
            MemberBrandWordmark(
                height = if (isTablet) 30.dp else 26.dp,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            Text(
                text = "github.com/somilkhan/MewMobile",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .clickable { uriHandler.openUri(MewRepositoryUrl) },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(
                    Res.string.compose_about_version_format,
                    AppVersionConfig.VERSION_NAME,
                    AppVersionConfig.VERSION_CODE,
                ),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(
                    Res.string.compose_about_nuvio_version_format,
                    AppVersionConfig.NUVIO_VERSION_NAME,
                ),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}
