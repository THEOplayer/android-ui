package com.theoplayer.android.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.theoplayer.android.api.error.THEOplayerException
import com.theoplayer.android.api.event.EventListener
import com.theoplayer.android.api.event.player.ErrorEvent
import com.theoplayer.android.api.event.player.PlayerEvent
import com.theoplayer.android.api.event.player.PlayerEventTypes
import com.theoplayer.android.api.player.Player

/**
 * State holder for an error display, such as [ErrorDisplay].
 *
 * The error is only captured from the player's `error` event. Create this state *before*
 * an error can happen, for example at the top of your UI instead of inside the error overlay
 * that is only shown once there's an error, because a state created after the error
 * will not see it.
 *
 * Create an instance with [rememberErrorState].
 */
@ExperimentalPlayerStateApi
class ErrorState internal constructor(
    private val player: Player?,
    private val initialError: THEOplayerException? = null
) {
    private val listeners = PlayerStateListeners()

    /**
     * The fatal error the player has encountered, if any.
     */
    var error by mutableStateOf<THEOplayerException?>(null)
        private set

    internal fun observe() {
        val player = player ?: return
        error = initialError
        listeners.run {
            val listener = EventListener<PlayerEvent<*>> { event ->
                error = (event as? ErrorEvent)?.errorObject
            }
            player.add(PlayerEventTypes.ERROR, listener)
            player.add(PlayerEventTypes.SOURCECHANGE, listener)
        }
    }

    internal fun dispose() {
        listeners.removeAll()
    }
}

/**
 * Creates an [ErrorState] that's remembered across compositions.
 *
 * The error is only captured from the player's `error` event, so create this state *before*
 * an error can happen. For example, create it at the top of your UI and pass it down to the
 * error overlay, instead of creating it inside the error overlay that is only shown
 * once there's an error.
 */
@ExperimentalPlayerStateApi
@Composable
fun rememberErrorState(player: Player?): ErrorState =
    rememberErrorState(player, initialError = null)

/**
 * Creates an [ErrorState] that's remembered across compositions, seeded with a known error.
 *
 * @suppress
 */
@InternalUiApi
@ExperimentalPlayerStateApi
@Composable
fun rememberErrorState(player: Player?, initialError: THEOplayerException?): ErrorState {
    val state = remember(player) { ErrorState(player, initialError) }
    DisposableEffect(state) {
        state.observe()
        onDispose { state.dispose() }
    }
    return state
}
