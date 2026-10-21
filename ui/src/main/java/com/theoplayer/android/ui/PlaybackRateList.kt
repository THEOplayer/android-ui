package com.theoplayer.android.ui

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role

/**
 * A list of playback rates, from which the user can choose a desired playback rate.
 *
 * @param modifier the [Modifier] to be applied to this menu
 * @param playbackRates the list of possible playback rates
 * @param onClick called when a playback rate in the list is clicked
 */
@OptIn(InternalUiApi::class)
@Composable
fun PlaybackRateList(
    modifier: Modifier = Modifier,
    playbackRates: List<Double> = defaultPlaybackRates,
    onClick: (() -> Unit)? = null,
) {
    PlaybackRateList(
        state = rememberPlaybackRateState(LocalTHEOplayerView.current?.player),
        modifier = modifier,
        playbackRates = playbackRates,
        onClick = onClick,
    )
}

/**
 * A list of playback rates, from which the user can choose a desired playback rate.
 *
 * @param state the [PlaybackRateState] to read and control
 * @param modifier the [Modifier] to be applied to this menu
 * @param playbackRates the list of possible playback rates
 * @param onClick called when a playback rate in the list is clicked
 */
@ExperimentalPlayerStateApi
@OptIn(InternalUiApi::class)
@Composable
fun PlaybackRateList(
    state: PlaybackRateState,
    modifier: Modifier = Modifier,
    playbackRates: List<Double> = defaultPlaybackRates,
    onClick: (() -> Unit)? = null,
) {
    val selectedIndex = playbackRates.indexOf(state.playbackRate).coerceAtLeast(0)
    val listState =
        rememberLazyListState(
            initialFirstVisibleItemIndex = (selectedIndex - 1).coerceAtLeast(0),
            initialFirstVisibleItemScrollOffset =
                if (selectedIndex > 0) {
                    with(LocalDensity.current) { menuListPeekOffset.toPx() }.toInt()
                } else {
                    0
                },
        )
    LazyColumn(
        modifier = modifier.selectableGroup().fadingEdges(listState),
        state = listState,
    ) {
        items(
            count = playbackRates.size,
            key = { playbackRates[it] },
        ) {
            val playbackRate = playbackRates[it]
            ListItem(
                headlineContent = { Text(text = formatPlaybackRate(playbackRate)) },
                leadingContent = {
                    RadioButton(
                        selected = (state.playbackRate == playbackRate),
                        onClick = null,
                    )
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                modifier =
                    Modifier.selectable(
                        selected = state.playbackRate == playbackRate,
                        onClick = {
                            state.select(playbackRate)
                            onClick?.let { it() }
                        },
                        role = Role.RadioButton,
                    ),
            )
        }
    }
}
