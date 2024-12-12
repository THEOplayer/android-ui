package com.theoplayer.android.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.theoplayer.android.api.player.track.Track
import java.util.Locale
import kotlin.math.absoluteValue

/**
 * Return the given time in a human-readable format, such as `"0:10"` or `"01:23:45"`.
 *
 * If the time is infinite or `NaN`, this returns `"--:--"`.
 *
 * @param time the time (in seconds) to be formatted
 * @param guide another time (in seconds) such that the returned formatted time has the same
 * number of parts and number of leading zeros as this guide.
 * @param preferNegative whether the returned formatted time should preferably be negative,
 * for example because it represents the time remaining in the video.
 */
fun formatTime(time: Double, guide: Double = 0.0, preferNegative: Boolean = false): String {
    val isNegative = time < 0 || (preferNegative && time == 0.0);
    val absoluteTime = time.absoluteValue.toInt()

    val guideMinutes = ((guide / 60) % 60).toInt()
    val guideHours = (guide / 3600).toInt()

    val result = StringBuilder()
    if (isNegative) {
        result.append('-')
    }
    if (time.isNaN() || time.isInfinite()) {
        if (guideHours > 0) {
            result.append("--:")
        }
        result.append("--:--")
    } else {
        val seconds = absoluteTime % 60
        val minutes = (absoluteTime / 60) % 60
        val hours = absoluteTime / 3600
        // Check if we need to show hours.
        val showHours = hours > 0 || guideHours > 0
        if (showHours) {
            result.append(hours)
            result.append(':')
        }
        // If hours are showing, we may need to add a leading zero to minutes.
        // Always show at least one digit of minutes.
        val showMinutesLeadingZero = (showHours || guideMinutes >= 0)
        if (showMinutesLeadingZero) {
            result.append(minutes.toString().padStart(2, '0'))
        } else {
            result.append(minutes)
        }
        result.append(':')
        result.append(seconds.toString().padStart(2, '0'))
    }
    return result.toString()
}

/**
 * Return a human-readable label for the given media track or text track.
 *
 * @param track the media track or text track
 */
@Composable
fun formatTrackLabel(track: Track): String {
    val label = track.label
    if (!label.isNullOrEmpty()) {
        return label
    }
    val languageCode = track.language
    if (!languageCode.isNullOrEmpty()) {
        val locale = Locale.forLanguageTag(languageCode)
        val languageName = locale.getDisplayName(locale)
        if (languageName.isNotEmpty()) {
            return languageName
        }
        return languageCode
    }
    return stringResource(R.string.theoplayer_ui_track_unknown)
}