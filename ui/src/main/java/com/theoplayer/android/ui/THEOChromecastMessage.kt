package com.theoplayer.android.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import com.theoplayer.android.api.THEOplayerView
import com.theoplayer.android.api.cast.chromecast.Chromecast
import com.theoplayer.android.api.cast.chromecast.PlayerCastState
import com.theoplayer.android.api.event.EventListener
import com.theoplayer.android.api.event.chromecast.CastStateChangeEvent
import com.theoplayer.android.api.event.chromecast.ChromecastEventTypes

/**
 * Textual placeholder to show that the player is currently casting to a device.
 */
@SuppressLint("AppCompatCustomView")
open class THEOChromecastMessage @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = R.style.THEOChromecastMessage
) : TextView(context, attributeSet, defStyleAttr, defStyleRes) {

    private var chromecast: Chromecast? = null

    private val castStateChangeEventListener = EventListener<CastStateChangeEvent> {
        if (it.state == PlayerCastState.CONNECTED) {
            var message = resources.getString(R.string.theo_casting)
            chromecast?.receiverName?.let { receiverName ->
                message += " on\n$receiverName"
            }
            text = message
        }
    }

    fun setTHEOplayerView(tpv: THEOplayerView) {
        this.chromecast = tpv.cast?.chromecast
        this.chromecast?.addEventListener(ChromecastEventTypes.STATECHANGE, castStateChangeEventListener)
    }

    fun release() {
        chromecast?.removeEventListener(ChromecastEventTypes.STATECHANGE, castStateChangeEventListener)
        chromecast = null
    }
}