package com.theoplayer.android.ui

import com.theoplayer.android.api.ads.Ads
import com.theoplayer.android.api.event.Event
import com.theoplayer.android.api.event.EventDispatcher
import com.theoplayer.android.api.event.EventListener
import com.theoplayer.android.api.event.EventType
import com.theoplayer.android.api.event.ads.AdBreakBeginEvent
import com.theoplayer.android.api.event.ads.AdBreakEndEvent
import com.theoplayer.android.api.event.ads.AdEvent
import com.theoplayer.android.api.event.ads.AdSkipEvent
import com.theoplayer.android.api.event.ads.AdsEventTypes
import com.theoplayer.android.api.error.THEOplayerException
import com.theoplayer.android.api.event.player.DurationChangeEvent
import com.theoplayer.android.api.event.player.EndedEvent
import com.theoplayer.android.api.event.player.ErrorEvent
import com.theoplayer.android.api.event.player.PauseEvent
import com.theoplayer.android.api.event.player.PlayEvent
import com.theoplayer.android.api.event.player.PlayerEvent
import com.theoplayer.android.api.event.player.PlayerEventTypes
import com.theoplayer.android.api.event.player.PresentationModeChange
import com.theoplayer.android.api.event.player.RateChangeEvent
import com.theoplayer.android.api.event.player.ReadyStateChangeEvent
import com.theoplayer.android.api.event.player.ResizeEvent
import com.theoplayer.android.api.event.player.SeekedEvent
import com.theoplayer.android.api.event.player.SeekingEvent
import com.theoplayer.android.api.event.player.SourceChangeEvent
import com.theoplayer.android.api.event.player.TimeUpdateEvent
import com.theoplayer.android.api.event.player.VolumeChangeEvent
import com.theoplayer.android.api.event.track.TrackEvent
import com.theoplayer.android.api.event.track.mediatrack.audio.list.AddTrackEvent as AudioAddTrackEvent
import com.theoplayer.android.api.event.track.mediatrack.audio.list.AudioTrackListEventTypes
import com.theoplayer.android.api.event.track.mediatrack.audio.list.RemoveTrackEvent as AudioRemoveTrackEvent
import com.theoplayer.android.api.event.track.mediatrack.audio.list.TrackListChangeEvent as AudioTrackListChangeEvent
import com.theoplayer.android.api.event.track.mediatrack.video.ActiveQualityChangedEvent
import com.theoplayer.android.api.event.track.mediatrack.video.TargetQualityChangedEvent
import com.theoplayer.android.api.event.track.mediatrack.video.VideoTrackEventTypes
import com.theoplayer.android.api.event.track.mediatrack.video.list.AddTrackEvent as VideoAddTrackEvent
import com.theoplayer.android.api.event.track.mediatrack.video.list.RemoveTrackEvent as VideoRemoveTrackEvent
import com.theoplayer.android.api.event.track.mediatrack.video.list.TrackListChangeEvent as VideoTrackListChangeEvent
import com.theoplayer.android.api.event.track.mediatrack.video.list.VideoTrackListEventTypes
import com.theoplayer.android.api.event.track.texttrack.list.AddTrackEvent as TextAddTrackEvent
import com.theoplayer.android.api.event.track.texttrack.list.RemoveTrackEvent as TextRemoveTrackEvent
import com.theoplayer.android.api.event.track.texttrack.list.TextTrackListEventTypes
import com.theoplayer.android.api.event.track.texttrack.list.TrackListChangeEvent as TextTrackListChangeEvent
import com.theoplayer.android.api.event.track.tracklist.TrackListEvent
import com.theoplayer.android.api.player.Player
import com.theoplayer.android.api.player.ReadyState
import com.theoplayer.android.api.player.track.mediatrack.MediaTrack
import com.theoplayer.android.api.player.track.mediatrack.MediaTrackList
import com.theoplayer.android.api.player.track.mediatrack.quality.AudioQuality
import com.theoplayer.android.api.player.track.mediatrack.quality.QualityList
import com.theoplayer.android.api.player.track.mediatrack.quality.VideoQuality
import com.theoplayer.android.api.player.track.texttrack.TextTrack
import com.theoplayer.android.api.player.track.texttrack.TextTrackKind
import com.theoplayer.android.api.player.track.texttrack.TextTrackList
import com.theoplayer.android.api.player.track.texttrack.TextTrackMode
import com.theoplayer.android.api.timerange.TimeRange
import com.theoplayer.android.api.timerange.TimeRanges
import io.mockk.every
import io.mockk.mockk

/**
 * A mocked [Player] that records the listeners added to it and to its [Ads],
 * so tests can fire events at them.
 */
internal class MockPlayer(
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
        PlayerEventTypes.PLAY -> mockk<PlayEvent>()
        PlayerEventTypes.PAUSE -> mockk<PauseEvent>()
        PlayerEventTypes.ENDED -> mockk<EndedEvent>()
        PlayerEventTypes.SEEKING -> mockk<SeekingEvent>()
        PlayerEventTypes.SEEKED -> mockk<SeekedEvent>()
        PlayerEventTypes.READYSTATECHANGE -> mockk<ReadyStateChangeEvent>()
        PlayerEventTypes.SOURCECHANGE -> mockk<SourceChangeEvent>()
        PlayerEventTypes.ERROR -> mockk<ErrorEvent> {
            every { errorObject } returns this@MockPlayer.errorObject
        }
        PlayerEventTypes.TIMEUPDATE -> mockk<TimeUpdateEvent>()
        PlayerEventTypes.DURATIONCHANGE -> mockk<DurationChangeEvent>()
        PlayerEventTypes.VOLUMECHANGE -> mockk<VolumeChangeEvent>()
        PlayerEventTypes.RESIZE -> mockk<ResizeEvent>()
        PlayerEventTypes.RATECHANGE -> mockk<RateChangeEvent>()
        PlayerEventTypes.PRESENTATIONMODECHANGE -> mockk<PresentationModeChange>()
        else -> mockk()
    }

    fun fireAdEvent() {
        adListeners.toList().forEach { (type, listener) ->
            @Suppress("UNCHECKED_CAST")
            (listener as EventListener<AdEvent<*>>).handleEvent(
                when (type) {
                    AdsEventTypes.AD_BREAK_BEGIN -> mockk<AdBreakBeginEvent>()
                    AdsEventTypes.AD_SKIP -> mockk<AdSkipEvent>()
                    AdsEventTypes.AD_BREAK_END -> mockk<AdBreakEndEvent>()
                    else -> mockk()
                }
            )
        }
    }
}

internal fun mockTimeRanges(ranges: List<Pair<Double, Double>>): TimeRanges = mockk {
    every { iterator() } answers {
        ranges.map { (start, end) ->
            mockk<TimeRange> {
                every { this@mockk.start } returns start
                every { this@mockk.end } returns end
            }
        }.toMutableList().listIterator()
    }
}

internal var nextTrackUid = 0

/**
 * A mocked track list that records the listeners added to it.
 */
internal inline fun <reified L> mockTrackList(
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
internal class MockAudioTrack(var enabled: Boolean = false) {
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
internal class MockTextTrack(kind: TextTrackKind, var mode: TextTrackMode) {
    val uid = nextTrackUid++
    private val kindType = kind.type
    val track: TextTrack = mockk {
        every { this@mockk.kind } returns kindType
        every { this@mockk.mode } answers { this@MockTextTrack.mode }
        every { setMode(any()) } answers { this@MockTextTrack.mode = firstArg() }
        every { this@mockk.uid } returns this@MockTextTrack.uid
    }
}

internal fun mockVideoQuality(): VideoQuality {
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
internal class MockVideoTrack(
    var enabled: Boolean = false,
    var activeQuality: VideoQuality? = null,
    var targetQuality: VideoQuality? = null,
    var qualities: List<VideoQuality> = listOf()
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
            mockk<QualityList<VideoQuality>> {
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

    // Fires at all listeners with an event matching their registered event type.
    fun fireEvent() {
        listeners.toList().forEach { (type, listener) ->
            @Suppress("UNCHECKED_CAST")
            (listener as EventListener<Event<*>>).handleEvent(
                when (type) {
                    VideoTrackEventTypes.ACTIVEQUALITYCHANGEDEVENT ->
                        mockk<ActiveQualityChangedEvent>()
                    VideoTrackEventTypes.TARGETQUALITYCHANGEDEVENT ->
                        mockk<TargetQualityChangedEvent>()
                    else -> mockk()
                }
            )
        }
    }
}

internal fun MutableList<Pair<EventType<*>, EventListener<*>>>.fireTrackListEvent() {
    toList().forEach { (type, listener) ->
        @Suppress("UNCHECKED_CAST")
        (listener as EventListener<Event<*>>).handleEvent(
            when (type) {
                AudioTrackListEventTypes.ADDTRACK -> mockk<AudioAddTrackEvent>()
                AudioTrackListEventTypes.REMOVETRACK -> mockk<AudioRemoveTrackEvent>()
                AudioTrackListEventTypes.TRACKLISTCHANGE -> mockk<AudioTrackListChangeEvent>()
                VideoTrackListEventTypes.ADDTRACK -> mockk<VideoAddTrackEvent>()
                VideoTrackListEventTypes.REMOVETRACK -> mockk<VideoRemoveTrackEvent>()
                VideoTrackListEventTypes.TRACKLISTCHANGE -> mockk<VideoTrackListChangeEvent>()
                TextTrackListEventTypes.ADDTRACK -> mockk<TextAddTrackEvent>()
                TextTrackListEventTypes.REMOVETRACK -> mockk<TextRemoveTrackEvent>()
                TextTrackListEventTypes.TRACKLISTCHANGE -> mockk<TextTrackListChangeEvent>()
                else -> mockk()
            }
        )
    }
}

internal val playbackEventTypes = setOf(
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

internal val adEventTypes = setOf(
    AdsEventTypes.AD_BREAK_BEGIN,
    AdsEventTypes.AD_SKIP,
    AdsEventTypes.AD_BREAK_END
)
