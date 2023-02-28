package com.theoplayer.android.ui

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.Color
import com.theoplayer.android.api.error.THEOplayerException
import com.theoplayer.android.api.event.EventListener
import com.theoplayer.android.api.event.player.ErrorEvent
import com.theoplayer.android.api.event.player.PlayerEventTypes
import com.theoplayer.android.api.event.player.SourceChangeEvent

@Composable
fun ErrorDisplay() {
    val player = LocalTHEOplayer.current
    val error by produceState<THEOplayerException?>(initialValue = null, player) {
        val sourceChangeListener = EventListener<SourceChangeEvent> { value = null }
        val errorListener = EventListener<ErrorEvent> { event -> value = event.errorObject }
        player?.addEventListener(PlayerEventTypes.SOURCECHANGE, sourceChangeListener)
        player?.addEventListener(PlayerEventTypes.ERROR, errorListener)
        awaitDispose {
            player?.removeEventListener(PlayerEventTypes.SOURCECHANGE, sourceChangeListener)
            player?.removeEventListener(PlayerEventTypes.ERROR, errorListener)
        }
    }

    error?.let {
        Text(
            color = Color.Red,
            text = "${it.message}"
        )
    }
}
