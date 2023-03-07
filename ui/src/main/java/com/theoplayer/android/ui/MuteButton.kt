package com.theoplayer.android.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun MuteButton(
    modifier: Modifier = Modifier,
    mute: @Composable () -> Unit = {
        Icon(
            Icons.Rounded.VolumeUp,
            tint = Color.White,
            contentDescription = "Mute"
        )
    },
    unmute: @Composable () -> Unit = {
        Icon(
            Icons.Rounded.VolumeOff,
            tint = Color.White,
            contentDescription = "Unmute"
        )
    }
) {
    val state = LocalTHEOplayer.current
    TextButton(
        modifier = modifier,
        shape = IconButtonDefaults.filledShape,
        onClick = {
            state?.let {
                it.muted = !it.muted
            }
        }) {
        if (state?.muted == true) {
            unmute()
        } else {
            mute()
        }
    }
}
