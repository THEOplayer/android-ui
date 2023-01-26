package com.theoplayer.android.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import com.theoplayer.android.api.THEOplayerView
import com.theoplayer.android.api.event.EventListener
import com.theoplayer.android.api.event.player.PlayerEventTypes
import com.theoplayer.android.api.event.player.PresentationModeChange
import com.theoplayer.android.api.fullscreen.FullScreenManager
import com.theoplayer.android.api.player.Player
import com.theoplayer.android.api.player.PresentationMode

/**
 * Button to enter fullscreen mode.
 *
 * - If you are already in fullscreen, then clicking the button will exit fullscreen.
 */
@SuppressLint("AppCompatCustomView")
open class THEOFullscreenButton @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = R.style.THEOFullscreenButton
) : THEOImageButton(context, attributeSet, defStyleAttr, defStyleRes) {

    private var player: Player? = null
    private var fullscreenManager: FullScreenManager? = null

    private val presentationModeChangeEventListener = EventListener<PresentationModeChange> { event ->
        if (event.presentationMode == PresentationMode.FULLSCREEN) {
            setImageResource(R.drawable.ic_fullscreen_exit)
        } else {
            setImageResource(R.drawable.ic_fullscreen_enter)
        }
    }

    fun setTHEOplayerView(tpv: THEOplayerView) {
        this.player = tpv.player
        this.fullscreenManager = tpv.fullScreenManager
        this.player?.addEventListener(PlayerEventTypes.PRESENTATIONMODECHANGE, presentationModeChangeEventListener)
    }

    override fun performClick(): Boolean {
        fullscreenManager?.let {
            if (!it.isFullScreen) {
                it.requestFullScreen()
            } else {
                it.exitFullScreen()
            }
        }
        return super.performClick()
    }

    fun release() {
        player?.removeEventListener(PlayerEventTypes.PRESENTATIONMODECHANGE, presentationModeChangeEventListener)
        player = null
        fullscreenManager = null
    }

}