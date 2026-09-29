package com.nuvio.app.features.player

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PlayerLaunchStoreTest {

    @Test
    fun storesAndRemovesLaunchesById() {
        val launch = PlayerLaunch(
            profileId = 1,
            title = "Title",
            sourceUrl = "https://example.com/video.m3u8?token=a/b:c",
            externalSubtitles = emptyList(),
            streamTitle = "Source",
            providerName = "Provider",
            parentMetaId = "tt1234567",
            parentMetaType = "movie",
        )

        val launchId = PlayerLaunchStore.put(launch)

        assertEquals(launch, PlayerLaunchStore.get(launchId))

        PlayerLaunchStore.update(launchId) { it.copy(initialPositionMs = 442_000L) }
        assertEquals(442_000L, PlayerLaunchStore.get(launchId)?.initialPositionMs)

        PlayerLaunchStore.remove(launchId)

        assertNull(PlayerLaunchStore.get(launchId))
    }
}
