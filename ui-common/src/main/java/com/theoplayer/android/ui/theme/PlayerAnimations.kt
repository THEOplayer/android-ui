package com.theoplayer.android.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import com.theoplayer.android.ui.InternalUiApi
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/** Animation settings used by THEOplayer UI components. */
@Immutable
data class PlayerAnimations(
    /** The animation duration when fading in the player controls. */
    val controlsEnterDuration: Duration = 0.milliseconds,
    /** The animation duration when fading out the player controls. */
    val controlsExitDuration: Duration = 500.milliseconds,
    /** The delay for a loading spinner to become visible after the player starts buffering. */
    val loadingSpinnerDelay: Duration = 500.milliseconds,
    /** How long the controls stay visible after user interaction while playing. */
    val controlsHideDelay: Duration = 2.seconds,
    /**
     * The animation duration when switching between menus, such as when opening a submenu or going
     * back to the previous menu.
     */
    val menuTransitionDuration: Duration = 300.milliseconds,
)

/** Animation settings shared by player UI modules. */
@InternalUiApi val LocalPlayerAnimations = staticCompositionLocalOf { PlayerAnimations() }
