package com.nuvio.app.features.addons

internal const val PendingAddonRemoteSyncMarker = "__mew_internal_pending_remote_sync__"

internal fun Map<String, String>.withAddonRemoteSyncPending(pending: Boolean): Map<String, String> =
    toMutableMap().apply {
        if (pending) {
            this[PendingAddonRemoteSyncMarker] = "1"
        } else {
            remove(PendingAddonRemoteSyncMarker)
        }
    }

internal fun Map<String, String>.hasAddonRemoteSyncPending(): Boolean =
    containsKey(PendingAddonRemoteSyncMarker)
