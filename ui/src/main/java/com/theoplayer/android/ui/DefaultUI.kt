package com.theoplayer.android.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
import com.theoplayer.android.api.Integration
import com.theoplayer.android.api.IntegrationType
import com.theoplayer.android.api.THEOplayerView
import com.theoplayer.android.api.event.EventListener
import com.theoplayer.android.api.event.player.*
import com.theoplayer.android.api.player.Player

open class DefaultUI @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : RelativeLayout(context, attributeSet, defStyleAttr, defStyleRes) {

    private val playerStateListeners = ArrayList<PlayerStateListener>()
    private lateinit var theoplayerView: THEOplayerView
    private lateinit var player: Player
    lateinit var uiController: UIController
        private set

    private val sourceChangeEventListener = EventListener<SourceChangeEvent> {
        playerStateListeners.forEach {
            it.onSourceChange()
        }
    }
    private val playEventListener = EventListener<PlayEvent> {
        playerStateListeners.forEach {
            it.onPlay()
        }
    }
    private val playingEventListener = EventListener<PlayingEvent> {
        playerStateListeners.forEach { it.onPlaying() }
    }
    private val pauseEventListener = EventListener<PauseEvent> {
        playerStateListeners.forEach { it.onPause() }
    }
    private val readyStateChangeEventListener = EventListener<ReadyStateChangeEvent> { event ->
        playerStateListeners.forEach { it.onReadyStateChanged(event.readyState) }
    }
    private val endedEventListener = EventListener<EndedEvent> {
        playerStateListeners.forEach { it.onEnded() }
    }
    private val errorEventListener = EventListener<ErrorEvent> {
        playerStateListeners.forEach { it.onError() }
    }

    override fun onViewAdded(child: View?) {
        super.onViewAdded(child)

        if (child is THEOplayerView) {
            setTHEOplayerView(child)
        }
    }

    fun setTHEOplayerView(theoplayerView: THEOplayerView) {
        this.theoplayerView = theoplayerView
        this.player = theoplayerView.player
        val uiContainer = theoplayerView.findViewById<FrameLayout>(com.theoplayer.android.R.id.theo_ui_container)
        val defaultViewLayout = LayoutInflater.from(context).inflate(R.layout.theo_defaultui, null) as ViewGroup

        uiController = defaultViewLayout.findViewById(R.id.ui_controller)
        uiContainer.addView(uiController)
        uiController.initialize(theoplayerView)
        playerStateListeners.add(uiController)
        addListeners()
    }

    private fun addListeners() {
        player.addEventListener(PlayerEventTypes.SOURCECHANGE, sourceChangeEventListener)
        player.addEventListener(PlayerEventTypes.PLAY, playEventListener)
        player.addEventListener(PlayerEventTypes.PLAYING, playingEventListener)
        player.addEventListener(PlayerEventTypes.PAUSE, pauseEventListener)
        player.addEventListener(PlayerEventTypes.READYSTATECHANGE, readyStateChangeEventListener)
        player.addEventListener(PlayerEventTypes.ENDED, endedEventListener)
        player.addEventListener(PlayerEventTypes.ERROR, errorEventListener)
    }

    private fun removeListeners() {
        player.removeEventListener(PlayerEventTypes.SOURCECHANGE, sourceChangeEventListener)
        player.removeEventListener(PlayerEventTypes.PLAY, playEventListener)
        player.removeEventListener(PlayerEventTypes.PLAYING, playingEventListener)
        player.removeEventListener(PlayerEventTypes.PAUSE, pauseEventListener)
        player.removeEventListener(PlayerEventTypes.READYSTATECHANGE, readyStateChangeEventListener)
        player.removeEventListener(PlayerEventTypes.ENDED, endedEventListener)
        player.removeEventListener(PlayerEventTypes.ERROR, errorEventListener)
    }

    fun addIntegrations(integrations: List<Integration>) {
        integrations.forEach {
            when (it.type) {
                IntegrationType.CAST -> {
                    val castController = THEOCastUIController(theoplayerView, it)
                    playerStateListeners.add(castController)
                }
                else -> {
                    // nothing to be done
                }
            }
        }
    }

    fun release() {
        removeListeners()
        playerStateListeners.forEach { it.release() }
        playerStateListeners.clear()
    }

}