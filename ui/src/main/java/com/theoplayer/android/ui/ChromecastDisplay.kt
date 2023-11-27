package com.theoplayer.android.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CastConnected
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.theoplayer.android.api.cast.chromecast.PlayerCastState

/**
 * A display for the state of an active Chromecast session.
 *
 * Depending on the available screen height, this shows either [compact][ChromecastDisplayCompact]
 * or [expanded][ChromecastDisplayExpanded] contents.
 *
 * @param modifier the [Modifier] to be applied to this display
 */
@Composable
fun ChromecastDisplay(
    modifier: Modifier = Modifier
) {
    val player = Player.current
    if (player?.castState != PlayerCastState.CONNECTED) {
        // Hide when not connected to Chromecast
        return
    }
    BoxWithConstraints {
        if (maxHeight < 100.dp) {
            ChromecastDisplayCompact(modifier = modifier)
        } else {
            ChromecastDisplayExpanded(modifier = modifier)
        }
    }
}

/**
 * A compact display for the state of an active Chromecast session.
 *
 * @param modifier the [Modifier] to be applied to this display
 */
@Composable
fun ChromecastDisplayCompact(
    modifier: Modifier = Modifier
) {
    val player = Player.current
    if (player?.castState != PlayerCastState.CONNECTED) {
        // Hide when not connected to Chromecast
        return
    }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Rounded.CastConnected,
            contentDescription = null
        )
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Text(
            text = "Playing on ${player.castReceiverName ?: "Chromecast"}"
        )
    }
}

/**
 * An expanded display for the state of an active Chromecast session.
 *
 * @param modifier the [Modifier] to be applied to this display
 */
@Composable
fun ChromecastDisplayExpanded(
    modifier: Modifier = Modifier
) {
    val player = Player.current
    if (player?.castState != PlayerCastState.CONNECTED) {
        // Hide when not connected to Chromecast
        return
    }
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(ButtonDefaults.IconSpacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Icon(
                Icons.Rounded.CastConnected,
                modifier = Modifier.size(48.dp),
                contentDescription = null
            )
        }
        Column {
            Text(
                text = "Playing on",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = player.castReceiverName ?: "Chromecast",
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}
