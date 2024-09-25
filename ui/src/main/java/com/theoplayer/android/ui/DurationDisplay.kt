package com.theoplayer.android.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * A text display that shows the player's duration.
 *
 * @param modifier the [Modifier] to be applied to this text
 */
@Composable
fun DurationDisplay(
    modifier: Modifier = Modifier
) {
    val player = Player.current
    val duration = player?.seekable?.lastEnd ?: player?.duration ?: Double.NaN

    Text(modifier = modifier, text = formatTime(duration))
}