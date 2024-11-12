package com.theoplayer.android.ui

import android.app.PictureInPictureParams
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.util.Log
import android.util.Printer
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.PictureInPictureModeChangedInfo
import androidx.core.util.Consumer

internal class PipHandler(
    private val view: View,
    private val context: Context,
) {

    private var previousViewParent: ViewGroup? = null
    private var previousViewIndex: Int = 0
    private var previousViewLayoutParams: ViewGroup.LayoutParams? = null

    val activity: ComponentActivity
        get() = context.findActivity()


    @Composable
    fun PipFUllScreenHandler() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LaunchedEffect(activity) {
                val observer = Consumer<PictureInPictureModeChangedInfo> { info ->
                    if (info.isInPictureInPictureMode) {
                        enterPipFullScreen(context)
                    } else {
                        exitPipFullScreen(context)
                    }
                }
                activity.addOnPictureInPictureModeChangedListener(observer)
            }
        } else {
            exitPipFullScreen(context)
        }
    }

    private fun Context.findActivity(): ComponentActivity {
        var context = this
        while (context is ContextWrapper) {
            if (context is ComponentActivity) return context
            context = context.baseContext
        }
        throw IllegalStateException("Picture in picture should be called in the context of an Activity")
    }

    private fun enterPipFullScreen(context: Context) {
        val activity = context.findActivity()
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
    }

    private fun exitPipFullScreen(context: Context) {
        val activity = context.findActivity()
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
    }


}


@Composable
internal fun Modifier.installPip(handler: PipHandler, player: Player): Modifier {
    val context = LocalContext.current
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        handler.PipFUllScreenHandler()
        this.then(
            Modifier
                .onGloballyPositioned {
                    val builder = PictureInPictureParams.Builder()
                    builder.setAutoEnterEnabled(player.paused.not())
                    handler.activity.setPictureInPictureParams(builder.build())
                }
        )
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        handler.PipFUllScreenHandler()
        DisposableEffect(context) {
            val onUserLeaveBehavior: () -> Unit = {
                if (player.paused.not())
                    handler.activity
                        .enterPictureInPictureMode(PictureInPictureParams.Builder().build())
            }
            handler.activity.addOnUserLeaveHintListener(
                onUserLeaveBehavior
            )
            onDispose {
                handler.activity.removeOnUserLeaveHintListener(
                    onUserLeaveBehavior
                )
            }
        }
        this
    } else
        this
}