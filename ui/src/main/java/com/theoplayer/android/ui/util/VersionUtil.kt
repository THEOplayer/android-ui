package com.theoplayer.android.ui.util

import com.theoplayer.android.api.THEOplayerGlobal
import com.theoplayer.android.ui.memoizeLast

private const val VERSION_DELIMITER = '.'

/**
 * A [semver](https://semver.org/) version.
 */
internal data class Version(
    /**
     * The major version.
     */
    val major: Int,
    /**
     * The minor version.
     */
    val minor: Int,
    /**
     * The patch (and prerelease) version.
     */
    val patchAndPrerelease: String,
) {
    override fun toString() = buildString {
        append(major)
        append(VERSION_DELIMITER)
        append(minor)
        append(VERSION_DELIMITER)
        append(patchAndPrerelease)
    }

    companion object {
        fun parse(version: String): Version {
            try {
                val versionParts = version.split(VERSION_DELIMITER, limit = 3)
                require(versionParts.size == 3)
                val (major, minor, patchAndPrerelease) = versionParts
                return Version(
                    major = major.toInt(),
                    minor = minor.toInt(),
                    patchAndPrerelease = patchAndPrerelease
                )
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException("Invalid version", e)
            }
        }
    }
}

private val getCachedTheoplayerVersion = memoizeLast(Version::parse)

/**
 * Returns the major version of THEOplayer.
 */
internal val theoplayerVersion: Version
    get() = getCachedTheoplayerVersion(THEOplayerGlobal.getVersion())
