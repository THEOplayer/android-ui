package com.theoplayer.android.ui.tv

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.fadeIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import kotlin.time.DurationUnit

/**
 * An indicator that shows whether the player is currently waiting for more data to resume playback.
 *
 * @param modifier the [Modifier] to be applied to this indicator
 * @param color color of this progress indicator
 * @param strokeWidth stroke width of this progress indicator
 * @param delay delay for the progress indicator to become visible after the player starts buffering.
 * In general, it's less jarring to *not* show the indicator for very short buffering periods.
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
 * An indicator that shows whether the player is currently waiting for more data to resume playback.
 *
 * @param state the [LoadingState] to read
 * @param modifier the [Modifier] to be applied to this indicator
 * @param color color of this progress indicator
 * @param strokeWidth stroke width of this progress indicator
 * @param delay delay for the progress indicator to become visible after the player starts buffering.
 * In general, it's less jarring to *not* show the indicator for very short buffering periods.
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
    AnimatedVisibility(
        visible = state.loading,
        // Show the loading spinner only after a small delay
        enter = fadeIn(snap(delayMillis = delay.toInt(DurationUnit.MILLISECONDS))),
        // Hide the loading spinner immediately when loaded
        exit = ExitTransition.None
    ) {
        CircularProgressIndicator(modifier = modifier, color = color, strokeWidth = strokeWidth)
    }
}
