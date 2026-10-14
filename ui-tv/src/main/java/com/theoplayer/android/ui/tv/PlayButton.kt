package com.theoplayer.android.ui.tv

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import com.theoplayer.android.ui.ExperimentalPlayerStateApi
import com.theoplayer.android.ui.LocalTHEOplayerView
import com.theoplayer.android.ui.PlayButtonState
import com.theoplayer.android.ui.common.R
import com.theoplayer.android.ui.rememberPlayButtonState

/**
 * A button that toggles whether the player is playing or paused.
 *
 * @param modifier the [Modifier] to be applied to this button
 * @param iconModifier the [Modifier] to be applied to the [Icon]s inside this button
 * @param contentPadding the spacing values to apply internally between the container and the
 * content
 * @param play button content when the player is paused but not ended, typically a "play" icon
 * @param pause button content when the player is playing, typically a "pause" icon
 * @param replay button content when the player is paused and ended, typically a "replay" icon
 */
@ExperimentalTvUiApi
@OptIn(ExperimentalPlayerStateApi::class)
@Composable
fun PlayButton(
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    play: @Composable () -> Unit = {
        Icon(
            painter = painterResource(R.drawable.play),
            modifier = iconModifier,
            contentDescription = stringResource(R.string.theoplayer_ui_btn_play)
        )
    },
    pause: @Composable () -> Unit = {
        Icon(
            painter = painterResource(R.drawable.pause),
            modifier = iconModifier,
            contentDescription = stringResource(R.string.theoplayer_ui_btn_pause)
        )
    },
    replay: @Composable () -> Unit = {
        Icon(
            Icons.Rounded.Replay,
            modifier = iconModifier,
            contentDescription = stringResource(R.string.theoplayer_ui_btn_replay)
        )
    }
) {
    PlayButton(
        state = rememberPlayButtonState(LocalTHEOplayerView.current?.player),
        modifier = modifier,
        iconModifier = iconModifier,
        contentPadding = contentPadding,
        play = play,
        pause = pause,
        replay = replay
    )
}

/**
 * A button that toggles whether the player is playing or paused.
 *
 * @param state the [PlayButtonState] to read and control
 * @param modifier the [Modifier] to be applied to this button
 * @param iconModifier the [Modifier] to be applied to the [Icon]s inside this button
 * @param contentPadding the spacing values to apply internally between the container and the
 * content
 * @param play button content when the player is paused but not ended, typically a "play" icon
 * @param pause button content when the player is playing, typically a "pause" icon
 * @param replay button content when the player is paused and ended, typically a "replay" icon
 */
@ExperimentalTvUiApi
@ExperimentalPlayerStateApi
@Composable
fun PlayButton(
    state: PlayButtonState,
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    play: @Composable () -> Unit = {
        Icon(
            painter = painterResource(R.drawable.play),
            modifier = iconModifier,
            contentDescription = stringResource(R.string.theoplayer_ui_btn_play)
        )
    },
    pause: @Composable () -> Unit = {
        Icon(
            painter = painterResource(R.drawable.pause),
            modifier = iconModifier,
            contentDescription = stringResource(R.string.theoplayer_ui_btn_pause)
        )
    },
    replay: @Composable () -> Unit = {
        Icon(
            Icons.Rounded.Replay,
            modifier = iconModifier,
            contentDescription = stringResource(R.string.theoplayer_ui_btn_replay)
        )
    }
) {
    IconButton(onClick = state::onClick, modifier = modifier) {
        Box(Modifier.padding(contentPadding)) {
            when {
                !state.paused -> pause()
                state.ended -> replay()
                else -> play()
            }
        }
    }
}
