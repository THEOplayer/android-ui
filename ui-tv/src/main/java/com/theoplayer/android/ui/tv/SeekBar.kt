package com.theoplayer.android.ui.tv

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import com.theoplayer.android.ui.ExperimentalPlayerStateApi
import com.theoplayer.android.ui.LocalTHEOplayerView
import com.theoplayer.android.ui.SeekBarState
import com.theoplayer.android.ui.rememberSeekBarState
import kotlin.math.max

/**
 * Displays the player's seek bar.
 */
@ExperimentalTvUiApi
@OptIn(ExperimentalPlayerStateApi::class)
@Composable
fun SeekBar(
    modifier: Modifier = Modifier,
    colors: SeekBarColors = SeekBarDefaults.colors(),
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
    colors: SeekBarColors = SeekBarDefaults.colors(),
    seekOffset: Int = 10
) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val thumbRadius by animateDpAsState(
        targetValue = if (focused) 8.dp else 4.dp,
        label = "seekbar-thumb-radius"
    )
    val range = state.valueRange

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(16.dp)
            .semantics {
                progressBarRangeInfo = ProgressBarRangeInfo(state.value, range)
                setProgress { value ->
                    state.seekTo(value.coerceIn(range))
                    true
                }
            }
            .seekBarKeyEvents(state, seekOffset)
            .focusable(interactionSource = interactionSource)
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawSeekBar(state, colors, thumbRadius)
        }
    }
}

@Composable
private fun Modifier.seekBarKeyEvents(state: SeekBarState, seekOffset: Int): Modifier {
    var isScrubbing by remember(state) { mutableStateOf(false) }
    var scrubValue by remember(state) { mutableStateOf(0f) }
    val range = state.valueRange

    fun finishScrubbing() {
        if (isScrubbing) {
            state.onValueChangeFinished()
            isScrubbing = false
        }
    }

    return onFocusChanged {
        if (!it.isFocused) finishScrubbing()
    }.onKeyEvent { event ->
        val direction = when (event.key) {
            Key.DirectionLeft -> -1
            Key.DirectionRight -> 1
            else -> return@onKeyEvent false
        }
        if (!state.enabled && !isScrubbing) return@onKeyEvent false

        when (event.type) {
            KeyEventType.KeyDown -> {
                if (event.nativeKeyEvent.repeatCount == 0) {
                    state.seekTo((state.value + direction * seekOffset).coerceIn(range))
                } else {
                    if (!isScrubbing) {
                        scrubValue = state.value
                        isScrubbing = true
                    }
                    val step = (range.endInclusive - range.start) * 0.01f
                    scrubValue = (scrubValue + direction * step).coerceIn(range)
                    state.onValueChange(scrubValue)
                }
                true
            }

            KeyEventType.KeyUp -> {
                finishScrubbing()
                true
            }

            else -> false
        }
    }
}

private fun DrawScope.drawSeekBar(
    state: SeekBarState,
    colors: SeekBarColors,
    thumbRadius: Dp
) {
    val range = state.valueRange
    val edge = 8.dp.toPx()
    val start = edge
    val end = max(start, size.width - edge)
    val width = end - start
    val valueWidth = (range.endInclusive - range.start).coerceAtLeast(0f)
    val fraction = if (valueWidth == 0f) {
        0f
    } else {
        ((state.value - range.start) / valueWidth).coerceIn(0f, 1f)
    }
    val thumbX = start + width * fraction
    val centerY = size.height / 2
    val trackWidth = 4.dp.toPx()

    drawLine(
        color = colors.inactiveTrackColor,
        start = Offset(start, centerY),
        end = Offset(end, centerY),
        strokeWidth = trackWidth,
        cap = StrokeCap.Round
    )
    drawLine(
        color = colors.activeTrackColor,
        start = Offset(start, centerY),
        end = Offset(thumbX, centerY),
        strokeWidth = trackWidth,
        cap = StrokeCap.Round
    )
    if (state.showThumb) {
        drawCircle(
            color = colors.thumbColor,
            radius = thumbRadius.toPx(),
            center = Offset(thumbX, centerY)
        )
    }
}

/**
 * Colors used to draw a TV seek bar.
 */
@ExperimentalTvUiApi
@Immutable
class SeekBarColors(
    /** Thumb color. */
    val thumbColor: Color,
    /** Active track color. */
    val activeTrackColor: Color,
    /** Inactive track color. */
    val inactiveTrackColor: Color
)

/**
 * Creates colors for a TV seek bar.
 */
@ExperimentalTvUiApi
object SeekBarDefaults {
    /**
     * Creates a seek bar color set from the current TV theme.
     */
    @Composable
    fun colors(
        thumbColor: Color = MaterialTheme.colorScheme.primary,
        activeTrackColor: Color = MaterialTheme.colorScheme.primary,
        inactiveTrackColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.24f)
    ): SeekBarColors = SeekBarColors(
        thumbColor = thumbColor,
        activeTrackColor = activeTrackColor,
        inactiveTrackColor = inactiveTrackColor
    )
}
