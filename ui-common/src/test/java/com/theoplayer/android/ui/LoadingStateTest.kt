package com.theoplayer.android.ui

import com.theoplayer.android.api.event.player.PlayerEventTypes
import com.theoplayer.android.api.player.ReadyState
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LoadingStateTest {
    @Test
    fun `registers the playback events`() = runTest {
        val mock = MockPlayer()
        launchObserve { LoadingState(mock.player).observe() }
        assertEquals(playbackEventTypes - PlayerEventTypes.TIMEUPDATE, mock.playerEventTypes)
    }

    @Test
    fun `loading truth table`() = runTest {
        fun state(
            paused: Boolean,
            ended: Boolean = false,
            seeking: Boolean = false,
            readyState: ReadyState,
        ) =
            LoadingState(
                    MockPlayer(
                            paused = paused,
                            ended = ended,
                            seeking = seeking,
                            readyState = readyState,
                        )
                        .player
                )
                .also { launchObserve { it.observe() } }
                .loading

        assertFalse(state(paused = false, readyState = ReadyState.HAVE_ENOUGH_DATA))
        assertTrue(state(paused = false, readyState = ReadyState.HAVE_CURRENT_DATA))
        assertTrue(state(paused = false, seeking = true, readyState = ReadyState.HAVE_ENOUGH_DATA))
        assertFalse(state(paused = true, readyState = ReadyState.HAVE_CURRENT_DATA))
        assertFalse(state(paused = false, ended = true, readyState = ReadyState.HAVE_CURRENT_DATA))
    }
}
