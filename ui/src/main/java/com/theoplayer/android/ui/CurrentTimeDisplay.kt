package com.theoplayer.android.ui

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun CurrentTimeDisplay(
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    showDuration: Boolean = false,
) {
    val state = LocalTHEOplayer.current
    val currentTime = state?.currentTime?.value ?: 0.0
    val duration = state?.duration?.value ?: Double.NaN

    val text = StringBuilder()
    text.append(formatTime(currentTime, duration))
    if (showDuration) {
        text.append(" / ")
        text.append(formatTime(duration))
    }

    Text(modifier = modifier, color = color, text = text.toString())
}