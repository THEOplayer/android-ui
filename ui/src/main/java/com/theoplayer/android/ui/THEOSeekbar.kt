package com.theoplayer.android.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.SeekBar
import com.theoplayer.android.api.event.EventListener
import com.theoplayer.android.api.event.player.DurationChangeEvent
import com.theoplayer.android.api.event.player.PlayerEventTypes
import com.theoplayer.android.api.event.player.SourceChangeEvent
import com.theoplayer.android.api.event.player.TimeUpdateEvent
import com.theoplayer.android.api.player.Player

/**
 * Seekbar where you can interact with to skim through the content.
 */
@SuppressLint("AppCompatCustomView")
open class THEOSeekbar @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = R.style.THEOSeekbar
) : SeekBar(context, attributeSet, defStyleAttr, defStyleRes), SeekBar.OnSeekBarChangeListener {

    private var player: Player? = null
    private var seekBarChangeListeners: ArrayList<OnSeekBarChangeListener> = ArrayList()
    private var isDVR: Boolean = false

    private val sourceChangeEventListener = EventListener<SourceChangeEvent> {
        progress = 0
        max = 0
    }

    private val durationChangeEventListener = EventListener<DurationChangeEvent> {
        isDVR = (player?.duration?.isInfinite() == true) && (player?.seekable?.length()?.equals(0) == false)
        max = player?.duration?.toInt() ?: 0
    }

    private val timeUpdateEventListener = EventListener<TimeUpdateEvent> {
        val playerTime = player?.currentTime ?: 0.0
        if (isDVR) {
            val seekableEnd = player?.seekable?.getEnd(0) ?: 0.0
            val seekableStart = player?.seekable?.getStart(0) ?: 0.0
            progress = (playerTime - seekableStart).toInt()
            max = (seekableEnd - seekableStart).toInt()
        } else {
            progress = playerTime.toInt()
        }
    }

    fun setPlayer(player: Player) {
        super.setOnSeekBarChangeListener(this)

        this.player = player
        this.player?.addEventListener(PlayerEventTypes.SOURCECHANGE, sourceChangeEventListener)
        this.player?.addEventListener(PlayerEventTypes.DURATIONCHANGE, durationChangeEventListener)
        this.player?.addEventListener(PlayerEventTypes.TIMEUPDATE, timeUpdateEventListener)
    }

    @Deprecated(
        "Multiple listeners may be needed for displaying the correct UI. Use addOnSeekBarChangeListener(seekBarChangeListener) instead.",
        ReplaceWith("addOnSeekBarChangeListener(seekBarChangeListener)"),
        DeprecationLevel.WARNING
    )
    override fun setOnSeekBarChangeListener(seekBarChangeListener: OnSeekBarChangeListener?) {
        super.setOnSeekBarChangeListener(seekBarChangeListener)
    }

    fun addOnSeekBarChangeListener(seekBarChangeListener: OnSeekBarChangeListener) {
        seekBarChangeListeners.add(seekBarChangeListener)
    }

    fun removeOnSeekBarChangeListener(seekBarChangeListener: OnSeekBarChangeListener) {
        seekBarChangeListeners.remove(seekBarChangeListener)
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        if (fromUser) {
            player?.let { player ->
                if (isDVR) {
                    player.currentTime = player.seekable.getStart(0) + progress.toDouble()
                } else {
                    player.currentTime = progress.toDouble()
                }
            }
        }

        for (listener in seekBarChangeListeners) {
            listener.onProgressChanged(seekBar, progress, fromUser)
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {
        for (listener in seekBarChangeListeners) {
            listener.onStartTrackingTouch(seekBar)
        }
    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {
        for (listener in seekBarChangeListeners) {
            listener.onStopTrackingTouch(seekBar)
        }
    }

    fun release() {
        super.setOnSeekBarChangeListener(null)
        seekBarChangeListeners.clear()
        player?.removeEventListener(PlayerEventTypes.SOURCECHANGE, sourceChangeEventListener)
        player?.removeEventListener(PlayerEventTypes.DURATIONCHANGE, durationChangeEventListener)
        player?.removeEventListener(PlayerEventTypes.TIMEUPDATE, timeUpdateEventListener)
        player = null
        progress = 0
        max = 0
    }

}