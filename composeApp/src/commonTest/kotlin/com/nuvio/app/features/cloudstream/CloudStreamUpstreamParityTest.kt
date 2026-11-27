package com.nuvio.app.features.cloudstream

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Behaviour pinned to upstream recloudstream/cloudstream semantics. */
class CloudStreamUpstreamParityTest {
    @Test
    fun linkHeadersWinOverRefererLikeGetAllHeaders() {
        val merged = mergeCloudStreamRequestHeaders(
            urlHeaders = emptyMap(),
            linkHeaders = mapOf("referer" to "https://explicit.example/"),
            referer = "https://link-field.example/",
        )
        assertEquals(mapOf("referer" to "https://explicit.example/"), merged)
    }

    @Test
    fun refererFieldIsAddedOnlyWhenNoHeaderCarriesIt() {
        val merged = mergeCloudStreamRequestHeaders(
            urlHeaders = emptyMap(),
            linkHeaders = mapOf("User-Agent" to "UA"),
            referer = "https://link-field.example/",
        )
        assertEquals("https://link-field.example/", merged["Referer"])
        assertEquals("UA", merged["User-Agent"])
        assertEquals(2, merged.size)

        val blank = mergeCloudStreamRequestHeaders(emptyMap(), mapOf("User-Agent" to "UA"), referer = " ")
        assertEquals(mapOf("User-Agent" to "UA"), blank)
    }

    @Test
    fun headerNamesAreMergedCaseInsensitively() {
        val merged = mergeCloudStreamRequestHeaders(
            urlHeaders = mapOf("origin" to "https://pipe.example", "Referer" to "https://pipe.example/"),
            linkHeaders = mapOf("Origin" to "https://link.example"),
            referer = "https://link-field.example/",
        )
        assertEquals(
            mapOf("Origin" to "https://link.example", "Referer" to "https://link-field.example/"),
            merged,
        )
    }

    @Test
    fun streamItemDoesNotSendDuplicateRefererHeaders() {
        val stream = CloudStreamPlaybackSource(
            name = "CDN",
            url = "https://video.example/file.mp4",
            referer = "https://provider.example/",
            headers = mapOf("referer" to "https://embed.example/"),
        ).toStreamItem(providerId = "https://example.com/repo.json#P", providerName = "P")

        val request = stream.behaviorHints.proxyHeaders?.request.orEmpty()
        assertEquals(1, request.keys.count { it.equals("referer", ignoreCase = true) })
        assertEquals("https://embed.example/", request["referer"])
    }

    @Test
    fun tvTypeWireValuesCoverEveryUpstreamTvType() {
        assertEquals(CloudStreamTvType.Nsfw, CloudStreamTvType.fromWireValue("NSFW"))
        assertEquals(CloudStreamTvType.Music, CloudStreamTvType.fromWireValue("AudioBook"))
        assertEquals(CloudStreamTvType.Music, CloudStreamTvType.fromWireValue("Audio"))
        assertEquals(CloudStreamTvType.Music, CloudStreamTvType.fromWireValue("Podcast"))
        assertEquals(CloudStreamTvType.Ova, CloudStreamTvType.fromWireValue("OVA"))
        assertEquals(CloudStreamTvType.AsianDrama, CloudStreamTvType.fromWireValue("AsianDrama"))
        assertEquals(CloudStreamTvType.Other, CloudStreamTvType.fromWireValue("Others"))
        assertEquals(CloudStreamTvType.Other, CloudStreamTvType.fromWireValue("CustomMedia"))
        assertEquals(CloudStreamTvType.Other, CloudStreamTvType.fromWireValue("Video"))
        assertEquals("other", CloudStreamTvType.Nsfw.nuvioType)
    }

    @Test
    fun invalidDataMatchesApiRepository() {
        assertTrue(isInvalidCloudStreamData(""))
        assertTrue(isInvalidCloudStreamData("[]"))
        assertTrue(isInvalidCloudStreamData("about:blank"))
        assertFalse(isInvalidCloudStreamData("https://provider.example/watch/1"))
        assertFalse(isInvalidCloudStreamData("[{\"id\":1}]"))
    }

    @Test
    fun unnumberedSeriesEpisodesAreNumberedByIndexAfterUpstreamSort() {
        val episodes = listOf(
            CloudStreamEpisode("a", "", season = 1),
            CloudStreamEpisode("b", "", season = 1),
            CloudStreamEpisode("c", "", season = 1),
        )
        val numbered = fillMissingCloudStreamEpisodeNumbers(episodes, sortBySeasonAndEpisode = true)
        assertEquals(listOf("a" to 1, "b" to 2, "c" to 3), numbered.map { it.data to it.episode })
    }

    @Test
    fun unnumberedAnimeEpisodesKeepProviderOrder() {
        val episodes = listOf(
            CloudStreamEpisode("x", "", season = 2),
            CloudStreamEpisode("y", "", season = 1),
        )
        val numbered = fillMissingCloudStreamEpisodeNumbers(episodes, sortBySeasonAndEpisode = false)
        assertEquals(listOf("x" to 1, "y" to 2), numbered.map { it.data to it.episode })
    }

    @Test
    fun explicitEpisodeNumbersAreNeverRewritten() {
        val episodes = listOf(
            CloudStreamEpisode("a", "A", season = 1, episode = 5),
            CloudStreamEpisode("b", "B", season = 1, episode = 7),
        )
        assertEquals(episodes, fillMissingCloudStreamEpisodeNumbers(episodes, sortBySeasonAndEpisode = true))
    }

    @Test
    fun externalIdsIgnoreBlankValues() {
        assertNull(normalizeCloudStreamExternalId(null))
        assertNull(normalizeCloudStreamExternalId("  "))
        assertNull(normalizeCloudStreamExternalId("null"))
        assertEquals("tt0111161", normalizeCloudStreamExternalId(" tt0111161 "))
    }

    @Test
    fun episodeRuntimeReachesMetaVideo() {
        val details = CloudStreamLoadItem(
            providerId = "https://example.com/repo.json#S",
            data = "series:1",
            name = "Series",
            type = CloudStreamTvType.TvSeries,
            episodes = listOf(CloudStreamEpisode("e1", "One", season = 1, episode = 1, runtimeMinutes = 42)),
        ).toMetaDetails()
        assertEquals(42, details.videos.single().runtime)
    }

    @Test
    fun csRepoDeepLinkWorksWithAndWithoutQuestionMark() {
        val expected = "https://example.com/repo.json"
        assertEquals(expected, normalizeCloudStreamRepositoryUrl("https://cs.repo/?https://example.com/repo.json"))
        assertEquals(expected, normalizeCloudStreamRepositoryUrl("https://cs.repo/example.com/repo.json"))
        assertEquals(expected, normalizeCloudStreamRepositoryUrl("cloudstreamrepo://example.com/repo.json"))
    }
}
