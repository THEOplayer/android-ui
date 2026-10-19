package com.theoplayer.android.ui.demo.tv.nitflex

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Pause
import androidx.compose.material.icons.sharp.PlayArrow
import androidx.compose.material.icons.sharp.Replay
import androidx.compose.material.icons.sharp.SkipNext
import androidx.compose.material.icons.sharp.Speed
import androidx.compose.material.icons.sharp.Subtitles
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.theoplayer.android.api.THEOplayerConfig
import com.theoplayer.android.api.THEOplayerView
import com.theoplayer.android.ui.ExperimentalPlayerStateApi
import com.theoplayer.android.ui.UIControllerScope
import com.theoplayer.android.ui.rememberFirstPlayState
import com.theoplayer.android.ui.rememberTHEOplayerView
import com.theoplayer.android.ui.tv.CurrentTimeDisplay
import com.theoplayer.android.ui.tv.ErrorDisplay
import com.theoplayer.android.ui.tv.ExperimentalTvUiApi
import com.theoplayer.android.ui.tv.LanguageMenu
import com.theoplayer.android.ui.tv.LoadingSpinner
import com.theoplayer.android.ui.tv.PlayButton
import com.theoplayer.android.ui.tv.PlaybackRateMenu
import com.theoplayer.android.ui.tv.SeekBar
import com.theoplayer.android.ui.tv.SeekButton
import com.theoplayer.android.ui.tv.UIController

/**
 * A Netflix-inspired player UI for Android TV, ported from the mobile `NitflexUI`.
 *
 * @param modifier the [Modifier] to be applied to this UI
 * @param theoplayerView the THEOplayer view to host in this UI
 * @param title the stream's title, shown at the top of the player
 * @param onNextEpisode called when the "Next episode" button is clicked,
 * or `null` to hide that button
 */
@OptIn(ExperimentalTvUiApi::class, ExperimentalPlayerStateApi::class)
@Composable
fun TvNitflexUI(
    modifier: Modifier = Modifier,
    theoplayerView: THEOplayerView,
    title: String? = null,
    onNextEpisode: (() -> Unit)? = null
) {
    val view = theoplayerView.takeUnless { it.isInEditMode }
    val firstPlayState = rememberFirstPlayState(view?.player)
    // Show all controls in previews, which never start playing.
    val firstPlay = firstPlayState.firstPlay || LocalInspectionMode.current
    UIController(
        modifier = modifier,
        theoplayerView = theoplayerView,
        centerOverlay = {
            if (firstPlayState.firstPlay) {
                LoadingSpinner()
            }
        },
        topChrome = {
            if (firstPlay) {
                title?.let { NitflexTitle(it) }
            }
        },
        centerChrome = {
            NitflexCenterControls(firstPlay = firstPlay)
        },
        bottomChrome = {
            if (firstPlay) {
                NitflexBottomControls(onNextEpisode = onNextEpisode)
            }
        },
        errorOverlay = {
            Box(contentAlignment = Alignment.Center) {
                ErrorDisplay()
            }
        }
    )
}

/**
 * The stream title, centered at the top.
 */
@Composable
private fun NitflexTitle(title: String) {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = safeAreaVerticalPadding),
        text = title,
        color = Color.White,
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        textAlign = TextAlign.Center
    )
}

/**
 * The play button, with seek buttons on either side once playback has started.
 */
@OptIn(ExperimentalTvUiApi::class)
@Composable
private fun NitflexCenterControls(firstPlay: Boolean) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (firstPlay) {
            SeekButton(seekOffset = -10, iconSize = 48.dp, modifier = Modifier.size(72.dp))
        }
        PlayButton(
            modifier = Modifier.size(80.dp),
            iconModifier = Modifier.size(48.dp),
            play = { Icon(Icons.Sharp.PlayArrow, contentDescription = "Play") },
            pause = { Icon(Icons.Sharp.Pause, contentDescription = "Pause") },
            replay = { Icon(Icons.Sharp.Replay, contentDescription = "Replay") }
        )
        if (firstPlay) {
            SeekButton(seekOffset = 10, iconSize = 48.dp, modifier = Modifier.size(72.dp))
        }
    }
}

/**
 * The seek bar with the remaining time, and a row of buttons below it.
 */
@OptIn(ExperimentalTvUiApi::class)
@Composable
private fun UIControllerScope.NitflexBottomControls(onNextEpisode: (() -> Unit)?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = safeAreaHorizontalPadding,
                end = safeAreaHorizontalPadding,
                bottom = safeAreaVerticalPadding
            )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SeekBar(modifier = Modifier.weight(1f))
            Spacer(Modifier.size(16.dp))
            CurrentTimeDisplay(showRemaining = true)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            NitflexTextButton(Icons.Sharp.Speed, "Speed") { openMenu { PlaybackRateMenu() } }
            NitflexTextButton(Icons.Sharp.Subtitles, "Audio and subtitles") { openMenu { LanguageMenu() } }
            onNextEpisode?.let { NitflexTextButton(Icons.Sharp.SkipNext, "Next episode", it) }
        }
    }
}

/**
 * A button with a leading icon and a label.
 */
@Composable
private fun NitflexTextButton(icon: ImageVector, text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.colors(containerColor = Color.Transparent, contentColor = Color.White)
    ) {
        Icon(icon, contentDescription = null)
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Text(text = text)
    }
}

// Keep content inside the overscan-safe area of TVs.
private val safeAreaHorizontalPadding = 48.dp
private val safeAreaVerticalPadding = 27.dp

@Preview(device = "spec:width=960dp,height=540dp")
@Composable
private fun TvNitflexUIPreview() {
    TvNitflexTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            TvNitflexUI(
                theoplayerView = rememberTHEOplayerView(THEOplayerConfig.Builder().build()),
                title = "Elephant's Dream",
                onNextEpisode = {}
            )
        }
    }
}
