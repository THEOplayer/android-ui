package com.theoplayer.android.ui

import androidx.compose.runtime.*
import com.theoplayer.android.api.THEOplayerView
import com.theoplayer.android.api.error.THEOplayerException
import com.theoplayer.android.api.event.EventListener
import com.theoplayer.android.api.event.player.*
import com.theoplayer.android.api.fullscreen.FullScreenChangeListener
import com.theoplayer.android.api.player.Player

interface PlayerState {
    val player: Player?

    val currentTime: Double
    val duration: Double
    val seekable: TimeRanges
    val paused: Boolean
    val ended: Boolean
    val seeking: Boolean
    val error: THEOplayerException?
    var fullscreen: Boolean
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
    override var error by mutableStateOf<THEOplayerException?>(null)

    val updateCurrentTime = {
        currentTime = player?.currentTime ?: 0.0
        seekable = player?.seekable?.let { toTimeRanges(it) } ?: TimeRanges(listOf())
    }
    val updateDuration = {
        duration = player?.duration ?: Double.NaN
    }
    val updateCurrentTimeAndPlaybackState = {
        updateCurrentTime()
        paused = player?.isPaused ?: true
        ended = player?.isEnded ?: false
        seeking = player?.isSeeking ?: false
    }
    val playListener = EventListener<PlayEvent> { updateCurrentTimeAndPlaybackState() }
    val pauseListener = EventListener<PauseEvent> { updateCurrentTimeAndPlaybackState() }
    val endedListener = EventListener<EndedEvent> { updateCurrentTimeAndPlaybackState() }
    val timeUpdateListener = EventListener<TimeUpdateEvent> { updateCurrentTime() }
    val durationChangeListener = EventListener<DurationChangeEvent> { updateDuration() }
    val seekingListener =
        EventListener<SeekingEvent> { updateCurrentTimeAndPlaybackState() }
    val seekedListener = EventListener<SeekedEvent> { updateCurrentTimeAndPlaybackState() }
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

    private var _fullscreen by mutableStateOf(false)
    override var fullscreen: Boolean
        get() = _fullscreen
        set(value) {
            _fullscreen = value
            if (value) {
                theoplayerView?.fullScreenManager?.requestFullScreen()
            } else {
                theoplayerView?.fullScreenManager?.exitFullScreen()
            }
        }
    val fullscreenListener = object : FullScreenChangeListener {
        override fun onEnterFullScreen() {
            _fullscreen = true
        }

        override fun onExitFullScreen() {
            _fullscreen = false
        }
    }

    init {
        updateCurrentTimeAndPlaybackState()
        updateDuration()
        player?.addEventListener(PlayerEventTypes.PLAY, playListener)
        player?.addEventListener(PlayerEventTypes.PAUSE, pauseListener)
        player?.addEventListener(PlayerEventTypes.ENDED, endedListener)
        player?.addEventListener(PlayerEventTypes.TIMEUPDATE, timeUpdateListener)
        player?.addEventListener(PlayerEventTypes.DURATIONCHANGE, durationChangeListener)
        player?.addEventListener(PlayerEventTypes.SEEKING, seekingListener)
        player?.addEventListener(PlayerEventTypes.SEEKED, seekedListener)
        player?.addEventListener(PlayerEventTypes.SOURCECHANGE, sourceChangeListener)
        player?.addEventListener(PlayerEventTypes.ERROR, errorListener)
        theoplayerView?.fullScreenManager?.addFullScreenChangeListener(fullscreenListener)
    }

    fun dispose() {
        player?.removeEventListener(PlayerEventTypes.PLAY, playListener)
        player?.removeEventListener(PlayerEventTypes.PAUSE, pauseListener)
        player?.removeEventListener(PlayerEventTypes.ENDED, endedListener)
        player?.removeEventListener(PlayerEventTypes.TIMEUPDATE, timeUpdateListener)
        player?.removeEventListener(PlayerEventTypes.DURATIONCHANGE, durationChangeListener)
        player?.removeEventListener(PlayerEventTypes.SEEKING, seekingListener)
        player?.removeEventListener(PlayerEventTypes.SEEKED, seekedListener)
        player?.removeEventListener(PlayerEventTypes.SOURCECHANGE, sourceChangeListener)
        player?.removeEventListener(PlayerEventTypes.ERROR, errorListener)
        theoplayerView?.fullScreenManager?.removeFullScreenChangeListener(fullscreenListener)
    }
}