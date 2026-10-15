package com.theoplayer.android.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.theoplayer.android.api.THEOplayerConfig
import com.theoplayer.android.api.THEOplayerView
import com.theoplayer.android.api.source.SourceDescription
import com.theoplayer.android.ui.theme.THEOplayerTheme

/**
 * A default THEOplayer UI component.
 *
 * This component provides a great player experience out-of-the-box, that works for all types
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
fun DefaultUI(
    modifier: Modifier = Modifier,
    config: THEOplayerConfig,
    source: SourceDescription? = null,
    title: String? = null
) {
    val theoplayerView = rememberTHEOplayerViewInternal(config)
    LaunchedEffect(theoplayerView, source) {
        theoplayerView?.player?.source = source
    }

    DefaultUIImpl(modifier = modifier, theoplayerView = theoplayerView, player = null, title = title)
}

/**
 * A default THEOplayer UI component.
 *
 * This component provides a great player experience out-of-the-box, that works for all types
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
fun DefaultUI(
    modifier: Modifier = Modifier,
    player: Player = rememberPlayer(),
    title: String? = null
) {
    DefaultUIImpl(
        modifier = modifier,
        theoplayerView = player.theoplayerView,
        player = player,
        title = title
    )
}

/**
 * A default THEOplayer UI component.
 *
 * This component provides a great player experience out-of-the-box, that works for all types
 * of streams. It provides all the common playback controls for playing, seeking, changing
 * languages and qualities.
 *
 * No [Player] is created unless [UIControllerScope.player] is accessed, so
 * [Player.current] is `null` inside this UI: use [LocalTHEOplayerView] and the experimental
 * state holders instead.
 *
 * This does not touch the lifecycle of the given view: use [rememberTHEOplayerView] to
 * create a view owned by the composition, or forward [THEOplayerView.onPause],
 * [THEOplayerView.onResume] and [THEOplayerView.onDestroy] yourself.
 *
 * The colors and fonts can be changed by wrapping this inside a [THEOplayerTheme].
 * For more extensive customizations, we recommend defining your own custom UI using
 * a [UIController].
 *
 * @param modifier the [Modifier] to be applied to this UI
 * @param theoplayerView the THEOplayer view to host in this UI
 * @param title the stream's title, shown at the top of the player
 * @see UIController
 */
@OptIn(ExperimentalPlayerStateApi::class)
@Composable
fun DefaultUI(
    modifier: Modifier = Modifier,
    theoplayerView: THEOplayerView,
    title: String? = null
) {
    DefaultUIImpl(
        modifier = modifier,
        theoplayerView = theoplayerView.takeUnless { it.isInEditMode },
        player = null,
        title = title
    )
}

@OptIn(ExperimentalPlayerStateApi::class)
@Composable
private fun DefaultUIImpl(
    modifier: Modifier,
    theoplayerView: THEOplayerView?,
    player: Player?,
    title: String?
) {
    val firstPlayState = rememberFirstPlayState(theoplayerView?.player)
    val adState = rememberAdState(theoplayerView?.player)
    val playbackTimeState = rememberPlaybackTimeState(theoplayerView?.player)
    val fullscreenState = rememberFullscreenState(theoplayerView)

    // Show all controls in previews, which never start playing.
    val firstPlay = firstPlayState.firstPlay || LocalInspectionMode.current
    UIControllerImpl(
        modifier = modifier,
        theoplayerView = theoplayerView,
        player = player,
        color = Color.Black.copy(alpha = 0.5f),
        centerOverlay = {
            if (firstPlayState.firstPlay) {
                LoadingSpinner()
            }
        },
        topChrome = {
            if (firstPlay) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    title?.let {
                        Text(
                            modifier = Modifier.padding(8.dp),
                            text = it
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    if (!adState.playingAd) {
                        LanguageMenuButton()
                        SettingsMenuButton()
                    }
                    ChromecastButton()
                }
            }
        },
        centerChrome = {
            if (firstPlay && !adState.playingAd) {
                SeekButton(seekOffset = -10, iconSize = 48.dp, contentPadding = PaddingValues(8.dp))
            }
            PlayButton(iconModifier = Modifier.size(96.dp), contentPadding = PaddingValues(8.dp))
            if (firstPlay && !adState.playingAd) {
                SeekButton(seekOffset = 10, iconSize = 48.dp, contentPadding = PaddingValues(8.dp))
            }
        },
        bottomChrome = {
            if (firstPlay) {
                ChromecastDisplay(modifier = Modifier.padding(8.dp))
                if (!adState.playingAd && playbackTimeState.streamType != StreamType.Live) {
                    SeekBar()
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MuteButton()
                    if (adState.playingAd) {
                        if (playbackTimeState.streamType != StreamType.Live) {
                            SeekBar()
                        }
                    } else {
                        LiveButton()
                        if (playbackTimeState.streamType != StreamType.Live) {
                            CurrentTimeDisplay(
                                showRemaining = playbackTimeState.streamType == StreamType.Dvr,
                                showDuration = playbackTimeState.streamType == StreamType.Vod
                            )
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    PictureInPictureButton()
                    FullscreenButton()
                }
            }
        },
        errorOverlay = {
            Box(contentAlignment = Alignment.Center) {
                ErrorDisplay()
                // Ensure the user can still exit fullscreen
                if (fullscreenState.fullscreen) {
                    FullscreenButton(fullscreenState, modifier = Modifier.align(Alignment.BottomEnd))
                }
            }
        }
    )
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF000000,
    device = "spec:width=411dp,height=891dp,orientation=landscape"
)
@Composable
private fun DefaultUIPreview() {
    THEOplayerTheme(useDarkTheme = true) {
        DefaultUI(
            theoplayerView = rememberTHEOplayerView(),
            title = "Big Buck Bunny"
        )
    }
}
