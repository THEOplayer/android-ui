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
 * State holder for a seek bar, such as [SeekBar].
 *
 * While the user drags the seek bar (see [onValueChange]), the player is temporarily paused,
 * and resumed on [onValueChangeFinished] if it was playing.
 *
 * Create an instance with [rememberSeekBarState].
 */
@ExperimentalPlayerStateApi
class SeekBarState(private val player: Player?) {
    private val listeners = PlayerStateListeners()

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
            player.add(PlayerEventTypes.DURATIONCHANGE, listener)
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
    DisposableEffect(state) {
        state.observe()
        onDispose { state.dispose() }
    }
    return state
}
