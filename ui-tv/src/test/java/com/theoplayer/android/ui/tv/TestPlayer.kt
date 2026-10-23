package com.theoplayer.android.ui.tv

import android.app.Activity
import android.content.Context
import android.view.KeyEvent
import com.theoplayer.android.api.THEOplayerView
import com.theoplayer.android.api.ads.Ads
import com.theoplayer.android.api.event.Event
import com.theoplayer.android.api.event.EventListener
import com.theoplayer.android.api.event.EventType
import com.theoplayer.android.api.event.ads.AdEvent
import com.theoplayer.android.api.event.player.PlayerEvent
import com.theoplayer.android.api.player.Player
import com.theoplayer.android.api.player.ReadyState
import com.theoplayer.android.api.timerange.TimeRange
import com.theoplayer.android.api.timerange.TimeRanges
import io.mockk.every
import io.mockk.mockk

internal class PlayerBackedTHEOplayerView(
    context: Context,
    private val nativePlayer: Player,
) : THEOplayerView(context) {
    override fun getPlayer(): Player = nativePlayer
}

internal class PlayerEventListeners {
    private val listeners = mutableListOf<Pair<EventType<*>, EventListener<*>>>()

    fun add(type: EventType<*>, listener: EventListener<*>) {
        listeners.add(type to listener)
    }

    fun remove(type: EventType<*>, listener: EventListener<*>) {
        listeners.remove(type to listener)
    }

    fun hasListeners(type: EventType<*>): Boolean = listeners.any { it.first == type }

    fun dispatch(type: EventType<*>, event: Event<*>) {
        listeners.toList().forEach { (eventType, listener) ->
            if (eventType == type) {
                @Suppress("UNCHECKED_CAST") (listener as EventListener<Event<*>>).handleEvent(event)
            }
        }
    }
}

internal class TestPlayer(
    var currentTime: Double = 50.0,
    var paused: Boolean = false,
    var ended: Boolean = false,
    var seekableRanges: List<Pair<Double, Double>> = listOf(0.0 to 100.0),
    private val duration: Double = 100.0,
) {
    val eventListeners = PlayerEventListeners()
    private val adEventListeners = PlayerEventListeners()
    private val ads =
        mockk<Ads> {
            every { isPlaying } returns false
            every { addEventListener(any<EventType<out AdEvent<*>>>(), any()) } answers
                {
                    adEventListeners.add(firstArg(), secondArg())
                    Unit
                }
            every { removeEventListener(any<EventType<out AdEvent<*>>>(), any()) } answers
                {
                    adEventListeners.remove(firstArg(), secondArg())
                    Unit
                }
        }
    val player: Player = mockk {
        every { isPaused } answers { this@TestPlayer.paused }
        every { isEnded } answers { this@TestPlayer.ended }
        every { getCurrentTime() } answers { this@TestPlayer.currentTime }
        every { getDuration() } answers { this@TestPlayer.duration }
        every { getSeekable() } answers { timeRanges(this@TestPlayer.seekableRanges) }
        every { getAds() } returns this@TestPlayer.ads
        every { getReadyState() } returns ReadyState.HAVE_FUTURE_DATA
        every { isSeeking } returns false
        every { setCurrentTime(any()) } answers { this@TestPlayer.currentTime = firstArg() }
        every { play() } answers
            {
                this@TestPlayer.paused = false
                Unit
            }
        every { pause() } answers
            {
                this@TestPlayer.paused = true
                Unit
            }
        every { addEventListener(any<EventType<out PlayerEvent<*>>>(), any()) } answers
            {
                eventListeners.add(firstArg(), secondArg())
                Unit
            }
        every { removeEventListener(any<EventType<out PlayerEvent<*>>>(), any()) } answers
            {
                eventListeners.remove(firstArg(), secondArg())
                Unit
            }
    }

    fun dispatch(type: EventType<*>, event: Event<*>) {
        eventListeners.dispatch(type, event)
    }
}

internal fun dispatchKeyEvent(activity: Activity, event: KeyEvent): Boolean =
    activity.dispatchKeyEvent(event)

private fun timeRanges(ranges: List<Pair<Double, Double>>): TimeRanges = mockk {
    every { iterator() } answers
        {
            ranges.map { (start, end) -> timeRange(start, end) }.toMutableList().listIterator()
        }
}

private fun timeRange(start: Double, end: Double): TimeRange {
    val range = mockk<TimeRange>()
    every { range.start } returns start
    every { range.end } returns end
    return range
}
