package com.theoplayer.android.ui.tv

import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.theoplayer.android.api.event.player.TimeUpdateEvent
import com.theoplayer.android.api.event.player.PlayerEventTypes
import com.theoplayer.android.ui.LocalTHEOplayerView
import com.theoplayer.android.ui.tv.theme.THEOplayerTvTheme
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
@OptIn(ExperimentalTvUiApi::class)
class SeekBarTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun seekBarIsFocusable() {
        val player = TestPlayer()
        val focusRequester = FocusRequester()
        setSeekBar(player, Modifier.focusRequester(focusRequester).testTag("seek-bar"))

        composeRule.runOnIdle { focusRequester.requestFocus() }
        composeRule.onNodeWithTag("seek-bar").assertIsFocused()
    }

    @Test
    fun singleRightPressSeeksWithoutPausing() {
        val player = TestPlayer()
        val focusRequester = FocusRequester()
        setSeekBar(player, Modifier.focusRequester(focusRequester))
        requestFocus(focusRequester)

        assertTrue(dispatchKey(KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.ACTION_DOWN))
        assertTrue(dispatchKey(KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.ACTION_UP))

        assertEquals(60.0, player.currentTime, 0.0)
        verify(exactly = 0) { player.player.pause() }
    }

    @Test
    fun singleLeftPressClampsToSeekableStart() {
        val player = TestPlayer(currentTime = 5.0)
        val focusRequester = FocusRequester()
        setSeekBar(player, Modifier.focusRequester(focusRequester))
        requestFocus(focusRequester)

        assertTrue(dispatchKey(KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.ACTION_DOWN))
        assertTrue(dispatchKey(KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.ACTION_UP))

        assertEquals(0.0, player.currentTime, 0.0)
        verify(exactly = 0) { player.player.pause() }
    }

    @Test
    fun heldRightPressScrubsAndResumesPlayback() {
        val player = TestPlayer()
        val focusRequester = FocusRequester()
        setSeekBar(player, Modifier.focusRequester(focusRequester))
        requestFocus(focusRequester)

        assertTrue(dispatchKey(KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.ACTION_DOWN, repeat = 0))
        composeRule.runOnIdle {
            player.dispatch(PlayerEventTypes.TIMEUPDATE, mockk<TimeUpdateEvent>())
        }
        composeRule.waitForIdle()
        val positions = mutableListOf(player.currentTime)
        for (repeat in 1..3) {
            assertTrue(
                dispatchKey(
                    KeyEvent.KEYCODE_DPAD_RIGHT,
                    KeyEvent.ACTION_DOWN,
                    repeat = repeat
                )
            )
            positions += player.currentTime
        }
        assertTrue(dispatchKey(KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.ACTION_UP))

        assertTrue(positions.zipWithNext().all { (previous, next) -> next > previous })
        verify(exactly = 1) { player.player.pause() }
        verify(exactly = 1) { player.player.play() }
    }

    @Test
    fun heldScrubResumesPlaybackWhenSeekBarBecomesDisabled() {
        val player = TestPlayer()
        val focusRequester = FocusRequester()
        setSeekBar(player, Modifier.focusRequester(focusRequester))
        requestFocus(focusRequester)

        assertTrue(dispatchKey(KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.ACTION_DOWN, repeat = 0))
        composeRule.runOnIdle {
            player.dispatch(PlayerEventTypes.TIMEUPDATE, mockk<TimeUpdateEvent>())
        }
        composeRule.waitForIdle()
        assertTrue(dispatchKey(KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.ACTION_DOWN, repeat = 1))

        player.seekableRanges = emptyList()
        composeRule.runOnIdle {
            player.dispatch(PlayerEventTypes.TIMEUPDATE, mockk<TimeUpdateEvent>())
        }
        composeRule.waitForIdle()

        assertTrue(dispatchKey(KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.ACTION_UP))

        verify(exactly = 1) { player.player.pause() }
        verify(exactly = 1) { player.player.play() }
    }

    @Test
    fun heldScrubResumesPlaybackWhenFocusLeavesSeekBar() {
        val player = TestPlayer()
        val focusRequester = FocusRequester()
        val otherFocusRequester = FocusRequester()
        setSeekBar(
            player,
            Modifier.focusRequester(focusRequester),
            otherFocusRequester
        )
        requestFocus(focusRequester)

        assertTrue(dispatchKey(KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.ACTION_DOWN, repeat = 0))
        composeRule.runOnIdle {
            player.dispatch(PlayerEventTypes.TIMEUPDATE, mockk<TimeUpdateEvent>())
        }
        composeRule.waitForIdle()
        assertTrue(dispatchKey(KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.ACTION_DOWN, repeat = 1))

        composeRule.runOnIdle { otherFocusRequester.requestFocus() }
        composeRule.waitForIdle()

        verify(exactly = 1) { player.player.pause() }
        verify(exactly = 1) { player.player.play() }
    }

    @Test
    fun disabledSeekBarDoesNotConsumeOrSeek() {
        val player = TestPlayer(seekableRanges = emptyList())
        val focusRequester = FocusRequester()
        setSeekBar(player, Modifier.focusRequester(focusRequester))
        requestFocus(focusRequester)

        assertFalse(dispatchKey(KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.ACTION_DOWN))
        assertEquals(50.0, player.currentTime, 0.0)
        verify(exactly = 0) { player.player.currentTime = any() }
    }

    private fun setSeekBar(
        player: TestPlayer,
        modifier: Modifier = Modifier,
        otherFocusRequester: FocusRequester? = null
    ) {
        composeRule.setContent {
            val view = remember { PlayerBackedTHEOplayerView(composeRule.activity, player.player) }
            CompositionLocalProvider(LocalTHEOplayerView provides view) {
                THEOplayerTvTheme {
                    Column {
                        SeekBar(modifier = modifier)
                        if (otherFocusRequester != null) {
                            Box(
                                Modifier
                                    .size(16.dp)
                                    .focusRequester(otherFocusRequester)
                                    .focusable()
                            )
                        }
                    }
                }
            }
        }
        composeRule.waitForIdle()
    }

    private fun requestFocus(focusRequester: FocusRequester) {
        composeRule.runOnIdle { focusRequester.requestFocus() }
    }

    private fun dispatchKey(
        keyCode: Int,
        action: Int,
        repeat: Int = 0
    ): Boolean = dispatchKeyEvent(
        composeRule.activity,
        KeyEvent(1_000L, eventTime++, action, keyCode, repeat)
    )

    private var eventTime = 1_001L
}
