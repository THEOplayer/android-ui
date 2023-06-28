package com.theoplayer.android.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A button that toggles whether audio is muted or not.
 *
 * @param modifier the [Modifier] to be applied to this button
 * @param contentPadding the spacing values to apply internally between the container and the
 * content
 * @param mute button content when the player is unmuted, typically a "mute" icon
 * @param unmute button content when the player is muted, typically an "unmute" icon
 */
@Composable
fun MuteButton(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    mute: @Composable () -> Unit = {
        Icon(
            Icons.Rounded.VolumeUp,
            contentDescription = "Mute"
        )
    },
    unmute: @Composable () -> Unit = {
        Icon(
            Icons.Rounded.VolumeOff,
            contentDescription = "Unmute"
        )
    }
) {
    val player = Player.current
    IconButton(
        modifier = modifier,
        contentPadding = contentPadding,
        onClick = {
            player?.let {
                it.muted = !it.muted
            }
        }) {
        if (player?.muted == true) {
            unmute()
        } else {
            mute()
        }
    }
}
