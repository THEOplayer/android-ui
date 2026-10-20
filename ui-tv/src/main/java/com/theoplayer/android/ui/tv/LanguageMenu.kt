package com.theoplayer.android.ui.tv

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Audiotrack
import androidx.compose.material.icons.rounded.ClosedCaption
import androidx.compose.material.icons.rounded.FormatSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import com.theoplayer.android.ui.AudioTrackState
import com.theoplayer.android.ui.ExperimentalPlayerStateApi
import com.theoplayer.android.ui.LocalTHEOplayerView
import com.theoplayer.android.ui.MenuScope
import com.theoplayer.android.ui.SubtitleTrackState
import com.theoplayer.android.ui.common.R as CommonR
import com.theoplayer.android.ui.formatTrackName
import com.theoplayer.android.ui.rememberAudioTrackState
import com.theoplayer.android.ui.rememberSubtitleTrackState

/** A menu to change the spoken language and subtitles of the stream. */
@ExperimentalTvUiApi
@Composable
fun MenuScope.LanguageMenu() {
    LanguageMenu(
        audioTrackState = rememberAudioTrackState(LocalTHEOplayerView.current?.player),
        subtitleTrackState = rememberSubtitleTrackState(LocalTHEOplayerView.current?.player),
    )
}

/**
 * A menu to change the spoken language and subtitles of the stream.
 *
 * Remember the states inside the `openMenu` block that opens this menu, see [MenuScope.openMenu].
 *
 * @param audioTrackState the [AudioTrackState] to read
 * @param subtitleTrackState the [SubtitleTrackState] to read
 */
@ExperimentalTvUiApi
@ExperimentalPlayerStateApi
@Composable
fun MenuScope.LanguageMenu(
    audioTrackState: AudioTrackState,
    subtitleTrackState: SubtitleTrackState,
) {
    Menu(title = { Text(text = stringResource(CommonR.string.theoplayer_ui_menu_language)) }) {
        if (showAudioTracks(audioTrackState)) {
            MenuSubmenuItem(
                onClick = { openMenu { AudioTrackMenu() } },
                headlineContent = {
                    Text(text = stringResource(CommonR.string.theoplayer_ui_menu_audio))
                },
                leadingContent = { Icon(Icons.Rounded.Audiotrack, contentDescription = null) },
                supportingContent = {
                    Text(
                        text =
                            audioTrackState.activeAudioTrack?.let { formatTrackName(it) }
                                ?: stringResource(CommonR.string.theoplayer_ui_audio_none)
                    )
                },
            )
        }
        if (showSubtitleTracks(subtitleTrackState)) {
            MenuSubmenuItem(
                onClick = { openMenu { SubtitleMenu() } },
                headlineContent = {
                    Text(text = stringResource(CommonR.string.theoplayer_ui_menu_subtitles))
                },
                leadingContent = { Icon(Icons.Rounded.ClosedCaption, contentDescription = null) },
                supportingContent = {
                    Text(
                        text =
                            subtitleTrackState.activeSubtitleTrack?.let { formatTrackName(it) }
                                ?: stringResource(CommonR.string.theoplayer_ui_subtitles_off)
                    )
                },
            )
            MenuSubmenuItem(
                onClick = { openMenu { TextTrackStyleMenu() } },
                headlineContent = {
                    Text(text = stringResource(CommonR.string.theoplayer_ui_menu_caption_style))
                },
                leadingContent = { Icon(Icons.Rounded.FormatSize, contentDescription = null) },
            )
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
 * A menu to change the spoken language of the stream.
 *
 * @see AudioTrackList
 */
@ExperimentalTvUiApi
@Composable
fun MenuScope.AudioTrackMenu() {
    AudioTrackMenu(state = rememberAudioTrackState(LocalTHEOplayerView.current?.player))
}

/**
 * A menu to change the spoken language of the stream.
 *
 * Remember the state inside the `openMenu` block that opens this menu, see [MenuScope.openMenu].
 *
 * @param state the [AudioTrackState] to read and control
 * @see AudioTrackList
 */
@ExperimentalTvUiApi
@ExperimentalPlayerStateApi
@Composable
fun MenuScope.AudioTrackMenu(state: AudioTrackState) {
    Menu(title = { Text(text = stringResource(CommonR.string.theoplayer_ui_menu_audio)) }) {
        AudioTrackList(state, onClick = { closeCurrentMenu() })
    }
}

/**
 * A menu to change the subtitles of the stream.
 *
 * @see SubtitleTrackList
 */
@ExperimentalTvUiApi
@Composable
fun MenuScope.SubtitleMenu() {
    SubtitleMenu(state = rememberSubtitleTrackState(LocalTHEOplayerView.current?.player))
}

/**
 * A menu to change the subtitles of the stream.
 *
 * Remember the state inside the `openMenu` block that opens this menu, see [MenuScope.openMenu].
 *
 * @param state the [SubtitleTrackState] to read and control
 * @see SubtitleTrackList
 */
@ExperimentalTvUiApi
@ExperimentalPlayerStateApi
@Composable
fun MenuScope.SubtitleMenu(state: SubtitleTrackState) {
    Menu(title = { Text(text = stringResource(CommonR.string.theoplayer_ui_menu_subtitles)) }) {
        SubtitleTrackList(state, onClick = { closeCurrentMenu() })
    }
}
