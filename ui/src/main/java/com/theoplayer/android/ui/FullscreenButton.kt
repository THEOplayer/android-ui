package com.theoplayer.android.ui

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
            contentDescription = stringResource(R.string.theoplayer_ui_btn_fullscreen_enter)
        )
    },
    exit: @Composable () -> Unit = {
        Icon(
            Icons.Rounded.FullscreenExit,
            contentDescription = stringResource(R.string.theoplayer_ui_btn_fullscreen_exit)
        )
    }
) {
    val player = Player.current
    IconButton(
        modifier = modifier,
        contentPadding = contentPadding,
        onClick = {
            player?.let {
                it.fullscreen = !it.fullscreen
            }
        }) {
        if (player?.fullscreen == true) {
            exit()
        } else {
            enter()
        }
    }
}
