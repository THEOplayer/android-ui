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
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.theoplayer.android.api.THEOplayerView
import com.theoplayer.android.ui.theme.LocalPlayerAnimations
import kotlin.time.DurationUnit

/**
 * Renders the shared player UI around the active controls, menu, or error state.
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
            controlsModifier = controlsModifier,
            contentTransform = contentTransform,
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
    controlsModifier: Modifier,
    contentTransform: AnimatedContentTransitionScope<UIState>.() -> ContentTransform,
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
            transitionSpec = contentTransform
        ) { state ->
            when (state) {
                UIState.Error -> errorOverlay?.let { scope.it() }
                is UIState.Menu -> state.menu.let { scope.it() }
                UIState.Controls -> scope.PlayerControls(
                    controlsVisible = controls.visible.value,
                    animationsActive = controls.animationsActive && !adPlaying,
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
    controlsModifier: Modifier,
    centerOverlay: (@Composable UIControllerScope.() -> Unit)?,
    topChrome: (@Composable UIControllerScope.() -> Unit)?,
    centerChrome: (@Composable UIControllerScope.() -> Unit)?,
    bottomChrome: (@Composable UIControllerScope.() -> Unit)?
) {
    val playerAnimations = LocalPlayerAnimations.current
    Box(modifier = Modifier.fillMaxSize()) {
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
                    topChrome?.let { it() }
                    Spacer(modifier = Modifier.weight(1f))
                    bottomChrome?.let { it() }
                }
            }
        }
    }
}

/**
 * The current error state provided by a UI controller.
 */
@InternalUiApi
val LocalUIControllerErrorState = staticCompositionLocalOf<ErrorState?> { null }
