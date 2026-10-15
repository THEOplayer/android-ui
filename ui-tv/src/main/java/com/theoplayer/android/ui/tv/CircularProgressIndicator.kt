/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.theoplayer.android.ui.tv

// Adapted from androidx.compose.material3.ProgressIndicator:
// https://android.googlesource.com/platform/frameworks/support/+/080d2b3e5326ba80392d93442c4a51a02dc22650/compose/material3/material3/src/commonMain/kotlin/androidx/compose/material3/ProgressIndicator.kt

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.progressSemantics
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.min

/**
 * [Material Design determinate circular progress
 * indicator](https://m3.material.io/components/progress-indicators/overview)
 *
 * Progress indicators express an unspecified wait time or display the duration of a process.
 *
 * ![Circular progress indicator
 * image](https://firebasestorage.googleapis.com/v0/b/design-spec/o/projects%2Fgoogle-material-3%2Fimages%2Flqdiyyvh-1P-progress-indicator-configurations.png?alt=media)
 *
 * @sample androidx.compose.material3.samples.IndeterminateCircularProgressIndicatorSample
 * @param modifier the [Modifier] to be applied to this progress indicator
 * @param color color of this progress indicator
 * @param strokeWidth stroke width of this progress indicator
 * @param trackColor color of the track behind the indicator, visible when the progress has not
 *   reached the area of the overall indicator yet
 * @param strokeCap stroke cap to use for the ends of this progress indicator
 * @param gapSize size of the gap between the progress indicator and the track
 */
@Composable
internal fun CircularProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color,
    strokeWidth: Dp,
    trackColor: Color = Color.Transparent,
    strokeCap: StrokeCap = StrokeCap.Round,
    gapSize: Dp = CircularIndicatorTrackGapSize,
) {
    val stroke = with(LocalDensity.current) { Stroke(width = strokeWidth.toPx(), cap = strokeCap) }

    val infiniteTransition = rememberInfiniteTransition()
    // A global rotation that does a 360 degrees rotation in 6 seconds.
    val globalRotation =
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = CircularGlobalRotationDegreesTarget,
            animationSpec = circularIndeterminateGlobalRotationAnimationSpec,
        )

    // An additional rotation that moves by 90 degrees in 500ms and then rest for 1 second.
    val additionalRotation =
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = CircularAdditionalRotationDegreesTarget,
            animationSpec = circularIndeterminateRotationAnimationSpec,
        )

    // Indicator progress animation that will be changing the progress up and down as the indicator
    // rotates.
    val progressAnimation =
        infiniteTransition.animateFloat(
            initialValue = CircularIndeterminateMinProgress,
            targetValue = CircularIndeterminateMaxProgress,
            animationSpec = circularIndeterminateProgressAnimationSpec,
        )

    Canvas(modifier.progressSemantics().size(CircularIndicatorDiameter)) {
        val sweep = progressAnimation.value * 360f
        val adjustedGapSize =
            if (strokeCap == StrokeCap.Butt || size.height > size.width) {
                gapSize
            } else {
                gapSize + strokeWidth
            }
        val gapSizeSweep = (adjustedGapSize.value / (PI * size.width.toDp().value).toFloat()) * 360f

        rotate(globalRotation.value + additionalRotation.value) {
            drawCircularIndicator(
                sweep + min(sweep, gapSizeSweep),
                360f - sweep - min(sweep, gapSizeSweep) * 2,
                trackColor,
                stroke,
            )
            drawDeterminateCircularIndicator(startAngle = 0f, sweep, color, stroke)
        }
    }
}

private fun DrawScope.drawCircularIndicator(
    startAngle: Float,
    sweep: Float,
    color: Color,
    stroke: Stroke,
) {
    // To draw this circle we need a rect with edges that line up with the midpoint of the stroke.
    // To do this we need to remove half the stroke width from the total diameter for both sides.
    val diameterOffset = stroke.width / 2
    val arcDimen = size.width - 2 * diameterOffset
    drawArc(
        color = color,
        startAngle = startAngle,
        sweepAngle = sweep,
        useCenter = false,
        topLeft = Offset(diameterOffset, diameterOffset),
        size = Size(arcDimen, arcDimen),
        style = stroke,
    )
}

private fun DrawScope.drawDeterminateCircularIndicator(
    startAngle: Float,
    sweep: Float,
    color: Color,
    stroke: Stroke,
) = drawCircularIndicator(startAngle, sweep, color, stroke)

/** A global animation spec for indeterminate circular progress indicator. */
private val circularIndeterminateGlobalRotationAnimationSpec
    get() =
        infiniteRepeatable<Float>(
            animation = tween(CircularAnimationProgressDuration, easing = LinearEasing)
        )

/**
 * An animation spec for indeterminate circular progress indicators that infinitely rotates a 360
 * degrees.
 */
private val circularIndeterminateRotationAnimationSpec
    get() =
        infiniteRepeatable(
            animation =
                keyframes {
                    durationMillis = CircularAnimationProgressDuration // 6000ms
                    90f at
                        CircularAnimationAdditionalRotationDuration using
                        CircularIndeterminateRotationEasing // 300ms
                    90f at CircularAnimationAdditionalRotationDelay // hold till 1500ms
                    180f at
                        CircularAnimationAdditionalRotationDuration +
                            CircularAnimationAdditionalRotationDelay // 1800ms
                    180f at CircularAnimationAdditionalRotationDelay * 2 // hold till 3000ms
                    270f at
                        CircularAnimationAdditionalRotationDuration +
                            CircularAnimationAdditionalRotationDelay * 2 // 3300ms
                    270f at CircularAnimationAdditionalRotationDelay * 3 // hold till 4500ms
                    360f at
                        CircularAnimationAdditionalRotationDuration +
                            CircularAnimationAdditionalRotationDelay * 3 // 4800ms
                    360f at CircularAnimationProgressDuration // hold till 6000ms
                }
        )

/** An animation spec for indeterminate circular progress indicators progress motion. */
private val circularIndeterminateProgressAnimationSpec
    get() =
        infiniteRepeatable(
            animation =
                keyframes {
                    durationMillis = CircularAnimationProgressDuration // 6000ms
                    CircularIndeterminateMaxProgress at
                        CircularAnimationProgressDuration / 2 using
                        CircularProgressEasing // 3000ms
                    CircularIndeterminateMinProgress at CircularAnimationProgressDuration
                }
        )

// CircularProgressIndicator Material specs
// Diameter of the indicator circle
/*@VisibleForTesting*/
private val CircularIndicatorDiameter = 40.dp // CircularProgressIndicatorTokens.Size
private val CircularIndicatorTrackGapSize = 4.dp // CircularProgressIndicatorTokens.TrackActiveSpace

// Indeterminate circular indicator transition specs

// The indeterminate circular indicator easing constants for its motion
private val CircularProgressEasing =
    CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f) // MotionTokens.EasingStandardCubicBezier
private val CircularIndeterminateRotationEasing =
    CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f) // MotionTokens.EasingEmphasizedDecelerateCubicBezier
private const val CircularIndeterminateMinProgress = 0.1f
private const val CircularIndeterminateMaxProgress = 0.87f

private const val CircularAnimationProgressDuration = 6000
private const val CircularAnimationAdditionalRotationDelay = 1500
private const val CircularAnimationAdditionalRotationDuration = 300
private const val CircularAdditionalRotationDegreesTarget = 360f
private const val CircularGlobalRotationDegreesTarget = 1080f
