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
    /** A serif font where every glyph has the same width. */
    MONOSPACE_SERIF("serif-monospace"),
    /** A serif font where glyphs have varying widths. */
    PROPORTIONAL_SERIF("serif"),
    /** A sans-serif font where every glyph has the same width. */
    MONOSPACE_SANS("monospace"),
    /** A sans-serif font where glyphs have varying widths. */
    PROPORTIONAL_SANS("sans-serif");

    // This cached instance is passed to setFont(), so that getFont() returns an equal typeface.
    internal val typeface: Typeface? by lazy {
        familyName?.let { Typeface.create(it, Typeface.NORMAL) }
    }
}
