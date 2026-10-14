package com.theoplayer.android.ui

import com.theoplayer.android.api.event.player.PlayerEventTypes
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FirstPlayStateTest {
    @Test
    fun `seeds false when paused with empty played`() {
        val mock = MockPlayer(paused = true, playedLength = 0)
        val state = FirstPlayState(mock.player)
        state.observe()
        assertFalse(state.firstPlay)
    }

    @Test
    fun `seeds true when paused with non-empty played`() {
        val mock = MockPlayer(paused = true, playedLength = 1)
        val state = FirstPlayState(mock.player)
        state.observe()
        assertTrue(state.firstPlay)
    }

    @Test
    fun `seeds true when playing`() {
        val mock = MockPlayer(paused = false)
        val state = FirstPlayState(mock.player)
        state.observe()
        assertTrue(state.firstPlay)
    }

    @Test
    fun `play sets firstPlay`() {
        val mock = MockPlayer(paused = true)
        val state = FirstPlayState(mock.player)
        state.observe()

        mock.paused = false
        mock.firePlayerEvent(PlayerEventTypes.PLAY)
        assertTrue(state.firstPlay)
    }

    @Test
    fun `sourcechange resets firstPlay while paused`() {
        val mock = MockPlayer(paused = false)
        val state = FirstPlayState(mock.player)
        state.observe()
        assertTrue(state.firstPlay)

        mock.paused = true
        mock.playedLength = 0
        mock.firePlayerEvent(PlayerEventTypes.SOURCECHANGE)
        assertFalse(state.firstPlay)
    }

    @Test
    fun `sourcechange keeps firstPlay while playing`() {
        val mock = MockPlayer(paused = false)
        val state = FirstPlayState(mock.player)
        state.observe()
        assertTrue(state.firstPlay)

        mock.firePlayerEvent(PlayerEventTypes.SOURCECHANGE)
        assertTrue(state.firstPlay)
    }
}
