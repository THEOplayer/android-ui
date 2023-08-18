package com.theoplayer.android.ui.demo

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.theoplayer.android.api.THEOplayerConfig
import com.theoplayer.android.api.ads.ima.GoogleImaAdErrorEvent
import com.theoplayer.android.api.ads.ima.GoogleImaAdEventType
import com.theoplayer.android.api.ads.ima.GoogleImaIntegrationFactory.createGoogleImaIntegration
import com.theoplayer.android.api.event.player.PlayerEventTypes
import com.theoplayer.android.api.source.SourceDescription
import com.theoplayer.android.api.source.TypedSource
import com.theoplayer.android.api.source.addescription.GoogleImaAdDescription
import com.theoplayer.android.ui.DefaultUI
import com.theoplayer.android.ui.demo.nitflex.NitflexUI
import com.theoplayer.android.ui.demo.nitflex.theme.NitflexTheme
import com.theoplayer.android.ui.rememberPlayer
import com.theoplayer.android.ui.theme.THEOplayerTheme

private const val TAG = "DEMO"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            THEOplayerTheme(useDarkTheme = true) {
                MainContent()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent() {
    val player = rememberPlayer()

    val adTag = "https://cdn.theoplayer.com/demos/ads/vast/dfp-preroll-no-skip.xml"
    val imaAdDescription =
        GoogleImaAdDescription.Builder(adTag)
            .timeOffset("start")
            .build()
    val source = SourceDescription.Builder(
        TypedSource.Builder("https://cdn.theoplayer.com/video/dash/webvtt-embedded-in-isobmff/Manifest.mpd")
            .build()
    )
        .ads(imaAdDescription)
        .build()

    DisposableEffect(player) {
        player.theoplayerView?.let { playerView ->
            val imaIntegration = createGoogleImaIntegration(playerView)
            player.player?.addIntegration(imaIntegration)
        }
        player.player?.addEventListener(PlayerEventTypes.PLAY) { Log.d(TAG, "PLAY") }
        player.player?.addEventListener(PlayerEventTypes.PLAYING) { Log.d(TAG, "PLAYING") }
        player.player?.addEventListener(PlayerEventTypes.PAUSE) { Log.d(TAG, "PAUSE") }
        player.player?.ads?.addEventListener(GoogleImaAdEventType.AD_ERROR) { event ->
            Log.d(TAG, "IMA ERROR: ${(event as GoogleImaAdErrorEvent).adError?.errorType}")
        }
        player.source = source
        onDispose {
            // Clean-up: remove listeners
        }
    }

    var themeMenuOpen by remember { mutableStateOf(false) }
    var theme by rememberSaveable { mutableStateOf(PlayerTheme.Default) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(topBar = {
            TopAppBar(
                title = {
                    Text(text = "Demo")
                },
                actions = {
                    IconButton(onClick = {
                        player.source = source
                        player.play()
                    }) {
                        Icon(Icons.Rounded.Refresh, contentDescription = "Reload")
                    }
                    IconButton(onClick = { themeMenuOpen = true }) {
                        Icon(Icons.Rounded.Brush, contentDescription = "Theme")
                    }
                    DropdownMenu(
                        expanded = themeMenuOpen,
                        onDismissRequest = { themeMenuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text(text = "Default theme") },
                            onClick = { theme = PlayerTheme.Default })
                        DropdownMenuItem(
                            text = { Text(text = "Nitflex theme") },
                            onClick = { theme = PlayerTheme.Nitflex })
                    }
                }
            )
        }, content = { padding ->
            val playerModifier = Modifier
                .padding(padding)
                .fillMaxSize(1f)
            when (theme) {
                PlayerTheme.Default -> {
                    DefaultUI(
                        modifier = playerModifier,
                        player = player,
                        title = "Elephant's Dream"
                    )
                }

                PlayerTheme.Nitflex -> {
                    NitflexTheme(useDarkTheme = true) {
                        NitflexUI(
                            modifier = playerModifier,
                            player = player,
                            title = "Elephant's Dream"
                        )
                    }
                }
            }
        })
    }
}

enum class PlayerTheme {
    Default,
    Nitflex
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MaterialTheme {
        DefaultUI(config = THEOplayerConfig.Builder().build())
    }
}