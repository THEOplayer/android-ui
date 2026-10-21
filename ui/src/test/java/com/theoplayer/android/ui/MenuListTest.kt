package com.theoplayer.android.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
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

    @Test
    fun subtitleTrackListShowsHalfItemPeekAboveSelected() {
        val tracks = (1..12).map {
            mockSubtitleTrack(uid = it, label = "Track $it", mode = if (it == 6) TextTrackMode.SHOWING else TextTrackMode.DISABLED)
        }
        val player = subtitlePlayer(tracks = tracks)
        var state: SubtitleTrackState? = null
        composeRule.setContent {
            val subtitleState = rememberSubtitleTrackState(player)
            state = subtitleState
            SubtitleTrackList(
                state = subtitleState,
                // Bound the list height so the selected item can actually
                // reach the top of the viewport.
                modifier = Modifier.testTag("list").height(300.dp)
            )
        }
        assertEquals(tracks[5], state?.activeSubtitleTrack)
        assertEquals(6, state?.subtitleTracks?.indexOf(tracks[5])?.plus(1))

        val listBounds = composeRule.onNodeWithTag("list").getBoundsInRoot()
        // The item above the selected "Track 6" is partially scrolled off:
        // its bounds extend above the list's top edge.
        val track5Bounds = composeRule.onNodeWithText("Track 5").getBoundsInRoot()
        assertTrue(
            "top=${track5Bounds.top} listTop=${listBounds.top} bottom=${track5Bounds.bottom}",
            track5Bounds.top <= listBounds.top
        )
        assertTrue(track5Bounds.bottom > listBounds.top)
        // And earlier items are scrolled out of view entirely.
        assertTrue(composeRule.onAllNodesWithText("Track 4").fetchSemanticsNodes().isEmpty())
    }

    private fun playbackRatePlayer(rate: Double): THEOplayer {
        val player = mockk<THEOplayer>(relaxed = true)
        every { player.playbackRate } returns rate
        return player
    }

    private fun mockSubtitleTrack(
        uid: Int = 1,
        label: String = "English",
        mode: TextTrackMode = TextTrackMode.SHOWING
    ): TextTrack = mockk(relaxed = true) {
        every { this@mockk.uid } returns uid
        every { this@mockk.label } returns label
        every { kind } returns TextTrackKind.SUBTITLES.type
        every { this@mockk.mode } returns mode
    }

    private fun subtitlePlayer(track: TextTrack): THEOplayer =
        subtitlePlayer(listOf(track))

    private fun subtitlePlayer(tracks: List<TextTrack>): THEOplayer {
        val textTracks = mockk<TextTrackList>(relaxed = true) {
            every { iterator() } answers { tracks.toMutableList().listIterator() }
        }
        return mockk(relaxed = true) {
            every { this@mockk.textTracks } returns textTracks
        }
    }
}
