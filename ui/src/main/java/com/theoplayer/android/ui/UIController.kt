package com.theoplayer.android.ui

import android.view.ViewGroup
import androidx.compose.animation.*
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.theoplayer.android.api.THEOplayerConfig
import com.theoplayer.android.api.THEOplayerView
import com.theoplayer.android.api.source.SourceDescription
import kotlinx.coroutines.*
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

private val controlsExitDuration = 500.milliseconds

@OptIn(ExperimentalAnimationApi::class)
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
    val theoplayerView = if (LocalInspectionMode.current) {
        null
    } else {
        rememberTHEOplayerView(config)
    }
    val state = rememberPlayerState(theoplayerView)

    var tapCount by remember { mutableStateOf(0) }
    var isRecentlyTapped by remember { mutableStateOf(true) }
    LaunchedEffect(key1 = tapCount) {
        isRecentlyTapped = true
        delay(2.seconds)
        isRecentlyTapped = false
    }
    val isPressed by interactionSource.collectIsPressedAsState()
    var forceControlsHidden by remember { mutableStateOf(false) }
    val controlsVisible = remember {
        derivedStateOf {
            if (!state.firstPlay) {
                true
            } else if (forceControlsHidden) {
                false
            } else {
                isRecentlyTapped || isPressed || state.paused
            }
        }
    }

    val scope = remember { UIControllerScopeImpl() }

    val uiState by remember {
        derivedStateOf {
            val currentMenu = scope.currentMenu
            if (state.error != null) {
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
        targetValue = color.copy(alpha = if (backgroundVisible) 0.5f else 0f),
        animationSpec = if (backgroundVisible) snap(0) else tween(
            easing = LinearEasing,
            durationMillis = controlsExitDuration.toInt(
                DurationUnit.MILLISECONDS
            )
        )
    )

    PlayerContainer(modifier = modifier, theoplayerView = theoplayerView) {
        CompositionLocalProvider(LocalTHEOplayer provides state) {
            AnimatedContent(
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
                        EnterTransition.None with ExitTransition.None
                    } else if (initialState is UIState.Menu && targetState is UIState.Menu) {
                        if (scope.lastWasClosed) {
                            // Close menu towards the right
                            slideInHorizontally { -it } with
                                    slideOutHorizontally { it }
                        } else {
                            // Open new menu towards the left
                            slideInHorizontally(initialOffsetX = { it }) with
                                    slideOutHorizontally(targetOffsetX = { -it })
                        }
                    } else if (targetState is UIState.Menu) {
                        // Open first menu from the bottom
                        slideInVertically { it / 4 } + fadeIn() with fadeOut()
                    } else if (initialState is UIState.Menu) {
                        // Close last menu towards the bottom
                        fadeIn() with slideOutVertically { it / 4 } + fadeOut()
                    } else {
                        EnterTransition.None with ExitTransition.None
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

    LaunchedEffect(key1 = source) {
        state.player?.source = source
    }
}

interface UIControllerScope : MenuScope {
}

private class UIControllerScopeImpl() :
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
    theoplayerView: THEOplayerView? = null,
    ui: @Composable () -> Unit
) {
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
private fun UIControllerScope.PlayerControls(
    controlsVisible: Boolean,
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
        enter = EnterTransition.None,
        exit = fadeOut(
            animationSpec = tween(
                easing = LinearEasing,
                durationMillis = controlsExitDuration.toInt(DurationUnit.MILLISECONDS)
            )
        )
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

@Composable
private fun rememberTHEOplayerView(config: THEOplayerConfig): THEOplayerView {
    val context = LocalContext.current
    val theoplayerView = remember { THEOplayerView(context, config) }

    DisposableEffect(key1 = theoplayerView) {
        onDispose {
            theoplayerView.onDestroy()
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

private fun Modifier.toggleControlsOnTap(
    controlsVisible: State<Boolean>,
    showControlsTemporarily: () -> Unit,
    hideControls: () -> Unit
): Modifier {
    return this.pointerInput(Unit) {
        coroutineScope {
            var didHideControls = false
            launch {
                detectTapGestures(onPress = {
                    didHideControls = false
                    // Hide controls immediately when pressed while visible
                    val controlsWereVisible = controlsVisible.value
                    awaitRelease()
                    if (controlsWereVisible) {
                        didHideControls = true
                        hideControls()
                    }
                })
            }
            launch {
                detectAnyPointerEvent(pass = PointerEventPass.Final) {
                    // Show controls temporarily when pressing, moving or releasing a pointer
                    // - except if we just hid the controls by pressing
                    if (didHideControls) {
                        didHideControls = false
                    } else {
                        showControlsTemporarily()
                    }
                }
            }
        }
    }
}

private suspend fun PointerInputScope.detectAnyPointerEvent(
    pass: PointerEventPass = PointerEventPass.Main,
    onPointer: () -> Unit
) {
    val currentContext = currentCoroutineContext()
    awaitPointerEventScope {
        while (currentContext.isActive) {
            awaitPointerEvent(pass = pass)
            onPointer()
        }
    }
}