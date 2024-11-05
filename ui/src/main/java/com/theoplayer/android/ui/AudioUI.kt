package com.theoplayer.android.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.theoplayer.android.api.THEOplayerConfig
import com.theoplayer.android.api.THEOplayerView
import com.theoplayer.android.api.source.SourceDescription
import com.theoplayer.android.ui.theme.THEOplayerTheme

/**
 * An audio-only THEOplayer UI.
 *
 * This component provides a great audio player experience out-of-the-box, that works for all types
 * of streams. It provides all the common playback controls for playing, seeking, changing
 * languages and qualities.
 *
 * The colors and fonts can be changed by wrapping this inside a [THEOplayerTheme].
 * For more extensive customizations, we recommend defining your own custom UI using
 * a [UIController].
 *
 * @param modifier the [Modifier] to be applied to this UI
 * @param config the player configuration to be used when constructing the [THEOplayerView]
 * @param source the source description to load into the player
 * @param title the stream's title, shown at the top of the player
 * @see UIController
 */
@Composable
fun AudioUI(
    modifier: Modifier = Modifier,
    config: THEOplayerConfig,
    source: SourceDescription? = null,
    title: String? = null
) {
    val player = rememberPlayer(config)
    LaunchedEffect(player, source) {
        player.source = source
    }

    AudioUI(modifier = modifier, player = player, title = title)
}

/**
 * A default THEOplayer UI component.
 *
 * This component provides a great audio player experience out-of-the-box, that works for all types
 * of streams. It provides all the common playback controls for playing, seeking, changing
 * languages and qualities.
 *
 * The colors and fonts can be changed by wrapping this inside a [THEOplayerTheme].
 * For more extensive customizations, we recommend defining your own custom UI using
 * a [UIController].
 *
 * @param modifier the [Modifier] to be applied to this UI
 * @param player the player. This should always be created using [rememberPlayer].
 * @param title the stream's title, shown at the top of the player
 * @see UIController
 */
@Composable
fun AudioUI(
    modifier: Modifier = Modifier,
    iconSize: Dp = 32.dp,
    player: Player = rememberPlayer(),
    title: String? = null
) {
    UIController(
        modifier = modifier,
        player = player,
        controlsVisible = true,
        topChrome = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                title?.let {
                    Text(
                        modifier = Modifier.padding(8.dp),
                        text = it
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                LanguageMenuButton(iconModifier = Modifier.size(iconSize))
                ChromecastButton(iconModifier = Modifier.size(iconSize))
            }
        },
        centerChrome = {
            ChromecastDisplay(modifier = Modifier.padding(8.dp))
        },
        bottomChrome = {
            if (player.streamType != StreamType.Live) {
                Box(
                    modifier = Modifier.minimumInteractiveComponentSize(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    SeekBar()
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        // Move slightly up, to reduce space below seek bar
                        .offset(y = (-8).dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CurrentTimeDisplay()
                    Spacer(modifier = Modifier.weight(1f))
                    CurrentTimeDisplay(showRemaining = true)
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MuteButton(
                    iconModifier = Modifier.size(iconSize),
                    contentPadding = PaddingValues(8.dp)
                )
                SeekButton(
                    seekOffset = -10,
                    iconSize = iconSize,
                    contentPadding = PaddingValues(8.dp)
                )
                Box(contentAlignment = Alignment.Center) {
                    if (player.firstPlay) {
                        LoadingSpinner()
                    }
                    PlayButton(
                        iconModifier = Modifier.size(iconSize),
                        contentPadding = PaddingValues(8.dp)
                    )
                }
                SeekButton(
                    seekOffset = 10,
                    iconSize = iconSize,
                    contentPadding = PaddingValues(8.dp)
                )
                PlaybackRateButton(
                    iconModifier = Modifier.size(iconSize),
                    contentPadding = PaddingValues(8.dp)
                )
            }
        },
        errorOverlay = {
            Box(contentAlignment = Alignment.Center) {
                ErrorDisplay()
            }
        }
    )
}

@Composable
internal fun MenuScope.PlaybackRateButton(
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val player = LocalPlayer.current
    IconButton(
        modifier = modifier,
        contentPadding = contentPadding,
        onClick = { openMenu { PlaybackRateMenu() } }) {
        Text(
            modifier = iconModifier,
            text = formatPlaybackRate(player?.playbackRate ?: 1.0, "1x"),
        )
    }
}
