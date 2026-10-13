package com.theoplayer.android.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

/**
 * A button that opens the [language menu][LanguageMenu].
 *
 * @param modifier the [Modifier] to be applied to this button
 * @param contentPadding the spacing values to apply internally between the container and the
 * content
 */
@Composable
fun MenuScope.LanguageMenuButton(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable () -> Unit = {
        Icon(
            painter = painterResource(id = R.drawable.language),
            contentDescription = stringResource(R.string.theoplayer_ui_menu_language)
        )
    }
) {
    LanguageMenuButton(
        audioTrackState = rememberAudioTrackState(LocalTHEOplayerView.current?.player),
        subtitleTrackState = rememberSubtitleTrackState(LocalTHEOplayerView.current?.player),
        modifier = modifier,
        contentPadding = contentPadding,
        content = content
    )
}

/**
 * A button that opens the [language menu][LanguageMenu].
 *
 * The states are only used to decide whether the button is shown. They are *not* passed
 * to the opened menu, because this button leaves the composition while the menu is shown:
 * the menu remembers its own states instead.
 *
 * @param audioTrackState the [AudioTrackState] to read
 * @param subtitleTrackState the [SubtitleTrackState] to read
 * @param modifier the [Modifier] to be applied to this button
 * @param contentPadding the spacing values to apply internally between the container and the
 * content
 */
@ExperimentalPlayerStateApi
@Composable
fun MenuScope.LanguageMenuButton(
    audioTrackState: AudioTrackState,
    subtitleTrackState: SubtitleTrackState,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable () -> Unit = {
        Icon(
            painter = painterResource(id = R.drawable.language),
            contentDescription = stringResource(R.string.theoplayer_ui_menu_language)
        )
    }
) {
    if (!showLanguageMenuButton(audioTrackState, subtitleTrackState)) {
        // Hide when no alternative audio or subtitle tracks are available
        return
    }

    IconButton(
        modifier = modifier,
        contentPadding = contentPadding,
        onClick = { openMenu { LanguageMenu() } }) {
        content()
    }
}

internal fun showLanguageMenuButton(
    audioTrackState: AudioTrackState,
    subtitleTrackState: SubtitleTrackState
): Boolean {
    return showAudioTracks(audioTrackState) || showSubtitleTracks(subtitleTrackState)
}
