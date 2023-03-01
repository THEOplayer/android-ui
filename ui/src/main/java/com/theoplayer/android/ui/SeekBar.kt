package com.theoplayer.android.ui

import androidx.compose.material.Slider
import androidx.compose.material.SliderColors
import androidx.compose.material.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

@Composable
fun SeekBar(
    modifier: Modifier = Modifier,
    colors: SliderColors = SliderDefaults.colors()
) {
    val state = LocalTHEOplayer.current
    val currentTime = state?.currentTime?.toFloat() ?: 0.0f
    val seekable = state?.seekable ?: TimeRanges(listOf())
    val valueRange = if (seekable.ranges.isEmpty()) {
        0f.rangeTo(0f)
    } else {
        val start = seekable.ranges.first().first
        val end = seekable.ranges.last().second
        start.toFloat().rangeTo(end.toFloat())
    }

    val seekTime = remember { mutableStateOf<Float?>(null) }
    val wasPlayingBeforeSeek = remember { mutableStateOf(false) }

    Slider(
        modifier = modifier,
        colors = colors,
        value = seekTime.value ?: currentTime,
        valueRange = valueRange,
        enabled = seekable.ranges.isNotEmpty(),
        onValueChange = { time ->
            seekTime.value = time
            state?.player?.let {
                if (!it.isPaused) {
                    wasPlayingBeforeSeek.value = true
                    it.pause()
                }
                it.currentTime = time.toDouble()
            }
        },
        onValueChangeFinished = {
            seekTime.value = null
            if (wasPlayingBeforeSeek.value) {
                state?.player?.play()
                wasPlayingBeforeSeek.value = false
            }
        }
    )
}