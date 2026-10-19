package com.nuvio.app.features.tracking

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TrackingAnimeIdMapperTest {
    @Test
    fun `parses supported anime episode references`() {
        assertEquals(
            TrackingAnimeVideoRef(source = "mal", id = 123L, episode = 4),
            parseTrackingAnimeVideoId("mal:123:4"),
        )
        assertEquals(
            TrackingAnimeVideoRef(source = "anilist", id = 987L, episode = 12),
            parseTrackingAnimeVideoId("ANILIST:987:12"),
        )
    }

    @Test
    fun `rejects malformed and non anime video references`() {
        assertNull(parseTrackingAnimeVideoId(null))
        assertNull(parseTrackingAnimeVideoId("imdb:tt1234567:2"))
        assertNull(parseTrackingAnimeVideoId("mal:0:2"))
        assertNull(parseTrackingAnimeVideoId("mal:123:0"))
        assertNull(parseTrackingAnimeVideoId("mal:abc:2"))
    }
}
