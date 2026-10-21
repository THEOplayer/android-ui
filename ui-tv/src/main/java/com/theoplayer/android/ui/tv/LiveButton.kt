package com.theoplayer.android.ui.tv

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Circle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonColors
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import com.theoplayer.android.ui.ExperimentalPlayerStateApi
import com.theoplayer.android.ui.InternalUiApi
import com.theoplayer.android.ui.LiveButtonState
import com.theoplayer.android.ui.LocalTHEOplayerView
import com.theoplayer.android.ui.common.R
import com.theoplayer.android.ui.rememberLiveButtonState
import com.theoplayer.android.ui.theme.LocalPlayerColors

/**
 * A button that shows whether the player is currently playing at the live point, and seeks to the
 * live point when clicked.
 *
 * @param modifier the [Modifier] to be applied to this button
 * @param contentPadding the spacing values to apply internally between the container and the
 *   content
 * @param colors [ButtonColors] that resolve the colors for this button in different states. See
 *   [ButtonDefaults.colors].
 * @param liveThreshold the maximum distance (in seconds) from the live point that the player's
 *   current time can be for it to still be considered "at the live point"
 * @param live button content when the player is playing at the live point, typically a red circle
 *   icon followed by the text "LIVE"
 * @param dvr button content when the player is playing behind the live point, typically a gray
 *   circle icon followed by the text "LIVE"
 */
@ExperimentalTvUiApi
@OptIn(ExperimentalPlayerStateApi::class, InternalUiApi::class)
@Composable
fun LiveButton(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    colors: ButtonColors = ButtonDefaults.colors(),
    liveThreshold: Double = 10.0,
    live: @Composable RowScope.() -> Unit = {
        LiveButtonContent(LocalPlayerColors.current.liveButtonLive)
    },
    dvr: @Composable RowScope.() -> Unit = {
        LiveButtonContent(LocalPlayerColors.current.liveButtonDvr)
    },
) {
    LiveButton(
        state = rememberLiveButtonState(LocalTHEOplayerView.current?.player, liveThreshold),
        modifier = modifier,
        contentPadding = contentPadding,
        colors = colors,
        live = live,
        dvr = dvr,
    )
}

/**
 * A button that shows whether the player is currently playing at the live point, and seeks to the
 * live point when clicked.
 *
 * @param state the [LiveButtonState] to read and control
 * @param modifier the [Modifier] to be applied to this button
 * @param contentPadding the spacing values to apply internally between the container and the
 *   content
 * @param colors [ButtonColors] that resolve the colors for this button in different states. See
 *   [ButtonDefaults.colors].
 * @param live button content when the player is playing at the live point, typically a red circle
 *   icon followed by the text "LIVE"
 * @param dvr button content when the player is playing behind the live point, typically a gray
 *   circle icon followed by the text "LIVE"
 */
@ExperimentalTvUiApi
@ExperimentalPlayerStateApi
@OptIn(InternalUiApi::class)
@Composable
fun LiveButton(
    state: LiveButtonState,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    colors: ButtonColors = ButtonDefaults.colors(),
    live: @Composable RowScope.() -> Unit = {
        LiveButtonContent(LocalPlayerColors.current.liveButtonLive)
    },
    dvr: @Composable RowScope.() -> Unit = {
        LiveButtonContent(LocalPlayerColors.current.liveButtonDvr)
    },
) {
    if (state.visible) {
        Button(
            onClick = state::onClick,
            modifier = modifier,
            contentPadding = contentPadding,
            colors = colors,
        ) {
            if (state.isLive) live() else dvr()
        }
    }
}

@Composable
private fun RowScope.LiveButtonContent(color: Color) {
    Icon(
        Icons.Rounded.Circle,
        modifier = Modifier.size(12.dp).align(Alignment.CenterVertically),
        tint = color,
        contentDescription = null,
    )
    Spacer(Modifier.width(4.dp))
    Text(
        modifier = Modifier.align(Alignment.CenterVertically),
        text = stringResource(R.string.theoplayer_ui_btn_live),
    )
}
