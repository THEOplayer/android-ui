package com.theoplayer.android.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.FullscreenExit
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun FullscreenButton(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    enter: @Composable () -> Unit = {
        Icon(
            Icons.Rounded.Fullscreen,
            contentDescription = "Enter fullscreen"
        )
    },
    exit: @Composable () -> Unit = {
        Icon(
            Icons.Rounded.FullscreenExit,
            contentDescription = "Exit fullscreen"
        )
    }
) {
    val state = LocalTHEOplayer.current
    IconButton(
        modifier = modifier,
        contentPadding = contentPadding,
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
