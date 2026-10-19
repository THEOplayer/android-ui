package com.theoplayer.android.ui

import android.view.KeyEvent as NativeKeyEvent
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import com.theoplayer.android.api.player.Player

/** Handles keyboard and remote keys for a player UI. */
@InternalUiApi
fun Modifier.handlePlayerKeys(
    player: Player?,
    controlsVisible: () -> Boolean,
    showControls: () -> Unit,
    seekOffset: Int = 10
): Modifier = onPreviewKeyEvent { event ->
    if (event.type != KeyEventType.KeyDown) {
        return@onPreviewKeyEvent false
    }

    val nativeKeyEvent = event.nativeKeyEvent
    when {
        nativeKeyEvent.keyCode.isMediaKey() -> {
            showControls()
            handleMediaKey(player, nativeKeyEvent.keyCode, seekOffset)
            true
        }
        nativeKeyEvent.isSystem -> false
        else -> {
            val wasVisible = controlsVisible()
            showControls()
            !wasVisible
        }
    }
}

/** Checks whether a key code represents a media transport key. */
private fun Int.isMediaKey(): Boolean = when (this) {
    NativeKeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
    NativeKeyEvent.KEYCODE_MEDIA_PLAY,
    NativeKeyEvent.KEYCODE_MEDIA_PAUSE,
    NativeKeyEvent.KEYCODE_MEDIA_FAST_FORWARD,
    NativeKeyEvent.KEYCODE_MEDIA_REWIND -> true
    else -> false
}

/** Performs the action for a media transport key. */
private fun handleMediaKey(player: Player?, keyCode: Int, seekOffset: Int) {
    when (keyCode) {
        NativeKeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> togglePlayPause(player)
        NativeKeyEvent.KEYCODE_MEDIA_PLAY -> player?.play()
        NativeKeyEvent.KEYCODE_MEDIA_PAUSE -> player?.pause()
        NativeKeyEvent.KEYCODE_MEDIA_FAST_FORWARD -> seekBy(player, seekOffset)
        NativeKeyEvent.KEYCODE_MEDIA_REWIND -> seekBy(player, -seekOffset)
    }
}

/** Toggles between play and pause. */
private fun togglePlayPause(player: Player?) {
    if (player == null) return
    if (player.isPaused) {
        player.play()
    } else {
        player.pause()
    }
}

/** Seeks by [seekOffset], clamped to the media duration. */
private fun seekBy(player: Player?, seekOffset: Int) {
    if (player == null || player.ads.isPlaying) return
    val duration = player.duration
    if (!duration.isNaN()) {
        player.currentTime = (player.currentTime + seekOffset).coerceIn(0.0, duration)
    }
}
