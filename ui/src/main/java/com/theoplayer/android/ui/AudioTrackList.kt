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
 * A list of audio tracks, from which the user can choose an active audio track.
 *
 * @param modifier the [Modifier] to be applied to this menu
 * @param onClick called when an audio track in the list is clicked
 */
@Composable
fun AudioTrackList(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    AudioTrackList(
        state = rememberAudioTrackState(LocalTHEOplayerView.current?.player),
        modifier = modifier,
        onClick = onClick,
    )
}

/**
 * A list of audio tracks, from which the user can choose an active audio track.
 *
 * @param state the [AudioTrackState] to read and control
 * @param modifier the [Modifier] to be applied to this menu
 * @param onClick called when an audio track in the list is clicked
 */
@ExperimentalPlayerStateApi
@Composable
fun AudioTrackList(
    state: AudioTrackState,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val selectedIndex = state.audioTracks.indexOf(state.activeAudioTrack).coerceAtLeast(0)
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
            count = state.audioTracks.size,
            key = { state.audioTracks[it].uid },
        ) {
            val audioTrack = state.audioTracks[it]
            ListItem(
                headlineContent = { Text(text = formatTrackLabel(audioTrack)) },
                leadingContent = {
                    RadioButton(
                        selected = (state.activeAudioTrack == audioTrack),
                        onClick = null,
                    )
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                modifier =
                    Modifier.selectable(
                        selected = state.activeAudioTrack == audioTrack,
                        onClick = {
                            state.select(audioTrack)
                            onClick?.let { it() }
                        },
                        role = Role.RadioButton,
                    ),
            )
        }
    }
}
