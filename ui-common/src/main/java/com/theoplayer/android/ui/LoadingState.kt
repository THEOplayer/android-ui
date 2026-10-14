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
import com.theoplayer.android.api.player.ReadyState

/**
 * State holder for a loading indicator, such as [LoadingSpinner].
 *
 * Create an instance with [rememberLoadingState].
 */
@ExperimentalPlayerStateApi
class LoadingState internal constructor(private val player: Player?) {
    private val listeners = PlayerStateListeners()

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

    internal fun observe() {
        val player = player ?: return
        update()
        listeners.run {
            val listener = EventListener<PlayerEvent<*>> { update() }
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
 * Creates a [LoadingState] that's remembered across compositions.
 */
@ExperimentalPlayerStateApi
@Composable
fun rememberLoadingState(player: Player?): LoadingState {
    val state = remember(player) { LoadingState(player) }
    DisposableEffect(state) {
        state.observe()
        onDispose { state.dispose() }
    }
    return state
}
