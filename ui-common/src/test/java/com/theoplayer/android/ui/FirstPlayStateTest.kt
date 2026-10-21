package com.theoplayer.android.ui

import com.theoplayer.android.api.event.player.PlayerEventTypes
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FirstPlayStateTest {
    @Test
    fun `seeds false when paused with empty played`() = runTest {
        val mock = MockPlayer(paused = true, playedLength = 0)
        val state = FirstPlayState(mock.player)
        launchObserve { state.observe() }
        assertFalse(state.firstPlay)
    }

    @Test
    fun `seeds true when paused with non-empty played`() = runTest {
        val mock = MockPlayer(paused = true, playedLength = 1)
        val state = FirstPlayState(mock.player)
        launchObserve { state.observe() }
        assertTrue(state.firstPlay)
    }

    @Test
    fun `seeds true when playing`() = runTest {
        val mock = MockPlayer(paused = false)
        val state = FirstPlayState(mock.player)
        launchObserve { state.observe() }
        assertTrue(state.firstPlay)
    }

    @Test
    fun `play sets firstPlay`() = runTest {
        val mock = MockPlayer(paused = true)
        val state = FirstPlayState(mock.player)
        launchObserve { state.observe() }

        mock.paused = false
        mock.firePlayerEvent(PlayerEventTypes.PLAY)
        assertTrue(state.firstPlay)
    }

    @Test
    fun `sourcechange resets firstPlay while paused`() = runTest {
        val mock = MockPlayer(paused = false)
        val state = FirstPlayState(mock.player)
        launchObserve { state.observe() }
        assertTrue(state.firstPlay)

        mock.paused = true
        mock.playedLength = 0
        mock.firePlayerEvent(PlayerEventTypes.SOURCECHANGE)
        assertFalse(state.firstPlay)
    }

    @Test
    fun `sourcechange keeps firstPlay while playing`() = runTest {
        val mock = MockPlayer(paused = false)
        val state = FirstPlayState(mock.player)
        launchObserve { state.observe() }
        assertTrue(state.firstPlay)

        mock.firePlayerEvent(PlayerEventTypes.SOURCECHANGE)
        assertTrue(state.firstPlay)
    }
}
