package com.theoplayer.android.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

    private fun updateFirstPlay() {
        firstPlay = player?.let { !it.isPaused || (it.played?.length() ?: 0) > 0 } == true
    }

    init {
        updateFirstPlay()
    }

    internal suspend fun observe() {
        val player = player ?: return
        updateFirstPlay()
        player.listen(
            PlayerEventTypes.PLAY,
            PlayerEventTypes.PAUSE,
            PlayerEventTypes.ENDED,
            PlayerEventTypes.SEEKING,
            PlayerEventTypes.SEEKED,
            PlayerEventTypes.READYSTATECHANGE,
            PlayerEventTypes.SOURCECHANGE,
            PlayerEventTypes.ERROR
        ) { event ->
            if (event is SourceChangeEvent) {
                firstPlay = false
            }
            updatePlaying()
        }
    }
}

/**
 * Creates a [FirstPlayState] that's remembered across compositions.
 */
@ExperimentalPlayerStateApi
@Composable
fun rememberFirstPlayState(player: Player?): FirstPlayState {
    val state = remember(player) { FirstPlayState(player) }
    LaunchedEffect(state) { state.observe() }
    return state
}
