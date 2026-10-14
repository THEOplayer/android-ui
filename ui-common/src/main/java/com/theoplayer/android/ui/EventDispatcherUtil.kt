package com.theoplayer.android.ui

import com.theoplayer.android.api.event.Event
import com.theoplayer.android.api.event.EventDispatcher
import com.theoplayer.android.api.event.EventListener
import com.theoplayer.android.api.event.EventType
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Listens to events of the given [eventTypes] until the calling coroutine is cancelled,
 * passing each event to [onEvent]. One listener instance is shared between all event types.
 * If [onEvent] throws, the listener is removed and the exception is rethrown.
 * Must be called on the main thread.
 */
internal suspend fun <E : Event<*>> EventDispatcher<in E>.listen(
    vararg eventTypes: EventType<out E>,
    onEvent: (E) -> Unit
): Nothing {
    var continuation: CancellableContinuation<Nothing>? = null
    val listener = EventListener<E> { event ->
        val cont = continuation
        if (cont == null || !cont.isActive) return@EventListener
        try {
            onEvent(event)
        } catch (t: Throwable) {
            cont.resumeWithException(t)
        }
    }
    eventTypes.forEach { addEventListener(it, listener) }
    try {
        suspendCancellableCoroutine<Nothing> { continuation = it }
    } finally {
        eventTypes.forEach { removeEventListener(it, listener) }
    }
}
