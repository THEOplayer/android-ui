package com.theoplayer.android.ui

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
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
    play: @Composable () -> Unit = {
        Icon(
            Icons.Rounded.PlayArrow,
            tint = Color.White,
            contentDescription = "Play"
        )
    },
    pause: @Composable () -> Unit = {
        Icon(
            Icons.Rounded.Pause,
            tint = Color.White,
            contentDescription = "Pause"
        )
    },
    replay: @Composable () -> Unit = {
        Icon(
            Icons.Rounded.Replay,
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
                if (it.paused.value) {
                    it.player?.play()
                } else {
                    it.player?.pause()
                }
            }
        }) {
        if (state == null) {
            play()
        } else if (!state.paused.value) {
            pause()
        } else if (state.ended.value) {
            replay()
        } else {
            play()
        }
    }
}
