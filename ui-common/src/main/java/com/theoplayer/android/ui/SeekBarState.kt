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
 * State holder for a seek bar, such as [SeekBar].
 *
 * While the user drags the seek bar (see [onValueChange]), the player is temporarily paused,
 * and resumed on [onValueChangeFinished] if it was playing.
 *
 * Create an instance with [rememberSeekBarState].
 */
@ExperimentalPlayerStateApi
class SeekBarState internal constructor(private val player: Player?) {

    /**
     * The current scrub position of the seek bar, relative to the start of [seekableRange].
     */
    val value: Float
        get() = seekTime ?: (currentTime - seekableRange.start).toFloat()

    /**
     * The range of [value]. Always starts at zero.
     */
    val valueRange: ClosedFloatingPointRange<Float>
        get() = 0f..(seekableRange.endInclusive - seekableRange.start).toFloat()

    /**
     * Whether the seek bar can be interacted with.
     */
    val enabled: Boolean
        get() = !playingAd && seekable.isNotEmpty()

    /**
     * Whether to show the seek bar's thumb. Hidden while an ad is playing.
     */
    val showThumb: Boolean
        get() = !playingAd

    private var currentTime by mutableStateOf(0.0)
    private var seekable by mutableStateOf(TimeRanges.empty())
    private var duration by mutableStateOf(Double.NaN)
    private var playingAd by mutableStateOf(false)
    private var seekTime by mutableStateOf<Float?>(null)
    private var wasPlayingBeforeSeek by mutableStateOf(false)

    // Seekable start can be very large (e.g. a Unix timestamp), which can lead to precision loss
    // when converted to a float. Instead, make the slider always start at zero.
    private val seekableRange: ClosedFloatingPointRange<Double>
        get() = seekable.bounds ?: run {
            0.0..(if (duration.isFinite()) duration.coerceAtLeast(0.0) else 0.0)
        }

    private fun update() {
        currentTime = player?.currentTime ?: 0.0
        seekable = player?.seekable?.let { TimeRanges.fromTHEOplayer(it) } ?: TimeRanges.empty()
        duration = player?.duration ?: Double.NaN
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
                        AdsEventTypes.AD_BREAK_END
                    ) { update() }
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
                PlayerEventTypes.DURATIONCHANGE,
                PlayerEventTypes.SOURCECHANGE,
                PlayerEventTypes.ERROR
            ) { update() }
        }
    }

    /**
     * Called while the user drags the seek bar. Temporarily pauses a playing player and seeks
     * to the given position within [valueRange].
     */
    fun onValueChange(value: Float) {
        seekTime = value
        player?.let {
            if (!it.isPaused) {
                wasPlayingBeforeSeek = true
                it.pause()
            }
            it.currentTime = seekableRange.start + value.toDouble()
        }
    }

    /**
     * Called when the user stops dragging the seek bar. Resumes playback if the player was
     * playing before the drag started.
     */
    fun onValueChangeFinished() {
        seekTime = null
        if (wasPlayingBeforeSeek) {
            player?.play()
            wasPlayingBeforeSeek = false
        }
    }
}

/**
 * Creates a [SeekBarState] that's remembered across compositions.
 */
@ExperimentalPlayerStateApi
@Composable
fun rememberSeekBarState(player: Player?): SeekBarState {
    val state = remember(player) { SeekBarState(player) }
    LaunchedEffect(state) { state.observe() }
    return state
}
