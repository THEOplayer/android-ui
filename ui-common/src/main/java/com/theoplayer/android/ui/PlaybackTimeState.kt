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
 * State holder for a playback time display, such as [CurrentTimeDisplay] or [DurationDisplay].
 *
 * A [SeekBarState] is also a [PlaybackTimeState], whose [currentTime] follows the seek bar while
 * the user is scrubbing. Pass it to a time display to show the position the user is scrubbing to.
 *
 * Create an instance with [rememberPlaybackTimeState].
 */
@ExperimentalPlayerStateApi
open class PlaybackTimeState internal constructor(internal val player: Player?) {
    /** The current playback position of the media, in seconds. */
    open val currentTime: Double
        get() = playerTime

    /** The current playback position reported by the player, in seconds. */
    internal var playerTime by mutableStateOf(0.0)
        private set

    /**
     * The duration of the media, in seconds.
     *
     * For live streams, this is the end of the seekable window if it has one.
     */
    var duration by mutableStateOf(Double.NaN)
        private set

    internal var seekable by mutableStateOf(TimeRanges.empty())
        private set

    private var rawDuration by mutableStateOf(Double.NaN)

    /** The [StreamType] of the media. */
    val streamType: StreamType
        get() = streamTypeOf(rawDuration, seekable)

    /** Re-reads the state from the player. */
    internal open fun update() {
        updateTime()
    }

    private fun updateTime() {
        playerTime = player?.currentTime ?: 0.0
        rawDuration = player?.duration ?: Double.NaN
        seekable = player?.seekable?.let { TimeRanges.fromTHEOplayer(it) } ?: TimeRanges.empty()
        duration = seekable.lastEnd ?: rawDuration
    }

    init {
        updateTime()
    }

    internal open suspend fun observe() {
        val player = player ?: return
        update()
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
            PlayerEventTypes.ERROR,
        ) {
            update()
        }
    }
}

/**
 * Creates a [PlaybackTimeState] that's remembered across compositions.
 *
 * Inside a `UIController` for the same [player], this returns the controller's [SeekBarState], so
 * time displays follow its seek bar while the user is scrubbing.
 */
@ExperimentalPlayerStateApi
@Composable
fun rememberPlaybackTimeState(player: Player?): PlaybackTimeState {
    uiControllerSeekBarState(player)?.let {
        return it
    }
    val state = remember(player) { PlaybackTimeState(player) }
    LaunchedEffect(state) { state.observe() }
    return state
}
