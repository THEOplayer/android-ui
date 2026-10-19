package com.theoplayer.android.ui.demo.tv

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import com.theoplayer.android.api.THEOplayerView
import com.theoplayer.android.ui.demo.Stream
import com.theoplayer.android.ui.tv.DefaultUI
import com.theoplayer.android.ui.tv.ExperimentalTvUiApi

/**
 * Plays the given [stream] with the given [theme].
 *
 * Back first hides the controls (handled by the player UI), and then calls [onExit].
 */
@OptIn(ExperimentalTvUiApi::class)
@Composable
fun TvPlayerScreen(
    modifier: Modifier = Modifier,
    theoplayerView: THEOplayerView,
    stream: Stream,
    theme: TvPlayerTheme,
    onExit: () -> Unit
) {
    DisposableEffect(theoplayerView, stream) {
        val player = theoplayerView.player
        player.isAutoplay = true
        player.source = stream.source
        onDispose {
            player.source = null
        }
    }

    // Registered before the player UI, so the UI's own Back handler takes precedence.
    BackHandler(onBack = onExit)

    when (theme) {
        TvPlayerTheme.Default -> DefaultUI(
            modifier = modifier,
            theoplayerView = theoplayerView,
            title = stream.title
        )
    }
}
