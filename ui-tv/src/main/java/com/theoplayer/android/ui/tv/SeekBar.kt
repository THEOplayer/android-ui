package com.theoplayer.android.ui.tv

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import com.theoplayer.android.api.player.Player
import com.theoplayer.android.ui.ExperimentalPlayerStateApi
import com.theoplayer.android.ui.InternalUiApi
import com.theoplayer.android.ui.LocalTHEOplayerView
import com.theoplayer.android.ui.SeekBarState
import com.theoplayer.android.ui.rememberSeekBarState
import com.theoplayer.android.ui.togglePlayerPlayPause

/**
 * A seek bar showing the player's current time and allowing D-pad seeking.
 *
 * Press Left or Right to seek by [seekOffset] seconds. Hold either key to scrub through the
 * timeline; playback pauses while scrubbing. Press Center to play or pause.
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
 * timeline; playback pauses while scrubbing. Press Center to play or pause.
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
        modifier = modifier.togglePlayPauseOnCenter(LocalTHEOplayerView.current?.player),
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

/**
 * Toggles play/pause when Center or Enter is pressed, following Google's "Playback controls on TV"
 * guide. Only key presses that start on this component toggle playback, so the key press that
 * reveals the controls doesn't toggle twice.
 */
@OptIn(InternalUiApi::class)
@Composable
private fun Modifier.togglePlayPauseOnCenter(player: Player?): Modifier {
    var isKeyPressed by remember { mutableStateOf(false) }
    return onKeyEvent { event ->
        if (!event.isCenterKey()) return@onKeyEvent false
        when (event.type) {
            KeyEventType.KeyDown -> {
                if (event.nativeKeyEvent.repeatCount == 0) {
                    isKeyPressed = true
                    togglePlayerPlayPause(player)
                }
                true
            }
            KeyEventType.KeyUp -> isKeyPressed.also { isKeyPressed = false }
            else -> false
        }
    }
}

/** Checks whether this is a Center or Enter key event. */
private fun KeyEvent.isCenterKey(): Boolean =
    key == Key.DirectionCenter || key == Key.Enter || key == Key.NumPadEnter
