package com.theoplayer.android.ui.tv

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.theoplayer.android.api.THEOplayerConfig
import com.theoplayer.android.api.THEOplayerView
import com.theoplayer.android.api.source.SourceDescription
import com.theoplayer.android.ui.ExperimentalPlayerStateApi
import com.theoplayer.android.ui.LocalTHEOplayerView
import com.theoplayer.android.ui.StreamType
import com.theoplayer.android.ui.UIControllerScope
import com.theoplayer.android.ui.rememberAdState
import com.theoplayer.android.ui.rememberFirstPlayState
import com.theoplayer.android.ui.rememberPlaybackTimeState
import com.theoplayer.android.ui.rememberTHEOplayerView
import com.theoplayer.android.ui.tv.theme.THEOplayerTvTheme

/**
 * A default THEOplayer UI component for Android TV.
 *
 * This component provides a great player experience out-of-the-box, that works for all types of
 * streams. It provides all the common playback controls for playing, seeking, changing languages
 * and qualities, and can be fully controlled with a D-pad.
 *
 * The colors and fonts can be changed by wrapping this inside a [THEOplayerTvTheme]. For more
 * extensive customizations, we recommend defining your own custom UI using a [UIController].
 *
 * @param modifier the [Modifier] to be applied to this UI
 * @param config the player configuration to be used when constructing the [THEOplayerView]
 * @param source the source description to load into the player
 * @param title the stream's title, shown at the top of the player
 * @see UIController
 */
@ExperimentalTvUiApi
@Composable
fun DefaultUI(
    modifier: Modifier = Modifier,
    config: THEOplayerConfig,
    source: SourceDescription? = null,
    title: String? = null,
) {
    val theoplayerView = rememberTHEOplayerView(config)
    LaunchedEffect(theoplayerView, source) {
        if (!theoplayerView.isInEditMode) {
            theoplayerView.player.source = source
        }
    }

    DefaultUI(modifier = modifier, theoplayerView = theoplayerView, title = title)
}

/**
 * A default THEOplayer UI component for Android TV.
 *
 * This component provides a great player experience out-of-the-box, that works for all types of
 * streams. It provides all the common playback controls for playing, seeking, changing languages
 * and qualities, and can be fully controlled with a D-pad.
 *
 * Use [LocalTHEOplayerView] and the experimental state holders to access the player inside this UI.
 *
 * This does not touch the lifecycle of the given view: use [rememberTHEOplayerView] to create a
 * view owned by the composition, or forward [THEOplayerView.onPause], [THEOplayerView.onResume] and
 * [THEOplayerView.onDestroy] yourself.
 *
 * The colors and fonts can be changed by wrapping this inside a [THEOplayerTvTheme]. For more
 * extensive customizations, we recommend defining your own custom UI using a [UIController].
 *
 * @param modifier the [Modifier] to be applied to this UI
 * @param theoplayerView the THEOplayer view to host in this UI
 * @param title the stream's title, shown at the top of the player
 * @see UIController
 */
@ExperimentalTvUiApi
@OptIn(ExperimentalPlayerStateApi::class)
@Composable
fun DefaultUI(
    modifier: Modifier = Modifier,
    theoplayerView: THEOplayerView,
    title: String? = null,
) {
    val player = theoplayerView.takeUnless { it.isInEditMode }?.player
    val firstPlayState = rememberFirstPlayState(player)
    val adState = rememberAdState(player)
    val playbackTimeState = rememberPlaybackTimeState(player)
    // Show all controls in previews, which never start playing.
    val firstPlay = firstPlayState.firstPlay || LocalInspectionMode.current
    UIController(
        modifier = modifier,
        theoplayerView = theoplayerView,
        color = Color.Black.copy(alpha = 0.5f),
        centerOverlay = {
            if (firstPlayState.firstPlay) {
                LoadingSpinner()
            }
        },
        topChrome = {
            if (firstPlay) {
                title?.let {
                    Text(
                        modifier =
                            Modifier.padding(
                                start = safeAreaHorizontalPadding,
                                top = safeAreaVerticalPadding,
                                end = safeAreaHorizontalPadding,
                            ),
                        text = it,
                        style = MaterialTheme.typography.headlineMedium,
                    )
                }
            }
        },
        centerChrome = {
            if (!firstPlay) {
                PlayButton(
                    modifier = Modifier.size(80.dp),
                    iconModifier = Modifier.size(48.dp),
                )
            }
        },
        bottomChrome = {
            if (firstPlay) {
                BottomControls(
                    playingAd = adState.playingAd,
                    streamType = playbackTimeState.streamType,
                )
            }
        },
        errorOverlay = { Box(contentAlignment = Alignment.Center) { ErrorDisplay() } },
    )
}

/** The time displays, seek bar and buttons at the bottom of [DefaultUI]. */
@ExperimentalTvUiApi
@Composable
private fun UIControllerScope.BottomControls(playingAd: Boolean, streamType: StreamType) {
    Column(
        modifier =
            Modifier.fillMaxWidth()
                .padding(
                    start = safeAreaHorizontalPadding,
                    end = safeAreaHorizontalPadding,
                    bottom = safeAreaVerticalPadding,
                )
    ) {
        if (streamType != StreamType.Live) {
            if (!playingAd) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CurrentTimeDisplay(showRemaining = streamType == StreamType.Dvr)
                    Spacer(modifier = Modifier.weight(1f))
                    if (streamType == StreamType.Vod) {
                        DurationDisplay()
                    }
                }
            }
            SeekBar(modifier = Modifier.fillMaxWidth())
        }
        Spacer(modifier = Modifier.height(8.dp))
        // Entering this row focuses the play button, rather than the first button in the row.
        // This matters when the controls open without a seek bar, e.g. for live streams without
        // DVR.
        val playButtonFocusRequester = remember { FocusRequester() }
        Row(
            modifier =
                Modifier.focusProperties { onEnter = { playButtonFocusRequester.requestFocus() } }
                    .focusGroup(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(modifier = Modifier.weight(1f)) {
                if (!playingAd) {
                    LiveButton()
                }
            }
            PlayButton(
                modifier = Modifier.focusRequester(playButtonFocusRequester).size(56.dp),
                iconModifier = Modifier.size(32.dp),
            )
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            ) {
                if (!playingAd) {
                    LanguageMenuButton()
                    SettingsMenuButton()
                }
            }
        }
    }
}

// Keep controls inside the overscan-safe area of TVs.
private val safeAreaHorizontalPadding = 48.dp
private val safeAreaVerticalPadding = 27.dp

@OptIn(ExperimentalTvUiApi::class)
@Preview(
    showBackground = true,
    backgroundColor = 0xFF000000,
    device = "spec:width=960dp,height=540dp",
)
@Composable
private fun DefaultUIPreview() {
    THEOplayerTvTheme {
        Surface {
            DefaultUI(
                theoplayerView = rememberTHEOplayerView(),
                title = "Big Buck Bunny",
            )
        }
    }
}
