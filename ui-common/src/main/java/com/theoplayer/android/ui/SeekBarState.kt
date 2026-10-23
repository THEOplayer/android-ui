package com.theoplayer.android.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.theoplayer.android.api.event.ads.AdsEventTypes
import com.theoplayer.android.api.player.Player
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * State holder for a seek bar, such as [SeekBar].
 *
 * While the user drags the seek bar (see [onValueChange]), the player is temporarily paused, and
 * resumed on [onValueChangeFinished] if it was playing. To avoid loading media for positions that
 * are scrubbed past, the player only seeks once the user stops moving for a moment, or when they
 * finish.
 *
 * This is also a [PlaybackTimeState], whose [currentTime] is the scrub position while scrubbing.
 *
 * Create an instance with [rememberSeekBarState].
 */
@ExperimentalPlayerStateApi
class SeekBarState internal constructor(player: Player?) : PlaybackTimeState(player) {

    /** The current scrub position of the seek bar, relative to the start of [seekableRange]. */
    val value: Float
        get() = seekTime ?: (playerTime - seekableRange.start).toFloat()

    /** The range of [value]. Always starts at zero. */
    val valueRange: ClosedFloatingPointRange<Float>
        get() = 0f..(seekableRange.endInclusive - seekableRange.start).toFloat()

    /**
     * The position the user is scrubbing to, in seconds, or `null` if the user is not scrubbing.
     *
     * The player may not have seeked to this position yet.
     */
    val scrubTime: Double?
        get() = seekTime?.let { seekableRange.start + it.toDouble() }

    /**
     * The current playback position of the media, in seconds. While the user is scrubbing, this is
     * the [scrubTime].
     */
    override val currentTime: Double
        get() = scrubTime ?: playerTime

    /** Whether the seek bar can be interacted with. */
    val enabled: Boolean
        get() = !playingAd && seekable.isNotEmpty()

    /** Whether to show the seek bar's thumb. Hidden while an ad is playing. */
    val showThumb: Boolean
        get() = !playingAd

    private var playingAd by mutableStateOf(false)
    private var seekTime by mutableStateOf<Float?>(null)
    private var wasPlayingBeforeSeek by mutableStateOf(false)
    private val pendingSeek = MutableStateFlow<Float?>(null)
    private var lastSeekValue: Float? = null

    // Seekable start can be very large (e.g. a Unix timestamp), which can lead to precision loss
    // when converted to a float. Instead, make the slider always start at zero.
    private val seekableRange: ClosedFloatingPointRange<Double>
        get() =
            seekable.bounds
                ?: run { 0.0..(if (duration.isFinite()) duration.coerceAtLeast(0.0) else 0.0) }

    override fun update() {
        super.update()
        playingAd = player?.ads?.isPlaying ?: false
    }

    init {
        update()
    }

    override suspend fun observe() {
        val player = player ?: return
        coroutineScope {
            launch(start = CoroutineStart.UNDISPATCHED) { seekWhenIdle() }
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
            super.observe()
        }
    }

    /** Seeks to the pending scrub position once it hasn't changed for [SeekDebounce]. */
    private suspend fun seekWhenIdle() {
        pendingSeek.collectLatest { value ->
            if (value != null) {
                delay(SeekDebounce)
                seekToValue(value)
            }
        }
    }

    /** Seeks the player to a position within [valueRange]. */
    private fun seekToValue(value: Float) {
        lastSeekValue = value
        player?.currentTime = seekableRange.start + value.toDouble()
        // Re-read the position right away, so the seek bar doesn't jump back to the old position
        // when scrubbing finishes before the player fires its seeking event.
        update()
    }

    /**
     * Called while the user drags the seek bar. Temporarily pauses a playing player, and seeks to
     * the given position within [valueRange] once the user stops dragging for a moment.
     */
    fun onValueChange(value: Float) {
        seekTime = value
        pendingSeek.value = value
        player?.let {
            if (!it.isPaused) {
                wasPlayingBeforeSeek = true
                it.pause()
            }
        }
    }

    /** Seeks to a position within [valueRange] without changing playback state. */
    fun seekTo(value: Float) {
        if (!enabled) return
        player?.currentTime = seekableRange.start + value.toDouble()
    }

    /**
     * Plays or pauses the player. While the user is scrubbing, this instead changes whether
     * playback resumes when scrubbing finishes.
     */
    fun togglePlayPause() {
        val player = player ?: return
        if (seekTime != null) {
            wasPlayingBeforeSeek = !wasPlayingBeforeSeek
        } else if (player.isPaused) {
            player.play()
        } else {
            player.pause()
        }
    }

    /**
     * Called when the user stops dragging the seek bar. Seeks to the final position, and resumes
     * playback if the player was playing before the drag started.
     */
    fun onValueChangeFinished() {
        seekTime?.let { if (it != lastSeekValue) seekToValue(it) }
        seekTime = null
        pendingSeek.value = null
        lastSeekValue = null
        if (wasPlayingBeforeSeek) {
            player?.play()
            wasPlayingBeforeSeek = false
        }
    }
}

/** How long the scrub position must stay unchanged before the player seeks to it. */
private val SeekDebounce = 250.milliseconds

/** Creates a [SeekBarState] that's remembered across compositions. */
@ExperimentalPlayerStateApi
@Composable
fun rememberSeekBarState(player: Player?): SeekBarState {
    val state = remember(player) { SeekBarState(player) }
    LaunchedEffect(state) { state.observe() }
    return state
}

/**
 * Returns the [SeekBarState] of the enclosing UI controller if it belongs to [player], or remembers
 * a new one otherwise.
 */
@InternalUiApi
@ExperimentalPlayerStateApi
@Composable
fun rememberUIControllerSeekBarState(player: Player?): SeekBarState =
    uiControllerSeekBarState(player) ?: rememberSeekBarState(player)

/** Returns the [SeekBarState] of the enclosing UI controller if it belongs to [player]. */
@OptIn(InternalUiApi::class)
@ExperimentalPlayerStateApi
@Composable
internal fun uiControllerSeekBarState(player: Player?): SeekBarState? =
    LocalUIControllerSeekBarState.current?.takeIf { it.player == player }
