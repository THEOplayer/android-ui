package com.theoplayer.android.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AudioTrackStateTest {
    private fun playerWithTracks(vararg tracks: MockAudioTrack): MockPlayer {
        return MockPlayer().also { mock ->
            mock.audioTrackList.addAll(tracks.map { it.track })
        }
    }

    @Test
    fun `registers the track list events with one listener`() {
        val mock = playerWithTracks(MockAudioTrack(enabled = true))
        val state = AudioTrackState(mock.player)
        state.observe()

        val listener = mock.audioTrackListeners.first().second
        assertTrue(mock.audioTrackListeners.all { it.second === listener })
        assertEquals(3, mock.audioTrackListeners.size)

        state.dispose()
        assertTrue(mock.audioTrackListeners.isEmpty())
    }

    @Test
    fun `seeds tracks and active track`() {
        val disabled = MockAudioTrack(enabled = false)
        val enabled = MockAudioTrack(enabled = true)
        val mock = playerWithTracks(disabled, enabled)
        val state = AudioTrackState(mock.player)
        state.observe()

        assertEquals(listOf(disabled.track, enabled.track), state.audioTracks)
        assertEquals(enabled.track, state.activeAudioTrack)
    }

    @Test
    fun `addtrack updates the list`() {
        val mock = playerWithTracks(MockAudioTrack(enabled = true))
        val state = AudioTrackState(mock.player)
        state.observe()
        assertEquals(1, state.audioTracks.size)

        val added = MockAudioTrack(enabled = false)
        mock.audioTrackList.add(added.track)
        mock.audioTrackListeners.fireTrackListEvent()
        assertEquals(2, state.audioTracks.size)
    }

    @Test
    fun `tracklistchange updates the active track`() {
        val first = MockAudioTrack(enabled = true)
        val second = MockAudioTrack(enabled = false)
        val mock = playerWithTracks(first, second)
        val state = AudioTrackState(mock.player)
        state.observe()
        assertEquals(first.track, state.activeAudioTrack)

        first.enabled = false
        second.enabled = true
        mock.audioTrackListeners.fireTrackListEvent()
        assertEquals(second.track, state.activeAudioTrack)
    }

    @Test
    fun `select enables the track and updates the state`() {
        val first = MockAudioTrack(enabled = true)
        val second = MockAudioTrack(enabled = false)
        val mock = playerWithTracks(first, second)
        val state = AudioTrackState(mock.player)
        state.observe()

        state.select(second.track)
        assertTrue(second.enabled)
        assertEquals(second.track, state.activeAudioTrack)
    }

    @Test
    fun `select null disables all tracks`() {
        val enabled = MockAudioTrack(enabled = true)
        val mock = playerWithTracks(enabled)
        val state = AudioTrackState(mock.player)
        state.observe()

        state.select(null)
        assertFalse(enabled.enabled)
        assertEquals(null, state.activeAudioTrack)
    }

    @Test
    fun `null player gives defaults and no listeners`() {
        val state = AudioTrackState(null)
        state.observe()
        assertTrue(state.audioTracks.isEmpty())
        assertEquals(null, state.activeAudioTrack)
        state.dispose()
    }
}
