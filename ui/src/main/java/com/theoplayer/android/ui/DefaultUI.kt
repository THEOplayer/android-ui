package com.theoplayer.android.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.theoplayer.android.api.THEOplayerConfig

@Composable
fun DefaultUI(config: THEOplayerConfig) {
    UIController(
        config = config,
        topChrome = {
            ErrorDisplay()
        },
        bottomChrome = {
            SeekBar()
            Row {
                PlayButton(modifier = Modifier.align(Alignment.CenterVertically))
                MuteButton(modifier = Modifier.align(Alignment.CenterVertically))
                CurrentTimeDisplay(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    color = Color.White,
                    showDuration = true
                )
                Spacer(modifier = Modifier.weight(1f))
                FullscreenButton(modifier = Modifier.align(Alignment.CenterVertically))
            }
        })
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    DefaultUI(THEOplayerConfig.Builder().build())
}
