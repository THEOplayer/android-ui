package com.theoplayer.android.ui

import android.content.Context
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.unit.dp
import com.theoplayer.android.api.THEOplayerView
import com.theoplayer.android.api.ads.Ads
import com.theoplayer.android.api.player.Player as NativePlayer
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class UIControllerKeysTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun dpadDownReachesOpenMenuWhenPlayingControlsAreHidden() {
        val nativePlayer = mockPlayingPlayer()
        val theoplayerView = PlayerBackedTHEOplayerView(composeRule.activity, nativePlayer)
        val menuFocusRequester = FocusRequester()
        lateinit var uiControllerScope: UIControllerScope
        var menuItemComposed = false
        var menuItemFocused = false
        var menuItemSawKey = false

        composeRule.setContent {
            UIController(
                theoplayerView = theoplayerView,
                centerOverlay = {
                    uiControllerScope = this
                    Box(Modifier.size(1.dp).testTag("center-overlay"))
                },
                bottomChrome = {
                    Box(Modifier.size(1.dp).testTag("controls-visible"))
                }
            )
        }

        composeRule.mainClock.advanceTimeBy(100)
        composeRule.waitForIdle()
        assertTrue(
            composeRule.onAllNodesWithTag("center-overlay")
                .fetchSemanticsNodes()
                .isNotEmpty()
        )
        assertTrue(
            composeRule.onAllNodesWithTag("controls-visible")
                .fetchSemanticsNodes()
                .isEmpty()
        )
        composeRule.runOnIdle {
            uiControllerScope.openMenu {
                Box(
                    Modifier
                        .fillMaxSize()
                        .focusRequester(menuFocusRequester)
                        .onGloballyPositioned { menuItemComposed = true }
                        .onFocusChanged { menuItemFocused = it.isFocused }
                        .onKeyEvent {
                            menuItemSawKey = true
                            true
                        }
                        .focusable()
                        .testTag("menu-item")
                )
            }
        }
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithTag("center-overlay").fetchSemanticsNodes().isEmpty()
        }
        composeRule.waitUntil(10_000) { menuItemComposed }
        composeRule.runOnIdle { menuFocusRequester.requestFocus() }
        composeRule.waitUntil(10_000) { menuItemFocused }
        assertTrue(
            composeRule.activity.dispatchKeyEvent(
                KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_DOWN)
            )
        )
        assertTrue(menuItemSawKey)
    }

    @Test
    fun backClosesMenuBeforeFallingThroughToActivity() {
        val nativePlayer = mockPlayingPlayer()
        val theoplayerView = PlayerBackedTHEOplayerView(composeRule.activity, nativePlayer)
        var activityBackCalls = 0
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                activityBackCalls++
            }
        }
        composeRule.activity.onBackPressedDispatcher.addCallback(callback)
        lateinit var uiControllerScope: UIControllerScope

        composeRule.setContent {
            UIController(
                theoplayerView = theoplayerView,
                centerOverlay = { uiControllerScope = this }
            )
        }
        composeRule.waitForIdle()
        composeRule.runOnIdle {
            uiControllerScope.openMenu {
                Box(Modifier.size(20.dp).testTag("menu"))
            }
        }
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithTag("menu").fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.runOnIdle {
            composeRule.activity.onBackPressedDispatcher.onBackPressed()
        }
        composeRule.waitForIdle()

        assertTrue(composeRule.onAllNodesWithTag("menu").fetchSemanticsNodes().isEmpty())
        assertEquals(0, activityBackCalls)
        composeRule.runOnIdle {
            composeRule.activity.onBackPressedDispatcher.onBackPressed()
        }
        composeRule.waitForIdle()
        assertEquals(1, activityBackCalls)
    }

    @Test
    fun backFallsThroughWhenNoMenuIsOpen() {
        val nativePlayer = mockPlayingPlayer()
        val theoplayerView = PlayerBackedTHEOplayerView(composeRule.activity, nativePlayer)
        var activityBackCalls = 0
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                activityBackCalls++
            }
        }
        composeRule.activity.onBackPressedDispatcher.addCallback(callback)

        composeRule.setContent {
            UIController(theoplayerView = theoplayerView)
        }
        composeRule.waitForIdle()
        composeRule.runOnIdle {
            composeRule.activity.onBackPressedDispatcher.onBackPressed()
        }

        assertEquals(1, activityBackCalls)
    }

    private fun mockPlayingPlayer(): NativePlayer {
        val ads = mockk<Ads>(relaxed = true)
        every { ads.isPlaying } returns false
        val player = mockk<NativePlayer>(relaxed = true)
        every { player.isPaused } returns false
        every { player.isEnded } returns false
        every { player.ads } returns ads
        return player
    }

    private class PlayerBackedTHEOplayerView(
        context: Context,
        private val nativePlayer: NativePlayer
    ) : THEOplayerView(context) {
        override fun getPlayer(): NativePlayer = nativePlayer
    }
}
