package com.lagradost.cloudstream3.syncproviders

/**
 * Minimal binary-compatibility surface for extensions that reference SyncRepo while loading
 * ordinary stream providers. Full CloudStream account-sync UI/runtime is not hosted by Mew.
 */
class SyncRepo(val api: SyncAPI) {
    val syncIdName: SyncIdName?
        get() = api.syncIdName

    var requireLibraryRefresh: Boolean
        get() = api.requireLibraryRefresh
        set(value) {
            api.requireLibraryRefresh = value
        }
}
