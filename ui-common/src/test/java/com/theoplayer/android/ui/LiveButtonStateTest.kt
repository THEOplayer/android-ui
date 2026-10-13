package com.theoplayer.android.ui

import com.theoplayer.android.api.event.player.PlayerEventTypes
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LiveButtonStateTest {
    @Test
    fun `registers the playback events`() {
        val mock = MockPlayer()
        LiveButtonState(mock.player, liveThreshold = 10.0).observe()
        assertEquals(
            playbackEventTypes + PlayerEventTypes.DURATIONCHANGE,
            mock.playerEventTypes
        )
    }

    @Test
    fun `visible for live but not for vod`() {
        val live = MockPlayer(duration = Double.POSITIVE_INFINITY, seekableRanges = listOf(0.0 to 30.0))
        val vod = MockPlayer(duration = 100.0)

        assertTrue(LiveButtonState(live.player, liveThreshold = 10.0).apply { observe() }.visible)
        assertFalse(LiveButtonState(vod.player, liveThreshold = 10.0).apply { observe() }.visible)
    }

    @Test
    fun `isLive at threshold boundary`() {
        val mock = MockPlayer(
            paused = false,
            duration = Double.POSITIVE_INFINITY,
            currentTime = 20.0,
            seekableRanges = listOf(0.0 to 30.0)
        )
        val atThreshold = LiveButtonState(mock.player, liveThreshold = 10.0)
        atThreshold.observe()
        assertTrue(atThreshold.isLive)

        mock.currentTime = 19.9
        val behindThreshold = LiveButtonState(mock.player, liveThreshold = 10.0)
        behindThreshold.observe()
        assertFalse(behindThreshold.isLive)
    }

    @Test
    fun `onClick seeks to live and plays`() {
        val mock = MockPlayer(duration = Double.POSITIVE_INFINITY)
        val state = LiveButtonState(mock.player, liveThreshold = 10.0)
        state.observe()

        state.onClick()
        verify { mock.player.currentTime = Double.POSITIVE_INFINITY }
        verify(exactly = 1) { mock.player.play() }
    }
}
