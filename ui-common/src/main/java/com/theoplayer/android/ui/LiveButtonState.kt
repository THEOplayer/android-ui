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

/**
 * State holder for a "live" button, such as [LiveButton].
 *
 * Create an instance with [rememberLiveButtonState].
 */
@ExperimentalPlayerStateApi
class LiveButtonState(
    private val player: Player?,
    private val liveThreshold: Double
) {
    private val listeners = PlayerStateListeners()

    /**
     * Whether the button should be shown. Only live and DVR streams have a live point.
     */
    val visible: Boolean
        get() = streamType == StreamType.Live || streamType == StreamType.Dvr

    /**
     * Whether the player is currently playing at the live point.
     */
    val isLive: Boolean
        get() = !paused && ((seekable.lastEnd ?: 0.0) - currentTime) <= liveThreshold

    private var paused by mutableStateOf(true)
    private var currentTime by mutableStateOf(0.0)
    private var seekable by mutableStateOf(TimeRanges.empty())
    private var duration by mutableStateOf(Double.NaN)

    private val streamType: StreamType
        get() = streamTypeOf(duration, seekable)

    private fun updatePlaybackState() {
        paused = player?.isPaused ?: true
    }

    private fun update() {
        currentTime = player?.currentTime ?: 0.0
        duration = player?.duration ?: Double.NaN
        seekable = player?.seekable?.let { TimeRanges.fromTHEOplayer(it) } ?: TimeRanges.empty()
        updatePlaybackState()
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
        }
    }

    internal fun dispose() {
        listeners.removeAll()
    }

    /**
     * Seeks to the live point and resumes playback.
     */
    fun onClick() {
        player?.let {
            it.currentTime = Double.POSITIVE_INFINITY
            it.play()
        }
    }
}

/**
 * Creates a [LiveButtonState] that's remembered across compositions.
 *
 * @param liveThreshold the maximum distance (in seconds) from the live point that the player's
 * current time can be for it to still be considered "at the live point"
 */
@ExperimentalPlayerStateApi
@Composable
fun rememberLiveButtonState(player: Player?, liveThreshold: Double): LiveButtonState {
    val state = remember(player, liveThreshold) { LiveButtonState(player, liveThreshold) }
    DisposableEffect(state) {
        state.observe()
        onDispose { state.dispose() }
    }
    return state
}
