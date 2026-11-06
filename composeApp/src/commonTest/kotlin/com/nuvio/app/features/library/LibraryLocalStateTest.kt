package com.nuvio.app.features.library

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LibraryLocalStateTest {
    @Test
    fun stalePushCompletionDoesNotClearNewerOfflineMutations() {
        val state = LibraryLocalState()
        val loaded = state.beginProfileLoad(profileId = 5)
        val snapshot = state.completeProfileLoad(
            token = loaded.snapshot.token,
            activeProfileId = 5,
            items = emptyList(),
        )!!

        val first = item("tt001", "First")
        val second = item("tt002", "Second")
        val pushSnapshot = state.upsert(first)
        state.upsert(second)

        assertNull(state.markPushCompleted(pushSnapshot))
        val current = state.snapshot()
        assertEquals(setOf("tt001", "tt002"), current.pendingUpsertKeys.map { it.contentId }.toSet())
        assertEquals(setOf("tt001", "tt002"), current.items.map { it.id }.toSet())
        assertTrue(current.hasPendingPush)
        assertTrue(state.isCurrent(snapshot.token).not())
    }

    @Test
    fun successfulPushCompletionClearsOnlyTheCurrentPendingBatch() {
        val state = LibraryLocalState()
        val load = state.beginProfileLoad(profileId = 3)
        state.completeProfileLoad(
            token = load.snapshot.token,
            activeProfileId = 3,
            items = emptyList(),
        )
        val pushSnapshot = state.upsert(item("tt010", "Saved"))

        val completed = state.markPushCompleted(pushSnapshot)
        assertEquals(emptyList(), completed?.pendingUpsertKeys)
        assertEquals(emptyList(), completed?.pendingDeleteKeys)
        assertFalse(completed!!.hasPendingPush)
        assertEquals("tt010", completed.items.single().id)
    }

    @Test
    fun profileSwitchInvalidatesOldPushSnapshotAndRetainsNewProfileIsolation() {
        val state = LibraryLocalState()
        val firstLoad = state.beginProfileLoad(profileId = 1)
        state.completeProfileLoad(firstLoad.snapshot.token, 1, emptyList())
        val oldPush = state.upsert(item("tt100", "Profile One"))

        val secondLoad = state.beginProfileLoad(profileId = 2)
        state.completeProfileLoad(secondLoad.snapshot.token, 2, emptyList())

        assertNull(state.markPushCompleted(oldPush))
        assertEquals(2, state.snapshot().token.profileId)
        assertTrue(state.snapshot().items.isEmpty())
        assertTrue(state.snapshot().pendingUpsertKeys.isEmpty())
    }

    private fun item(id: String, name: String) = LibraryItem(
        id = id,
        type = "movie",
        name = name,
        savedAtEpochMs = 1L,
    )
}
