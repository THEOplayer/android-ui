package com.theoplayer.android.ui

import com.theoplayer.android.ui.common.R as CommonR
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.PictureInPictureAlt
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.theoplayer.android.api.pip.PiPType

/**
 * A button that toggles picture-in-picture mode.
 *
 * @param modifier the [Modifier] to be applied to this button
 * @param contentPadding the spacing values to apply internally between the container
 *        and the content
 * @param pipType the type of the picture-in-picture window when entering
 * @param enter button content when the player is not in picture-in-picture mode,
 *        typically an "enter picture-in-picture" icon
 * @param exit button content when the player is in picture-in-picture mode,
 *        typically an "exit picture-in-picture" icon
 */
@Composable
fun PictureInPictureButton(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    pipType: PiPType = PiPType.ACTIVITY,
    enter: @Composable () -> Unit = {
        Icon(
            Icons.Rounded.PictureInPictureAlt,
            contentDescription = stringResource(CommonR.string.theoplayer_ui_pip_enter),
        )
    },
    exit: @Composable () -> Unit = {
        Icon(
            Icons.Rounded.Fullscreen,
            contentDescription = stringResource(CommonR.string.theoplayer_ui_pip_exit)
        )
    }
) {
    PictureInPictureButton(
        state = rememberPictureInPictureState(LocalTHEOplayerView.current),
        modifier = modifier,
        contentPadding = contentPadding,
        pipType = pipType,
        enter = enter,
        exit = exit
    )
}

/**
 * A button that toggles picture-in-picture mode.
 *
 * @param state the [PictureInPictureState] to read and control
 * @param modifier the [Modifier] to be applied to this button
 * @param contentPadding the spacing values to apply internally between the container
 *        and the content
 * @param pipType the type of the picture-in-picture window when entering
 * @param enter button content when the player is not in picture-in-picture mode,
 *        typically an "enter picture-in-picture" icon
 * @param exit button content when the player is in picture-in-picture mode,
 *        typically an "exit picture-in-picture" icon
 */
@ExperimentalPlayerStateApi
@Composable
fun PictureInPictureButton(
    state: PictureInPictureState,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    pipType: PiPType = PiPType.ACTIVITY,
    enter: @Composable () -> Unit = {
        Icon(
            Icons.Rounded.PictureInPictureAlt,
            contentDescription = stringResource(CommonR.string.theoplayer_ui_pip_enter),
        )
    },
    exit: @Composable () -> Unit = {
        Icon(
            Icons.Rounded.Fullscreen,
            contentDescription = stringResource(CommonR.string.theoplayer_ui_pip_exit)
        )
    }
) {
    if (!state.pictureInPictureSupported) {
        // Hide when picture-in-picture is not supported
        return
    }
    IconButton(
        modifier = modifier,
        contentPadding = contentPadding,
        onClick = {
            if (state.pictureInPicture) {
                state.exitPictureInPicture()
            } else {
                state.enterPictureInPicture(pipType)
            }
        }) {
        if (state.pictureInPicture) {
            exit()
        } else {
            enter()
        }
    }
}
