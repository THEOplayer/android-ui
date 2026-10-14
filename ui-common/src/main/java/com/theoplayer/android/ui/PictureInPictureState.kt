package com.theoplayer.android.ui

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.theoplayer.android.api.THEOplayerView
import com.theoplayer.android.api.event.player.PlayerEventTypes
import com.theoplayer.android.api.pip.PiPType
import com.theoplayer.android.ui.util.rememberIsInPipMode
import com.theoplayer.android.ui.util.supportsPictureInPictureMode
import kotlinx.coroutines.awaitCancellation

/**
 * State holder for a picture-in-picture button, such as [PictureInPictureButton].
 *
 * Create an instance with [rememberPictureInPictureState].
 */
@ExperimentalPlayerStateApi
class PictureInPictureState internal constructor(private val theoplayerView: THEOplayerView?) {
    private var pendingPiPListener: FullscreenHandler.OnFullscreenChangeListener? = null

    /**
     * Whether the activity and player support picture-in-picture mode.
     */
    val pictureInPictureSupported: Boolean = theoplayerView?.let { view ->
        (view.context as? Activity)?.let { activity ->
            view.piPManager != null && activity.supportsPictureInPictureMode()
        }
    } ?: false

    /**
     * Whether the player is in (or transitioning to) picture-in-picture mode.
     */
    val pictureInPicture: Boolean
        get() = activityInPipMode || pipManagerInPipMode

    internal var activityInPipMode by mutableStateOf(false)
    private var pipManagerInPipMode by mutableStateOf(false)

    private fun update() {
        pipManagerInPipMode = theoplayerView?.piPManager?.isInPiP == true
    }

    internal suspend fun observe() {
        try {
            val player = theoplayerView?.player ?: awaitCancellation()
            update()
            player.listen(PlayerEventTypes.PRESENTATIONMODECHANGE) { update() }
        } finally {
            pendingPiPListener?.let {
                theoplayerView?.fullscreenHandler?.removeOnFullscreenChangeListener(it)
            }
            pendingPiPListener = null
        }
    }

    /**
     * Enters picture-in-picture mode, leaving fullscreen first if needed.
     */
    fun enterPictureInPicture(pipType: PiPType) {
        val piPManager = theoplayerView?.piPManager
        val handler = theoplayerView?.fullscreenHandler
        if (handler?.fullscreen == true) {
            pendingPiPListener?.let { handler.removeOnFullscreenChangeListener(it) }
            val listener = FullscreenHandler.OnFullscreenChangeListener { fullscreen ->
                if (!fullscreen) {
                    handler.removeOnFullscreenChangeListener(pendingPiPListener ?: return@OnFullscreenChangeListener)
                    pendingPiPListener = null
                    piPManager?.enterPiP(pipType)
                }
            }
            pendingPiPListener = listener
            handler.addOnFullscreenChangeListener(listener)
            handler.exitFullscreen()
        } else {
            piPManager?.enterPiP(pipType)
        }
    }

    /**
     * Leaves picture-in-picture mode.
     */
    fun exitPictureInPicture() {
        theoplayerView?.piPManager?.exitPiP()
    }
}

/**
 * Creates a [PictureInPictureState] that's remembered across compositions.
 */
@ExperimentalPlayerStateApi
@Composable
fun rememberPictureInPictureState(theoplayerView: THEOplayerView?): PictureInPictureState {
    val state = remember(theoplayerView) { PictureInPictureState(theoplayerView) }
    LaunchedEffect(state) { state.observe() }
    state.activityInPipMode = rememberIsInPipMode()
    return state
}
