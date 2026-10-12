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
import com.theoplayer.android.api.player.Player

/**
 * State holder for a play/pause button, such as [PlayButton].
 *
 * Create an instance with [rememberPlayButtonState].
 */
@ExperimentalPlayerStateApi
class PlayButtonState(private val player: Player?) {
    private val listeners = PlayerStateListeners()

    /**
     * Whether the player is paused.
     */
    var paused by mutableStateOf(true)
        private set

    /**
     * Whether playback has reached the end of the media.
     */
    var ended by mutableStateOf(false)
        private set

    private fun updatePlaybackState() {
        paused = player?.isPaused ?: true
        ended = player?.isEnded ?: false
    }

    internal fun observe() {
        val player = player ?: return
        updatePlaybackState()
        listeners.run {
            player.add(PlayerEventTypes.PLAY, EventListener<PlayerEvent<*>> { updatePlaybackState() })
            player.add(PlayerEventTypes.PAUSE, EventListener<PlayerEvent<*>> { updatePlaybackState() })
            player.add(PlayerEventTypes.ENDED, EventListener<PlayerEvent<*>> { updatePlaybackState() })
            player.add(PlayerEventTypes.SEEKING, EventListener<PlayerEvent<*>> { updatePlaybackState() })
            player.add(PlayerEventTypes.SEEKED, EventListener<PlayerEvent<*>> { updatePlaybackState() })
            player.add(PlayerEventTypes.READYSTATECHANGE, EventListener<PlayerEvent<*>> { updatePlaybackState() })
            player.add(PlayerEventTypes.SOURCECHANGE, EventListener<PlayerEvent<*>> { updatePlaybackState() })
            player.add(PlayerEventTypes.ERROR, EventListener<PlayerEvent<*>> { updatePlaybackState() })
        }
    }

    internal fun dispose() {
        listeners.removeAll()
    }

    /**
     * Toggles between play and pause.
     */
    fun onClick() {
        if (paused) {
            player?.play()
        } else {
            player?.pause()
        }
    }
}

/**
 * Creates a [PlayButtonState] that's remembered across compositions.
 */
@ExperimentalPlayerStateApi
@Composable
fun rememberPlayButtonState(player: Player?): PlayButtonState {
    val state = remember(player) { PlayButtonState(player) }
    DisposableEffect(state) {
        state.observe()
        onDispose { state.dispose() }
    }
    return state
}
