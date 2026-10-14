package com.theoplayer.android.ui

import com.theoplayer.android.api.event.player.PlayerEventTypes
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Test

class PlaybackRateStateTest {
    @Test
    fun `registers only the ratechange event`() {
        val mock = MockPlayer()
        PlaybackRateState(mock.player).observe()
        assertEquals(setOf(PlayerEventTypes.RATECHANGE), mock.playerEventTypes)
    }

    @Test
    fun `seeds rate and updates on ratechange`() {
        val mock = MockPlayer(playbackRate = 1.5)
        val state = PlaybackRateState(mock.player)
        state.observe()
        assertEquals(1.5, state.playbackRate, 0.0)

        mock.playbackRate = 2.0
        mock.firePlayerEvent(PlayerEventTypes.RATECHANGE)
        assertEquals(2.0, state.playbackRate, 0.0)
    }

    @Test
    fun `select sets player rate and state`() {
        val mock = MockPlayer()
        val state = PlaybackRateState(mock.player)
        state.observe()

        state.select(2.0)
        verify { mock.player.playbackRate = 2.0 }
        assertEquals(2.0, state.playbackRate, 0.0)
    }

    @Test
    fun `null player gives defaults and no listeners`() {
        val state = PlaybackRateState(null)
        state.observe()
        assertEquals(1.0, state.playbackRate, 0.0)
        state.select(2.0)
        state.dispose()
    }
}
