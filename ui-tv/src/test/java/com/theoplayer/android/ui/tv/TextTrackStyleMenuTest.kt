package com.theoplayer.android.ui.tv

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
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
    fun languageMenuShowsTextTrackStyleWithSubtitleTracks() {
        val player = mockPlayer(textTracks = listOf(mockSubtitleTrack()))
        val menuStack = mutableStateListOf<MenuContent>()
        val scope = TestMenuScope(menuStack)
        menuStack.add { LanguageMenu() }
        setMenus(player, scope, menuStack)

        assertTextExists("Subtitle options")
    }

    @Test
    fun languageMenuHidesTextTrackStyleWithoutSubtitleTracks() {
        val player = mockPlayer()
        val menuStack = mutableStateListOf<MenuContent>()
        val scope = TestMenuScope(menuStack)
        menuStack.add { LanguageMenu() }
        setMenus(player, scope, menuStack)

        assertTrue(
            composeRule.onAllNodesWithText("Subtitle options").fetchSemanticsNodes().isEmpty()
        )
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
