package com.theoplayer.android.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ListItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import java.util.*

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
    val player = Player.current
    val subtitleTracks = player?.subtitleTracks ?: listOf()
    val activeSubtitleTrack = player?.activeSubtitleTrack
    LazyColumn(modifier = modifier) {
        item(key = null) {
            ListItem(
                headlineContent = { Text(text = "Off") },
                leadingContent = {
                    RadioButton(
                        selected = (activeSubtitleTrack == null),
                        onClick = null
                    )
                },
                modifier = Modifier.clickable(onClick = {
                    player?.activeSubtitleTrack = null
                    onClick?.let { it() }
                })
            )
        }
        items(
            count = subtitleTracks.size,
            key = { subtitleTracks[it].uid }
        ) {
            val audioTrack = subtitleTracks[it]
            ListItem(
                headlineContent = { Text(text = formatTrackLabel(audioTrack)) },
                leadingContent = {
                    RadioButton(
                        selected = (activeSubtitleTrack == audioTrack),
                        onClick = null
                    )
                },
                modifier = Modifier.clickable(onClick = {
                    player?.activeSubtitleTrack = audioTrack
                    onClick?.let { it() }
                })
            )
        }
    }
}
