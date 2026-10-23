package com.theoplayer.android.ui.tv

import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import com.theoplayer.android.api.player.Player
import com.theoplayer.android.api.player.track.texttrack.TextTrack
import com.theoplayer.android.api.player.track.texttrack.TextTrackKind
import com.theoplayer.android.api.player.track.texttrack.TextTrackList
import com.theoplayer.android.api.player.track.texttrack.TextTrackMode
import com.theoplayer.android.api.player.track.texttrack.TextTrackStyle
import com.theoplayer.android.ui.ExperimentalPlayerStateApi
import com.theoplayer.android.ui.LocalTHEOplayerView
import com.theoplayer.android.ui.MenuContent
import com.theoplayer.android.ui.MenuScope
import com.theoplayer.android.ui.UIControllerScope
import com.theoplayer.android.ui.tv.theme.THEOplayerTvTheme
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
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
@OptIn(ExperimentalTvUiApi::class, ExperimentalPlayerStateApi::class)
class TextTrackStyleMenuTest {
    @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun fontColorMenuAppliesColorAndKeepsOpacity() {
        val style = mockTextTrackStyle(fontColor = 0x80FFFFFF.toInt())
        val player = mockPlayer(style = style)
        val menuStack = mutableStateListOf<MenuContent>()
        val scope = TestMenuScope(menuStack)
        menuStack.add { TextTrackStyleMenu() }
        setMenus(player, scope, menuStack)

        composeRule
            .onNode(
                hasAnyDescendant(hasText("Font color")) and hasClickAction(),
                useUnmergedTree = true,
            )
            .performSemanticsAction(SemanticsActions.OnClick)
        composeRule.waitForIdle()
        composeRule
            .onNode(
                hasAnyDescendant(hasText("Green")) and hasClickAction(),
                useUnmergedTree = true,
            )
            .performSemanticsAction(SemanticsActions.OnClick)
        composeRule.waitForIdle()

        assertEquals(0x8000FF00.toInt(), style.fontColor)
        assertTextExists("Subtitle options")
        assertTextExists("Green")
    }

    @Test
    fun languageMenuDoesNotShowTextTrackStyle() {
        val player = mockPlayer(textTracks = listOf(mockSubtitleTrack()))
        val menuStack = mutableStateListOf<MenuContent>()
        val scope = TestMenuScope(menuStack)
        menuStack.add { LanguageMenu() }
        setMenus(player, scope, menuStack)

        assertTrue(
            composeRule.onAllNodesWithText("Subtitle options").fetchSemanticsNodes().isEmpty()
        )
    }

    @Test
    fun textTrackStyleMenuRestoresFocusOnBack() {
        val player = mockPlayer(textTracks = listOf(mockSubtitleTrack()))
        val scope = setController(player)
        composeRule.runOnIdle { scope.openMenu { TextTrackStyleMenu() } }
        waitForText("Font color")

        moveFocusTo("Font color")
        pressCenter()
        waitForText("Green")

        pressBack()
        waitForText("Font color")
        composeRule.onNodeWithText("Font color").assertIsFocused()
    }

    @Test
    fun subtitleMenuRestoresFocusOnBack() {
        val player = mockPlayer(textTracks = listOf(mockSubtitleTrack()))
        val scope = setController(player)
        composeRule.runOnIdle { scope.openMenu { SubtitleMenu() } }
        waitForText("English")

        // The selected track is focused when the menu is first opened.
        composeRule.onNodeWithText("English").assertIsFocused()

        moveFocusTo("Subtitle options", KeyEvent.KEYCODE_DPAD_UP)
        pressCenter()
        waitForText("Font color")

        pressBack()
        waitForText("Subtitle options")
        composeRule.onNodeWithText("Subtitle options").assertIsFocused()
    }

    @Test
    fun subtitleMenuDpadUpFromFirstTrackFocusesOptions() {
        val player = mockPlayer(textTracks = listOf(mockSubtitleTrack()))
        val scope = setController(player)
        composeRule.runOnIdle { scope.openMenu { SubtitleMenu() } }
        waitForText("English")

        pressDpad(KeyEvent.KEYCODE_DPAD_UP)
        composeRule.onNodeWithText("Off").assertIsFocused()
        pressDpad(KeyEvent.KEYCODE_DPAD_UP)
        composeRule.onNodeWithText("Subtitle options").assertIsFocused()
        pressDpad(KeyEvent.KEYCODE_DPAD_DOWN)
        composeRule.onNodeWithText("Off").assertIsFocused()
    }

    @Test
    fun subtitleMenuOpensTextTrackStyleMenu() {
        val player = mockPlayer(textTracks = listOf(mockSubtitleTrack()))
        val menuStack = mutableStateListOf<MenuContent>()
        val scope = TestMenuScope(menuStack)
        menuStack.add { SubtitleMenu() }
        setMenus(player, scope, menuStack)

        composeRule
            .onNode(
                hasAnyDescendant(hasText("Subtitle options")) and hasClickAction(),
                useUnmergedTree = true,
            )
            .performSemanticsAction(SemanticsActions.OnClick)
        composeRule.waitForIdle()

        assertTextExists("Subtitle options")
        assertTextExists("Font color")
    }

    private fun setController(player: Player): UIControllerScope {
        lateinit var scope: UIControllerScope
        val view = PlayerBackedTHEOplayerView(composeRule.activity, player)
        composeRule.setContent {
            THEOplayerTvTheme {
                UIController(
                    theoplayerView = view,
                    centerOverlay = { scope = this },
                )
            }
        }
        composeRule.waitForIdle()
        return scope
    }

    private fun moveFocusTo(text: String, keyCode: Int = KeyEvent.KEYCODE_DPAD_DOWN) {
        repeat(12) {
            val focused =
                composeRule
                    .onAllNodes(hasText(text) and isFocused())
                    .fetchSemanticsNodes()
                    .isNotEmpty()
            if (focused) return
            pressDpad(keyCode)
        }
    }

    private fun pressCenter() {
        assertTrue(
            composeRule.activity.dispatchKeyEvent(
                KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_CENTER)
            )
        )
        assertTrue(
            composeRule.activity.dispatchKeyEvent(
                KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_CENTER)
            )
        )
        composeRule.waitForIdle()
    }

    private fun pressDpad(keyCode: Int) {
        assertTrue(composeRule.activity.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode)))
        composeRule.activity.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
        composeRule.waitForIdle()
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

    private fun setMenus(
        player: Player,
        scope: MenuScope,
        menuStack: List<MenuContent>,
    ) {
        val view = PlayerBackedTHEOplayerView(composeRule.activity, player)
        composeRule.setContent {
            THEOplayerTvTheme {
                CompositionLocalProvider(LocalTHEOplayerView provides view) {
                    menuStack.lastOrNull()?.invoke(scope)
                }
            }
        }
        composeRule.waitForIdle()
    }

    private fun assertTextExists(text: String) {
        assertTrue(composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty())
    }

    private class TestMenuScope(private val menuStack: MutableList<MenuContent>) : MenuScope {
        override fun openMenu(menu: MenuContent) {
            menuStack.add(menu)
        }

        override fun closeCurrentMenu() {
            menuStack.removeLastOrNull()
        }

        override fun closeAllMenus() {
            menuStack.clear()
        }
    }

    private class MockStyle(
        var fontColor: Int = 0xFFFFFFFF.toInt(),
        var backgroundColor: Int = 0xFF000000.toInt(),
        var windowColor: Int = 0x00000000,
        var edgeColor: Int = 0xFFFFFFFF.toInt(),
        var edgeType: TextTrackStyle.EdgeType = TextTrackStyle.EdgeType.EDGE_TYPE_NONE,
        var fontSize: Int = 100,
    ) {
        val style: TextTrackStyle = mockk {
            every { getFontColor() } answers { this@MockStyle.fontColor }
            every { setFontColor(any()) } answers { this@MockStyle.fontColor = firstArg() }
            every { getBackgroundColor() } answers { this@MockStyle.backgroundColor }
            every { setBackgroundColor(any()) } answers
                {
                    this@MockStyle.backgroundColor = firstArg()
                }
            every { getWindowColor() } answers { this@MockStyle.windowColor }
            every { setWindowColor(any()) } answers { this@MockStyle.windowColor = firstArg() }
            every { getEdgeColor() } answers { this@MockStyle.edgeColor }
            every { setEdgeColor(any()) } answers { this@MockStyle.edgeColor = firstArg() }
            every { getEdgeType() } answers { this@MockStyle.edgeType }
            every { setEdgeType(any()) } answers { this@MockStyle.edgeType = firstArg() }
            every { getFontSize() } answers { this@MockStyle.fontSize }
            every { setFontSize(any()) } answers { this@MockStyle.fontSize = firstArg() }
            every { getFont() } returns null
            every { setFont(any<android.graphics.Typeface>()) } just Runs
            every { getMarginLeft() } returns 0
            every { getMarginRight() } returns 0
            every { getMarginTop() } returns 0
            every { getMarginBottom() } returns 0
            every { reset() } just Runs
        }
    }

    private fun mockTextTrackStyle(fontColor: Int): MockStyle = MockStyle(fontColor = fontColor)

    private fun mockSubtitleTrack(): TextTrack =
        mockk(relaxed = true) {
            every { kind } returns TextTrackKind.SUBTITLES.type
            every { mode } returns TextTrackMode.SHOWING
            every { uid } returns 1
            every { label } returns "English"
        }

    private fun mockPlayer(
        style: MockStyle = MockStyle(),
        textTracks: List<TextTrack> = emptyList(),
    ): Player {
        val textTrackList =
            mockk<TextTrackList>(relaxed = true) {
                every { iterator() } answers { textTracks.toMutableList().listIterator() }
            }
        return mockk(relaxed = true) {
            every { getTextTrackStyle() } returns style.style
            every { getTextTracks() } returns textTrackList
        }
    }
}
