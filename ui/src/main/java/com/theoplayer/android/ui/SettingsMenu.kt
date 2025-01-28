package com.theoplayer.android.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.theoplayer.android.api.player.track.mediatrack.quality.VideoQuality

/**
 * A [Menu] to change the settings of the player, such as the video quality or the playback rate.
 *
 * @see QualityMenu
 * @see PlaybackRateMenu
 */
@Composable
fun MenuScope.SettingsMenu() {
    Menu(
        title = { Text(text = stringResource(R.string.theoplayer_ui_menu_settings)) },
        backIcon = {
            Icon(
                Icons.Rounded.Close,
                contentDescription = stringResource(R.string.theoplayer_ui_btn_menu_close)
            )
        },
    ) {
        val player = Player.current
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row {
                Text(
                    modifier = Modifier
                        .weight(1f)
                        .alignByBaseline(),
                    text = stringResource(R.string.theoplayer_ui_menu_quality)
                )
                TextButton(
                    modifier = Modifier
                        .weight(1f)
                        .alignByBaseline(),
                    onClick = { openMenu { QualityMenu() } }
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = formatActiveQualityLabel(
                            player?.targetVideoQuality,
                            player?.activeVideoQuality
                        ),
                        textAlign = TextAlign.Center
                    )
                    Icon(
                        Icons.Rounded.ChevronRight,
                        contentDescription = null
                    )
                }
            }
            Row {
                Text(
                    modifier = Modifier
                        .weight(1f)
                        .alignByBaseline(),
                    text = stringResource(R.string.theoplayer_ui_menu_playback_rate)
                )
                TextButton(
                    modifier = Modifier
                        .weight(1f)
                        .alignByBaseline(),
                    onClick = { openMenu { PlaybackRateMenu() } }
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = formatPlaybackRate(player?.playbackRate ?: 1.0),
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
 * A [Menu] to change the video quality of the stream.
 *
 * @see QualityList
 */
@Composable
fun MenuScope.QualityMenu() {
    Menu(
        title = { Text(text = stringResource(R.string.theoplayer_ui_menu_quality)) }
    ) {
        QualityList(onClick = { closeCurrentMenu() })
    }
}

/**
 * A [Menu] to change the playback rate of the player.
 *
 * @see PlaybackRateList
 */
@Composable
fun MenuScope.PlaybackRateMenu() {
    Menu(
        title = { Text(text = stringResource(R.string.theoplayer_ui_menu_playback_rate)) }
    ) {
        PlaybackRateList(onClick = { closeCurrentMenu() })
    }
}

@Composable
internal fun formatActiveQualityLabel(
    targetVideoQuality: VideoQuality?,
    activeVideoQuality: VideoQuality?
): String {
    return if (targetVideoQuality != null) {
        stringResource(
            R.string.theoplayer_ui_quality_with_height,
            targetVideoQuality.height
        )
    } else if (activeVideoQuality != null) {
        stringResource(
            R.string.theoplayer_ui_quality_automatic_with_height,
            activeVideoQuality.height
        )
    } else {
        stringResource(R.string.theoplayer_ui_quality_automatic)
    }
}