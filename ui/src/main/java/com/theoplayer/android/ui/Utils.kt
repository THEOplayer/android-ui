package com.theoplayer.android.ui

import com.theoplayer.android.api.player.track.Track
import java.util.*

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