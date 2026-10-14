package com.theoplayer.android.ui

import com.theoplayer.android.api.player.track.texttrack.TextTrack
import com.theoplayer.android.api.player.track.texttrack.TextTrackKind

/**
 * Returns whether the given [TextTrack] can be shown as subtitles.
 */
internal fun isSubtitleTrack(track: TextTrack): Boolean {
    return when (track.kind) {
        TextTrackKind.SUBTITLES.type -> true
        TextTrackKind.CAPTIONS.type -> true
        else -> false
    }
}
