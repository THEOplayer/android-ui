package com.theoplayer.android.ui

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
    LanguageMenu(
        audioTrackState = rememberAudioTrackState(LocalTHEOplayerView.current?.player),
        subtitleTrackState = rememberSubtitleTrackState(LocalTHEOplayerView.current?.player)
    )
}

/**
 * A [Menu] to change the spoken language and subtitles of the stream.
 *
 * Depending on the available screen width, this shows either [compact][LanguageMenuCompact]
 * or [expanded][LanguageMenuExpanded] contents.
 *
 * Remember the states inside the `openMenu { }` block that opens this menu, not where
 * the opening button is, because the button leaves the composition while the menu is shown.
 *
 * @param audioTrackState the [AudioTrackState] to read and control
 * @param subtitleTrackState the [SubtitleTrackState] to read and control
 */
@ExperimentalPlayerStateApi
@Composable
fun MenuScope.LanguageMenu(
    audioTrackState: AudioTrackState,
    subtitleTrackState: SubtitleTrackState
) {
    Menu(
        title = { Text(text = stringResource(R.string.theoplayer_ui_menu_language)) },
        backIcon = {
            Icon(
                Icons.Rounded.Close,
                contentDescription = stringResource(R.string.theoplayer_ui_btn_menu_close)
            )
        },
    ) {
        BoxWithConstraints {
            val neededWidth =
                (if (showAudioTracks(audioTrackState)) 300.dp else 0.dp) +
                        (if (showSubtitleTracks(subtitleTrackState)) 300.dp else 0.dp)
            if (maxWidth < neededWidth) {
                LanguageMenuCompact(audioTrackState, subtitleTrackState)
            } else {
                LanguageMenuExpanded(audioTrackState, subtitleTrackState)
            }
        }
    }
}

internal fun showAudioTracks(audioTrackState: AudioTrackState): Boolean {
    return audioTrackState.audioTracks.size >= 2
}

internal fun showSubtitleTracks(subtitleTrackState: SubtitleTrackState): Boolean {
    return subtitleTrackState.subtitleTracks.isNotEmpty()
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
    LanguageMenuCompact(
        audioTrackState = rememberAudioTrackState(LocalTHEOplayerView.current?.player),
        subtitleTrackState = rememberSubtitleTrackState(LocalTHEOplayerView.current?.player)
    )
}

/**
 * The compact menu contents of the [LanguageMenu].
 *
 * In this form, the currently selected audio and subtitle track are shown inside buttons.
 * Click these buttons opens a separate menu to change the audio track or subtitle track.
 *
 * Remember the states inside the `openMenu { }` block that opens this menu, not where
 * the opening button is, because the button leaves the composition while the menu is shown.
 *
 * @param audioTrackState the [AudioTrackState] to read and control
 * @param subtitleTrackState the [SubtitleTrackState] to read and control
 * @see AudioTrackMenu
 * @see SubtitleMenu
 */
@ExperimentalPlayerStateApi
@Composable
fun MenuScope.LanguageMenuCompact(
    audioTrackState: AudioTrackState,
    subtitleTrackState: SubtitleTrackState
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        if (showAudioTracks(audioTrackState)) {
            Row {
                Text(
                    modifier = Modifier
                        .weight(1f)
                        .alignByBaseline(),
                    text = stringResource(R.string.theoplayer_ui_menu_audio)
                )
                TextButton(
                    modifier = Modifier
                        .weight(1f)
                        .alignByBaseline(),
                    onClick = { openMenu { AudioTrackMenu() } }
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = audioTrackState.activeAudioTrack?.let { formatTrackLabel(it) }
                            ?: stringResource(
                                R.string.theoplayer_ui_audio_none
                            ),
                        textAlign = TextAlign.Center
                    )
                    Icon(
                        Icons.Rounded.ChevronRight,
                        contentDescription = null
                    )
                }
            }
        }
        if (showSubtitleTracks(subtitleTrackState)) {
            Row {
                Text(
                    modifier = Modifier
                        .weight(1f)
                        .alignByBaseline(),
                    text = stringResource(R.string.theoplayer_ui_menu_subtitles)
                )
                TextButton(
                    modifier = Modifier
                        .weight(1f)
                        .alignByBaseline(),
                    onClick = { openMenu { SubtitleMenu() } }
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = subtitleTrackState.activeSubtitleTrack?.let { formatTrackLabel(it) } ?: stringResource(
                            R.string.theoplayer_ui_subtitles_off
                        ),
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
    LanguageMenuExpanded(
        audioTrackState = rememberAudioTrackState(LocalTHEOplayerView.current?.player),
        subtitleTrackState = rememberSubtitleTrackState(LocalTHEOplayerView.current?.player)
    )
}

/**
 * The expanded menu contents of the [LanguageMenu].
 *
 * In this form, the list of audio and subtitle tracks are shown side-by-side.
 *
 * Remember the states inside the `openMenu { }` block that opens this menu, not where
 * the opening button is, because the button leaves the composition while the menu is shown.
 *
 * @param audioTrackState the [AudioTrackState] to read and control
 * @param subtitleTrackState the [SubtitleTrackState] to read and control
 * @see AudioTrackList
 * @see SubtitleTrackList
 */
@ExperimentalPlayerStateApi
@Composable
fun MenuScope.LanguageMenuExpanded(
    audioTrackState: AudioTrackState,
    subtitleTrackState: SubtitleTrackState
) {
    Row(modifier = Modifier.padding(horizontal = 16.dp)) {
        if (showAudioTracks(audioTrackState)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth(1f)
                        .padding(0.dp, 8.dp),
                    text = stringResource(R.string.theoplayer_ui_menu_audio),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium
                )
                AudioTrackList(audioTrackState)
            }
        }
        if (showSubtitleTracks(subtitleTrackState)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth(1f)
                        .padding(0.dp, 8.dp),
                    text = stringResource(R.string.theoplayer_ui_menu_subtitles),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium
                )
                SubtitleTrackList(subtitleTrackState)
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
    AudioTrackMenu(
        state = rememberAudioTrackState(LocalTHEOplayerView.current?.player)
    )
}

/**
 * A [Menu] to change the spoken language of the stream.
 *
 * Remember the state inside the `openMenu { }` block that opens this menu, not where
 * the opening button is, because the button leaves the composition while the menu is shown.
 *
 * @param state the [AudioTrackState] to read and control
 * @see AudioTrackList
 */
@ExperimentalPlayerStateApi
@Composable
fun MenuScope.AudioTrackMenu(state: AudioTrackState) {
    Menu(
        title = { Text(text = stringResource(R.string.theoplayer_ui_menu_audio)) }
    ) {
        AudioTrackList(state, onClick = { closeCurrentMenu() })
    }
}

/**
 * A [Menu] to change the subtitles of the stream.
 *
 * @see SubtitleTrackList
 */
@Composable
fun MenuScope.SubtitleMenu() {
    SubtitleMenu(
        state = rememberSubtitleTrackState(LocalTHEOplayerView.current?.player)
    )
}

/**
 * A [Menu] to change the subtitles of the stream.
 *
 * Remember the state inside the `openMenu { }` block that opens this menu, not where
 * the opening button is, because the button leaves the composition while the menu is shown.
 *
 * @param state the [SubtitleTrackState] to read and control
 * @see SubtitleTrackList
 */
@ExperimentalPlayerStateApi
@Composable
fun MenuScope.SubtitleMenu(state: SubtitleTrackState) {
    Menu(
        title = { Text(text = stringResource(R.string.theoplayer_ui_menu_subtitles)) }
    ) {
        SubtitleTrackList(state, onClick = { closeCurrentMenu() })
    }
}
