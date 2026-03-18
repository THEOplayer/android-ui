package com.theoplayer.android.ui.util

import com.theoplayer.android.api.THEOplayerGlobal

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
) : Comparable<Version> {
    override fun toString() = buildString {
        append(major)
        append(VERSION_DELIMITER)
        append(minor)
        append(VERSION_DELIMITER)
        append(patchAndPrerelease)
    }

    override fun compareTo(other: Version): Int {
        return compareBy<Version> { it.major }
            .thenBy { it.minor }
            .thenBy { it.patchAndPrerelease }
            .compare(this, other)
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

internal object THEOplayerGlobalExt {
    /**
     * Returns the version of THEOplayer, as a [Version].
     */
    val version: Version by lazy { Version.parse(THEOplayerGlobal.getVersion()) }
}
