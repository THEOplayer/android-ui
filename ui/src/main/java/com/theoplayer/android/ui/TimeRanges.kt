package com.theoplayer.android.ui

data class TimeRanges(val ranges: List<Pair<Double, Double>>)

fun toTimeRanges(ranges: com.theoplayer.android.api.timerange.TimeRanges): TimeRanges {
    return TimeRanges(ranges.map { range -> Pair(range.start, range.end) })
}