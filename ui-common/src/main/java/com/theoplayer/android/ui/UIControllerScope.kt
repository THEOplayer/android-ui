package com.theoplayer.android.ui

import androidx.compose.runtime.mutableStateListOf
import com.theoplayer.android.api.THEOplayerView

/**
 * Scope for the contents of a [UIController].
 *
 * This interface is implemented by `UIController`; implementing it yourself is not supported.
 */
@OptIn(ExperimentalSubclassOptIn::class)
@SubclassOptInRequired(InternalUiApi::class)
interface UIControllerScope : MenuScope {
    /**
     * The player hosted in this [UIController].
     *
     * If the UIController was created from a [THEOplayerView], the [Player] is created on first
     * access (and only then adds its listeners). Prefer [theoplayerView] with the experimental
     * state holders instead.
     */
    val player: Player

    /** The THEOplayer view hosted in this [UIController], or `null` in inspection mode. */
    val theoplayerView: THEOplayerView?
        get() = player.theoplayerView
}

/** Default implementation of [UIControllerScope], managing the menu stack. */
@InternalUiApi
class UIControllerScopeImpl(
    override val theoplayerView: THEOplayerView?,
    private val providedPlayer: Player?,
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
        menuStack.removeLastOrNull()?.also { lastWasClosed = true }
    }
}
