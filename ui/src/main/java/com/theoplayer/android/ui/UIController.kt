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
import com.theoplayer.android.api.source.TypedSource
import kotlinx.coroutines.*
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

val controlsExitDuration = 500.milliseconds

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun UIController(
    config: THEOplayerConfig,
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    centerOverlay: (@Composable UIControllerScope.() -> Unit)? = null,
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

    val backgroundVisible by remember { derivedStateOf { controlsVisible.value || scope.currentMenu != null } }
    val background by animateColorAsState(
        targetValue = Color.Black.copy(alpha = if (backgroundVisible) 0.5f else 0f),
        animationSpec = if (backgroundVisible) snap(0) else tween(
            easing = LinearEasing,
            durationMillis = controlsExitDuration.toInt(
                DurationUnit.MILLISECONDS
            )
        )
    )

    PlayerContainer(modifier = modifier, theoplayerView = theoplayerView) {
        CompositionLocalProvider(LocalTHEOplayer provides state) {
            Box(
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
                        })
            ) {
                AnimatedContent(
                    targetState = scope.currentMenu,
                    transitionSpec = {
                        if (initialState == null) {
                            // Open first menu from the bottom
                            slideInVertically { it / 4 } + fadeIn() with ExitTransition.None
                        } else if (targetState == null) {
                            // Close last menu towards the bottom
                            EnterTransition.None with slideOutVertically { it / 4 } + fadeOut()
                        } else if (scope.lastWasClosed) {
                            // Close menu towards the right
                            slideInHorizontally { -it } with
                                    slideOutHorizontally { it }
                        } else {
                            // Open new menu towards the left
                            slideInHorizontally(initialOffsetX = { it }) with
                                    slideOutHorizontally(targetOffsetX = { -it })
                        }
                    }
                ) { menu ->
                    if (menu == null) {
                        scope.PlayerControls(
                            controlsVisible = controlsVisible.value,
                            centerOverlay = centerOverlay,
                            topChrome = topChrome,
                            centerChrome = centerChrome,
                            bottomChrome = bottomChrome
                        )
                    } else {
                        scope.menu()
                    }
                }
            }
        }
    }
}

interface UIControllerScope : MenuScope {
}

internal class UIControllerScopeImpl() :
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

@Composable
internal fun PlayerContainer(
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
internal fun UIControllerScope.PlayerControls(
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
fun rememberTHEOplayerView(config: THEOplayerConfig): THEOplayerView {
    val context = LocalContext.current
    val theoplayerView = remember {
        THEOplayerView(context, config).apply {
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

internal fun Modifier.toggleControlsOnTap(
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

internal suspend fun PointerInputScope.detectAnyPointerEvent(
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