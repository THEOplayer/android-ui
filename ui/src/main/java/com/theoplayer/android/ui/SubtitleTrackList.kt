package com.theoplayer.android.ui

import com.theoplayer.android.ui.common.R as CommonR
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role

/**
 * A list of subtitle tracks, from which the user can choose an active subtitle track.
 *
 * @param modifier the [Modifier] to be applied to this menu
 * @param onClick called when a subtitle track in the list is clicked
 */
@Composable
fun SubtitleTrackList(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    SubtitleTrackList(
        state = rememberSubtitleTrackState(LocalTHEOplayerView.current?.player),
        modifier = modifier,
        onClick = onClick
    )
}

/**
 * A list of subtitle tracks, from which the user can choose an active subtitle track.
 *
 * @param state the [SubtitleTrackState] to read and control
 * @param modifier the [Modifier] to be applied to this menu
 * @param onClick called when a subtitle track in the list is clicked
 */
@ExperimentalPlayerStateApi
@Composable
fun SubtitleTrackList(
    state: SubtitleTrackState,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val selectedIndex = state.activeSubtitleTrack
        ?.let { state.subtitleTracks.indexOf(it) + 1 }
        ?.coerceAtLeast(0)
        ?: 0
    // Scroll the selected item to the top, but keep half of the item above
    // it visible to hint the list is scrolled.
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = (selectedIndex - 1).coerceAtLeast(0),
        initialFirstVisibleItemScrollOffset = if (selectedIndex > 0) {
            with(LocalDensity.current) { menuListPeekOffset.toPx() }.toInt()
        } else {
            0
        }
    )
    LazyColumn(
        modifier = modifier.selectableGroup().fadingEdges(listState),
        state = listState
    ) {
        item(key = null) {
            ListItem(
                headlineContent = { Text(text = stringResource(CommonR.string.theoplayer_ui_subtitles_off)) },
                leadingContent = {
                    RadioButton(
                        selected = (state.activeSubtitleTrack == null),
                        onClick = null
                    )
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                modifier = Modifier.selectable(
                    selected = state.activeSubtitleTrack == null,
                    onClick = {
                        state.select(null)
                        onClick?.let { it() }
                    },
                    role = Role.RadioButton
                )
            )
        }
        items(
            count = state.subtitleTracks.size,
            key = { state.subtitleTracks[it].uid }
        ) {
            val subtitleTrack = state.subtitleTracks[it]
            ListItem(
                headlineContent = { Text(text = formatTrackLabel(subtitleTrack)) },
                leadingContent = {
                    RadioButton(
                        selected = (state.activeSubtitleTrack == subtitleTrack),
                        onClick = null
                    )
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                modifier = Modifier.selectable(
                    selected = state.activeSubtitleTrack == subtitleTrack,
                    onClick = {
                        state.select(subtitleTrack)
                        onClick?.let { it() }
                    },
                    role = Role.RadioButton
                )
            )
        }
    }
}
