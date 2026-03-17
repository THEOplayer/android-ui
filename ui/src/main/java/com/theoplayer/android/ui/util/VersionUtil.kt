package com.theoplayer.android.ui.util

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
    val patch: String,
) {
    override fun toString() = buildString {
        append(major)
        append(VERSION_DELIMITER)
        append(minor)
        append(VERSION_DELIMITER)
        append(patch)
    }

    companion object {
        val ZERO = Version(major = 0, minor = 0, patch = "0")

        fun parse(version: String): Version? {
            val versionParts = version.split(VERSION_DELIMITER, limit = 3)
            if (versionParts.size != 3) return null
            val (major, minor, patch) = versionParts
            return Version(
                major = major.toIntOrNull() ?: return null,
                minor = minor.toIntOrNull() ?: return null,
                patch = patch
            )
        }
    }
}
