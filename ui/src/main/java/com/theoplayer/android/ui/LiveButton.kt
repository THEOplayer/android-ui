package com.theoplayer.android.ui

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun LiveButton(
    modifier: Modifier = Modifier,
    colors: ButtonColors = ButtonDefaults.textButtonColors(),
    liveThreshold: Double = 10.0,
    live: @Composable RowScope.() -> Unit = {
        Icon(
            Icons.Rounded.Circle,
            modifier = Modifier.size(12.dp),
            tint = Color.Red,
            contentDescription = null
        )
        Text(text = " LIVE")
    },
    dvr: @Composable RowScope. () -> Unit = {
        Icon(
            Icons.Rounded.Circle,
            modifier = Modifier.size(12.dp),
            tint = Color.Gray,
            contentDescription = null
        )
        Text(text = " LIVE")
    }
) {
    val state = LocalTHEOplayer.current
    if (state?.streamType == StreamType.Live || state?.streamType == StreamType.Dvr) {
        val isLive =
            !state.paused && ((state.seekable.lastEnd ?: 0.0) - state.currentTime) <= liveThreshold
        TextButton(
            modifier = modifier,
            colors = colors,
            onClick = {
                state.player?.let {
                    it.currentTime = Double.POSITIVE_INFINITY
                    it.play()
                }
            }) {
            if (isLive) {
                live()
            } else {
                dvr()
            }
        }
    }
}
