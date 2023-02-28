package com.theoplayer.android.ui

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable

@Composable
fun PlayButton(
    play: @Composable () -> Unit = { Text("Play") },
    pause: @Composable () -> Unit = { Text("Pause") }
) {
    val state = LocalTHEOplayer.current
    Button(onClick = {
        state?.let {
            if (it.paused.value) {
                it.player?.play()
            } else {
                it.player?.pause()
            }
        }
    }) {
        if (state == null || state.paused.value) {
            play()
        } else {
            pause()
        }
    }
}