package com.theoplayer.android.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.theoplayer.android.api.event.player.PlayerEventTypes
import com.theoplayer.android.api.player.Player
import com.theoplayer.android.api.player.ReadyState

/**
 * State holder for a loading indicator, such as [LoadingSpinner].
 *
 * Create an instance with [rememberLoadingState].
 */
@ExperimentalPlayerStateApi
class LoadingState internal constructor(private val player: Player?) {
    /**
     * Whether the player is currently waiting for more data to resume playback.
     */
    val loading: Boolean
        get() = !paused && !ended &&
            (seeking || readyState.ordinal < ReadyState.HAVE_FUTURE_DATA.ordinal)

    private var paused by mutableStateOf(true)
    private var ended by mutableStateOf(false)
    private var seeking by mutableStateOf(false)
    private var readyState by mutableStateOf(ReadyState.HAVE_NOTHING)

    private fun update() {
        paused = player?.isPaused ?: true
        ended = player?.isEnded ?: false
        seeking = player?.isSeeking ?: false
        readyState = player?.readyState ?: ReadyState.HAVE_NOTHING
    }

    init {
        update()
    }

    internal suspend fun observe() {
        val player = player ?: return
        update()
        player.listen(
            PlayerEventTypes.PLAY,
            PlayerEventTypes.PAUSE,
            PlayerEventTypes.ENDED,
            PlayerEventTypes.SEEKING,
            PlayerEventTypes.SEEKED,
            PlayerEventTypes.READYSTATECHANGE,
            PlayerEventTypes.SOURCECHANGE,
            PlayerEventTypes.ERROR
        ) { update() }
    }
}

/**
 * Creates a [LoadingState] that's remembered across compositions.
 */
@ExperimentalPlayerStateApi
@Composable
fun rememberLoadingState(player: Player?): LoadingState {
    val state = remember(player) { LoadingState(player) }
    LaunchedEffect(state) { state.observe() }
    return state
}
