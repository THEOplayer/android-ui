package com.theoplayer.android.ui

data class TimeRanges(val ranges: List<ClosedFloatingPointRange<Double>>) {
    val firstStart: Double?
        get() = ranges.firstOrNull()?.start
    val lastEnd: Double?
        get() = ranges.lastOrNull()?.endInclusive

    companion object {
        @JvmStatic
        fun fromTHEOplayer(ranges: com.theoplayer.android.api.timerange.TimeRanges): TimeRanges {
            return TimeRanges(ranges.map { range -> range.start.rangeTo(range.end) })
        }
    }
}
