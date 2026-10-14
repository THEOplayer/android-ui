package com.theoplayer.android.ui

import android.view.View
import com.theoplayer.android.api.THEOplayerView
import com.theoplayer.android.ui.common.R
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * A [FullscreenHandler] that records its listeners and request calls,
 * so tests can fire fullscreen changes at them.
 */
internal class FakeFullscreenHandler : FullscreenHandler {
    override var fullscreen: Boolean = false
        private set
    val listeners = mutableListOf<FullscreenHandler.OnFullscreenChangeListener>()
    var requestCount = 0
    var exitCount = 0

    override fun addOnFullscreenChangeListener(listener: FullscreenHandler.OnFullscreenChangeListener) {
        listeners.add(listener)
    }

    override fun removeOnFullscreenChangeListener(listener: FullscreenHandler.OnFullscreenChangeListener) {
        listeners.remove(listener)
    }

    override fun requestFullscreen() {
        requestCount++
    }

    override fun exitFullscreen() {
        exitCount++
    }

    fun fire(fullscreen: Boolean) {
        this.fullscreen = fullscreen
        listeners.toList().forEach { it.onFullscreenChange(fullscreen) }
    }
}

private fun mockViewWithHandler(handler: FullscreenHandler?): THEOplayerView = mockk {
    every { getTag(R.id.theoplayer_ui_fullscreen_handler) } returns handler
}

class FullscreenStateTest {
    @Test
    fun `null view gives defaults and does not throw`() = runTest {
        val state = FullscreenState(null)
        val job = launchObserve { state.observe() }
        assertFalse(state.fullscreen)
        state.enterFullscreen()
        state.exitFullscreen()
        assertTrue(state.fullscreen.not())
        job.cancelAndJoin()
    }

    @Test
    fun `observe seeds fullscreen and adds one listener`() = runTest {
        val handler = FakeFullscreenHandler().also { it.fire(true) }
        val state = FullscreenState(mockViewWithHandler(handler))
        launchObserve { state.observe() }

        assertTrue(state.fullscreen)
        assertEquals(1, handler.listeners.size)
    }

    @Test
    fun `cancelling removes the listener`() = runTest {
        val handler = FakeFullscreenHandler()
        val state = FullscreenState(mockViewWithHandler(handler))
        val job = launchObserve { state.observe() }
        assertEquals(1, handler.listeners.size)

        job.cancelAndJoin()
        assertTrue(handler.listeners.isEmpty())
    }

    @Test
    fun `handler notification updates fullscreen`() = runTest {
        val handler = FakeFullscreenHandler()
        val state = FullscreenState(mockViewWithHandler(handler))
        launchObserve { state.observe() }
        assertFalse(state.fullscreen)

        handler.fire(true)
        assertTrue(state.fullscreen)
    }

    @Test
    fun `enterFullscreen updates state and calls the handler once`() = runTest {
        val handler = FakeFullscreenHandler()
        val state = FullscreenState(mockViewWithHandler(handler))
        launchObserve { state.observe() }

        state.enterFullscreen()
        assertTrue(state.fullscreen)
        assertEquals(1, handler.requestCount)
    }

    @Test
    fun `exitFullscreen updates state and calls the handler once`() = runTest {
        val handler = FakeFullscreenHandler().also { it.fire(true) }
        val state = FullscreenState(mockViewWithHandler(handler))
        launchObserve { state.observe() }

        state.exitFullscreen()
        assertFalse(state.fullscreen)
        assertEquals(1, handler.exitCount)
    }

    @Test
    fun `two states on the same view share the handler`() = runTest {
        val handler = FakeFullscreenHandler()
        val view = mockViewWithHandler(handler)
        val first = FullscreenState(view)
        val second = FullscreenState(view)
        launchObserve { first.observe() }
        launchObserve { second.observe() }
        assertEquals(2, handler.listeners.size)

        handler.fire(true)
        assertTrue(first.fullscreen)
        assertTrue(second.fullscreen)
    }

    @Test
    fun `fullscreenHandler returns the tagged handler without findViewById`() = runTest {
        val handler = FakeFullscreenHandler()
        val view = mockViewWithHandler(handler)

        assertSame(handler, view.fullscreenHandler)
        verify(exactly = 0) {
            view.findViewById<View>(com.theoplayer.android.R.id.theo_player_container)
        }
    }

    @Test
    fun `fullscreenHandler returns null without a container`() = runTest {
        val view = mockk<THEOplayerView> {
            every { getTag(R.id.theoplayer_ui_fullscreen_handler) } returns null
            every { findViewById<View>(com.theoplayer.android.R.id.theo_player_container) } returns null
        }

        assertEquals(null, view.fullscreenHandler)
    }
}
