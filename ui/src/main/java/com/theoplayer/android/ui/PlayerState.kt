package com.theoplayer.android.ui

import android.view.View
import androidx.compose.runtime.*
import com.theoplayer.android.api.THEOplayerView
import com.theoplayer.android.api.error.THEOplayerException
import com.theoplayer.android.api.event.EventListener
import com.theoplayer.android.api.event.player.*
import com.theoplayer.android.api.player.Player
import com.theoplayer.android.api.player.ReadyState

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
    val error: THEOplayerException?
    var fullscreen: Boolean
    val loading: Boolean
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
        updateCurrentTimeAndPlaybackState()
        updateDuration()
    }
    val errorListener = EventListener<ErrorEvent> { event ->
        error = event.errorObject
        updateCurrentTimeAndPlaybackState()
        updateDuration()
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

    init {
        updateCurrentTimeAndPlaybackState()
        updateDuration()
        updateVolumeAndMuted()
        updateFullscreen()
        player?.addEventListener(PlayerEventTypes.PLAY, playListener)
        player?.addEventListener(PlayerEventTypes.PAUSE, pauseListener)
        player?.addEventListener(PlayerEventTypes.ENDED, endedListener)
        player?.addEventListener(PlayerEventTypes.TIMEUPDATE, timeUpdateListener)
        player?.addEventListener(PlayerEventTypes.DURATIONCHANGE, durationChangeListener)
        player?.addEventListener(PlayerEventTypes.SEEKING, seekingListener)
        player?.addEventListener(PlayerEventTypes.SEEKED, seekedListener)
        player?.addEventListener(PlayerEventTypes.READYSTATECHANGE, readyStateChangeListener)
        player?.addEventListener(PlayerEventTypes.VOLUMECHANGE, volumeChangeListener)
        player?.addEventListener(PlayerEventTypes.SOURCECHANGE, sourceChangeListener)
        player?.addEventListener(PlayerEventTypes.ERROR, errorListener)
        fullscreenHandler?.onFullscreenChangeListener = fullscreenListener
    }

    fun dispose() {
        player?.removeEventListener(PlayerEventTypes.PLAY, playListener)
        player?.removeEventListener(PlayerEventTypes.PAUSE, pauseListener)
        player?.removeEventListener(PlayerEventTypes.ENDED, endedListener)
        player?.removeEventListener(PlayerEventTypes.TIMEUPDATE, timeUpdateListener)
        player?.removeEventListener(PlayerEventTypes.DURATIONCHANGE, durationChangeListener)
        player?.removeEventListener(PlayerEventTypes.SEEKING, seekingListener)
        player?.removeEventListener(PlayerEventTypes.SEEKED, seekedListener)
        player?.removeEventListener(PlayerEventTypes.READYSTATECHANGE, readyStateChangeListener)
        player?.removeEventListener(PlayerEventTypes.VOLUMECHANGE, volumeChangeListener)
        player?.removeEventListener(PlayerEventTypes.SOURCECHANGE, sourceChangeListener)
        player?.removeEventListener(PlayerEventTypes.ERROR, errorListener)
        fullscreenHandler?.onFullscreenChangeListener = null
    }
}