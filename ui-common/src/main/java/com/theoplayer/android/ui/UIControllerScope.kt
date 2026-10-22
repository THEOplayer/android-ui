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

/**
 * An entry on the menu stack, pairing a [MenuContent] with a stable [id].
 *
 * The [id] uniquely identifies the menu for as long as it is on the stack, so saved state (such as
 * scroll positions) can be keyed to it.
 */
@InternalUiApi
class MenuEntry
internal constructor(
    val id: Int,
    val content: MenuContent,
)

/** Default implementation of [UIControllerScope], managing the menu stack. */
@InternalUiApi
class UIControllerScopeImpl(
    override val theoplayerView: THEOplayerView?,
    private val providedPlayer: Player?,
) : UIControllerScope {
    private var menuStack = mutableStateListOf<MenuEntry>()
    private var nextMenuId = 0

    val currentMenu: MenuEntry?
        get() = menuStack.lastOrNull()

    /** The ids of all menus currently on the stack, in stack order. */
    val menuIds: List<Int>
        get() = menuStack.map { it.id }

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
        menuStack.add(MenuEntry(nextMenuId++, menu))
        lastWasClosed = false
    }

    override fun closeCurrentMenu() {
        menuStack.removeLastOrNull()?.also { lastWasClosed = true }
    }
}
