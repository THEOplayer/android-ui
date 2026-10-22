package com.theoplayer.android.ui

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.unit.dp
import com.theoplayer.android.api.THEOplayerView
import com.theoplayer.android.api.player.Player
import com.theoplayer.android.api.player.track.texttrack.TextTrack
import com.theoplayer.android.api.player.track.texttrack.TextTrackKind
import com.theoplayer.android.api.player.track.texttrack.TextTrackList
import com.theoplayer.android.api.player.track.texttrack.TextTrackMode
import com.theoplayer.android.api.player.track.texttrack.TextTrackStyle
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
@OptIn(ExperimentalPlayerStateApi::class, InternalUiApi::class)
class UIControllerMenuStateTest {
    @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun menuSaveableStateSurvivesSubmenuAndIsResetOnClose() {
        val player = mockPlayer()
        val scope = setController(player)
        composeRule.runOnIdle { scope.openMenu { CounterMenu() } }
        waitForTag("counter")

        composeRule.onNodeWithText("Increment").performClick()
        composeRule.waitForIdle()
        assertTagText("counter", "1")

        composeRule.onNodeWithText("Submenu").performClick()
        composeRule.waitForIdle()
        waitForText("Inside submenu")

        pressBack()
        waitForTag("counter")
        assertTagText("counter", "1")

        pressBack()
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithTag("counter").fetchSemanticsNodes().isEmpty()
        }
        composeRule.runOnIdle { scope.openMenu { CounterMenu() } }
        waitForTag("counter")
        assertTagText("counter", "0")
    }

    @Test
    fun textTrackStyleMenuRestoresScrollPositionAfterSubmenu() {
        val player = mockPlayer(textTracks = listOf(mockSubtitleTrack()))
        val scope = setController(player)
        composeRule.runOnIdle { scope.openMenu { TextTrackStyleMenu() } }
        waitForText("Edge color")

        composeRule.onNode(hasScrollAction()).performTouchInput { swipeUp() }
        composeRule.waitForIdle()
        val scrolledValue = scrollValue()
        assertTrue(scrolledValue > 0f)
        // The "Edge color" row shows "White" (same as "Font color"), so activate the
        // second value button, which opens the edge color submenu.
        composeRule
            .onAllNodes(
                hasAnyDescendant(hasText("White")) and
                    hasClickAction() and
                    !hasAnyDescendant(hasClickAction()),
                useUnmergedTree = true,
            )[1]
            .performSemanticsAction(SemanticsActions.OnClick)
        composeRule.waitForIdle()
        waitForText("Yellow")

        pressBack()
        waitForText("Edge color")
        assertEquals(scrolledValue, scrollValue())

        pressBack()
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithText("Edge color").fetchSemanticsNodes().isEmpty()
        }
        composeRule.runOnIdle { scope.openMenu { TextTrackStyleMenu() } }
        waitForText("Edge color")
        assertEquals(0f, scrollValue())
    }

    @androidx.compose.runtime.Composable
    private fun MenuScope.CounterMenu() {
        var count by rememberSaveable { mutableIntStateOf(0) }
        Column {
            Text("$count", modifier = Modifier.testTag("counter"))
            TextButton(onClick = { count++ }) { Text("Increment") }
            TextButton(
                onClick = { openMenu { Box(Modifier.size(1.dp)) { Text("Inside submenu") } } }
            ) {
                Text("Submenu")
            }
        }
    }

    private fun setController(player: Player): UIControllerScope {
        lateinit var scope: UIControllerScope
        val view = PlayerBackedTHEOplayerView(composeRule.activity, player)
        composeRule.setContent {
            UIController(
                theoplayerView = view,
                centerOverlay = { scope = this },
            )
        }
        composeRule.waitForIdle()
        return scope
    }

    private fun scrollValue(): Float {
        return composeRule
            .onNode(hasScrollAction())
            .fetchSemanticsNode()
            .config[SemanticsProperties.VerticalScrollAxisRange]
            .value()
    }

    private fun pressBack() {
        composeRule.runOnIdle { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
        composeRule.waitForIdle()
    }

    private fun waitForText(text: String) {
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun waitForTag(tag: String) {
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun assertTagText(tag: String, text: String) {
        composeRule.onNodeWithTag(tag).assertTextEquals(text)
    }

    private class PlayerBackedTHEOplayerView(
        context: Context,
        private val nativePlayer: Player,
    ) : THEOplayerView(context) {
        override fun getPlayer(): Player = nativePlayer
    }

    private fun mockSubtitleTrack(): TextTrack =
        mockk(relaxed = true) {
            every { kind } returns TextTrackKind.SUBTITLES.type
            every { mode } returns TextTrackMode.SHOWING
            every { uid } returns 1
            every { label } returns "English"
        }

    private fun mockPlayer(textTracks: List<TextTrack> = emptyList()): Player {
        val textTrackList =
            mockk<TextTrackList>(relaxed = true) {
                every { iterator() } answers { textTracks.toMutableList().listIterator() }
            }
        val style =
            mockk<TextTrackStyle>(relaxed = true) {
                every { getFontColor() } returns 0xFFFFFFFF.toInt()
                every { getBackgroundColor() } returns 0xFF000000.toInt()
                every { getWindowColor() } returns 0
                every { getEdgeColor() } returns 0xFFFFFFFF.toInt()
                every { getEdgeType() } returns TextTrackStyle.EdgeType.EDGE_TYPE_NONE
                every { getFontSize() } returns 100
                every { getFont() } returns null
            }
        return mockk(relaxed = true) {
            every { getTextTrackStyle() } returns style
            every { getTextTracks() } returns textTrackList
        }
    }
}
