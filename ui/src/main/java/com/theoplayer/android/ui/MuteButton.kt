package com.theoplayer.android.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Icon
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
    IconButton(
        modifier = modifier,
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
