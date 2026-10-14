package com.theoplayer.android.ui

import com.theoplayer.android.api.event.player.PlayerEventTypes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Test

class AdStateTest {
    @Test
    fun `registers sourcechange error and ad events`() = runTest {
        val mock = MockPlayer()
        launchObserve { AdState(mock.player).observe() }
        assertEquals(setOf(PlayerEventTypes.SOURCECHANGE, PlayerEventTypes.ERROR), mock.playerEventTypes)
        assertEquals(adEventTypes, mock.adEventTypes)
    }

    @Test
    fun `seeds playingAd and updates on ad events and sourcechange`() = runTest {
        val mock = MockPlayer(adPlaying = true)
        val state = AdState(mock.player)
        launchObserve { state.observe() }
        assertTrue(state.playingAd)

        mock.adPlaying = false
        mock.fireAdEvent()
        assertFalse(state.playingAd)

        mock.adPlaying = true
        mock.firePlayerEvent(PlayerEventTypes.SOURCECHANGE)
        assertTrue(state.playingAd)
    }
}
