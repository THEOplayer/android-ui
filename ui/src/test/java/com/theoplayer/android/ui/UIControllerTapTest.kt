package com.theoplayer.android.ui

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import com.theoplayer.android.api.THEOplayerView
import com.theoplayer.android.api.ads.Ads
import com.theoplayer.android.api.event.EventListener
import com.theoplayer.android.api.event.EventType
import com.theoplayer.android.api.event.player.PlayEvent
import com.theoplayer.android.api.event.player.PlayerEvent
import com.theoplayer.android.api.event.player.PlayerEventTypes
import com.theoplayer.android.api.player.Player as NativePlayer
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class UIControllerTapTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun tapShowsControlsAfterAutoHideAndTapAgainHides() {
        val mock = MockPlayer(paused = true)
        val theoplayerView = PlayerBackedTHEOplayerView(composeRule.activity, mock.player)

        composeRule.setContent {
            Box(Modifier.size(300.dp)) {
                UIController(
                    modifier = Modifier.fillMaxSize().testTag("controller"),
                    theoplayerView = theoplayerView,
                    bottomChrome = {
                        Box(Modifier.size(1.dp).testTag("controls-visible"))
                    }
                )
            }
        }
        composeRule.waitForIdle()

        // A tap before playback starts (e.g. on the play button) launches the
        // pointerInput coroutine, which captures the controls state as it is at this moment.
        composeRule.onNodeWithTag("controller").performTouchInput { click() }
        composeRule.waitForIdle()

        // Start playback after the first composition.
        composeRule.runOnIdle {
            mock.paused = false
            mock.firePlayerEvent(PlayerEventTypes.PLAY)
        }
        composeRule.waitForIdle()

        // isReady delay (50ms) + controlsHideDelay (2s) + controlsExitDuration (500ms).
        composeRule.mainClock.advanceTimeBy(3_000)
        composeRule.waitForIdle()
        assertTrue(
            composeRule.onAllNodesWithTag("controls-visible")
                .fetchSemanticsNodes()
                .isEmpty()
        )

        // Tap to show controls.
        composeRule.onNodeWithTag("controller").performTouchInput { click() }
        composeRule.mainClock.advanceTimeBy(1_000)
        composeRule.waitForIdle()
        assertTrue(
            composeRule.onAllNodesWithTag("controls-visible")
                .fetchSemanticsNodes()
                .isNotEmpty()
        )

        // Tap again to hide.
        composeRule.onNodeWithTag("controller").performTouchInput { click() }
        composeRule.mainClock.advanceTimeBy(1_000)
        composeRule.waitForIdle()
        assertTrue(
            composeRule.onAllNodesWithTag("controls-visible")
                .fetchSemanticsNodes()
                .isEmpty()
        )
    }

    private class PlayerBackedTHEOplayerView(
        context: Context,
        private val nativePlayer: NativePlayer
    ) : THEOplayerView(context) {
        override fun getPlayer(): NativePlayer = nativePlayer
    }

    /**
     * A mocked [NativePlayer] with a mutable [paused] state that records the
     * listeners added to it, so tests can fire events at them.
     */
    private class MockPlayer(var paused: Boolean = true) {
        val listeners = mutableListOf<Pair<EventType<*>, EventListener<*>>>()

        val ads: Ads = mockk(relaxed = true) {
            every { isPlaying } returns false
        }

        val player: NativePlayer = mockk(relaxed = true) {
            every { isPaused } answers { this@MockPlayer.paused }
            every { isEnded } returns false
            every { getAds() } returns this@MockPlayer.ads
            every {
                addEventListener(any<EventType<out PlayerEvent<*>>>(), any())
            } answers {
                listeners.add(firstArg<EventType<*>>() to secondArg<EventListener<*>>())
                Unit
            }
            every {
                removeEventListener(any<EventType<out PlayerEvent<*>>>(), any())
            } answers {
                listeners.remove(firstArg<EventType<*>>() to secondArg<EventListener<*>>())
                Unit
            }
        }

        fun firePlayerEvent(type: EventType<*>) {
            listeners.toList().forEach { (eventType, listener) ->
                if (eventType == type) {
                    @Suppress("UNCHECKED_CAST")
                    (listener as EventListener<PlayerEvent<*>>).handleEvent(
                        if (type == PlayerEventTypes.PLAY) mockk<PlayEvent>() else mockk()
                    )
                }
            }
        }
    }
}
