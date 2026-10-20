package com.theoplayer.android.ui

import android.graphics.Typeface

/**
 * A font family that can be selected in a text track style menu, such as [TextTrackStyleMenu].
 *
 * Changing the font family requires THEOplayer 9.9.0 or higher.
 */
@ExperimentalPlayerStateApi
enum class TextTrackFontFamily(internal val familyName: String?) {
    /** The default font family of the system or the player. */
    DEFAULT(null),
    CASUAL("casual"),
    CURSIVE("cursive"),
    MONOSPACE_SANS_SERIF("sans-serif-monospace"),
    MONOSPACE_SERIF("serif-monospace"),
    SANS_SERIF("sans-serif"),
    SERIF("serif"),
    SMALL_CAPITALS("sans-serif-smallcaps");

    // This cached instance is passed to setFont(), so that getFont() returns an equal typeface.
    internal val typeface: Typeface? by lazy {
        familyName?.let { Typeface.create(it, Typeface.NORMAL) }
    }
}
