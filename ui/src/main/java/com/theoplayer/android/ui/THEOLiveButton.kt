package com.theoplayer.android.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.Button
import androidx.core.content.ContextCompat
import com.theoplayer.android.api.event.EventListener
import com.theoplayer.android.api.event.player.PlayerEventTypes.TIMEUPDATE
import com.theoplayer.android.api.event.player.TimeUpdateEvent
import com.theoplayer.android.api.player.Player

/**
 * Live button to indicate whether player is close to live-point.
 */
@SuppressLint("AppCompatCustomView")
open class THEOLiveButton @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = R.style.THEOLiveButton
) : Button(context, attributeSet, defStyleAttr, defStyleRes) {

    private var player: Player? = null
    private var isLivePoint = false

    private val timeUpdateEventListener = EventListener<TimeUpdateEvent> {
        if (player!!.seekable.length() == 0) {
            return@EventListener
        }

        val playerDelay = player!!.seekable.getEnd(0) - player!!.currentTime
        val offset = player!!.source?.sources?.get(0)?.liveOffset ?: (player!!.abr.targetBuffer * 3.0)
        val isLiveNow = playerDelay < offset

        if (isLivePoint == isLiveNow) {
            return@EventListener
        }
        isLivePoint = isLiveNow

        if (isLivePoint) {
            setTextColor(ContextCompat.getColor(context, R.color.theoRed))
            setTextViewDrawableTintColor(R.color.theoRed)
        } else {
            setTextColor(ContextCompat.getColor(context, R.color.theoWhite))
            setTextViewDrawableTintColor(R.color.theoWhite)
        }
    }

    fun setPlayer(player: Player) {
        this.player = player
        this.player?.addEventListener(TIMEUPDATE, timeUpdateEventListener)
    }

    private fun setTextViewDrawableTintColor(colorId: Int) {
        for (drawable: Drawable? in compoundDrawables) {
            drawable?.setTint(ContextCompat.getColor(context, colorId))
        }
    }

    override fun performClick(): Boolean {
        if (player?.source != null) {
            // seek to live
            player?.currentTime = Double.POSITIVE_INFINITY
        }

        return super.performClick()
    }

    fun release() {
        player?.removeEventListener(TIMEUPDATE, timeUpdateEventListener)
        player = null
    }

}