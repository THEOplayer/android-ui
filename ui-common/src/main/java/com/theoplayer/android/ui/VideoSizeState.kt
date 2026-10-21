package com.theoplayer.android.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.theoplayer.android.api.event.player.PlayerEventTypes
import com.theoplayer.android.api.player.Player

/**
 * State holder for the video dimensions.
 *
 * Create an instance with [rememberVideoSizeState].
 */
@ExperimentalPlayerStateApi
class VideoSizeState internal constructor(private val player: Player?) {

    /** The width of the video, in pixels. */
    var videoWidth by mutableIntStateOf(0)
        private set

    /** The height of the video, in pixels. */
    var videoHeight by mutableIntStateOf(0)
        private set

    private fun update() {
        videoWidth = player?.videoWidth ?: 0
        videoHeight = player?.videoHeight ?: 0
    }

    init {
        update()
    }

    internal suspend fun observe() {
        val player = player ?: return
        update()
        player.listen(
            PlayerEventTypes.RESIZE,
            PlayerEventTypes.SOURCECHANGE,
            PlayerEventTypes.ERROR,
        ) {
            update()
        }
    }
}

/** Creates a [VideoSizeState] that's remembered across compositions. */
@ExperimentalPlayerStateApi
@Composable
fun rememberVideoSizeState(player: Player?): VideoSizeState {
    val state = remember(player) { VideoSizeState(player) }
    LaunchedEffect(state) { state.observe() }
    return state
}
