package com.theoplayer.android.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.theoplayer.android.api.THEOplayerConfig

@Composable
fun DefaultUI(
    modifier: Modifier = Modifier,
    config: THEOplayerConfig,
    title: String? = null
) {
    UIController(
        modifier = modifier,
        config = config,
        centerOverlay = {
            val state = LocalTHEOplayer.current
            if (state?.firstPlay == true) {
                LoadingSpinner()
            }
        },
        topChrome = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                title?.let {
                    Text(
                        modifier = Modifier.padding(8.dp),
                        color = Color.White,
                        text = it
                    )
                }
            }
            ErrorDisplay()
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
                        color = Color.White,
                        showDuration = true
                    )
                    Spacer(modifier = Modifier.weight(1f))
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
