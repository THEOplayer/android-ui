package com.theoplayer.android.ui

import com.theoplayer.android.api.event.Event
import com.theoplayer.android.api.event.EventDispatcher
import com.theoplayer.android.api.event.EventListener
import com.theoplayer.android.api.event.EventType
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Continuously listens to events of the given [eventTypes], passing each received event to
 * [onEvent]. One listener instance is shared between all event types.
 *
 * This function must be called on the main thread, which is where the player dispatches its events.
 * [onEvent] is invoked synchronously from within the event listener.
 *
 * If [onEvent] throws an exception, listening is terminated: the listener is removed, and the
 * exception is rethrown from this function.
 *
 * A note on [callbackFlow][kotlinx.coroutines.flow.callbackFlow] vs [suspendCancellableCoroutine]
 * (adapted from AndroidX Media3's `Player.listen`):
 *
 * Although a callback flow is recommended for a multi-shot API like an [EventDispatcher], a
 * [Flow][kotlinx.coroutines.flow.Flow] buffers its values and delivers them to its collector later
 * on. Here, [suspendCancellableCoroutine] is not used to wait for a single callback to resume the
 * continuation (its common single-shot use). Instead, it keeps the coroutine suspended while
 * [onEvent] handles incoming events directly, as they are dispatched. This keeps tighter control
 * over the timing of events, so the UI state is updated in the same frame as the event that caused
 * the change.
 *
 * The listener is removed in the `finally` block, which runs on the calling (main) thread. Until
 * then, the listener checks whether the continuation is still active, so that events dispatched
 * after cancellation are ignored.
 *
 * @return Nothing. This function never returns normally: it either keeps listening indefinitely, or
 *   terminates due to an exception or cancellation.
 */
internal suspend fun <E : Event<*>> EventDispatcher<in E>.listen(
    vararg eventTypes: EventType<out E>,
    onEvent: (E) -> Unit,
): Nothing {
    var continuation: CancellableContinuation<Nothing>? = null
    val listener =
        EventListener<E> { event ->
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
