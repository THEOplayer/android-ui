package com.theoplayer.android.ui.util

import androidx.annotation.CheckResult
import com.theoplayer.android.api.player.track.Track
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
internal val Track.localisedLanguage: String?
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
