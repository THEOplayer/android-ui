package com.theoplayer.android.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.theoplayer.android.api.event.EventListener
import com.theoplayer.android.api.event.player.PlayerEvent
import com.theoplayer.android.api.event.player.PlayerEventTypes
import com.theoplayer.android.api.player.Player

private const val DVR_WINDOW_THRESHOLD = 60.0

/**
 * State holder for a playback time display, such as [CurrentTimeDisplay] or [DurationDisplay].
 *
 * Create an instance with [rememberPlaybackTimeState].
 */
@ExperimentalPlayerStateApi
class PlaybackTimeState(private val player: Player?) {
    private val listeners = PlayerStateListeners()

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
        get() = if (rawDuration.isInfinite()) {
            val slidingWindow = seekable.bounds?.let { it.endInclusive - it.start } ?: 0.0
            // TODO Make DVR threshold configurable?
            if (slidingWindow >= DVR_WINDOW_THRESHOLD) {
                StreamType.Dvr
            } else {
                StreamType.Live
            }
        } else {
            StreamType.Vod
        }

    private fun update() {
        currentTime = player?.currentTime ?: 0.0
        rawDuration = player?.duration ?: Double.NaN
        seekable = player?.seekable?.let { TimeRanges.fromTHEOplayer(it) } ?: TimeRanges.empty()
        duration = seekable.lastEnd ?: rawDuration
    }

    internal fun observe() {
        val player = player ?: return
        update()
        listeners.run {
            player.add(PlayerEventTypes.TIMEUPDATE, EventListener<PlayerEvent<*>> { update() })
            player.add(PlayerEventTypes.DURATIONCHANGE, EventListener<PlayerEvent<*>> { update() })
            player.add(PlayerEventTypes.SEEKING, EventListener<PlayerEvent<*>> { update() })
            player.add(PlayerEventTypes.SEEKED, EventListener<PlayerEvent<*>> { update() })
            player.add(PlayerEventTypes.SOURCECHANGE, EventListener<PlayerEvent<*>> { update() })
        }
    }

    internal fun dispose() {
        listeners.removeAll()
    }
}

/**
 * Creates a [PlaybackTimeState] that's remembered across compositions.
 */
@ExperimentalPlayerStateApi
@Composable
fun rememberPlaybackTimeState(player: Player?): PlaybackTimeState {
    val state = remember(player) { PlaybackTimeState(player) }
    DisposableEffect(state) {
        state.observe()
        onDispose { state.dispose() }
    }
    return state
}
