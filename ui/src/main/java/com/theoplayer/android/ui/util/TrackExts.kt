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
internal val Track.localizedLanguageName: String?
    get() {
        val languageCode = this.language
            ?.takeUnless { it.isBlank() || it == LANGUAGE_UNDEFINED }
            ?: return null
        val locale = Locale.forLanguageTag(languageCode)
        val localisedLanguage: String? = locale.getDisplayName(locale)
        return localisedLanguage?.takeUnless { it.isBlank() }
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
 * 3. Track caption channel if a text CEA-608 track
 * 4. Track label if was either a language code or a CEA-prefixed string
 *
 * If none of the above is satisfied, returns `null`.
 *
 * On version 11 and later the logic has slightly changed as
 * the player no longer constructs the [Track.getLabel] internally:
 * 1. Track label
 * 2. Track language display name
 * 3. Track caption channel
 */
internal fun constructLabel(
    track: Track,
): String? {
    val playerVersion = Version.parse(THEOplayerGlobal.getVersion()) ?: Version.ZERO

    val label: String? = if (
        (track is TextTrack) &&
        playerVersion.major < 11 &&
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

    if ((track is TextTrack) && track.type == TextTrackType.CEA608) {
        track.captionChannelCompat
            ?.let { getLabelForChannelNumber(it) }
            ?.let { return it }

        track.label
            ?.takeUnless { it.isBlank() }
            ?.let { return it }
    }

    return null
}

/**
 * Returns [TextTrack.getCaptionChannel], if available.
 */
private val TextTrack.captionChannelCompat: Int?
    get() = textTrackCaptionChannelGetter?.invoke(this) as? Int

private val textTrackCaptionChannelGetter: Method? by lazy {
    try {
        TextTrack::class.java.getMethod("getCaptionChannel").also {
            check(it.returnType.kotlin == Int::class)
        }
    } catch (_: NoSuchMethodException) {
        null
    } catch (_: SecurityException) {
        null
    } catch (_: IllegalStateException) {
        null
    }
}
