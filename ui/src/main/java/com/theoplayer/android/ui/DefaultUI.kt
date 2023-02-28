package com.theoplayer.android.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.theoplayer.android.api.THEOplayerConfig

@Composable
fun DefaultUI(config: THEOplayerConfig) {
    UIController(config) {
        SeekBar()
        Row {
            PlayButton()
            CurrentTimeDisplay(color = Color.White, showDuration = true)
        }
        ErrorDisplay()
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    DefaultUI(THEOplayerConfig.Builder().build())
}
