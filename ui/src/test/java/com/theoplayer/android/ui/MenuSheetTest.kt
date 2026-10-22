package com.theoplayer.android.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.click
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onParent
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.theoplayer.android.api.THEOplayerView
import com.theoplayer.android.api.ads.Ads
import com.theoplayer.android.api.player.Player
import com.theoplayer.android.api.player.track.mediatrack.MediaTrack
import com.theoplayer.android.api.player.track.mediatrack.MediaTrackList
import com.theoplayer.android.api.player.track.mediatrack.quality.AudioQuality
import com.theoplayer.android.api.player.track.texttrack.TextTrack
import com.theoplayer.android.api.player.track.texttrack.TextTrackKind
import com.theoplayer.android.api.player.track.texttrack.TextTrackList
import com.theoplayer.android.api.player.track.texttrack.TextTrackMode
import com.theoplayer.android.ui.theme.PlayerAnimations
import com.theoplayer.android.ui.theme.THEOplayerTheme
import io.mockk.every
import io.mockk.mockk
import kotlin.time.Duration.Companion.milliseconds
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@OptIn(ExperimentalPlayerStateApi::class)
class MenuSheetTest {
    @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    @Config(qualifiers = "w900dp-h400dp")
    fun menuIsSideSheetOnWidePlayer() {
        val scope = TestMenuScope()
        setMenuContent(scope)

        val rootWidth = composeRule.onRoot().getBoundsInRoot().right
        val bounds = contentBounds()
        assertEquals(400.dp.value, (bounds.right - bounds.left).value, 1f)
        assertEquals(rootWidth.value, bounds.right.value, 1f)
    }

    @Test
    @Config(qualifiers = "w900dp-h400dp")
    fun tapOutsideSheetClosesMenu() {
        val scope = TestMenuScope()
        setMenuContent(scope)

        // Tapping the scrim to the left of the 400dp side sheet closes the menu.
        composeRule.onRoot().performTouchInput {
            click(Offset(with(composeRule.density) { 200.dp.toPx() }, centerY))
        }
        assertEquals(1, scope.closeCalls)
    }

    @Test
    @Config(qualifiers = "w900dp-h400dp")
    fun tapOutsideSheetClosesAllMenus() {
        setControllerAndOpenMenus()
        composeRule.waitForIdle()
        pushSubmenu()
        composeRule.waitForIdle()
        assertEquals(1, sheetCount())
        assertTrue(
            composeRule
                .onAllNodesWithTag("submenu-content", useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        )

        // Tapping the scrim to the left of the 400dp side sheet closes all
        // menus, not just the top submenu.
        composeRule.onNodeWithTag("menu-sheet").onParent().performTouchInput {
            click(Offset(with(composeRule.density) { 200.dp.toPx() }, centerY))
        }
        composeRule.waitForIdle()

        assertEquals(0, sheetCount())
        assertTrue(
            composeRule
                .onAllNodesWithTag("menu-content", useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isEmpty()
        )
        assertTrue(
            composeRule
                .onAllNodesWithTag("submenu-content", useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isEmpty()
        )
    }

    @Test
    @Config(qualifiers = "w900dp-h400dp")
    fun backFromSubmenuReturnsToParentMenu() {
        setControllerAndOpenMenus()
        composeRule.waitForIdle()
        pushSubmenu()
        composeRule.waitForIdle()

        // Back only closes the top menu, returning to the parent menu.
        composeRule.activity.onBackPressedDispatcher.onBackPressed()
        composeRule.waitForIdle()

        assertEquals(1, sheetCount())
        assertTrue(
            composeRule
                .onAllNodesWithTag("menu-content", useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        )
        assertTrue(
            composeRule
                .onAllNodesWithTag("submenu-content", useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isEmpty()
        )
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp")
    fun menuIsBottomSheetOnCompactPlayer() {
        val scope = TestMenuScope()
        setMenuContent(scope)

        val rootBounds = composeRule.onRoot().getBoundsInRoot()
        val bounds = sheetBounds()
        assertEquals(
            (rootBounds.right - rootBounds.left).value,
            (bounds.right - bounds.left).value,
            1f,
        )
        assertEquals(rootBounds.bottom.value, bounds.bottom.value, 1f)
        // Title row + 100dp content fits below the 240dp minimum height.
        assertEquals(240.dp.value, (bounds.bottom - bounds.top).value, 1f)
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp")
    fun menuSheetIsCappedAtSixtyPercentOnCompactPlayer() {
        val scope = TestMenuScope()
        composeRule.setContent {
            MenuSheet(onDismiss = { scope.closeCurrentMenu() }) {
                scope.Menu(title = { Text("Menu") }) {
                    Box(Modifier.fillMaxWidth().height(2000.dp).testTag("menu-content"))
                }
            }
        }

        val rootBounds = composeRule.onRoot().getBoundsInRoot()
        val bounds = sheetBounds()
        assertEquals(480.dp.value, (bounds.bottom - bounds.top).value, 1f)
        assertEquals(rootBounds.bottom.value, bounds.bottom.value, 1f)
    }

    @Test
    @Config(qualifiers = "w400dp-h300dp")
    fun menuSheetHasMinHeightOnShortPlayer() {
        val scope = TestMenuScope()
        setMenuContent(scope)

        val rootBounds = composeRule.onRoot().getBoundsInRoot()
        val bounds = sheetBounds()
        assertEquals(240.dp.value, (bounds.bottom - bounds.top).value, 1f)
        assertEquals(rootBounds.bottom.value, bounds.bottom.value, 1f)
    }

    @Test
    @Config(qualifiers = "w400dp-h200dp")
    fun menuSheetFillsVeryShortPlayer() {
        val scope = TestMenuScope()
        setMenuContent(scope)

        val rootBounds = composeRule.onRoot().getBoundsInRoot()
        val bounds = sheetBounds()
        assertEquals(200.dp.value, (bounds.bottom - bounds.top).value, 1f)
        assertEquals(rootBounds.bottom.value, bounds.bottom.value, 1f)
        assertEquals(0.dp.value, bounds.top.value, 1f)
    }

    @Test
    @Config(qualifiers = "w900dp-h400dp")
    fun menuIsSideSheetAtStartInRtl() {
        val scope = TestMenuScope()
        composeRule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                MenuSheet(onDismiss = { scope.closeCurrentMenu() }) {
                    scope.Menu(title = { Text("Menu") }) {
                        Box(Modifier.fillMaxWidth().height(100.dp).testTag("menu-content"))
                    }
                }
            }
        }

        val bounds = contentBounds()
        assertEquals(400.dp.value, (bounds.right - bounds.left).value, 1f)
        assertEquals(0f, bounds.left.value, 1f)
    }

    @Test
    @Config(qualifiers = "w900dp-h400dp")
    fun languageMenuShowsCompactLayoutOnWidePlayer() {
        val scope = TestMenuScope()
        val player =
            mockPlayer(
                audioTracks =
                    listOf(
                        mockAudioTrack(uid = 1, label = "English audio", enabled = true),
                        mockAudioTrack(uid = 2, label = "French audio", enabled = false),
                    ),
                textTracks = listOf(mockSubtitleTrack()),
            )
        composeRule.setContent {
            MenuSheet(onDismiss = { scope.closeCurrentMenu() }) {
                scope.LanguageMenu(
                    audioTrackState = rememberAudioTrackState(player),
                    subtitleTrackState = rememberSubtitleTrackState(player),
                )
            }
        }

        // The 400dp side sheet means the expanded side-by-side layout (2 * 300dp)
        // no longer fits, so the compact layout is shown instead.
        assertTrue(composeRule.onAllNodesWithText("Audio").fetchSemanticsNodes().isNotEmpty())
        assertTrue(composeRule.onAllNodesWithText("Subtitles").fetchSemanticsNodes().isNotEmpty())
        assertTrue(composeRule.onAllNodesWithText("French audio").fetchSemanticsNodes().isEmpty())
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp")
    fun submenuPushesInsideOneSharedSheet() {
        val theoplayerView =
            object : THEOplayerView(composeRule.activity) {
                private val nativePlayer =
                    mockk<com.theoplayer.android.api.player.Player>(relaxed = true) {
                        every { isPaused } returns false
                        every { isEnded } returns false
                        every { ads } returns
                            mockk<Ads>(relaxed = true) { every { isPlaying } returns false }
                    }

                override fun getPlayer() = nativePlayer
            }
        var controllerScope: UIControllerScope? = null
        composeRule.setContent {
            UIController(
                modifier = Modifier.fillMaxSize(),
                theoplayerView = theoplayerView,
                centerOverlay = { controllerScope = this },
            )
        }
        composeRule.waitForIdle()

        // Open a menu; the controller draws it in the shared sheet.
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
        composeRule.waitForIdle()
        assertEquals(1, sheetCount())
        val sheetBounds = composeRule.onNodeWithTag("menu-sheet").getBoundsInRoot()

        // Push the submenu: mid-transition the contents animate inside the
        // same sheet, which does not move horizontally.
        composeRule.mainClock.autoAdvance = false
        // Invoke the click semantics action directly: under Robolectric the
        // sheet's clipped Surface is opaque to touch hit-testing, so a real
        // tap would fall through to the scrim.
        composeRule.onNodeWithTag("menu-content").performSemanticsAction(SemanticsActions.OnClick)
        composeRule.mainClock.advanceTimeBy(100)
        assertEquals(1, sheetCount())
        val midBounds = composeRule.onNodeWithTag("menu-sheet").getBoundsInRoot()
        assertEquals(sheetBounds.left.value, midBounds.left.value, 1f)
        assertEquals(sheetBounds.right.value, midBounds.right.value, 1f)

        composeRule.mainClock.autoAdvance = true
        composeRule.waitForIdle()
        assertEquals(1, sheetCount())
        assertTrue(
            composeRule.onAllNodesWithTag("submenu-content").fetchSemanticsNodes().isNotEmpty()
        )
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp")
    fun submenuPushSlidesShortDistanceInsideSheet() {
        val theoplayerView =
            object : THEOplayerView(composeRule.activity) {
                private val nativePlayer =
                    mockk<com.theoplayer.android.api.player.Player>(relaxed = true) {
                        every { isPaused } returns false
                        every { isEnded } returns false
                        every { ads } returns
                            mockk<Ads>(relaxed = true) { every { isPlaying } returns false }
                    }

                override fun getPlayer() = nativePlayer
            }
        var controllerScope: UIControllerScope? = null
        composeRule.setContent {
            UIController(
                modifier = Modifier.fillMaxSize(),
                theoplayerView = theoplayerView,
                centerOverlay = { controllerScope = this },
            )
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
        composeRule.waitForIdle()
        val menuStartLeft = boundsOf("menu-content")!!.left

        // Push the submenu and sample the contents' positions while the
        // shared-axis transition is frozen half-way.
        composeRule.mainClock.autoAdvance = false
        // Invoke the click semantics action directly: under Robolectric the
        // sheet's clipped Surface is opaque to touch hit-testing, so a real
        // tap would fall through to the scrim.
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

        // The incoming content slid in from the right, but by at most the 30dp
        // slide distance — not the full sheet width.
        assertTrue(
            "incomingLefts=$midIncomingLefts final=$finalIncomingLeft slidePx=$slidePx",
            midIncomingLefts.isNotEmpty() &&
                midIncomingLefts.all { left -> left - finalIncomingLeft in -1f..slidePx + 1f } &&
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
    @Config(qualifiers = "w400dp-h800dp")
    fun menuTransitionDurationIsConfigurable() {
        val theoplayerView =
            object : THEOplayerView(composeRule.activity) {
                private val nativePlayer =
                    mockk<com.theoplayer.android.api.player.Player>(relaxed = true) {
                        every { isPaused } returns false
                        every { isEnded } returns false
                        every { ads } returns
                            mockk<Ads>(relaxed = true) { every { isPlaying } returns false }
                    }

                override fun getPlayer() = nativePlayer
            }
        var controllerScope: UIControllerScope? = null
        composeRule.setContent {
            THEOplayerTheme(
                useDarkTheme = true,
                playerAnimations = PlayerAnimations(menuTransitionDuration = 1000.milliseconds),
            ) {
                UIController(
                    modifier = Modifier.fillMaxSize(),
                    theoplayerView = theoplayerView,
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
        composeRule.waitForIdle()

        composeRule.mainClock.autoAdvance = false
        // Invoke the click semantics action directly: under Robolectric the
        // sheet's clipped Surface is opaque to touch hit-testing, so a real
        // tap would fall through to the scrim.
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

    private fun setControllerAndOpenMenus(): UIControllerScope {
        val theoplayerView =
            object : THEOplayerView(composeRule.activity) {
                private val nativePlayer =
                    mockk<com.theoplayer.android.api.player.Player>(relaxed = true) {
                        every { isPaused } returns false
                        every { isEnded } returns false
                        every { ads } returns
                            mockk<Ads>(relaxed = true) { every { isPlaying } returns false }
                    }

                override fun getPlayer() = nativePlayer
            }
        var controllerScope: UIControllerScope? = null
        composeRule.setContent {
            UIController(
                modifier = Modifier.fillMaxSize(),
                theoplayerView = theoplayerView,
                centerOverlay = { controllerScope = this },
            )
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
        return controllerScope!!
    }

    // Invoke the click semantics action directly: under Robolectric the
    // sheet's clipped Surface is opaque to touch hit-testing, so a real
    // tap would fall through to the scrim.
    private fun pushSubmenu() {
        composeRule.onNodeWithTag("menu-content").performSemanticsAction(SemanticsActions.OnClick)
    }

    private fun boundsOf(tag: String) =
        composeRule
            .onAllNodesWithTag(tag, useUnmergedTree = true)
            .fetchSemanticsNodes()
            .firstOrNull()
            ?.boundsInRoot

    private fun sheetCount() =
        composeRule
            .onAllNodesWithTag("menu-sheet", useUnmergedTree = true)
            .fetchSemanticsNodes()
            .size

    private fun contentBounds(): DpRect =
        composeRule.onNodeWithTag("menu-content", useUnmergedTree = true).getBoundsInRoot()

    private fun sheetBounds(): DpRect =
        composeRule.onNodeWithTag("menu-sheet", useUnmergedTree = true).getBoundsInRoot()

    private fun setMenuContent(scope: TestMenuScope) {
        composeRule.setContent {
            MenuSheet(onDismiss = { scope.closeCurrentMenu() }) {
                scope.Menu(modifier = Modifier.testTag("menu-root"), title = { Text("Menu") }) {
                    Box(Modifier.fillMaxWidth().height(100.dp).testTag("menu-content"))
                }
            }
        }
    }

    @OptIn(InternalUiApi::class)
    private class TestMenuScope : MenuScope {
        var closeCalls = 0

        override fun openMenu(menu: MenuContent) = Unit

        override fun closeCurrentMenu() {
            closeCalls++
        }

        override fun closeAllMenus() {
            closeCalls++
        }
    }

    private fun mockAudioTrack(
        uid: Int,
        label: String,
        enabled: Boolean,
    ): MediaTrack<AudioQuality> =
        mockk(relaxed = true) {
            every { this@mockk.uid } returns uid
            every { this@mockk.label } returns label
            every { isEnabled } returns enabled
        }

    private fun mockSubtitleTrack(): TextTrack =
        mockk(relaxed = true) {
            every { kind } returns TextTrackKind.SUBTITLES.type
            every { mode } returns TextTrackMode.SHOWING
            every { uid } returns 1
            every { label } returns "English"
        }

    private fun mockPlayer(
        audioTracks: List<MediaTrack<AudioQuality>> = emptyList(),
        textTracks: List<TextTrack> = emptyList(),
    ): Player {
        val audioTrackList =
            mockk<MediaTrackList<AudioQuality>>(relaxed = true) {
                every { iterator() } answers { audioTracks.toMutableList().listIterator() }
            }
        val textTrackList =
            mockk<TextTrackList>(relaxed = true) {
                every { iterator() } answers { textTracks.toMutableList().listIterator() }
            }
        return mockk(relaxed = true) {
            every { getAudioTracks() } returns audioTrackList
            every { getTextTracks() } returns textTrackList
        }
    }
}
