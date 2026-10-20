package com.theoplayer.android.ui

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import com.theoplayer.android.api.THEOplayerView
import com.theoplayer.android.api.player.Player
import com.theoplayer.android.api.player.track.texttrack.TextTrack
import com.theoplayer.android.api.player.track.texttrack.TextTrackKind
import com.theoplayer.android.api.player.track.texttrack.TextTrackList
import com.theoplayer.android.api.player.track.texttrack.TextTrackMode
import com.theoplayer.android.api.player.track.texttrack.TextTrackStyle
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
@OptIn(ExperimentalPlayerStateApi::class, InternalUiApi::class)
class TextTrackStyleMenuTest {
    @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun fontColorMenuAppliesColorAndKeepsOpacity() {
        val style = MockStyle(fontColor = 0x80FFFFFF.toInt())
        val player = mockPlayer(style = style)
        val menuStack = mutableStateListOf<MenuContent>()
        val scope = TestMenuScope(menuStack)
        menuStack.add { TextTrackStyleMenu() }
        setMenus(player, scope, menuStack)

        composeRule
            .onAllNodes(
                hasAnyDescendant(hasText("White")) and
                    hasClickAction() and
                    !hasAnyDescendant(hasClickAction()),
                useUnmergedTree = true,
            )[0]
            .performClick()
        composeRule.waitForIdle()
        composeRule
            .onNode(
                hasAnyDescendant(hasText("Green")) and
                    hasClickAction() and
                    !hasAnyDescendant(hasClickAction()),
                useUnmergedTree = true,
            )
            .performClick()
        composeRule.waitForIdle()

        assertEquals(0x8000FF00.toInt(), style.fontColor)
        assertTextExists("Caption style")
        assertTextExists("Green")
    }

    @Test
    fun languageMenuShowsCaptionStyleActionWithSubtitleTracks() {
        val player = mockPlayer(textTracks = listOf(mockSubtitleTrack()))
        val menuStack = mutableStateListOf<MenuContent>()
        val scope = TestMenuScope(menuStack)
        menuStack.add { LanguageMenu() }
        setMenus(player, scope, menuStack)

        assertTrue(
            composeRule
                .onAllNodes(hasContentDescription("Caption style"))
                .fetchSemanticsNodes()
                .isNotEmpty()
        )
    }

    @Test
    fun languageMenuHidesCaptionStyleActionWithoutSubtitleTracks() {
        val player = mockPlayer()
        val menuStack = mutableStateListOf<MenuContent>()
        val scope = TestMenuScope(menuStack)
        menuStack.add { LanguageMenu() }
        setMenus(player, scope, menuStack)

        assertTrue(
            composeRule
                .onAllNodes(hasContentDescription("Caption style"))
                .fetchSemanticsNodes()
                .isEmpty()
        )
    }

    private fun setMenus(
        player: Player,
        scope: MenuScope,
        menuStack: List<MenuContent>,
    ) {
        val view = PlayerBackedTHEOplayerView(composeRule.activity, player)
        composeRule.setContent {
            CompositionLocalProvider(LocalTHEOplayerView provides view) {
                menuStack.lastOrNull()?.invoke(scope)
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

    private class PlayerBackedTHEOplayerView(
        context: Context,
        private val nativePlayer: Player,
    ) : THEOplayerView(context) {
        override fun getPlayer(): Player = nativePlayer
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
