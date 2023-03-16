package com.theoplayer.android.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun CurrentTimeDisplay(
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    showRemaining: Boolean = false,
    showDuration: Boolean = false,
) {
    val state = LocalTHEOplayer.current
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

    Text(modifier = modifier, color = color, text = text.toString())
}