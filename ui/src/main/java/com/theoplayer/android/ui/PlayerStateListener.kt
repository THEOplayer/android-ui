package com.theoplayer.android.ui

import com.theoplayer.android.api.player.ReadyState

interface PlayerStateListener {
    fun onSourceChange()
    fun onPlay()
    fun onPlaying()
    fun onPause()
    fun onReadyStateChanged(readyState: ReadyState)
    fun onEnded()
    fun onError()
    fun release()
}