package com.theoplayer.android.ui.util

import com.theoplayer.android.api.player.track.texttrack.TextTrackStyle
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CompatTest {
    private val style = mockk<TextTrackStyle>(relaxed = true)

    @After
    fun tearDown() {
        clearMocks(THEOplayerGlobalExt)
    }

    @Test
    fun `GIVEN THEOplayer 9_5_0 WHEN getting marginBottomCompat THEN returns the bottom margin`() {
        mockPlayerVersion("9.5.0")
        every { style.marginBottom } returns 20

        assertEquals(20, style.marginBottomCompat)
    }

    @Test
    fun `GIVEN THEOplayer 9_5_0 WHEN setting marginBottomCompat THEN sets the bottom margin`() {
        mockPlayerVersion("9.5.0")

        style.marginBottomCompat = 20

        verify { style.marginBottom = 20 }
    }

    @Test
    fun `GIVEN THEOplayer 9_4_0 WHEN getting marginBottomCompat THEN returns null`() {
        mockPlayerVersion("9.4.0")

        assertNull(style.marginBottomCompat)
        verify(exactly = 0) { style.marginBottom }
    }

    @Test
    fun `GIVEN THEOplayer 9_4_0 WHEN setting marginBottomCompat THEN does nothing`() {
        mockPlayerVersion("9.4.0")

        style.marginBottomCompat = 20

        verify(exactly = 0) { style.marginBottom = any() }
    }

    private fun mockPlayerVersion(version: String) {
        mockkObject(THEOplayerGlobalExt)
        every { THEOplayerGlobalExt.version } returns Version.parse(version)
    }
}
