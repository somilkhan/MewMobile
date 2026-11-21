package com.nuvio.app.features.cloudstream

internal actual object CloudStreamPlatformRuntime {
    // Downloaded .cs3 DEX is intentionally not executed. Only reviewed providers
    // compiled into fullCommonMain (currently KickTR) can run on this build.
    actual val supportsAndroidDex: Boolean = false

    actual fun initialize(context: Any?) = Unit
    actual fun validatePackage(bytes: ByteArray, inspection: CloudStreamPackageInspection) = Unit
    actual suspend fun provider(plugin: CloudStreamPluginItem): CloudStreamProvider? = null
    actual suspend fun registerNativeRepository(url: String, name: String, iconUrl: String?) = Unit
    actual suspend fun removeNativeRepository(url: String) = Unit
    actual fun unload(pluginId: String) = Unit
    actual fun clear() = Unit
}
