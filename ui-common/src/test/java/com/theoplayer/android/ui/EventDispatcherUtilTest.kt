package com.theoplayer.android.ui

import com.theoplayer.android.api.event.EventListener
import com.theoplayer.android.api.event.player.PlayerEvent
import com.theoplayer.android.api.event.player.PlayerEventTypes
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class EventDispatcherUtilTest {
    @Test
    fun `one listener instance is registered for all types and removed on cancel`() = runTest {
        val mock = MockPlayer()
        val job = launchObserve {
            mock.player.listen(PlayerEventTypes.PLAY, PlayerEventTypes.PAUSE) {}
        }

        assertEquals(
            listOf(PlayerEventTypes.PLAY, PlayerEventTypes.PAUSE),
            mock.playerListeners.map { it.first }
        )
        assertSame(mock.playerListeners[0].second, mock.playerListeners[1].second)

        job.cancelAndJoin()
        assertTrue(mock.playerListeners.isEmpty())
    }

    @Test
    fun `a throwing onEvent removes the listeners and rethrows`() = runTest {
        val mock = MockPlayer()
        val exception = RuntimeException("boom")
        var received: Throwable? = null
        val job = launchObserve {
            try {
                mock.player.listen(PlayerEventTypes.PLAY) { throw exception }
            } catch (t: Throwable) {
                received = t
            }
        }

        mock.firePlayerEvent(PlayerEventTypes.PLAY)
        job.join()
        // The test dispatcher may copy exceptions for stack-trace recovery.
        assertEquals(exception.message, (received as? RuntimeException)?.message)
        assertTrue(mock.playerListeners.isEmpty())
    }

    @Test
    fun `a captured listener does not call onEvent after cancel`() = runTest {
        val mock = MockPlayer()
        var calls = 0
        val job = launchObserve {
            mock.player.listen(PlayerEventTypes.PLAY) { calls++ }
        }
        val captured = mock.playerListeners.single().second as EventListener<PlayerEvent<*>>
        mock.firePlayerEvent(PlayerEventTypes.PLAY)
        assertEquals(1, calls)

        job.cancelAndJoin()
        captured.handleEvent(io.mockk.mockk<com.theoplayer.android.api.event.player.PlayEvent>())
        assertEquals(1, calls)
    }
}
