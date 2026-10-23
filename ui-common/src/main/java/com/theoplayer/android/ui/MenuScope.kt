package com.theoplayer.android.ui

import androidx.compose.runtime.Composable

/** The content for a new menu, typically this should draw a single [Menu]. */
typealias MenuContent = @Composable MenuScope.() -> Unit

/**
 * Scope for the contents of a [Menu].
 *
 * This interface is implemented by `UIController`; implementing it yourself is not supported.
 */
@OptIn(ExperimentalSubclassOptIn::class)
@SubclassOptInRequired(InternalUiApi::class)
interface MenuScope {
    /**
     * Open a new submenu.
     *
     * When the submenu is closed, the original menu is shown again.
     *
     * Only the top-most menu is composed. While the submenu is open, the original menu (or the
     * player controls, if no menu was open yet) leaves the composition, and any state remembered
     * there with `remember` is disposed — state remembered with `rememberSaveable` (such as scroll
     * positions) is restored when the submenu closes. So instead of passing state from the caller
     * into [menu], remember the state inside [menu]:
     * ```kotlin
     * // Don't: audioTrackState is disposed while the menu is open
     * val audioTrackState = rememberAudioTrackState(player)
     * openMenu { AudioTrackMenu(audioTrackState) }
     *
     * // Do
     * openMenu { AudioTrackMenu(rememberAudioTrackState(player)) }
     * ```
     */
    fun openMenu(menu: MenuContent)

    /** Close the currently open menu, returning to the previous menu (if any). */
    fun closeCurrentMenu()

    /** Close all open menus, including the current menu and all menus below it. */
    fun closeAllMenus()
}
