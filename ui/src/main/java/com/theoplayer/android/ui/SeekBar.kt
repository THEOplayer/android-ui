package com.theoplayer.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

/**
 * A seek bar showing the current time of the player, and which seeks the player when clicked or
 * dragged.
 *
 * While the user is dragging the seek bar, the player is temporarily paused.
 *
 * @param modifier the [Modifier] to be applied to this seek bar
 * @param colors [SliderColors] that will be used to resolve the colors used for this seek bar in
 *   different states. See [SliderDefaults.colors].
 */
@Composable
fun SeekBar(
    modifier: Modifier = Modifier,
    colors: SliderColors = SliderDefaults.colors(),
) {
    SeekBar(
        state = rememberSeekBarState(LocalTHEOplayerView.current?.player),
        modifier = modifier,
        colors = colors,
    )
}

/**
 * A seek bar showing the current time of the player, and which seeks the player when clicked or
 * dragged.
 *
 * While the user is dragging the seek bar, the player is temporarily paused.
 *
 * @param state the [SeekBarState] to read and control
 * @param modifier the [Modifier] to be applied to this seek bar
 * @param colors [SliderColors] that will be used to resolve the colors used for this seek bar in
 *   different states. See [SliderDefaults.colors].
 */
@ExperimentalPlayerStateApi
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeekBar(
    state: SeekBarState,
    modifier: Modifier = Modifier,
    colors: SliderColors = SliderDefaults.colors(),
) {
    val interactionSource = remember { MutableInteractionSource() }

    Slider(
        modifier = modifier.systemGestureExclusion(),
        colors = colors,
        value = state.value,
        valueRange = state.valueRange,
        enabled = state.enabled,
        interactionSource = interactionSource,
        thumb = {
            if (state.showThumb) {
                SeekBarThumb(
                    interactionSource = interactionSource,
                    colors = colors,
                    enabled = state.enabled,
                )
            }
        },
        track = { sliderState ->
            SliderDefaults.Track(
                modifier = Modifier.height(4.dp),
                colors = colors,
                enabled = state.enabled,
                sliderState = sliderState,
                // Don't draw the stop indicator at the end of the track
                drawStopIndicator = {},
                // Remove the gap in the track around the thumb
                thumbTrackGapSize = 0.dp,
            )
        },
        onValueChange = state::onValueChange,
        onValueChangeFinished = state::onValueChangeFinished,
    )
}

private val ThumbSize = DpSize(20.dp, 20.dp)
private val ThumbDefaultElevation = 1.dp
private val ThumbPressedElevation = 6.dp
private val StateLayerSize = 40.0.dp

// Slider.Thumb look-and-feel from Compose Material3 version 1.2.1
// https://github.com/androidx/androidx/blob/d4655d87a9f8dbced1c3c768a595cbfcea505c07/compose/material3/material3/src/commonMain/kotlin/androidx/compose/material3/Slider.kt#L980
@Composable
private fun SeekBarThumb(
    interactionSource: MutableInteractionSource,
    modifier: Modifier = Modifier,
    colors: SliderColors = SliderDefaults.colors(),
    enabled: Boolean = true,
    thumbSize: DpSize = ThumbSize,
) {
    val interactions = remember { mutableStateListOf<Interaction>() }
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> interactions.add(interaction)
                is PressInteraction.Release -> interactions.remove(interaction.press)
                is PressInteraction.Cancel -> interactions.remove(interaction.press)
                is DragInteraction.Start -> interactions.add(interaction)
                is DragInteraction.Stop -> interactions.remove(interaction.start)
                is DragInteraction.Cancel -> interactions.remove(interaction.start)
            }
        }
    }

    val elevation =
        if (interactions.isNotEmpty()) {
            ThumbPressedElevation
        } else {
            ThumbDefaultElevation
        }
    val shape = CircleShape

    Spacer(
        modifier
            .size(thumbSize)
            .indication(
                interactionSource = interactionSource,
                indication =
                    ripple(
                        bounded = false,
                        radius = StateLayerSize / 2,
                    ),
            )
            .hoverable(interactionSource = interactionSource)
            .shadow(if (enabled) elevation else 0.dp, shape, clip = false)
            .background(colors.thumbColor(enabled), shape)
    )
}

private fun SliderColors.thumbColor(enabled: Boolean): Color =
    if (enabled) thumbColor else disabledThumbColor
