package com.theoplayer.android.ui

import com.theoplayer.android.api.player.track.texttrack.TextTrackStyle
import com.theoplayer.android.ui.util.THEOplayerGlobalExt
import com.theoplayer.android.ui.util.Version
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class TextTrackMarginsUpdaterTest {
    private var styleMarginTop = 0
    private var styleMarginBottom = 0
    private val style = mockk<TextTrackStyle> {
        val newMargin = slot<Int>()
        every { marginTop } answers { styleMarginTop }
        every { marginTop = capture(newMargin) } answers { styleMarginTop = newMargin.captured }
        every { marginBottom } answers { styleMarginBottom }
        every { marginBottom = capture(newMargin) } answers { styleMarginBottom = newMargin.captured }
    }
    private val updater = TextTrackMarginsUpdater()

    @Before
    fun setUp() {
        mockPlayerVersion("11.0.0")
    }

    @After
    fun tearDown() {
        clearMocks(THEOplayerGlobalExt)
    }

    @Test
    fun `WHEN chrome is visible THEN moves text tracks out of the way`() {
        updater.update(style, ChromeMargins(top = 10, bottom = 25))

        assertEquals(10, styleMarginTop)
        assertEquals(25, styleMarginBottom)
    }

    @Test
    fun `WHEN chrome is hidden THEN restores the margins`() {
        updater.update(style, ChromeMargins(top = 10, bottom = 25))
        updater.update(style, ChromeMargins())

        assertEquals(0, styleMarginTop)
        assertEquals(0, styleMarginBottom)
    }

    @Test
    fun `GIVEN a larger user margin WHEN chrome is visible THEN keeps the user margin`() {
        styleMarginBottom = 30

        updater.update(style, ChromeMargins(bottom = 25))

        assertEquals(30, styleMarginBottom)
    }

    @Test
    fun `GIVEN a smaller user margin WHEN chrome is hidden THEN restores the user margin`() {
        styleMarginBottom = 5

        updater.update(style, ChromeMargins(bottom = 25))
        assertEquals(25, styleMarginBottom)

        updater.update(style, ChromeMargins())
        assertEquals(5, styleMarginBottom)
    }

    @Test
    fun `WHEN the user changes the margin while chrome is visible THEN restores the new user margin`() {
        updater.update(style, ChromeMargins(bottom = 25))
        styleMarginBottom = 10

        updater.update(style, ChromeMargins())

        assertEquals(10, styleMarginBottom)
    }

    @Test
    fun `GIVEN THEOplayer before 9_5_0 WHEN chrome is visible THEN only sets the top margin`() {
        mockPlayerVersion("9.4.0")

        updater.update(style, ChromeMargins(top = 10, bottom = 25))

        assertEquals(10, styleMarginTop)
        assertEquals(0, styleMarginBottom)
    }

    private fun mockPlayerVersion(version: String) {
        mockkObject(THEOplayerGlobalExt)
        every { THEOplayerGlobalExt.version } returns Version.parse(version)
    }
}
