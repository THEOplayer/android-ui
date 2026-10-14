package com.theoplayer.android.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.theoplayer.android.api.event.EventListener
import com.theoplayer.android.api.event.ads.AdEvent
import com.theoplayer.android.api.event.ads.AdsEventTypes
import com.theoplayer.android.api.event.player.PlayerEvent
import com.theoplayer.android.api.event.player.PlayerEventTypes
import com.theoplayer.android.api.player.Player

/**
 * State holder for UI that depends on whether an ad is playing.
 *
 * Create an instance with [rememberAdState].
 */
@ExperimentalPlayerStateApi
class AdState internal constructor(private val player: Player?) {
    private val listeners = PlayerStateListeners()

    /**
     * Whether an ad is currently playing.
     */
    var playingAd by mutableStateOf(false)
        private set

    private fun update() {
        playingAd = player?.ads?.isPlaying ?: false
    }

    internal fun observe() {
        val player = player ?: return
        update()
        listeners.run {
            val listener = EventListener<PlayerEvent<*>> { update() }
            player.add(PlayerEventTypes.SOURCECHANGE, listener)
            player.add(PlayerEventTypes.ERROR, listener)
            player.ads?.let { ads ->
                val adListener = EventListener<AdEvent<*>> { update() }
                ads.add(AdsEventTypes.AD_BREAK_BEGIN, adListener)
                ads.add(AdsEventTypes.AD_SKIP, adListener)
                ads.add(AdsEventTypes.AD_BREAK_END, adListener)
            }
        }
    }

    internal fun dispose() {
        listeners.removeAll()
    }
}

/**
 * Creates an [AdState] that's remembered across compositions.
 */
@ExperimentalPlayerStateApi
@Composable
fun rememberAdState(player: Player?): AdState {
    val state = remember(player) { AdState(player) }
    DisposableEffect(state) {
        state.observe()
        onDispose { state.dispose() }
    }
    return state
}
