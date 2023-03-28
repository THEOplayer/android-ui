package com.theoplayer.android.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.theoplayer.android.api.THEOplayerConfig
import com.theoplayer.android.api.source.SourceDescription

@Composable
fun DefaultUI(
    modifier: Modifier = Modifier,
    config: THEOplayerConfig,
    source: SourceDescription? = null,
    title: String? = null
) {
    UIController(
        modifier = modifier,
        config = config,
        source = source,
        centerOverlay = {
            val state = LocalTHEOplayer.current
            if (state?.firstPlay == true) {
                LoadingSpinner()
            }
        },
        topChrome = {
            val state = LocalTHEOplayer.current
            if (state?.firstPlay == true) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    title?.let {
                        Text(
                            modifier = Modifier.padding(8.dp),
                            text = it
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    LanguageMenuButton()
                    SettingsMenuButton()
                }
            }
        },
        centerChrome = {
            val state = LocalTHEOplayer.current
            if (state?.firstPlay == true) {
                SeekButton(seekOffset = -10, iconSize = 48.dp, contentPadding = PaddingValues(8.dp))
            }
            PlayButton(iconModifier = Modifier.size(96.dp), contentPadding = PaddingValues(8.dp))
            if (state?.firstPlay == true) {
                SeekButton(seekOffset = 10, iconSize = 48.dp, contentPadding = PaddingValues(8.dp))
            }
        },
        bottomChrome = {
            val state = LocalTHEOplayer.current
            if (state?.firstPlay == true) {
                if (state.streamType != StreamType.Live) {
                    SeekBar()
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MuteButton()
                    LiveButton()
                    CurrentTimeDisplay(showDuration = true)
                    Spacer(modifier = Modifier.weight(1f))
                    FullscreenButton()
                }
            }
        },
        errorOverlay = {
            val state = LocalTHEOplayer.current
            Box(contentAlignment = Alignment.Center) {
                ErrorDisplay()
                // Ensure the user can still exit fullscreen
                if (state?.fullscreen == true) {
                    FullscreenButton(modifier = Modifier.align(Alignment.BottomEnd))
                }
            }
        }
    )
}
