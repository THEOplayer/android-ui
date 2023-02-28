package com.theoplayer.android.ui

import androidx.compose.runtime.*
import com.theoplayer.android.api.error.THEOplayerException
import com.theoplayer.android.api.event.EventListener
import com.theoplayer.android.api.event.player.*
import com.theoplayer.android.api.player.Player

interface PlayerState {
    val player: Player?

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
    override val paused = mutableStateOf(true)
    val updatePaused = {
        paused.value = player?.isPaused ?: true
    }
    val playListener = EventListener<PlayEvent> { updatePaused() }
    val pauseListener = EventListener<PauseEvent> { updatePaused() }

    override val error = mutableStateOf<THEOplayerException?>(null)
    val sourceChangeListener = EventListener<SourceChangeEvent> { error.value = null }
    val errorListener = EventListener<ErrorEvent> { event -> error.value = event.errorObject }

    init {
        updatePaused()
        player?.addEventListener(PlayerEventTypes.PLAY, playListener)
        player?.addEventListener(PlayerEventTypes.PAUSE, pauseListener)
        player?.addEventListener(PlayerEventTypes.SOURCECHANGE, sourceChangeListener)
        player?.addEventListener(PlayerEventTypes.ERROR, errorListener)
    }

    fun dispose() {
        player?.removeEventListener(PlayerEventTypes.PLAY, playListener)
        player?.removeEventListener(PlayerEventTypes.PAUSE, pauseListener)
        player?.removeEventListener(PlayerEventTypes.SOURCECHANGE, sourceChangeListener)
        player?.removeEventListener(PlayerEventTypes.ERROR, errorListener)
    }
}