package com.nuvio.app.features.library

import com.nuvio.app.features.library.sync.LibrarySyncKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class LibraryStoragePayloadTest {
    @Test
    fun `offline delete remains pending across persisted payload restoration`() {
        val original = LibraryLocalState()
        val initialLoad = original.beginProfileLoad(profileId = 4)
        original.completeProfileLoad(
            token = initialLoad.snapshot.token,
            activeProfileId = 4,
            items = listOf(libraryItem("tt-delete")),
        )

        val deleted = original.remove("tt-delete", "movie").snapshot
        assertEquals(1, deleted.pendingDeleteKeys.size)
        assertTrue(deleted.items.isEmpty())

        val restoredPayload = LibraryStoragePayloadCodec.decode(
            LibraryStoragePayloadCodec.encode(deleted),
        )
        val restored = LibraryLocalState()
        val restoredLoad = restored.beginProfileLoad(profileId = 4)
        val restoredSnapshot = assertNotNull(
            restored.completeProfileLoad(
                token = restoredLoad.snapshot.token,
                activeProfileId = 4,
                items = restoredPayload.items,
                deltaCursorEventId = restoredPayload.deltaCursorEventId,
                deltaInitialized = restoredPayload.deltaInitialized,
                pendingUpsertKeys = restoredPayload.pendingUpsertKeys,
                pendingDeleteKeys = restoredPayload.pendingDeleteKeys,
            ),
        )

        assertTrue(restoredSnapshot.items.isEmpty())
        assertEquals(
            listOf(LibrarySyncKey(contentId = "tt-delete", contentType = "movie")),
            restoredSnapshot.pendingDeleteKeys,
        )
        assertTrue(restoredSnapshot.hasPendingPush)
    }

    private fun libraryItem(id: String) = LibraryItem(
        id = id,
        type = "movie",
        name = id,
        savedAtEpochMs = 1L,
    )
}
