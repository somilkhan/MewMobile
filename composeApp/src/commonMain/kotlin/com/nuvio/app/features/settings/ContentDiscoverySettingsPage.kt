package com.nuvio.app.features.settings

import androidx.compose.foundation.lazy.LazyListScope
import com.nuvio.app.core.build.AppFeaturePolicy
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.compose_settings_page_addons
import nuvio.composeapp.generated.resources.compose_settings_page_cloudstream
import nuvio.composeapp.generated.resources.compose_settings_page_plugins
import nuvio.composeapp.generated.resources.settings_content_discovery_addons_description
import nuvio.composeapp.generated.resources.settings_content_discovery_get_started
import nuvio.composeapp.generated.resources.settings_content_discovery_providers_title
import nuvio.composeapp.generated.resources.settings_content_discovery_providers_description
import nuvio.composeapp.generated.resources.settings_content_discovery_addons_description_appstore
import nuvio.composeapp.generated.resources.settings_content_discovery_cloudstream_description
import nuvio.composeapp.generated.resources.settings_content_discovery_plugins_description
import nuvio.composeapp.generated.resources.settings_content_discovery_section_sources
import nuvio.composeapp.generated.resources.settings_tmdb_enable_enrichment
import nuvio.composeapp.generated.resources.settings_tmdb_enable_enrichment_description
import org.jetbrains.compose.resources.stringResource

internal fun LazyListScope.contentDiscoveryContent(
    isTablet: Boolean,
    showPluginsEntry: Boolean,
    showCloudStreamEntry: Boolean,
    tmdbEnabled: Boolean,
    tmdbHasApiKey: Boolean,
    onTmdbEnabledChange: (Boolean) -> Unit,
    onAddonsClick: () -> Unit,
    onPluginsClick: () -> Unit,
    onCloudStreamClick: () -> Unit,
) {
    item {
        SettingsSection(
            title = stringResource(Res.string.settings_content_discovery_section_sources),
            isTablet = isTablet,
        ) {
            SettingsGroup(isTablet = isTablet) {
                SettingsSwitchRow(
                    title = stringResource(Res.string.settings_tmdb_enable_enrichment),
                    description = stringResource(Res.string.settings_tmdb_enable_enrichment_description),
                    checked = tmdbEnabled,
                    enabled = tmdbHasApiKey,
                    isTablet = isTablet,
                    onCheckedChange = onTmdbEnabledChange,
                )
                SettingsGroupDivider(isTablet = isTablet)
                if (showCloudStreamEntry || showPluginsEntry) {
                    SettingsGroupDivider(isTablet = isTablet)
                }
                SettingsNavigationRow(
                    title = stringResource(Res.string.compose_settings_page_addons),
                    description = stringResource(
                        if (AppFeaturePolicy.personalMediaAddonCopyEnabled) {
                            Res.string.settings_content_discovery_addons_description_appstore
                        } else {
                            Res.string.settings_content_discovery_addons_description
                        },
                    ),
                    isTablet = isTablet,
                    onClick = onAddonsClick,
                )
                if (showPluginsEntry) {
                    SettingsGroupDivider(isTablet = isTablet)
                    SettingsNavigationRow(
                        title = stringResource(Res.string.compose_settings_page_plugins),
                        description = stringResource(Res.string.settings_content_discovery_plugins_description),
                        isTablet = isTablet,
                        onClick = onPluginsClick,
                    )
                }
                if (showCloudStreamEntry) {
                    SettingsGroupDivider(isTablet = isTablet)
                    SettingsNavigationRow(
                        title = stringResource(Res.string.compose_settings_page_cloudstream),
                        description = stringResource(Res.string.settings_content_discovery_cloudstream_description),
                        isTablet = isTablet,
                        onClick = onCloudStreamClick,
                    )
                }
            }
        }
    }
}
