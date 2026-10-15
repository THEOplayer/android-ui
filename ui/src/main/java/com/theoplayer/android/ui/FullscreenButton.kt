package com.theoplayer.android.ui

import com.theoplayer.android.ui.common.R as CommonR
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.FullscreenExit
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

/**
 * A button that toggles fullscreen.
 *
 * @param modifier the [Modifier] to be applied to this button
 * @param contentPadding the spacing values to apply internally between the container and the
 * content
 * @param enter button content when the player is not fullscreen, typically an "enter fullscreen" icon
 * @param exit button content when the player is fullscreen, typically an "exit fullscreen" icon
 */
@Composable
fun FullscreenButton(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    enter: @Composable () -> Unit = {
        Icon(
            Icons.Rounded.Fullscreen,
            contentDescription = stringResource(CommonR.string.theoplayer_ui_btn_fullscreen_enter)
        )
    },
    exit: @Composable () -> Unit = {
        Icon(
            Icons.Rounded.FullscreenExit,
            contentDescription = stringResource(CommonR.string.theoplayer_ui_btn_fullscreen_exit)
        )
    }
) {
    FullscreenButton(
        state = rememberFullscreenState(LocalTHEOplayerView.current),
        modifier = modifier,
        contentPadding = contentPadding,
        enter = enter,
        exit = exit
    )
}

/**
 * A button that toggles fullscreen.
 *
 * @param state the [FullscreenState] to read and control
 * @param modifier the [Modifier] to be applied to this button
 * @param contentPadding the spacing values to apply internally between the container and the
 * content
 * @param enter button content when the player is not fullscreen, typically an "enter fullscreen" icon
 * @param exit button content when the player is fullscreen, typically an "exit fullscreen" icon
 */
@ExperimentalPlayerStateApi
@Composable
fun FullscreenButton(
    state: FullscreenState,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    enter: @Composable () -> Unit = {
        Icon(
            Icons.Rounded.Fullscreen,
            contentDescription = stringResource(CommonR.string.theoplayer_ui_btn_fullscreen_enter)
        )
    },
    exit: @Composable () -> Unit = {
        Icon(
            Icons.Rounded.FullscreenExit,
            contentDescription = stringResource(CommonR.string.theoplayer_ui_btn_fullscreen_exit)
        )
    }
) {
    IconButton(
        modifier = modifier,
        contentPadding = contentPadding,
        onClick = {
            if (state.fullscreen) {
                state.exitFullscreen()
            } else {
                state.enterFullscreen()
            }
        }) {
        if (state.fullscreen) {
            exit()
        } else {
            enter()
        }
    }
}
