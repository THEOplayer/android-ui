package com.theoplayer.android.ui.tv

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Error
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.theoplayer.android.api.error.THEOplayerException
import com.theoplayer.android.ui.ErrorState
import com.theoplayer.android.ui.ExperimentalPlayerStateApi
import com.theoplayer.android.ui.InternalUiApi
import com.theoplayer.android.ui.LocalTHEOplayerView
import com.theoplayer.android.ui.LocalUIControllerErrorState
import com.theoplayer.android.ui.Player
import com.theoplayer.android.ui.common.R
import com.theoplayer.android.ui.rememberErrorState

/**
 * Displays a fatal player error, if one occurs.
 */
@ExperimentalTvUiApi
@OptIn(ExperimentalPlayerStateApi::class, InternalUiApi::class)
@Composable
fun ErrorDisplay(modifier: Modifier = Modifier) {
    ErrorDisplay(
        state = LocalUIControllerErrorState.current
            ?: rememberErrorState(LocalTHEOplayerView.current?.player, Player.current?.error),
        modifier = modifier
    )
}

/**
 * Displays the error held by [state].
 */
@ExperimentalTvUiApi
@ExperimentalPlayerStateApi
@Composable
fun ErrorDisplay(state: ErrorState, modifier: Modifier = Modifier) {
    state.error?.let { ErrorDisplayContent(it, modifier) }
}

@Composable
private fun ErrorDisplayContent(error: THEOplayerException, modifier: Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(
            Icons.Rounded.Error,
            modifier = Modifier.size(48.dp),
            contentDescription = null
        )
        Column {
            Box(
                modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = stringResource(R.string.theoplayer_ui_error_title),
                    style = MaterialTheme.typography.headlineMedium
                )
            }
            Text(text = "${error.message}")
        }
    }
}
