package com.theoplayer.android.ui.util

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.app.OnPictureInPictureModeChangedProvider
import androidx.core.app.OnPictureInPictureUiStateChangedProvider
import androidx.core.app.PictureInPictureModeChangedInfo
import androidx.core.app.PictureInPictureUiStateCompat
import androidx.core.util.Consumer

/**
 * From [android.content.pm.ActivityInfo]
 */
private const val FLAG_SUPPORTS_PICTURE_IN_PICTURE = 0x400000

/**
 * Check if the given activity supports [Activity.enterPictureInPictureMode].
 */
internal fun Activity.supportsPictureInPictureMode(): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return false
    if (!packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)) return false
    val info = packageManager.getActivityInfo(componentName, 0)
    return (info.flags and FLAG_SUPPORTS_PICTURE_IN_PICTURE) != 0
}

/**
 * Returns whether the activity is in (or transitioning to) picture-in-picture mode.
 */
@Composable
internal fun rememberIsInPipMode(): Boolean {
    // https://developer.android.com/develop/ui/compose/system/picture-in-picture#handle-ui
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return false
    val activity = LocalActivity.current
    var pipMode by remember { mutableStateOf(activity?.isInPictureInPictureMode ?: false) }
    if (activity is OnPictureInPictureModeChangedProvider) {
        DisposableEffect(activity) {
            val observer = Consumer<PictureInPictureModeChangedInfo> { info ->
                pipMode = info.isInPictureInPictureMode
            }
            activity.addOnPictureInPictureModeChangedListener(observer)
            onDispose { activity.removeOnPictureInPictureModeChangedListener(observer) }
        }
    }
    var transitioningToPip by remember { mutableStateOf(false) }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM && activity is OnPictureInPictureUiStateChangedProvider) {
        DisposableEffect(activity) {
            val observer = Consumer<PictureInPictureUiStateCompat> { info ->
                transitioningToPip = info.isTransitioningToPip
            }
            activity.addOnPictureInPictureUiStateChangedListener(observer)
            onDispose { activity.addOnPictureInPictureUiStateChangedListener(observer) }
        }
    }
    return pipMode || transitioningToPip
}