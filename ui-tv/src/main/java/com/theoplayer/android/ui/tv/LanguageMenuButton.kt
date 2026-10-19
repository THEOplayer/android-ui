package com.theoplayer.android.ui.tv

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import com.theoplayer.android.ui.AudioTrackState
import com.theoplayer.android.ui.ExperimentalPlayerStateApi
import com.theoplayer.android.ui.LocalTHEOplayerView
import com.theoplayer.android.ui.MenuScope
import com.theoplayer.android.ui.SubtitleTrackState
import com.theoplayer.android.ui.common.R as CommonR
import com.theoplayer.android.ui.rememberAudioTrackState
import com.theoplayer.android.ui.rememberSubtitleTrackState

/**
 * A button that opens the [language menu][LanguageMenu].
 *
 * @param modifier the [Modifier] to be applied to this button
 * @param contentPadding the spacing values to apply internally between the container and content
 * @param content the button content
 */
@ExperimentalTvUiApi
@Composable
fun MenuScope.LanguageMenuButton(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable () -> Unit = {
        Icon(
            painter = painterResource(id = CommonR.drawable.language),
            contentDescription = stringResource(CommonR.string.theoplayer_ui_menu_language)
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
 * The states are only used to decide whether the button is shown. The opened menu remembers its
 * own states.
 *
 * @param audioTrackState the [AudioTrackState] to read
 * @param subtitleTrackState the [SubtitleTrackState] to read
 * @param modifier the [Modifier] to be applied to this button
 * @param contentPadding the spacing values to apply internally between the container and content
 * @param content the button content
 */
@ExperimentalTvUiApi
@ExperimentalPlayerStateApi
@Composable
fun MenuScope.LanguageMenuButton(
    audioTrackState: AudioTrackState,
    subtitleTrackState: SubtitleTrackState,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable () -> Unit = {
        Icon(
            painter = painterResource(id = CommonR.drawable.language),
            contentDescription = stringResource(CommonR.string.theoplayer_ui_menu_language)
        )
    }
) {
    if (!showLanguageMenuButton(audioTrackState, subtitleTrackState)) {
        return
    }

    IconButton(
        modifier = modifier,
        onClick = { openMenu { LanguageMenu() } }
    ) {
        Box(Modifier.padding(contentPadding)) {
            content()
        }
    }
}

internal fun showLanguageMenuButton(
    audioTrackState: AudioTrackState,
    subtitleTrackState: SubtitleTrackState
): Boolean {
    return showAudioTracks(audioTrackState) || showSubtitleTracks(subtitleTrackState)
}
