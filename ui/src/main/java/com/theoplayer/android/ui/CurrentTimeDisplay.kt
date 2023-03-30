package com.theoplayer.android.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * A text display that shows the player's current time.
 *
 * @param modifier the [Modifier] to be applied to this text
 * @param showRemaining if set, shows the remaining time of the stream as a negative value
 * (e.g. "-01:23")
 * @param showDuration if set, also shows the duration of the stream
 * (e.g. "01:23 / 10:00")
 */
@Composable
fun CurrentTimeDisplay(
    modifier: Modifier = Modifier,
    showRemaining: Boolean = false,
    showDuration: Boolean = false,
) {
    val state = PlayerState.current
    val currentTime = state?.currentTime ?: 0.0
    val duration = state?.duration ?: Double.NaN

    val time = if (showRemaining) {
        -(duration - currentTime)
    } else {
        currentTime
    }

    val text = StringBuilder()
    text.append(formatTime(time, duration))
    if (showDuration) {
        text.append(" / ")
        text.append(formatTime(duration))
    }

    Text(modifier = modifier, text = text.toString())
}