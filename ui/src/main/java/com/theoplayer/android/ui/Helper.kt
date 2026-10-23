package com.theoplayer.android.ui

import androidx.compose.runtime.Composable
import com.theoplayer.android.api.player.track.Track
import com.theoplayer.android.api.player.track.mediatrack.MediaTrack
import com.theoplayer.android.api.player.track.texttrack.TextTrack

/**
 * Return the given time in a human-readable format, such as `"0:10"` or `"01:23:45"`.
 *
 * If the time is infinite or `NaN`, this returns `"--:--"`.
 *
 * @param time the time (in seconds) to be formatted
 * @param guide another time (in seconds) such that the returned formatted time has the same number
 *   of parts and number of leading zeros as this guide.
 * @param preferNegative whether the returned formatted time should preferably be negative, for
 *   example because it represents the time remaining in the video.
 */
fun formatTime(time: Double, guide: Double = 0.0, preferNegative: Boolean = false): String {
    return formatPlaybackTime(time, guide, preferNegative)
}

/**
 * Return a human-readable label for the given media track or text track.
 *
 * @param track the media track or text track
 */
@OptIn(InternalUiApi::class)
@Composable
fun formatTrackLabel(track: Track): String {
    return formatTrackName(track)
}

/**
 * Return a human-readable label for the given media track.
 *
 * @param track the media track
 */
@OptIn(InternalUiApi::class)
@Composable
fun formatTrackLabel(track: MediaTrack<*>): String {
    return formatTrackName(track)
}

/**
 * Return a human-readable label for the given text track.
 *
 * @param track the text track
 */
@OptIn(InternalUiApi::class)
@Composable
fun formatTrackLabel(track: TextTrack): String {
    return formatTrackName(track)
}
