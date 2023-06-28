package com.theoplayer.android.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * A [Menu] to change the spoken language and subtitles of the stream.
 *
 * Depending on the available screen width, this shows either [compact][LanguageMenuCompact]
 * or [expanded][LanguageMenuExpanded] contents.
 */
@Composable
fun MenuScope.LanguageMenu() {
    Menu(
        title = { Text(text = "Language") },
        backIcon = {
            Icon(
                Icons.Rounded.Close,
                contentDescription = "Close"
            )
        },
    ) {
        BoxWithConstraints {
            val player = Player.current
            val neededWidth =
                (if (showAudioTracks(player)) 300.dp else 0.dp) +
                        (if (showSubtitleTracks(player)) 300.dp else 0.dp)
            if (maxWidth < neededWidth) {
                LanguageMenuCompact()
            } else {
                LanguageMenuExpanded()
            }
        }
    }
}

private fun showAudioTracks(player: Player?): Boolean {
    return player != null && player.audioTracks.size >= 2
}

private fun showSubtitleTracks(player: Player?): Boolean {
    return player != null && player.subtitleTracks.isNotEmpty()
}

/**
 * The compact menu contents of the [LanguageMenu].
 *
 * In this form, the currently selected audio and subtitle track are shown inside buttons.
 * Click these buttons opens a separate menu to change the audio track or subtitle track.
 *
 * @see AudioTrackMenu
 * @see SubtitleMenu
 */
@Composable
fun MenuScope.LanguageMenuCompact() {
    val player = Player.current
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        if (showAudioTracks(player)) {
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
                        modifier = Modifier.weight(1f),
                        text = player?.activeAudioTrack?.let { formatTrackLabel(it) } ?: "None",
                        textAlign = TextAlign.Center
                    )
                    Icon(
                        Icons.Rounded.ChevronRight,
                        contentDescription = null
                    )
                }
            }
        }
        if (showSubtitleTracks(player)) {
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
                        modifier = Modifier.weight(1f),
                        text = player?.activeSubtitleTrack?.let { formatTrackLabel(it) } ?: "Off",
                        textAlign = TextAlign.Center
                    )
                    Icon(
                        Icons.Rounded.ChevronRight,
                        contentDescription = null
                    )
                }
            }
        }
    }
}

/**
 * The expanded menu contents of the [LanguageMenu].
 *
 * In this form, the list of audio and subtitle tracks are shown side-by-side.
 *
 * @see AudioTrackList
 * @see SubtitleTrackList
 */
@Composable
fun MenuScope.LanguageMenuExpanded() {
    val player = Player.current
    Row(modifier = Modifier.padding(horizontal = 16.dp)) {
        if (showAudioTracks(player)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth(1f)
                        .padding(0.dp, 8.dp),
                    text = "Audio",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium
                )
                AudioTrackList()
            }
        }
        if (showSubtitleTracks(player)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth(1f)
                        .padding(0.dp, 8.dp),
                    text = "Subtitles",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium
                )
                SubtitleTrackList()
            }
        }
    }
}

/**
 * A [Menu] to change the spoken language of the stream.
 *
 * @see AudioTrackList
 */
@Composable
fun MenuScope.AudioTrackMenu() {
    Menu(
        title = { Text(text = "Audio") }
    ) {
        AudioTrackList(onClick = { closeCurrentMenu() })
    }
}

/**
 * A [Menu] to change the subtitles of the stream.
 *
 * @see SubtitleTrackList
 */
@Composable
fun MenuScope.SubtitleMenu() {
    Menu(
        title = { Text(text = "Subtitles") }
    ) {
        SubtitleTrackList(onClick = { closeCurrentMenu() })
    }
}
