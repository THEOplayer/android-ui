package com.theoplayer.android.ui

import com.theoplayer.android.api.THEOplayerView
import com.theoplayer.android.api.cast.Cast
import com.theoplayer.android.api.cast.chromecast.Chromecast
import com.theoplayer.android.api.cast.chromecast.PlayerCastState
import com.theoplayer.android.api.event.EventListener
import com.theoplayer.android.api.event.EventType
import com.theoplayer.android.api.event.chromecast.ChromecastEvent
import com.theoplayer.android.api.event.chromecast.ChromecastEventTypes
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.test.runTest
import org.junit.Test

class CastStateTest {
    private fun mockCast(
        state: PlayerCastState = PlayerCastState.AVAILABLE,
        receiverName: String? = "Receiver",
        casting: Boolean = false
    ): Triple<Cast, MutableList<Pair<EventType<*>, EventListener<*>>>, Chromecast> {
        val listeners = mutableListOf<Pair<EventType<*>, EventListener<*>>>()
        val chromecast = mockk<Chromecast> {
            every { this@mockk.state } answers { state }
            every { this@mockk.receiverName } returns receiverName
            every { isCasting } answers { casting }
            every { start() } returns Unit
            every { stop() } returns Unit
            every {
                addEventListener(any<EventType<out ChromecastEvent<*>>>(), any())
            } answers {
                listeners.add(firstArg<EventType<*>>() to secondArg<EventListener<*>>())
                Unit
            }
            every {
                removeEventListener(any<EventType<out ChromecastEvent<*>>>(), any())
            } answers {
                listeners.remove(firstArg<EventType<*>>() to secondArg<EventListener<*>>())
                Unit
            }
        }
        return Triple(
            mockk { every { this@mockk.chromecast } returns chromecast },
            listeners,
            chromecast
        )
    }

    private fun mockView(cast: Cast): THEOplayerView = mockk {
        every { this@mockk.cast } returns cast
    }

    @Test
    fun `null view and null cast give defaults`() = runTest {
        val state = CastState(null)
        val job = launchObserve { state.observe() }
        assertEquals(PlayerCastState.UNAVAILABLE, state.castState)
        assertEquals(null, state.castReceiverName)
        assertFalse(state.casting)
        state.startCasting()
        state.stopCasting()
        job.cancelAndJoin()
    }

    @Test
    fun `observe registers both events with one listener and dispose removes them`() = runTest {
        val (cast, listeners, _) = mockCast()
        val view = mockView(cast)
        val state = CastState(view)
        val job = launchObserve { state.observe() }

        assertEquals(2, listeners.size)
        assertEquals(
            setOf(ChromecastEventTypes.STATECHANGE, ChromecastEventTypes.ERROR),
            listeners.map { it.first }.toSet()
        )
        assertTrue(listeners.all { it.second === listeners.first().second })

        job.cancelAndJoin()
        assertTrue(listeners.isEmpty())
    }

    @Test
    fun `seeds and updates on events`() = runTest {
        val (cast, listeners, _) = mockCast(state = PlayerCastState.CONNECTED, casting = true)
        val view = mockView(cast)
        val state = CastState(view)
        launchObserve { state.observe() }

        assertEquals(PlayerCastState.CONNECTED, state.castState)
        assertEquals("Receiver", state.castReceiverName)
        assertTrue(state.casting)

        listeners.toList().forEach { (type, listener) ->
            @Suppress("UNCHECKED_CAST")
            (listener as EventListener<ChromecastEvent<*>>).handleEvent(
                when (type) {
                    ChromecastEventTypes.STATECHANGE -> mockk<com.theoplayer.android.api.event.chromecast.CastStateChangeEvent>()
                    else -> mockk<com.theoplayer.android.api.event.chromecast.CastErrorEvent>()
                }
            )
        }
        assertEquals(PlayerCastState.CONNECTED, state.castState)
    }

    @Test
    fun `start and stop delegate to the chromecast`() = runTest {
        val (cast, _, chromecast) = mockCast()
        val view = mockView(cast)
        val state = CastState(view)
        launchObserve { state.observe() }

        state.startCasting()
        verify { chromecast.start() }
        state.stopCasting()
        verify { chromecast.stop() }
    }

    @Test
    fun `replacing the cast integration moves the listeners`() = runTest {
        val (oldCast, oldListeners, _) = mockCast()
        val (newCast, newListeners, _) = mockCast(state = PlayerCastState.CONNECTED)
        var currentCast: Cast = oldCast
        val view = mockk<THEOplayerView> {
            every { this@mockk.cast } answers { currentCast }
        }
        val state = CastState(view)
        launchObserve { state.observe() }
        assertEquals(2, oldListeners.size)

        currentCast = newCast
        state.castState
        androidx.compose.runtime.snapshots.Snapshot.sendApplyNotifications()
        testScheduler.runCurrent()
        assertEquals(PlayerCastState.CONNECTED, state.castState)
        assertTrue(oldListeners.isEmpty())
        assertEquals(2, newListeners.size)
    }

    @Test
    fun `getters before observe register nothing`() = runTest {
        val (cast, listeners, _) = mockCast()
        val view = mockView(cast)
        val state = CastState(view)

        state.castState
        state.castReceiverName
        state.casting
        assertTrue(listeners.isEmpty())
    }
}
