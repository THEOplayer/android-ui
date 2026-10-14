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
 * Displays the player's current time.
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
 * Displays playback time from [state].
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
