package com.theoplayer.android.ui

import com.theoplayer.android.api.error.THEOplayerException
import com.theoplayer.android.api.event.player.PlayerEventTypes
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ErrorStateTest {
    @Test
    fun `registers error and sourcechange`() {
        val mock = MockPlayer()
        ErrorState(mock.player).observe()
        assertEquals(setOf(PlayerEventTypes.ERROR, PlayerEventTypes.SOURCECHANGE), mock.playerEventTypes)
    }

    @Test
    fun `error event sets error and sourcechange clears it`() {
        val exception = mockk<THEOplayerException>()
        val mock = MockPlayer()
        val state = ErrorState(mock.player)
        state.observe()
        assertEquals(null, state.error)

        mock.errorObject = exception
        mock.firePlayerEvent(PlayerEventTypes.ERROR)
        assertEquals(exception, state.error)

        mock.firePlayerEvent(PlayerEventTypes.SOURCECHANGE)
        assertEquals(null, state.error)
    }

    @Test
    fun `seeds initial error and sourcechange clears it`() {
        val exception = mockk<THEOplayerException>()
        val mock = MockPlayer()
        val state = ErrorState(mock.player, initialError = exception)
        state.observe()
        assertEquals(exception, state.error)

        mock.firePlayerEvent(PlayerEventTypes.SOURCECHANGE)
        assertEquals(null, state.error)
    }

    @Test
    fun `null player keeps initial error and registers no listeners`() {
        val exception = mockk<THEOplayerException>()
        val mock = MockPlayer()
        val state = ErrorState(null, initialError = exception)
        state.observe()
        assertEquals(exception, state.error)
        assertTrue(mock.playerListeners.isEmpty())
        state.dispose()
    }
}
