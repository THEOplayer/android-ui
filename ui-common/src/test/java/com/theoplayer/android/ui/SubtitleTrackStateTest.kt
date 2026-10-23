package com.theoplayer.android.ui

import com.theoplayer.android.api.player.track.texttrack.TextTrackKind
import com.theoplayer.android.api.player.track.texttrack.TextTrackMode
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SubtitleTrackStateTest {
    private fun playerWithTracks(vararg tracks: MockTextTrack): MockPlayer {
        return MockPlayer().also { mock -> mock.textTrackList.addAll(tracks.map { it.track }) }
    }

    @Test
    fun `registers the track list events with one listener`() = runTest {
        val mock = playerWithTracks(MockTextTrack(TextTrackKind.SUBTITLES, TextTrackMode.SHOWING))
        val state = SubtitleTrackState(mock.player)
        val job = launchObserve { state.observe() }

        val listener = mock.textTrackListeners.first().second
        assertTrue(mock.textTrackListeners.all { it.second === listener })
        assertEquals(3, mock.textTrackListeners.size)

        job.cancelAndJoin()
        assertTrue(mock.textTrackListeners.isEmpty())
    }

    @Test
    fun `filters out non-subtitle tracks`() = runTest {
        val subtitles = MockTextTrack(TextTrackKind.SUBTITLES, TextTrackMode.DISABLED)
        val captions = MockTextTrack(TextTrackKind.CAPTIONS, TextTrackMode.SHOWING)
        val metadata = MockTextTrack(TextTrackKind.METADATA, TextTrackMode.DISABLED)
        val mock = playerWithTracks(subtitles, captions, metadata)
        val state = SubtitleTrackState(mock.player)
        launchObserve { state.observe() }

        assertEquals(listOf(subtitles.track, captions.track), state.subtitleTracks)
        assertEquals(captions.track, state.activeSubtitleTrack)
    }

    @Test
    fun `select shows the track and disables the other subtitles`() = runTest {
        val first = MockTextTrack(TextTrackKind.SUBTITLES, TextTrackMode.SHOWING)
        val second = MockTextTrack(TextTrackKind.SUBTITLES, TextTrackMode.DISABLED)
        val metadata = MockTextTrack(TextTrackKind.METADATA, TextTrackMode.DISABLED)
        val mock = playerWithTracks(first, second, metadata)
        val state = SubtitleTrackState(mock.player)
        launchObserve { state.observe() }

        state.select(second.track)
        assertEquals(TextTrackMode.DISABLED, first.mode)
        assertEquals(TextTrackMode.SHOWING, second.mode)
        assertEquals(TextTrackMode.DISABLED, metadata.mode)
        assertEquals(second.track, state.activeSubtitleTrack)
    }

    @Test
    fun `select null disables all subtitle tracks`() = runTest {
        val showing = MockTextTrack(TextTrackKind.SUBTITLES, TextTrackMode.SHOWING)
        val mock = playerWithTracks(showing)
        val state = SubtitleTrackState(mock.player)
        launchObserve { state.observe() }

        state.select(null)
        assertEquals(TextTrackMode.DISABLED, showing.mode)
        assertEquals(null, state.activeSubtitleTrack)
    }

    @Test
    fun `null player gives defaults and no listeners`() = runTest {
        val state = SubtitleTrackState(null)
        val job = launchObserve { state.observe() }
        assertTrue(state.subtitleTracks.isEmpty())
        assertEquals(null, state.activeSubtitleTrack)
        job.cancelAndJoin()
    }
}
