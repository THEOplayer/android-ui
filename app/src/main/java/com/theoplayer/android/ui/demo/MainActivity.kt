package com.theoplayer.android.ui.demo

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
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
import androidx.compose.material.icons.rounded.PictureInPicture
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.core.pip.PictureInPictureDelegate
import androidx.core.pip.VideoPlaybackPictureInPicture
import com.google.android.gms.cast.framework.CastContext
import com.theoplayer.android.api.THEOplayerConfig
import com.theoplayer.android.api.THEOplayerView
import com.theoplayer.android.api.ads.ima.GoogleImaIntegrationFactory
import com.theoplayer.android.api.cast.CastConfiguration
import com.theoplayer.android.api.cast.CastIntegrationFactory
import com.theoplayer.android.api.cast.CastStrategy
import com.theoplayer.android.api.event.player.PlayerEventTypes
import com.theoplayer.android.api.pip.PiPType
import com.theoplayer.android.api.pip.PipConfiguration
import com.theoplayer.android.ui.DefaultUI
import com.theoplayer.android.ui.Player
import com.theoplayer.android.ui.demo.nitflex.NitflexUI
import com.theoplayer.android.ui.demo.nitflex.theme.NitflexTheme
import com.theoplayer.android.ui.rememberPlayer
import com.theoplayer.android.ui.theme.THEOplayerTheme
import androidx.compose.material3.Icon as Material3Icon

class MainActivity : ComponentActivity(), PictureInPictureDelegate.OnPictureInPictureEventListener {
    private lateinit var theoplayerView: THEOplayerView
    private lateinit var pip: VideoPlaybackPictureInPicture

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Chromecast immediately, for automatic receiver discovery to work correctly.
        CastContext.getSharedInstance(this)

        // Initialize THEOplayer
        val config = THEOplayerConfig.Builder().apply {
            pipConfiguration(PipConfiguration.Builder().build())
        }.build()
        theoplayerView = THEOplayerView(this, config).apply {
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

        initializePictureInPicture()

        setContent {
            THEOplayerTheme(useDarkTheme = true) {
                MainContent(
                    theoplayerView = theoplayerView,
                    onEnterPip = ::enterPictureInPicture
                )
            }
        }
    }

    private fun initializePictureInPicture() {
        pip = VideoPlaybackPictureInPicture(this)
        pip.addOnPictureInPictureEventListener(
            ContextCompat.getMainExecutor(this),
            this
        )
        pip.setAspectRatio(Rational(16, 9))
        pip.setPlayerView(theoplayerView)
        pip.setEnabled(true)

        theoplayerView.player.addEventListener(PlayerEventTypes.RESIZE) { updatePictureInPictureAspectRatio() }
    }

    private fun enterPictureInPicture() {
        theoplayerView.piPManager?.enterPiP(PiPType.CUSTOM)
    }

    private fun updatePictureInPictureAspectRatio() {
        val player = theoplayerView.player
        if (player.videoWidth > 0 && player.videoHeight > 0) {
            pip.setAspectRatio(Rational(player.videoWidth, player.videoHeight))
        }
    }

    override fun onPictureInPictureEvent(
        event: PictureInPictureDelegate.Event,
        config: Configuration?
    ) {
        val pipManager = theoplayerView.piPManager ?: return
        when (event) {
            PictureInPictureDelegate.Event.ENTERED -> {
                if (!pipManager.isInPiP) {
                    pipManager.enterPiP(PiPType.CUSTOM)
                }
            }

            PictureInPictureDelegate.Event.EXITED -> {
                if (pipManager.isInPiP) {
                    pipManager.exitPiP()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        pip.close()
        theoplayerView.onDestroy()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(
    theoplayerView: THEOplayerView,
    onEnterPip: () -> Unit = {}
) {
    var stream by rememberSaveable(stateSaver = StreamSaver) { mutableStateOf(streams.first()) }
    var streamMenuOpen by remember { mutableStateOf(false) }

    val player = rememberPlayer(theoplayerView)
    LaunchedEffect(player, stream) {
        player.source = stream.source
    }

    var themeMenuOpen by remember { mutableStateOf(false) }
    var theme by rememberSaveable { mutableStateOf(PlayerTheme.Default) }
    val activity = LocalActivity.current as? ComponentActivity

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && activity?.isInPictureInPictureMode == true) {
        // Only show player while in picture-in-picture mode
        Surface(modifier = Modifier.fillMaxSize()) {
            PlayerContent(
                modifier = Modifier.fillMaxSize(),
                player = player,
                stream = stream,
                theme = theme
            )
        }
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Demo")
                },
                actions = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        IconButton(onClick = onEnterPip) {
                            Material3Icon(
                                Icons.Rounded.PictureInPicture,
                                contentDescription = "Enter picture-in-picture"
                            )
                        }
                    }
                    IconButton(onClick = {
                        player.source = stream.source
                        player.play()
                    }) {
                        Material3Icon(Icons.Rounded.Refresh, contentDescription = "Reload")
                    }
                    IconButton(onClick = { streamMenuOpen = true }) {
                        Material3Icon(Icons.Rounded.Movie, contentDescription = "Stream")
                    }
                    IconButton(onClick = { themeMenuOpen = true }) {
                        Material3Icon(Icons.Rounded.Brush, contentDescription = "Theme")
                    }
                }
            )
        }
    ) { padding ->
        PlayerContent(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(1f),
            player = player,
            stream = stream,
            theme = theme
        )

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

@Composable
fun PlayerContent(
    modifier: Modifier = Modifier,
    player: Player,
    stream: Stream,
    theme: PlayerTheme
) {
    when (theme) {
        PlayerTheme.Default -> {
            DefaultUI(
                modifier = modifier,
                player = player,
                title = stream.title
            )
        }

        PlayerTheme.Nitflex -> {
            NitflexTheme(useDarkTheme = true) {
                NitflexUI(
                    modifier = modifier,
                    player = player,
                    title = stream.title
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
                    items(items = PlayerTheme.entries) {
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