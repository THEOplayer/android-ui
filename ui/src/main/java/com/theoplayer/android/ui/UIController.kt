package com.theoplayer.android.ui

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.ContextWrapper
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
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
import com.theoplayer.android.ui.util.rememberIsInPipMode
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
    val adState = rememberAdState(nativePlayer)
    val videoSizeState = rememberVideoSizeState(nativePlayer)

    var tapCount by remember { mutableIntStateOf(0) }
    var isRecentlyTapped by remember { mutableStateOf(false) }
    LaunchedEffect(tapCount) {
        if (tapCount > 0) {
            isRecentlyTapped = true
            delay(2.seconds)
            isRecentlyTapped = false
        }
    }
    var forceControlsHidden by remember { mutableStateOf(false) }

    // Wait a little bit before showing the controls and enabling animations,
    // so we can hide the UI immediately in case of autoplay.
    var isReady by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(50.milliseconds)
        isReady = true
    }

    val controlsVisible = remember(firstPlayState, castState, playButtonState) {
        derivedStateOf {
            if (!isReady) {
                false
            } else if (!firstPlayState.firstPlay || castState.castState == PlayerCastState.CONNECTED) {
                true
            } else if (forceControlsHidden) {
                false
            } else {
                isRecentlyTapped || playButtonState.paused
            }
        }
    }

    val scope = remember(theoplayerView, player) { UIControllerScopeImpl(theoplayerView, player) }
    if (player == null) {
        val isInPipMode = rememberIsInPipMode()
        SideEffect { scope.activityInPipMode = isInPipMode }
    }
    DisposableEffect(scope) {
        onDispose { scope.dispose() }
    }

    val uiState by remember(scope, pictureInPictureState, errorState) {
        derivedStateOf {
            val currentMenu = scope.currentMenu
            when {
                pictureInPictureState.pictureInPicture -> UIState.Hidden
                errorState.error != null -> UIState.Error
                currentMenu != null -> UIState.Menu(currentMenu)
                else -> UIState.Controls
            }
        }
    }
    val backgroundVisible = when (uiState) {
        is UIState.Controls -> controlsVisible.value
        is UIState.Hidden -> false
        else -> true
    }
    val background by animateColorAsState(
        label = "BackgroundAnimation",
        targetValue = if (backgroundVisible) color else color.copy(alpha = 0f),
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

    PlayerContainer(
        theoplayerView = theoplayerView,
        modifier = Modifier
            .background(Color.Black)
            .then(modifier)
            .playerAspectRatio(videoSizeState)
            .toggleControlsOnTap(
                controlsVisible = controlsVisible,
                showControlsTemporarily = {
                    forceControlsHidden = false
                    tapCount++
                },
                hideControls = {
                    forceControlsHidden = true
                    tapCount++
                }
            )
    ) {
        CompositionLocalProvider(
            LocalPlayer provides player,
            LocalTHEOplayerView provides theoplayerView,
            LocalUIControllerErrorState provides errorState
        ) {
            AnimatedContent(
                label = "ContentAnimation",
                modifier = Modifier
                    .fillMaxSize()
                    .background(background),
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
                            animationsActive = isReady && !adState.playingAd,
                            centerOverlay = centerOverlay,
                            topChrome = topChrome,
                            centerChrome = centerChrome,
                            bottomChrome = bottomChrome
                        )
                    }

                    is UIState.Hidden -> {}
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
     *
     * If the UIController was created from a [THEOplayerView], the [Player] is created on
     * first access (and only then adds its listeners). Prefer [theoplayerView] with the
     * experimental state holders instead.
     */
    val player: Player

    /**
     * The THEOplayer view hosted in this [UIController], or `null` in inspection mode.
     */
    val theoplayerView: THEOplayerView?
        get() = player.theoplayerView
}

private class UIControllerScopeImpl(
    override val theoplayerView: THEOplayerView?,
    private val providedPlayer: Player?
) : UIControllerScope {
    private var menuStack = mutableStateListOf<MenuContent>()

    val currentMenu: MenuContent?
        get() = menuStack.lastOrNull()
    var lastWasClosed: Boolean = false
        private set

    var activityInPipMode: Boolean = false
        set(value) {
            field = value
            if (lazyPlayer.isInitialized()) {
                lazyPlayer.value.activityInPipMode = value
            }
        }

    private val lazyPlayer = lazy {
        PlayerImpl(theoplayerView).also { it.activityInPipMode = activityInPipMode }
    }

    override val player: Player
        get() = providedPlayer ?: lazyPlayer.value

    fun dispose() {
        if (lazyPlayer.isInitialized()) {
            lazyPlayer.value.dispose()
        }
    }

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

internal val LocalUIControllerErrorState = staticCompositionLocalOf<ErrorState?> { null }

private sealed class UIState {
    object Error : UIState()
    data class Menu(val menu: MenuContent) : UIState()
    object Controls : UIState()
    object Hidden
}

@Composable
private fun PlayerContainer(
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
 * Creates and remembers a [THEOplayerView] owned by the composition.
 *
 * This couples the lifecycle of the view to the current activity: it automatically calls
 * [THEOplayerView.onPause] and [THEOplayerView.onResume] whenever the current activity is
 * [paused][Activity.onPause] or [resumed][Activity.onResume] (resuming an ad that was
 * interrupted), and it destroys the view when it leaves the composition.
 *
 * In previews, the SDK does not initialize the view ([View.isInEditMode]), so do not
 * access its [THEOplayerView.player] there. [UIController] and [DefaultUI] handle this
 * automatically.
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
        DisposableEffect(theoplayerView) {
            onDispose {
                theoplayerView.onDestroy()
            }
        }
    }
    return theoplayerView
}

/**
 * A context for creating a [THEOplayerView] in a preview.
 *
 * The [THEOplayerView] constructor casts the application context to [Application],
 * but layoutlib's application context is not an [Application].
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

@Composable
internal fun rememberTHEOplayerViewInternal(config: THEOplayerConfig?): THEOplayerView? {
    return if (LocalInspectionMode.current) null else rememberTHEOplayerView(config)
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

@Composable
internal fun setupTHEOplayerView(
    theoplayerView: THEOplayerView,
    destroyOnLifecycleDestroy: Boolean = true
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
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }

    return theoplayerView
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
