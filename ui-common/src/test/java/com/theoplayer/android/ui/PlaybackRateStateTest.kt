package com.theoplayer.android.ui

import com.theoplayer.android.api.event.player.PlayerEventTypes
import io.mockk.verify
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class PlaybackRateStateTest {
    @Test
    fun `registers only the ratechange event`() = runTest {
        val mock = MockPlayer()
        launchObserve { PlaybackRateState(mock.player).observe() }
        assertEquals(setOf(PlayerEventTypes.RATECHANGE), mock.playerEventTypes)
    }

    @Test
    fun `seeds rate and updates on ratechange`() = runTest {
        val mock = MockPlayer(playbackRate = 1.5)
        val state = PlaybackRateState(mock.player)
        launchObserve { state.observe() }
        assertEquals(1.5, state.playbackRate, 0.0)

        mock.playbackRate = 2.0
        mock.firePlayerEvent(PlayerEventTypes.RATECHANGE)
        assertEquals(2.0, state.playbackRate, 0.0)
    }

    @Test
    fun `select sets player rate and state`() = runTest {
        val mock = MockPlayer()
        val state = PlaybackRateState(mock.player)
        launchObserve { state.observe() }

        state.select(2.0)
        verify { mock.player.playbackRate = 2.0 }
        assertEquals(2.0, state.playbackRate, 0.0)
    }

    @Test
    fun `null player gives defaults and no listeners`() = runTest {
        val state = PlaybackRateState(null)
        val job = launchObserve { state.observe() }
        assertEquals(1.0, state.playbackRate, 0.0)
        state.select(2.0)
        job.cancelAndJoin()
    }
}
