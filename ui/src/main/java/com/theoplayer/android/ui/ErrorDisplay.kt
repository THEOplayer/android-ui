package com.theoplayer.android.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.theoplayer.android.api.error.THEOplayerException
import com.theoplayer.android.ui.common.R as CommonR

/**
 * A display for a fatal error, if the player encounters one.
 *
 * @param modifier the [Modifier] to be applied to this display
 */
@Composable
fun ErrorDisplay(modifier: Modifier = Modifier) {
    ErrorDisplay(
        state =
            LocalUIControllerErrorState.current
                ?: rememberErrorState(LocalTHEOplayerView.current?.player, Player.current?.error),
        modifier = modifier,
    )
}

/**
 * A display for a fatal error, if the player encounters one.
 *
 * The error is only captured from the player's `error` event, so create the [ErrorState] *before*
 * an error can happen: at the top of your UI instead of inside the error overlay that is only shown
 * once there's an error.
 *
 * @param state the [ErrorState] to read
 * @param modifier the [Modifier] to be applied to this display
 */
@ExperimentalPlayerStateApi
@Composable
fun ErrorDisplay(
    state: ErrorState,
    modifier: Modifier = Modifier,
) {
    state.error?.let { ErrorDisplayContent(it, modifier) }
}

@Composable
private fun ErrorDisplayContent(error: THEOplayerException, modifier: Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Column {
            Icon(
                Icons.Rounded.Error,
                modifier = Modifier.size(48.dp),
                contentDescription = null,
            )
        }
        Column {
            Box(
                modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Text(
                    text = stringResource(CommonR.string.theoplayer_ui_error_title),
                    style = MaterialTheme.typography.headlineMedium,
                )
            }
            Text(text = "${error.message}")
        }
    }
}
