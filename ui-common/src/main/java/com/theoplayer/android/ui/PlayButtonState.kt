package com.theoplayer.android.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.theoplayer.android.api.event.player.PlayerEventTypes
import com.theoplayer.android.api.player.Player

/**
 * State holder for a play/pause button, such as [PlayButton].
 *
 * Create an instance with [rememberPlayButtonState].
 */
@ExperimentalPlayerStateApi
class PlayButtonState internal constructor(private val player: Player?) {
    /** Whether the player is paused. */
    var paused by mutableStateOf(true)
        private set

    /** Whether playback has reached the end of the media. */
    var ended by mutableStateOf(false)
        private set

    private fun updatePlaybackState() {
        paused = player?.isPaused ?: true
        ended = player?.isEnded ?: false
    }

    init {
        updatePlaybackState()
    }

    internal suspend fun observe() {
        val player = player ?: return
        updatePlaybackState()
        player.listen(
            PlayerEventTypes.PLAY,
            PlayerEventTypes.PAUSE,
            PlayerEventTypes.ENDED,
            PlayerEventTypes.SEEKING,
            PlayerEventTypes.SEEKED,
            PlayerEventTypes.READYSTATECHANGE,
            PlayerEventTypes.SOURCECHANGE,
            PlayerEventTypes.ERROR,
        ) {
            updatePlaybackState()
        }
    }

    /** Toggles between play and pause. */
    fun onClick() {
        if (paused) {
            player?.play()
        } else {
            player?.pause()
        }
    }
}

/** Creates a [PlayButtonState] that's remembered across compositions. */
@ExperimentalPlayerStateApi
@Composable
fun rememberPlayButtonState(player: Player?): PlayButtonState {
    val state = remember(player) { PlayButtonState(player) }
    LaunchedEffect(state) { state.observe() }
    return state
}
