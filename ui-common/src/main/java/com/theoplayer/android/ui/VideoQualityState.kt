package com.theoplayer.android.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.theoplayer.android.api.event.Event
import com.theoplayer.android.api.event.EventListener
import com.theoplayer.android.api.event.track.mediatrack.video.VideoTrackEventTypes
import com.theoplayer.android.api.event.track.mediatrack.video.list.VideoTrackListEventTypes
import com.theoplayer.android.api.player.Player
import com.theoplayer.android.api.player.track.mediatrack.MediaTrack
import com.theoplayer.android.api.player.track.mediatrack.quality.VideoQuality

/**
 * State holder for video qualities, such as [QualityList] or [QualityMenu].
 *
 * Create an instance with [rememberVideoQualityState].
 */
@ExperimentalPlayerStateApi
class VideoQualityState internal constructor(private val player: Player?) {
    private val listeners = PlayerStateListeners()
    private val trackListeners = PlayerStateListeners()

    /**
     * The qualities of the active video track.
     */
    var videoQualities by mutableStateOf<List<VideoQuality>>(listOf())
        private set

    /**
     * The currently playing video quality, if any.
     */
    var activeVideoQuality by mutableStateOf<VideoQuality?>(null)
        private set

    /**
     * The desired video quality, or `null` for automatic selection.
     */
    var targetVideoQuality by mutableStateOf<VideoQuality?>(null)
        private set

    private var activeVideoTrack: MediaTrack<VideoQuality>? = null

    private fun update() {
        setActiveVideoTrack(player?.videoTracks?.find { it.isEnabled })
    }

    private fun setActiveVideoTrack(videoTrack: MediaTrack<VideoQuality>?) {
        if (activeVideoTrack == videoTrack) {
            return
        }
        trackListeners.removeAll()
        activeVideoTrack = videoTrack
        videoQualities = videoTrack?.qualities?.toList() ?: listOf()
        activeVideoQuality = videoTrack?.activeQuality
        targetVideoQuality = videoTrack?.targetQuality
        videoTrack?.let { track ->
            trackListeners.run {
                val listener = EventListener<Event<*>> {
                    activeVideoQuality = track.activeQuality
                    targetVideoQuality = track.targetQuality
                }
                track.add(VideoTrackEventTypes.ACTIVEQUALITYCHANGEDEVENT, listener)
                track.add(VideoTrackEventTypes.TARGETQUALITYCHANGEDEVENT, listener)
            }
        }
    }

    internal fun observe() {
        val player = player ?: return
        update()
        listeners.run {
            val listener = EventListener<Event<*>> { update() }
            player.videoTracks.add(VideoTrackListEventTypes.ADDTRACK, listener)
            player.videoTracks.add(VideoTrackListEventTypes.REMOVETRACK, listener)
            player.videoTracks.add(VideoTrackListEventTypes.TRACKLISTCHANGE, listener)
        }
    }

    internal fun dispose() {
        listeners.removeAll()
        trackListeners.removeAll()
        activeVideoTrack = null
    }

    /**
     * Selects the desired video quality, or `null` for automatic selection.
     */
    fun select(quality: VideoQuality?) {
        targetVideoQuality = quality
        activeVideoTrack?.targetQuality = quality
    }
}

/**
 * Creates a [VideoQualityState] that's remembered across compositions.
 */
@ExperimentalPlayerStateApi
@Composable
fun rememberVideoQualityState(player: Player?): VideoQualityState {
    val state = remember(player) { VideoQualityState(player) }
    DisposableEffect(state) {
        state.observe()
        onDispose { state.dispose() }
    }
    return state
}
