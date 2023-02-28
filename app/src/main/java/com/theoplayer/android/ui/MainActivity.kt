package com.theoplayer.android.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.theoplayer.android.api.THEOplayerConfig
import com.theoplayer.android.api.THEOplayerView
import com.theoplayer.android.api.error.THEOplayerException
import com.theoplayer.android.api.event.EventListener
import com.theoplayer.android.api.event.player.*
import com.theoplayer.android.api.player.Player
import com.theoplayer.android.api.source.SourceDescription
import com.theoplayer.android.api.source.TypedSource
import com.theoplayer.android.ui.ui.theme.THEOplayerAndroidUITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            THEOplayerAndroidUITheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background
                ) {
                    DefaultUI(THEOplayerConfig.Builder().build())
                }
            }
        }
    }
}

val LocalTHEOplayer = staticCompositionLocalOf<Player?> { null }

@Composable
fun UIController(
    config: THEOplayerConfig, bottomControlBar: @Composable () -> Unit
) {
    val theoplayerView = remember { mutableStateOf<THEOplayerView?>(null) }

    if (LocalInspectionMode.current) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        )
    } else {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                THEOplayerView(context, config).also {
                    it.settings.isFullScreenOrientationCoupled = true
                    it.player.source = SourceDescription.Builder(
                        TypedSource.Builder("https://amssamples.streaming.mediaservices.windows.net/7ceb010f-d9a3-467a-915e-5728acced7e3/ElephantsDreamMultiAudio.ism/manifest(format=mpd-time-csf)")
                            .build()
                    ).build()
                    theoplayerView.value = it
                }
            })
    }

    CompositionLocalProvider(LocalTHEOplayer provides theoplayerView.value?.player) {
        Column(modifier = Modifier.fillMaxSize()) {
            bottomControlBar()
        }
    }
}

@Composable
fun DefaultUI(config: THEOplayerConfig) {
    UIController(config) {
        Row {
            PlayButton()
        }
        ErrorDisplay()
    }
}

@Composable
fun PlayButton(
    play: @Composable () -> Unit = { Text("Play") },
    pause: @Composable () -> Unit = { Text("Pause") }
) {
    val player = LocalTHEOplayer.current
    val paused by produceState(initialValue = true, player) {
        val updatePaused = {
            value = player?.isPaused ?: true
        }
        updatePaused()
        val playListener = EventListener<PlayEvent> { updatePaused() }
        val pauseListener = EventListener<PauseEvent> { updatePaused() }
        player?.addEventListener(PlayerEventTypes.PLAY, playListener)
        player?.addEventListener(PlayerEventTypes.PAUSE, pauseListener)
        awaitDispose {
            player?.removeEventListener(PlayerEventTypes.PLAY, playListener)
            player?.removeEventListener(PlayerEventTypes.PAUSE, pauseListener)
        }
    }

    Button(onClick = {
        player?.let {
            if (it.isPaused) {
                it.play()
            } else {
                it.pause()
            }
        }
    }) {
        if (paused) {
            play()
        } else {
            pause()
        }
    }
}

@Composable
fun ErrorDisplay() {
    val player = LocalTHEOplayer.current
    val error by produceState<THEOplayerException?>(initialValue = null, player) {
        val sourceChangeListener = EventListener<SourceChangeEvent> { value = null }
        val errorListener = EventListener<ErrorEvent> { event -> value = event.errorObject }
        player?.addEventListener(PlayerEventTypes.SOURCECHANGE, sourceChangeListener)
        player?.addEventListener(PlayerEventTypes.ERROR, errorListener)
        awaitDispose {
            player?.removeEventListener(PlayerEventTypes.SOURCECHANGE, sourceChangeListener)
            player?.removeEventListener(PlayerEventTypes.ERROR, errorListener)
        }
    }

    error?.let {
        Text(
            color = Color.Red,
            text = "${it.message}"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    THEOplayerAndroidUITheme {
        DefaultUI(THEOplayerConfig.Builder().build())
    }
}