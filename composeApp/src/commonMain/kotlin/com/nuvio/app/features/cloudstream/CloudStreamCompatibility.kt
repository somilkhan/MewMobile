package com.nuvio.app.features.cloudstream

/**
 * TMDB metadata and CloudStream provider discovery are independent features.
 * The TMDB setting must never gate CloudStream provider availability.
 */
@Suppress("UNUSED_PARAMETER")
internal fun isCloudStreamDiscoveryAllowed(tmdbEnabled: Boolean): Boolean = true

object CloudStreamCompatibilityResolver {
    private val crossPlatformAdapters = mapOf(
        "https://raw.githubusercontent.com/Kraptor123/cs-kraptor/master/repo.json#KickTR" to "kick-tr-v1",
    )

    fun resolve(
        metadata: CloudStreamPluginMetadata,
        supportsAndroidDex: Boolean = false,
    ): CloudStreamCompatibility {
        val adapterId = crossPlatformAdapters[metadata.id.value]
        return when {
            adapterId != null -> {
            CloudStreamCompatibility(
                runtimeKind = CloudStreamRuntimeKind.PrecompiledCrossPlatformAdapter,
                platformSupport = CloudStreamPlatformSupport.AndroidAndIos,
                adapterId = adapterId,
                reason = "This provider has a reviewed cross-platform adapter compiled into Mew.",
            )
            }
            supportsAndroidDex -> {
                CloudStreamCompatibility(
                    runtimeKind = CloudStreamRuntimeKind.AndroidDex,
                    platformSupport = CloudStreamPlatformSupport.AndroidOnly,
                    reason = "This build explicitly enables Android DEX execution for this standard CloudStream .cs3 package.",
                )
            }
            else -> {
            CloudStreamCompatibility(
                runtimeKind = CloudStreamRuntimeKind.UnsupportedAndroidDex,
                platformSupport = CloudStreamPlatformSupport.Unsupported,
                reason = "This build does not execute downloaded Android DEX packages. Only reviewed providers compiled into Mew are supported.",
            )
            }
        }
    }
}

fun sortCloudStreamEpisodes(episodes: List<CloudStreamEpisode>): List<CloudStreamEpisode> =
    episodes.sortedWith(
        compareBy<CloudStreamEpisode>(
            { it.season ?: Int.MAX_VALUE },
            { it.episode ?: Int.MAX_VALUE },
            { it.name.lowercase() },
        ),
    )
