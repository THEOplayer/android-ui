package com.theoplayer.android.ui

import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.theoplayer.android.api.THEOplayerView
import com.theoplayer.android.ui.common.R
import java.util.concurrent.CopyOnWriteArrayList
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

internal interface FullscreenHandler {
    val fullscreen: Boolean
    fun addOnFullscreenChangeListener(listener: OnFullscreenChangeListener)
    fun removeOnFullscreenChangeListener(listener: OnFullscreenChangeListener)
    fun requestFullscreen()
    fun exitFullscreen()

    fun interface OnFullscreenChangeListener {
        fun onFullscreenChange(isFullscreen: Boolean)
    }
}

/**
 * Continuously listens to fullscreen changes, passing the new fullscreen state to [onChange].
 *
 * If [onChange] throws an exception, listening is terminated: the listener is removed,
 * and the exception is rethrown from this function.
 *
 * See [EventDispatcher.listen][com.theoplayer.android.ui.listen] for why this uses
 * [suspendCancellableCoroutine] rather than a [callbackFlow][kotlinx.coroutines.flow.callbackFlow].
 *
 * @return Nothing. This function never returns normally: it either keeps listening indefinitely,
 *   or terminates due to an exception or cancellation.
 */
internal suspend fun FullscreenHandler.listen(onChange: (Boolean) -> Unit): Nothing {
    var continuation: CancellableContinuation<Nothing>? = null
    val listener = FullscreenHandler.OnFullscreenChangeListener { isFullscreen ->
        val cont = continuation
        if (cont == null || !cont.isActive) return@OnFullscreenChangeListener
        try {
            onChange(isFullscreen)
        } catch (t: Throwable) {
            cont.resumeWithException(t)
        }
    }
    addOnFullscreenChangeListener(listener)
    try {
        suspendCancellableCoroutine<Nothing> { continuation = it }
    } finally {
        removeOnFullscreenChangeListener(listener)
    }
}

/**
 * The [FullscreenHandler] shared by everything that controls fullscreen for this view.
 *
 * A fullscreen handler moves the view within the activity, so there can only be one per
 * [THEOplayerView]. The shared instance is stored in the view's tag.
 */
internal val THEOplayerView.fullscreenHandler: FullscreenHandler?
    get() {
        (getTag(R.id.theoplayer_ui_fullscreen_handler) as? FullscreenHandler)?.let { return it }
        val container = findViewById<View>(com.theoplayer.android.R.id.theo_player_container)
            ?: return null
        return FullscreenHandlerImpl(container).also {
            setTag(R.id.theoplayer_ui_fullscreen_handler, it)
        }
    }

internal class FullscreenHandlerImpl(private val view: View) : FullscreenHandler {
    override var fullscreen: Boolean = false
        private set
    private val listeners = CopyOnWriteArrayList<FullscreenHandler.OnFullscreenChangeListener>()

    override fun addOnFullscreenChangeListener(listener: FullscreenHandler.OnFullscreenChangeListener) {
        listeners.add(listener)
    }

    override fun removeOnFullscreenChangeListener(listener: FullscreenHandler.OnFullscreenChangeListener) {
        listeners.remove(listener)
    }

    private var previousSystemBarsBehavior: Int = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
    private var previousRequestedOrientation: Int = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    private var previousViewParent: ViewGroup? = null
    private var previousViewIndex: Int = 0
    private var previousViewLayoutParams: ViewGroup.LayoutParams? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    override fun requestFullscreen() {
        scope.launch { requestFullscreenAsync() }
    }

    suspend fun requestFullscreenAsync() {
        if (fullscreen) return
        val activity = view.context as? Activity ?: return
        val window = activity.window

        // Hide system bars
        WindowCompat.getInsetsController(window, view).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            previousSystemBarsBehavior = controller.systemBarsBehavior
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        // Enter landscape mode
        previousRequestedOrientation = activity.requestedOrientation
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

        // Move view to root of activity
        val rootView = activity.findViewById<ViewGroup>(android.R.id.content)
        (view.parent as? ViewGroup?)?.let { parent ->
            previousViewParent = parent
            previousViewIndex = parent.indexOfChild(view)
            previousViewLayoutParams = view.layoutParams
            parent.removeView(view)
            rootView.postAsync()
            rootView.addView(
                view,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
        }

        fullscreen = true
        listeners.forEach { it.onFullscreenChange(fullscreen) }
    }

    override fun exitFullscreen() {
        scope.launch { exitFullscreenAsync() }
    }

    private suspend fun exitFullscreenAsync() {
        if (!fullscreen) return
        val activity = view.context as? Activity ?: return
        val window = activity.window

        // Restore system bars
        WindowCompat.getInsetsController(window, view).let { controller ->
            controller.show(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = previousSystemBarsBehavior
        }

        // Restore orientation
        activity.requestedOrientation = previousRequestedOrientation

        // Move view back to previous parent
        val rootView = activity.findViewById<ViewGroup>(android.R.id.content)
        previousViewParent?.let { parent ->
            rootView.removeView(view)
            parent.postAsync()
            parent.addView(view, previousViewIndex, previousViewLayoutParams)
            view.layout(view.left, view.top, view.right, view.bottom)
        }
        previousViewParent = null
        previousViewIndex = 0

        fullscreen = false
        listeners.forEach { it.onFullscreenChange(fullscreen) }
    }
}

private suspend fun View.postAsync() =
    suspendCoroutine { continuation -> post { continuation.resume(Unit) } }