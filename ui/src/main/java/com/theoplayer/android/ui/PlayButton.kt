package com.theoplayer.android.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

/**
 * A button that toggles whether the player is playing or paused.
 *
 * @param modifier the [Modifier] to be applied to this button
 * @param iconModifier the [Modifier] to be applied to the [Icon]s inside this button
 * @param contentPadding the spacing values to apply internally between the container and the
 * content
 * @param play button content when the player is paused but not ended, typically a "play" icon
 * @param pause button content when the player is playing, typically a "pause" icon
 * @param replay button content when the player is paused and ended, typically a "replay" icon
 */
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
    val player = Player.current
    IconButton(
        modifier = modifier,
        contentPadding = contentPadding,
        onClick = {
            player?.let {
                if (it.paused) {
                    it.play()
                } else {
                    it.pause()
                }
            }
        }) {
        if (player == null) {
            play()
        } else if (!player.paused) {
            pause()
        } else if (player.ended) {
            replay()
        } else {
            play()
        }
    }
}
