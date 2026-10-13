package com.theoplayer.android.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ListItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * A list of audio tracks, from which the user can choose an active audio track.
 *
 * @param modifier the [Modifier] to be applied to this menu
 * @param onClick called when an audio track in the list is clicked
 */
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
 * @param modifier the [Modifier] to be applied to this menu
 * @param onClick called when an audio track in the list is clicked
 */
@ExperimentalPlayerStateApi
@Composable
fun AudioTrackList(
    state: AudioTrackState,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    LazyColumn(modifier = modifier) {
        items(
            count = state.audioTracks.size,
            key = { state.audioTracks[it].uid }
        ) {
            val audioTrack = state.audioTracks[it]
            ListItem(
                headlineContent = { Text(text = formatTrackLabel(audioTrack)) },
                leadingContent = {
                    RadioButton(
                        selected = (state.activeAudioTrack == audioTrack),
                        onClick = null
                    )
                },
                modifier = Modifier.clickable(onClick = {
                    state.select(audioTrack)
                    onClick?.let { it() }
                })
            )
        }
    }
}
