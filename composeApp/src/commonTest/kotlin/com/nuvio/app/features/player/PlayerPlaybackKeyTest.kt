package com.nuvio.app.features.player

import kotlin.test.Test
import kotlin.test.assertNotEquals

class PlayerPlaybackKeyTest {
    @Test
    fun episodeIdentityChangesWhenEpisodeChangesEvenWhenSourceUrlIsReused() {
        val first = PlaybackKey("url:https://example.com/shared.mp4", "show:1:1", 1, 1)
        val second = first.copy(videoId = "show:1:2", episodeNumber = 2)
        assertNotEquals(first, second)
    }

    @Test
    fun sourceIdentityChangesWhenTheProviderSourceChangesForTheSameEpisode() {
        val first = PlaybackKey("url:https://example.com/a.mp4", "show:1:1", 1, 1)
        val second = first.copy(sourceIdentity = "url:https://example.com/b.mp4")
        assertNotEquals(first, second)
    }
}
