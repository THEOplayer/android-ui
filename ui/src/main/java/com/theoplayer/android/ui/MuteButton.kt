package com.theoplayer.android.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

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
    val state = LocalTHEOplayer.current
    IconButton(
        modifier = modifier,
        contentPadding = contentPadding,
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
