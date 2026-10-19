package com.theoplayer.android.ui.tv

import android.view.KeyEvent as NativeKeyEvent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.focusGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import com.theoplayer.android.api.THEOplayerView
import com.theoplayer.android.api.player.Player
import com.theoplayer.android.ui.ControlsVisibility
import com.theoplayer.android.ui.ExperimentalPlayerStateApi
import com.theoplayer.android.ui.InternalUiApi
import com.theoplayer.android.ui.UIControllerCore
import com.theoplayer.android.ui.UIControllerScope
import com.theoplayer.android.ui.UIState
import com.theoplayer.android.ui.isVisible
import com.theoplayer.android.ui.rememberControlsVisibility
import com.theoplayer.android.ui.rememberErrorState
import com.theoplayer.android.ui.rememberFirstPlayState
import com.theoplayer.android.ui.rememberPlayButtonState
import com.theoplayer.android.ui.rememberUIControllerScope
import com.theoplayer.android.ui.rememberUIState
import com.theoplayer.android.ui.seekPlayerBy
import com.theoplayer.android.ui.theme.LocalPlayerAnimations
import com.theoplayer.android.ui.togglePlayerPlayPause

/**
 * A TV-oriented UI controller for a THEOplayer view.
 *
 * @param modifier the [Modifier] to be applied to this controller
 * @param theoplayerView the THEOplayer view to host
 * @param color the background color behind visible controls
 * @param centerOverlay content displayed over the center of the player
 * @param errorOverlay content displayed when the player encounters a fatal error
 * @param topChrome controls displayed at the top of the player
 * @param centerChrome controls displayed at the center of the player
 * @param bottomChrome controls displayed at the bottom of the player
 */
@ExperimentalTvUiApi
@OptIn(ExperimentalPlayerStateApi::class, InternalUiApi::class)
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
    val view = theoplayerView.takeUnless { it.isInEditMode }
    val nativePlayer = view?.player
    val firstPlayState = rememberFirstPlayState(nativePlayer)
    val playButtonState = rememberPlayButtonState(nativePlayer)
    val errorState = rememberErrorState(nativePlayer)
    val scope = rememberUIControllerScope(view, player = null)
    val uiState = rememberUIState(
        scope = scope,
        errorState = errorState,
        pictureInPictureState = null
    )
    val playerAnimations = LocalPlayerAnimations.current
    val controls = rememberControlsVisibility(
        firstPlay = firstPlayState.firstPlay,
        paused = playButtonState.paused,
        forceVisible = false,
        hideDelay = playerAnimations.controlsHideDelay
    )
    val focusRequester = remember { FocusRequester() }
    val currentUiState = uiState.value

    rememberControlsFocus(
        uiState = currentUiState,
        menuOpen = scope.currentMenu != null,
        controls = controls,
        focusRequester = focusRequester
    )
    BackHandler(
        enabled = currentUiState is UIState.Controls &&
                controls.visible.value && firstPlayState.firstPlay
    ) {
        controls.hide()
    }

    UIControllerCore(
        modifier = modifier.tvHiddenControlsKeys(
            player = nativePlayer,
            controlsVisible = { uiState.value.isVisible(controls.visible.value) },
            showControls = controls.show
        ),
        theoplayerView = view,
        player = null,
        scope = scope,
        uiState = uiState,
        errorState = errorState,
        controls = controls,
        color = color,
        contentTransform = tvContentTransform(),
        controlsModifier = Modifier
            .focusRequester(focusRequester)
            .focusRestorer()
            .focusGroup(),
        centerOverlay = centerOverlay,
        errorOverlay = errorOverlay,
        topChrome = topChrome,
        centerChrome = centerChrome,
        bottomChrome = bottomChrome
    )
}

/**
 * Restores focus to controls when they become visible or a menu closes.
 */
@OptIn(InternalUiApi::class)
@Composable
private fun rememberControlsFocus(
    uiState: UIState,
    menuOpen: Boolean,
    controls: ControlsVisibility,
    focusRequester: FocusRequester
) {
    var menuWasOpen by remember { mutableStateOf(false) }
    LaunchedEffect(controls.visible.value, menuOpen, uiState) {
        if (uiState is UIState.Controls && !menuOpen &&
            (controls.visible.value || menuWasOpen)
        ) {
            focusRequester.requestFocus()
        }
        menuWasOpen = menuOpen
    }
}

/**
 * Returns fade-only transitions for TV content.
 */
private fun tvContentTransform(): AnimatedContentTransitionScope<UIState>.() -> ContentTransform = {
    when {
        targetState is UIState.Error ->
            EnterTransition.None togetherWith ExitTransition.None
        targetState is UIState.Menu || initialState is UIState.Menu ->
            fadeIn() togetherWith fadeOut()
        else ->
            EnterTransition.None togetherWith ExitTransition.None
    }
}

/**
 * Handles TV transport keys while controls are hidden.
 */
@OptIn(InternalUiApi::class)
private fun Modifier.tvHiddenControlsKeys(
    player: Player?,
    controlsVisible: () -> Boolean,
    showControls: () -> Unit
): Modifier = onPreviewKeyEvent { event ->
    if (controlsVisible() || event.type != KeyEventType.KeyDown) {
        return@onPreviewKeyEvent false
    }

    when (event.nativeKeyEvent.keyCode) {
        NativeKeyEvent.KEYCODE_DPAD_CENTER,
        NativeKeyEvent.KEYCODE_ENTER -> {
            showControls()
            togglePlayerPlayPause(player)
            true
        }
        NativeKeyEvent.KEYCODE_DPAD_LEFT -> {
            showControls()
            seekPlayerBy(player, -SEEK_OFFSET_SECONDS)
            true
        }
        NativeKeyEvent.KEYCODE_DPAD_RIGHT -> {
            showControls()
            seekPlayerBy(player, SEEK_OFFSET_SECONDS)
            true
        }
        else -> false
    }
}

private const val SEEK_OFFSET_SECONDS = 10
