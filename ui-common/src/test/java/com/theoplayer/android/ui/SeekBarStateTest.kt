package com.theoplayer.android.ui

import com.theoplayer.android.api.event.player.PlayerEventTypes
import io.mockk.verify
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
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
        assertEquals(50f, state.value)

        state.onValueChangeFinished()
        verify(exactly = 1) { mock.player.currentTime = 50.0 }
        verify(exactly = 1) { mock.player.play() }
    }

    @Test
    fun `scrubbing seeks once the position stays unchanged`() = runTest {
        val mock = MockPlayer(paused = false, currentTime = 10.0)
        val state = SeekBarState(mock.player)
        launchObserve { state.observe() }

        state.onValueChange(40f)
        advanceTimeBy(200)
        state.onValueChange(50f)
        advanceTimeBy(249)
        verify(exactly = 0) { mock.player.currentTime = any() }

        advanceTimeBy(1)
        runCurrent()
        verify(exactly = 0) { mock.player.currentTime = 40.0 }
        verify(exactly = 1) { mock.player.currentTime = 50.0 }

        state.onValueChangeFinished()
        verify(exactly = 1) { mock.player.currentTime = any() }
    }

    @Test
    fun `finishing seeks immediately and cancels the pending seek`() = runTest {
        val mock = MockPlayer(paused = false, currentTime = 10.0)
        val state = SeekBarState(mock.player)
        launchObserve { state.observe() }

        state.onValueChange(40f)
        state.onValueChange(50f)
        state.onValueChangeFinished()
        verify(exactly = 1) { mock.player.currentTime = 50.0 }

        advanceTimeBy(1_000)
        runCurrent()
        verify(exactly = 1) { mock.player.currentTime = any() }
    }

    @Test
    fun `scrubTime is the absolute scrub position`() = runTest {
        val mock = MockPlayer(seekableRanges = listOf(100.0 to 200.0), currentTime = 110.0)
        val state = SeekBarState(mock.player)
        launchObserve { state.observe() }
        assertNull(state.scrubTime)

        state.onValueChange(50f)
        assertEquals(150.0, state.scrubTime!!, 0.001)
        assertEquals(150.0, (state as PlaybackTimeState).currentTime, 0.001)
        assertEquals(110.0, mock.currentTime, 0.0)

        state.onValueChangeFinished()
        assertNull(state.scrubTime)
        assertEquals(150.0, state.currentTime, 0.001)
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
    fun `togglePlayPause plays and pauses`() = runTest {
        val mock = MockPlayer(paused = true)
        val state = SeekBarState(mock.player)
        launchObserve { state.observe() }

        state.togglePlayPause()
        verify(exactly = 1) { mock.player.play() }
        assertFalse(mock.paused)

        state.togglePlayPause()
        verify(exactly = 1) { mock.player.pause() }
        assertTrue(mock.paused)
    }

    @Test
    fun `togglePlayPause while scrubbing changes whether playback resumes`() = runTest {
        val mock = MockPlayer(paused = false, currentTime = 10.0)
        val state = SeekBarState(mock.player)
        launchObserve { state.observe() }

        state.onValueChange(50f)
        state.togglePlayPause()
        state.onValueChangeFinished()
        verify(exactly = 0) { mock.player.play() }

        state.onValueChange(60f)
        state.togglePlayPause()
        state.onValueChangeFinished()
        verify(exactly = 1) { mock.player.play() }
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
    fun `scrubbing does nothing during ads`() = runTest {
        val mock = MockPlayer(paused = false, adPlaying = true)
        val state = SeekBarState(mock.player)
        launchObserve { state.observe() }

        state.onValueChange(50f)
        advanceTimeBy(1_000)
        runCurrent()
        state.onValueChangeFinished()

        assertNull(state.scrubTime)
        verify(exactly = 0) { mock.player.pause() }
        verify(exactly = 0) { mock.player.currentTime = any() }
    }

    @Test
    fun `an ad starting while scrubbing cancels the seek`() = runTest {
        val mock = MockPlayer(paused = false)
        val state = SeekBarState(mock.player)
        launchObserve { state.observe() }

        state.onValueChange(50f)
        mock.adPlaying = true
        mock.fireAdEvent()
        advanceTimeBy(1_000)
        runCurrent()
        state.onValueChangeFinished()

        verify(exactly = 0) { mock.player.currentTime = any() }
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
