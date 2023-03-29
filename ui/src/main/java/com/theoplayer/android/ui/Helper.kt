package com.theoplayer.android.ui

import com.theoplayer.android.api.player.track.Track
import java.util.*
import kotlin.math.absoluteValue

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

internal fun formatTrackLabel(track: Track): String {
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
    return ""
}