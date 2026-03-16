package com.theoplayer.android.ui.util

import androidx.annotation.IntRange

/**
 * Creates a text track label for CEA-608 and CEA-708 formats.
 *
 * @return an optional string composed of a [channelNumber] and a prepended
 * "CC" suffix, or `null` if the channel number is invalid.
 */
internal fun getLabelForChannelNumber(
    @IntRange(from = 0L, to = 63L) channelNumber: Int?,
): String? {
    // CEA-608 only supports channel numbers in [1, 4],
    // while CEA-708 support service numbers in [1, 63].
    if (channelNumber !in 1..63) {
        return null
    }
    return "CC${channelNumber}"
}
