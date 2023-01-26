package com.theoplayer.android.ui

import kotlin.math.absoluteValue

class Helper {

    companion object {

        /**
         * Function to convert from double precision number to textual time representation.
         *
         * - E.g. 656.0 to "00:10:56"
         * - This ignores the precision of the double, because it doesn't add value for the user.
         */
        fun secondsToHourFormat(duration: Double): String {
            if (duration.isInfinite()) {
                return "INF"
            }

            val absoluteDuration = duration.absoluteValue
            val hours = (absoluteDuration / 3600).toInt()
            val hoursRemainder = (absoluteDuration % 3600).toInt()
            val minutes = (hoursRemainder / 60)
            val seconds = (hoursRemainder % 60)

            var result = ""

            if (duration < 0) {
                result += "-"
            }

            if (hours != 0) {
                result += "$hours:"
            }

            result += if (minutes < 10) "0$minutes" else "$minutes"
            result += ":"
            result += if (seconds < 10) "0$seconds" else "$seconds"
            return result
        }

        fun remainingSeconds(duration: Double, currentTime: Double): String {
            return (duration - currentTime).toInt().toString()
        }
    }
}