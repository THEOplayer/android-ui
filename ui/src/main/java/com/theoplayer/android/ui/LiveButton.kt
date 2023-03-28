package com.theoplayer.android.ui

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Circle
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.theoplayer.android.ui.theme.THEOplayerTheme

@Composable
fun LiveButton(
    modifier: Modifier = Modifier,
    colors: ButtonColors = defaultIconButtonColors(),
    liveThreshold: Double = 10.0,
    live: @Composable RowScope.() -> Unit = {
        Icon(
            Icons.Rounded.Circle,
            modifier = Modifier.size(12.dp),
            tint = THEOplayerTheme.playerColors.liveButtonLive,
            contentDescription = null
        )
        Text(text = " LIVE")
    },
    dvr: @Composable RowScope. () -> Unit = {
        Icon(
            Icons.Rounded.Circle,
            modifier = Modifier.size(12.dp),
            tint = THEOplayerTheme.playerColors.liveButtonDvr,
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
