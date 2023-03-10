package com.theoplayer.android.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
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
                }
            }
            ErrorDisplay(modifier = Modifier.fillMaxWidth(1f))
        },
        centerChrome = {
            PlayButton(iconModifier = Modifier.size(96.dp))
        },
        bottomChrome = {
            val state = LocalTHEOplayer.current
            if (state?.firstPlay == true) {
                SeekBar()
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MuteButton()
                    LiveButton()
                    CurrentTimeDisplay(
                        showDuration = true
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    LanguageMenuButton()
                    SettingsMenuButton()
                    FullscreenButton()
                }
            }
        })
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    DefaultUI(
        config = THEOplayerConfig.Builder().build(),
        title = "Elephant's Dream"
    )
}
