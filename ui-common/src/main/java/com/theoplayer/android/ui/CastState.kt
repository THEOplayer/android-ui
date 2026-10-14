package com.theoplayer.android.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.theoplayer.android.api.THEOplayerView
import com.theoplayer.android.api.cast.Cast
import com.theoplayer.android.api.cast.chromecast.PlayerCastState
import com.theoplayer.android.api.event.EventListener
import com.theoplayer.android.api.event.chromecast.ChromecastEvent
import com.theoplayer.android.api.event.chromecast.ChromecastEventTypes

/**
 * State holder for Chromecast controls, such as [ChromecastButton] and [ChromecastDisplay].
 *
 * The cast integration is only registered after the view is created, so this keeps track
 * of the current [Cast] integration and moves its listeners when it changes.
 *
 * Create an instance with [rememberCastState].
 */
@ExperimentalPlayerStateApi
class CastState internal constructor(private val theoplayerView: THEOplayerView?) {
    private val listeners = PlayerStateListeners()
    private var cast: Cast? = null
    private var observing = false

    /**
     * The current cast state of the player.
     */
    val castState: PlayerCastState
        get() {
            if (observing) updateCast(theoplayerView?.cast)
            return _castState
        }
    private var _castState: PlayerCastState by mutableStateOf(PlayerCastState.UNAVAILABLE)

    /**
     * The name of the connected Chromecast receiver, if any.
     */
    val castReceiverName: String?
        get() {
            if (observing) updateCast(theoplayerView?.cast)
            return _castReceiverName
        }
    private var _castReceiverName: String? by mutableStateOf(null)

    /**
     * Whether the player is casting.
     */
    val casting: Boolean
        get() {
            if (observing) updateCast(theoplayerView?.cast)
            return _casting
        }
    private var _casting: Boolean by mutableStateOf(false)

    private fun update() {
        _castState = cast?.chromecast?.state ?: PlayerCastState.UNAVAILABLE
        _castReceiverName = cast?.chromecast?.receiverName
        _casting = cast?.chromecast?.isCasting == true
    }

    private fun updateCast(newCast: Cast?) {
        if (newCast === cast) return
        listeners.removeAll()
        cast = newCast
        newCast?.chromecast?.let { chromecast ->
            listeners.run {
                val listener = EventListener<ChromecastEvent<*>> { update() }
                chromecast.add(ChromecastEventTypes.STATECHANGE, listener)
                chromecast.add(ChromecastEventTypes.ERROR, listener)
            }
        }
        update()
    }

    internal fun observe() {
        observing = true
        updateCast(theoplayerView?.cast)
    }

    internal fun dispose() {
        observing = false
        listeners.removeAll()
        cast = null
    }

    /**
     * Starts casting to a Chromecast receiver.
     */
    fun startCasting() {
        theoplayerView?.cast?.chromecast?.start()
    }

    /**
     * Stops casting.
     */
    fun stopCasting() {
        theoplayerView?.cast?.chromecast?.stop()
    }
}

/**
 * Creates a [CastState] that's remembered across compositions.
 */
@ExperimentalPlayerStateApi
@Composable
fun rememberCastState(theoplayerView: THEOplayerView?): CastState {
    val state = remember(theoplayerView) { CastState(theoplayerView) }
    DisposableEffect(state) {
        state.observe()
        onDispose { state.dispose() }
    }
    return state
}
