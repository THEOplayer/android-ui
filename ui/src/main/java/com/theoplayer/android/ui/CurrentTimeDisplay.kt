package com.theoplayer.android.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource

/**
 * A text display that shows the player's current time.
 *
 * @param modifier the [Modifier] to be applied to this text
 * @param showRemaining if set, shows the remaining time of the stream as a negative value
 * (e.g. "-01:23")
 * @param showDuration if set, also shows the duration of the stream
 * (e.g. "01:23 / 10:00")
 */
@Composable
fun CurrentTimeDisplay(
    modifier: Modifier = Modifier,
    showRemaining: Boolean = false,
    showDuration: Boolean = false,
) {
    CurrentTimeDisplay(
        state = rememberPlaybackTimeState(LocalTHEOplayerView.current?.player),
        modifier = modifier,
        showRemaining = showRemaining,
        showDuration = showDuration
    )
}

/**
 * A text display that shows the player's current time.
 *
 * @param state the [PlaybackTimeState] to read
 * @param modifier the [Modifier] to be applied to this text
 * @param showRemaining if set, shows the remaining time of the stream as a negative value
 * (e.g. "-01:23")
 * @param showDuration if set, also shows the duration of the stream
 * (e.g. "01:23 / 10:00")
 */
@ExperimentalPlayerStateApi
@Composable
fun CurrentTimeDisplay(
    state: PlaybackTimeState,
    modifier: Modifier = Modifier,
    showRemaining: Boolean = false,
    showDuration: Boolean = false,
) {
    val currentTime = state.currentTime
    val duration = state.duration

    val time = if (showRemaining) {
        -(duration - currentTime)
    } else {
        currentTime
    }

    val text = if (showDuration) {
        stringResource(
            if (showRemaining) {
                R.string.theoplayer_ui_current_time_remaining_with_duration
            } else {
                R.string.theoplayer_ui_current_time_with_duration
            },
            formatTime(time, duration, preferNegative = showRemaining),
            formatTime(duration)
        )
    } else {
        stringResource(
            if (showRemaining) {
                R.string.theoplayer_ui_current_time_remaining
            } else {
                R.string.theoplayer_ui_current_time
            },
            formatTime(time, duration, preferNegative = showRemaining),
        )
    }

    Text(modifier = modifier, text = text)
}