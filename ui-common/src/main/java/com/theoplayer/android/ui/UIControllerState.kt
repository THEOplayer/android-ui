package com.theoplayer.android.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import com.theoplayer.android.api.THEOplayerView
import com.theoplayer.android.ui.util.rememberIsInPipMode

/** The active content state for a player UI. */
@InternalUiApi
sealed class UIState {
    data object Error : UIState()

    data class Menu(val menu: MenuContent) : UIState()

    data object Controls : UIState()

    data object Hidden : UIState()
}

/** Returns whether this state should be treated as visible. */
@InternalUiApi
fun UIState.isVisible(controlsVisible: Boolean): Boolean =
    when (this) {
        is UIState.Controls -> controlsVisible
        is UIState.Hidden -> false
        else -> true
    }

/** Remembers the UI controller scope and disposes it with the composition. */
@InternalUiApi
@Composable
fun rememberUIControllerScope(
    theoplayerView: THEOplayerView?,
    player: Player?,
): UIControllerScopeImpl {
    val scope = remember(theoplayerView, player) { UIControllerScopeImpl(theoplayerView, player) }
    if (player == null) {
        val isInPipMode = rememberIsInPipMode()
        SideEffect { scope.activityInPipMode = isInPipMode }
    }
    DisposableEffect(scope) { onDispose { scope.dispose() } }
    return scope
}

/** Derives the active UI state from menus, errors, and picture-in-picture mode. */
@InternalUiApi
@Composable
fun rememberUIState(
    scope: UIControllerScopeImpl,
    errorState: ErrorState,
    pictureInPictureState: PictureInPictureState?,
): State<UIState> =
    remember(scope, errorState, pictureInPictureState) {
        derivedStateOf {
            val currentMenu = scope.currentMenu
            when {
                pictureInPictureState?.pictureInPicture == true -> UIState.Hidden
                errorState.error != null -> UIState.Error
                currentMenu != null -> UIState.Menu(currentMenu)
                else -> UIState.Controls
            }
        }
    }
