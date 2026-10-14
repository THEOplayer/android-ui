package com.theoplayer.android.ui

import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.KeyEvent as ComposeKeyEvent
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import com.theoplayer.android.api.THEOplayerView
import com.theoplayer.android.api.ads.Ads
import com.theoplayer.android.api.player.Player
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@OptIn(InternalUiApi::class)
class PlayerKeysTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun hiddenControlsShowAndConsumeDpadKey() {
        var showControlsCalls = 0
        var childSawKey = false
        setContent(
            controlsVisible = { false },
            showControls = { showControlsCalls++ },
            onChildKey = {
                childSawKey = true
                true
            }
        )

        assertTrue(dispatchKey(KeyEvent.KEYCODE_DPAD_DOWN))
        assertEquals(1, showControlsCalls)
        assertFalse(childSawKey)
    }

    @Test
    fun visibleControlsShowAndPassDpadKeyToFocusedChild() {
        var showControlsCalls = 0
        var childSawKey = false
        setContent(
            controlsVisible = { true },
            showControls = { showControlsCalls++ },
            onChildKey = {
                childSawKey = true
                true
            }
        )

        assertTrue(dispatchKey(KeyEvent.KEYCODE_DPAD_DOWN))
        assertEquals(1, showControlsCalls)
        assertTrue(childSawKey)
    }

    @Test
    fun mediaPlayPausePausesWhilePlaying() {
        val player = mockPlayer(paused = false)
        setContent(player = player)

        assertTrue(dispatchKey(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE))
        verify(exactly = 1) { player.pause() }
    }

    @Test
    fun mediaPlayPausePlaysWhilePaused() {
        val player = mockPlayer(paused = true)
        setContent(player = player)

        assertTrue(dispatchKey(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE))
        verify(exactly = 1) { player.play() }
    }

    @Test
    fun mediaPlayPauseReplaysWhenPausedAndEnded() {
        val player = mockPlayer(paused = true, ended = true)
        setContent(player = player)

        assertTrue(dispatchKey(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE))
        verify(exactly = 1) { player.play() }
        verify(exactly = 0) { player.currentTime = any() }
    }

    @Test
    fun mediaPlayReplaysWhenEnded() {
        val player = mockPlayer(paused = true, ended = true)
        setContent(player = player)

        assertTrue(dispatchKey(KeyEvent.KEYCODE_MEDIA_PLAY))
        verify(exactly = 1) { player.play() }
    }

    @Test
    fun mediaFastForwardSeeksForwardByTenSeconds() {
        val player = mockPlayer(currentTime = 40.0)
        setContent(player = player)

        assertTrue(dispatchKey(KeyEvent.KEYCODE_MEDIA_FAST_FORWARD))
        verify(exactly = 1) { player.currentTime = 50.0 }
    }

    @Test
    fun mediaRewindSeeksBackwardByTenSeconds() {
        val player = mockPlayer(currentTime = 40.0)
        setContent(player = player)

        assertTrue(dispatchKey(KeyEvent.KEYCODE_MEDIA_REWIND))
        verify(exactly = 1) { player.currentTime = 30.0 }
    }

    @Test
    fun mediaFastForwardIsClampedToDuration() {
        val player = mockPlayer(currentTime = 95.0, duration = 100.0)
        setContent(player = player)

        assertTrue(dispatchKey(KeyEvent.KEYCODE_MEDIA_FAST_FORWARD))
        verify(exactly = 1) { player.currentTime = 100.0 }
    }

    @Test
    fun mediaRewindIsClampedToZero() {
        val player = mockPlayer(currentTime = 5.0, duration = 100.0)
        setContent(player = player)

        assertTrue(dispatchKey(KeyEvent.KEYCODE_MEDIA_REWIND))
        verify(exactly = 1) { player.currentTime = 0.0 }
    }

    @Test
    fun mediaSeekIsSkippedDuringAds() {
        val player = mockPlayer(currentTime = 40.0, adPlaying = true)
        setContent(player = player)

        assertTrue(dispatchKey(KeyEvent.KEYCODE_MEDIA_FAST_FORWARD))
        verify(exactly = 0) { player.currentTime = any() }
    }

    @Test
    fun mediaSeekIsSkippedForUnknownDuration() {
        val player = mockPlayer(currentTime = 40.0, duration = Double.NaN)
        setContent(player = player)

        assertTrue(dispatchKey(KeyEvent.KEYCODE_MEDIA_FAST_FORWARD))
        verify(exactly = 0) { player.currentTime = any() }
    }

    @Test
    fun backPassesThroughWithoutShowingControls() {
        var showControlsCalls = 0
        var childSawKey = false
        setContent(
            controlsVisible = { false },
            showControls = { showControlsCalls++ },
            onChildKey = {
                childSawKey = true
                false
            }
        )

        dispatchKey(KeyEvent.KEYCODE_BACK)
        assertEquals(0, showControlsCalls)
        assertTrue(childSawKey)
    }

    @Test
    fun keysFromThePlayerControlsComposeViewReachTheContainerModifier() {
        val player = mockPlayer()
        val focusRequester = FocusRequester()
        val showControlsCalls = mutableIntStateOf(0)
        var focused = false
        var childSawKey = false
        lateinit var theoplayerView: THEOplayerView

        composeRule.setContent {
            val renderCount = showControlsCalls.intValue
            val view = remember { THEOplayerView(composeRule.activity) }
            theoplayerView = view

            Box(Modifier.fillMaxSize()) {
                PlayerContainer(
                    theoplayerView = view,
                    modifier = Modifier
                        .fillMaxSize()
                        .handlePlayerKeys(
                            player = player,
                            controlsVisible = { true },
                            showControls = { showControlsCalls.intValue++ }
                        )
                ) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .focusRequester(focusRequester)
                            .onFocusChanged { focused = it.isFocused }
                            .onKeyEvent {
                                childSawKey = true
                                true
                            }
                            .focusable()
                    )
                }
                Spacer(Modifier.size(renderCount.dp))
            }
        }

        composeRule.waitUntil(10_000) {
            val playerUiContainer = theoplayerView.findViewById<ViewGroup>(
                com.theoplayer.android.R.id.theo_ui_container
            )
            (0 until playerUiContainer.childCount)
                .any { playerUiContainer.getChildAt(it) is ComposeView }
        }
        composeRule.runOnIdle { focusRequester.requestFocus() }
        composeRule.waitUntil(10_000) { focused }

        val container = theoplayerView.findViewById<ViewGroup>(
            com.theoplayer.android.R.id.theo_ui_container
        )
        val hostedComposeView = (0 until container.childCount)
            .map(container::getChildAt)
            .filterIsInstance<ComposeView>()
            .single()
        val hostedComposeViewIndex = container.indexOfChild(hostedComposeView)
        var addedViews = 0
        var removedViews = 0
        container.setOnHierarchyChangeListener(object : ViewGroup.OnHierarchyChangeListener {
            override fun onChildViewAdded(parent: View?, child: View?) {
                addedViews++
            }

            override fun onChildViewRemoved(parent: View?, child: View?) {
                removedViews++
            }
        })

        assertTrue(
            composeRule.activity.dispatchKeyEvent(
                KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_DOWN)
            )
        )
        composeRule.waitForIdle()

        assertEquals(1, showControlsCalls.intValue)
        assertTrue(focused)
        assertTrue(childSawKey)
        assertSame(hostedComposeView, container.getChildAt(hostedComposeViewIndex))
        assertEquals(0, addedViews)
        assertEquals(0, removedViews)
    }

    private fun setContent(
        player: Player? = null,
        controlsVisible: () -> Boolean = { false },
        showControls: () -> Unit = {},
        onChildKey: (ComposeKeyEvent) -> Boolean = { false }
    ) {
        val focusRequester = FocusRequester()
        composeRule.setContent {
            Box(
                Modifier
                    .fillMaxSize()
                    .handlePlayerKeys(player, controlsVisible, showControls)
            ) {
                Box(
                    Modifier
                        .size(100.dp)
                        .focusRequester(focusRequester)
                        .onKeyEvent(onChildKey)
                        .focusable()
                )
            }
        }
        composeRule.runOnIdle { focusRequester.requestFocus() }
    }

    private fun dispatchKey(keyCode: Int): Boolean = composeRule.activity.dispatchKeyEvent(
        KeyEvent(KeyEvent.ACTION_DOWN, keyCode)
    )

    private fun mockPlayer(
        paused: Boolean = true,
        ended: Boolean = false,
        currentTime: Double = 40.0,
        duration: Double = 100.0,
        adPlaying: Boolean = false
    ): Player {
        val ads = mockk<Ads>(relaxed = true)
        every { ads.isPlaying } returns adPlaying
        val player = mockk<Player>(relaxed = true)
        every { player.isPaused } returns paused
        every { player.isEnded } returns ended
        every { player.ads } returns ads
        every { player.currentTime } returns currentTime
        every { player.duration } returns duration
        return player
    }
}
