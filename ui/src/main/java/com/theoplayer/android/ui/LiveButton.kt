package com.theoplayer.android.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Circle
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.theoplayer.android.ui.theme.THEOplayerTheme

/**
 * A button that shows whether the player is currently playing at the live point,
 * and seeks to the live point when clicked.
 *
 * @param modifier the [Modifier] to be applied to this button
 * @param contentPadding the spacing values to apply internally between the container and the
 * content
 * @param colors [ButtonColors] that will be used to resolve the colors for this button in different
 * states. See [IconButtonDefaults.iconButtonColors]
 * @param liveThreshold the maximum distance (in seconds) from the live point that the player's
 * current time can be for it to still be considered "at the live point"
 * @param live button content when the player is playing at the live point,
 * typically a red circle icon followed by the text "LIVE"
 * @param dvr button content when the player is playing behind the live point,
 * typically a gray circle icon followed by the text "LIVE"
 */
@Composable
fun LiveButton(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = ButtonDefaults.TextButtonContentPadding,
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors(),
    liveThreshold: Double = 10.0,
    live: @Composable RowScope.() -> Unit = {
        Icon(
            Icons.Rounded.Circle,
            modifier = Modifier.size(12.dp),
            tint = THEOplayerTheme.playerColors.liveButtonLive,
            contentDescription = null
        )
        Text(text = " " + stringResource(R.string.theoplayer_ui_btn_live))
    },
    dvr: @Composable RowScope. () -> Unit = {
        Icon(
            Icons.Rounded.Circle,
            modifier = Modifier.size(12.dp),
            tint = THEOplayerTheme.playerColors.liveButtonDvr,
            contentDescription = null
        )
        Text(text = " " + stringResource(R.string.theoplayer_ui_btn_live))
    }
) {
    val player = Player.current
    if (player?.streamType == StreamType.Live || player?.streamType == StreamType.Dvr) {
        val isLive =
            !player.paused && ((player.seekable.lastEnd
                ?: 0.0) - player.currentTime) <= liveThreshold
        TextButton(
            modifier = modifier,
            contentPadding = contentPadding,
            colors = colors.toButtonColors(),
            onClick = {
                player.player?.let {
                    it.currentTime = Double.POSITIVE_INFINITY
                    it.play()
                }
            }) {
            if (isLive) {
                live()
            } else {
                dvr()
            }
        }
    }
}
