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
 * State holder for a mute button, such as [MuteButton].
 *
 * Create an instance with [rememberMuteButtonState].
 */
@ExperimentalPlayerStateApi
class MuteButtonState internal constructor(private val player: Player?) {
    /** Whether the audio is muted. */
    var muted by mutableStateOf(false)
        private set

    private fun update() {
        muted = player?.isMuted ?: false
    }

    init {
        update()
    }

    internal suspend fun observe() {
        val player = player ?: return
        update()
        player.listen(PlayerEventTypes.VOLUMECHANGE) { update() }
    }

    /** Toggles whether the audio is muted. */
    fun onClick() {
        player?.let {
            val newMuted = !it.isMuted
            muted = newMuted
            it.isMuted = newMuted
        }
    }
}

/** Creates a [MuteButtonState] that's remembered across compositions. */
@ExperimentalPlayerStateApi
@Composable
fun rememberMuteButtonState(player: Player?): MuteButtonState {
    val state = remember(player) { MuteButtonState(player) }
    LaunchedEffect(state) { state.observe() }
    return state
}
