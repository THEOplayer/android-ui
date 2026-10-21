package com.theoplayer.android.ui.tv

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.res.stringResource
import androidx.tv.material3.Icon
import androidx.tv.material3.ListItem
import androidx.tv.material3.Text
import com.theoplayer.android.ui.ExperimentalPlayerStateApi
import com.theoplayer.android.ui.LocalTHEOplayerView
import com.theoplayer.android.ui.SubtitleTrackState
import com.theoplayer.android.ui.common.R as CommonR
import com.theoplayer.android.ui.formatTrackName
import com.theoplayer.android.ui.rememberSubtitleTrackState

/**
 * A list of subtitle tracks, from which the user can choose an active subtitle track.
 *
 * @param modifier the [Modifier] to be applied to this list
 * @param onClick called when a subtitle track in the list is clicked
 */
@ExperimentalTvUiApi
@Composable
fun SubtitleTrackList(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    SubtitleTrackList(
        state = rememberSubtitleTrackState(LocalTHEOplayerView.current?.player),
        modifier = modifier,
        onClick = onClick,
    )
}

/**
 * A list of subtitle tracks, from which the user can choose an active subtitle track.
 *
 * @param state the [SubtitleTrackState] to read and control
 * @param modifier the [Modifier] to be applied to this list
 * @param onClick called when a subtitle track in the list is clicked
 */
@ExperimentalTvUiApi
@ExperimentalPlayerStateApi
@Composable
fun SubtitleTrackList(
    state: SubtitleTrackState,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val selectedIndex = state.activeSubtitleTrack?.let { state.subtitleTracks.indexOf(it) + 1 } ?: 0
    val selectedRequester = remember { FocusRequester() }

    LazyColumn(
        modifier = modifier.focusRestorer(selectedRequester),
        state = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex),
        contentPadding = PaddingValues(vertical = menuItemFocusOverflowPadding),
        verticalArrangement = Arrangement.spacedBy(menuItemVerticalSpacing),
    ) {
        item(key = null) {
            val isActive = state.activeSubtitleTrack == null
            ListItem(
                selected = isActive,
                onClick = {
                    state.select(null)
                    onClick?.invoke()
                },
                colors = menuItemColors(),
                headlineContent = {
                    Text(text = stringResource(CommonR.string.theoplayer_ui_subtitles_off))
                },
                trailingContent =
                    if (isActive) {
                        { Icon(Icons.Rounded.Check, contentDescription = null) }
                    } else {
                        null
                    },
                modifier =
                    if (selectedIndex == 0) {
                        Modifier.focusRequester(selectedRequester)
                    } else {
                        Modifier
                    },
            )
        }
        items(
            count = state.subtitleTracks.size,
            key = { state.subtitleTracks[it].uid },
        ) { index ->
            val subtitleTrack = state.subtitleTracks[index]
            val isActive = state.activeSubtitleTrack == subtitleTrack
            ListItem(
                selected = isActive,
                onClick = {
                    state.select(subtitleTrack)
                    onClick?.invoke()
                },
                colors = menuItemColors(),
                headlineContent = { Text(text = formatTrackName(subtitleTrack)) },
                trailingContent =
                    if (isActive) {
                        { Icon(Icons.Rounded.Check, contentDescription = null) }
                    } else {
                        null
                    },
                modifier =
                    if (index + 1 == selectedIndex) {
                        Modifier.focusRequester(selectedRequester)
                    } else {
                        Modifier
                    },
            )
        }
    }
}
