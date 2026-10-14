package com.theoplayer.android.ui.tv

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import kotlin.math.roundToInt

private val ThumbSize = 16.dp
private const val TrackLayoutId = "track"
private const val ThumbLayoutId = "thumb"

/**
 * Holds the value and range for a TV slider.
 */
@ExperimentalTvUiApi
@Stable
class SliderState(
    value: Float = 0f,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f
) {
    private var valueState by mutableFloatStateOf(value.coerceIn(valueRange))

    /** The current slider value, constrained to [valueRange]. */
    var value: Float
        get() = valueState
        set(value) {
            valueState = value.coerceIn(valueRange)
        }

    /** The range of values available to the slider. */
    var valueRange by mutableStateOf(valueRange)

    /** The current value's fraction within [valueRange]. */
    val coercedValueAsFraction: Float
        get() {
            val range = valueRange
            val width = range.endInclusive - range.start
            if (width == 0f) return 0f
            return ((value.coerceIn(range) - range.start) / width).coerceIn(0f, 1f)
        }

    /** Whether the user is holding Left or Right to scrub. */
    var isDragging by mutableStateOf(false)
        internal set
}

/**
 * Displays a focusable slider controlled by D-pad keys.
 */
@ExperimentalTvUiApi
@Composable
fun Slider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    keyStep: Float = (valueRange.endInclusive - valueRange.start) / 100f,
    keyRepeatStep: Float = keyStep,
    onValueChangeFinished: (() -> Unit)? = null,
    colors: SliderColors = SliderDefaults.colors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    thumb: @Composable (SliderState) -> Unit = {
        SliderDefaults.Thumb(interactionSource = interactionSource, colors = colors)
    },
    track: @Composable (SliderState) -> Unit = { sliderState ->
        SliderDefaults.Track(sliderState = sliderState, colors = colors)
    }
) {
    val state = rememberSyncedSliderState(value, valueRange)
    Slider(
        state = state,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        keyStep = keyStep,
        keyRepeatStep = keyRepeatStep,
        onValueChangeFinished = onValueChangeFinished,
        colors = colors,
        interactionSource = interactionSource,
        thumb = thumb,
        track = track
    )
}

/**
 * Displays a slider backed by [state].
 */
@ExperimentalTvUiApi
@Composable
fun Slider(
    state: SliderState,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    keyStep: Float = (state.valueRange.endInclusive - state.valueRange.start) / 100f,
    keyRepeatStep: Float = keyStep,
    onValueChangeFinished: (() -> Unit)? = null,
    colors: SliderColors = SliderDefaults.colors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    thumb: @Composable (SliderState) -> Unit = {
        SliderDefaults.Thumb(interactionSource = interactionSource, colors = colors)
    },
    track: @Composable (SliderState) -> Unit = { sliderState ->
        SliderDefaults.Track(sliderState = sliderState, colors = colors)
    }
) {
    val range = state.valueRange
    val sliderModifier = modifier
        .fillMaxWidth()
        .height(ThumbSize)
        .semantics {
            progressBarRangeInfo = ProgressBarRangeInfo(state.value, range)
            setProgress { value ->
                if (!enabled) {
                    false
                } else {
                    state.value = value
                    onValueChange(state.value)
                    onValueChangeFinished?.invoke()
                    true
                }
            }
            if (!enabled) disabled()
        }
        .sliderKeyEvents(
            state = state,
            enabled = enabled,
            keyStep = keyStep,
            keyRepeatStep = keyRepeatStep,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished
        )
        .focusable(interactionSource = interactionSource)

    SliderLayout(
        state = state,
        modifier = sliderModifier,
        thumb = thumb,
        track = track
    )
}

/**
 * Creates a remembered slider state synchronized with external values.
 */
@Composable
internal fun rememberSyncedSliderState(
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>
): SliderState {
    val state = remember { SliderState(value, valueRange) }
    state.valueRange = valueRange
    if (!state.isDragging) state.value = value
    return state
}

/**
 * Colors used to draw a TV slider.
 */
@ExperimentalTvUiApi
@Immutable
class SliderColors(
    /** Thumb color. */
    val thumbColor: Color,
    /** Active track color. */
    val activeTrackColor: Color,
    /** Inactive track color. */
    val inactiveTrackColor: Color
)

/**
 * Creates slider colors and default slider content.
 */
@ExperimentalTvUiApi
object SliderDefaults {
    /**
     * Creates a slider color set from the current TV theme.
     */
    @Composable
    fun colors(
        thumbColor: Color = MaterialTheme.colorScheme.primary,
        activeTrackColor: Color = MaterialTheme.colorScheme.primary,
        inactiveTrackColor: Color =
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.24f)
    ): SliderColors = SliderColors(
        thumbColor = thumbColor,
        activeTrackColor = activeTrackColor,
        inactiveTrackColor = inactiveTrackColor
    )

    /**
     * Displays a slider thumb that grows when focused.
     */
    @Composable
    fun Thumb(
        interactionSource: MutableInteractionSource,
        modifier: Modifier = Modifier,
        colors: SliderColors = colors()
    ) {
        val focused by interactionSource.collectIsFocusedAsState()
        val radius by animateDpAsState(
            targetValue = if (focused) 8.dp else 4.dp,
            label = "slider-thumb-radius"
        )

        Canvas(modifier.size(ThumbSize)) {
            drawCircle(
                color = colors.thumbColor,
                radius = radius.toPx()
            )
        }
    }

    /**
     * Displays a slider track with active and inactive sections.
     */
    @Composable
    fun Track(
        sliderState: SliderState,
        modifier: Modifier = Modifier,
        colors: SliderColors = colors()
    ) {
        Canvas(modifier.fillMaxWidth().height(4.dp)) {
            val centerY = size.height / 2
            val end = Offset(size.width, centerY)
            val strokeWidth = 4.dp.toPx()
            drawLine(
                color = colors.inactiveTrackColor,
                start = Offset(0f, centerY),
                end = end,
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            drawLine(
                color = colors.activeTrackColor,
                start = Offset(0f, centerY),
                end = Offset(size.width * sliderState.coercedValueAsFraction, centerY),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun Modifier.sliderKeyEvents(
    state: SliderState,
    enabled: Boolean,
    keyStep: Float,
    keyRepeatStep: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: (() -> Unit)?
): Modifier {
    val currentOnValueChange by rememberUpdatedState(onValueChange)
    val currentOnValueChangeFinished by rememberUpdatedState(onValueChangeFinished)
    var isKeyPressed by remember(state) { mutableStateOf(false) }

    fun finishDragging() {
        if (state.isDragging) {
            state.isDragging = false
            currentOnValueChangeFinished?.invoke()
        }
    }

    LaunchedEffect(enabled) {
        if (!enabled) finishDragging()
    }

    return onFocusChanged {
        if (!it.isFocused) {
            isKeyPressed = false
            finishDragging()
        }
    }.onKeyEvent { event ->
        val direction = when (event.key) {
            Key.DirectionLeft -> -1
            Key.DirectionRight -> 1
            else -> return@onKeyEvent false
        }

        when (event.type) {
            KeyEventType.KeyDown -> {
                if (!enabled) return@onKeyEvent isKeyPressed
                isKeyPressed = true
                // The first key down is a single step; repeats indicate a held key, which scrubs.
                // Accumulate repeats from SliderState because external values may update later.
                if (event.nativeKeyEvent.repeatCount == 0) {
                    state.value += direction * keyStep
                } else {
                    state.isDragging = true
                    state.value += direction * keyRepeatStep
                }
                currentOnValueChange(state.value)
                true
            }

            KeyEventType.KeyUp -> {
                if (!isKeyPressed) return@onKeyEvent enabled
                isKeyPressed = false
                if (state.isDragging) {
                    finishDragging()
                } else if (enabled) {
                    currentOnValueChangeFinished?.invoke()
                }
                true
            }

            else -> false
        }
    }
}

@Composable
private fun SliderLayout(
    state: SliderState,
    modifier: Modifier,
    thumb: @Composable (SliderState) -> Unit,
    track: @Composable (SliderState) -> Unit
) {
    Layout(
        modifier = modifier,
        content = {
            Box(Modifier.layoutId(TrackLayoutId)) { track(state) }
            Box(Modifier.layoutId(ThumbLayoutId)) { thumb(state) }
        }
    ) { measurables, constraints ->
        val thumbSize = ThumbSize.roundToPx()
        val width = constraints.maxWidth
        val height = thumbSize.coerceIn(constraints.minHeight, constraints.maxHeight)
        val trackWidth = (width - thumbSize).coerceAtLeast(0)
        val track = measurables.first { it.layoutId == TrackLayoutId }.measure(
            Constraints(
                minWidth = trackWidth,
                maxWidth = trackWidth,
                maxHeight = height
            )
        )
        val thumb = measurables.first { it.layoutId == ThumbLayoutId }.measure(
            Constraints(maxWidth = width, maxHeight = height)
        )
        val thumbCenterX = thumbSize / 2 + (trackWidth * state.coercedValueAsFraction).roundToInt()

        layout(width, height) {
            track.place(
                x = thumbSize / 2,
                y = (height - track.height) / 2
            )
            thumb.place(
                x = thumbCenterX - thumb.width / 2,
                y = (height - thumb.height) / 2
            )
        }
    }
}
