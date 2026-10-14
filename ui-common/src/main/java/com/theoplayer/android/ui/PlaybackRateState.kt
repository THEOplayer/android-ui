package com.theoplayer.android.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.theoplayer.android.api.event.player.PlayerEventTypes
import com.theoplayer.android.api.player.Player

/**
 * State holder for the playback rate, such as [PlaybackRateList] or [PlaybackRateMenu].
 *
 * Create an instance with [rememberPlaybackRateState].
 */
@ExperimentalPlayerStateApi
class PlaybackRateState internal constructor(private val player: Player?) {
    /**
     * The playback rate of the player. `1.0` is normal speed.
     */
    var playbackRate by mutableDoubleStateOf(1.0)
        private set

    private fun update() {
        playbackRate = player?.playbackRate ?: 1.0
    }

    internal suspend fun observe() {
        val player = player ?: return
        update()
        player.listen(PlayerEventTypes.RATECHANGE) { update() }
    }

    /**
     * Selects the desired playback rate.
     */
    fun select(rate: Double) {
        playbackRate = rate
        player?.playbackRate = rate
    }
}

/**
 * Creates a [PlaybackRateState] that's remembered across compositions.
 */
@ExperimentalPlayerStateApi
@Composable
fun rememberPlaybackRateState(player: Player?): PlaybackRateState {
    val state = remember(player) { PlaybackRateState(player) }
    LaunchedEffect(state) { state.observe() }
    return state
}
