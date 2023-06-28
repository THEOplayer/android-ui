package com.theoplayer.android.ui

import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.*
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
    val player = Player.current
    val currentTime = player?.currentTime?.toFloat() ?: 0.0f
    val seekable = player?.seekable ?: TimeRanges(listOf())
    val valueRange = seekable.bounds ?: 0.0..0.0

    var seekTime by remember { mutableStateOf<Float?>(null) }
    var wasPlayingBeforeSeek by remember { mutableStateOf(false) }

    Slider(
        modifier = modifier,
        colors = colors,
        value = seekTime ?: currentTime,
        valueRange = valueRange.start.toFloat()..valueRange.endInclusive.toFloat(),
        enabled = seekable.isNotEmpty(),
        onValueChange = { time ->
            seekTime = time
            player?.player?.let {
                if (!it.isPaused) {
                    wasPlayingBeforeSeek = true
                    it.pause()
                }
                it.currentTime = time.toDouble()
            }
        },
        onValueChangeFinished = {
            seekTime = null
            if (wasPlayingBeforeSeek) {
                player?.player?.play()
                wasPlayingBeforeSeek = false
            }
        }
    )
}