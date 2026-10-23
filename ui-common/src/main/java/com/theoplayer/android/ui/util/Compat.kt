package com.theoplayer.android.ui.util

import android.graphics.Typeface
import androidx.annotation.DoNotInline
import com.theoplayer.android.api.player.track.texttrack.TextTrack
import com.theoplayer.android.api.player.track.texttrack.TextTrackStyle
import com.theoplayer.android.ui.InternalUiApi

/** Returns [TextTrack.getCaptionChannel], if available. */
@InternalUiApi
val TextTrack.captionChannelCompat: Int?
    get() {
        // TextTrack.getCaptionChannel was added in THEOplayer 10.13.0.
        return if (THEOplayerGlobalExt.version >= version1013) {
            TheoPlayer1013Impl.getTextTrackCaptionChannel(this)
        } else null
    }

/**
 * The bottom margin of the subtitle drawing box, as a percentage of the player's height.
 *
 * Returns `null` and ignores new values if [TextTrackStyle.setMarginBottom] isn't available.
 */
@InternalUiApi
var TextTrackStyle.marginBottomCompat: Int?
    get() {
        // TextTrackStyle.getMarginBottom was added in THEOplayer 9.5.0.
        return if (THEOplayerGlobalExt.version >= version95) {
            TheoPlayer95Impl.getTextTrackStyleMarginBottom(this)
        } else null
    }
    set(value) {
        if (value != null && THEOplayerGlobalExt.version >= version95) {
            TheoPlayer95Impl.setTextTrackStyleMarginBottom(this, value)
        }
    }

/**
 * The font of the text track style.
 *
 * Returns `null` and ignores new values if [TextTrackStyle.setFont] isn't available.
 *
 * @see isTextTrackStyleFontSupported
 */
@InternalUiApi
var TextTrackStyle.fontCompat: Typeface?
    get() {
        // TextTrackStyle.getFont was added in THEOplayer 9.9.0.
        return if (isTextTrackStyleFontSupported) {
            TheoPlayer99Impl.getTextTrackStyleFont(this)
        } else null
    }
    set(value) {
        if (isTextTrackStyleFontSupported) {
            TheoPlayer99Impl.setTextTrackStyleFont(this, value)
        }
    }

/**
 * Whether [TextTrackStyle.getFont] and [TextTrackStyle.setFont] are available.
 *
 * These were added in THEOplayer 9.9.0.
 */
@InternalUiApi
val isTextTrackStyleFontSupported: Boolean
    get() = THEOplayerGlobalExt.version >= version99

private val version95 = Version(major = 9, minor = 5, patchAndPrerelease = "0")
private val version99 = Version(major = 9, minor = 9, patchAndPrerelease = "0")
private val version1013 = Version(major = 10, minor = 13, patchAndPrerelease = "0")

/**
 * This class must be loaded **only** with THEOplayer 9.5.0 or higher.
 *
 * @see TheoPlayer1013Impl
 */
private class TheoPlayer95Impl private constructor() {
    companion object {
        @DoNotInline
        fun getTextTrackStyleMarginBottom(style: TextTrackStyle): Int = style.marginBottom

        @DoNotInline
        fun setTextTrackStyleMarginBottom(style: TextTrackStyle, marginBottom: Int) {
            style.marginBottom = marginBottom
        }
    }
}

/**
 * This class must be loaded **only** with THEOplayer 9.9.0 or higher.
 *
 * @see TheoPlayer1013Impl
 */
private class TheoPlayer99Impl private constructor() {
    companion object {
        @DoNotInline fun getTextTrackStyleFont(style: TextTrackStyle): Typeface? = style.getFont()

        @DoNotInline
        fun setTextTrackStyleFont(style: TextTrackStyle, font: Typeface?) {
            style.setFont(font)
        }
    }
}

/**
 * This class must be loaded **only** with THEOplayer 10.13.0 or higher.
 *
 * This uses the same pattern as AndroidX AppCompat, see e.g.
 * [androidx.appcompat.app.AppCompatDelegate.Api33Impl] and the docs about
 * [API-specific implementations](https://github.com/androidx/androidx/blob/androidx-main/docs/api_guidelines/compat.md#delegating-to-api-specific-implementations-delegating-to-api-specific-implementations)
 * and
 * [static shims](https://github.com/androidx/androidx/blob/androidx-main/docs/api_guidelines/platform_compat.md#static-shims-ex-viewcompat-static-shim).
 */
private class TheoPlayer1013Impl private constructor() {
    companion object {
        @DoNotInline fun getTextTrackCaptionChannel(track: TextTrack): Int? = track.captionChannel
    }
}
