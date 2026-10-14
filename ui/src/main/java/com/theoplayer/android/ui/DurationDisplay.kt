package com.theoplayer.android.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * A text display that shows the player's duration.
 *
 * @param modifier the [Modifier] to be applied to this text
 */
@Composable
fun DurationDisplay(
    modifier: Modifier = Modifier
) {
    DurationDisplay(
        state = rememberPlaybackTimeState(LocalTHEOplayerView.current?.player),
        modifier = modifier
    )
}

/**
 * A text display that shows the player's duration.
 *
 * @param state the [PlaybackTimeState] to read
 * @param modifier the [Modifier] to be applied to this text
 */
@ExperimentalPlayerStateApi
@Composable
fun DurationDisplay(
    state: PlaybackTimeState,
    modifier: Modifier = Modifier
) {
    Text(modifier = modifier, text = formatTime(state.duration))
}