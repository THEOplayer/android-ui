package com.theoplayer.android.ui.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.tv.material3.Surface
import com.theoplayer.android.api.THEOplayerConfig
import com.theoplayer.android.ui.tv.DefaultUI
import com.theoplayer.android.ui.tv.ExperimentalTvUiApi
import com.theoplayer.android.ui.tv.theme.THEOplayerTvTheme

/**
 * Demo activity for Android TV, launched from the Android TV launcher.
 */
class TvActivity : ComponentActivity() {
    @OptIn(ExperimentalTvUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val stream = streams.first()
        setContent {
            THEOplayerTvTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DefaultUI(
                        modifier = Modifier.fillMaxSize(),
                        config = THEOplayerConfig.Builder().build(),
                        source = stream.source,
                        title = stream.title
                    )
                }
            }
        }
    }
}
