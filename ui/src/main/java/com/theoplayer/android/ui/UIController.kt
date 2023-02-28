package com.theoplayer.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.viewinterop.AndroidView
import com.theoplayer.android.api.THEOplayerConfig
import com.theoplayer.android.api.THEOplayerView
import com.theoplayer.android.api.source.SourceDescription
import com.theoplayer.android.api.source.TypedSource

@Composable
fun UIController(
    config: THEOplayerConfig, bottomControlBar: @Composable () -> Unit
) {
    val theoplayerView = remember { mutableStateOf<THEOplayerView?>(null) }
    val state = rememberPlayerState(theoplayerView.value?.player)

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

    CompositionLocalProvider(LocalTHEOplayer provides state) {
        Column(modifier = Modifier.fillMaxSize()) {
            bottomControlBar()
        }
    }
}