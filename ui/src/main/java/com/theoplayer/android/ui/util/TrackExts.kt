package com.theoplayer.android.ui.util

import androidx.annotation.CheckResult
import com.theoplayer.android.api.THEOplayerGlobal
import com.theoplayer.android.api.player.track.Track
import com.theoplayer.android.api.player.track.texttrack.TextTrack
import com.theoplayer.android.api.player.track.texttrack.TextTrackType
import java.lang.reflect.Method
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
internal val Track.localizedLanguage: String?
    get() {
        val languageCode = this.language
        if (languageCode.isNullOrBlank() || languageCode == LANGUAGE_UNDEFINED) {
            return null
        }

        val localisedLanguage =
            Locale.forLanguageTag(languageCode).displayLanguage
        if (localisedLanguage.isNullOrBlank()) {
            return null
        }

        return localisedLanguage
    }

/**
 * Constructs a label for the given [Track] instance.
 * The method works slightly different for different player version.
 *
 * On version 10 and below the logic checks the following and condition
 * and the first not `null` entry from the list:
 * 1. Track label if is not a language code
 * or a CEA-prefixed string.
 * 2. Track language display name
 * 3. Track channel number if a text CEA-608 track
 * 4. Track label if was either a language code or a CEA-prefixed string
 *
 * If none of the above is satisfied, returns `null`.
 *
 * On version 11 and later the logic has slightly changed as
 * the player no longer constructs the [Track.getLabel] internally:
 * 1. Track label
 * 2. Track language display name
 * 3. Track channel number
 */
internal fun constructLabel(
    track: Track,
): String? {
    val playerVersion = getPlayerMajorVersion(THEOplayerGlobal.getVersion())

    val label: String? = if(
        playerVersion != null &&
        playerVersion < 11 &&
        (track is TextTrack) &&
        (
                isLabelCeaFormatted(track.label) ||
                        (track.label != null && track.language == track.label)
        )) {
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

    val localisedLanguage = track.localizedLanguage
    if (localisedLanguage != null) {
        return localisedLanguage
    }

    if ((track is TextTrack) && track.type == TextTrackType.CEA608) {
        val channelNumberLabel = track.channelNumberCompat?.let { getLabelForChannelNumber(it) }
        if (channelNumberLabel != null) {
            return channelNumberLabel
        }
        if (!track.label.isNullOrBlank()) {
            return track.label
        }
    }

    return null
}

/**
 * Returns [TextTrack.channelNumber], if available.
 */
private val TextTrack.channelNumberCompat: Int?
    get() = textTrackChannelNumberGetter?.invoke(this) as? Int

private val textTrackChannelNumberGetter: Method? by lazy {
    try {
        TextTrack::class.java.getDeclaredMethod("getChannelNumber").also {
            check(it.returnType == Int::class.java)
        }
    } catch (_: Throwable) {
        null
    }
}
