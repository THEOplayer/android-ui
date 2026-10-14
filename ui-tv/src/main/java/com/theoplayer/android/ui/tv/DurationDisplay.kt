package com.theoplayer.android.ui.tv

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.tv.material3.Text
import com.theoplayer.android.ui.ExperimentalPlayerStateApi
import com.theoplayer.android.ui.InternalUiApi
import com.theoplayer.android.ui.LocalTHEOplayerView
import com.theoplayer.android.ui.PlaybackTimeState
import com.theoplayer.android.ui.formatPlaybackTime
import com.theoplayer.android.ui.rememberPlaybackTimeState

/**
 * Displays the player's duration.
 */
@ExperimentalTvUiApi
@OptIn(ExperimentalPlayerStateApi::class, InternalUiApi::class)
@Composable
fun DurationDisplay(modifier: Modifier = Modifier) {
    DurationDisplay(
        state = rememberPlaybackTimeState(LocalTHEOplayerView.current?.player),
        modifier = modifier
    )
}

/**
 * Displays the duration from [state].
 */
@ExperimentalTvUiApi
@ExperimentalPlayerStateApi
@OptIn(InternalUiApi::class)
@Composable
fun DurationDisplay(state: PlaybackTimeState, modifier: Modifier = Modifier) {
    Text(modifier = modifier, text = formatPlaybackTime(state.duration))
}
