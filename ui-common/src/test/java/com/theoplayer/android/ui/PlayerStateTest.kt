package com.theoplayer.android.ui

import com.theoplayer.android.api.ads.Ads
import com.theoplayer.android.api.event.Event
import com.theoplayer.android.api.event.EventDispatcher
import com.theoplayer.android.api.event.EventListener
import com.theoplayer.android.api.event.EventType
import com.theoplayer.android.api.event.ads.AdEvent
import com.theoplayer.android.api.event.ads.AdsEventTypes
import com.theoplayer.android.api.error.THEOplayerException
import com.theoplayer.android.api.event.player.ErrorEvent
import com.theoplayer.android.api.event.player.PlayerEvent
import com.theoplayer.android.api.event.player.PlayerEventTypes
import com.theoplayer.android.api.event.player.SourceChangeEvent
import com.theoplayer.android.api.event.track.TrackEvent
import com.theoplayer.android.api.event.track.tracklist.TrackListEvent
import com.theoplayer.android.api.player.Player
import com.theoplayer.android.api.player.ReadyState
import com.theoplayer.android.api.player.track.mediatrack.MediaTrack
import com.theoplayer.android.api.player.track.mediatrack.MediaTrackList
import com.theoplayer.android.api.player.track.mediatrack.quality.AudioQuality
import com.theoplayer.android.api.player.track.mediatrack.quality.VideoQuality
import com.theoplayer.android.api.player.track.texttrack.TextTrack
import com.theoplayer.android.api.player.track.texttrack.TextTrackKind
import com.theoplayer.android.api.player.track.texttrack.TextTrackList
import com.theoplayer.android.api.player.track.texttrack.TextTrackMode
import com.theoplayer.android.api.timerange.TimeRange
import com.theoplayer.android.api.timerange.TimeRanges
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * A mocked [Player] that records the listeners added to it and to its [Ads],
 * so tests can fire events at them.
 */
private class MockPlayer(
    var paused: Boolean = true,
    var ended: Boolean = false,
    var currentTime: Double = 0.0,
    var duration: Double = 100.0,
    var seekableRanges: List<Pair<Double, Double>> = listOf(0.0 to 100.0),
    var adPlaying: Boolean = false,
    var muted: Boolean = false,
    var seeking: Boolean = false,
    var readyState: ReadyState = ReadyState.HAVE_NOTHING,
    var videoWidth: Int = 0,
    var videoHeight: Int = 0,
    var playedLength: Int = 0,
    var playbackRate: Double = 1.0,
) {
    var errorObject: THEOplayerException = mockk()
    val playerListeners = mutableListOf<Pair<EventType<*>, EventListener<*>>>()
    val adListeners = mutableListOf<Pair<EventType<*>, EventListener<*>>>()
    val audioTrackListeners = mutableListOf<Pair<EventType<*>, EventListener<*>>>()
    val textTrackListeners = mutableListOf<Pair<EventType<*>, EventListener<*>>>()
    val videoTrackListeners = mutableListOf<Pair<EventType<*>, EventListener<*>>>()
    val audioTrackList = mutableListOf<MediaTrack<AudioQuality>>()
    val textTrackList = mutableListOf<TextTrack>()
    val videoTrackList = mutableListOf<MediaTrack<VideoQuality>>()

    val ads: Ads = mockk {
        every { isPlaying } answers { this@MockPlayer.adPlaying }
        every {
            addEventListener(any<EventType<out AdEvent<*>>>(), any())
        } answers {
            adListeners.add(firstArg<EventType<*>>() to secondArg<EventListener<*>>())
            Unit
        }
        every {
            removeEventListener(any<EventType<out AdEvent<*>>>(), any())
        } answers {
            adListeners.remove(firstArg<EventType<*>>() to secondArg<EventListener<*>>())
            Unit
        }
    }

    val audioTracks: MediaTrackList<AudioQuality> = mockTrackList(
        { audioTrackList.toMutableList().listIterator() },
        audioTrackListeners
    )

    val textTracks: TextTrackList = mockTrackList(
        { textTrackList.toMutableList().listIterator() },
        textTrackListeners
    )

    val videoTracks: MediaTrackList<VideoQuality> = mockTrackList(
        { videoTrackList.toMutableList().listIterator() },
        videoTrackListeners
    )

    val player: Player = mockk {
        every { isPaused } answers { this@MockPlayer.paused }
        every { isEnded } answers { this@MockPlayer.ended }
        every { getCurrentTime() } answers { this@MockPlayer.currentTime }
        every { getDuration() } answers { this@MockPlayer.duration }
        every { getSeekable() } answers { mockTimeRanges(this@MockPlayer.seekableRanges) }
        every { getAds() } returns this@MockPlayer.ads
        every { setCurrentTime(any()) } answers { this@MockPlayer.currentTime = firstArg() }
        every { play() } answers { this@MockPlayer.paused = false }
        every { pause() } answers { this@MockPlayer.paused = true }
        every { isMuted } answers { this@MockPlayer.muted }
        every { setMuted(any()) } answers { this@MockPlayer.muted = firstArg() }
        every { isSeeking } answers { this@MockPlayer.seeking }
        every { getReadyState() } answers { this@MockPlayer.readyState }
        every { getVideoWidth() } answers { this@MockPlayer.videoWidth }
        every { getVideoHeight() } answers { this@MockPlayer.videoHeight }
        every { getPlayed() } answers {
            mockk { every { length() } returns this@MockPlayer.playedLength }
        }
        every { getPlaybackRate() } answers { this@MockPlayer.playbackRate }
        every { setPlaybackRate(any()) } answers { this@MockPlayer.playbackRate = firstArg() }
        every { getAudioTracks() } returns this@MockPlayer.audioTracks
        every { getTextTracks() } returns this@MockPlayer.textTracks
        every { getVideoTracks() } returns this@MockPlayer.videoTracks
        every {
            addEventListener(any<EventType<out PlayerEvent<*>>>(), any())
        } answers {
            playerListeners.add(firstArg<EventType<*>>() to secondArg<EventListener<*>>())
            Unit
        }
        every {
            removeEventListener(any<EventType<out PlayerEvent<*>>>(), any())
        } answers {
            playerListeners.remove(firstArg<EventType<*>>() to secondArg<EventListener<*>>())
            Unit
        }
    }

    val playerEventTypes: Set<EventType<*>>
        get() = playerListeners.map { it.first }.toSet()

    val adEventTypes: Set<EventType<*>>
        get() = adListeners.map { it.first }.toSet()

    fun firePlayerEvent(type: EventType<*>? = null, event: PlayerEvent<*>? = null) {
        playerListeners.toList().forEach { (eventType, listener) ->
            if (type == null || eventType == type) {
                @Suppress("UNCHECKED_CAST")
                (listener as EventListener<PlayerEvent<*>>).handleEvent(
                    event ?: mockEvent(eventType)
                )
            }
        }
    }

    private fun mockEvent(type: EventType<*>): PlayerEvent<*> = when (type) {
        PlayerEventTypes.ERROR -> mockk<ErrorEvent> {
            every { errorObject } returns this@MockPlayer.errorObject
        }
        PlayerEventTypes.SOURCECHANGE -> mockk<SourceChangeEvent>()
        else -> mockk()
    }

    fun fireAdEvent() {
        adListeners.toList().forEach { (_, listener) ->
            @Suppress("UNCHECKED_CAST")
            (listener as EventListener<AdEvent<*>>).handleEvent(mockk())
        }
    }
}

private fun mockTimeRanges(ranges: List<Pair<Double, Double>>): TimeRanges = mockk {
    every { iterator() } answers {
        ranges.map { (start, end) ->
            mockk<TimeRange> {
                every { this@mockk.start } returns start
                every { this@mockk.end } returns end
            }
        }.toMutableList().listIterator()
    }
}

private var nextTrackUid = 0

/**
 * A mocked track list that records the listeners added to it.
 */
private inline fun <reified L> mockTrackList(
    noinline items: () -> MutableIterator<*>,
    listeners: MutableList<Pair<EventType<*>, EventListener<*>>>
): L where L : EventDispatcher<TrackListEvent<*, *>>, L : Iterable<*> = mockk<L> {
    every { iterator() } answers { items() }
    every { addEventListener(any<EventType<out TrackListEvent<*, *>>>(), any()) } answers {
        listeners.add(firstArg<EventType<*>>() to secondArg<EventListener<*>>())
        Unit
    }
    every { removeEventListener(any<EventType<out TrackListEvent<*, *>>>(), any()) } answers {
        listeners.remove(firstArg<EventType<*>>() to secondArg<EventListener<*>>())
        Unit
    }
}

/**
 * A mocked [MediaTrack] of [AudioQuality] with a mutable [MediaTrack.isEnabled].
 */
private class MockAudioTrack(var enabled: Boolean = false) {
    val uid = nextTrackUid++
    val track: MediaTrack<AudioQuality> = mockk {
        every { isEnabled } answers { this@MockAudioTrack.enabled }
        every { setEnabled(any()) } answers { this@MockAudioTrack.enabled = firstArg() }
        every { this@mockk.uid } returns this@MockAudioTrack.uid
    }
}

/**
 * A mocked [TextTrack] with a fixed [TextTrack.getKind] and a mutable [TextTrack.getMode].
 */
private class MockTextTrack(kind: TextTrackKind, var mode: TextTrackMode) {
    val uid = nextTrackUid++
    private val kindType = kind.type
    val track: TextTrack = mockk {
        every { this@mockk.kind } returns kindType
        every { this@mockk.mode } answers { this@MockTextTrack.mode }
        every { setMode(any()) } answers { this@MockTextTrack.mode = firstArg() }
        every { this@mockk.uid } returns this@MockTextTrack.uid
    }
}

private fun mockVideoQuality(): VideoQuality {
    val qualityUid = nextTrackUid++
    return mockk {
        every { this@mockk.uid } returns qualityUid
        every { this@mockk.height } returns 1080
        every { this@mockk.bandwidth } returns 5_000_000L
    }
}

/**
 * A mocked [MediaTrack] of [VideoQuality] that records its own event listeners.
 */
private class MockVideoTrack(
    var enabled: Boolean = false,
    var activeQuality: VideoQuality? = null,
    var targetQuality: VideoQuality? = null,
    val qualities: List<VideoQuality> = listOf()
) {
    val uid = nextTrackUid++
    val listeners = mutableListOf<Pair<EventType<*>, EventListener<*>>>()

    val track: MediaTrack<VideoQuality> = mockk {
        every { isEnabled } answers { this@MockVideoTrack.enabled }
        every { setEnabled(any()) } answers { this@MockVideoTrack.enabled = firstArg() }
        every { this@mockk.uid } returns this@MockVideoTrack.uid
        every { getActiveQuality() } answers { this@MockVideoTrack.activeQuality }
        every { getTargetQuality() } answers { this@MockVideoTrack.targetQuality }
        every { setTargetQuality(any()) } answers { this@MockVideoTrack.targetQuality = firstArg() }
        every { getQualities() } answers {
            mockk<com.theoplayer.android.api.player.track.mediatrack.quality.QualityList<VideoQuality>> {
                every { iterator() } answers { this@MockVideoTrack.qualities.toMutableList().listIterator() }
            }
        }
        every { addEventListener(any<EventType<out TrackEvent<*>>>(), any()) } answers {
            listeners.add(firstArg<EventType<*>>() to secondArg<EventListener<*>>())
            Unit
        }
        every { removeEventListener(any<EventType<out TrackEvent<*>>>(), any()) } answers {
            listeners.remove(firstArg<EventType<*>>() to secondArg<EventListener<*>>())
            Unit
        }
    }

    fun fireEvent() {
        listeners.toList().forEach { (_, listener) ->
            @Suppress("UNCHECKED_CAST")
            (listener as EventListener<Event<*>>).handleEvent(mockk())
        }
    }
}

private fun MutableList<Pair<EventType<*>, EventListener<*>>>.fireTrackListEvent() {
    toList().forEach { (_, listener) ->
        @Suppress("UNCHECKED_CAST")
        (listener as EventListener<Event<*>>).handleEvent(mockk())
    }
}

private val playbackEventTypes = setOf(
    PlayerEventTypes.TIMEUPDATE,
    PlayerEventTypes.PLAY,
    PlayerEventTypes.PAUSE,
    PlayerEventTypes.ENDED,
    PlayerEventTypes.SEEKING,
    PlayerEventTypes.SEEKED,
    PlayerEventTypes.READYSTATECHANGE,
    PlayerEventTypes.SOURCECHANGE,
    PlayerEventTypes.ERROR
)

private val adEventTypes = setOf(
    AdsEventTypes.AD_BREAK_BEGIN,
    AdsEventTypes.AD_SKIP,
    AdsEventTypes.AD_BREAK_END
)

class PlayButtonStateTest {
    @Test
    fun `registers the playback events`() {
        val mock = MockPlayer()
        PlayButtonState(mock.player).observe()
        assertEquals(playbackEventTypes - PlayerEventTypes.TIMEUPDATE, mock.playerEventTypes)
    }

    @Test
    fun `a seeked event clears ended`() {
        val mock = MockPlayer(paused = true, ended = true)
        val state = PlayButtonState(mock.player)
        state.observe()
        assertTrue(state.ended)

        mock.ended = false
        mock.firePlayerEvent(PlayerEventTypes.SEEKED)
        assertFalse(state.ended)
        assertTrue(state.paused)
    }

    @Test
    fun `seeds state and reacts to playback events`() {
        val mock = MockPlayer(paused = true, ended = false)
        val state = PlayButtonState(mock.player)
        state.observe()

        assertTrue(state.paused)
        assertFalse(state.ended)

        mock.paused = false
        mock.firePlayerEvent()
        assertFalse(state.paused)

        mock.ended = true
        mock.firePlayerEvent()
        assertTrue(state.ended)
    }

    @Test
    fun `onClick plays when paused and pauses when playing`() {
        val mock = MockPlayer(paused = true)
        val state = PlayButtonState(mock.player)
        state.observe()

        state.onClick()
        verify(exactly = 1) { mock.player.play() }

        mock.firePlayerEvent()
        state.onClick()
        verify(exactly = 1) { mock.player.pause() }
    }

    @Test
    fun `dispose removes all listeners`() {
        val mock = MockPlayer()
        val state = PlayButtonState(mock.player)
        state.observe()
        val added = mock.playerListeners.toList()
        assertTrue(added.isNotEmpty())

        state.dispose()
        assertTrue(mock.playerListeners.isEmpty())
        verify(exactly = added.size) {
            mock.player.removeEventListener(any<EventType<out PlayerEvent<*>>>(), any())
        }
    }

    @Test
    fun `shares one listener instance and removes it for every type`() {
        val mock = MockPlayer()
        val state = PlayButtonState(mock.player)
        state.observe()

        val added = mock.playerListeners.toList()
        val listener = added.first().second
        assertTrue(added.all { it.second === listener })

        state.dispose()
        added.forEach { (type, _) ->
            @Suppress("UNCHECKED_CAST")
            verify {
                mock.player.removeEventListener(
                    type as EventType<PlayerEvent<*>>,
                    listener as EventListener<PlayerEvent<*>>
                )
            }
        }
    }

    @Test
    fun `null player gives defaults and no listeners`() {
        val state = PlayButtonState(null)
        state.observe()
        assertTrue(state.paused)
        state.onClick()
        state.dispose()
    }
}

class SeekBarStateTest {
    @Test
    fun `registers the playback and ad events`() {
        val mock = MockPlayer()
        SeekBarState(mock.player).observe()
        assertEquals(
            playbackEventTypes + PlayerEventTypes.DURATIONCHANGE,
            mock.playerEventTypes
        )
        assertEquals(adEventTypes, mock.adEventTypes)
    }

    @Test
    fun `an error event re-reads playingAd`() {
        val mock = MockPlayer(adPlaying = false)
        val state = SeekBarState(mock.player)
        state.observe()
        assertTrue(state.showThumb)

        mock.adPlaying = true
        mock.firePlayerEvent(PlayerEventTypes.ERROR)
        assertFalse(state.enabled)
        assertFalse(state.showThumb)
    }

    @Test
    fun `computes value within zero-based range`() {
        val mock = MockPlayer(currentTime = 30.0, duration = 100.0)
        val state = SeekBarState(mock.player)
        state.observe()

        assertEquals(0f..100f, state.valueRange)
        assertEquals(30f, state.value)
        assertTrue(state.enabled)
        assertTrue(state.showThumb)
    }

    @Test
    fun `huge seekable start maps to zero-based range`() {
        val mock = MockPlayer(
            currentTime = 1.7e9 + 30.0,
            duration = 1.7e9 + 100.0,
            seekableRanges = listOf(1.7e9 to 1.7e9 + 100.0)
        )
        val state = SeekBarState(mock.player)
        state.observe()

        assertEquals(0f, state.valueRange.start)
        assertEquals(100f, state.valueRange.endInclusive, 0.001f)
        assertEquals(30f, state.value, 0.001f)
    }

    @Test
    fun `scrubbing pauses a playing player and resumes on finish`() {
        val mock = MockPlayer(paused = false, currentTime = 10.0)
        val state = SeekBarState(mock.player)
        state.observe()

        state.onValueChange(50f)
        verify(exactly = 1) { mock.player.pause() }
        verify { mock.player.currentTime = 50.0 }
        assertEquals(50f, state.value)

        state.onValueChangeFinished()
        verify(exactly = 1) { mock.player.play() }
    }

    @Test
    fun `scrubbing a paused player does not resume it`() {
        val mock = MockPlayer(paused = true, currentTime = 10.0)
        val state = SeekBarState(mock.player)
        state.observe()

        state.onValueChange(50f)
        state.onValueChangeFinished()
        verify(exactly = 0) { mock.player.play() }
    }

    @Test
    fun `disabled during ads and thumb hidden`() {
        val mock = MockPlayer(adPlaying = true)
        val state = SeekBarState(mock.player)
        state.observe()

        assertFalse(state.enabled)
        assertFalse(state.showThumb)

        mock.adPlaying = false
        mock.fireAdEvent()
        assertTrue(state.enabled)
        assertTrue(state.showThumb)
    }

    @Test
    fun `dispose removes all listeners`() {
        val mock = MockPlayer()
        val state = SeekBarState(mock.player)
        state.observe()
        val added = mock.playerListeners.toList()
        assertTrue(added.isNotEmpty())
        assertTrue(mock.adListeners.isNotEmpty())

        state.dispose()
        assertTrue(mock.playerListeners.isEmpty())
        assertTrue(mock.adListeners.isEmpty())
    }
}

class PlaybackTimeStateTest {
    @Test
    fun `registers the playback events`() {
        val mock = MockPlayer()
        PlaybackTimeState(mock.player).observe()
        assertEquals(
            playbackEventTypes + PlayerEventTypes.DURATIONCHANGE,
            mock.playerEventTypes
        )
    }

    @Test
    fun `streamTypeOf classifies vod, live and dvr`() {
        assertEquals(
            StreamType.Vod,
            streamTypeOf(100.0, com.theoplayer.android.ui.TimeRanges(listOf(0.0..100.0)))
        )
        assertEquals(
            StreamType.Vod,
            streamTypeOf(Double.NaN, com.theoplayer.android.ui.TimeRanges.empty())
        )
        assertEquals(
            StreamType.Live,
            streamTypeOf(
                Double.POSITIVE_INFINITY,
                com.theoplayer.android.ui.TimeRanges(listOf(0.0..30.0))
            )
        )
        assertEquals(
            StreamType.Dvr,
            streamTypeOf(
                Double.POSITIVE_INFINITY,
                com.theoplayer.android.ui.TimeRanges(listOf(0.0..60.0))
            )
        )
    }

    @Test
    fun `seeds and updates on time events`() {
        val mock = MockPlayer(currentTime = 5.0, duration = 100.0)
        val state = PlaybackTimeState(mock.player)
        state.observe()

        assertEquals(5.0, state.currentTime, 0.0)
        assertEquals(100.0, state.duration, 0.0)

        mock.currentTime = 42.0
        mock.firePlayerEvent()
        assertEquals(42.0, state.currentTime, 0.0)
    }

    @Test
    fun `duration prefers end of seekable range`() {
        val mock = MockPlayer(
            duration = Double.POSITIVE_INFINITY,
            seekableRanges = listOf(10.0 to 70.0)
        )
        val state = PlaybackTimeState(mock.player)
        state.observe()

        assertEquals(70.0, state.duration, 0.0)
    }

    @Test
    fun `reports live and dvr stream types`() {
        val live = MockPlayer(duration = Double.POSITIVE_INFINITY, seekableRanges = listOf(0.0 to 30.0))
        val dvr = MockPlayer(duration = Double.POSITIVE_INFINITY, seekableRanges = listOf(0.0 to 120.0))
        val vod = MockPlayer(duration = 100.0)

        assertEquals(StreamType.Live, PlaybackTimeState(live.player).apply { observe() }.streamType)
        assertEquals(StreamType.Dvr, PlaybackTimeState(dvr.player).apply { observe() }.streamType)
        assertEquals(StreamType.Vod, PlaybackTimeState(vod.player).apply { observe() }.streamType)
    }
}

class LiveButtonStateTest {
    @Test
    fun `registers the playback events`() {
        val mock = MockPlayer()
        LiveButtonState(mock.player, liveThreshold = 10.0).observe()
        assertEquals(
            playbackEventTypes + PlayerEventTypes.DURATIONCHANGE,
            mock.playerEventTypes
        )
    }

    @Test
    fun `visible for live but not for vod`() {
        val live = MockPlayer(duration = Double.POSITIVE_INFINITY, seekableRanges = listOf(0.0 to 30.0))
        val vod = MockPlayer(duration = 100.0)

        assertTrue(LiveButtonState(live.player, liveThreshold = 10.0).apply { observe() }.visible)
        assertFalse(LiveButtonState(vod.player, liveThreshold = 10.0).apply { observe() }.visible)
    }

    @Test
    fun `isLive at threshold boundary`() {
        val mock = MockPlayer(
            paused = false,
            duration = Double.POSITIVE_INFINITY,
            currentTime = 20.0,
            seekableRanges = listOf(0.0 to 30.0)
        )
        val atThreshold = LiveButtonState(mock.player, liveThreshold = 10.0)
        atThreshold.observe()
        assertTrue(atThreshold.isLive)

        mock.currentTime = 19.9
        val behindThreshold = LiveButtonState(mock.player, liveThreshold = 10.0)
        behindThreshold.observe()
        assertFalse(behindThreshold.isLive)
    }

    @Test
    fun `onClick seeks to live and plays`() {
        val mock = MockPlayer(duration = Double.POSITIVE_INFINITY)
        val state = LiveButtonState(mock.player, liveThreshold = 10.0)
        state.observe()

        state.onClick()
        verify { mock.player.currentTime = Double.POSITIVE_INFINITY }
        verify(exactly = 1) { mock.player.play() }
    }
}

class SeekButtonStateTest {
    @Test
    fun `registers the playback and ad events`() {
        val mock = MockPlayer()
        SeekButtonState(mock.player, seekOffset = 10).observe()
        assertEquals(playbackEventTypes, mock.playerEventTypes)
        assertEquals(adEventTypes, mock.adEventTypes)
    }

    @Test
    fun `onClick seeks by offset and clamps to duration`() {
        val mock = MockPlayer(currentTime = 95.0, duration = 100.0)
        val state = SeekButtonState(mock.player, seekOffset = 10)
        state.observe()

        state.onClick()
        verify { mock.player.currentTime = 100.0 }
    }

    @Test
    fun `onClick clamps negative seek to zero`() {
        val mock = MockPlayer(currentTime = 5.0, duration = 100.0)
        val state = SeekButtonState(mock.player, seekOffset = -10)
        state.observe()

        state.onClick()
        verify { mock.player.currentTime = 0.0 }
    }

    @Test
    fun `onClick does nothing for streams without duration`() {
        val mock = MockPlayer(currentTime = 50.0, duration = Double.NaN)
        val state = SeekButtonState(mock.player, seekOffset = 10)
        state.observe()

        state.onClick()
        verify(exactly = 0) { mock.player.currentTime = any() }
    }

    @Test
    fun `disabled during ads`() {
        val mock = MockPlayer(adPlaying = true)
        val state = SeekButtonState(mock.player, seekOffset = 10)
        state.observe()

        assertFalse(state.enabled)
    }

    @Test
    fun `null player is disabled and onClick does nothing`() {
        val state = SeekButtonState(null, seekOffset = 10)
        state.observe()
        assertFalse(state.enabled)
        state.onClick()
        state.dispose()
    }
}

class MuteButtonStateTest {
    @Test
    fun `registers only the volume change event`() {
        val mock = MockPlayer()
        MuteButtonState(mock.player).observe()
        assertEquals(setOf(PlayerEventTypes.VOLUMECHANGE), mock.playerEventTypes)
    }

    @Test
    fun `seeds muted and updates on volume change`() {
        val mock = MockPlayer(muted = true)
        val state = MuteButtonState(mock.player)
        state.observe()
        assertTrue(state.muted)

        mock.muted = false
        mock.firePlayerEvent(PlayerEventTypes.VOLUMECHANGE)
        assertFalse(state.muted)
    }

    @Test
    fun `onClick toggles player and state`() {
        val mock = MockPlayer(muted = false)
        val state = MuteButtonState(mock.player)
        state.observe()

        state.onClick()
        verify { mock.player.isMuted = true }
        assertTrue(state.muted)
    }

    @Test
    fun `null player gives defaults and onClick does nothing`() {
        val state = MuteButtonState(null)
        state.observe()
        assertFalse(state.muted)
        state.onClick()
        state.dispose()
    }
}

class LoadingStateTest {
    @Test
    fun `registers the playback events`() {
        val mock = MockPlayer()
        LoadingState(mock.player).observe()
        assertEquals(playbackEventTypes - PlayerEventTypes.TIMEUPDATE, mock.playerEventTypes)
    }

    @Test
    fun `loading truth table`() {
        fun state(paused: Boolean, ended: Boolean = false, seeking: Boolean = false, readyState: ReadyState) =
            LoadingState(MockPlayer(paused = paused, ended = ended, seeking = seeking, readyState = readyState).player)
                .apply { observe() }.loading

        assertFalse(state(paused = false, readyState = ReadyState.HAVE_ENOUGH_DATA))
        assertTrue(state(paused = false, readyState = ReadyState.HAVE_CURRENT_DATA))
        assertTrue(state(paused = false, seeking = true, readyState = ReadyState.HAVE_ENOUGH_DATA))
        assertFalse(state(paused = true, readyState = ReadyState.HAVE_CURRENT_DATA))
        assertFalse(state(paused = false, ended = true, readyState = ReadyState.HAVE_CURRENT_DATA))
    }
}

class ErrorStateTest {
    @Test
    fun `registers error and sourcechange`() {
        val mock = MockPlayer()
        ErrorState(mock.player).observe()
        assertEquals(setOf(PlayerEventTypes.ERROR, PlayerEventTypes.SOURCECHANGE), mock.playerEventTypes)
    }

    @Test
    fun `error event sets error and sourcechange clears it`() {
        val exception = mockk<THEOplayerException>()
        val mock = MockPlayer()
        val state = ErrorState(mock.player)
        state.observe()
        assertEquals(null, state.error)

        mock.errorObject = exception
        mock.firePlayerEvent(PlayerEventTypes.ERROR)
        assertEquals(exception, state.error)

        mock.firePlayerEvent(PlayerEventTypes.SOURCECHANGE)
        assertEquals(null, state.error)
    }

    @Test
    fun `seeds initial error and sourcechange clears it`() {
        val exception = mockk<THEOplayerException>()
        val mock = MockPlayer()
        val state = ErrorState(mock.player, initialError = exception)
        state.observe()
        assertEquals(exception, state.error)

        mock.firePlayerEvent(PlayerEventTypes.SOURCECHANGE)
        assertEquals(null, state.error)
    }

    @Test
    fun `null player keeps initial error and registers no listeners`() {
        val exception = mockk<THEOplayerException>()
        val mock = MockPlayer()
        val state = ErrorState(null, initialError = exception)
        state.observe()
        assertEquals(exception, state.error)
        assertTrue(mock.playerListeners.isEmpty())
        state.dispose()
    }
}

class FirstPlayStateTest {
    @Test
    fun `seeds false when paused with empty played`() {
        val mock = MockPlayer(paused = true, playedLength = 0)
        val state = FirstPlayState(mock.player)
        state.observe()
        assertFalse(state.firstPlay)
    }

    @Test
    fun `seeds true when paused with non-empty played`() {
        val mock = MockPlayer(paused = true, playedLength = 1)
        val state = FirstPlayState(mock.player)
        state.observe()
        assertTrue(state.firstPlay)
    }

    @Test
    fun `seeds true when playing`() {
        val mock = MockPlayer(paused = false)
        val state = FirstPlayState(mock.player)
        state.observe()
        assertTrue(state.firstPlay)
    }

    @Test
    fun `play sets firstPlay`() {
        val mock = MockPlayer(paused = true)
        val state = FirstPlayState(mock.player)
        state.observe()

        mock.paused = false
        mock.firePlayerEvent(PlayerEventTypes.PLAY)
        assertTrue(state.firstPlay)
    }

    @Test
    fun `sourcechange resets firstPlay while paused`() {
        val mock = MockPlayer(paused = false)
        val state = FirstPlayState(mock.player)
        state.observe()
        assertTrue(state.firstPlay)

        mock.paused = true
        mock.playedLength = 0
        mock.firePlayerEvent(PlayerEventTypes.SOURCECHANGE)
        assertFalse(state.firstPlay)
    }

    @Test
    fun `sourcechange keeps firstPlay while playing`() {
        val mock = MockPlayer(paused = false)
        val state = FirstPlayState(mock.player)
        state.observe()
        assertTrue(state.firstPlay)

        mock.firePlayerEvent(PlayerEventTypes.SOURCECHANGE)
        assertTrue(state.firstPlay)
    }
}

class VideoSizeStateTest {
    @Test
    fun `registers resize sourcechange and error`() {
        val mock = MockPlayer()
        VideoSizeState(mock.player).observe()
        assertEquals(
            setOf(PlayerEventTypes.RESIZE, PlayerEventTypes.SOURCECHANGE, PlayerEventTypes.ERROR),
            mock.playerEventTypes
        )
    }

    @Test
    fun `seeds size and updates on resize`() {
        val mock = MockPlayer(videoWidth = 1920, videoHeight = 1080)
        val state = VideoSizeState(mock.player)
        state.observe()
        assertEquals(1920, state.videoWidth)
        assertEquals(1080, state.videoHeight)

        mock.videoWidth = 1280
        mock.videoHeight = 720
        mock.firePlayerEvent(PlayerEventTypes.RESIZE)
        assertEquals(1280, state.videoWidth)
        assertEquals(720, state.videoHeight)
    }
}

class AdStateTest {
    @Test
    fun `registers sourcechange error and ad events`() {
        val mock = MockPlayer()
        AdState(mock.player).observe()
        assertEquals(setOf(PlayerEventTypes.SOURCECHANGE, PlayerEventTypes.ERROR), mock.playerEventTypes)
        assertEquals(adEventTypes, mock.adEventTypes)
    }

    @Test
    fun `seeds playingAd and updates on ad events and sourcechange`() {
        val mock = MockPlayer(adPlaying = true)
        val state = AdState(mock.player)
        state.observe()
        assertTrue(state.playingAd)

        mock.adPlaying = false
        mock.fireAdEvent()
        assertFalse(state.playingAd)

        mock.adPlaying = true
        mock.firePlayerEvent(PlayerEventTypes.SOURCECHANGE)
        assertTrue(state.playingAd)
    }
}

class PlaybackRateStateTest {
    @Test
    fun `registers only the ratechange event`() {
        val mock = MockPlayer()
        PlaybackRateState(mock.player).observe()
        assertEquals(setOf(PlayerEventTypes.RATECHANGE), mock.playerEventTypes)
    }

    @Test
    fun `seeds rate and updates on ratechange`() {
        val mock = MockPlayer(playbackRate = 1.5)
        val state = PlaybackRateState(mock.player)
        state.observe()
        assertEquals(1.5, state.playbackRate, 0.0)

        mock.playbackRate = 2.0
        mock.firePlayerEvent(PlayerEventTypes.RATECHANGE)
        assertEquals(2.0, state.playbackRate, 0.0)
    }

    @Test
    fun `select sets player rate and state`() {
        val mock = MockPlayer()
        val state = PlaybackRateState(mock.player)
        state.observe()

        state.select(2.0)
        verify { mock.player.playbackRate = 2.0 }
        assertEquals(2.0, state.playbackRate, 0.0)
    }

    @Test
    fun `null player gives defaults and no listeners`() {
        val state = PlaybackRateState(null)
        state.observe()
        assertEquals(1.0, state.playbackRate, 0.0)
        state.select(2.0)
        state.dispose()
    }
}

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

class SubtitleTrackStateTest {
    private fun playerWithTracks(vararg tracks: MockTextTrack): MockPlayer {
        return MockPlayer().also { mock ->
            mock.textTrackList.addAll(tracks.map { it.track })
        }
    }

    @Test
    fun `registers the track list events with one listener`() {
        val mock = playerWithTracks(MockTextTrack(TextTrackKind.SUBTITLES, TextTrackMode.SHOWING))
        val state = SubtitleTrackState(mock.player)
        state.observe()

        val listener = mock.textTrackListeners.first().second
        assertTrue(mock.textTrackListeners.all { it.second === listener })
        assertEquals(3, mock.textTrackListeners.size)

        state.dispose()
        assertTrue(mock.textTrackListeners.isEmpty())
    }

    @Test
    fun `filters out non-subtitle tracks`() {
        val subtitles = MockTextTrack(TextTrackKind.SUBTITLES, TextTrackMode.DISABLED)
        val captions = MockTextTrack(TextTrackKind.CAPTIONS, TextTrackMode.SHOWING)
        val metadata = MockTextTrack(TextTrackKind.METADATA, TextTrackMode.DISABLED)
        val mock = playerWithTracks(subtitles, captions, metadata)
        val state = SubtitleTrackState(mock.player)
        state.observe()

        assertEquals(listOf(subtitles.track, captions.track), state.subtitleTracks)
        assertEquals(captions.track, state.activeSubtitleTrack)
    }

    @Test
    fun `select shows the track and disables the other subtitles`() {
        val first = MockTextTrack(TextTrackKind.SUBTITLES, TextTrackMode.SHOWING)
        val second = MockTextTrack(TextTrackKind.SUBTITLES, TextTrackMode.DISABLED)
        val metadata = MockTextTrack(TextTrackKind.METADATA, TextTrackMode.DISABLED)
        val mock = playerWithTracks(first, second, metadata)
        val state = SubtitleTrackState(mock.player)
        state.observe()

        state.select(second.track)
        assertEquals(TextTrackMode.DISABLED, first.mode)
        assertEquals(TextTrackMode.SHOWING, second.mode)
        assertEquals(TextTrackMode.DISABLED, metadata.mode)
        assertEquals(second.track, state.activeSubtitleTrack)
    }

    @Test
    fun `select null disables all subtitle tracks`() {
        val showing = MockTextTrack(TextTrackKind.SUBTITLES, TextTrackMode.SHOWING)
        val mock = playerWithTracks(showing)
        val state = SubtitleTrackState(mock.player)
        state.observe()

        state.select(null)
        assertEquals(TextTrackMode.DISABLED, showing.mode)
        assertEquals(null, state.activeSubtitleTrack)
    }

    @Test
    fun `null player gives defaults and no listeners`() {
        val state = SubtitleTrackState(null)
        state.observe()
        assertTrue(state.subtitleTracks.isEmpty())
        assertEquals(null, state.activeSubtitleTrack)
        state.dispose()
    }
}

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
