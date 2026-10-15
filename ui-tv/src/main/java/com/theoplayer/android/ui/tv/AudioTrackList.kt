package com.theoplayer.android.ui.tv

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.tv.material3.ListItem
import androidx.tv.material3.RadioButton
import androidx.tv.material3.Text
import com.theoplayer.android.ui.AudioTrackState
import com.theoplayer.android.ui.ExperimentalPlayerStateApi
import com.theoplayer.android.ui.LocalTHEOplayerView
import com.theoplayer.android.ui.common.R as CommonR
import com.theoplayer.android.ui.formatTrackName
import com.theoplayer.android.ui.rememberAudioTrackState

/**
 * A list of audio tracks, from which the user can choose an active audio track.
 *
 * @param modifier the [Modifier] to be applied to this list
 * @param onClick called when an audio track in the list is clicked
 */
@ExperimentalTvUiApi
@Composable
fun AudioTrackList(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    AudioTrackList(
        state = rememberAudioTrackState(LocalTHEOplayerView.current?.player),
        modifier = modifier,
        onClick = onClick
    )
}

/**
 * A list of audio tracks, from which the user can choose an active audio track.
 *
 * @param state the [AudioTrackState] to read and control
 * @param modifier the [Modifier] to be applied to this list
 * @param onClick called when an audio track in the list is clicked
 */
@ExperimentalTvUiApi
@ExperimentalPlayerStateApi
@Composable
fun AudioTrackList(
    state: AudioTrackState,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val selectedIndex = state.audioTracks.indexOf(state.activeAudioTrack).coerceAtLeast(0)
    val selectedRequester = remember { FocusRequester() }

    LazyColumn(
        modifier = modifier.focusRestorer(selectedRequester),
        state = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex)
    ) {
        items(
            count = state.audioTracks.size,
            key = { state.audioTracks[it].uid }
        ) { index ->
            val audioTrack = state.audioTracks[index]
            val isActive = state.activeAudioTrack == audioTrack
            ListItem(
                selected = isActive,
                onClick = {
                    state.select(audioTrack)
                    onClick?.invoke()
                },
                headlineContent = { Text(text = formatTrackName(audioTrack)) },
                leadingContent = { RadioButton(selected = isActive, onClick = null) },
                modifier = if (index == selectedIndex) {
                    Modifier.focusRequester(selectedRequester)
                } else {
                    Modifier
                }
            )
        }
    }
}
