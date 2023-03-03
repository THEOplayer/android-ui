package com.theoplayer.android.ui

data class TimeRanges(val ranges: List<Pair<Double, Double>>) {
    val firstStart: Double?
        get() = if (ranges.isEmpty()) {
            null
        } else {
            ranges.first().first
        }
    val lastEnd: Double?
        get() = if (ranges.isEmpty()) {
            null
        } else {
            ranges.last().second
        }

    companion object {
        @JvmStatic
        fun fromTHEOplayer(ranges: com.theoplayer.android.api.timerange.TimeRanges): TimeRanges {
            return TimeRanges(ranges.map { range -> Pair(range.start, range.end) })
        }
    }
}
