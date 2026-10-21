package com.theoplayer.android.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.theoplayer.android.api.event.track.mediatrack.audio.list.AudioTrackListEventTypes
import com.theoplayer.android.api.player.Player
import com.theoplayer.android.api.player.track.mediatrack.MediaTrack
import com.theoplayer.android.api.player.track.mediatrack.quality.AudioQuality

/**
 * State holder for audio tracks, such as [AudioTrackList] or [AudioTrackMenu].
 *
 * Create an instance with [rememberAudioTrackState].
 */
@ExperimentalPlayerStateApi
class AudioTrackState internal constructor(private val player: Player?) {
    /** The available audio tracks. */
    var audioTracks by mutableStateOf<List<MediaTrack<AudioQuality>>>(listOf())
        private set

    /** The currently enabled audio track, if any. */
    var activeAudioTrack by mutableStateOf<MediaTrack<AudioQuality>?>(null)
        private set

    private fun update() {
        val newAudioTracks = player?.audioTracks?.toList() ?: listOf()
        if (audioTracks != newAudioTracks) {
            audioTracks = newAudioTracks
        }
        activeAudioTrack = audioTracks.find { it.isEnabled }
    }

    init {
        update()
    }

    internal suspend fun observe() {
        val player = player ?: return
        update()
        player.audioTracks.listen(
            AudioTrackListEventTypes.ADDTRACK,
            AudioTrackListEventTypes.REMOVETRACK,
            AudioTrackListEventTypes.TRACKLISTCHANGE,
        ) {
            update()
        }
    }

    /** Selects the active audio track, or `null` to disable all audio tracks. */
    fun select(track: MediaTrack<AudioQuality>?) {
        activeAudioTrack = track
        if (track != null) {
            track.isEnabled = true
        } else {
            player?.audioTracks?.forEach { it.isEnabled = false }
        }
    }
}

/** Creates an [AudioTrackState] that's remembered across compositions. */
@ExperimentalPlayerStateApi
@Composable
fun rememberAudioTrackState(player: Player?): AudioTrackState {
    val state = remember(player) { AudioTrackState(player) }
    LaunchedEffect(state) { state.observe() }
    return state
}
