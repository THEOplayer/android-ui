package com.theoplayer.android.ui

import com.theoplayer.android.api.ads.Ads
import com.theoplayer.android.api.event.Event
import com.theoplayer.android.api.event.EventListener
import com.theoplayer.android.api.event.EventType
import com.theoplayer.android.api.event.ads.AdEvent
import com.theoplayer.android.api.event.ads.AdsEventTypes
import com.theoplayer.android.api.event.player.PlayerEvent
import com.theoplayer.android.api.event.player.PlayerEventTypes
import com.theoplayer.android.api.player.Player
import com.theoplayer.android.api.timerange.TimeRange
import com.theoplayer.android.api.timerange.TimeRanges
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * A mocked [Player] that records the listeners added to it and to its [Ads],
 * so tests can fire events at them.
 */
private class MockPlayer(
    var paused: Boolean = true,
    var ended: Boolean = false,
    var currentTime: Double = 0.0,
    var duration: Double = 100.0,
    var seekableRanges: List<Pair<Double, Double>> = listOf(0.0 to 100.0),
    var adPlaying: Boolean = false,
) {
    val playerListeners = mutableListOf<Pair<EventType<*>, EventListener<*>>>()
    val adListeners = mutableListOf<Pair<EventType<*>, EventListener<*>>>()

    val ads: Ads = mockk {
        every { isPlaying } answers { this@MockPlayer.adPlaying }
        every {
            addEventListener(any<EventType<out AdEvent<*>>>(), any())
        } answers {
            adListeners.add(firstArg<EventType<*>>() to secondArg<EventListener<*>>())
            Unit
        }
        every {
            removeEventListener(any<EventType<out AdEvent<*>>>(), any())
        } answers {
            adListeners.remove(firstArg<EventType<*>>() to secondArg<EventListener<*>>())
            Unit
        }
    }

    val player: Player = mockk {
        every { isPaused } answers { this@MockPlayer.paused }
        every { isEnded } answers { this@MockPlayer.ended }
        every { getCurrentTime() } answers { this@MockPlayer.currentTime }
        every { getDuration() } answers { this@MockPlayer.duration }
        every { getSeekable() } answers { mockTimeRanges(this@MockPlayer.seekableRanges) }
        every { getAds() } returns this@MockPlayer.ads
        every { setCurrentTime(any()) } answers { this@MockPlayer.currentTime = firstArg() }
        every { play() } answers { this@MockPlayer.paused = false }
        every { pause() } answers { this@MockPlayer.paused = true }
        every {
            addEventListener(any<EventType<out PlayerEvent<*>>>(), any())
        } answers {
            playerListeners.add(firstArg<EventType<*>>() to secondArg<EventListener<*>>())
            Unit
        }
        every {
            removeEventListener(any<EventType<out PlayerEvent<*>>>(), any())
        } answers {
            playerListeners.remove(firstArg<EventType<*>>() to secondArg<EventListener<*>>())
            Unit
        }
    }

    val playerEventTypes: Set<EventType<*>>
        get() = playerListeners.map { it.first }.toSet()

    val adEventTypes: Set<EventType<*>>
        get() = adListeners.map { it.first }.toSet()

    fun firePlayerEvent(type: EventType<*>? = null) {
        playerListeners.toList().forEach { (eventType, listener) ->
            if (type == null || eventType == type) {
                @Suppress("UNCHECKED_CAST")
                (listener as EventListener<PlayerEvent<*>>).handleEvent(mockk())
            }
        }
    }

    fun fireAdEvent() {
        adListeners.toList().forEach { (_, listener) ->
            @Suppress("UNCHECKED_CAST")
            (listener as EventListener<AdEvent<*>>).handleEvent(mockk())
        }
    }
}

private fun mockTimeRanges(ranges: List<Pair<Double, Double>>): TimeRanges = mockk {
    every { iterator() } answers {
        ranges.map { (start, end) ->
            mockk<TimeRange> {
                every { this@mockk.start } returns start
                every { this@mockk.end } returns end
            }
        }.toMutableList().listIterator()
    }
}

private val playbackEventTypes = setOf(
    PlayerEventTypes.TIMEUPDATE,
    PlayerEventTypes.PLAY,
    PlayerEventTypes.PAUSE,
    PlayerEventTypes.ENDED,
    PlayerEventTypes.SEEKING,
    PlayerEventTypes.SEEKED,
    PlayerEventTypes.READYSTATECHANGE,
    PlayerEventTypes.SOURCECHANGE,
    PlayerEventTypes.ERROR
)

private val adEventTypes = setOf(
    AdsEventTypes.AD_BREAK_BEGIN,
    AdsEventTypes.AD_SKIP,
    AdsEventTypes.AD_BREAK_END
)

class PlayButtonStateTest {
    @Test
    fun `registers the playback events`() {
        val mock = MockPlayer()
        PlayButtonState(mock.player).observe()
        assertEquals(playbackEventTypes - PlayerEventTypes.TIMEUPDATE, mock.playerEventTypes)
    }

    @Test
    fun `a seeked event clears ended`() {
        val mock = MockPlayer(paused = true, ended = true)
        val state = PlayButtonState(mock.player)
        state.observe()
        assertTrue(state.ended)

        mock.ended = false
        mock.firePlayerEvent(PlayerEventTypes.SEEKED)
        assertFalse(state.ended)
        assertTrue(state.paused)
    }

    @Test
    fun `seeds state and reacts to playback events`() {
        val mock = MockPlayer(paused = true, ended = false)
        val state = PlayButtonState(mock.player)
        state.observe()

        assertTrue(state.paused)
        assertFalse(state.ended)

        mock.paused = false
        mock.firePlayerEvent()
        assertFalse(state.paused)

        mock.ended = true
        mock.firePlayerEvent()
        assertTrue(state.ended)
    }

    @Test
    fun `onClick plays when paused and pauses when playing`() {
        val mock = MockPlayer(paused = true)
        val state = PlayButtonState(mock.player)
        state.observe()

        state.onClick()
        verify(exactly = 1) { mock.player.play() }

        mock.firePlayerEvent()
        state.onClick()
        verify(exactly = 1) { mock.player.pause() }
    }

    @Test
    fun `dispose removes all listeners`() {
        val mock = MockPlayer()
        val state = PlayButtonState(mock.player)
        state.observe()
        val added = mock.playerListeners.toList()
        assertTrue(added.isNotEmpty())

        state.dispose()
        assertTrue(mock.playerListeners.isEmpty())
        verify(exactly = added.size) {
            mock.player.removeEventListener(any<EventType<out PlayerEvent<*>>>(), any())
        }
    }

    @Test
    fun `shares one listener instance and removes it for every type`() {
        val mock = MockPlayer()
        val state = PlayButtonState(mock.player)
        state.observe()

        val added = mock.playerListeners.toList()
        val listener = added.first().second
        assertTrue(added.all { it.second === listener })

        state.dispose()
        added.forEach { (type, _) ->
            @Suppress("UNCHECKED_CAST")
            verify {
                mock.player.removeEventListener(
                    type as EventType<PlayerEvent<*>>,
                    listener as EventListener<PlayerEvent<*>>
                )
            }
        }
    }

    @Test
    fun `null player gives defaults and no listeners`() {
        val state = PlayButtonState(null)
        state.observe()
        assertTrue(state.paused)
        state.onClick()
        state.dispose()
    }
}

class SeekBarStateTest {
    @Test
    fun `registers the playback and ad events`() {
        val mock = MockPlayer()
        SeekBarState(mock.player).observe()
        assertEquals(
            playbackEventTypes + PlayerEventTypes.DURATIONCHANGE,
            mock.playerEventTypes
        )
        assertEquals(adEventTypes, mock.adEventTypes)
    }

    @Test
    fun `an error event re-reads playingAd`() {
        val mock = MockPlayer(adPlaying = false)
        val state = SeekBarState(mock.player)
        state.observe()
        assertTrue(state.showThumb)

        mock.adPlaying = true
        mock.firePlayerEvent(PlayerEventTypes.ERROR)
        assertFalse(state.enabled)
        assertFalse(state.showThumb)
    }

    @Test
    fun `computes value within zero-based range`() {
        val mock = MockPlayer(currentTime = 30.0, duration = 100.0)
        val state = SeekBarState(mock.player)
        state.observe()

        assertEquals(0f..100f, state.valueRange)
        assertEquals(30f, state.value)
        assertTrue(state.enabled)
        assertTrue(state.showThumb)
    }

    @Test
    fun `huge seekable start maps to zero-based range`() {
        val mock = MockPlayer(
            currentTime = 1.7e9 + 30.0,
            duration = 1.7e9 + 100.0,
            seekableRanges = listOf(1.7e9 to 1.7e9 + 100.0)
        )
        val state = SeekBarState(mock.player)
        state.observe()

        assertEquals(0f, state.valueRange.start)
        assertEquals(100f, state.valueRange.endInclusive, 0.001f)
        assertEquals(30f, state.value, 0.001f)
    }

    @Test
    fun `scrubbing pauses a playing player and resumes on finish`() {
        val mock = MockPlayer(paused = false, currentTime = 10.0)
        val state = SeekBarState(mock.player)
        state.observe()

        state.onValueChange(50f)
        verify(exactly = 1) { mock.player.pause() }
        verify { mock.player.currentTime = 50.0 }
        assertEquals(50f, state.value)

        state.onValueChangeFinished()
        verify(exactly = 1) { mock.player.play() }
    }

    @Test
    fun `scrubbing a paused player does not resume it`() {
        val mock = MockPlayer(paused = true, currentTime = 10.0)
        val state = SeekBarState(mock.player)
        state.observe()

        state.onValueChange(50f)
        state.onValueChangeFinished()
        verify(exactly = 0) { mock.player.play() }
    }

    @Test
    fun `disabled during ads and thumb hidden`() {
        val mock = MockPlayer(adPlaying = true)
        val state = SeekBarState(mock.player)
        state.observe()

        assertFalse(state.enabled)
        assertFalse(state.showThumb)

        mock.adPlaying = false
        mock.fireAdEvent()
        assertTrue(state.enabled)
        assertTrue(state.showThumb)
    }

    @Test
    fun `dispose removes all listeners`() {
        val mock = MockPlayer()
        val state = SeekBarState(mock.player)
        state.observe()
        val added = mock.playerListeners.toList()
        assertTrue(added.isNotEmpty())
        assertTrue(mock.adListeners.isNotEmpty())

        state.dispose()
        assertTrue(mock.playerListeners.isEmpty())
        assertTrue(mock.adListeners.isEmpty())
    }
}

class PlaybackTimeStateTest {
    @Test
    fun `registers the playback events`() {
        val mock = MockPlayer()
        PlaybackTimeState(mock.player).observe()
        assertEquals(
            playbackEventTypes + PlayerEventTypes.DURATIONCHANGE,
            mock.playerEventTypes
        )
    }

    @Test
    fun `streamTypeOf classifies vod, live and dvr`() {
        assertEquals(
            StreamType.Vod,
            streamTypeOf(100.0, com.theoplayer.android.ui.TimeRanges(listOf(0.0..100.0)))
        )
        assertEquals(
            StreamType.Vod,
            streamTypeOf(Double.NaN, com.theoplayer.android.ui.TimeRanges.empty())
        )
        assertEquals(
            StreamType.Live,
            streamTypeOf(
                Double.POSITIVE_INFINITY,
                com.theoplayer.android.ui.TimeRanges(listOf(0.0..30.0))
            )
        )
        assertEquals(
            StreamType.Dvr,
            streamTypeOf(
                Double.POSITIVE_INFINITY,
                com.theoplayer.android.ui.TimeRanges(listOf(0.0..60.0))
            )
        )
    }

    @Test
    fun `seeds and updates on time events`() {
        val mock = MockPlayer(currentTime = 5.0, duration = 100.0)
        val state = PlaybackTimeState(mock.player)
        state.observe()

        assertEquals(5.0, state.currentTime, 0.0)
        assertEquals(100.0, state.duration, 0.0)

        mock.currentTime = 42.0
        mock.firePlayerEvent()
        assertEquals(42.0, state.currentTime, 0.0)
    }

    @Test
    fun `duration prefers end of seekable range`() {
        val mock = MockPlayer(
            duration = Double.POSITIVE_INFINITY,
            seekableRanges = listOf(10.0 to 70.0)
        )
        val state = PlaybackTimeState(mock.player)
        state.observe()

        assertEquals(70.0, state.duration, 0.0)
    }

    @Test
    fun `reports live and dvr stream types`() {
        val live = MockPlayer(duration = Double.POSITIVE_INFINITY, seekableRanges = listOf(0.0 to 30.0))
        val dvr = MockPlayer(duration = Double.POSITIVE_INFINITY, seekableRanges = listOf(0.0 to 120.0))
        val vod = MockPlayer(duration = 100.0)

        assertEquals(StreamType.Live, PlaybackTimeState(live.player).apply { observe() }.streamType)
        assertEquals(StreamType.Dvr, PlaybackTimeState(dvr.player).apply { observe() }.streamType)
        assertEquals(StreamType.Vod, PlaybackTimeState(vod.player).apply { observe() }.streamType)
    }
}

class LiveButtonStateTest {
    @Test
    fun `registers the playback events`() {
        val mock = MockPlayer()
        LiveButtonState(mock.player, liveThreshold = 10.0).observe()
        assertEquals(
            playbackEventTypes + PlayerEventTypes.DURATIONCHANGE,
            mock.playerEventTypes
        )
    }

    @Test
    fun `visible for live but not for vod`() {
        val live = MockPlayer(duration = Double.POSITIVE_INFINITY, seekableRanges = listOf(0.0 to 30.0))
        val vod = MockPlayer(duration = 100.0)

        assertTrue(LiveButtonState(live.player, liveThreshold = 10.0).apply { observe() }.visible)
        assertFalse(LiveButtonState(vod.player, liveThreshold = 10.0).apply { observe() }.visible)
    }

    @Test
    fun `isLive at threshold boundary`() {
        val mock = MockPlayer(
            paused = false,
            duration = Double.POSITIVE_INFINITY,
            currentTime = 20.0,
            seekableRanges = listOf(0.0 to 30.0)
        )
        val atThreshold = LiveButtonState(mock.player, liveThreshold = 10.0)
        atThreshold.observe()
        assertTrue(atThreshold.isLive)

        mock.currentTime = 19.9
        val behindThreshold = LiveButtonState(mock.player, liveThreshold = 10.0)
        behindThreshold.observe()
        assertFalse(behindThreshold.isLive)
    }

    @Test
    fun `onClick seeks to live and plays`() {
        val mock = MockPlayer(duration = Double.POSITIVE_INFINITY)
        val state = LiveButtonState(mock.player, liveThreshold = 10.0)
        state.observe()

        state.onClick()
        verify { mock.player.currentTime = Double.POSITIVE_INFINITY }
        verify(exactly = 1) { mock.player.play() }
    }
}

class SeekButtonStateTest {
    @Test
    fun `registers the playback and ad events`() {
        val mock = MockPlayer()
        SeekButtonState(mock.player, seekOffset = 10).observe()
        assertEquals(playbackEventTypes, mock.playerEventTypes)
        assertEquals(adEventTypes, mock.adEventTypes)
    }

    @Test
    fun `onClick seeks by offset and clamps to duration`() {
        val mock = MockPlayer(currentTime = 95.0, duration = 100.0)
        val state = SeekButtonState(mock.player, seekOffset = 10)
        state.observe()

        state.onClick()
        verify { mock.player.currentTime = 100.0 }
    }

    @Test
    fun `onClick clamps negative seek to zero`() {
        val mock = MockPlayer(currentTime = 5.0, duration = 100.0)
        val state = SeekButtonState(mock.player, seekOffset = -10)
        state.observe()

        state.onClick()
        verify { mock.player.currentTime = 0.0 }
    }

    @Test
    fun `onClick does nothing for streams without duration`() {
        val mock = MockPlayer(currentTime = 50.0, duration = Double.NaN)
        val state = SeekButtonState(mock.player, seekOffset = 10)
        state.observe()

        state.onClick()
        verify(exactly = 0) { mock.player.currentTime = any() }
    }

    @Test
    fun `disabled during ads`() {
        val mock = MockPlayer(adPlaying = true)
        val state = SeekButtonState(mock.player, seekOffset = 10)
        state.observe()

        assertFalse(state.enabled)
    }

    @Test
    fun `null player is disabled and onClick does nothing`() {
        val state = SeekButtonState(null, seekOffset = 10)
        state.observe()
        assertFalse(state.enabled)
        state.onClick()
        state.dispose()
    }
}
