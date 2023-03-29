package com.theoplayer.android.ui

import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

/**
 * A seek bar showing the current time of the player, and which seeks the player when clicked or dragged.
 *
 * While the user is dragging the seek bar, the player is temporarily paused.
 *
 * @param modifier the [Modifier] to be applied to this seek bar
 * @param colors [SliderColors] that will be used to resolve the colors used for this seek bar in
 * different states. See [SliderDefaults.colors].
 */
@Composable
fun SeekBar(
    modifier: Modifier = Modifier,
    colors: SliderColors = SliderDefaults.colors()
) {
    val state = LocalTHEOplayer.current
    val currentTime = state?.currentTime?.toFloat() ?: 0.0f
    val seekable = state?.seekable ?: TimeRanges(listOf())
    val valueRange = (seekable.firstStart ?: 0).toFloat().rangeTo((seekable.lastEnd ?: 0).toFloat())

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