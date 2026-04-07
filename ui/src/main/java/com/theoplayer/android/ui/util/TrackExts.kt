package com.theoplayer.android.ui.util

import androidx.annotation.CheckResult
import com.theoplayer.android.api.player.track.Track
import com.theoplayer.android.api.player.track.mediatrack.MediaTrack
import com.theoplayer.android.api.player.track.texttrack.TextTrack
import com.theoplayer.android.api.player.track.texttrack.TextTrackType
import java.util.Locale

private const val LANGUAGE_UNDEFINED = "und"

/**
 * Returns a name for the [Track.language] in the
 * [Locale.Category.DISPLAY] locale that is appropriate
 * for display to the user.
 * If such conversion is not possible, for instance
 * when [Track.language] is `null`, blank, or `"und"`,
 * returns `null`.
 */
@get:CheckResult
internal val Track.localizedLanguageName: String?
    get() {
        val languageCode = this.language
            ?.takeUnless { it.isBlank() || it == LANGUAGE_UNDEFINED }
            ?: return null
        val locale = Locale.forLanguageTag(languageCode)
        val localisedLanguage = locale.getDisplayName(locale).orEmpty()
        return localisedLanguage.takeUnless { it.isBlank() }
    }

/**
 * Constructs a label for the given [MediaTrack] instance.
 */
internal fun constructLabel(track: MediaTrack<*>): String? {
    return track.label?.takeUnless { it.isBlank() }
        ?: track.localizedLanguageName
}

/**
 * Constructs a label for the given [TextTrack] instance.
 */
internal fun constructLabel(track: TextTrack): String? {
    val label: String? = if (
        (isLabelCeaFormatted(track.label) || (track.label != null && track.language == track.label))
    ) {
        // If we are below 11th major release
        // and the label is CEA-formatted we
        // can safely assume it was the last resort
        // option to produce a meaningful label, given
        // we cannot localize the language code in the player.
        null
    } else {
        // With 11 release, the player will no longer
        // prefix text tracks with "CC" for CEA-608 and CEA-708,
        // if [Track.label] is `null`.
        track.label
    }

    if (!label.isNullOrBlank()) {
        return label
    }

    track.localizedLanguageName?.let { return it }

    if (track.type == TextTrackType.CEA608) {
        track.captionChannelCompat
            ?.let { getLabelForChannelNumber(it) }
            ?.let { return it }

        track.label
            ?.takeUnless { it.isBlank() }
            ?.let { return it }
    }

    return null
}
