package com.theoplayer.android.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VideoQualityStateTest {
    private fun playerWithTracks(vararg tracks: MockVideoTrack): MockPlayer {
        return MockPlayer().also { mock ->
            mock.videoTrackList.addAll(tracks.map { it.track })
        }
    }

    @Test
    fun `registers list events plus quality events on the enabled track`() {
        val enabled = MockVideoTrack(enabled = true)
        val mock = playerWithTracks(enabled)
        val state = VideoQualityState(mock.player)
        state.observe()

        assertEquals(3, mock.videoTrackListeners.size)
        assertEquals(2, enabled.listeners.size)

        state.dispose()
        assertTrue(mock.videoTrackListeners.isEmpty())
        assertTrue(enabled.listeners.isEmpty())
    }

    @Test
    fun `seeds qualities from the enabled track`() {
        val quality = mockVideoQuality()
        val enabled = MockVideoTrack(enabled = true, activeQuality = quality, qualities = listOf(quality))
        val mock = playerWithTracks(enabled)
        val state = VideoQualityState(mock.player)
        state.observe()

        assertEquals(listOf(quality), state.videoQualities)
        assertEquals(quality, state.activeVideoQuality)
    }

    @Test
    fun `quality events update active and target`() {
        val old = mockVideoQuality()
        val new = mockVideoQuality()
        val enabled = MockVideoTrack(enabled = true, activeQuality = old, qualities = listOf(old, new))
        val mock = playerWithTracks(enabled)
        val state = VideoQualityState(mock.player)
        state.observe()

        enabled.activeQuality = new
        enabled.targetQuality = new
        enabled.fireEvent()
        assertEquals(new, state.activeVideoQuality)
        assertEquals(new, state.targetVideoQuality)
    }

    @Test
    fun `select sets the track target quality`() {
        val quality = mockVideoQuality()
        val enabled = MockVideoTrack(enabled = true, qualities = listOf(quality))
        val mock = playerWithTracks(enabled)
        val state = VideoQualityState(mock.player)
        state.observe()

        state.select(quality)
        assertEquals(quality, enabled.targetQuality)
        assertEquals(quality, state.targetVideoQuality)

        state.select(null)
        assertEquals(null, enabled.targetQuality)
    }

    @Test
    fun `switching the enabled track moves the quality listeners`() {
        val first = MockVideoTrack(enabled = true)
        val second = MockVideoTrack(enabled = false)
        val mock = playerWithTracks(first, second)
        val state = VideoQualityState(mock.player)
        state.observe()
        assertEquals(2, first.listeners.size)

        first.enabled = false
        second.enabled = true
        mock.videoTrackListeners.fireTrackListEvent()

        assertEquals(0, first.listeners.size)
        assertEquals(2, second.listeners.size)
    }

    @Test
    fun `null player gives defaults and no listeners`() {
        val state = VideoQualityState(null)
        state.observe()
        assertTrue(state.videoQualities.isEmpty())
        assertEquals(null, state.activeVideoQuality)
        assertEquals(null, state.targetVideoQuality)
        state.dispose()
    }
}
