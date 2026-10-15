package com.theoplayer.android.ui

import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.theoplayer.android.api.THEOplayerView

/**
 * Hosts [theoplayerView] and draws [ui] inside its UI container.
 */
@InternalUiApi
@Composable
fun PlayerContainer(
    modifier: Modifier = Modifier,
    theoplayerView: THEOplayerView?,
    ui: @Composable () -> Unit
) {
    if (theoplayerView == null) {
        Box(
            modifier = modifier
        ) {
            ui()
        }
    } else {
        val lifecycle = LocalLifecycleOwner.current.lifecycle
        var uiContainer by remember { mutableStateOf<ViewGroup?>(null) }
        var composeView by remember { mutableStateOf<ComposeView?>(null) }

        AndroidView(
            modifier = modifier,
            factory = { context ->
                uiContainer =
                    theoplayerView.findViewById(com.theoplayer.android.R.id.theo_ui_container)
                // Wrap our UI inside a ComposeView
                composeView = ComposeView(context).apply {
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
                }
                // Host the THEOplayer view inside our AndroidView
                (theoplayerView.parent as? ViewGroup)?.removeView(theoplayerView)
                theoplayerView
            })

        // Install inside THEOplayerView's UI container
        DisposableEffect(uiContainer, composeView) {
            val container = uiContainer
            val view = composeView
            if (container != null && view != null) {
                container.addView(view)
                theoplayerView.postInvalidate()
            }
            onDispose {
                if (container != null && view != null) {
                    container.removeView(view)
                    view.disposeComposition()
                    theoplayerView.postInvalidate()
                }
            }
        }
    }
}
