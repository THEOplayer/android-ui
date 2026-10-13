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
    fun `null view gives defaults and does not throw`() {
        val state = FullscreenState(null)
        state.observe()
        assertFalse(state.fullscreen)
        state.enterFullscreen()
        state.exitFullscreen()
        assertTrue(state.fullscreen.not())
        state.dispose()
    }

    @Test
    fun `observe seeds fullscreen and adds one listener`() {
        val handler = FakeFullscreenHandler().also { it.fire(true) }
        val state = FullscreenState(mockViewWithHandler(handler))
        state.observe()

        assertTrue(state.fullscreen)
        assertEquals(1, handler.listeners.size)
    }

    @Test
    fun `dispose removes the listener`() {
        val handler = FakeFullscreenHandler()
        val state = FullscreenState(mockViewWithHandler(handler))
        state.observe()
        assertEquals(1, handler.listeners.size)

        state.dispose()
        assertTrue(handler.listeners.isEmpty())
    }

    @Test
    fun `handler notification updates fullscreen`() {
        val handler = FakeFullscreenHandler()
        val state = FullscreenState(mockViewWithHandler(handler))
        state.observe()
        assertFalse(state.fullscreen)

        handler.fire(true)
        assertTrue(state.fullscreen)
    }

    @Test
    fun `enterFullscreen updates state and calls the handler once`() {
        val handler = FakeFullscreenHandler()
        val state = FullscreenState(mockViewWithHandler(handler))
        state.observe()

        state.enterFullscreen()
        assertTrue(state.fullscreen)
        assertEquals(1, handler.requestCount)
    }

    @Test
    fun `exitFullscreen updates state and calls the handler once`() {
        val handler = FakeFullscreenHandler().also { it.fire(true) }
        val state = FullscreenState(mockViewWithHandler(handler))
        state.observe()

        state.exitFullscreen()
        assertFalse(state.fullscreen)
        assertEquals(1, handler.exitCount)
    }

    @Test
    fun `two states on the same view share the handler`() {
        val handler = FakeFullscreenHandler()
        val view = mockViewWithHandler(handler)
        val first = FullscreenState(view)
        val second = FullscreenState(view)
        first.observe()
        second.observe()
        assertEquals(2, handler.listeners.size)

        handler.fire(true)
        assertTrue(first.fullscreen)
        assertTrue(second.fullscreen)
    }

    @Test
    fun `fullscreenHandler returns the tagged handler without findViewById`() {
        val handler = FakeFullscreenHandler()
        val view = mockViewWithHandler(handler)

        assertSame(handler, view.fullscreenHandler)
        verify(exactly = 0) {
            view.findViewById<View>(com.theoplayer.android.R.id.theo_player_container)
        }
    }

    @Test
    fun `fullscreenHandler returns null without a container`() {
        val view = mockk<THEOplayerView> {
            every { getTag(R.id.theoplayer_ui_fullscreen_handler) } returns null
            every { findViewById<View>(com.theoplayer.android.R.id.theo_player_container) } returns null
        }

        assertEquals(null, view.fullscreenHandler)
    }
}
