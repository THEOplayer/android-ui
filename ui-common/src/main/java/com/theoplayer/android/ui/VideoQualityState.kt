package com.theoplayer.android.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.theoplayer.android.api.event.track.mediatrack.video.VideoTrackEventTypes
import com.theoplayer.android.api.event.track.mediatrack.video.list.VideoTrackListEventTypes
import com.theoplayer.android.api.player.Player
import com.theoplayer.android.api.player.track.mediatrack.MediaTrack
import com.theoplayer.android.api.player.track.mediatrack.quality.VideoQuality
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * State holder for video qualities, such as [QualityList] or [QualityMenu].
 *
 * Create an instance with [rememberVideoQualityState].
 */
@ExperimentalPlayerStateApi
class VideoQualityState internal constructor(private val player: Player?) {

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

    private fun findActiveVideoTrack() = player?.videoTracks?.find { it.isEnabled }

    private fun setActiveVideoTrack(videoTrack: MediaTrack<VideoQuality>?) {
        if (activeVideoTrack == videoTrack) {
            return
        }
        activeVideoTrack = videoTrack
        videoQualities = videoTrack?.qualities?.toList() ?: listOf()
        activeVideoQuality = videoTrack?.activeQuality
        targetVideoQuality = videoTrack?.targetQuality
    }

    init {
        setActiveVideoTrack(findActiveVideoTrack())
    }

    internal suspend fun observe() {
        val player = player ?: return
        val activeTrack = MutableStateFlow(findActiveVideoTrack())
        try {
            coroutineScope {
                launch(start = CoroutineStart.UNDISPATCHED) {
                    activeTrack.collectLatest { track ->
                        setActiveVideoTrack(track)
                        track?.listen(
                            VideoTrackEventTypes.ACTIVEQUALITYCHANGEDEVENT,
                            VideoTrackEventTypes.TARGETQUALITYCHANGEDEVENT
                        ) {
                            activeVideoQuality = track.activeQuality
                            targetVideoQuality = track.targetQuality
                        }
                    }
                }
                player.videoTracks.listen(
                    VideoTrackListEventTypes.ADDTRACK,
                    VideoTrackListEventTypes.REMOVETRACK,
                    VideoTrackListEventTypes.TRACKLISTCHANGE
                ) { activeTrack.value = findActiveVideoTrack() }
            }
        } finally {
            activeVideoTrack = null
        }
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
    LaunchedEffect(state) { state.observe() }
    return state
}
