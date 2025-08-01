package com.theoplayer.android.ui

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build

// From android.content.pm.ActivityInfo
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