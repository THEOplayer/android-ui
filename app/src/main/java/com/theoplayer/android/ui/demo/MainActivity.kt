package com.theoplayer.android.ui.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.google.android.gms.cast.framework.CastContext
import com.theoplayer.android.api.THEOplayerConfig
import com.theoplayer.android.api.THEOplayerView
import com.theoplayer.android.api.ads.ima.GoogleImaIntegrationFactory
import com.theoplayer.android.api.cast.CastConfiguration
import com.theoplayer.android.api.cast.CastIntegrationFactory
import com.theoplayer.android.api.cast.CastStrategy
import com.theoplayer.android.ui.DefaultUI
import com.theoplayer.android.ui.demo.nitflex.NitflexUI
import com.theoplayer.android.ui.demo.nitflex.theme.NitflexTheme
import com.theoplayer.android.ui.rememberPlayer
import com.theoplayer.android.ui.theme.THEOplayerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Chromecast immediately, for automatic receiver discovery to work correctly.
        CastContext.getSharedInstance(this)

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
    var stream by rememberSaveable(stateSaver = StreamSaver) { mutableStateOf(streams.first()) }
    var streamMenuOpen by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val theoplayerView = remember(context) {
        THEOplayerView(context).apply {
            // Add ads integration through Google IMA
            player.addIntegration(
                GoogleImaIntegrationFactory.createGoogleImaIntegration(this)
            )
            // Add Chromecast integration
            val castConfiguration = CastConfiguration.Builder().apply {
                castStrategy(CastStrategy.AUTO)
            }.build()
            player.addIntegration(
                CastIntegrationFactory.createCastIntegration(this, castConfiguration)
            )
        }
    }
    val player = rememberPlayer(theoplayerView)
    LaunchedEffect(player, stream) {
        player.source = stream.source
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
                        player.source = stream.source
                        player.play()
                    }) {
                        Icon(Icons.Rounded.Refresh, contentDescription = "Reload")
                    }
                    IconButton(onClick = { streamMenuOpen = true }) {
                        Icon(Icons.Rounded.Movie, contentDescription = "Stream")
                    }
                    IconButton(onClick = { themeMenuOpen = true }) {
                        Icon(Icons.Rounded.Brush, contentDescription = "Theme")
                    }
                }
            )
        }) { padding ->
            val playerModifier = Modifier
                .padding(padding)
                .fillMaxSize(1f)
            when (theme) {
                PlayerTheme.Default -> {
                    DefaultUI(
                        modifier = playerModifier,
                        player = player,
                        title = stream.title
                    )
                }

                PlayerTheme.Nitflex -> {
                    NitflexTheme(useDarkTheme = true) {
                        NitflexUI(
                            modifier = playerModifier,
                            player = player,
                            title = stream.title
                        )
                    }
                }
            }

            if (streamMenuOpen) {
                SelectStreamDialog(
                    streams = streams,
                    currentStream = stream,
                    onSelectStream = {
                        stream = it
                        streamMenuOpen = false
                    },
                    onDismissRequest = { streamMenuOpen = false }
                )
            }
            if (themeMenuOpen) {
                SelectThemeDialog(
                    currentTheme = theme,
                    onSelectTheme = {
                        theme = it
                        themeMenuOpen = false
                    },
                    onDismissRequest = { themeMenuOpen = false }
                )
            }
        }
    }
}

enum class PlayerTheme(val title: String) {
    Default(title = "Default theme"),
    Nitflex(title = "Nitflex theme")
}

@Composable
fun SelectStreamDialog(
    streams: List<Stream>,
    currentStream: Stream,
    onSelectStream: (Stream) -> Unit,
    onDismissRequest: () -> Unit,
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Select a stream",
                    style = MaterialTheme.typography.headlineSmall
                )
                LazyColumn {
                    items(items = streams) {
                        ListItem(
                            headlineContent = { Text(text = it.title) },
                            leadingContent = {
                                RadioButton(
                                    selected = (it == currentStream),
                                    onClick = null
                                )
                            },
                            modifier = Modifier.clickable(onClick = {
                                onSelectStream(it)
                            })
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SelectThemeDialog(
    currentTheme: PlayerTheme,
    onSelectTheme: (PlayerTheme) -> Unit,
    onDismissRequest: () -> Unit,
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Select a theme",
                    style = MaterialTheme.typography.headlineSmall
                )
                LazyColumn {
                    items(items = PlayerTheme.values()) {
                        ListItem(
                            headlineContent = { Text(text = it.title) },
                            leadingContent = {
                                RadioButton(
                                    selected = (it == currentTheme),
                                    onClick = null
                                )
                            },
                            modifier = Modifier.clickable(onClick = {
                                onSelectTheme(it)
                            })
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MaterialTheme {
        DefaultUI(config = THEOplayerConfig.Builder().build())
    }
}