package com.theoplayer.android.ui

import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.theoplayer.android.api.THEOplayerConfig
import com.theoplayer.android.api.THEOplayerView
import com.theoplayer.android.api.source.SourceDescription
import com.theoplayer.android.api.source.TypedSource

@Composable
fun UIController(
    config: THEOplayerConfig,
    modifier: Modifier = Modifier,
    centerOverlay: (@Composable ColumnScope.() -> Unit)? = null,
    topChrome: (@Composable ColumnScope.() -> Unit)? = null,
    bottomChrome: (@Composable ColumnScope.() -> Unit)? = null
) {
    val theoplayerView = if (LocalInspectionMode.current) {
        null
    } else {
        rememberTHEOplayerView(config)
    }
    val state = rememberPlayerState(theoplayerView)

    val ui = remember {
        movableContentOf {
            CompositionLocalProvider(LocalTHEOplayer provides state) {
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    centerOverlay?.let { it() }
                }
                Column(modifier = Modifier.fillMaxSize()) {
                    topChrome?.let { it() }
                    Spacer(modifier = Modifier.weight(1f))
                    bottomChrome?.let { it() }
                }
            }
        }
    }

    if (theoplayerView == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            ui()
        }
    } else {
        val lifecycle = LocalLifecycleOwner.current.lifecycle
        AndroidView(
            modifier = modifier.fillMaxSize(),
            factory = { context ->
                // Install inside THEOplayerView's UI container
                val uiContainer =
                    theoplayerView.findViewById<ViewGroup>(com.theoplayer.android.R.id.theo_ui_container)
                uiContainer.addView(ComposeView(context).apply {
                    // When entering fullscreen, we remove the view from its original location
                    // and add it to the activity's root view.
                    // When it is temporarily removed (and detached from the window),
                    // we do *not* want to lose the composition. Therefore, don't use the default
                    // DisposeOnDetachedFromWindow or DisposeOnDetachedFromWindowOrReleasedFromPool
                    // strategies!
                    setViewCompositionStrategy(
                        ViewCompositionStrategy.DisposeOnLifecycleDestroyed(lifecycle)
                    )
                    setContent {
                        ui()
                    }
                })
                theoplayerView
            })
    }
}

@Composable
fun rememberTHEOplayerView(config: THEOplayerConfig): THEOplayerView {
    val context = LocalContext.current
    val theoplayerView = remember {
        THEOplayerView(context, config).apply {
            settings.isFullScreenOrientationCoupled = true
            player.source = SourceDescription.Builder(
                TypedSource.Builder("https://amssamples.streaming.mediaservices.windows.net/7ceb010f-d9a3-467a-915e-5728acced7e3/ElephantsDreamMultiAudio.ism/manifest(format=mpd-time-csf)")
                    .build()
            ).build()
        }
    }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(key1 = lifecycle, key2 = theoplayerView) {
        val lifecycleObserver = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> theoplayerView.onResume()
                Lifecycle.Event.ON_PAUSE -> theoplayerView.onPause()
                Lifecycle.Event.ON_DESTROY -> theoplayerView.onDestroy()
                else -> {}
            }
        }
        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }

    return theoplayerView
}