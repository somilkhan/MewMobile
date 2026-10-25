package com.lagradost.cloudstream3.syncproviders

/**
 * Compatibility marker for CloudStream extensions that reference the optional sync API.
 * Mew does not currently expose CloudStream's in-app sync-provider UI.
 */
open class SyncAPI {
    open var requireLibraryRefresh: Boolean = true
    open val syncIdName: SyncIdName? = null
}
