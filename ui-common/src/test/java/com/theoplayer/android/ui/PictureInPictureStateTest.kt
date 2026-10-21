package com.theoplayer.android.ui

import com.theoplayer.android.api.THEOplayerView
import com.theoplayer.android.api.event.player.PlayerEventTypes
import com.theoplayer.android.api.pip.PiPManager
import com.theoplayer.android.api.pip.PiPType
import com.theoplayer.android.ui.common.R
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PictureInPictureStateTest {
    private fun mockView(
        player: MockPlayer = MockPlayer(),
        piPManager: PiPManager? = mockk(relaxed = true),
        fullscreenHandler: FullscreenHandler? = null,
    ): THEOplayerView = mockk {
        every { context } returns mockk<android.app.Activity>(relaxed = true)
        every { this@mockk.player } returns player.player
        every { this@mockk.piPManager } returns piPManager
        every { getTag(R.id.theoplayer_ui_fullscreen_handler) } returns fullscreenHandler
    }

    @Test
    fun `null view gives defaults and does not throw`() = runTest {
        val state = PictureInPictureState(null)
        val job = launchObserve { state.observe() }
        assertFalse(state.pictureInPictureSupported)
        assertFalse(state.pictureInPicture)
        state.enterPictureInPicture(PiPType.ACTIVITY)
        state.exitPictureInPicture()
        job.cancelAndJoin()
    }

    @Test
    fun `observe registers presentationmodechange and cancelling removes it`() = runTest {
        val mock = MockPlayer()
        val state = PictureInPictureState(mockView(player = mock))
        val job = launchObserve { state.observe() }

        assertEquals(setOf(PlayerEventTypes.PRESENTATIONMODECHANGE), mock.playerEventTypes)

        job.cancelAndJoin()
        assertTrue(mock.playerListeners.isEmpty())
    }

    @Test
    fun `seeds and updates pip mode on presentationmodechange`() = runTest {
        val piPManager = mockk<PiPManager> { every { isInPiP } returns true }
        val mock = MockPlayer()
        val state = PictureInPictureState(mockView(player = mock, piPManager = piPManager))
        val job = launchObserve { state.observe() }
        assertTrue(state.pictureInPicture)

        every { piPManager.isInPiP } returns false
        mock.firePlayerEvent(PlayerEventTypes.PRESENTATIONMODECHANGE)
        assertFalse(state.pictureInPicture)
    }

    @Test
    fun `activityInPipMode alone counts as pictureInPicture`() = runTest {
        val state = PictureInPictureState(mockView())
        state.activityInPipMode = true
        assertTrue(state.pictureInPicture)
    }

    @Test
    fun `enter when not fullscreen calls enterPiP directly`() = runTest {
        val piPManager = mockk<PiPManager>(relaxed = true)
        val handler = FakeFullscreenHandler()
        val state =
            PictureInPictureState(mockView(piPManager = piPManager, fullscreenHandler = handler))

        state.enterPictureInPicture(PiPType.ACTIVITY)
        verify(exactly = 1) { piPManager.enterPiP(PiPType.ACTIVITY) }
        assertTrue(handler.listeners.isEmpty())
    }

    @Test
    fun `enter while fullscreen waits for the exit notification`() = runTest {
        val piPManager = mockk<PiPManager>(relaxed = true)
        val handler = FakeFullscreenHandler().also { it.fire(true) }
        val state =
            PictureInPictureState(mockView(piPManager = piPManager, fullscreenHandler = handler))

        state.enterPictureInPicture(PiPType.ACTIVITY)
        assertEquals(1, handler.exitCount)
        verify(exactly = 0) { piPManager.enterPiP(any()) }
        assertEquals(1, handler.listeners.size)

        handler.fire(false)
        verify(exactly = 1) { piPManager.enterPiP(PiPType.ACTIVITY) }
        assertTrue(handler.listeners.isEmpty())
    }

    @Test
    fun `cancelling cancels a pending enterPiP`() = runTest {
        val piPManager = mockk<PiPManager>(relaxed = true)
        val handler = FakeFullscreenHandler().also { it.fire(true) }
        val state =
            PictureInPictureState(mockView(piPManager = piPManager, fullscreenHandler = handler))
        val job = launchObserve { state.observe() }

        state.enterPictureInPicture(PiPType.ACTIVITY)
        job.cancelAndJoin()
        assertTrue(handler.listeners.isEmpty())

        handler.fire(false)
        verify(exactly = 0) { piPManager.enterPiP(any()) }
    }
}
