package com.theoplayer.android.ui

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun PlayButton(
    modifier: Modifier = Modifier,
    play: @Composable RowScope.() -> Unit = {
        Text(
            modifier = Modifier.alignByBaseline(),
            text = "Play"
        )
    },
    pause: @Composable RowScope.() -> Unit = {
        Text(
            modifier = Modifier.alignByBaseline(),
            text = "Pause"
        )
    },
    replay: @Composable RowScope.() -> Unit = {
        Text(
            modifier = Modifier.alignByBaseline(),
            text = "Replay"
        )
    }
) {
    val state = LocalTHEOplayer.current
    Button(
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