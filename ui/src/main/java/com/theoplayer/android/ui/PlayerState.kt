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
    val paused: State<Boolean>
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
    override val paused = mutableStateOf(true)
    override val error = mutableStateOf<THEOplayerException?>(null)

    val updateCurrentTime = {
        currentTime.value = player?.currentTime ?: 0.0
    }
    val updateDuration = {
        duration.value = player?.duration ?: Double.NaN
    }
    val updatePaused = {
        paused.value = player?.isPaused ?: true
    }
    val playListener = EventListener<PlayEvent> { updatePaused() }
    val pauseListener = EventListener<PauseEvent> { updatePaused() }
    val timeUpdateListener = EventListener<TimeUpdateEvent> { updateCurrentTime() }
    val durationChangeListener = EventListener<DurationChangeEvent> { updateDuration() }
    val sourceChangeListener = EventListener<SourceChangeEvent> {
        error.value = null
        updateCurrentTime()
        updateDuration()
        updatePaused()
    }
    val errorListener = EventListener<ErrorEvent> { event ->
        error.value = event.errorObject
        updateCurrentTime()
        updateDuration()
        updatePaused()
    }

    init {
        updatePaused()
        player?.addEventListener(PlayerEventTypes.PLAY, playListener)
        player?.addEventListener(PlayerEventTypes.PAUSE, pauseListener)
        player?.addEventListener(PlayerEventTypes.TIMEUPDATE, timeUpdateListener)
        player?.addEventListener(PlayerEventTypes.DURATIONCHANGE, durationChangeListener)
        player?.addEventListener(PlayerEventTypes.SOURCECHANGE, sourceChangeListener)
        player?.addEventListener(PlayerEventTypes.ERROR, errorListener)
    }

    fun dispose() {
        player?.removeEventListener(PlayerEventTypes.PLAY, playListener)
        player?.removeEventListener(PlayerEventTypes.PAUSE, pauseListener)
        player?.removeEventListener(PlayerEventTypes.TIMEUPDATE, timeUpdateListener)
        player?.removeEventListener(PlayerEventTypes.DURATIONCHANGE, durationChangeListener)
        player?.removeEventListener(PlayerEventTypes.SOURCECHANGE, sourceChangeListener)
        player?.removeEventListener(PlayerEventTypes.ERROR, errorListener)
    }
}