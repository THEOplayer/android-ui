package com.theoplayer.android.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
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
                PlayButton(modifier = Modifier.alignByBaseline())
                CurrentTimeDisplay(
                    modifier = Modifier.alignByBaseline(),
                    color = Color.White,
                    showDuration = true
                )
            }
        })
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    DefaultUI(THEOplayerConfig.Builder().build())
}
