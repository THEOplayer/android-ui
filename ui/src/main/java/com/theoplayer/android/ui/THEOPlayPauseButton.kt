package com.theoplayer.android.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import com.theoplayer.android.api.event.EventListener
import com.theoplayer.android.api.event.player.*
import com.theoplayer.android.api.player.Player

/**
 * Button to play or pause the content.
 *
 * - Icon shown is updated based upon the player state.
 * - If the content has ended, it will show a replay-icon. Clicking this will restart the content. (VOD-only)
 */
@SuppressLint("AppCompatCustomView")
open class THEOPlayPauseButton @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = R.style.THEOPlayPauseButton
) : THEOImageButton(context, attributeSet, defStyleAttr, defStyleRes) {

    private var player: Player? = null

    private val sourceChangeEventListener = EventListener<SourceChangeEvent> { setImageResource(R.drawable.ic_play) }
    private val playEventListener = EventListener<PlayEvent> { setImageResource(R.drawable.ic_pause) }
    private val playingEventListener = EventListener<PlayingEvent> { setImageResource(R.drawable.ic_pause) }
    private val pauseEventListener = EventListener<PauseEvent> { setImageResource(R.drawable.ic_play) }
    private val endedEventListener = EventListener<EndedEvent> { setImageResource(R.drawable.ic_replay) }
    private val seekedEventListener = EventListener<SeekedEvent> {
        if (player?.isPaused == true) {
            setImageResource(R.drawable.ic_play)
        } else {
            setImageResource(R.drawable.ic_pause)
        }
    }

    fun setPlayer(player: Player) {
        this.player = player
        this.player?.addEventListener(PlayerEventTypes.SOURCECHANGE, sourceChangeEventListener)
        this.player?.addEventListener(PlayerEventTypes.PLAY, playEventListener)
        this.player?.addEventListener(PlayerEventTypes.PLAYING, playingEventListener)
        this.player?.addEventListener(PlayerEventTypes.PAUSE, pauseEventListener)
        this.player?.addEventListener(PlayerEventTypes.ENDED, endedEventListener)
        this.player?.addEventListener(PlayerEventTypes.SEEKED, seekedEventListener)
    }

    override fun performClick(): Boolean {
        player?.let {
            if (it.isPaused) {
                it.play()
            } else if (it.isEnded) {
                it.currentTime = 0.0
                it.play()
            } else {
                it.pause()
            }
        }

        return super.performClick()
    }

    fun release() {
        player?.removeEventListener(PlayerEventTypes.SOURCECHANGE, sourceChangeEventListener)
        player?.removeEventListener(PlayerEventTypes.PLAY, playEventListener)
        player?.removeEventListener(PlayerEventTypes.PLAYING, playingEventListener)
        player?.removeEventListener(PlayerEventTypes.PAUSE, pauseEventListener)
        player?.removeEventListener(PlayerEventTypes.ENDED, endedEventListener)
        player?.removeEventListener(PlayerEventTypes.SEEKED, seekedEventListener)
        player = null
    }

}