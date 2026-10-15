package com.theoplayer.android.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect
import com.theoplayer.android.api.player.Player
import com.theoplayer.android.api.player.track.texttrack.TextTrackStyle
import com.theoplayer.android.ui.util.marginBottomCompat
import kotlin.math.ceil

/**
 * The space covered by the top and bottom chrome, as a percentage of the player's height.
 */
internal data class ChromeMargins(val top: Int = 0, val bottom: Int = 0)

/**
 * The bounds of the player and its top and bottom chrome, in root coordinates.
 */
@Stable
internal class ChromeBounds {
    var player by mutableStateOf(Rect.Zero)
    var topChrome by mutableStateOf(Rect.Zero)
    var bottomChrome by mutableStateOf(Rect.Zero)

    /**
     * Returns the space covered by the top and bottom chrome.
     */
    val margins: ChromeMargins
        get() = ChromeMargins(
            top = if (topChrome.isEmpty) 0 else percentOfPlayerHeight(topChrome.bottom - player.top),
            bottom = if (bottomChrome.isEmpty) 0 else percentOfPlayerHeight(player.bottom - bottomChrome.top)
        )

    /**
     * Converts a distance to a percentage of the player's height, rounded up.
     */
    private fun percentOfPlayerHeight(distance: Float): Int {
        if (player.height <= 0f) return 0
        return ceil(distance / player.height * 100f).toInt().coerceIn(0, 100)
    }
}

/**
 * Moves the player's text tracks out of the way of the given [chromeMargins].
 *
 * The margins are restored when the effect leaves the composition.
 */
@Composable
internal fun TextTrackMarginsEffect(player: Player?, chromeMargins: ChromeMargins) {
    val updater = remember(player) { TextTrackMarginsUpdater() }
    DisposableEffect(player, chromeMargins) {
        player?.textTrackStyle?.let { updater.update(it, chromeMargins) }
        onDispose {}
    }
    DisposableEffect(player) {
        onDispose {
            player?.textTrackStyle?.let { updater.update(it, ChromeMargins()) }
        }
    }
}

/**
 * Keeps text tracks clear of the chrome, without reducing the margins set by the user.
 *
 * Based on `UIContainer._updateTextTrackMargins` in [Open Video UI for Web](https://github.com/THEOplayer/web-ui).
 */
internal class TextTrackMarginsUpdater {
    private val top = MarginUpdater(
        get = { it.marginTop },
        set = { style, margin -> style.marginTop = margin }
    )
    private val bottom = MarginUpdater(
        get = { it.marginBottomCompat },
        set = { style, margin -> style.marginBottomCompat = margin }
    )

    /**
     * Updates the margins of the [style] to keep clear of the [chromeMargins].
     */
    fun update(style: TextTrackStyle, chromeMargins: ChromeMargins) {
        top.update(style, chromeMargins.top)
        bottom.update(style, chromeMargins.bottom)
    }
}

/**
 * Updates a single margin of a [TextTrackStyle].
 */
private class MarginUpdater(
    private val get: (TextTrackStyle) -> Int?,
    private val set: (TextTrackStyle, Int) -> Unit
) {
    private var userMargin: Int = 0
    private var appliedMargin: Int? = null

    /**
     * Sets the margin to the largest of the user's margin and the [chromeMargin].
     */
    fun update(style: TextTrackStyle, chromeMargin: Int) {
        val currentMargin = get(style) ?: return
        // A value different from the last update was set by the user.
        if (currentMargin != appliedMargin) {
            userMargin = currentMargin
        }
        val margin = maxOf(userMargin, chromeMargin)
        if (currentMargin != margin) {
            set(style, margin)
        }
        appliedMargin = margin
    }
}
