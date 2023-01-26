package com.theoplayer.android.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.theoplayer.android.api.THEOplayerView
import com.theoplayer.android.api.cast.chromecast.Chromecast
import com.theoplayer.android.api.cast.chromecast.PlayerCastState
import com.theoplayer.android.api.event.EventListener
import com.theoplayer.android.api.event.chromecast.CastStateChangeEvent
import com.theoplayer.android.api.event.chromecast.ChromecastEventTypes

/**
 * Button to select a Chromecast receiver.
 *
 * - Note that if you are connected, pressing the button will stop the session.
 */
@SuppressLint("AppCompatCustomView")
open class THEOChromecastButton @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = R.style.THEOCastButton
) : THEOImageButton(context, attributeSet, defStyleAttr, defStyleRes) {

    private var chromecast: Chromecast? = null

    private val castStateChangeEventListener = EventListener<CastStateChangeEvent> {
        when (it.state) {
            PlayerCastState.AVAILABLE -> {
                setImageResource(R.drawable.ic_cast_disconnected)
                imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.theoYellow))
                isEnabled = true
            }
            PlayerCastState.CONNECTING -> {
                setImageResource(R.drawable.ic_cast_disconnected)
            }
            PlayerCastState.CONNECTED -> {
                setImageResource(R.drawable.ic_cast_connected)
            }
            else -> {
                setImageResource(R.drawable.ic_cast_disconnected)
                imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.theoGray))
                isEnabled = false
            }
        }
    }

    fun setTHEOplayerView(tpv: THEOplayerView) {
        this.chromecast = tpv.cast?.chromecast
        this.chromecast?.addEventListener(ChromecastEventTypes.STATECHANGE, castStateChangeEventListener)
    }

    override fun performClick(): Boolean {
        chromecast?.let { chromecast ->
            if (chromecast.isCasting) {
                chromecast.stop()
            } else {
                chromecast.start()
            }
        }
        return super.performClick()
    }

    fun release() {
        chromecast?.removeEventListener(ChromecastEventTypes.STATECHANGE, castStateChangeEventListener)
        chromecast = null
    }
}