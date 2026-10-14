package com.theoplayer.android.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.theoplayer.android.api.event.Event
import com.theoplayer.android.api.event.EventListener
import com.theoplayer.android.api.event.track.texttrack.list.TextTrackListEventTypes
import com.theoplayer.android.api.player.Player
import com.theoplayer.android.api.player.track.texttrack.TextTrack
import com.theoplayer.android.api.player.track.texttrack.TextTrackMode

/**
 * State holder for subtitle tracks, such as [SubtitleTrackList] or [SubtitleMenu].
 *
 * Create an instance with [rememberSubtitleTrackState].
 */
@ExperimentalPlayerStateApi
class SubtitleTrackState internal constructor(private val player: Player?) {
    private val listeners = PlayerStateListeners()

    /**
     * The available subtitle tracks.
     */
    var subtitleTracks by mutableStateOf<List<TextTrack>>(listOf())
        private set

    /**
     * The currently shown subtitle track, if any.
     */
    var activeSubtitleTrack by mutableStateOf<TextTrack?>(null)
        private set

    private fun update() {
        val newSubtitleTracks = player?.textTracks?.filter { isSubtitleTrack(it) } ?: listOf()
        if (subtitleTracks != newSubtitleTracks) {
            subtitleTracks = newSubtitleTracks
        }
        activeSubtitleTrack = subtitleTracks.find { it.mode == TextTrackMode.SHOWING }
    }

    internal fun observe() {
        val player = player ?: return
        update()
        listeners.run {
            val listener = EventListener<Event<*>> { update() }
            player.textTracks.add(TextTrackListEventTypes.ADDTRACK, listener)
            player.textTracks.add(TextTrackListEventTypes.REMOVETRACK, listener)
            player.textTracks.add(TextTrackListEventTypes.TRACKLISTCHANGE, listener)
        }
    }

    internal fun dispose() {
        listeners.removeAll()
    }

    /**
     * Selects the subtitle track to show, or `null` to hide all subtitles.
     * All other subtitle tracks are disabled; non-subtitle text tracks are not touched.
     */
    fun select(track: TextTrack?) {
        activeSubtitleTrack = track
        player?.textTracks?.forEach {
            if (isSubtitleTrack(it)) {
                it.mode = if (it == track) TextTrackMode.SHOWING else TextTrackMode.DISABLED
            }
        }
    }
}

/**
 * Creates a [SubtitleTrackState] that's remembered across compositions.
 */
@ExperimentalPlayerStateApi
@Composable
fun rememberSubtitleTrackState(player: Player?): SubtitleTrackState {
    val state = remember(player) { SubtitleTrackState(player) }
    DisposableEffect(state) {
        state.observe()
        onDispose { state.dispose() }
    }
    return state
}
