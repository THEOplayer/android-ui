package com.theoplayer.android.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.theoplayer.android.api.player.Player as THEOplayer
import com.theoplayer.android.api.player.track.texttrack.TextTrack
import com.theoplayer.android.api.player.track.texttrack.TextTrackKind
import com.theoplayer.android.api.player.track.texttrack.TextTrackList
import com.theoplayer.android.api.player.track.texttrack.TextTrackMode
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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
class MenuListTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun playbackRateListScrollsToSelectedRate() {
        val player = playbackRatePlayer(rate = 2.0)
        composeRule.setContent {
            PlaybackRateList(
                state = rememberPlaybackRateState(player),
                modifier = Modifier.height(120.dp)
            )
        }

        assertTrue(composeRule.onAllNodes(hasText("2x")).fetchSemanticsNodes().isNotEmpty())
        assertTrue(composeRule.onAllNodes(hasText("0.25x")).fetchSemanticsNodes().isEmpty())
    }

    @Test
    fun playbackRateListExposesSelectionSemantics() {
        val player = playbackRatePlayer(rate = 1.5)
        composeRule.setContent {
            PlaybackRateList(state = rememberPlaybackRateState(player))
        }

        composeRule.onNode(hasText("1.5x") and isSelectable()).assertIsSelected()
        composeRule.onNode(hasText("1.25x") and isSelectable()).assertIsNotSelected()
    }

    @Test
    fun subtitleTrackListSelectsActiveTrackInsteadOfOff() {
        val track = mockSubtitleTrack()
        val player = subtitlePlayer(track)
        var state: SubtitleTrackState? = null
        composeRule.setContent {
            val subtitleState = rememberSubtitleTrackState(player)
            state = subtitleState
            SubtitleTrackList(state = subtitleState)
        }

        assertEquals(listOf(track), state?.subtitleTracks)
        assertEquals(track, state?.activeSubtitleTrack)
        composeRule.onNode(hasText("English") and isSelectable()).assertIsSelected()
        composeRule.onNode(hasText("Off") and isSelectable()).assertIsNotSelected()
    }

    @Test
    fun clickingPlaybackRateSelectsItAndCallsOnClick() {
        val player = playbackRatePlayer(rate = 1.0)
        var selectedState: PlaybackRateState? = null
        var clickCount = 0
        composeRule.setContent {
            val state = rememberPlaybackRateState(player)
            selectedState = state
            PlaybackRateList(state = state, onClick = { clickCount++ })
        }

        composeRule.onNodeWithText("1.5x").performClick()

        assertEquals(1.5, selectedState?.playbackRate ?: 0.0, 0.0)
        assertEquals(1, clickCount)
        verify(exactly = 1) { player.playbackRate = 1.5 }
    }

    private fun playbackRatePlayer(rate: Double): THEOplayer {
        val player = mockk<THEOplayer>(relaxed = true)
        every { player.playbackRate } returns rate
        return player
    }

    private fun mockSubtitleTrack(): TextTrack = mockk(relaxed = true) {
        every { kind } returns TextTrackKind.SUBTITLES.type
        every { mode } returns TextTrackMode.SHOWING
        every { uid } returns 1
        every { label } returns "English"
    }

    private fun subtitlePlayer(track: TextTrack): THEOplayer {
        val textTracks = mockk<TextTrackList>(relaxed = true) {
            every { iterator() } answers { listOf(track).toMutableList().listIterator() }
        }
        return mockk(relaxed = true) {
            every { this@mockk.textTracks } returns textTracks
        }
    }
}
