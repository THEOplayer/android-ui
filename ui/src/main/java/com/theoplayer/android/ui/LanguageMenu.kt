package com.theoplayer.android.ui

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun MenuScope.LanguageMenu() {
    Menu(
        title = { Text(text = "Language") },
        backIcon = {
            Icon(
                Icons.Rounded.Close,
                tint = Color.White,
                contentDescription = "Close"
            )
        },
    ) {
        BoxWithConstraints {
            val state = LocalTHEOplayer.current
            val neededWidth =
                (if (showAudioTracks(state)) 300.dp else 0.dp) +
                        (if (showSubtitleTracks(state)) 300.dp else 0.dp)
            if (maxWidth < neededWidth) {
                LanguageMenuCompact()
            } else {
                LanguageMenuExpanded()
            }
        }
    }
}

private fun showAudioTracks(state: PlayerState?): Boolean {
    return state != null && state.audioTracks.size >= 2
}

private fun showSubtitleTracks(state: PlayerState?): Boolean {
    return state != null && state.subtitleTracks.isNotEmpty()
}

@Composable
fun MenuScope.LanguageMenuCompact() {
    val state = LocalTHEOplayer.current
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        if (showAudioTracks(state)) {
            Row {
                Text(
                    modifier = Modifier
                        .weight(1f)
                        .alignByBaseline(),
                    text = "Audio"
                )
                TextButton(
                    modifier = Modifier
                        .weight(1f)
                        .alignByBaseline(),
                    onClick = { openMenu { AudioTrackMenu() } }
                ) {
                    Text(
                        text = state?.activeAudioTrack?.let { formatTrackLabel(it) } ?: "None"
                    )
                }
            }
        }
        if (showSubtitleTracks(state)) {
            Row {
                Text(
                    modifier = Modifier
                        .weight(1f)
                        .alignByBaseline(),
                    text = "Subtitles"
                )
                TextButton(
                    modifier = Modifier
                        .weight(1f)
                        .alignByBaseline(),
                    onClick = { openMenu { SubtitleMenu() } }
                ) {
                    Text(
                        text = state?.activeSubtitleTrack?.let { formatTrackLabel(it) } ?: "Off"
                    )
                }
            }
        }
    }
}

@Composable
fun MenuScope.LanguageMenuExpanded() {
    val state = LocalTHEOplayer.current
    Row(modifier = Modifier.padding(horizontal = 16.dp)) {
        if (showAudioTracks(state)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Audio",
                    textAlign = TextAlign.Center
                )
                AudioTrackList(modifier = Modifier.weight(1f))
            }
        }
        if (showSubtitleTracks(state)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Subtitles",
                    textAlign = TextAlign.Center
                )
                SubtitleTrackList(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun MenuScope.AudioTrackMenu() {
    Menu(
        title = { Text(text = "Audio") }
    ) {
        AudioTrackList(onClick = { closeCurrentMenu() })
    }
}

@Composable
fun MenuScope.SubtitleMenu() {
    Menu(
        title = { Text(text = "Subtitles") }
    ) {
        SubtitleTrackList(onClick = { closeCurrentMenu() })
    }
}
