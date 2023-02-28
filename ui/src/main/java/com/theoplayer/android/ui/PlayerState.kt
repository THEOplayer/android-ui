package com.theoplayer.android.ui

import androidx.compose.runtime.*
import com.theoplayer.android.api.error.THEOplayerException
import com.theoplayer.android.api.event.EventListener
import com.theoplayer.android.api.event.player.*
import com.theoplayer.android.api.player.Player

interface PlayerState {
    val player: Player?

    val currentTime: State<Double>
    val duration: State<Double>
    val seekable: State<TimeRanges>
    val paused: State<Boolean>
    val ended: State<Boolean>
    val seeking: State<Boolean>
    val error: State<THEOplayerException?>
}

@Composable
fun rememberPlayerState(player: Player?): PlayerState {
    val state = remember(player) { PlayerStateImpl(player) }
    DisposableEffect(state) {
        onDispose {
            state.dispose()
        }
    }
    return state
}

private class PlayerStateImpl(override val player: Player?) : PlayerState {
    override val currentTime = mutableStateOf(0.0)
    override val duration = mutableStateOf(Double.NaN)
    override val seekable = mutableStateOf(TimeRanges(listOf()))
    override val paused = mutableStateOf(true)
    override val ended = mutableStateOf(false)
    override val seeking = mutableStateOf(false)
    override val error = mutableStateOf<THEOplayerException?>(null)

    val updateCurrentTime = {
        currentTime.value = player?.currentTime ?: 0.0
        seekable.value = player?.seekable?.let { toTimeRanges(it) } ?: TimeRanges(listOf())
    }
    val updateDuration = {
        duration.value = player?.duration ?: Double.NaN
    }
    val updateCurrentTimeAndPlaybackState = {
        updateCurrentTime()
        paused.value = player?.isPaused ?: true
        ended.value = player?.isEnded ?: false
        seeking.value = player?.isSeeking ?: false
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
        error.value = null
        updateCurrentTimeAndPlaybackState()
        updateDuration()
    }
    val errorListener = EventListener<ErrorEvent> { event ->
        error.value = event.errorObject
        updateCurrentTimeAndPlaybackState()
        updateDuration()
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
    }
}