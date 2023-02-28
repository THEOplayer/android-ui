package com.theoplayer.android.ui

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import com.theoplayer.android.api.event.EventListener
import com.theoplayer.android.api.event.player.PauseEvent
import com.theoplayer.android.api.event.player.PlayEvent
import com.theoplayer.android.api.event.player.PlayerEventTypes

@Composable
fun PlayButton(
    play: @Composable () -> Unit = { Text("Play") },
    pause: @Composable () -> Unit = { Text("Pause") }
) {
    val player = LocalTHEOplayer.current
    val paused by produceState(initialValue = true, player) {
        val updatePaused = {
            value = player?.isPaused ?: true
        }
        updatePaused()
        val playListener = EventListener<PlayEvent> { updatePaused() }
        val pauseListener = EventListener<PauseEvent> { updatePaused() }
        player?.addEventListener(PlayerEventTypes.PLAY, playListener)
        player?.addEventListener(PlayerEventTypes.PAUSE, pauseListener)
        awaitDispose {
            player?.removeEventListener(PlayerEventTypes.PLAY, playListener)
            player?.removeEventListener(PlayerEventTypes.PAUSE, pauseListener)
        }
    }

    Button(onClick = {
        player?.let {
            if (it.isPaused) {
                it.play()
            } else {
                it.pause()
            }
        }
    }) {
        if (paused) {
            play()
        } else {
            pause()
        }
    }
}