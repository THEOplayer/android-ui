package com.theoplayer.android.ui

import com.theoplayer.android.api.event.Event
import com.theoplayer.android.api.event.EventDispatcher
import com.theoplayer.android.api.event.EventListener
import com.theoplayer.android.api.event.EventType

/**
 * Tracks the event listeners registered by a state holder, so they can all be removed again
 * on dispose.
 */
internal class PlayerStateListeners {
    private val removers = mutableListOf<() -> Unit>()

    fun <B : Event<*>, T : B> EventDispatcher<B>.add(type: EventType<T>, listener: EventListener<in T>) {
        addEventListener(type, listener)
        removers.add { removeEventListener(type, listener) }
    }

    fun removeAll() {
        removers.forEach { it() }
        removers.clear()
    }
}
