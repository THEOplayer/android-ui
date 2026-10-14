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
 * State holder for a button that seeks forward or backward by a fixed offset,
 * such as [SeekButton].
 *
 * Create an instance with [rememberSeekButtonState].
 */
@ExperimentalPlayerStateApi
class SeekButtonState internal constructor(
    private val player: Player?,
    private val seekOffset: Int
) {
    private val listeners = PlayerStateListeners()

    /**
     * Whether the button can be interacted with.
     */
    val enabled: Boolean
        get() = !playingAd && seekable.isNotEmpty()

    private var seekable by mutableStateOf(TimeRanges.empty())
    private var playingAd by mutableStateOf(false)

    private fun update() {
        seekable = player?.seekable?.let { TimeRanges.fromTHEOplayer(it) } ?: TimeRanges.empty()
        playingAd = player?.ads?.isPlaying ?: false
    }

    internal fun observe() {
        val player = player ?: return
        update()
        listeners.run {
            val listener = EventListener<PlayerEvent<*>> { update() }
            player.add(PlayerEventTypes.TIMEUPDATE, listener)
            player.add(PlayerEventTypes.PLAY, listener)
            player.add(PlayerEventTypes.PAUSE, listener)
            player.add(PlayerEventTypes.ENDED, listener)
            player.add(PlayerEventTypes.SEEKING, listener)
            player.add(PlayerEventTypes.SEEKED, listener)
            player.add(PlayerEventTypes.READYSTATECHANGE, listener)
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

    /**
     * Seeks forward (if [seekOffset] is positive) or backward (if negative) by [seekOffset]
     * seconds, clamped to the media duration.
     */
    fun onClick() {
        player?.let {
            if (!it.duration.isNaN()) {
                it.currentTime = (it.currentTime + seekOffset).coerceIn(0.0, it.duration)
            }
        }
    }
}

/**
 * Creates a [SeekButtonState] that's remembered across compositions.
 *
 * @param seekOffset the offset (in seconds) by which to seek forward (if positive)
 * or backward (if negative)
 */
@ExperimentalPlayerStateApi
@Composable
fun rememberSeekButtonState(player: Player?, seekOffset: Int): SeekButtonState {
    val state = remember(player, seekOffset) { SeekButtonState(player, seekOffset) }
    DisposableEffect(state) {
        state.observe()
        onDispose { state.dispose() }
    }
    return state
}
