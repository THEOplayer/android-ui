package com.theoplayer.android.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/**
 * A seek bar showing the current time of the player, and which seeks the player when clicked or dragged.
 *
 * While the user is dragging the seek bar, the player is temporarily paused.
 *
 * @param modifier the [Modifier] to be applied to this seek bar
 * @param colors [SliderColors] that will be used to resolve the colors used for this seek bar in
 * different states. See [SliderDefaults.colors].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeekBar(
    modifier: Modifier = Modifier,
    colors: SliderColors = SliderDefaults.colors()
) {
    val player = Player.current
    val currentTime = player?.currentTime?.toFloat() ?: 0.0f
    val seekable = player?.seekable ?: TimeRanges.empty()
    val duration = player?.duration ?: Double.NaN
    val playingAd = player?.playingAd ?: false
    val enabled = seekable.isNotEmpty() && !playingAd

    val valueRange = remember(seekable, duration) {
        seekable.bounds?.let { bounds ->
            bounds.start.toFloat()..bounds.endInclusive.toFloat()
        } ?: run {
            0f..(if (duration.isFinite()) duration.toFloat() else 0f)
        }
    }
    var seekTime by remember { mutableStateOf<Float?>(null) }
    var wasPlayingBeforeSeek by remember { mutableStateOf(false) }

    // val adMarkers = player?.ads?.scheduledAds?.mapNotNull { ad -> ad.adBreak?.timeOffset }?.distinct()?.toIntArray()
    val adMarkers = remember {
        derivedStateOf {
            intArrayOf(0, 10, 30, 60, duration.toInt())
        }
    }

    Slider(
        modifier = modifier,
        colors = colors,
        value = seekTime ?: currentTime,
        valueRange = valueRange,
        enabled = enabled,
        track = { sliderState ->
            SeekbarTrack(
                enabled = enabled,
                colors = colors,
                sliderState = sliderState,
                adMarkerState = adMarkers
            )
        },
        onValueChange = remember {
            { time ->
                seekTime = time
                player?.player?.let {
                    if (!it.isPaused) {
                        wasPlayingBeforeSeek = true
                        it.pause()
                    }
                    it.currentTime = time.toDouble()
                }
            }
        },
        // This needs to always be the *same* callback,
        // otherwise Slider will reset its internal SliderState while dragging.
        // https://github.com/androidx/androidx/blob/4d69c45e6361a2e5af77edc9f7f92af3d0db3877/compose/material3/material3/src/commonMain/kotlin/androidx/compose/material3/Slider.kt#L270-L282
        onValueChangeFinished = remember {
            {
                seekTime = null
                if (wasPlayingBeforeSeek) {
                    player?.player?.play()
                    wasPlayingBeforeSeek = false
                }
            }
        }
    )
}

private val TrackHeight = 4.0.dp

@ExperimentalMaterial3Api
@Composable
private fun SeekbarTrack(
    sliderState: SliderState,
    adMarkerState: State<IntArray?>,
    modifier: Modifier = Modifier,
    colors: SliderColors = SliderDefaults.colors(),
    enabled: Boolean = true
) {
    val inactiveTrackColor = colors.trackColor(enabled, active = false)
    val activeTrackColor = colors.trackColor(enabled, active = true)

    val adMarkers = adMarkerState.value
    val adFractions = remember(adMarkers, sliderState.valueRange) {
        adMarkers?.mapToFloatArray { adMarker ->
            calcFraction(
                sliderState.valueRange.start,
                sliderState.valueRange.endInclusive,
                adMarker.toFloat()
            )
        } ?: floatArrayOf()
    }

    Canvas(
        modifier
            .fillMaxWidth()
            .height(TrackHeight)
    ) {
        drawTrack(
            activeRangeStart = 0f,
            activeRangeEnd = sliderState.coercedValueAsFraction,
            inactiveTrackColor = inactiveTrackColor,
            activeTrackColor = activeTrackColor,
            adColor = activeTrackColor,
            adFractions = adFractions,
        )
    }
}

private fun DrawScope.drawTrack(
    activeRangeStart: Float,
    activeRangeEnd: Float,
    inactiveTrackColor: Color,
    activeTrackColor: Color,
    adColor: Color,
    adFractions: FloatArray,
) {
    val isRtl = layoutDirection == LayoutDirection.Rtl
    val sliderLeft = Offset(0f, center.y)
    val sliderRight = Offset(size.width, center.y)
    val sliderStart = if (isRtl) sliderRight else sliderLeft
    val sliderEnd = if (isRtl) sliderLeft else sliderRight
    val trackStrokeWidth = TrackHeight.toPx()
    drawLine(
        inactiveTrackColor,
        sliderStart,
        sliderEnd,
        trackStrokeWidth,
        StrokeCap.Round
    )
    val sliderValueEnd = Offset(
        sliderStart.x + (sliderEnd.x - sliderStart.x) * activeRangeEnd,
        center.y
    )

    val sliderValueStart = Offset(
        sliderStart.x + (sliderEnd.x - sliderStart.x) * activeRangeStart,
        center.y
    )

    drawLine(
        activeTrackColor,
        sliderValueStart,
        sliderValueEnd,
        trackStrokeWidth,
        StrokeCap.Round
    )

    for (adFraction in adFractions) {
        if (adFraction < 0f || adFraction > 1f) continue
        drawCircle(
            color = adColor,
            center = Offset(lerp(sliderStart, sliderEnd, adFraction).x, center.y),
            radius = trackStrokeWidth / 2f
        )
    }
}

@Stable
private fun SliderColors.trackColor(enabled: Boolean, active: Boolean): Color =
    if (enabled) {
        if (active) activeTrackColor else inactiveTrackColor
    } else {
        if (active) disabledActiveTrackColor else disabledInactiveTrackColor
    }

@Stable
private fun SliderColors.tickColor(enabled: Boolean, active: Boolean): Color =
    if (enabled) {
        if (active) activeTickColor else inactiveTickColor
    } else {
        if (active) disabledActiveTickColor else disabledInactiveTickColor
    }

// Calculate the 0..1 fraction that `pos` value represents between `a` and `b`
private fun calcFraction(a: Float, b: Float, pos: Float) =
    (if (b - a == 0f) 0f else (pos - a) / (b - a)).coerceIn(0f, 1f)

@ExperimentalMaterial3Api
private fun SliderState.coerceAsFraction(value: Float) = calcFraction(
    valueRange.start,
    valueRange.endInclusive,
    value.coerceIn(valueRange.start, valueRange.endInclusive)
)

@ExperimentalMaterial3Api
private val SliderState.coercedValueAsFraction
    get() = coerceAsFraction(value)

private inline fun IntArray.mapToFloatArray(transform: (Int) -> Float): FloatArray {
    val destination = FloatArray(size)
    forEachIndexed { index, element ->
        destination[index] = transform(element)
    }
    return destination
}