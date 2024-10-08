package com.theoplayer.android.ui

import android.app.Activity
import android.view.ViewGroup
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.theoplayer.android.api.THEOplayerConfig
import com.theoplayer.android.api.THEOplayerView
import com.theoplayer.android.api.cast.chromecast.PlayerCastState
import com.theoplayer.android.api.source.SourceDescription
import com.theoplayer.android.ui.theme.THEOplayerTheme
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

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
 * @param interactionSource the [MutableInteractionSource] representing the stream of [Interaction]s
 * for this container. You can create and pass in your own `remember`ed instance to observe
 * [Interaction]s and customize the behavior of this container.
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
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    color: Color = Color.Black,
    centerOverlay: (@Composable UIControllerScope.() -> Unit)? = null,
    errorOverlay: (@Composable UIControllerScope.() -> Unit)? = null,
    topChrome: (@Composable UIControllerScope.() -> Unit)? = null,
    centerChrome: (@Composable UIControllerScope.() -> Unit)? = null,
    bottomChrome: (@Composable UIControllerScope.() -> Unit)? = null
) {
    val player = rememberPlayer(config)
    LaunchedEffect(player, source) {
        player.source = source
    }

    UIController(
        modifier = modifier,
        player = player,
        interactionSource = interactionSource,
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
 * @param interactionSource the [MutableInteractionSource] representing the stream of [Interaction]s
 * for this container. You can create and pass in your own `remember`ed instance to observe
 * [Interaction]s and customize the behavior of this container.
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
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    color: Color = Color.Black,
    centerOverlay: (@Composable UIControllerScope.() -> Unit)? = null,
    errorOverlay: (@Composable UIControllerScope.() -> Unit)? = null,
    topChrome: (@Composable UIControllerScope.() -> Unit)? = null,
    centerChrome: (@Composable UIControllerScope.() -> Unit)? = null,
    bottomChrome: (@Composable UIControllerScope.() -> Unit)? = null
) {
    var tapCount by remember { mutableStateOf(0) }
    var isRecentlyTapped by remember { mutableStateOf(false) }
    LaunchedEffect(tapCount) {
        if (tapCount > 0) {
            isRecentlyTapped = true
            delay(2.seconds)
            isRecentlyTapped = false
        }
    }
    val isPressed by interactionSource.collectIsPressedAsState()
    var forceControlsHidden by remember { mutableStateOf(false) }

    // Wait a little bit before showing the controls and enabling animations,
    // so we can hide the UI immediately in case of autoplay.
    var isReady by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(50.milliseconds)
        isReady = true
    }

    val controlsVisible = remember {
        derivedStateOf {
            if (!isReady) {
                false
            } else if (!player.firstPlay || player.castState == PlayerCastState.CONNECTED) {
                true
            } else if (player.playingAd) {
                false
            } else if (forceControlsHidden) {
                false
            } else {
                isRecentlyTapped || isPressed || player.paused
            }
        }
    }

    val scope = remember(player) { UIControllerScopeImpl(player) }

    val uiState by remember {
        derivedStateOf {
            val currentMenu = scope.currentMenu
            if (player.error != null) {
                UIState.Error
            } else if (currentMenu != null) {
                UIState.Menu(currentMenu)
            } else {
                UIState.Controls
            }
        }
    }
    val backgroundVisible = if (uiState is UIState.Controls) {
        controlsVisible.value
    } else {
        true
    }
    val background by animateColorAsState(
        label = "BackgroundAnimation",
        targetValue = color.copy(alpha = if (backgroundVisible) 0.5f else 0f),
        animationSpec = tween(
            easing = LinearEasing,
            durationMillis = if (backgroundVisible) {
                THEOplayerTheme.playerAnimations.controlsEnterDuration.toInt(
                    DurationUnit.MILLISECONDS
                )
            } else {
                THEOplayerTheme.playerAnimations.controlsExitDuration.toInt(
                    DurationUnit.MILLISECONDS
                )
            }
        )
    )

    PlayerContainer(modifier = modifier, player = player) {
        CompositionLocalProvider(LocalPlayer provides player) {
            if (player.playingAd) {
                // Remove player UI entirely while playing an ad, to make clickthrough work
                return@CompositionLocalProvider
            }
            AnimatedContent(
                label = "ContentAnimation",
                modifier = Modifier
                    .background(background)
                    .pressable(interactionSource = interactionSource, requireUnconsumed = false)
                    .toggleControlsOnTap(
                        controlsVisible = controlsVisible,
                        showControlsTemporarily = {
                            forceControlsHidden = false
                            tapCount++
                        },
                        hideControls = {
                            forceControlsHidden = true
                            tapCount++
                        }),
                targetState = uiState,
                transitionSpec = {
                    if (targetState is UIState.Error) {
                        // Show errors immediately
                        EnterTransition.None togetherWith ExitTransition.None
                    } else if (initialState is UIState.Menu && targetState is UIState.Menu) {
                        if (scope.lastWasClosed) {
                            // Close menu towards the right
                            slideInHorizontally { -it } togetherWith
                                    slideOutHorizontally { it }
                        } else {
                            // Open new menu towards the left
                            slideInHorizontally(initialOffsetX = { it }) togetherWith
                                    slideOutHorizontally(targetOffsetX = { -it })
                        }
                    } else if (targetState is UIState.Menu) {
                        // Open first menu from the bottom
                        slideInVertically { it / 4 } + fadeIn() togetherWith fadeOut()
                    } else if (initialState is UIState.Menu) {
                        // Close last menu towards the bottom
                        fadeIn() togetherWith slideOutVertically { it / 4 } + fadeOut()
                    } else {
                        EnterTransition.None togetherWith ExitTransition.None
                    }
                }
            ) { uiState ->
                when (uiState) {
                    is UIState.Error -> {
                        errorOverlay?.let { scope.it() }
                    }

                    is UIState.Menu -> {
                        uiState.menu.let { scope.it() }
                    }

                    is UIState.Controls -> {
                        scope.PlayerControls(
                            controlsVisible = controlsVisible.value,
                            animationsActive = isReady && !player.playingAd,
                            centerOverlay = centerOverlay,
                            topChrome = topChrome,
                            centerChrome = centerChrome,
                            bottomChrome = bottomChrome
                        )
                    }
                }
            }
        }
    }
}

/**
 * Scope for the contents of a [UIController].
 */
interface UIControllerScope : MenuScope {
    /**
     * The player hosted in this [UIController].
     */
    val player: Player
}

private class UIControllerScopeImpl(override val player: Player) :
    UIControllerScope {
    private var menuStack = mutableStateListOf<MenuContent>()

    val currentMenu: MenuContent?
        get() = menuStack.lastOrNull()
    var lastWasClosed: Boolean = false
        private set

    override fun openMenu(menu: MenuContent) {
        menuStack.add(menu)
        lastWasClosed = false
    }

    override fun closeCurrentMenu() {
        menuStack.removeLastOrNull()?.also {
            lastWasClosed = true
        }
    }
}

private sealed class UIState {
    object Error : UIState()
    data class Menu(val menu: MenuContent) : UIState()
    object Controls : UIState()
}

@Composable
private fun PlayerContainer(
    modifier: Modifier = Modifier,
    player: Player,
    ui: @Composable () -> Unit
) {
    val theoplayerView = player.theoplayerView
    val containerModifier = Modifier
        .background(Color.Black)
        .then(modifier)
        .playerAspectRatio(player)
    if (theoplayerView == null) {
        Box(
            modifier = containerModifier
        ) {
            ui()
        }
    } else {
        val lifecycle = LocalLifecycleOwner.current.lifecycle
        var uiContainer by remember { mutableStateOf<ViewGroup?>(null) }
        var composeView by remember { mutableStateOf<ComposeView?>(null) }

        AndroidView(
            modifier = containerModifier,
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
                    theoplayerView.postInvalidate()
                }
            }
        }
    }
}

@Composable
private fun UIControllerScope.PlayerControls(
    controlsVisible: Boolean,
    animationsActive: Boolean,
    centerOverlay: (@Composable UIControllerScope.() -> Unit)? = null,
    topChrome: (@Composable UIControllerScope.() -> Unit)? = null,
    centerChrome: (@Composable UIControllerScope.() -> Unit)? = null,
    bottomChrome: (@Composable UIControllerScope.() -> Unit)? = null
) {
    centerOverlay?.let {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            it()
        }
    }
    AnimatedVisibility(
        visible = controlsVisible,
        enter = if (animationsActive) fadeIn(
            animationSpec = tween(
                easing = LinearEasing,
                durationMillis = THEOplayerTheme.playerAnimations.controlsEnterDuration.toInt(
                    DurationUnit.MILLISECONDS
                )
            )
        ) else EnterTransition.None,
        exit = if (animationsActive) fadeOut(
            animationSpec = tween(
                easing = LinearEasing,
                durationMillis = THEOplayerTheme.playerAnimations.controlsExitDuration.toInt(
                    DurationUnit.MILLISECONDS
                )
            )
        ) else ExitTransition.None,
    ) {
        centerChrome?.let {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                it()
            }
        }
        Column(modifier = Modifier.fillMaxSize()) {
            topChrome?.let { it() }
            Spacer(modifier = Modifier.weight(1f))
            bottomChrome?.let { it() }
        }
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

    return player
}

@Composable
internal fun setupTHEOplayerView(theoplayerView: THEOplayerView): THEOplayerView {
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

internal fun Modifier.playerAspectRatio(player: Player): Modifier {
    return this.constrainedAspectRatio(
        if (player.videoWidth != 0 && player.videoHeight != 0) {
            player.videoWidth.toFloat() / player.videoHeight.toFloat()
        } else {
            16f / 9f
        }
    )
}
