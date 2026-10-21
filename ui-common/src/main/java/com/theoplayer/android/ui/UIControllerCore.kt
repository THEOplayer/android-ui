package com.theoplayer.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import com.theoplayer.android.api.THEOplayerView
import com.theoplayer.android.ui.theme.LocalPlayerAnimations
import kotlin.math.roundToInt
import kotlin.time.DurationUnit

/**
 * Renders the shared player UI around the active controls, menu, or error state.
 *
 * @param menuContainer the container that wraps all menus (e.g. a sheet). All menus are
 * rendered inside it, so opening a submenu does not trigger the outer content transition;
 * the menu contents animate with a push/pop transition inside the container instead.
 */
@InternalUiApi
@Composable
fun UIControllerCore(
    modifier: Modifier = Modifier,
    theoplayerView: THEOplayerView?,
    player: Player?,
    scope: UIControllerScopeImpl,
    uiState: State<UIState>,
    errorState: ErrorState,
    controls: ControlsVisibility,
    color: Color,
    contentTransform: AnimatedContentTransitionScope<UIState>.() -> ContentTransform,
    menuContainer: @Composable (menuContent: @Composable () -> Unit) -> Unit,
    controlsModifier: Modifier = Modifier,
    centerOverlay: (@Composable UIControllerScope.() -> Unit)? = null,
    errorOverlay: (@Composable UIControllerScope.() -> Unit)? = null,
    topChrome: (@Composable UIControllerScope.() -> Unit)? = null,
    centerChrome: (@Composable UIControllerScope.() -> Unit)? = null,
    bottomChrome: (@Composable UIControllerScope.() -> Unit)? = null
) {
    val nativePlayer = theoplayerView?.player
    val adState = rememberAdState(nativePlayer)
    val playerAnimations = LocalPlayerAnimations.current
    val backgroundVisible = uiState.value.isVisible(controls.visible.value)
    val animatedBackground by animateColorAsState(
        label = "BackgroundAnimation",
        targetValue = if (backgroundVisible) color else color.copy(alpha = 0f),
        animationSpec = tween(
            easing = LinearEasing,
            durationMillis = if (backgroundVisible) {
                playerAnimations.controlsEnterDuration.toInt(DurationUnit.MILLISECONDS)
            } else {
                playerAnimations.controlsExitDuration.toInt(DurationUnit.MILLISECONDS)
            }
        )
    )

    val chromeBounds = remember { ChromeBounds() }
    val chromeVisible = uiState.value == UIState.Controls && controls.visible.value
    TextTrackMarginsEffect(
        player = nativePlayer.takeUnless { theoplayerView?.isInEditMode == true },
        chromeMargins = if (chromeVisible) chromeBounds.margins else ChromeMargins()
    )

    BackHandler(enabled = scope.currentMenu != null) {
        scope.closeCurrentMenu()
    }

    PlayerContainer(
        theoplayerView = theoplayerView,
        modifier = Modifier
            .background(Color.Black)
            .then(modifier)
            .handlePlayerKeys(
                player = nativePlayer,
                controlsVisible = {
                    uiState.value.isVisible(controls.visible.value)
                },
                showControls = controls.show
            )
    ) {
        UIControllerContent(
            theoplayerView = theoplayerView,
            player = player,
            scope = scope,
            uiState = uiState.value,
            errorState = errorState,
            controls = controls,
            adPlaying = adState.playingAd,
            background = animatedBackground,
            chromeBounds = chromeBounds,
            controlsModifier = controlsModifier,
            contentTransform = contentTransform,
            menuContainer = menuContainer,
            centerOverlay = centerOverlay,
            errorOverlay = errorOverlay,
            topChrome = topChrome,
            centerChrome = centerChrome,
            bottomChrome = bottomChrome
        )
    }
}

/**
 * Provides controller locals and displays the active content.
 */
@Composable
private fun UIControllerContent(
    theoplayerView: THEOplayerView?,
    player: Player?,
    scope: UIControllerScopeImpl,
    uiState: UIState,
    errorState: ErrorState,
    controls: ControlsVisibility,
    adPlaying: Boolean,
    background: Color,
    chromeBounds: ChromeBounds,
    controlsModifier: Modifier,
    contentTransform: AnimatedContentTransitionScope<UIState>.() -> ContentTransform,
    menuContainer: @Composable (menuContent: @Composable () -> Unit) -> Unit,
    centerOverlay: (@Composable UIControllerScope.() -> Unit)?,
    errorOverlay: (@Composable UIControllerScope.() -> Unit)?,
    topChrome: (@Composable UIControllerScope.() -> Unit)?,
    centerChrome: (@Composable UIControllerScope.() -> Unit)?,
    bottomChrome: (@Composable UIControllerScope.() -> Unit)?
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
            transitionSpec = contentTransform,
            contentKey = { state ->
                if (state is UIState.Menu) {
                    // One stable content key while menus are open, so pushing a submenu
                    // does not animate the whole menu container.
                    menusOpenContentKey
                } else {
                    state
                }
            }
        ) { state ->
            when (state) {
                UIState.Error -> errorOverlay?.let { scope.it() }
                is UIState.Menu -> menuContainer { InnerMenus(scope) }
                UIState.Controls -> scope.PlayerControls(
                    controlsVisible = controls.visible.value,
                    animationsActive = controls.animationsActive && !adPlaying,
                    chromeBounds = chromeBounds,
                    controlsModifier = controlsModifier,
                    centerOverlay = centerOverlay,
                    topChrome = topChrome,
                    centerChrome = centerChrome,
                    bottomChrome = bottomChrome
                )
                UIState.Hidden -> Unit
            }
        }
    }
}

/**
 * Displays the center overlay and animated control slots.
 */
@Composable
private fun UIControllerScope.PlayerControls(
    controlsVisible: Boolean,
    animationsActive: Boolean,
    chromeBounds: ChromeBounds,
    controlsModifier: Modifier,
    centerOverlay: (@Composable UIControllerScope.() -> Unit)?,
    topChrome: (@Composable UIControllerScope.() -> Unit)?,
    centerChrome: (@Composable UIControllerScope.() -> Unit)?,
    bottomChrome: (@Composable UIControllerScope.() -> Unit)?
) {
    val playerAnimations = LocalPlayerAnimations.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { chromeBounds.player = it.boundsInRoot() }
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
                    durationMillis = playerAnimations.controlsEnterDuration.toInt(
                        DurationUnit.MILLISECONDS
                    )
                )
            ) else EnterTransition.None,
            exit = if (animationsActive) fadeOut(
                animationSpec = tween(
                    easing = LinearEasing,
                    durationMillis = playerAnimations.controlsExitDuration.toInt(
                        DurationUnit.MILLISECONDS
                    )
                )
            ) else ExitTransition.None,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(controlsModifier)
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
                    topChrome?.let { chrome ->
                        Column(
                            modifier = Modifier.onGloballyPositioned {
                                chromeBounds.topChrome = it.boundsInRoot()
                            }
                        ) { chrome() }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    bottomChrome?.let { chrome ->
                        Column(
                            modifier = Modifier.onGloballyPositioned {
                                chromeBounds.bottomChrome = it.boundsInRoot()
                            }
                        ) { chrome() }
                    }
                }
            }
        }
    }
}

private val menusOpenContentKey = UIState.Menu { }

/**
 * Renders the open menu stack, animating menu pushes and pops within the current
 * [menuContainer][UIControllerCore].
 */
@Composable
private fun InnerMenus(scope: UIControllerScopeImpl) {
    val layoutDirection = LocalLayoutDirection.current
    val slideDistance = with(LocalDensity.current) { SharedAxisSlideDistance.toPx() }.roundToInt()
    val durationMillis = LocalPlayerAnimations.current.menuTransitionDuration
        .inWholeMilliseconds.toInt()
    AnimatedContent(
        label = "InnerMenusAnimation",
        targetState = scope.currentMenu,
        transitionSpec = {
            sharedAxisX(
                forward = !scope.lastWasClosed,
                slideDistance = slideDistance,
                layoutDirection = layoutDirection,
                durationMillis = durationMillis
            )
        }
    ) { menu -> menu?.let { scope.it() } }
}

/**
 * The current error state provided by a UI controller.
 */
@InternalUiApi
val LocalUIControllerErrorState = staticCompositionLocalOf<ErrorState?> { null }
