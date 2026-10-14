package com.theoplayer.android.ui.tv

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import com.theoplayer.android.ui.ExperimentalPlayerStateApi
import com.theoplayer.android.ui.InternalUiApi
import com.theoplayer.android.ui.LoadingState
import com.theoplayer.android.ui.LocalTHEOplayerView
import com.theoplayer.android.ui.rememberLoadingState
import com.theoplayer.android.ui.theme.LocalPlayerAnimations
import kotlin.time.Duration
import kotlinx.coroutines.delay as delayMillis

/**
 * Shows a spinner while the player is loading.
 */
@ExperimentalTvUiApi
@OptIn(ExperimentalPlayerStateApi::class, InternalUiApi::class)
@Composable
fun LoadingSpinner(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    strokeWidth: Dp = 4.dp,
    delay: Duration = LocalPlayerAnimations.current.loadingSpinnerDelay
) {
    LoadingSpinner(
        state = rememberLoadingState(LocalTHEOplayerView.current?.player),
        modifier = modifier,
        color = color,
        strokeWidth = strokeWidth,
        delay = delay
    )
}

/**
 * Shows a spinner while [state] is loading.
 */
@ExperimentalTvUiApi
@ExperimentalPlayerStateApi
@OptIn(InternalUiApi::class)
@Composable
fun LoadingSpinner(
    state: LoadingState,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    strokeWidth: Dp = 4.dp,
    delay: Duration = LocalPlayerAnimations.current.loadingSpinnerDelay
) {
    val inspectionMode = LocalInspectionMode.current
    var delayElapsed by remember(state, delay) { mutableStateOf(false) }
    LaunchedEffect(state.loading, delay) {
        delayElapsed = false
        if (state.loading && !inspectionMode) {
            delayMillis(delay.inWholeMilliseconds.coerceAtLeast(0))
            delayElapsed = true
        }
    }

    if (state.loading && (inspectionMode || delayElapsed)) {
        val transition = rememberInfiniteTransition(label = "loading-spinner")
        val rotation by transition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1_000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "rotation"
        )
        Canvas(modifier = modifier.size(40.dp)) {
            val stroke = strokeWidth.toPx()
            val inset = stroke / 2
            drawArc(
                color = color,
                startAngle = rotation - 90f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = Size(size.width - stroke, size.height - stroke),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
    }
}
