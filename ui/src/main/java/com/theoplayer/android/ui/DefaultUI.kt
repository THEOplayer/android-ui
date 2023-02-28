package com.theoplayer.android.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.theoplayer.android.api.THEOplayerConfig

@Composable
fun DefaultUI(config: THEOplayerConfig) {
    UIController(config) {
        Row {
            PlayButton()
        }
        ErrorDisplay()
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    DefaultUI(THEOplayerConfig.Builder().build())
}
