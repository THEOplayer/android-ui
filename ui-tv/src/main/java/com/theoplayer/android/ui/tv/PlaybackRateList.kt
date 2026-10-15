package com.theoplayer.android.ui.tv

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.focusRequester
import androidx.tv.material3.Icon
import androidx.tv.material3.ListItem
import androidx.tv.material3.Text
import com.theoplayer.android.ui.ExperimentalPlayerStateApi
import com.theoplayer.android.ui.LocalTHEOplayerView
import com.theoplayer.android.ui.PlaybackRateState
import com.theoplayer.android.ui.defaultPlaybackRates
import com.theoplayer.android.ui.formatPlaybackRate
import com.theoplayer.android.ui.rememberPlaybackRateState

/**
 * A list of playback rates, from which the user can choose a desired playback rate.
 *
 * @param modifier the [Modifier] to be applied to this list
 * @param playbackRates the list of possible playback rates
 * @param onClick called when a playback rate in the list is clicked
 */
@ExperimentalTvUiApi
@Composable
fun PlaybackRateList(
    modifier: Modifier = Modifier,
    playbackRates: List<Double> = defaultPlaybackRates,
    onClick: (() -> Unit)? = null
) {
    PlaybackRateList(
        state = rememberPlaybackRateState(LocalTHEOplayerView.current?.player),
        modifier = modifier,
        playbackRates = playbackRates,
        onClick = onClick
    )
}

/**
 * A list of playback rates, from which the user can choose a desired playback rate.
 *
 * @param state the [PlaybackRateState] to read and control
 * @param modifier the [Modifier] to be applied to this list
 * @param playbackRates the list of possible playback rates
 * @param onClick called when a playback rate in the list is clicked
 */
@ExperimentalTvUiApi
@ExperimentalPlayerStateApi
@Composable
fun PlaybackRateList(
    state: PlaybackRateState,
    modifier: Modifier = Modifier,
    playbackRates: List<Double> = defaultPlaybackRates,
    onClick: (() -> Unit)? = null
) {
    val selectedIndex = playbackRates.indexOf(state.playbackRate).coerceAtLeast(0)
    val selectedRequester = remember { FocusRequester() }

    LazyColumn(
        modifier = modifier.focusRestorer(selectedRequester),
        state = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex),
        verticalArrangement = Arrangement.spacedBy(menuItemVerticalSpacing)
    ) {
        items(
            items = playbackRates,
            key = { it }
        ) { playbackRate ->
            val isActive = state.playbackRate == playbackRate
            ListItem(
                selected = isActive,
                onClick = {
                    state.select(playbackRate)
                    onClick?.invoke()
                },
                colors = menuItemColors(),
                headlineContent = { Text(text = formatPlaybackRate(playbackRate)) },
                trailingContent = if (isActive) {
                    { Icon(Icons.Rounded.Check, contentDescription = null) }
                } else {
                    null
                },
                modifier = if (playbackRates.indexOf(playbackRate) == selectedIndex) {
                    Modifier.focusRequester(selectedRequester)
                } else {
                    Modifier
                }
            )
        }
    }
}
