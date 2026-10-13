package com.theoplayer.android.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.theoplayer.android.api.event.EventListener
import com.theoplayer.android.api.event.player.PlayerEvent
import com.theoplayer.android.api.event.player.PlayerEventTypes
import com.theoplayer.android.api.player.Player

/**
 * State holder for the video dimensions.
 *
 * Create an instance with [rememberVideoSizeState].
 */
@ExperimentalPlayerStateApi
class VideoSizeState internal constructor(private val player: Player?) {
    private val listeners = PlayerStateListeners()

    /**
     * The width of the video, in pixels.
     */
    var videoWidth by mutableIntStateOf(0)
        private set

    /**
     * The height of the video, in pixels.
     */
    var videoHeight by mutableIntStateOf(0)
        private set

    private fun update() {
        videoWidth = player?.videoWidth ?: 0
        videoHeight = player?.videoHeight ?: 0
    }

    internal fun observe() {
        val player = player ?: return
        update()
        listeners.run {
            val listener = EventListener<PlayerEvent<*>> { update() }
            player.add(PlayerEventTypes.RESIZE, listener)
            player.add(PlayerEventTypes.SOURCECHANGE, listener)
            player.add(PlayerEventTypes.ERROR, listener)
        }
    }

    internal fun dispose() {
        listeners.removeAll()
    }
}

/**
 * Creates a [VideoSizeState] that's remembered across compositions.
 */
@ExperimentalPlayerStateApi
@Composable
fun rememberVideoSizeState(player: Player?): VideoSizeState {
    val state = remember(player) { VideoSizeState(player) }
    DisposableEffect(state) {
        state.observe()
        onDispose { state.dispose() }
    }
    return state
}
