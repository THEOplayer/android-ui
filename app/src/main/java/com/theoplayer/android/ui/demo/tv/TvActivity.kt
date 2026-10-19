package com.theoplayer.android.ui.demo.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.tv.material3.Surface
import com.theoplayer.android.api.THEOplayerConfig
import com.theoplayer.android.api.THEOplayerView
import com.theoplayer.android.api.ads.ima.GoogleImaIntegrationFactory
import com.theoplayer.android.ui.demo.Stream
import com.theoplayer.android.ui.demo.streams
import com.theoplayer.android.ui.tv.theme.THEOplayerTvTheme

/**
 * Demo activity for Android TV, launched from the Android TV launcher.
 */
class TvActivity : ComponentActivity() {
    private lateinit var theoplayerView: THEOplayerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        theoplayerView = THEOplayerView(this, THEOplayerConfig.Builder().build()).apply {
            // Add ads integration through Google IMA
            player.addIntegration(
                GoogleImaIntegrationFactory.createGoogleImaIntegration(this)
            )
        }

        setContent {
            THEOplayerTvTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    TvContent(theoplayerView = theoplayerView)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        theoplayerView.onPause()
    }

    override fun onResume() {
        super.onResume()
        theoplayerView.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        theoplayerView.onDestroy()
    }
}

/**
 * Shows the start screen until a stream is selected, then plays that stream.
 */
@Composable
fun TvContent(theoplayerView: THEOplayerView) {
    var stream by rememberSaveable(stateSaver = OptionalStreamSaver) { mutableStateOf<Stream?>(null) }
    var theme by rememberSaveable { mutableStateOf(TvPlayerTheme.Default) }

    when (val currentStream = stream) {
        null -> TvHomeScreen(
            modifier = Modifier.fillMaxSize(),
            streams = streams,
            currentTheme = theme,
            onSelectStream = { stream = it },
            onSelectTheme = { theme = it }
        )

        else -> TvPlayerScreen(
            modifier = Modifier.fillMaxSize(),
            theoplayerView = theoplayerView,
            stream = currentStream,
            theme = theme,
            onExit = { stream = null }
        )
    }
}

/**
 * The player themes available on Android TV.
 */
enum class TvPlayerTheme(val title: String) {
    Default(title = "Default theme")
}

/**
 * Saves the selected stream by its index in [streams], or -1 when no stream is selected.
 */
private val OptionalStreamSaver = Saver<Stream?, Int>(
    save = { streams.indexOf(it) },
    restore = { streams.getOrNull(it) }
)
