package com.nuvio.app.features.home.components

import com.nuvio.app.features.watchprogress.ContinueWatchingSectionStyle
import kotlin.test.Test
import kotlin.test.assertEquals

class HomeHeroSectionTest {

    @Test
    fun `mobile hero height follows viewport height when provided`() {
        val layout = homeHeroLayout(
            maxWidthDp = 390f,
            viewportHeightDp = 844f,
        )

        assertEquals(false, layout.isTablet)
        assertEquals(692.08f, layout.heroHeight.value, 0.001f)
    }

    @Test
    fun `tablet hero height remains width driven even with viewport height`() {
        val layout = homeHeroLayout(
            maxWidthDp = 840f,
            viewportHeightDp = 1200f,
        )

        assertEquals(true, layout.isTablet)
        assertEquals(386.4f, layout.heroHeight.value, 0.001f)
    }

    @Test
    fun `mobile hero height leaves room for continue watching card section`() {
        val viewportHeight = 844f
        val continueWatchingLayout = rememberContinueWatchingLayout(maxWidthDp = 390f)
        val continueWatchingHeight = continueWatchingSectionHeightEstimate(
            style = ContinueWatchingSectionStyle.Card,
            layout = continueWatchingLayout,
            basePosterWidthDp = 110,
            showHeaderAccent = true,
        )
        val reserveHeight = continueWatchingHeroViewportReserveHeight(
            style = ContinueWatchingSectionStyle.Card,
            layout = continueWatchingLayout,
            basePosterWidthDp = 110,
            showHeaderAccent = true,
        )
        val layout = homeHeroLayout(
            maxWidthDp = 390f,
            viewportHeightDp = viewportHeight,
            mobileBelowSectionHeightHintDp = reserveHeight.value,
        )

        assertEquals(24f, viewportHeight - layout.heroHeight.value - continueWatchingHeight.value, 0.001f)
    }

    @Test
    fun `mobile hero can shrink below default minimum to fit short viewport`() {
        val layout = homeHeroLayout(
            maxWidthDp = 390f,
            viewportHeightDp = 568f,
            mobileBelowSectionHeightHintDp = 300f,
        )

        assertEquals(false, layout.isTablet)
        assertEquals(268f, layout.heroHeight.value, 0.001f)
    }
    @Test
    fun `original TMDB title mode prefers the title selected for the home item`() {
        assertEquals(
            "Original title",
            selectHomeHeroDisplayTitle(
                itemTitle = "Original title",
                detailTitle = "Localized title",
                preferItemTitle = true,
            ),
        )
        assertEquals(
            "Localized title",
            selectHomeHeroDisplayTitle(
                itemTitle = "Original title",
                detailTitle = "Localized title",
                preferItemTitle = false,
            ),
        )
    }

    @Test
    fun `hero title selection falls back when preferred source is blank`() {
        assertEquals(
            "Fallback title",
            selectHomeHeroDisplayTitle(
                itemTitle = " ",
                detailTitle = "Fallback title",
                preferItemTitle = true,
            ),
        )
        assertEquals(
            "Fallback title",
            selectHomeHeroDisplayTitle(
                itemTitle = "Fallback title",
                detailTitle = " ",
                preferItemTitle = false,
            ),
        )
    }

    @Test
    fun `original title mode prefers matching hero artwork logo`() {
        assertEquals(
            "original-logo.png",
            selectHomeHeroLogoUrl("original-logo.png", "localized-logo.png", preferItemLogo = true),
        )
        assertEquals(
            "localized-logo.png",
            selectHomeHeroLogoUrl("original-logo.png", "localized-logo.png", preferItemLogo = false),
        )
        assertEquals(
            "detail-logo.png",
            selectHomeHeroLogoUrl(" ", "detail-logo.png", preferItemLogo = true),
        )
    }

}
