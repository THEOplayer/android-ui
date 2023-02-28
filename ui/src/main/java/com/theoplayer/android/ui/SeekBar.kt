package com.theoplayer.android.ui

import androidx.compose.material.Slider
import androidx.compose.material.SliderColors
import androidx.compose.material.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SeekBar(
    modifier: Modifier = Modifier,
    colors: SliderColors = SliderDefaults.colors()
) {
    val state = LocalTHEOplayer.current
    val currentTime = state?.currentTime?.value?.toFloat() ?: 0.0f
    val seekable = state?.seekable?.value ?: TimeRanges(listOf())
    val valueRange = if (seekable.ranges.isEmpty()) {
        0f.rangeTo(0f)
    } else {
        val start = seekable.ranges.first().first
        val end = seekable.ranges.last().second
        start.toFloat().rangeTo(end.toFloat())
    }
    Slider(modifier = modifier,
        colors = colors,
        value = currentTime,
        valueRange = valueRange,
        enabled = seekable.ranges.isNotEmpty(),
        onValueChange = { seekTime ->
            state?.player?.let {
                it.currentTime = seekTime.toDouble()
            }
        })
}