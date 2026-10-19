package com.theoplayer.android.ui.tv

import android.view.KeyEvent
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import com.theoplayer.android.api.THEOplayerView
import com.theoplayer.android.api.ads.Ads
import com.theoplayer.android.api.player.Player
import com.theoplayer.android.api.timerange.TimeRanges
import com.theoplayer.android.ui.tv.theme.THEOplayerTvTheme
import com.theoplayer.android.api.player.track.texttrack.TextTrackStyle
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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
class DefaultUITest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val seekBar = SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo)

    @Test
    fun beforeFirstPlayShowsOnlyPlayButton() {
        setDefaultUI(mockPlayer(paused = true, played = false))

        waitUntil { hasContentDescription("Play") }
        assertFalse(hasText(TITLE))
        assertFalse(hasContentDescription("Settings"))
        assertTrue(composeRule.onAllNodes(seekBar).fetchSemanticsNodes().isEmpty())
    }

    @Test
    fun afterFirstPlayShowsTitleTimesSeekBarAndSettings() {
        setDefaultUI(mockPlayer(paused = true, played = true))

        waitUntil { hasText(TITLE) }
        assertTrue(hasText("00:40"))
        assertTrue(hasText("01:40"))
        assertTrue(hasContentDescription("Play"))
        assertTrue(hasContentDescription("Settings"))
        assertTrue(composeRule.onAllNodes(seekBar).fetchSemanticsNodes().isNotEmpty())
    }

    @Test
    fun seekBarIsFocusedWhenControlsAreShown() {
        setDefaultUI(mockPlayer(paused = true, played = true))

        composeRule.waitUntil(10_000) {
            composeRule.onAllNodes(seekBar and SemanticsMatcher.expectValue(SemanticsProperties.Focused, true))
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNode(seekBar).assertIsFocused()
    }

    @Test
    fun liveStreamShowsLiveButtonWithoutSeekBar() {
        setDefaultUI(mockPlayer(paused = true, played = true, duration = Double.POSITIVE_INFINITY))

        waitUntil { hasText("LIVE") }
        assertTrue(composeRule.onAllNodes(seekBar).fetchSemanticsNodes().isEmpty())
        assertFalse(hasText("01:40"))
    }

    @Test
    fun hiddenDpadCenterPausesAndShowsControls() {
        val player = mockPlayer(paused = false, played = true)
        setDefaultUI(player)
        composeRule.mainClock.advanceTimeBy(100)
        composeRule.waitForIdle()
        assertFalse(hasText(TITLE))

        assertTrue(
            composeRule.activity.dispatchKeyEvent(
                KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_CENTER)
            )
        )
        composeRule.waitForIdle()

        verify { player.pause() }
        waitUntil { hasText(TITLE) }
    }

    @Test
    fun visibleControlsMoveSubtitlesOutOfTheWay() {
        val textTrackStyle = mockk<TextTrackStyle>(relaxed = true)
        val player = mockPlayer(paused = true, played = true)
        every { player.textTrackStyle } returns textTrackStyle
        setDefaultUI(player)

        waitUntil { hasText(TITLE) }
        composeRule.waitForIdle()

        verify { textTrackStyle.marginTop = more(0) }
        verify { textTrackStyle.marginBottom = more(0) }
    }

    private fun setDefaultUI(player: Player) {
        lateinit var theoplayerView: THEOplayerView
        composeRule.setContent {
            val view = remember { PlayerBackedTHEOplayerView(composeRule.activity, player) }
            theoplayerView = view
            THEOplayerTvTheme {
                DefaultUI(
                    modifier = Modifier.fillMaxSize(),
                    theoplayerView = view,
                    title = TITLE
                )
            }
        }
        composeRule.waitUntil(10_000) {
            val container = theoplayerView.findViewById<ViewGroup>(
                com.theoplayer.android.R.id.theo_ui_container
            )
            (0 until container.childCount).any { container.getChildAt(it) is ComposeView }
        }
        composeRule.waitForIdle()
    }

    private fun waitUntil(condition: () -> Boolean) {
        composeRule.waitUntil(10_000, condition)
    }

    private fun hasText(text: String): Boolean =
        composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()

    private fun hasContentDescription(description: String): Boolean =
        composeRule.onAllNodesWithContentDescription(description).fetchSemanticsNodes().isNotEmpty()

    private fun mockPlayer(
        paused: Boolean,
        played: Boolean,
        duration: Double = 100.0
    ): Player {
        val ads = mockk<Ads>(relaxed = true)
        every { ads.isPlaying } returns false
        val playedRanges = mockk<TimeRanges>(relaxed = true)
        every { playedRanges.length() } returns if (played) 1 else 0
        val player = mockk<Player>(relaxed = true)
        every { player.isPaused } returns paused
        every { player.isEnded } returns false
        every { player.played } returns playedRanges
        every { player.ads } returns ads
        every { player.currentTime } returns 40.0
        every { player.duration } returns duration
        return player
    }

    private companion object {
        const val TITLE = "Big Buck Bunny"
    }
}
