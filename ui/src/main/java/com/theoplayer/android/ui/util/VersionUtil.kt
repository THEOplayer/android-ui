package com.theoplayer.android.ui.util

private const val DEFAULT_VERSION_DELIMITER = "."

/**
 * Extracts a major version number from
 * a semver-formatted string.
 */
internal fun getPlayerMajorVersion(version: String): Int? {
    val versionSplits = version.split(
        DEFAULT_VERSION_DELIMITER,
        limit = 3,
    )
    if (versionSplits.size != 3) {
        return null
    }
    return versionSplits.getOrNull(0)?.toIntOrNull()
}
