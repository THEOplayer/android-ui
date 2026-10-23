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
 * A seek bar showing the player's current time and allowing D-pad seeking.
 *
 * Press Left or Right to seek by [seekOffset] seconds. Hold either key to scrub through the
 * timeline; playback pauses while scrubbing.
 *
 * @param modifier the [Modifier] to be applied to this seek bar
 * @param colors [SliderColors] that resolve the seek bar colors for enabled and disabled states.
 *   See [SliderDefaults.colors].
 * @param seekOffset the number of seconds to seek on a single Left or Right press
 */
@ExperimentalTvUiApi
@OptIn(ExperimentalPlayerStateApi::class)
@Composable
fun SeekBar(
    modifier: Modifier = Modifier,
    colors: SliderColors = SliderDefaults.colors(),
    seekOffset: Int = 10,
) {
    SeekBar(
        state = rememberSeekBarState(LocalTHEOplayerView.current?.player),
        modifier = modifier,
        colors = colors,
        seekOffset = seekOffset,
    )
}

/**
 * A seek bar showing the current time from [state] and allowing D-pad seeking.
 *
 * Press Left or Right to seek by [seekOffset] seconds. Hold either key to scrub through the
 * timeline; playback pauses while scrubbing.
 *
 * @param state the [SeekBarState] to read and control
 * @param modifier the [Modifier] to be applied to this seek bar
 * @param colors [SliderColors] that resolve the seek bar colors for enabled and disabled states.
 *   See [SliderDefaults.colors].
 * @param seekOffset the number of seconds to seek on a single Left or Right press
 */
@ExperimentalTvUiApi
@ExperimentalPlayerStateApi
@Composable
fun SeekBar(
    state: SeekBarState,
    modifier: Modifier = Modifier,
    colors: SliderColors = SliderDefaults.colors(),
    seekOffset: Int = 10,
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
                SliderDefaults.Thumb(
                    interactionSource = interactionSource,
                    colors = colors,
                    enabled = state.enabled,
                )
            }
        },
        track = { sliderState ->
            SliderDefaults.Track(
                sliderState = sliderState,
                colors = colors,
                enabled = state.enabled,
            )
        },
    )
}
