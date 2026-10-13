package com.theoplayer.android.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.theoplayer.android.api.event.EventListener
import com.theoplayer.android.api.event.player.PlayerEvent
import com.theoplayer.android.api.event.player.PlayerEventTypes
import com.theoplayer.android.api.event.player.SourceChangeEvent
import com.theoplayer.android.api.player.Player

/**
 * State holder for UI that should only appear once playback has started for the first time.
 *
 * Create an instance with [rememberFirstPlayState].
 */
@ExperimentalPlayerStateApi
class FirstPlayState internal constructor(private val player: Player?) {
    private val listeners = PlayerStateListeners()

    /**
     * Whether playback has started for the first time.
     */
    var firstPlay by mutableStateOf(false)
        private set

    private fun updatePlaying() {
        if (player?.isPaused == false) {
            firstPlay = true
        }
    }

    internal fun observe() {
        val player = player ?: return
        firstPlay = !player.isPaused || (player.played?.length() ?: 0) > 0
        listeners.run {
            val listener = EventListener<PlayerEvent<*>> { event ->
                if (event is SourceChangeEvent) {
                    firstPlay = false
                }
                updatePlaying()
            }
            player.add(PlayerEventTypes.PLAY, listener)
            player.add(PlayerEventTypes.PAUSE, listener)
            player.add(PlayerEventTypes.ENDED, listener)
            player.add(PlayerEventTypes.SEEKING, listener)
            player.add(PlayerEventTypes.SEEKED, listener)
            player.add(PlayerEventTypes.READYSTATECHANGE, listener)
            player.add(PlayerEventTypes.SOURCECHANGE, listener)
            player.add(PlayerEventTypes.ERROR, listener)
        }
    }

    internal fun dispose() {
        listeners.removeAll()
    }
}

/**
 * Creates a [FirstPlayState] that's remembered across compositions.
 */
@ExperimentalPlayerStateApi
@Composable
fun rememberFirstPlayState(player: Player?): FirstPlayState {
    val state = remember(player) { FirstPlayState(player) }
    DisposableEffect(state) {
        state.observe()
        onDispose { state.dispose() }
    }
    return state
}
