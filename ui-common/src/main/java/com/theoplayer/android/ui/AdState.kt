package com.theoplayer.android.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.theoplayer.android.api.event.ads.AdsEventTypes
import com.theoplayer.android.api.event.player.PlayerEventTypes
import com.theoplayer.android.api.player.Player
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * State holder for UI that depends on whether an ad is playing.
 *
 * Create an instance with [rememberAdState].
 */
@ExperimentalPlayerStateApi
class AdState internal constructor(private val player: Player?) {
    /** Whether an ad is currently playing. */
    var playingAd by mutableStateOf(false)
        private set

    private fun update() {
        playingAd = player?.ads?.isPlaying ?: false
    }

    init {
        update()
    }

    internal suspend fun observe() {
        val player = player ?: return
        update()
        coroutineScope {
            player.ads?.let { ads ->
                launch(start = CoroutineStart.UNDISPATCHED) {
                    ads.listen(
                        AdsEventTypes.AD_BREAK_BEGIN,
                        AdsEventTypes.AD_SKIP,
                        AdsEventTypes.AD_BREAK_END,
                    ) {
                        update()
                    }
                }
            }
            player.listen(PlayerEventTypes.SOURCECHANGE, PlayerEventTypes.ERROR) { update() }
        }
    }
}

/** Creates an [AdState] that's remembered across compositions. */
@ExperimentalPlayerStateApi
@Composable
fun rememberAdState(player: Player?): AdState {
    val state = remember(player) { AdState(player) }
    LaunchedEffect(state) { state.observe() }
    return state
}
