package com.theoplayer.android.ui.tv

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.theoplayer.android.ui.ExperimentalPlayerStateApi
import com.theoplayer.android.ui.LocalTHEOplayerView
import com.theoplayer.android.ui.SeekBarState
import com.theoplayer.android.ui.rememberSeekBarState

/**
 * Displays the player's seek bar.
 */
@ExperimentalTvUiApi
@OptIn(ExperimentalPlayerStateApi::class)
@Composable
fun SeekBar(
    modifier: Modifier = Modifier,
    colors: SliderColors = SliderDefaults.colors(),
    seekOffset: Int = 10
) {
    SeekBar(
        state = rememberSeekBarState(LocalTHEOplayerView.current?.player),
        modifier = modifier,
        colors = colors,
        seekOffset = seekOffset
    )
}

/**
 * Displays the seek bar from [state].
 */
@ExperimentalTvUiApi
@ExperimentalPlayerStateApi
@Composable
fun SeekBar(
    state: SeekBarState,
    modifier: Modifier = Modifier,
    colors: SliderColors = SliderDefaults.colors(),
    seekOffset: Int = 10
) {
    val interactionSource = remember { MutableInteractionSource() }
    val sliderState = rememberSyncedSliderState(state.value, state.valueRange)
    Slider(
        state = sliderState,
        onValueChange = { value ->
            if (sliderState.isDragging) {
                state.onValueChange(value)
            } else {
                state.seekTo(value)
            }
        },
        modifier = modifier,
        enabled = state.enabled,
        keyStep = seekOffset.toFloat(),
        keyRepeatStep = (state.valueRange.endInclusive - state.valueRange.start) / 100f,
        onValueChangeFinished = state::onValueChangeFinished,
        colors = colors,
        interactionSource = interactionSource,
        thumb = {
            if (state.showThumb) {
                SliderDefaults.Thumb(interactionSource = interactionSource, colors = colors)
            }
        },
        track = { sliderState ->
            SliderDefaults.Track(sliderState = sliderState, colors = colors)
        }
    )
}
