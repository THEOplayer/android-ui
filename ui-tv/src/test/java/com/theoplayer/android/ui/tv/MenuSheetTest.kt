package com.theoplayer.android.ui.tv

import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import com.theoplayer.android.api.ads.Ads
import com.theoplayer.android.api.player.Player
import com.theoplayer.android.ui.InternalUiApi
import com.theoplayer.android.ui.UIControllerScope
import com.theoplayer.android.ui.theme.PlayerAnimations
import com.theoplayer.android.ui.tv.theme.THEOplayerTvTheme
import io.mockk.every
import io.mockk.mockk
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@OptIn(ExperimentalTvUiApi::class, InternalUiApi::class)
class MenuSheetTest {
    @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun submenuPushesInsideOneSharedSheet() {
        setControllerAndOpenMenu()
        composeRule.waitForIdle()
        assertEquals(1, sheetCount())
        val sheetBounds = boundsOf("menu-sheet")!!

        // Push the submenu: mid-transition the contents animate inside the
        // same sheet, which does not move.
        composeRule.mainClock.autoAdvance = false
        composeRule.onNodeWithTag("menu-content").performSemanticsAction(SemanticsActions.OnClick)
        composeRule.mainClock.advanceTimeBy(100)
        assertEquals(1, sheetCount())
        val midBounds = boundsOf("menu-sheet")!!
        assertEquals(sheetBounds.left, midBounds.left, 1f)
        assertEquals(sheetBounds.right, midBounds.right, 1f)

        composeRule.mainClock.autoAdvance = true
        composeRule.waitForIdle()
        assertEquals(1, sheetCount())
        assertTrue(
            composeRule
                .onAllNodesWithTag("submenu-content", useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        )
    }

    @Test
    fun submenuPushSlidesShortDistanceInsideSheet() {
        setControllerAndOpenMenu()
        composeRule.waitForIdle()
        val menuStartLeft = boundsOf("menu-content")!!.left

        // Push the submenu and sample the contents' positions while the
        // shared-axis transition is frozen half-way.
        composeRule.mainClock.autoAdvance = false
        // Invoke the click semantics action directly: under Robolectric the
        // sheet's clipped Surface is opaque to touch hit-testing.
        composeRule.onNodeWithTag("menu-content").performSemanticsAction(SemanticsActions.OnClick)
        // Let the recomposition apply the push before measuring.
        composeRule.mainClock.advanceTimeByFrame()
        val midIncomingLefts = mutableListOf<Float>()
        var outgoingVisibleMidTransition = false
        repeat(8) {
            composeRule.mainClock.advanceTimeBy(50)
            boundsOf("menu-content")?.let { outgoingVisibleMidTransition = true }
            boundsOf("submenu-content")?.let { midIncomingLefts += it.left }
        }

        composeRule.mainClock.autoAdvance = true
        composeRule.waitForIdle()
        val finalIncomingLeft = boundsOf("submenu-content")!!.left
        val slidePx = with(composeRule.density) { 30.dp.toPx() }

        // The incoming content slid in, but by at most the 30dp slide
        // distance — not the full sheet width.
        assertTrue(
            "incomingLefts=$midIncomingLefts final=$finalIncomingLeft slidePx=$slidePx",
            midIncomingLefts.isNotEmpty() &&
                midIncomingLefts.all { left ->
                    left - finalIncomingLeft in -slidePx - 1f..slidePx + 1f
                } &&
                midIncomingLefts.any { it - finalIncomingLeft > 1f },
        )
        // The outgoing content remained visible while the incoming one arrived
        // (fade-through), then only the submenu content remains.
        assertTrue(outgoingVisibleMidTransition)
        assertEquals(menuStartLeft, finalIncomingLeft, 1f)
        assertTrue(
            composeRule
                .onAllNodesWithTag("menu-content", useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isEmpty()
        )
    }

    @Test
    fun menuTransitionDurationIsConfigurable() {
        setControllerAndOpenMenu(
            playerAnimations = PlayerAnimations(menuTransitionDuration = 1000.milliseconds)
        )
        composeRule.waitForIdle()

        composeRule.mainClock.autoAdvance = false
        composeRule.onNodeWithTag("menu-content").performSemanticsAction(SemanticsActions.OnClick)
        composeRule.mainClock.advanceTimeByFrame()

        // At 600ms the outgoing menu is still visible: it would already be
        // gone at the default 300ms duration.
        composeRule.mainClock.advanceTimeBy(600)
        fun menuContentNodes() =
            composeRule
                .onAllNodesWithTag("menu-content", useUnmergedTree = true)
                .fetchSemanticsNodes()
        assertTrue(menuContentNodes().isNotEmpty())

        // Keep advancing past the configured 1000ms duration until only the
        // submenu remains, and check it really took longer than the default
        // 300ms (the transition starts a few frames into the first advance).
        var advanced = 600L
        while (menuContentNodes().isNotEmpty() && advanced < 3000L) {
            composeRule.mainClock.advanceTimeBy(200)
            advanced += 200
        }
        assertTrue(
            "menu-content still present after ${advanced}ms",
            menuContentNodes().isEmpty(),
        )
        assertTrue(
            "transition took only ${advanced}ms, expected more than the default",
            advanced > 800L,
        )
        assertTrue(
            composeRule.onAllNodesWithTag("submenu-content").fetchSemanticsNodes().isNotEmpty()
        )
    }

    private fun setControllerAndOpenMenu(playerAnimations: PlayerAnimations? = null) {
        val player = mockPlayingPlayer()
        var controllerScope: UIControllerScope? = null
        composeRule.setContent {
            val view = remember { PlayerBackedTHEOplayerView(composeRule.activity, player) }
            THEOplayerTvTheme(
                playerAnimations =
                    playerAnimations ?: PlayerAnimations(controlsHideDelay = 5.seconds)
            ) {
                UIController(
                    modifier = Modifier.fillMaxSize(),
                    theoplayerView = view,
                    centerOverlay = { controllerScope = this },
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.runOnIdle {
            controllerScope!!.openMenu {
                Menu(title = { Text("Menu") }) {
                    Box(
                        Modifier.fillMaxWidth().height(100.dp).testTag("menu-content").clickable {
                            openMenu {
                                Menu(title = { Text("Submenu") }) {
                                    Box(
                                        Modifier.fillMaxWidth()
                                            .height(50.dp)
                                            .testTag("submenu-content")
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    private fun sheetCount() =
        composeRule
            .onAllNodesWithTag("menu-sheet", useUnmergedTree = true)
            .fetchSemanticsNodes()
            .size

    private fun boundsOf(tag: String) =
        composeRule
            .onAllNodesWithTag(tag, useUnmergedTree = true)
            .fetchSemanticsNodes()
            .firstOrNull()
            ?.boundsInRoot

    private fun mockPlayingPlayer(): Player {
        val ads = mockk<Ads>(relaxed = true)
        every { ads.isPlaying } returns false
        val player = mockk<Player>(relaxed = true)
        every { player.isPaused } returns false
        every { player.isEnded } returns false
        every { player.ads } returns ads
        return player
    }
}
