package com.theoplayer.android.ui.util

import com.theoplayer.android.api.THEOplayerGlobal

private const val VERSION_DELIMITER = "."

/**
 * Performs a player version check and executes an appropriate action:
 * if the major version is equal or above to the [desiredMajorVersion]
 * then [actionIfEqualOrAbove] is triggered, otherwise [actionIfBelow].
 */
internal inline fun <reified T> runForPlayerWith(
    desiredMajorVersion: Int,
    actionIfEqualOrAbove: () -> T,
    actionIfBelow: () -> T,
): T {
    val version: String? = THEOplayerGlobal.getVersion()
    val versionSplits = version?.split(VERSION_DELIMITER)
    val majorVersionNumber = versionSplits?.getOrNull(0)?.toIntOrNull()

    return if (majorVersionNumber == null || majorVersionNumber < desiredMajorVersion) {
        actionIfBelow()
    } else {
        actionIfEqualOrAbove()
    }
}
