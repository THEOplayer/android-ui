package com.theoplayer.android.ui

import android.view.View
import androidx.compose.runtime.*
import com.theoplayer.android.api.THEOplayerView
import com.theoplayer.android.api.error.THEOplayerException
import com.theoplayer.android.api.event.EventListener
import com.theoplayer.android.api.event.player.*
import com.theoplayer.android.api.event.track.mediatrack.AbstractTargetQualityChangedEvent
import com.theoplayer.android.api.event.track.mediatrack.video.ActiveQualityChangedEvent
import com.theoplayer.android.api.event.track.mediatrack.video.VideoTrackEventTypes
import com.theoplayer.android.api.event.track.mediatrack.video.list.AddTrackEvent
import com.theoplayer.android.api.event.track.mediatrack.video.list.RemoveTrackEvent
import com.theoplayer.android.api.event.track.mediatrack.video.list.TrackListChangeEvent
import com.theoplayer.android.api.event.track.mediatrack.video.list.VideoTrackListEventTypes
import com.theoplayer.android.api.player.Player
import com.theoplayer.android.api.player.ReadyState
import com.theoplayer.android.api.player.track.mediatrack.MediaTrack
import com.theoplayer.android.api.player.track.mediatrack.quality.VideoQuality

interface PlayerState {
    val player: Player?

    val currentTime: Double
    val duration: Double
    val seekable: TimeRanges
    val paused: Boolean
    val ended: Boolean
    val seeking: Boolean
    val readyState: ReadyState
    var volume: Double
    var muted: Boolean
    var playbackRate: Double
    val firstPlay: Boolean
    val error: THEOplayerException?
    var fullscreen: Boolean

    val loading: Boolean
    val streamType: StreamType

    val activeVideoQuality: VideoQuality?
    var targetVideoQuality: VideoQuality?
    val videoQualities: List<VideoQuality>
}

enum class StreamType {
    Vod,
    Live,
    Dvr
}

@Composable
fun rememberPlayerState(theoplayerView: THEOplayerView?): PlayerState {
    val state = remember(theoplayerView) { PlayerStateImpl(theoplayerView) }
    DisposableEffect(state) {
        onDispose {
            state.dispose()
        }
    }
    return state
}

private class PlayerStateImpl(private val theoplayerView: THEOplayerView?) : PlayerState {
    override val player = theoplayerView?.player
    override var currentTime by mutableStateOf(0.0)
    override var duration by mutableStateOf(Double.NaN)
    override var seekable by mutableStateOf(TimeRanges(listOf()))
    override var paused by mutableStateOf(true)
    override var ended by mutableStateOf(false)
    override var seeking by mutableStateOf(false)
    override var readyState by mutableStateOf(ReadyState.HAVE_NOTHING)
    override var firstPlay by mutableStateOf(false)
    override var error by mutableStateOf<THEOplayerException?>(null)

    private fun updateCurrentTime() {
        currentTime = player?.currentTime ?: 0.0
        seekable = player?.seekable?.let { TimeRanges.fromTHEOplayer(it) } ?: TimeRanges(listOf())
    }

    private fun updateDuration() {
        duration = player?.duration ?: Double.NaN
    }

    private fun updateCurrentTimeAndPlaybackState() {
        updateCurrentTime()
        paused = player?.isPaused ?: true
        ended = player?.isEnded ?: false
        seeking = player?.isSeeking ?: false
        readyState = player?.readyState ?: ReadyState.HAVE_NOTHING
        if (!paused) {
            firstPlay = true
        }
    }

    val playListener = EventListener<PlayEvent> { updateCurrentTimeAndPlaybackState() }
    val pauseListener = EventListener<PauseEvent> { updateCurrentTimeAndPlaybackState() }
    val endedListener = EventListener<EndedEvent> { updateCurrentTimeAndPlaybackState() }
    val timeUpdateListener = EventListener<TimeUpdateEvent> { updateCurrentTime() }
    val durationChangeListener = EventListener<DurationChangeEvent> { updateDuration() }
    val seekingListener = EventListener<SeekingEvent> { updateCurrentTimeAndPlaybackState() }
    val seekedListener = EventListener<SeekedEvent> { updateCurrentTimeAndPlaybackState() }
    val readyStateChangeListener =
        EventListener<ReadyStateChangeEvent> { updateCurrentTimeAndPlaybackState() }
    val sourceChangeListener = EventListener<SourceChangeEvent> {
        error = null
        firstPlay = false
        updateCurrentTimeAndPlaybackState()
        updateDuration()
        updateActiveVideoTrack()
    }
    val errorListener = EventListener<ErrorEvent> { event ->
        error = event.errorObject
        updateCurrentTimeAndPlaybackState()
        updateDuration()
        updateActiveVideoTrack()
    }

    private var _volume by mutableStateOf(1.0)
    private var _muted by mutableStateOf(false)
    override var volume: Double
        get() = _volume
        set(value) {
            _volume = value
            player?.volume = value
        }
    override var muted: Boolean
        get() = _muted
        set(value) {
            _muted = value
            player?.isMuted = value
        }

    private fun updateVolumeAndMuted() {
        _volume = player?.volume ?: 1.0
        _muted = player?.isMuted ?: false
    }

    val volumeChangeListener = EventListener<VolumeChangeEvent> { updateVolumeAndMuted() }

    private var _playbackRate by mutableStateOf(1.0)
    override var playbackRate: Double
        get() = _playbackRate
        set(value) {
            _playbackRate = value
            player?.playbackRate = value
        }

    private fun updatePlaybackRate() {
        _playbackRate = player?.playbackRate ?: 1.0
    }

    val rateChangeListener = EventListener<RateChangeEvent> { updatePlaybackRate() }

    private val fullscreenHandler: FullscreenHandler? =
        theoplayerView?.findViewById<View>(com.theoplayer.android.R.id.theo_player_container)
            ?.let { FullscreenHandlerImpl(it) }
    private var _fullscreen by mutableStateOf(false)
    override var fullscreen: Boolean
        get() = _fullscreen
        set(value) {
            _fullscreen = value
            if (value) {
                fullscreenHandler?.requestFullscreen()
            } else {
                fullscreenHandler?.exitFullscreen()
            }
        }

    private fun updateFullscreen() {
        _fullscreen = fullscreenHandler?.fullscreen ?: false
    }

    val fullscreenListener =
        FullscreenHandler.OnFullscreenChangeListener { updateFullscreen() }

    override val loading by derivedStateOf {
        !paused && !ended && (seeking || readyState.ordinal < ReadyState.HAVE_FUTURE_DATA.ordinal)
    }

    override val streamType by derivedStateOf {
        if (duration.isInfinite()) {
            val slidingWindow = (seekable.lastEnd ?: 0.0) - (seekable.firstStart ?: 0.0)
            // TODO Make DVR threshold configurable?
            if (slidingWindow >= 60) {
                StreamType.Dvr
            } else {
                StreamType.Live
            }
        } else {
            StreamType.Vod
        }
    }

    private var activeVideoTrack: MediaTrack<VideoQuality>? = null
    override val videoQualities = mutableStateListOf<VideoQuality>()
    override var activeVideoQuality by mutableStateOf<VideoQuality?>(null)
        private set
    private var _targetVideoQuality by mutableStateOf<VideoQuality?>(null)
    override var targetVideoQuality: VideoQuality?
        get() = _targetVideoQuality
        set(value) {
            _targetVideoQuality = value
            activeVideoTrack?.targetQuality = value
        }

    private fun updateActiveVideoTrack() {
        setActiveVideoTrack(player?.videoTracks?.find { it.isEnabled })
    }

    private fun setActiveVideoTrack(videoTrack: MediaTrack<VideoQuality>?) {
        val oldActiveVideoTrack = activeVideoTrack
        if (oldActiveVideoTrack == videoTrack) {
            return
        }
        oldActiveVideoTrack?.removeEventListener(
            VideoTrackEventTypes.ACTIVEQUALITYCHANGEDEVENT,
            videoActiveQualityChangeListener
        )
        oldActiveVideoTrack?.removeEventListener(
            VideoTrackEventTypes.TARGETQUALITYCHANGEDEVENT,
            videoTargetQualityChangeListener
        )
        activeVideoTrack = videoTrack
        videoQualities.clear()
        videoTrack?.qualities?.let { videoQualities.addAll(it) }
        updateActiveVideoQuality()
        updateTargetVideoQuality()
        videoTrack?.addEventListener(
            VideoTrackEventTypes.ACTIVEQUALITYCHANGEDEVENT,
            videoActiveQualityChangeListener
        )
        videoTrack?.addEventListener(
            VideoTrackEventTypes.TARGETQUALITYCHANGEDEVENT,
            videoTargetQualityChangeListener
        )
    }

    private fun updateActiveVideoQuality() {
        activeVideoQuality = activeVideoTrack?.activeQuality
    }

    private fun updateTargetVideoQuality() {
        _targetVideoQuality = activeVideoTrack?.targetQuality
    }

    val videoAddTrackListener = EventListener<AddTrackEvent> { updateActiveVideoTrack() }
    val videoRemoveTrackListener = EventListener<RemoveTrackEvent> { updateActiveVideoTrack() }
    val videoTrackListChangeListener =
        EventListener<TrackListChangeEvent> { updateActiveVideoTrack() }
    val videoActiveQualityChangeListener =
        EventListener<ActiveQualityChangedEvent> { updateActiveVideoQuality() }
    val videoTargetQualityChangeListener =
        EventListener<AbstractTargetQualityChangedEvent<VideoQuality>> { updateTargetVideoQuality() }

    init {
        updateCurrentTimeAndPlaybackState()
        updateDuration()
        updateVolumeAndMuted()
        updatePlaybackRate()
        updateFullscreen()
        updateActiveVideoTrack()
        player?.addEventListener(PlayerEventTypes.PLAY, playListener)
        player?.addEventListener(PlayerEventTypes.PAUSE, pauseListener)
        player?.addEventListener(PlayerEventTypes.ENDED, endedListener)
        player?.addEventListener(PlayerEventTypes.TIMEUPDATE, timeUpdateListener)
        player?.addEventListener(PlayerEventTypes.DURATIONCHANGE, durationChangeListener)
        player?.addEventListener(PlayerEventTypes.SEEKING, seekingListener)
        player?.addEventListener(PlayerEventTypes.SEEKED, seekedListener)
        player?.addEventListener(PlayerEventTypes.READYSTATECHANGE, readyStateChangeListener)
        player?.addEventListener(PlayerEventTypes.VOLUMECHANGE, volumeChangeListener)
        player?.addEventListener(PlayerEventTypes.RATECHANGE, rateChangeListener)
        player?.addEventListener(PlayerEventTypes.SOURCECHANGE, sourceChangeListener)
        player?.addEventListener(PlayerEventTypes.ERROR, errorListener)
        player?.videoTracks?.addEventListener(
            VideoTrackListEventTypes.ADDTRACK,
            videoAddTrackListener
        )
        player?.videoTracks?.addEventListener(
            VideoTrackListEventTypes.REMOVETRACK,
            videoRemoveTrackListener
        )
        player?.videoTracks?.addEventListener(
            VideoTrackListEventTypes.TRACKLISTCHANGE,
            videoTrackListChangeListener
        )
        fullscreenHandler?.onFullscreenChangeListener = fullscreenListener
    }

    fun dispose() {
        setActiveVideoTrack(null)
        player?.removeEventListener(PlayerEventTypes.PLAY, playListener)
        player?.removeEventListener(PlayerEventTypes.PAUSE, pauseListener)
        player?.removeEventListener(PlayerEventTypes.ENDED, endedListener)
        player?.removeEventListener(PlayerEventTypes.TIMEUPDATE, timeUpdateListener)
        player?.removeEventListener(PlayerEventTypes.DURATIONCHANGE, durationChangeListener)
        player?.removeEventListener(PlayerEventTypes.SEEKING, seekingListener)
        player?.removeEventListener(PlayerEventTypes.SEEKED, seekedListener)
        player?.removeEventListener(PlayerEventTypes.READYSTATECHANGE, readyStateChangeListener)
        player?.removeEventListener(PlayerEventTypes.VOLUMECHANGE, volumeChangeListener)
        player?.removeEventListener(PlayerEventTypes.RATECHANGE, rateChangeListener)
        player?.removeEventListener(PlayerEventTypes.SOURCECHANGE, sourceChangeListener)
        player?.removeEventListener(PlayerEventTypes.ERROR, errorListener)
        player?.videoTracks?.removeEventListener(
            VideoTrackListEventTypes.ADDTRACK,
            videoAddTrackListener
        )
        player?.videoTracks?.removeEventListener(
            VideoTrackListEventTypes.REMOVETRACK,
            videoRemoveTrackListener
        )
        player?.videoTracks?.removeEventListener(
            VideoTrackListEventTypes.TRACKLISTCHANGE,
            videoTrackListChangeListener
        )
        fullscreenHandler?.onFullscreenChangeListener = null
    }
}