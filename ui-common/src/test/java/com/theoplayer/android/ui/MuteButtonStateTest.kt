package com.theoplayer.android.ui

import com.theoplayer.android.api.event.player.PlayerEventTypes
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.test.runTest
import org.junit.Test

class MuteButtonStateTest {
    @Test
    fun `registers only the volume change event`() = runTest {
        val mock = MockPlayer()
        launchObserve { MuteButtonState(mock.player).observe() }
        assertEquals(setOf(PlayerEventTypes.VOLUMECHANGE), mock.playerEventTypes)
    }

    @Test
    fun `seeds muted and updates on volume change`() = runTest {
        val mock = MockPlayer(muted = true)
        val state = MuteButtonState(mock.player)
        launchObserve { state.observe() }
        assertTrue(state.muted)

        mock.muted = false
        mock.firePlayerEvent(PlayerEventTypes.VOLUMECHANGE)
        assertFalse(state.muted)
    }

    @Test
    fun `onClick toggles player and state`() = runTest {
        val mock = MockPlayer(muted = false)
        val state = MuteButtonState(mock.player)
        launchObserve { state.observe() }

        state.onClick()
        verify { mock.player.isMuted = true }
        assertTrue(state.muted)
    }

    @Test
    fun `null player gives defaults and onClick does nothing`() = runTest {
        val state = MuteButtonState(null)
        val job = launchObserve { state.observe() }
        assertFalse(state.muted)
        state.onClick()
        job.cancelAndJoin()
    }
}
