package com.theoplayer.android.ui

import com.theoplayer.android.api.ads.Ads
import com.theoplayer.android.api.event.Event
import com.theoplayer.android.api.event.EventListener
import com.theoplayer.android.api.event.EventType
import com.theoplayer.android.api.event.ads.AdEvent
import com.theoplayer.android.api.event.player.PlayerEvent
import com.theoplayer.android.api.player.Player

/**
 * Tracks the event listeners registered by a state holder, so they can all be removed again
 * on dispose.
 */
internal class PlayerStateListeners {
    private val listeners = mutableListOf<Triple<Any?, EventType<*>, EventListener<*>>>()

    fun <E : PlayerEvent<*>> Player.add(type: EventType<E>, listener: EventListener<in E>) {
        addEventListener(type, listener)
        listeners.add(Triple(this, type, listener))
    }

    fun <E : AdEvent<*>> Ads.add(type: EventType<E>, listener: EventListener<in E>) {
        addEventListener(type, listener)
        listeners.add(Triple(this, type, listener))
    }

    fun removeAll() {
        for ((target, type, listener) in listeners) {
            remove(target, type, listener)
        }
        listeners.clear()
    }

    @Suppress("UNCHECKED_CAST")
    private fun remove(target: Any?, type: EventType<*>, listener: EventListener<*>) {
        when (target) {
            is Player -> target.removeEventListener(
                type as EventType<PlayerEvent<*>>,
                listener as EventListener<PlayerEvent<*>>
            )
            is Ads -> target.removeEventListener(
                type as EventType<AdEvent<*>>,
                listener as EventListener<AdEvent<*>>
            )
        }
    }
}
