package com.theoplayer.android.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.PictureInPictureAlt
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
            contentDescription = "Enter picture-in-picture"
        )
    },
    exit: @Composable () -> Unit = {
        Icon(
            Icons.Rounded.Fullscreen,
            contentDescription = "Exit picture-in-picture"
        )
    }
) {
    val player = Player.current
    if (player?.pictureInPictureSupported != true) return
    IconButton(
        modifier = modifier,
        contentPadding = contentPadding,
        onClick = {
            player?.let {
                if (it.pictureInPicture) {
                    it.exitPictureInPicture()
                } else {
                    it.enterPictureInPicture(pipType)
                }
            }
        }) {
        if (player?.pictureInPicture == true) {
            exit()
        } else {
            enter()
        }
    }
}
