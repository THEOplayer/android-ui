package com.theoplayer.android.ui.tv

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.HighQuality
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import com.theoplayer.android.ui.ExperimentalPlayerStateApi
import com.theoplayer.android.ui.LocalTHEOplayerView
import com.theoplayer.android.ui.MenuScope
import com.theoplayer.android.ui.PlaybackRateState
import com.theoplayer.android.ui.VideoQualityState
import com.theoplayer.android.ui.common.R as CommonR
import com.theoplayer.android.ui.formatActiveQualityLabel
import com.theoplayer.android.ui.formatPlaybackRate
import com.theoplayer.android.ui.rememberPlaybackRateState
import com.theoplayer.android.ui.rememberVideoQualityState

/**
 * A menu to change the settings of the player, such as video quality or playback rate.
 *
 * @see QualityMenu
 * @see PlaybackRateMenu
 */
@ExperimentalTvUiApi
@Composable
fun MenuScope.SettingsMenu() {
    SettingsMenu(
        videoQualityState = rememberVideoQualityState(LocalTHEOplayerView.current?.player),
        playbackRateState = rememberPlaybackRateState(LocalTHEOplayerView.current?.player)
    )
}

/**
 * A menu to change the settings of the player, such as video quality or playback rate.
 *
 * Remember the states inside the `openMenu` block that opens this menu, see [MenuScope.openMenu].
 *
 * @param videoQualityState the [VideoQualityState] to read
 * @param playbackRateState the [PlaybackRateState] to read
 * @see QualityMenu
 * @see PlaybackRateMenu
 */
@ExperimentalTvUiApi
@ExperimentalPlayerStateApi
@Composable
fun MenuScope.SettingsMenu(
    videoQualityState: VideoQualityState,
    playbackRateState: PlaybackRateState
) {
    Menu(
        title = { Text(text = stringResource(CommonR.string.theoplayer_ui_menu_settings)) }
    ) {
        MenuSubmenuItem(
            onClick = { openMenu { QualityMenu() } },
            headlineContent = {
                Text(text = stringResource(CommonR.string.theoplayer_ui_menu_quality))
            },
            leadingContent = {
                Icon(Icons.Rounded.HighQuality, contentDescription = null)
            },
            supportingContent = {
                Text(
                    text = formatActiveQualityLabel(
                        videoQualityState.targetVideoQuality,
                        videoQualityState.activeVideoQuality
                    )
                )
            }
        )
        MenuSubmenuItem(
            onClick = { openMenu { PlaybackRateMenu() } },
            headlineContent = {
                Text(text = stringResource(CommonR.string.theoplayer_ui_menu_playback_rate))
            },
            leadingContent = {
                Icon(Icons.Rounded.Speed, contentDescription = null)
            },
            supportingContent = {
                Text(text = formatPlaybackRate(playbackRateState.playbackRate))
            }
        )
    }
}

/**
 * A menu to change the video quality of the stream.
 *
 * @see QualityList
 */
@ExperimentalTvUiApi
@Composable
fun MenuScope.QualityMenu() {
    QualityMenu(state = rememberVideoQualityState(LocalTHEOplayerView.current?.player))
}

/**
 * A menu to change the video quality of the stream.
 *
 * Remember the state inside the `openMenu` block that opens this menu, see [MenuScope.openMenu].
 *
 * @param state the [VideoQualityState] to read and control
 * @see QualityList
 */
@ExperimentalTvUiApi
@ExperimentalPlayerStateApi
@Composable
fun MenuScope.QualityMenu(state: VideoQualityState) {
    Menu(
        title = { Text(text = stringResource(CommonR.string.theoplayer_ui_menu_quality)) }
    ) {
        QualityList(state, onClick = { closeCurrentMenu() })
    }
}

/**
 * A menu to change the playback rate of the player.
 *
 * @see PlaybackRateList
 */
@ExperimentalTvUiApi
@Composable
fun MenuScope.PlaybackRateMenu() {
    PlaybackRateMenu(state = rememberPlaybackRateState(LocalTHEOplayerView.current?.player))
}

/**
 * A menu to change the playback rate of the player.
 *
 * Remember the state inside the `openMenu` block that opens this menu, see [MenuScope.openMenu].
 *
 * @param state the [PlaybackRateState] to read and control
 * @see PlaybackRateList
 */
@ExperimentalTvUiApi
@ExperimentalPlayerStateApi
@Composable
fun MenuScope.PlaybackRateMenu(state: PlaybackRateState) {
    Menu(
        title = { Text(text = stringResource(CommonR.string.theoplayer_ui_menu_playback_rate)) }
    ) {
        PlaybackRateList(state, onClick = { closeCurrentMenu() })
    }
}
