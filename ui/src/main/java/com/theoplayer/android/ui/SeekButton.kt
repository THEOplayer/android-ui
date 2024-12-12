package com.theoplayer.android.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.absoluteValue

/**
 * A button that seeks forward or backward by a fixed offset.
 *
 * This draws a seek forward or backward icon inside the button, with the seek offset centered
 * within the icon. If you want to use a different icon, use the other overload that accepts
 * custom `content`.
 *
 * @param modifier the [Modifier] to be applied to this button
 * @param seekOffset the offset (in seconds) by which to seek forward (if positive)
 * or backward (if negative)
 * @param iconSize the size of the icon
 * @param contentPadding the spacing values to apply internally between the container and the
 * content
 */
@Composable
fun SeekButton(
    modifier: Modifier = Modifier,
    seekOffset: Int = 10,
    iconSize: Dp = 24.dp,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    SeekButton(
        modifier = modifier,
        seekOffset = seekOffset,
        contentPadding = contentPadding
    ) {
        Box {
            Icon(
                Icons.Rounded.Replay,
                modifier = Modifier
                    .size(iconSize)
                    .scale(scaleX = if (seekOffset >= 0) -1f else 1f, scaleY = 1f),
                contentDescription = if (seekOffset >= 0) {
                    stringResource(R.string.theoplayer_ui_btn_seek_forward, seekOffset)
                } else {
                    stringResource(R.string.theoplayer_ui_btn_seek_backward, seekOffset)
                }
            )
            Text(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = iconSize * 0.1f),
                text = "${seekOffset.absoluteValue}",
                fontSize = 6.sp * (iconSize / 24.dp)
            )
        }
    }
}

/**
 * A button that seeks forward or backward by a fixed offset.
 *
 * @param modifier the [Modifier] to be applied to this button
 * @param seekOffset the offset (in seconds) by which to seek forward (if positive)
 * or backward (if negative)
 * @param contentPadding the spacing values to apply internally between the container and the
 * content
 */
@Composable
fun SeekButton(
    modifier: Modifier = Modifier,
    seekOffset: Int = 10,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable () -> Unit
) {
    val player = Player.current
    IconButton(
        modifier = modifier,
        contentPadding = contentPadding,
        onClick = {
            player?.player?.let {
                if (!it.duration.isNaN()) {
                    it.currentTime = (it.currentTime + seekOffset).coerceIn(0.0, it.duration)
                }
            }
        },
        content = content
    )
}
