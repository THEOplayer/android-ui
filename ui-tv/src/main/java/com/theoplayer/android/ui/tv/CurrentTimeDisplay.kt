package com.theoplayer.android.ui.tv

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.tv.material3.Text
import com.theoplayer.android.ui.ExperimentalPlayerStateApi
import com.theoplayer.android.ui.InternalUiApi
import com.theoplayer.android.ui.LocalTHEOplayerView
import com.theoplayer.android.ui.PlaybackTimeState
import com.theoplayer.android.ui.common.R
import com.theoplayer.android.ui.formatPlaybackTime
import com.theoplayer.android.ui.rememberPlaybackTimeState

/**
 * A text display that shows the player's current time.
 *
 * @param modifier the [Modifier] to be applied to this text
 * @param showRemaining if set, shows the remaining time of the stream as a negative value
 * (e.g. "-01:23")
 * @param showDuration if set, also shows the duration of the stream
 * (e.g. "01:23 / 10:00")
 */
@ExperimentalTvUiApi
@OptIn(ExperimentalPlayerStateApi::class, InternalUiApi::class)
@Composable
fun CurrentTimeDisplay(
    modifier: Modifier = Modifier,
    showRemaining: Boolean = false,
    showDuration: Boolean = false
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
@ExperimentalTvUiApi
@ExperimentalPlayerStateApi
@OptIn(InternalUiApi::class)
@Composable
fun CurrentTimeDisplay(
    state: PlaybackTimeState,
    modifier: Modifier = Modifier,
    showRemaining: Boolean = false,
    showDuration: Boolean = false
) {
    val currentTime = state.currentTime
    val duration = state.duration
    val time = if (showRemaining) -(duration - currentTime) else currentTime
    val text = if (showDuration) {
        stringResource(
            if (showRemaining) {
                R.string.theoplayer_ui_current_time_remaining_with_duration
            } else {
                R.string.theoplayer_ui_current_time_with_duration
            },
            formatPlaybackTime(time, duration, preferNegative = showRemaining),
            formatPlaybackTime(duration)
        )
    } else {
        stringResource(
            if (showRemaining) {
                R.string.theoplayer_ui_current_time_remaining
            } else {
                R.string.theoplayer_ui_current_time
            },
            formatPlaybackTime(time, duration, preferNegative = showRemaining)
        )
    }
    Text(modifier = modifier, text = text)
}
