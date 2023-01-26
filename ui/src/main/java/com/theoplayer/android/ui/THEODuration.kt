package com.theoplayer.android.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import com.theoplayer.android.api.event.EventListener
import com.theoplayer.android.api.event.player.DurationChangeEvent
import com.theoplayer.android.api.event.player.PlayerEventTypes
import com.theoplayer.android.api.event.player.SourceChangeEvent
import com.theoplayer.android.api.player.Player

/**
 * Textual representation of the total duration of the content.
 *
 * - Note that this will be hidden for live content.
 * - See [Helper] for the format of the current time.
 */
@SuppressLint("AppCompatCustomView")
open class THEODuration @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = R.style.THEODuration
) : TextView(context, attributeSet, defStyleAttr, defStyleRes) {

    private var player: Player? = null

    private val sourceChangeEventListener = EventListener<SourceChangeEvent> {
        text = Helper.secondsToHourFormat(0.0)
    }

    private val durationChangeEventListener = EventListener<DurationChangeEvent> {
        text = Helper.secondsToHourFormat(player?.duration ?: 0.0)
    }

    fun setPlayer(player: Player) {
        this.player = player
        this.player?.addEventListener(PlayerEventTypes.SOURCECHANGE, sourceChangeEventListener)
        this.player?.addEventListener(PlayerEventTypes.DURATIONCHANGE, durationChangeEventListener)
    }

    fun release() {
        player?.removeEventListener(PlayerEventTypes.SOURCECHANGE, sourceChangeEventListener)
        player?.removeEventListener(PlayerEventTypes.DURATIONCHANGE, durationChangeEventListener)
        player = null
        text = null
    }

}