package com.theoplayer.android.ui

import com.theoplayer.android.api.event.player.PlayerEventTypes
import org.junit.Assert.assertEquals
import org.junit.Test

class VideoSizeStateTest {
    @Test
    fun `registers resize sourcechange and error`() {
        val mock = MockPlayer()
        VideoSizeState(mock.player).observe()
        assertEquals(
            setOf(PlayerEventTypes.RESIZE, PlayerEventTypes.SOURCECHANGE, PlayerEventTypes.ERROR),
            mock.playerEventTypes
        )
    }

    @Test
    fun `seeds size and updates on resize`() {
        val mock = MockPlayer(videoWidth = 1920, videoHeight = 1080)
        val state = VideoSizeState(mock.player)
        state.observe()
        assertEquals(1920, state.videoWidth)
        assertEquals(1080, state.videoHeight)

        mock.videoWidth = 1280
        mock.videoHeight = 720
        mock.firePlayerEvent(PlayerEventTypes.RESIZE)
        assertEquals(1280, state.videoWidth)
        assertEquals(720, state.videoHeight)
    }
}
