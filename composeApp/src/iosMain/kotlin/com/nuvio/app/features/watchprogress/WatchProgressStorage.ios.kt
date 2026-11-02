package com.nuvio.app.features.watchprogress

import platform.Foundation.NSUserDefaults

actual object WatchProgressStorage {
    private const val payloadKey = "watch_progress_payload"
    private const val pendingDeletesKey = "watch_progress_pending_deletes"

    actual fun loadPayload(profileId: Int): String? =
        NSUserDefaults.standardUserDefaults.stringForKey("${payloadKey}_$profileId")

    actual fun savePayload(profileId: Int, payload: String) {
        NSUserDefaults.standardUserDefaults.setObject(payload, forKey = "${payloadKey}_$profileId")
    }

    actual fun loadPendingDeletes(profileId: Int): String? =
        NSUserDefaults.standardUserDefaults.stringForKey("${pendingDeletesKey}_$profileId")

    actual fun savePendingDeletes(profileId: Int, payload: String) {
        NSUserDefaults.standardUserDefaults.setObject(payload, forKey = "${pendingDeletesKey}_$profileId")
    }
}
