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
 * State holder for a button that seeks forward or backward by a fixed offset, such as [SeekButton].
 *
 * Create an instance with [rememberSeekButtonState].
 */
@ExperimentalPlayerStateApi
class SeekButtonState
internal constructor(
    private val player: Player?,
    private val seekOffset: Int,
) {

    /** Whether the button can be interacted with. */
    val enabled: Boolean
        get() = !playingAd && seekable.isNotEmpty()

    private var seekable by mutableStateOf(TimeRanges.empty())
    private var playingAd by mutableStateOf(false)

    private fun update() {
        seekable = player?.seekable?.let { TimeRanges.fromTHEOplayer(it) } ?: TimeRanges.empty()
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
            player.listen(
                PlayerEventTypes.TIMEUPDATE,
                PlayerEventTypes.PLAY,
                PlayerEventTypes.PAUSE,
                PlayerEventTypes.ENDED,
                PlayerEventTypes.SEEKING,
                PlayerEventTypes.SEEKED,
                PlayerEventTypes.READYSTATECHANGE,
                PlayerEventTypes.SOURCECHANGE,
                PlayerEventTypes.ERROR,
            ) {
                update()
            }
        }
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
 * @param seekOffset the offset (in seconds) by which to seek forward (if positive) or backward (if
 *   negative)
 */
@ExperimentalPlayerStateApi
@Composable
fun rememberSeekButtonState(player: Player?, seekOffset: Int): SeekButtonState {
    val state = remember(player, seekOffset) { SeekButtonState(player, seekOffset) }
    LaunchedEffect(state) { state.observe() }
    return state
}
