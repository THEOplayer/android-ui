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
 * Create an instance with [rememberPlaybackTimeState].
 */
@ExperimentalPlayerStateApi
class PlaybackTimeState internal constructor(private val player: Player?) {
    /**
     * The current playback position of the media, in seconds.
     */
    var currentTime by mutableStateOf(0.0)
        private set

    /**
     * The duration of the media, in seconds.
     *
     * For live streams, this is the end of the seekable window if it has one.
     */
    var duration by mutableStateOf(Double.NaN)
        private set

    private var seekable by mutableStateOf(TimeRanges.empty())
    private var rawDuration by mutableStateOf(Double.NaN)

    /**
     * The [StreamType] of the media.
     */
    val streamType: StreamType
        get() = streamTypeOf(rawDuration, seekable)

    private fun update() {
        currentTime = player?.currentTime ?: 0.0
        rawDuration = player?.duration ?: Double.NaN
        seekable = player?.seekable?.let { TimeRanges.fromTHEOplayer(it) } ?: TimeRanges.empty()
        duration = seekable.lastEnd ?: rawDuration
    }

    init {
        update()
    }

    internal suspend fun observe() {
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
            PlayerEventTypes.ERROR
        ) { update() }
    }
}

/**
 * Creates a [PlaybackTimeState] that's remembered across compositions.
 */
@ExperimentalPlayerStateApi
@Composable
fun rememberPlaybackTimeState(player: Player?): PlaybackTimeState {
    val state = remember(player) { PlaybackTimeState(player) }
    LaunchedEffect(state) { state.observe() }
    return state
}
