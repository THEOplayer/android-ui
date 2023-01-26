package com.theoplayer.android.ui

import android.view.ViewGroup
import com.theoplayer.android.api.Integration
import com.theoplayer.android.api.THEOplayerView
import com.theoplayer.android.api.cast.CastIntegration
import com.theoplayer.android.api.cast.chromecast.PlayerCastState
import com.theoplayer.android.api.event.EventListener
import com.theoplayer.android.api.event.chromecast.CastStateChangeEvent
import com.theoplayer.android.api.event.chromecast.ChromecastEventTypes
import com.theoplayer.android.api.player.ReadyState

open class THEOCastUIController : PlayerStateListener {

    private val theoplayerView: THEOplayerView
    private val castIntegration: CastIntegration
    private var theoChromecastMessage: THEOChromecastMessage? = null
    private var theoChromecastButton: THEOChromecastButton? = null

    private val castStateChangeListener = EventListener<CastStateChangeEvent> {
        if (it.state == PlayerCastState.CONNECTED) {
            theoChromecastMessage?.visibility = ViewGroup.VISIBLE
        } else {
            theoChromecastMessage?.visibility = ViewGroup.GONE
        }
    }

    constructor(theoplayerView: THEOplayerView, integration: Integration) {
        this.theoplayerView = theoplayerView
        this.castIntegration = integration as CastIntegration

        if (this.theoplayerView.cast != null) {
            setupCast()
        }
    }

    private fun setupCast() {
        val buttonsContainer = theoplayerView.findViewById<ViewGroup>(R.id.theo_right_buttons_container)
        theoChromecastButton = THEOChromecastButton(theoplayerView.context)
        theoChromecastButton?.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        theoChromecastButton?.visibility = ViewGroup.GONE
        theoChromecastButton?.setTHEOplayerView(theoplayerView)
        // modify the index to adjust the position of the cast button inside theo_right_buttons_container
        buttonsContainer.addView(theoChromecastButton, 1)

        val castContainer = theoplayerView.findViewById<ViewGroup>(com.theoplayer.android.R.id.theo_cast_container)
        theoChromecastMessage = THEOChromecastMessage(theoplayerView.context)
        theoChromecastMessage?.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        theoChromecastMessage?.visibility = ViewGroup.GONE
        theoChromecastMessage?.setTHEOplayerView(theoplayerView)
        castContainer.addView(theoChromecastMessage)

        theoplayerView.cast?.chromecast?.addEventListener(ChromecastEventTypes.STATECHANGE, castStateChangeListener)
    }

    override fun onSourceChange() {
        theoChromecastButton?.visibility = ViewGroup.GONE
    }

    override fun onPlay() {
        theoChromecastButton?.visibility = ViewGroup.VISIBLE
    }

    override fun onPlaying() {
        theoChromecastButton?.visibility = ViewGroup.VISIBLE
    }

    override fun onPause() {
        // do nothing
    }

    override fun onReadyStateChanged(readyState: ReadyState) {
        // do nothing
    }

    override fun onEnded() {
        // do nothing
    }

    override fun onError() {
        theoChromecastButton?.visibility = ViewGroup.GONE
        theoChromecastMessage?.visibility = ViewGroup.GONE
    }

    override fun release() {
        theoplayerView.cast?.chromecast?.removeEventListener(ChromecastEventTypes.STATECHANGE, castStateChangeListener)
        theoChromecastButton?.release()
        theoChromecastMessage?.release()
    }

}