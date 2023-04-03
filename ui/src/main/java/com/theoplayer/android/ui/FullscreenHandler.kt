package com.theoplayer.android.ui

import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

internal interface FullscreenHandler {
    val fullscreen: Boolean
    var onFullscreenChangeListener: OnFullscreenChangeListener?
    fun requestFullscreen()
    fun exitFullscreen()

    fun interface OnFullscreenChangeListener {
        fun onFullscreenChange(isFullscreen: Boolean)
    }
}

internal class FullscreenHandlerImpl(private val view: View) : FullscreenHandler {
    override var fullscreen: Boolean = false
        private set
    override var onFullscreenChangeListener: FullscreenHandler.OnFullscreenChangeListener? = null

    private var previousSystemBarsBehavior: Int =
        WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_SWIPE
    private var previousRequestedOrientation: Int =
        ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    private var previousViewParent: ViewGroup? = null
    private var previousViewIndex: Int = 0
    private var previousViewLayoutParams: ViewGroup.LayoutParams? = null

    override fun requestFullscreen() {
        val activity = view.context as? Activity ?: return
        val window = activity.window

        // Hide system bars
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, view).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            previousSystemBarsBehavior = controller.systemBarsBehavior
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        // Enter landscape mode
        previousRequestedOrientation = activity.requestedOrientation
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        // Move view to root of activity
        val rootView = activity.findViewById<ViewGroup>(android.R.id.content)
        (view.parent as? ViewGroup?)?.let { parent ->
            previousViewParent = parent
            previousViewIndex = parent.indexOfChild(view)
            previousViewLayoutParams = view.layoutParams
            parent.removeView(view)
            rootView.post {
                rootView.addView(
                    view,
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                )
            }
        }

        fullscreen = true
        onFullscreenChangeListener?.onFullscreenChange(fullscreen)
    }

    override fun exitFullscreen() {
        val activity = view.context as? Activity ?: return
        val window = activity.window

        // Restore system bars
        WindowCompat.setDecorFitsSystemWindows(window, true)
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
            parent.post {
                parent.addView(view, previousViewIndex, previousViewLayoutParams)
                view.layout(view.left, view.top, view.right, view.bottom)
            }
        }
        previousViewParent = null
        previousViewIndex = 0

        fullscreen = false
        onFullscreenChangeListener?.onFullscreenChange(fullscreen)
    }
}