package com.theoplayer.android.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.theoplayer.android.api.player.track.Track
import com.theoplayer.android.api.player.track.mediatrack.MediaTrack
import com.theoplayer.android.api.player.track.mediatrack.quality.VideoQuality
import com.theoplayer.android.api.player.track.texttrack.TextTrack
import com.theoplayer.android.ui.common.R as CommonR
import com.theoplayer.android.ui.util.constructLabel
import java.text.DecimalFormat

/** Returns a human-readable label for the given media track or text track. */
@InternalUiApi
@Composable
fun formatTrackName(track: Track): String {
    return remember(track.id, track.uid) {
        when (track) {
            is TextTrack -> constructLabel(track)
            is MediaTrack<*> -> constructLabel(track)
            else -> null
        }
    } ?: stringResource(CommonR.string.theoplayer_ui_track_unknown)
}

/** Returns a human-readable label for the given media track. */
@InternalUiApi
@Composable
fun formatTrackName(track: MediaTrack<*>): String {
    return remember(track.id, track.uid) { constructLabel(track) }
        ?: stringResource(CommonR.string.theoplayer_ui_track_unknown)
}

/** Returns a human-readable label for the given text track. */
@InternalUiApi
@Composable
fun formatTrackName(track: TextTrack): String {
    return remember(track.id, track.uid) { constructLabel(track) }
        ?: stringResource(CommonR.string.theoplayer_ui_track_unknown)
}

/** The default playback rates shown in playback rate lists. */
@InternalUiApi val defaultPlaybackRates = listOf(0.25, 0.5, 1.0, 1.25, 1.5, 2.0)

/** Formats a playback rate for display. */
@InternalUiApi
@Composable
fun formatPlaybackRate(rate: Double): String {
    return if (rate == 1.0) {
        stringResource(CommonR.string.theoplayer_ui_playback_rate_normal, rate)
    } else {
        val format = stringResource(CommonR.string.theoplayer_ui_playback_rate_format)
        val decimalFormat = remember(format) { DecimalFormat(format) }
        decimalFormat.format(rate)
    }
}

/** Formats a bandwidth for display. */
@InternalUiApi
@Composable
fun formatBandwidth(bandwidth: Long): String {
    val format =
        stringResource(
            if (bandwidth >= 1e7) {
                CommonR.string.theoplayer_ui_bandwidth_format_10mbps
            } else if (bandwidth >= 1e6) {
                CommonR.string.theoplayer_ui_bandwidth_format_1mbps
            } else {
                CommonR.string.theoplayer_ui_bandwidth_format_kbps
            }
        )
    val decimalFormat = remember(format) { DecimalFormat(format) }
    return decimalFormat.format(bandwidth / (if (bandwidth >= 1e6) 1e6 else 1e3))
}

/** Formats the active or target video quality for display. */
@InternalUiApi
@Composable
fun formatActiveQualityLabel(
    targetVideoQuality: VideoQuality?,
    activeVideoQuality: VideoQuality?,
): String {
    return if (targetVideoQuality != null) {
        stringResource(
            CommonR.string.theoplayer_ui_quality_with_height,
            targetVideoQuality.height,
        )
    } else if (activeVideoQuality != null) {
        stringResource(
            CommonR.string.theoplayer_ui_quality_automatic_with_height,
            activeVideoQuality.height,
        )
    } else {
        stringResource(CommonR.string.theoplayer_ui_quality_automatic)
    }
}
