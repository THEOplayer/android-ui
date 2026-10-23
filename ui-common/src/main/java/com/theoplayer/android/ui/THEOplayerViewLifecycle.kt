package com.theoplayer.android.ui

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.theoplayer.android.api.THEOplayerConfig
import com.theoplayer.android.api.THEOplayerView

/**
 * Creates and remembers a [THEOplayerView] owned by the composition.
 *
 * This couples the lifecycle of the view to the current activity: it automatically calls
 * [THEOplayerView.onPause] and [THEOplayerView.onResume] whenever the current activity is
 * [paused][Activity.onPause] or [resumed][Activity.onResume] (resuming an ad that was interrupted),
 * and it destroys the view when it leaves the composition.
 *
 * In previews, the SDK does not initialize the view ([View.isInEditMode]), so do not access its
 * [THEOplayerView.player] there. [UIController] and [DefaultUI] handle this automatically.
 *
 * @param config the player configuration
 */
@Composable
fun rememberTHEOplayerView(config: THEOplayerConfig? = null): THEOplayerView {
    val context = LocalContext.current
    val inspectionMode = LocalInspectionMode.current
    val theoplayerView = remember {
        THEOplayerView(if (inspectionMode) PreviewContext(context) else context, config)
    }
    if (!theoplayerView.isInEditMode) {
        setupTHEOplayerView(theoplayerView, destroyOnLifecycleDestroy = false)
        DisposableEffect(theoplayerView) { onDispose { theoplayerView.onDestroy() } }
    }
    return theoplayerView
}

/**
 * A context for creating a [THEOplayerView] in a preview.
 *
 * The [THEOplayerView] constructor casts the application context to [Application], but layoutlib's
 * application context is not an [Application].
 */
private class PreviewContext(base: Context) : ContextWrapper(base) {
    private val application = PreviewApplication(base.applicationContext)

    override fun getApplicationContext(): Context = application
}

private class PreviewApplication(base: Context) : Application() {
    init {
        attachBaseContext(base)
    }
}

/** Remembers a THEOplayer view for use by a UI module. */
@InternalUiApi
@Composable
fun rememberTHEOplayerViewInternal(config: THEOplayerConfig?): THEOplayerView? {
    return if (LocalInspectionMode.current) null else rememberTHEOplayerView(config)
}

/** Couples a THEOplayer view to the current lifecycle owner. */
@InternalUiApi
@Composable
fun setupTHEOplayerView(
    theoplayerView: THEOplayerView,
    destroyOnLifecycleDestroy: Boolean = true,
): THEOplayerView {
    var wasPlayingAd by remember { mutableStateOf(false) }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, theoplayerView) {
        val lifecycleObserver = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    theoplayerView.onResume()
                    if (wasPlayingAd) {
                        theoplayerView.player.play()
                        wasPlayingAd = false
                    }
                }

                Lifecycle.Event.ON_PAUSE -> {
                    wasPlayingAd = theoplayerView.player.ads.isPlaying
                    theoplayerView.onPause()
                }

                Lifecycle.Event.ON_DESTROY -> {
                    if (destroyOnLifecycleDestroy) {
                        theoplayerView.onDestroy()
                    }
                }
                else -> {}
            }
        }
        lifecycle.addObserver(lifecycleObserver)
        onDispose { lifecycle.removeObserver(lifecycleObserver) }
    }

    return theoplayerView
}
