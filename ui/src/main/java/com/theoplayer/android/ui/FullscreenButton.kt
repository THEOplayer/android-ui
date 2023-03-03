package com.theoplayer.android.ui

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.FullscreenExit
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun FullscreenButton(
    modifier: Modifier = Modifier,
    enter: @Composable () -> Unit = {
        Icon(
            Icons.Rounded.Fullscreen,
            tint = Color.White,
            contentDescription = "Enter fullscreen"
        )
    },
    exit: @Composable () -> Unit = {
        Icon(
            Icons.Rounded.FullscreenExit,
            tint = Color.White,
            contentDescription = "Exit fullscreen"
        )
    }
) {
    val state = LocalTHEOplayer.current
    IconButton(
        modifier = modifier,
        onClick = {
            state?.let {
                it.fullscreen = !it.fullscreen
            }
        }) {
        if (state?.fullscreen == true) {
            exit()
        } else {
            enter()
        }
    }
}
