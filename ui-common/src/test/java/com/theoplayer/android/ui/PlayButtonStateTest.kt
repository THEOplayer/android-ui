package com.theoplayer.android.ui

import com.theoplayer.android.api.event.EventListener
import com.theoplayer.android.api.event.EventType
import com.theoplayer.android.api.event.player.PlayerEvent
import com.theoplayer.android.api.event.player.PlayerEventTypes
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.test.runTest
import org.junit.Test

class PlayButtonStateTest {
    @Test
    fun `registers the playback events`() = runTest {
        val mock = MockPlayer()
        launchObserve { PlayButtonState(mock.player).observe() }
        assertEquals(playbackEventTypes - PlayerEventTypes.TIMEUPDATE, mock.playerEventTypes)
    }

    @Test
    fun `a seeked event clears ended`() = runTest {
        val mock = MockPlayer(paused = true, ended = true)
        val state = PlayButtonState(mock.player)
        launchObserve { state.observe() }
        assertTrue(state.ended)

        mock.ended = false
        mock.firePlayerEvent(PlayerEventTypes.SEEKED)
        assertFalse(state.ended)
        assertTrue(state.paused)
    }

    @Test
    fun `seeds state and reacts to playback events`() = runTest {
        val mock = MockPlayer(paused = true, ended = false)
        val state = PlayButtonState(mock.player)
        launchObserve { state.observe() }

        assertTrue(state.paused)
        assertFalse(state.ended)

        mock.paused = false
        mock.firePlayerEvent()
        assertFalse(state.paused)

        mock.ended = true
        mock.firePlayerEvent()
        assertTrue(state.ended)
    }

    @Test
    fun `onClick plays when paused and pauses when playing`() = runTest {
        val mock = MockPlayer(paused = true)
        val state = PlayButtonState(mock.player)
        launchObserve { state.observe() }

        state.onClick()
        verify(exactly = 1) { mock.player.play() }

        mock.firePlayerEvent()
        state.onClick()
        verify(exactly = 1) { mock.player.pause() }
    }

    @Test
    fun `dispose removes all listeners`() = runTest {
        val mock = MockPlayer()
        val state = PlayButtonState(mock.player)
        val job = launchObserve { state.observe() }
        val added = mock.playerListeners.toList()
        assertTrue(added.isNotEmpty())

        job.cancelAndJoin()
        assertTrue(mock.playerListeners.isEmpty())
        verify(exactly = added.size) {
            mock.player.removeEventListener(any<EventType<out PlayerEvent<*>>>(), any())
        }
    }

    @Test
    fun `shares one listener instance and removes it for every type`() = runTest {
        val mock = MockPlayer()
        val state = PlayButtonState(mock.player)
        val job = launchObserve { state.observe() }

        val added = mock.playerListeners.toList()
        val listener = added.first().second
        assertTrue(added.all { it.second === listener })

        job.cancelAndJoin()
        added.forEach { (type, _) ->
            @Suppress("UNCHECKED_CAST")
            verify {
                mock.player.removeEventListener(
                    type as EventType<PlayerEvent<*>>,
                    listener as EventListener<PlayerEvent<*>>
                )
            }
        }
    }

    @Test
    fun `null player gives defaults and no listeners`() = runTest {
        val state = PlayButtonState(null)
        val job = launchObserve { state.observe() }
        assertTrue(state.paused)
        state.onClick()
        job.cancelAndJoin()
    }
}
