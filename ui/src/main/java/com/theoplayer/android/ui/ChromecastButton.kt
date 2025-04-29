package com.theoplayer.android.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cast
import androidx.compose.material.icons.rounded.CastConnected
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.theoplayer.android.api.cast.chromecast.PlayerCastState


/**
 * A button to start and stop casting using Chromecast.
 *
 * @param modifier the [Modifier] to be applied to this button
 * @param contentPadding the spacing values to apply internally between the container and the
 * content
 * @param availableIcon button content when Chromecast is available but not connected,
 * typically a "start casting" icon
 * @param connectingIcon button content while connecting to Chromecast,
 * typically a "connecting to Chromecast" icon
 * @param connectedIcon button content when connected to Chromecast,
 * typically a "stop casting" icon
 */
@Composable
fun ChromecastButton(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    availableIcon: @Composable () -> Unit = {
        Icon(
            Icons.Rounded.Cast,
            contentDescription = stringResource(R.string.theoplayer_ui_btn_chromecast_start)
        )
    },
    connectingIcon: @Composable () -> Unit = {
        Icon(
            Icons.Rounded.Cast,
            contentDescription = stringResource(R.string.theoplayer_ui_btn_chromecast_stop)
        )
    },
    connectedIcon: @Composable () -> Unit = {
        Icon(
            Icons.Rounded.CastConnected,
            contentDescription = stringResource(R.string.theoplayer_ui_btn_chromecast_stop)
        )
    }
) {
    val player = Player.current
    val castState = player?.castState ?: PlayerCastState.UNAVAILABLE
    if (castState == PlayerCastState.UNAVAILABLE) {
        // Hide when Chromecast is not available
        return
    }

    IconButton(
        modifier = modifier,
        contentPadding = contentPadding,
        onClick = {
            player?.cast?.chromecast?.let {
                if (it.isCasting) {
                    it.stop()
                } else {
                    it.start()
                }
            }
        }) {
        when (player?.castState) {
            PlayerCastState.AVAILABLE -> availableIcon()
            PlayerCastState.CONNECTING -> connectingIcon()
            PlayerCastState.CONNECTED -> connectedIcon()
            else -> {}
        }
    }
}
