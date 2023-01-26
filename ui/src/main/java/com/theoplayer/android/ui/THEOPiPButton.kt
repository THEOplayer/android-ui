package com.theoplayer.android.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import com.theoplayer.android.api.THEOplayerView
import com.theoplayer.android.api.event.EventListener
import com.theoplayer.android.api.event.player.PlayerEventTypes
import com.theoplayer.android.api.event.player.PresentationModeChange
import com.theoplayer.android.api.pip.PiPManager
import com.theoplayer.android.api.pip.PiPType
import com.theoplayer.android.api.player.Player
import com.theoplayer.android.api.player.PresentationMode.PICTURE_IN_PICTURE

/**
 * Button to enter Picture-in-picture (PiP) mode.
 *
 * - If you are already in PiP, this will exit back to inline.
 */
@SuppressLint("AppCompatCustomView")
open class THEOPiPButton @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = R.style.THEOPiPButton
) : THEOImageButton(context, attributeSet, defStyleAttr, defStyleRes) {

    private var player: Player? = null
    private var pipManager: PiPManager? = null

    private val presentationModeChangeEventListener = EventListener<PresentationModeChange> { event ->
        if (event.presentationMode == PICTURE_IN_PICTURE) {
            setImageResource(R.drawable.ic_pip_exit)
        } else {
            setImageResource(R.drawable.ic_pip_enter)
        }
    }

    fun setTHEOplayerView(tpv: THEOplayerView) {
        this.player = tpv.player
        this.pipManager = tpv.piPManager
        this.player?.addEventListener(PlayerEventTypes.PRESENTATIONMODECHANGE, presentationModeChangeEventListener)
    }

    override fun performClick(): Boolean {
        pipManager?.let {
            if (!it.isInPiP) {
                it.enterPiP(PiPType.ACTIVITY)
            } else {
                it.exitPiP()
            }
        }
        return super.performClick()
    }

    fun release() {
        player?.removeEventListener(PlayerEventTypes.PRESENTATIONMODECHANGE, presentationModeChangeEventListener)
        player = null
        pipManager = null
    }

}