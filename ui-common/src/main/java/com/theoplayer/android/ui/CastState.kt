package com.theoplayer.android.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import com.theoplayer.android.api.THEOplayerView
import com.theoplayer.android.api.cast.Cast
import com.theoplayer.android.api.cast.chromecast.PlayerCastState
import com.theoplayer.android.api.event.chromecast.ChromecastEventTypes
import kotlinx.coroutines.flow.collectLatest

/**
 * State holder for Chromecast controls, such as [ChromecastButton] and [ChromecastDisplay].
 *
 * The cast integration is only registered after the view is created, so this keeps track of the
 * current [Cast] integration and moves its listeners when it changes.
 *
 * Create an instance with [rememberCastState].
 */
@ExperimentalPlayerStateApi
class CastState internal constructor(private val theoplayerView: THEOplayerView?) {
    private var cast: Cast? by mutableStateOf(null)

    /** The current cast state of the player. */
    val castState: PlayerCastState
        get() {
            refreshCast()
            return _castState
        }

    private var _castState: PlayerCastState by mutableStateOf(PlayerCastState.UNAVAILABLE)

    /** The name of the connected Chromecast receiver, if any. */
    val castReceiverName: String?
        get() {
            refreshCast()
            return _castReceiverName
        }

    private var _castReceiverName: String? by mutableStateOf(null)

    /** Whether the player is casting. */
    val casting: Boolean
        get() {
            refreshCast()
            return _casting
        }

    private var _casting: Boolean by mutableStateOf(false)

    private fun update(cast: Cast?) {
        _castState = cast?.chromecast?.state ?: PlayerCastState.UNAVAILABLE
        _castReceiverName = cast?.chromecast?.receiverName
        _casting = cast?.chromecast?.isCasting == true
    }

    internal fun refreshCast() {
        cast = theoplayerView?.cast
    }

    internal suspend fun observe() {
        refreshCast()
        snapshotFlow { cast }
            .collectLatest { cast ->
                update(cast)
                cast?.chromecast?.listen(
                    ChromecastEventTypes.STATECHANGE,
                    ChromecastEventTypes.ERROR,
                ) {
                    update(cast)
                }
            }
    }

    /** Starts casting to a Chromecast receiver. */
    fun startCasting() {
        theoplayerView?.cast?.chromecast?.start()
    }

    /** Stops casting. */
    fun stopCasting() {
        theoplayerView?.cast?.chromecast?.stop()
    }
}

/** Creates a [CastState] that's remembered across compositions. */
@ExperimentalPlayerStateApi
@Composable
fun rememberCastState(theoplayerView: THEOplayerView?): CastState {
    val state = remember(theoplayerView) { CastState(theoplayerView) }
    LaunchedEffect(state) { state.observe() }
    state.refreshCast()
    return state
}
