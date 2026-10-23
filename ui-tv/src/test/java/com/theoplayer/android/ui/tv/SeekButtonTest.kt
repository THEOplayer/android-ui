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
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import com.theoplayer.android.ui.LocalTHEOplayerView
import com.theoplayer.android.ui.common.R
import com.theoplayer.android.ui.tv.theme.THEOplayerTvTheme
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
class SeekButtonTest {
    @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun forwardButtonSeeksForward() {
        val player = TestPlayer(currentTime = 50.0)
        val focusRequester = FocusRequester()
        setSeekButton(player, seekOffset = 10, Modifier.focusRequester(focusRequester))
        val description =
            composeRule.activity.resources.getQuantityString(
                R.plurals.theoplayer_ui_btn_seek_forward,
                10,
                10,
            )

        composeRule.onNodeWithContentDescription(description).assertExists()
        focusSeekButton(focusRequester)
        pressCenter()

        verify(exactly = 1) { player.player.currentTime = 60.0 }
    }

    @Test
    fun backwardButtonSeeksBackward() {
        val player = TestPlayer(currentTime = 50.0)
        val focusRequester = FocusRequester()
        setSeekButton(player, seekOffset = -10, Modifier.focusRequester(focusRequester))
        val description =
            composeRule.activity.resources.getQuantityString(
                R.plurals.theoplayer_ui_btn_seek_backward,
                10,
                10,
            )

        composeRule.onNodeWithContentDescription(description).assertExists()
        focusSeekButton(focusRequester)
        pressCenter()

        verify(exactly = 1) { player.player.currentTime = 40.0 }
    }

    @Test
    fun buttonIsDisabledWhenNotSeekable() {
        val player = TestPlayer(seekableRanges = emptyList())
        setSeekButton(player, seekOffset = 10)

        composeRule.onNodeWithTag(SEEK_BUTTON).assertIsNotEnabled()
    }

    private fun setSeekButton(player: TestPlayer, seekOffset: Int, modifier: Modifier = Modifier) {
        composeRule.setContent {
            val view = remember { PlayerBackedTHEOplayerView(composeRule.activity, player.player) }
            CompositionLocalProvider(LocalTHEOplayerView provides view) {
                THEOplayerTvTheme {
                    SeekButton(modifier = modifier.testTag(SEEK_BUTTON), seekOffset = seekOffset)
                }
            }
        }
        composeRule.waitForIdle()
    }

    private fun focusSeekButton(focusRequester: FocusRequester) {
        composeRule.runOnIdle { focusRequester.requestFocus() }
        composeRule.onNodeWithTag(SEEK_BUTTON).assertIsFocused()
    }

    private fun pressCenter() {
        for (action in listOf(KeyEvent.ACTION_DOWN, KeyEvent.ACTION_UP)) {
            assertTrue(
                dispatchKeyEvent(
                    composeRule.activity,
                    KeyEvent(action, KeyEvent.KEYCODE_DPAD_CENTER),
                )
            )
        }
        composeRule.waitForIdle()
    }

    private companion object {
        const val SEEK_BUTTON = "seek-button"
    }
}
