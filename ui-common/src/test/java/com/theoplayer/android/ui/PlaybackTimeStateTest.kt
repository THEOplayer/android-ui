package com.theoplayer.android.ui

import com.theoplayer.android.api.event.player.PlayerEventTypes
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class PlaybackTimeStateTest {
    @Test
    fun `exposes the player state before observe`() {
        val state = PlaybackTimeState(MockPlayer(currentTime = 5.0, duration = 100.0).player)
        assertEquals(5.0, state.currentTime, 0.0)
        assertEquals(100.0, state.duration, 0.0)
        assertEquals(StreamType.Vod, state.streamType)
    }

    @Test
    fun `registers the playback events`() = runTest {
        val mock = MockPlayer()
        launchObserve { PlaybackTimeState(mock.player).observe() }
        assertEquals(
            playbackEventTypes + PlayerEventTypes.DURATIONCHANGE,
            mock.playerEventTypes,
        )
    }

    @Test
    fun `streamTypeOf classifies vod, live and dvr`() = runTest {
        assertEquals(
            StreamType.Vod,
            streamTypeOf(100.0, com.theoplayer.android.ui.TimeRanges(listOf(0.0..100.0))),
        )
        assertEquals(
            StreamType.Vod,
            streamTypeOf(Double.NaN, com.theoplayer.android.ui.TimeRanges.empty()),
        )
        assertEquals(
            StreamType.Live,
            streamTypeOf(
                Double.POSITIVE_INFINITY,
                com.theoplayer.android.ui.TimeRanges(listOf(0.0..30.0)),
            ),
        )
        assertEquals(
            StreamType.Dvr,
            streamTypeOf(
                Double.POSITIVE_INFINITY,
                com.theoplayer.android.ui.TimeRanges(listOf(0.0..60.0)),
            ),
        )
    }

    @Test
    fun `seeds and updates on time events`() = runTest {
        val mock = MockPlayer(currentTime = 5.0, duration = 100.0)
        val state = PlaybackTimeState(mock.player)
        launchObserve { state.observe() }

        assertEquals(5.0, state.currentTime, 0.0)
        assertEquals(100.0, state.duration, 0.0)

        mock.currentTime = 42.0
        mock.firePlayerEvent()
        assertEquals(42.0, state.currentTime, 0.0)
    }

    @Test
    fun `duration prefers end of seekable range`() = runTest {
        val mock =
            MockPlayer(
                duration = Double.POSITIVE_INFINITY,
                seekableRanges = listOf(10.0 to 70.0),
            )
        val state = PlaybackTimeState(mock.player)
        launchObserve { state.observe() }

        assertEquals(70.0, state.duration, 0.0)
    }

    @Test
    fun `reports live and dvr stream types`() = runTest {
        val live =
            MockPlayer(duration = Double.POSITIVE_INFINITY, seekableRanges = listOf(0.0 to 30.0))
        val dvr =
            MockPlayer(duration = Double.POSITIVE_INFINITY, seekableRanges = listOf(0.0 to 120.0))
        val vod = MockPlayer(duration = 100.0)

        assertEquals(
            StreamType.Live,
            PlaybackTimeState(live.player).also { launchObserve { it.observe() } }.streamType,
        )
        assertEquals(
            StreamType.Dvr,
            PlaybackTimeState(dvr.player).also { launchObserve { it.observe() } }.streamType,
        )
        assertEquals(
            StreamType.Vod,
            PlaybackTimeState(vod.player).also { launchObserve { it.observe() } }.streamType,
        )
    }
}
