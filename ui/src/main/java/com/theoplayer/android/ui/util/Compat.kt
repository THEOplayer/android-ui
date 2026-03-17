package com.theoplayer.android.ui.util

import androidx.annotation.DoNotInline
import com.theoplayer.android.api.player.track.texttrack.TextTrack

/**
 * Returns [TextTrack.getCaptionChannel], if available.
 */
internal val TextTrack.captionChannelCompat: Int?
    get() {
        // TextTrack.getCaptionChannel was added in THEOplayer 10.13.0.
        return if (theoplayerVersion >= version1013) {
            TheoPlayer1013Impl.getTextTrackCaptionChannel(this)
        } else null
    }

private val version1013 = Version(major = 10, minor = 13, patchAndPrerelease = "0")

/**
 * This class must be loaded **only** with THEOplayer 10.13.0 or higher.
 *
 * This uses the same pattern as AndroidX AppCompat,
 * see e.g. [androidx.appcompat.app.AppCompatDelegate.Api33Impl]
 */
private class TheoPlayer1013Impl private constructor() {
    companion object {
        @DoNotInline
        fun getTextTrackCaptionChannel(track: TextTrack): Int? = track.captionChannel
    }
}
