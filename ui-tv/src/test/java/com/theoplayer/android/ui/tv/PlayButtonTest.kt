package com.theoplayer.android.ui.tv

import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import com.theoplayer.android.api.event.Event
import com.theoplayer.android.api.event.EventType
import com.theoplayer.android.api.event.player.EndedEvent
import com.theoplayer.android.api.event.player.PlayEvent
import com.theoplayer.android.api.event.player.PlayerEventTypes
import com.theoplayer.android.ui.LocalTHEOplayerView
import com.theoplayer.android.ui.common.R
import com.theoplayer.android.ui.tv.theme.THEOplayerTvTheme
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@OptIn(ExperimentalTvUiApi::class)
class PlayButtonTest {
    @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun pausedPlayerShowsPlayAndClickStartsPlayback() {
        val player = TestPlayer(paused = true)
        val focusRequester = FocusRequester()
        setPlayButton(player, Modifier.focusRequester(focusRequester))
        focusPlayButton(focusRequester)
        val playDescription = composeRule.activity.getString(R.string.theoplayer_ui_btn_play)

        composeRule.onNodeWithContentDescription(playDescription).assertExists()
        pressCenter()

        verify(exactly = 1) { player.player.play() }
    }

    @Test
    fun playingPlayerShowsPauseAndClickPausesPlayback() {
        val player = TestPlayer(paused = true)
        val focusRequester = FocusRequester()
        setPlayButton(player, Modifier.focusRequester(focusRequester))
        focusPlayButton(focusRequester)
        player.paused = false
        dispatch(player, PlayerEventTypes.PLAY, mockk<PlayEvent>())
        val pauseDescription = composeRule.activity.getString(R.string.theoplayer_ui_btn_pause)

        composeRule.onNodeWithContentDescription(pauseDescription).assertExists()
        pressCenter()

        verify(exactly = 1) { player.player.pause() }
    }

    @Test
    fun endedPlayerShowsReplay() {
        val player = TestPlayer(paused = true)
        setPlayButton(player)
        player.ended = true
        dispatch(player, PlayerEventTypes.ENDED, mockk<EndedEvent>())

        val replayDescription = composeRule.activity.getString(R.string.theoplayer_ui_btn_replay)
        composeRule.onNodeWithContentDescription(replayDescription).assertExists()
    }

    private fun setPlayButton(player: TestPlayer, modifier: Modifier = Modifier) {
        composeRule.setContent {
            val view = remember { PlayerBackedTHEOplayerView(composeRule.activity, player.player) }
            CompositionLocalProvider(LocalTHEOplayerView provides view) {
                THEOplayerTvTheme { PlayButton(modifier = modifier.testTag("play-button")) }
            }
        }
        composeRule.waitForIdle()
    }

    private fun focusPlayButton(focusRequester: FocusRequester) {
        composeRule.runOnIdle { focusRequester.requestFocus() }
        composeRule.onNodeWithTag("play-button").assertIsFocused()
    }

    private fun pressCenter() {
        assertTrue(
            dispatchKeyEvent(
                composeRule.activity,
                KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_CENTER),
            )
        )
        assertTrue(
            dispatchKeyEvent(
                composeRule.activity,
                KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_CENTER),
            )
        )
    }

    private fun dispatch(player: TestPlayer, eventType: EventType<*>, event: Event<*>) {
        composeRule.runOnIdle { player.dispatch(eventType, event) }
        composeRule.waitForIdle()
    }
}
