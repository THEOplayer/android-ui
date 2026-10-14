package com.theoplayer.android.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.theoplayer.android.api.THEOplayerView

/**
 * State holder for a fullscreen button, such as [FullscreenButton].
 *
 * All fullscreen states (and the [Player]) for the same view share one fullscreen handler.
 *
 * Create an instance with [rememberFullscreenState].
 */
@ExperimentalPlayerStateApi
class FullscreenState internal constructor(private val theoplayerView: THEOplayerView?) {
    private val handler: FullscreenHandler? = theoplayerView?.fullscreenHandler

    /**
     * Whether the player is shown in fullscreen mode.
     */
    var fullscreen by mutableStateOf(false)
        private set

    private fun update() {
        fullscreen = handler?.fullscreen ?: false
    }

    internal suspend fun observe() {
        val handler = handler ?: return
        update()
        handler.listen { update() }
    }

    /**
     * Shows the player in fullscreen mode.
     */
    fun enterFullscreen() {
        fullscreen = true
        handler?.requestFullscreen()
    }

    /**
     * Shows the player inline again.
     */
    fun exitFullscreen() {
        fullscreen = false
        handler?.exitFullscreen()
    }
}

/**
 * Creates a [FullscreenState] that's remembered across compositions.
 */
@ExperimentalPlayerStateApi
@Composable
fun rememberFullscreenState(theoplayerView: THEOplayerView?): FullscreenState {
    val state = remember(theoplayerView) { FullscreenState(theoplayerView) }
    LaunchedEffect(state) { state.observe() }
    return state
}
