package com.theoplayer.android.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import com.theoplayer.android.api.event.EventListener
import com.theoplayer.android.api.event.player.DurationChangeEvent
import com.theoplayer.android.api.event.player.PlayerEventTypes
import com.theoplayer.android.api.event.player.SourceChangeEvent
import com.theoplayer.android.api.event.player.TimeUpdateEvent
import com.theoplayer.android.api.player.Player

/**
 * Textual representation of the current time in the content.
 *
 * - Note that this will be hidden for live content.
 * - See [Helper] for the format of the current time.
 */
@SuppressLint("AppCompatCustomView")
open class THEOCurrentTime @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = R.style.THEOCurrentTime
) : TextView(context, attributeSet, defStyleAttr, defStyleRes) {

    private var player: Player? = null
    private var isDVR: Boolean = false

    private val sourceChangeEventListener = EventListener<SourceChangeEvent> {
        isDVR = false
        text = Helper.secondsToHourFormat(0.0)
    }

    private val durationChangeEventListener = EventListener<DurationChangeEvent> {
        isDVR = (player?.duration?.isInfinite() == true) && (player?.seekable?.length()?.equals(0) == false)
    }

    private val timeUpdateEventListener = EventListener<TimeUpdateEvent> {
        val currentTime: Double
        if (isDVR) {
            val playerTime = player?.currentTime ?: 0.0
            val seekableEnd = this.player?.seekable?.getEnd(0) ?: 0.0
            currentTime = playerTime - seekableEnd
        } else {
            currentTime = it.currentTime
        }

        text = Helper.secondsToHourFormat(currentTime)
    }

    fun setPlayer(player: Player) {
        this.player = player
        this.player?.addEventListener(PlayerEventTypes.SOURCECHANGE, sourceChangeEventListener)
        this.player?.addEventListener(PlayerEventTypes.DURATIONCHANGE, durationChangeEventListener)
        this.player?.addEventListener(PlayerEventTypes.TIMEUPDATE, timeUpdateEventListener)
    }

    fun release() {
        player?.removeEventListener(PlayerEventTypes.SOURCECHANGE, sourceChangeEventListener)
        player?.removeEventListener(PlayerEventTypes.DURATIONCHANGE, durationChangeEventListener)
        player?.removeEventListener(PlayerEventTypes.TIMEUPDATE, timeUpdateEventListener)
        player = null
        text = null
    }

}