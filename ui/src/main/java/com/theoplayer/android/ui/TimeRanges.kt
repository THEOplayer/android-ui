package com.theoplayer.android.ui

/**
 * Represents one or more ranges of time, each specified by a start time and an end time.
 *
 * @property ranges the time ranges. Must be non-overlapping, and sorted by their start time.
 */
data class TimeRanges(private val ranges: List<ClosedFloatingPointRange<Double>>) :
    List<ClosedFloatingPointRange<Double>> by ranges {

    /**
     * Returns the start of the first range, or `null` if [empty][isEmpty].
     */
    val firstStart: Double?
        get() = firstOrNull()?.start

    /**
     * Returns the end of the last range, or `null` if [empty][isEmpty].
     */
    val lastEnd: Double?
        get() = lastOrNull()?.endInclusive

    /**
     * Returns the bounding time range from [firstStart] to [lastEnd], or `null` if [empty][isEmpty].
     */
    val bounds: ClosedFloatingPointRange<Double>?
        get() = if (isEmpty()) {
            null
        } else {
            first().start..last().endInclusive
        }

    companion object {
        /**
         * Converts a [com.theoplayer.android.api.timerange.TimeRanges] to a [TimeRanges].
         */
        @JvmStatic
        fun fromTHEOplayer(ranges: com.theoplayer.android.api.timerange.TimeRanges): TimeRanges {
            return TimeRanges(ranges.map { range -> range.start..range.end })
        }
    }
}
