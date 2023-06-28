package com.theoplayer.android.ui.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.theoplayer.android.api.THEOplayerConfig
import com.theoplayer.android.api.source.SourceDescription
import com.theoplayer.android.api.source.TypedSource
import com.theoplayer.android.ui.DefaultUI
import com.theoplayer.android.ui.demo.nitflex.NitflexUI
import com.theoplayer.android.ui.demo.nitflex.theme.NitflexTheme
import com.theoplayer.android.ui.rememberPlayer
import com.theoplayer.android.ui.theme.THEOplayerTheme

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
    val source = SourceDescription.Builder(
        TypedSource.Builder("https://amssamples.streaming.mediaservices.windows.net/7ceb010f-d9a3-467a-915e-5728acced7e3/ElephantsDreamMultiAudio.ism/manifest(format=mpd-time-csf)")
            .build()
    ).build()

    val player = rememberPlayer()
    LaunchedEffect(key1 = player, key2 = source) {
        player.player?.source = source
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
            when (theme) {
                PlayerTheme.Default -> {
                    DefaultUI(
                        modifier = Modifier.padding(padding),
                        player = player,
                        title = "Elephant's Dream"
                    )
                }

                PlayerTheme.Nitflex -> {
                    NitflexTheme(useDarkTheme = true) {
                        NitflexUI(
                            modifier = Modifier.padding(padding),
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