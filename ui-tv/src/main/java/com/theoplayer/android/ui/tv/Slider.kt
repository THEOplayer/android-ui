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
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
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
 *
 * @param value the initial slider value, constrained to [valueRange]
 * @param valueRange the range of values available to the slider
 */
@ExperimentalTvUiApi
@Stable
class SliderState(
    value: Float = 0f,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
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
 *
 * A single Left or Right press changes the value by [keyStep]. Holding a key changes it by
 * [keyRepeatStep] for each repeat.
 *
 * @param value the current slider value, constrained to [valueRange]
 * @param onValueChange called with each value change
 * @param modifier the [Modifier] to be applied to this slider
 * @param enabled whether the slider responds to user input
 * @param valueRange the range of values available to the slider
 * @param keyStep the value change for a single Left or Right press
 * @param keyRepeatStep the value change for each key repeat while Left or Right is held
 * @param onValueChangeFinished called on key-up, when focus is lost, or when the slider is disabled
 * @param colors the colors used for enabled and disabled states. See [SliderDefaults.colors].
 * @param interactionSource the [MutableInteractionSource] used to observe focus interactions
 * @param thumb the thumb slot, which receives the current [SliderState]
 * @param track the track slot, which receives the current [SliderState]
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
        SliderDefaults.Thumb(
            interactionSource = interactionSource,
            colors = colors,
            enabled = enabled,
        )
    },
    track: @Composable (SliderState) -> Unit = { sliderState ->
        SliderDefaults.Track(sliderState = sliderState, colors = colors, enabled = enabled)
    },
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
        track = track,
    )
}

/**
 * Displays a slider backed by [state] and controlled by D-pad keys.
 *
 * A single Left or Right press changes the value by [keyStep]. Holding a key changes it by
 * [keyRepeatStep] for each repeat.
 *
 * @param state the [SliderState] that holds the slider value and range
 * @param onValueChange called with each value change
 * @param modifier the [Modifier] to be applied to this slider
 * @param enabled whether the slider responds to user input
 * @param keyStep the value change for a single Left or Right press
 * @param keyRepeatStep the value change for each key repeat while Left or Right is held
 * @param onValueChangeFinished called on key-up, when focus is lost, or when the slider is disabled
 * @param colors the colors used for enabled and disabled states. See [SliderDefaults.colors].
 * @param interactionSource the [MutableInteractionSource] used to observe focus interactions
 * @param thumb the thumb slot, which receives the current [SliderState]
 * @param track the track slot, which receives the current [SliderState]
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
        SliderDefaults.Thumb(
            interactionSource = interactionSource,
            colors = colors,
            enabled = enabled,
        )
    },
    track: @Composable (SliderState) -> Unit = { sliderState ->
        SliderDefaults.Track(sliderState = sliderState, colors = colors, enabled = enabled)
    },
) {
    val range = state.valueRange
    val sliderModifier =
        modifier
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
                onValueChangeFinished = onValueChangeFinished,
            )
            .focusable(interactionSource = interactionSource)

    SliderLayout(
        state = state,
        modifier = sliderModifier,
        thumb = thumb,
        track = track,
    )
}

/** Creates a remembered slider state synchronized with external values. */
@Composable
internal fun rememberSyncedSliderState(
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
): SliderState {
    val state = remember { SliderState(value, valueRange) }
    state.valueRange = valueRange
    if (!state.isDragging) state.value = value
    return state
}

/** Colors used to draw a TV slider in enabled and disabled states. */
@ExperimentalTvUiApi
@Immutable
class SliderColors(
    /** Thumb color when enabled. */
    val thumbColor: Color,
    /** Active track color when enabled. */
    val activeTrackColor: Color,
    /** Inactive track color when enabled. */
    val inactiveTrackColor: Color,
    /** Thumb color when disabled. */
    val disabledThumbColor: Color,
    /** Active track color when disabled. */
    val disabledActiveTrackColor: Color,
    /** Inactive track color when disabled. */
    val disabledInactiveTrackColor: Color,
) {
    /** Returns the thumb color for [enabled]. */
    internal fun thumbColor(enabled: Boolean): Color =
        if (enabled) thumbColor else disabledThumbColor

    /** Returns the active track color for [enabled]. */
    internal fun activeTrackColor(enabled: Boolean): Color =
        if (enabled) activeTrackColor else disabledActiveTrackColor

    /** Returns the inactive track color for [enabled]. */
    internal fun inactiveTrackColor(enabled: Boolean): Color =
        if (enabled) inactiveTrackColor else disabledInactiveTrackColor
}

/** Creates slider colors and default slider content. */
@ExperimentalTvUiApi
object SliderDefaults {
    /**
     * Creates a slider color set from the current TV theme.
     *
     * @param thumbColor thumb color when enabled
     * @param activeTrackColor active track color when enabled
     * @param inactiveTrackColor inactive track color when enabled
     * @param disabledThumbColor thumb color when disabled
     * @param disabledActiveTrackColor active track color when disabled
     * @param disabledInactiveTrackColor inactive track color when disabled
     */
    @Composable
    fun colors(
        thumbColor: Color = MaterialTheme.colorScheme.primary,
        activeTrackColor: Color = MaterialTheme.colorScheme.primary,
        inactiveTrackColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.24f),
        disabledThumbColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        disabledActiveTrackColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        disabledInactiveTrackColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
    ): SliderColors =
        SliderColors(
            thumbColor = thumbColor,
            activeTrackColor = activeTrackColor,
            inactiveTrackColor = inactiveTrackColor,
            disabledThumbColor = disabledThumbColor,
            disabledActiveTrackColor = disabledActiveTrackColor,
            disabledInactiveTrackColor = disabledInactiveTrackColor,
        )

    /**
     * Displays a slider thumb that grows when focused.
     *
     * @param interactionSource the interaction source used to observe focus
     * @param modifier the [Modifier] to be applied to the thumb
     * @param colors the colors used for the thumb in enabled and disabled states
     * @param enabled whether the slider is enabled
     */
    @Composable
    fun Thumb(
        interactionSource: MutableInteractionSource,
        modifier: Modifier = Modifier,
        colors: SliderColors = colors(),
        enabled: Boolean = true,
    ) {
        val focused by interactionSource.collectIsFocusedAsState()
        val radius by
            animateDpAsState(
                targetValue = if (focused) 8.dp else 4.dp,
                label = "slider-thumb-radius",
            )

        Canvas(modifier.size(ThumbSize)) {
            drawCircle(
                color = colors.thumbColor(enabled),
                radius = radius.toPx(),
            )
        }
    }

    /**
     * Displays a slider track with active and inactive sections.
     *
     * @param sliderState the state used to determine the active track length
     * @param modifier the [Modifier] to be applied to the track
     * @param colors the colors used for the track in enabled and disabled states
     * @param enabled whether the slider is enabled
     */
    @Composable
    fun Track(
        sliderState: SliderState,
        modifier: Modifier = Modifier,
        colors: SliderColors = colors(),
        enabled: Boolean = true,
    ) {
        Canvas(modifier.fillMaxWidth().height(4.dp)) {
            val centerY = size.height / 2
            val end = Offset(size.width, centerY)
            val strokeWidth = 4.dp.toPx()
            drawLine(
                color = colors.inactiveTrackColor(enabled),
                start = Offset(0f, centerY),
                end = end,
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
            drawLine(
                color = colors.activeTrackColor(enabled),
                start = Offset(0f, centerY),
                end = Offset(size.width * sliderState.coercedValueAsFraction, centerY),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
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
    onValueChangeFinished: (() -> Unit)?,
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

    LaunchedEffect(enabled) { if (!enabled) finishDragging() }

    return onFocusChanged {
        if (!it.isFocused) {
            isKeyPressed = false
            finishDragging()
        }
    }
        .onKeyEvent { event ->
            val direction =
                when (event.key) {
                    Key.DirectionLeft -> -1
                    Key.DirectionRight -> 1
                    else -> return@onKeyEvent false
                }

            when (event.type) {
                KeyEventType.KeyDown -> {
                    if (!enabled) return@onKeyEvent isKeyPressed
                    isKeyPressed = true
                    // The first key down is a single step; repeats indicate a held key, which
                    // scrubs.
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
    track: @Composable (SliderState) -> Unit,
) {
    Layout(
        modifier = modifier,
        content = {
            Box(Modifier.layoutId(TrackLayoutId)) { track(state) }
            Box(Modifier.layoutId(ThumbLayoutId)) { thumb(state) }
        },
    ) { measurables, constraints ->
        val thumbSize = ThumbSize.roundToPx()
        val width = constraints.maxWidth
        val height = thumbSize.coerceIn(constraints.minHeight, constraints.maxHeight)
        val trackWidth = (width - thumbSize).coerceAtLeast(0)
        val track =
            measurables
                .first { it.layoutId == TrackLayoutId }
                .measure(
                    Constraints(
                        minWidth = trackWidth,
                        maxWidth = trackWidth,
                        maxHeight = height,
                    )
                )
        val thumb =
            measurables
                .first { it.layoutId == ThumbLayoutId }
                .measure(Constraints(maxWidth = width, maxHeight = height))
        val thumbCenterX = thumbSize / 2 + (trackWidth * state.coercedValueAsFraction).roundToInt()

        layout(width, height) {
            track.place(
                x = thumbSize / 2,
                y = (height - track.height) / 2,
            )
            thumb.place(
                x = thumbCenterX - thumb.width / 2,
                y = (height - thumb.height) / 2,
            )
        }
    }
}
