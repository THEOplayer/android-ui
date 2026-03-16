package com.theoplayer.android.ui.util

import androidx.annotation.CheckResult
import androidx.annotation.IntRange

private val CEA_FORMATTING_REGEX = "^CC(\\d+)$".toRegex()

/**
 * Checks whether a provided label is CEA-608 or CEA-708 formed.
 */
@CheckResult
internal fun isLabelCeaFormatted(label: String?): Boolean {
    if (label == null) {
        return false
    }

    val matchResult = CEA_FORMATTING_REGEX.find(label)
    val groupValues = matchResult?.groupValues
    if (matchResult == null ||
        groupValues == null ||
        // There is one group we want to match with the channel number.
        groupValues.size != 2) {
        return false
    }

    val rawChannelNumber = groupValues[1]
    val channelNumber = rawChannelNumber.toIntOrNull()
    return !rawChannelNumber.startsWith("0") && channelNumber in 1..63
}

/**
 * Creates a text track label for CEA-608 and CEA-708 formats.
 *
 * @return an optional string composed of a [channelNumber] and a prepended
 * "CC" suffix, or `null` if the channel number is invalid.
 */
@CheckResult
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
