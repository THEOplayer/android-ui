package com.theoplayer.android.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun PlayButton(
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    play: @Composable () -> Unit = {
        Icon(
            painter = painterResource(id = R.drawable.play),
            modifier = iconModifier,
            contentDescription = "Play"
        )
    },
    pause: @Composable () -> Unit = {
        Icon(
            painter = painterResource(id = R.drawable.pause),
            modifier = iconModifier,
            contentDescription = "Pause"
        )
    },
    replay: @Composable () -> Unit = {
        Icon(
            Icons.Rounded.Replay,
            modifier = iconModifier,
            contentDescription = "Replay"
        )
    }
) {
    val state = LocalTHEOplayer.current
    IconButton(
        modifier = modifier,
        contentPadding = contentPadding,
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
