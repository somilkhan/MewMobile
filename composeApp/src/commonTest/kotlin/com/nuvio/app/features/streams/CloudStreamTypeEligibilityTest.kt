package com.nuvio.app.features.streams

import com.nuvio.app.features.cloudstream.CloudStreamTvType
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CloudStreamTypeEligibilityTest {
    @Test
    fun nsfwOnlyPluginIsNotQueriedForMoviesOrSeries() {
        val nsfw = setOf(CloudStreamTvType.fromWireValue("NSFW"))
        assertFalse(cloudStreamTypeSupportsRequest(nsfw, "movie"))
        assertFalse(cloudStreamTypeSupportsRequest(nsfw, "series"))
    }

    @Test
    fun audioOnlyPluginIsTreatedAsMusicNotAnyType() {
        val audio = setOf(CloudStreamTvType.fromWireValue("Podcast"))
        assertFalse(cloudStreamTypeSupportsRequest(audio, "movie"))
        assertTrue(cloudStreamTypeSupportsRequest(audio, "live"))
    }

    @Test
    fun othersStillMeansAnyType() {
        assertTrue(cloudStreamTypeSupportsRequest(setOf(CloudStreamTvType.fromWireValue("Others")), "movie"))
    }
}
