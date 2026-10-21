package com.theoplayer.android.ui

import android.app.Activity
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.theoplayer.android.api.THEOplayerConfig
import com.theoplayer.android.api.THEOplayerView
import com.theoplayer.android.api.cast.chromecast.PlayerCastState
import com.theoplayer.android.api.source.SourceDescription
import com.theoplayer.android.ui.theme.THEOplayerTheme
import com.theoplayer.android.ui.util.rememberIsInPipMode

/**
 * A container component for a THEOplayer UI.
 *
 * This component provides a basic layout structure for a player UI,
 * and handles the creation and management of a [Player] instance for this UI.
 *
 * The colors and fonts can be changed by wrapping this inside a [THEOplayerTheme].
 *
 * @param modifier the [Modifier] to be applied to this container
 * @param config the player configuration to be used when constructing the [THEOplayerView]
 * @param source the source description to load into the player
 * @param color the background color for the overlay while showing the UI controls
 * @param centerOverlay content to show in the center of the player, typically a [LoadingSpinner].
 * @param errorOverlay content to show when the player encountered a fatal error,
 * typically an [ErrorDisplay].
 * @param topChrome controls to show at the top of the player, for example the stream's title.
 * @param centerChrome controls to show in the center of the player, for example a large [PlayButton].
 * @param bottomChrome controls to show at the bottom of the player, for example a [SeekBar]
 * or a [Row] containing a [MuteButton] and a [FullscreenButton].
 */
@Composable
fun UIController(
    modifier: Modifier = Modifier,
    config: THEOplayerConfig,
    source: SourceDescription? = null,
    color: Color = Color.Black.copy(alpha = 0.5f),
    centerOverlay: (@Composable UIControllerScope.() -> Unit)? = null,
    errorOverlay: (@Composable UIControllerScope.() -> Unit)? = null,
    topChrome: (@Composable UIControllerScope.() -> Unit)? = null,
    centerChrome: (@Composable UIControllerScope.() -> Unit)? = null,
    bottomChrome: (@Composable UIControllerScope.() -> Unit)? = null
) {
    val theoplayerView = rememberTHEOplayerViewInternal(config)
    LaunchedEffect(theoplayerView, source) {
        theoplayerView?.player?.source = source
    }

    UIControllerImpl(
        modifier = modifier,
        theoplayerView = theoplayerView,
        player = null,
        color = color,
        centerOverlay = centerOverlay,
        errorOverlay = errorOverlay,
        topChrome = topChrome,
        centerChrome = centerChrome,
        bottomChrome = bottomChrome
    )
}

/**
 * A container component for a THEOplayer UI.
 *
 * This component provides a basic layout structure for a player UI using the given [player].
 *
 * The colors and fonts can be changed by wrapping this inside a [THEOplayerTheme].
 *
 * @param modifier the [Modifier] to be applied to this container
 * @param player the player. This should always be created using [rememberPlayer].
 * @param color the background color for the overlay while showing the UI controls
 * @param centerOverlay content to show in the center of the player, typically a [LoadingSpinner].
 * @param errorOverlay content to show when the player encountered a fatal error,
 * typically an [ErrorDisplay].
 * @param topChrome controls to show at the top of the player, for example the stream's title.
 * @param centerChrome controls to show in the center of the player, for example a large [PlayButton].
 * @param bottomChrome controls to show at the bottom of the player, for example a [SeekBar]
 * or a [Row] containing a [MuteButton] and a [FullscreenButton].
 */
@Composable
fun UIController(
    modifier: Modifier = Modifier,
    player: Player = rememberPlayer(),
    color: Color = Color.Black.copy(alpha = 0.5f),
    centerOverlay: (@Composable UIControllerScope.() -> Unit)? = null,
    errorOverlay: (@Composable UIControllerScope.() -> Unit)? = null,
    topChrome: (@Composable UIControllerScope.() -> Unit)? = null,
    centerChrome: (@Composable UIControllerScope.() -> Unit)? = null,
    bottomChrome: (@Composable UIControllerScope.() -> Unit)? = null
) {
    UIControllerImpl(
        modifier = modifier,
        theoplayerView = player.theoplayerView,
        player = player,
        color = color,
        centerOverlay = centerOverlay,
        errorOverlay = errorOverlay,
        topChrome = topChrome,
        centerChrome = centerChrome,
        bottomChrome = bottomChrome
    )
}

/**
 * A container component for a THEOplayer UI.
 *
 * This component provides a basic layout structure for a player UI using the given
 * [theoplayerView]. No [Player] is created unless [UIControllerScope.player] is accessed,
 * so [Player.current] is `null` inside this UI: use [LocalTHEOplayerView] and the
 * experimental state holders instead.
 *
 * This does not touch the lifecycle of the given view: use [rememberTHEOplayerView] to
 * create a view owned by the composition, or forward [THEOplayerView.onPause],
 * [THEOplayerView.onResume] and [THEOplayerView.onDestroy] yourself.
 *
 * The colors and fonts can be changed by wrapping this inside a [THEOplayerTheme].
 *
 * @param modifier the [Modifier] to be applied to this container
 * @param theoplayerView the THEOplayer view to host in this container
 * @param color the background color for the overlay while showing the UI controls
 * @param centerOverlay content to show in the center of the player, typically a [LoadingSpinner].
 * @param errorOverlay content to show when the player encountered a fatal error,
 * typically an [ErrorDisplay].
 * @param topChrome controls to show at the top of the player, for example the stream's title.
 * @param centerChrome controls to show in the center of the player, for example a large [PlayButton].
 * @param bottomChrome controls to show at the bottom of the player, for example a [SeekBar]
 * or a [Row] containing a [MuteButton] and a [FullscreenButton].
 */
@OptIn(ExperimentalPlayerStateApi::class)
@Composable
fun UIController(
    modifier: Modifier = Modifier,
    theoplayerView: THEOplayerView,
    color: Color = Color.Black.copy(alpha = 0.5f),
    centerOverlay: (@Composable UIControllerScope.() -> Unit)? = null,
    errorOverlay: (@Composable UIControllerScope.() -> Unit)? = null,
    topChrome: (@Composable UIControllerScope.() -> Unit)? = null,
    centerChrome: (@Composable UIControllerScope.() -> Unit)? = null,
    bottomChrome: (@Composable UIControllerScope.() -> Unit)? = null
) {
    UIControllerImpl(
        modifier = modifier,
        theoplayerView = theoplayerView.takeUnless { it.isInEditMode },
        player = null,
        color = color,
        centerOverlay = centerOverlay,
        errorOverlay = errorOverlay,
        topChrome = topChrome,
        centerChrome = centerChrome,
        bottomChrome = bottomChrome
    )
}

@OptIn(ExperimentalPlayerStateApi::class)
@Composable
internal fun UIControllerImpl(
    modifier: Modifier,
    theoplayerView: THEOplayerView?,
    player: Player?,
    color: Color,
    centerOverlay: (@Composable UIControllerScope.() -> Unit)?,
    errorOverlay: (@Composable UIControllerScope.() -> Unit)?,
    topChrome: (@Composable UIControllerScope.() -> Unit)?,
    centerChrome: (@Composable UIControllerScope.() -> Unit)?,
    bottomChrome: (@Composable UIControllerScope.() -> Unit)?
) {
    val nativePlayer = theoplayerView?.player
    val firstPlayState = rememberFirstPlayState(nativePlayer)
    val castState = rememberCastState(theoplayerView)
    val playButtonState = rememberPlayButtonState(nativePlayer)
    val pictureInPictureState = rememberPictureInPictureState(theoplayerView)
    val errorState = rememberErrorState(nativePlayer, player?.error)
    val videoSizeState = rememberVideoSizeState(nativePlayer)
    val scope = rememberUIControllerScope(theoplayerView, player)
    val uiState = rememberUIState(scope, errorState, pictureInPictureState)
    val controls = rememberControlsVisibility(
        firstPlay = firstPlayState.firstPlay,
        paused = playButtonState.paused,
        forceVisible = castState.castState == PlayerCastState.CONNECTED,
        hideDelay = THEOplayerTheme.playerAnimations.controlsHideDelay
    )
    var playerWidth by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current

    UIControllerCore(
        modifier = modifier
            .playerAspectRatio(videoSizeState)
            .onSizeChanged { playerWidth = with(density) { it.width.toDp() } }
            .toggleControlsOnTap(
                controlsVisible = controls.visible,
                showControlsTemporarily = controls.show,
                hideControls = controls.hide
            ),
        theoplayerView = theoplayerView,
        player = player,
        scope = scope,
        uiState = uiState,
        errorState = errorState,
        controls = controls,
        color = color,
        contentTransform = mobileContentTransform(
            isWide = { playerWidth >= menuSheetBreakpoint },
            layoutDirection = layoutDirection
        ),
        menuContainer = { menuContent ->
            MenuSheet(
                onDismiss = { scope.closeCurrentMenu() },
                content = menuContent
            )
        },
        centerOverlay = centerOverlay,
        errorOverlay = errorOverlay,
        topChrome = topChrome,
        centerChrome = centerChrome,
        bottomChrome = bottomChrome
    )
}

/**
 * Returns the mobile menu transitions.
 */
private fun mobileContentTransform(
    isWide: () -> Boolean,
    layoutDirection: LayoutDirection
): AnimatedContentTransitionScope<UIState>.() -> ContentTransform = {
    if (targetState is UIState.Error) {
        EnterTransition.None togetherWith ExitTransition.None
    } else if (initialState is UIState.Menu && targetState is UIState.Menu) {
        // Menus are drawn inside one shared sheet: only their contents animate.
        EnterTransition.None togetherWith ExitTransition.None
    } else if (targetState is UIState.Menu) {
        if (isWide()) {
            val ltr = layoutDirection == LayoutDirection.Ltr
            slideInHorizontally { if (ltr) it else -it } + fadeIn() togetherWith fadeOut()
        } else {
            slideInVertically { it } + fadeIn() togetherWith fadeOut()
        }
    } else if (initialState is UIState.Menu) {
        if (isWide()) {
            val ltr = layoutDirection == LayoutDirection.Ltr
            fadeIn() togetherWith slideOutHorizontally { if (ltr) it else -it } + fadeOut()
        } else {
            fadeIn() togetherWith slideOutVertically { it } + fadeOut()
        }
    } else {
        EnterTransition.None togetherWith ExitTransition.None
    }
}

/**
 * Creates and remembers a [Player].
 *
 * @param config the player configuration
 */
@Composable
fun rememberPlayer(config: THEOplayerConfig? = null): Player {
    val theoplayerView = if (LocalInspectionMode.current) {
        null
    } else {
        val context = LocalContext.current
        remember { THEOplayerView(context, config) }
    }

    DisposableEffect(theoplayerView) {
        onDispose {
            theoplayerView?.onDestroy()
        }
    }

    return rememberPlayerInternal(theoplayerView)
}

/**
 * Create a [Player] wrapping an existing [THEOplayerView].
 *
 * The [THEOplayerView] should be [remembered][remember] so it's not re-created on every
 * recomposition.
 *
 * Example usage:
 * ```kotlin
 * val context = LocalContext.current
 * val theoplayerView = remember(context) {
 *   val config = THEOplayerConfig.Builder().build()
 *   THEOplayerView(context, config)
 * }
 * val player = rememberPlayer(theoplayerView)
 * ```
 *
 * This couples the lifecycle of the given [THEOplayerView] to the current activity.
 * That is, it automatically calls [THEOplayerView.onPause] and [THEOplayerView.onResume]
 * whenever the current activity is [paused][Activity.onPause] or [resumed][Activity.onResume].
 *
 * The [THEOplayerView] is **not** automatically destroyed when the composition is disposed.
 * If you need this, use a [DisposableEffect]:
 * ```kotlin
 * val player = rememberPlayer(theoplayerView)
 * DisposableEffect(theoplayerView) {
 *     onDispose {
 *         theoplayerView.onDestroy()
 *     }
 * }
 * ```
 *
 * @param theoplayerView the existing THEOplayer view
 */
@Composable
fun rememberPlayer(theoplayerView: THEOplayerView): Player {
    return rememberPlayerInternal(theoplayerView)
}

@Composable
internal fun rememberPlayerInternal(theoplayerView: THEOplayerView?): Player {
    theoplayerView?.let { setupTHEOplayerView(it) }

    val player = remember(theoplayerView) { PlayerImpl(theoplayerView) }
    DisposableEffect(player) {
        onDispose {
            player.dispose()
        }
    }

    player.activityInPipMode = rememberIsInPipMode()

    return player
}

internal fun Modifier.playerAspectRatio(videoSizeState: VideoSizeState): Modifier {
    return this.constrainedAspectRatio(
        if (videoSizeState.videoWidth != 0 && videoSizeState.videoHeight != 0) {
            videoSizeState.videoWidth.toFloat() / videoSizeState.videoHeight.toFloat()
        } else {
            16f / 9f
        }
    )
}
