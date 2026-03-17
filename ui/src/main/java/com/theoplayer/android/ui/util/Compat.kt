package com.theoplayer.android.ui.util

import com.theoplayer.android.api.player.track.texttrack.TextTrack
import java.lang.reflect.Method

/**
 * Returns [TextTrack.getCaptionChannel], if available.
 */
internal val TextTrack.captionChannelCompat: Int?
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
