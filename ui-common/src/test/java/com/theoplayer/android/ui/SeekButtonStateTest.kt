package com.theoplayer.android.ui

import com.theoplayer.android.api.event.player.PlayerEventTypes
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class SeekButtonStateTest {
    @Test
    fun `registers the playback and ad events`() {
        val mock = MockPlayer()
        SeekButtonState(mock.player, seekOffset = 10).observe()
        assertEquals(playbackEventTypes, mock.playerEventTypes)
        assertEquals(adEventTypes, mock.adEventTypes)
    }

    @Test
    fun `onClick seeks by offset and clamps to duration`() {
        val mock = MockPlayer(currentTime = 95.0, duration = 100.0)
        val state = SeekButtonState(mock.player, seekOffset = 10)
        state.observe()

        state.onClick()
        verify { mock.player.currentTime = 100.0 }
    }

    @Test
    fun `onClick clamps negative seek to zero`() {
        val mock = MockPlayer(currentTime = 5.0, duration = 100.0)
        val state = SeekButtonState(mock.player, seekOffset = -10)
        state.observe()

        state.onClick()
        verify { mock.player.currentTime = 0.0 }
    }

    @Test
    fun `onClick does nothing for streams without duration`() {
        val mock = MockPlayer(currentTime = 50.0, duration = Double.NaN)
        val state = SeekButtonState(mock.player, seekOffset = 10)
        state.observe()

        state.onClick()
        verify(exactly = 0) { mock.player.currentTime = any() }
    }

    @Test
    fun `disabled during ads`() {
        val mock = MockPlayer(adPlaying = true)
        val state = SeekButtonState(mock.player, seekOffset = 10)
        state.observe()

        assertFalse(state.enabled)
    }

    @Test
    fun `null player is disabled and onClick does nothing`() {
        val state = SeekButtonState(null, seekOffset = 10)
        state.observe()
        assertFalse(state.enabled)
        state.onClick()
        state.dispose()
    }
}
