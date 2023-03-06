package com.theoplayer.android.ui

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun PlayButton(
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier,
    play: @Composable () -> Unit = {
        Icon(
            Icons.Rounded.PlayArrow,
            modifier = iconModifier,
            tint = Color.White,
            contentDescription = "Play"
        )
    },
    pause: @Composable () -> Unit = {
        Icon(
            Icons.Rounded.Pause,
            modifier = iconModifier,
            tint = Color.White,
            contentDescription = "Pause"
        )
    },
    replay: @Composable () -> Unit = {
        Icon(
            Icons.Rounded.Replay,
            modifier = iconModifier,
            tint = Color.White,
            contentDescription = "Replay"
        )
    }
) {
    val state = LocalTHEOplayer.current
    IconButton(
        modifier = modifier,
        onClick = {
            state?.let {
                if (it.paused) {
                    it.player?.play()
                } else {
                    it.player?.pause()
                }
            }
        }) {
        if (state == null) {
            play()
        } else if (!state.paused) {
            pause()
        } else if (state.ended) {
            replay()
        } else {
            play()
        }
    }
}
