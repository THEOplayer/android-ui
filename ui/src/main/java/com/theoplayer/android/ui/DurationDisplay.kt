package com.theoplayer.android.ui

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun DurationDisplay(
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified
) {
    val state = LocalTHEOplayer.current
    val duration = state?.duration?.value ?: Double.NaN

    Text(modifier = modifier, color = color, text = formatTime(duration))
}