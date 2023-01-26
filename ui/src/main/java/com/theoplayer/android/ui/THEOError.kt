package com.theoplayer.android.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import com.theoplayer.android.api.event.EventListener
import com.theoplayer.android.api.event.player.ErrorEvent
import com.theoplayer.android.api.event.player.PlayerEventTypes
import com.theoplayer.android.api.event.player.SourceChangeEvent
import com.theoplayer.android.api.player.Player

/**
 * Textual representation of the error that occurred, if any, on the player.
 */
@SuppressLint("AppCompatCustomView")
open class THEOError @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = R.style.THEOError
) : TextView(context, attributeSet, defStyleAttr, defStyleRes) {

    private var player: Player? = null

    private val sourceChangeEventListener = EventListener<SourceChangeEvent> {
        text = null
    }

    private val errorEventListener = EventListener<ErrorEvent> {
        text = it.errorObject.message
    }

    fun setPlayer(player: Player) {
        this.player = player
        this.player?.addEventListener(PlayerEventTypes.SOURCECHANGE, sourceChangeEventListener)
        this.player?.addEventListener(PlayerEventTypes.ERROR, errorEventListener)
    }

    fun release() {
        player?.removeEventListener(PlayerEventTypes.SOURCECHANGE, sourceChangeEventListener)
        player?.removeEventListener(PlayerEventTypes.ERROR, errorEventListener)
        player = null
        text = null
    }

}