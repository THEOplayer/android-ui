package com.theoplayer.android.ui

import com.theoplayer.android.api.event.player.PlayerEventTypes
import io.mockk.verify
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SeekBarStateTest {
    @Test
    fun `registers the playback and ad events`() = runTest {
        val mock = MockPlayer()
        launchObserve { SeekBarState(mock.player).observe() }
        assertEquals(
            playbackEventTypes + PlayerEventTypes.DURATIONCHANGE,
            mock.playerEventTypes,
        )
        assertEquals(adEventTypes, mock.adEventTypes)
    }

    @Test
    fun `an error event re-reads playingAd`() = runTest {
        val mock = MockPlayer(adPlaying = false)
        val state = SeekBarState(mock.player)
        launchObserve { state.observe() }
        assertTrue(state.showThumb)

        mock.adPlaying = true
        mock.firePlayerEvent(PlayerEventTypes.ERROR)
        assertFalse(state.enabled)
        assertFalse(state.showThumb)
    }

    @Test
    fun `computes value within zero-based range`() = runTest {
        val mock = MockPlayer(currentTime = 30.0, duration = 100.0)
        val state = SeekBarState(mock.player)
        launchObserve { state.observe() }

        assertEquals(0f..100f, state.valueRange)
        assertEquals(30f, state.value)
        assertTrue(state.enabled)
        assertTrue(state.showThumb)
    }

    @Test
    fun `huge seekable start maps to zero-based range`() = runTest {
        val mock =
            MockPlayer(
                currentTime = 1.7e9 + 30.0,
                duration = 1.7e9 + 100.0,
                seekableRanges = listOf(1.7e9 to 1.7e9 + 100.0),
            )
        val state = SeekBarState(mock.player)
        launchObserve { state.observe() }

        assertEquals(0f, state.valueRange.start)
        assertEquals(100f, state.valueRange.endInclusive, 0.001f)
        assertEquals(30f, state.value, 0.001f)
    }

    @Test
    fun `scrubbing pauses a playing player and resumes on finish`() = runTest {
        val mock = MockPlayer(paused = false, currentTime = 10.0)
        val state = SeekBarState(mock.player)
        launchObserve { state.observe() }

        state.onValueChange(50f)
        verify(exactly = 1) { mock.player.pause() }
        verify { mock.player.currentTime = 50.0 }
        assertEquals(50f, state.value)

        state.onValueChangeFinished()
        verify(exactly = 1) { mock.player.play() }
    }

    @Test
    fun `seekTo seeks without changing playback state`() {
        val mock = MockPlayer(paused = false, currentTime = 50.0)
        val state = SeekBarState(mock.player)

        state.seekTo(60f)

        verify { mock.player.currentTime = 60.0 }
        verify(exactly = 0) { mock.player.pause() }
        verify(exactly = 0) { mock.player.play() }
    }

    @Test
    fun `seekTo does nothing when disabled or player is missing`() {
        val mock = MockPlayer(seekableRanges = emptyList())

        SeekBarState(mock.player).seekTo(60f)
        SeekBarState(null).seekTo(60f)

        verify(exactly = 0) { mock.player.currentTime = any() }
        verify(exactly = 0) { mock.player.pause() }
        verify(exactly = 0) { mock.player.play() }
    }

    @Test
    fun `scrubbing a paused player does not resume it`() = runTest {
        val mock = MockPlayer(paused = true, currentTime = 10.0)
        val state = SeekBarState(mock.player)
        launchObserve { state.observe() }

        state.onValueChange(50f)
        state.onValueChangeFinished()
        verify(exactly = 0) { mock.player.play() }
    }

    @Test
    fun `disabled during ads and thumb hidden`() = runTest {
        val mock = MockPlayer(adPlaying = true)
        val state = SeekBarState(mock.player)
        launchObserve { state.observe() }

        assertFalse(state.enabled)
        assertFalse(state.showThumb)

        mock.adPlaying = false
        mock.fireAdEvent()
        assertTrue(state.enabled)
        assertTrue(state.showThumb)
    }

    @Test
    fun `cancelling removes all listeners`() = runTest {
        val mock = MockPlayer()
        val state = SeekBarState(mock.player)
        val job = launchObserve { state.observe() }
        val added = mock.playerListeners.toList()
        assertTrue(added.isNotEmpty())
        assertTrue(mock.adListeners.isNotEmpty())

        job.cancelAndJoin()
        assertTrue(mock.playerListeners.isEmpty())
        assertTrue(mock.adListeners.isEmpty())
    }
}
