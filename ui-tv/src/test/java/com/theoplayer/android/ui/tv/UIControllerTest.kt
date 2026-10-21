package com.theoplayer.android.ui.tv

import android.view.KeyEvent
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEvent as ComposeKeyEvent
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.theoplayer.android.api.THEOplayerView
import com.theoplayer.android.api.ads.Ads
import com.theoplayer.android.api.error.THEOplayerException
import com.theoplayer.android.api.event.player.ErrorEvent
import com.theoplayer.android.api.event.player.PlayerEventTypes
import com.theoplayer.android.api.player.Player
import com.theoplayer.android.api.player.track.mediatrack.MediaTrack
import com.theoplayer.android.api.player.track.mediatrack.MediaTrackList
import com.theoplayer.android.api.player.track.mediatrack.quality.AudioQuality
import com.theoplayer.android.api.player.track.mediatrack.quality.QualityList
import com.theoplayer.android.api.player.track.mediatrack.quality.VideoQuality
import com.theoplayer.android.api.player.track.texttrack.TextTrack
import com.theoplayer.android.api.player.track.texttrack.TextTrackList
import com.theoplayer.android.api.timerange.TimeRanges
import com.theoplayer.android.ui.InternalUiApi
import com.theoplayer.android.ui.UIControllerScope
import com.theoplayer.android.ui.tv.theme.THEOplayerTvTheme
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@OptIn(ExperimentalTvUiApi::class, InternalUiApi::class)
class UIControllerTest {
    @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun hiddenDpadDownShowsControlsAndConsumesKey() {
        val player = mockPlayingPlayer()
        val keyFocusRequester = FocusRequester()
        var childSawKey = false
        setController(
            player = player,
            centerOverlay =
                focusableOverlay(keyFocusRequester) {
                    childSawKey = true
                    false
                },
            bottomChrome = { Box(Modifier.size(20.dp).testTag("controls")) },
        )
        composeRule.runOnIdle { keyFocusRequester.requestFocus() }

        assertTrue(dispatchKey(KeyEvent.KEYCODE_DPAD_DOWN))
        waitForTag("controls")

        assertTagExists("controls")
        assertFalse(childSawKey)
    }

    @Test
    fun hiddenDpadLeftAndRightSeekByTenSeconds() {
        val player = mockPlayingPlayer(currentTime = 40.0)
        val keyFocusRequester = FocusRequester()
        setController(
            player = player,
            centerOverlay = focusableOverlay(keyFocusRequester),
        )
        composeRule.runOnIdle { keyFocusRequester.requestFocus() }

        assertTrue(dispatchKey(KeyEvent.KEYCODE_DPAD_RIGHT))
        verify(exactly = 1) { player.currentTime = 50.0 }

        assertTrue(dispatchKey(KeyEvent.KEYCODE_DPAD_LEFT))
        verify(exactly = 1) { player.currentTime = 30.0 }
    }

    @Test
    fun languageMenuButtonIsHiddenWithoutAlternativeTracks() {
        val onlyTrack = MenuAudioTrack(uid = 1, label = "English", enabled = true)
        setControllerAndShowControls(mockMenuPlayer(audioTracks = listOf(onlyTrack.track))) {
            LanguageMenuButton(modifier = Modifier.testTag("language-menu-button"))
        }
        assertTagDoesNotExist("language-menu-button")
    }

    @Test
    fun languageMenuButtonIsShownWithAlternativeAudioTracks() {
        val onlyTrack = MenuAudioTrack(uid = 1, label = "English", enabled = true)
        val secondTrack = MenuAudioTrack(uid = 2, label = "French", enabled = false)
        setControllerAndShowControls(
            mockMenuPlayer(audioTracks = listOf(onlyTrack.track, secondTrack.track))
        ) {
            LanguageMenuButton(modifier = Modifier.testTag("language-menu-button"))
        }
        waitForTag("language-menu-button")
    }

    @Test
    fun languageMenuShowsActiveTrackAndFocusesFirstRow() {
        val english = MenuAudioTrack(uid = 1, label = "English", enabled = true)
        val french = MenuAudioTrack(uid = 2, label = "French", enabled = false)
        val buttonFocusRequester = FocusRequester()
        setControllerAndShowControls(
            mockMenuPlayer(audioTracks = listOf(english.track, french.track))
        ) {
            LanguageMenuButton(
                modifier =
                    Modifier.focusRequester(buttonFocusRequester).testTag("language-menu-button")
            )
        }
        waitForTag("language-menu-button")
        composeRule.runOnIdle { buttonFocusRequester.requestFocus() }
        composeRule.onNodeWithTag("language-menu-button").assertIsFocused()
        pressCenter()

        waitForText("Language")
        assertTextExists("Language")
        waitForText("English")
        assertTextExists("English")
        composeRule.onNodeWithText("Audio").assertIsFocused()
    }

    @Test
    fun audioTrackMenuFocusesActiveTrackAndSelectionReturnsToLanguageMenu() {
        val english = MenuAudioTrack(uid = 1, label = "English", enabled = false)
        val french = MenuAudioTrack(uid = 2, label = "French", enabled = true)
        val buttonFocusRequester = FocusRequester()
        setControllerAndShowControls(
            mockMenuPlayer(audioTracks = listOf(english.track, french.track))
        ) {
            LanguageMenuButton(
                modifier =
                    Modifier.focusRequester(buttonFocusRequester).testTag("language-menu-button")
            )
        }
        waitForTag("language-menu-button")
        composeRule.runOnIdle { buttonFocusRequester.requestFocus() }
        composeRule.onNodeWithTag("language-menu-button").assertIsFocused()
        pressCenter()
        waitForText("Audio")
        composeRule.onNodeWithText("Audio").assertIsFocused()
        pressCenter()

        waitForText("French")
        composeRule.onNodeWithText("French").assertIsFocused()
        pressDpad(KeyEvent.KEYCODE_DPAD_UP)
        composeRule.onNodeWithText("English").assertIsFocused()
        pressCenter()
        waitForText("Language")
        assertTextExists("Language")
        assertTextExists("English")
        assertTrue(english.enabled)
    }

    @Test
    fun backReturnsFromSubmenuAndRestoresFocusToControls() {
        val english = MenuAudioTrack(uid = 1, label = "English", enabled = true)
        val french = MenuAudioTrack(uid = 2, label = "French", enabled = false)
        val buttonFocusRequester = FocusRequester()
        setControllerAndShowControls(
            mockMenuPlayer(audioTracks = listOf(english.track, french.track))
        ) {
            LanguageMenuButton(
                modifier =
                    Modifier.focusRequester(buttonFocusRequester).testTag("language-menu-button")
            )
        }
        waitForTag("language-menu-button")
        composeRule.runOnIdle { buttonFocusRequester.requestFocus() }
        composeRule.onNodeWithTag("language-menu-button").assertIsFocused()
        pressCenter()
        waitForText("Audio")
        composeRule.onNodeWithText("Audio").assertIsFocused()
        pressCenter()
        waitForText("English")
        assertTextExists("English")

        composeRule.runOnIdle { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
        waitForText("Language")
        assertTextExists("Language")
        composeRule.onNodeWithText("Audio").assertIsFocused()
        composeRule.runOnIdle { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
        composeRule.waitForIdle()

        assertTagExists("language-menu-button")
        composeRule.onNodeWithTag("language-menu-button").assertIsFocused()
    }

    @Test
    fun settingsMenuShowsCurrentValuesAndSelectsPlaybackRate() {
        val player = mockMenuPlayer(playbackRate = 1.25)
        val buttonFocusRequester = FocusRequester()
        setControllerAndShowControls(player) {
            SettingsMenuButton(
                modifier =
                    Modifier.focusRequester(buttonFocusRequester).testTag("settings-menu-button")
            )
        }
        waitForTag("settings-menu-button")
        composeRule.runOnIdle { buttonFocusRequester.requestFocus() }
        composeRule.onNodeWithTag("settings-menu-button").assertIsFocused()
        pressCenter()

        waitForText("Automatic")
        assertTextExists("Automatic")
        assertTextExists("1.25x")
        composeRule.onNodeWithText("Quality").assertIsFocused()
        pressDpad(KeyEvent.KEYCODE_DPAD_DOWN)
        composeRule.onNodeWithText("Playback speed").assertIsFocused()
        waitForText("Playback speed")
        pressCenter()
        waitForText("1.5x")
        composeRule.onNodeWithText("1.25x").assertIsFocused()
        pressDpad(KeyEvent.KEYCODE_DPAD_DOWN)
        composeRule.onNodeWithText("1.5x").assertIsFocused()
        pressCenter()

        verify(exactly = 1) { player.setPlaybackRate(1.5) }
    }

    @Test
    fun qualityListSelectsTargetQuality() {
        val quality720 = menuVideoQuality(uid = 10, height = 720, bandwidth = 2_500_000)
        val quality1080 = menuVideoQuality(uid = 11, height = 1080, bandwidth = 5_000_000)
        val videoTrack = MenuVideoTrack(listOf(quality720, quality1080))
        val player = mockMenuPlayer(videoTracks = listOf(videoTrack.track))
        val buttonFocusRequester = FocusRequester()
        setControllerAndShowControls(player) {
            SettingsMenuButton(
                modifier =
                    Modifier.focusRequester(buttonFocusRequester).testTag("settings-menu-button")
            )
        }
        waitForTag("settings-menu-button")
        composeRule.runOnIdle { buttonFocusRequester.requestFocus() }
        composeRule.onNodeWithTag("settings-menu-button").assertIsFocused()
        pressCenter()
        waitForText("Quality")
        composeRule.onNodeWithText("Quality").assertIsFocused()
        pressCenter()
        waitForText("720p")
        composeRule.onNodeWithText("Automatic").assertIsFocused()
        pressDpad(KeyEvent.KEYCODE_DPAD_DOWN)
        composeRule.onNodeWithText("720p").assertIsFocused()
        pressCenter()

        assertEquals(quality720, videoTrack.targetQuality)
    }

    @Test
    fun hiddenDpadCenterPausesAndShowsControls() {
        val player = mockPlayingPlayer()
        val keyFocusRequester = FocusRequester()
        setController(
            player = player,
            centerOverlay = focusableOverlay(keyFocusRequester),
            bottomChrome = { Box(Modifier.size(20.dp).testTag("controls")) },
        )
        composeRule.runOnIdle { keyFocusRequester.requestFocus() }

        assertTrue(dispatchKey(KeyEvent.KEYCODE_DPAD_CENTER))

        verify(exactly = 1) { player.pause() }
        waitForTag("controls")
        assertTagExists("controls")
    }

    @Test
    fun visibleDpadRightReachesFocusedControlWithoutSeeking() {
        val player = mockPlayingPlayer(currentTime = 40.0)
        val keyFocusRequester = FocusRequester()
        var focusedControlSawKey = false
        var focused = false
        setController(
            player = player,
            centerOverlay = focusableOverlay(keyFocusRequester),
            bottomChrome = {
                Box(
                    Modifier.size(20.dp)
                        .onFocusChanged { focused = it.isFocused }
                        .onKeyEvent {
                            focusedControlSawKey = true
                            true
                        }
                        .focusable()
                        .testTag("control")
                )
            },
        )
        composeRule.runOnIdle { keyFocusRequester.requestFocus() }
        assertTrue(dispatchKey(KeyEvent.KEYCODE_DPAD_DOWN))
        composeRule.waitUntil(10_000) { focused }

        assertTrue(dispatchKey(KeyEvent.KEYCODE_DPAD_RIGHT))

        assertTrue(focusedControlSawKey)
        verify(exactly = 0) { player.currentTime = any() }
    }

    @Test
    fun controlsAutoHideAfterFiveSecondsWhilePlaying() {
        val player = mockPlayingPlayer()
        val keyFocusRequester = FocusRequester()
        setController(
            player = player,
            centerOverlay = focusableOverlay(keyFocusRequester),
            bottomChrome = { Box(Modifier.size(20.dp).testTag("controls")) },
        )
        composeRule.runOnIdle { keyFocusRequester.requestFocus() }
        assertTrue(dispatchKey(KeyEvent.KEYCODE_DPAD_DOWN))
        waitForTag("controls")
        assertTagExists("controls")

        composeRule.mainClock.advanceTimeBy(CONTROLS_AUTO_HIDE_DURATION_MILLIS)
        composeRule.waitForIdle()

        assertTagDoesNotExist("controls")
    }

    @Test
    fun backClosesMenuBeforeFallingThroughToActivity() {
        var activityBackCalls = 0
        val callback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    activityBackCalls++
                }
            }
        composeRule.activity.onBackPressedDispatcher.addCallback(callback)

        val player = mockPlayingPlayer()
        val keyFocusRequester = FocusRequester()
        setController(
            player = player,
            centerOverlay = focusableOverlay(keyFocusRequester),
            bottomChrome = {
                Box(
                    Modifier.size(20.dp)
                        .clickable { openMenu { Box(Modifier.fillMaxSize().testTag("menu")) } }
                        .focusable()
                        .testTag("open-menu")
                )
            },
        )
        composeRule.runOnIdle { keyFocusRequester.requestFocus() }
        assertTrue(dispatchKey(KeyEvent.KEYCODE_DPAD_DOWN))
        waitForTag("open-menu")
        composeRule.onNodeWithTag("open-menu").performClick()
        waitForTag("menu")
        assertTagExists("menu")

        composeRule.runOnIdle { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
        composeRule.waitForIdle()

        assertTagDoesNotExist("menu")
        assertEquals(0, activityBackCalls)
        waitForTag("open-menu")

        composeRule.runOnIdle { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
        composeRule.waitForIdle()
        assertTagDoesNotExist("open-menu")
        assertEquals(0, activityBackCalls)

        composeRule.runOnIdle { composeRule.activity.onBackPressedDispatcher.onBackPressed() }

        assertEquals(1, activityBackCalls)
    }

    @Test
    fun backHidesVisibleControlsAndFallsThroughWhenHidden() {
        var activityBackCalls = 0
        val callback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    activityBackCalls++
                }
            }
        composeRule.activity.onBackPressedDispatcher.addCallback(callback)

        val player = mockPlayingPlayer()
        val keyFocusRequester = FocusRequester()
        setController(
            player = player,
            centerOverlay = focusableOverlay(keyFocusRequester),
            bottomChrome = { Box(Modifier.size(20.dp).testTag("controls")) },
        )
        composeRule.runOnIdle { keyFocusRequester.requestFocus() }
        assertTrue(dispatchKey(KeyEvent.KEYCODE_DPAD_DOWN))
        waitForTag("controls")
        assertTagExists("controls")

        composeRule.runOnIdle { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
        composeRule.waitForIdle()

        assertTagDoesNotExist("controls")
        assertEquals(0, activityBackCalls)

        composeRule.runOnIdle { composeRule.activity.onBackPressedDispatcher.onBackPressed() }

        assertEquals(1, activityBackCalls)
    }

    @Test
    fun dpadCenterReachesMenuWhenControlsAreHidden() {
        val player = mockPlayingPlayer()
        var menuItemSawKey = false
        openMenuWhileControlsAreHidden(player) {
            menuItemSawKey = true
            true
        }

        assertTrue(dispatchKey(KeyEvent.KEYCODE_DPAD_CENTER))

        assertTrue(menuItemSawKey)
        verify(exactly = 0) { player.pause() }
        verify(exactly = 0) { player.play() }
        verify(exactly = 0) { player.currentTime = any() }
    }

    @Test
    fun dpadRightReachesMenuWhenControlsAreHidden() {
        val player = mockPlayingPlayer()
        var menuItemSawKey = false
        openMenuWhileControlsAreHidden(player) {
            menuItemSawKey = true
            true
        }

        assertTrue(dispatchKey(KeyEvent.KEYCODE_DPAD_RIGHT))

        assertTrue(menuItemSawKey)
        verify(exactly = 0) { player.pause() }
        verify(exactly = 0) { player.play() }
        verify(exactly = 0) { player.currentTime = any() }
    }

    @Test
    fun backBeforeFirstPlayFallsThroughToActivity() {
        var activityBackCalls = 0
        val callback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    activityBackCalls++
                }
            }
        composeRule.activity.onBackPressedDispatcher.addCallback(callback)
        setController(
            player = mockNotPlayedPlayer(),
            bottomChrome = { Box(Modifier.size(20.dp).testTag("controls")) },
        )
        composeRule.mainClock.advanceTimeBy(100)
        composeRule.waitForIdle()
        waitForTag("controls")

        composeRule.runOnIdle { composeRule.activity.onBackPressedDispatcher.onBackPressed() }

        assertEquals(1, activityBackCalls)
    }

    @Test
    fun backOnErrorFallsThroughToActivity() {
        var activityBackCalls = 0
        val callback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    activityBackCalls++
                }
            }
        composeRule.activity.onBackPressedDispatcher.addCallback(callback)
        val player = mockNotPlayedPlayer()
        val errorListeners = PlayerEventListeners()
        every { player.addEventListener(PlayerEventTypes.ERROR, any()) } answers
            {
                errorListeners.add(firstArg(), secondArg())
                Unit
            }
        setController(
            player = player,
            errorOverlay = { Box(Modifier.size(20.dp).testTag("error")) },
        )
        composeRule.mainClock.advanceTimeBy(100)
        composeRule.waitUntil(10_000) { errorListeners.hasListeners(PlayerEventTypes.ERROR) }
        composeRule.waitForIdle()
        val errorEvent = mockk<ErrorEvent>()
        every { errorEvent.errorObject } returns mockk<THEOplayerException>()
        composeRule.runOnIdle { errorListeners.dispatch(PlayerEventTypes.ERROR, errorEvent) }
        waitForTag("error")

        composeRule.runOnIdle { composeRule.activity.onBackPressedDispatcher.onBackPressed() }

        assertEquals(1, activityBackCalls)
        assertTagExists("error")
    }

    @Test
    fun controlsRequestFocusOnFirstBottomChromeControl() {
        val player = mockPlayingPlayer()
        val keyFocusRequester = FocusRequester()
        var firstControlFocused = false
        var secondControlFocused = false
        setController(
            player = player,
            centerOverlay = focusableOverlay(keyFocusRequester),
            bottomChrome = {
                Row {
                    Box(
                        Modifier.size(20.dp)
                            .onFocusChanged { firstControlFocused = it.isFocused }
                            .focusable()
                            .testTag("first-control")
                    )
                    Box(
                        Modifier.size(20.dp)
                            .onFocusChanged { secondControlFocused = it.isFocused }
                            .focusable()
                            .testTag("second-control")
                    )
                }
            },
        )
        composeRule.runOnIdle { keyFocusRequester.requestFocus() }

        assertTrue(dispatchKey(KeyEvent.KEYCODE_DPAD_DOWN))
        composeRule.waitUntil(10_000) { firstControlFocused }

        assertTrue(firstControlFocused)
        assertFalse(secondControlFocused)
    }

    private fun setController(
        player: Player,
        centerOverlay: (@Composable UIControllerScope.() -> Unit)? = null,
        errorOverlay: (@Composable UIControllerScope.() -> Unit)? = null,
        topChrome: (@Composable UIControllerScope.() -> Unit)? = null,
        centerChrome: (@Composable UIControllerScope.() -> Unit)? = null,
        bottomChrome: (@Composable UIControllerScope.() -> Unit)? = null,
    ) {
        lateinit var theoplayerView: THEOplayerView
        composeRule.setContent {
            val view = remember { PlayerBackedTHEOplayerView(composeRule.activity, player) }
            theoplayerView = view
            THEOplayerTvTheme {
                UIController(
                    modifier = Modifier.fillMaxSize(),
                    theoplayerView = view,
                    color = Color.Black.copy(alpha = 0.5f),
                    centerOverlay = centerOverlay,
                    errorOverlay = errorOverlay,
                    topChrome = topChrome,
                    centerChrome = centerChrome,
                    bottomChrome = bottomChrome,
                )
            }
        }
        composeRule.waitUntil(10_000) {
            val container =
                theoplayerView.findViewById<ViewGroup>(
                    com.theoplayer.android.R.id.theo_ui_container
                )
            (0 until container.childCount).any { container.getChildAt(it) is ComposeView }
        }
    }

    private fun openMenuWhileControlsAreHidden(
        player: Player,
        onMenuKeyEvent: (ComposeKeyEvent) -> Boolean,
    ) {
        val menuFocusRequester = FocusRequester()
        lateinit var scope: UIControllerScope
        var menuItemFocused = false
        setController(
            player = player,
            centerOverlay = {
                scope = this
                Box(Modifier.size(1.dp).testTag("center-overlay"))
            },
            bottomChrome = { Box(Modifier.size(20.dp).testTag("controls")) },
        )
        composeRule.mainClock.advanceTimeBy(100)
        composeRule.waitForIdle()
        assertTagDoesNotExist("controls")

        composeRule.runOnIdle {
            scope.openMenu {
                Box(
                    Modifier.size(20.dp)
                        .focusRequester(menuFocusRequester)
                        .onFocusChanged { menuItemFocused = it.isFocused }
                        .onKeyEvent(onMenuKeyEvent)
                        .focusable()
                        .testTag("menu-item")
                )
            }
        }
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithTag("center-overlay").fetchSemanticsNodes().isEmpty()
        }
        waitForTag("menu-item")
        composeRule.runOnIdle { menuFocusRequester.requestFocus() }
        composeRule.waitUntil(10_000) { menuItemFocused }
    }

    private fun waitForTag(tag: String) {
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun waitForText(text: String) {
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun assertTagExists(tag: String) {
        assertTrue(composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty())
    }

    private fun assertTagDoesNotExist(tag: String) {
        assertTrue(composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isEmpty())
    }

    private fun assertTextExists(text: String) {
        assertTrue(composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty())
    }

    private fun focusableOverlay(
        focusRequester: FocusRequester,
        onKeyEvent: (ComposeKeyEvent) -> Boolean = { false },
    ): @Composable UIControllerScope.() -> Unit = {
        Box(
            Modifier.fillMaxSize().focusRequester(focusRequester).onKeyEvent(onKeyEvent).focusable()
        )
    }

    private fun mockPlayingPlayer(currentTime: Double = 40.0): Player {
        val ads = mockk<Ads>(relaxed = true)
        every { ads.isPlaying } returns false
        val player = mockk<Player>(relaxed = true)
        every { player.isPaused } returns false
        every { player.isEnded } returns false
        every { player.ads } returns ads
        every { player.currentTime } returns currentTime
        every { player.duration } returns 100.0
        return player
    }

    private fun mockNotPlayedPlayer(): Player {
        val ads = mockk<Ads>(relaxed = true)
        every { ads.isPlaying } returns false
        val player = mockk<Player>(relaxed = true)
        val played = mockk<TimeRanges>(relaxed = true)
        every { played.length() } returns 0
        every { player.isPaused } returns true
        every { player.played } returns played
        every { player.ads } returns ads
        return player
    }

    private fun setControllerAndShowControls(
        player: Player,
        bottomChrome: @Composable UIControllerScope.() -> Unit,
    ) {
        val keyFocusRequester = FocusRequester()
        setController(
            player = player,
            centerOverlay = focusableOverlay(keyFocusRequester),
            bottomChrome = bottomChrome,
        )
        composeRule.runOnIdle { keyFocusRequester.requestFocus() }
        assertTrue(dispatchKey(KeyEvent.KEYCODE_DPAD_DOWN))
        composeRule.waitForIdle()
    }

    private fun mockMenuPlayer(
        audioTracks: List<MediaTrack<AudioQuality>> = emptyList(),
        videoTracks: List<MediaTrack<VideoQuality>> = emptyList(),
        playbackRate: Double = 1.0,
    ): Player {
        val player = mockPlayingPlayer()
        every { player.getAudioTracks() } returns mockAudioTrackList(audioTracks)
        every { player.getTextTracks() } returns mockTextTrackList(emptyList())
        every { player.getVideoTracks() } returns mockVideoTrackList(videoTracks)
        every { player.getPlaybackRate() } returns playbackRate
        return player
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

    private fun dispatchKey(keyCode: Int): Boolean =
        composeRule.activity.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))

    private companion object {
        const val CONTROLS_AUTO_HIDE_DURATION_MILLIS = 5_000L
    }
}

private class MenuAudioTrack(
    val uid: Int,
    val label: String,
    var enabled: Boolean,
) {
    val track: MediaTrack<AudioQuality> =
        mockk(relaxed = true) {
            every { this@mockk.uid } returns this@MenuAudioTrack.uid
            every { this@mockk.label } returns this@MenuAudioTrack.label
            every { isEnabled } answers { this@MenuAudioTrack.enabled }
            every { setEnabled(any()) } answers { this@MenuAudioTrack.enabled = firstArg() }
        }
}

private class MenuVideoTrack(private val qualities: List<VideoQuality>) {
    var targetQuality: VideoQuality? = null

    val track: MediaTrack<VideoQuality> =
        mockk(relaxed = true) {
            every { this@mockk.uid } returns 100
            every { isEnabled } returns true
            every { getActiveQuality() } returns qualities.firstOrNull()
            every { getTargetQuality() } answers { this@MenuVideoTrack.targetQuality }
            every { setTargetQuality(any()) } answers
                {
                    this@MenuVideoTrack.targetQuality = firstArg()
                }
            every { getQualities() } answers
                {
                    mockk<QualityList<VideoQuality>>(relaxed = true) {
                        every { iterator() } answers
                            {
                                this@MenuVideoTrack.qualities.toMutableList().listIterator()
                            }
                    }
                }
        }
}

private fun menuVideoQuality(uid: Int, height: Int, bandwidth: Long): VideoQuality = mockk {
    every { this@mockk.uid } returns uid
    every { this@mockk.height } returns height
    every { this@mockk.bandwidth } returns bandwidth
}

private fun mockAudioTrackList(
    tracks: List<MediaTrack<AudioQuality>>
): MediaTrackList<AudioQuality> =
    mockk(relaxed = true) { every { iterator() } answers { tracks.toMutableList().listIterator() } }

private fun mockVideoTrackList(
    tracks: List<MediaTrack<VideoQuality>>
): MediaTrackList<VideoQuality> =
    mockk(relaxed = true) { every { iterator() } answers { tracks.toMutableList().listIterator() } }

private fun mockTextTrackList(tracks: List<TextTrack>): TextTrackList =
    mockk(relaxed = true) { every { iterator() } answers { tracks.toMutableList().listIterator() } }
