package com.nuvio.app.features.watchprogress

internal fun mergePendingWatchProgressDeletes(
    existing: Collection<WatchProgressEntry>,
    deleted: Collection<WatchProgressEntry>,
    keyOf: (WatchProgressEntry) -> String,
): List<WatchProgressEntry> {
    val merged = existing.associateByTo(linkedMapOf<String, WatchProgressEntry>(), keyOf)
    deleted.forEach { entry ->
        val key = keyOf(entry)
        val current = merged[key]
        if (current == null || entry.lastUpdatedEpochMs >= current.lastUpdatedEpochMs) {
            merged[key] = entry
        }
    }
    return merged.values.toList()
}

internal fun acknowledgePendingWatchProgressDeletes(
    pending: Collection<WatchProgressEntry>,
    pushed: Collection<WatchProgressEntry>,
    keyOf: (WatchProgressEntry) -> String,
): List<WatchProgressEntry> {
    val pushedByKey = pushed.associateBy(keyOf)
    return pending.filterNot { entry ->
        val acknowledged = pushedByKey[keyOf(entry)]
        acknowledged != null && entry.lastUpdatedEpochMs <= acknowledged.lastUpdatedEpochMs
    }
}
