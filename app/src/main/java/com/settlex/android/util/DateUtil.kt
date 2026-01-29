package com.settlex.android.util

import java.util.Calendar
import java.util.Date

object DateUtil {

    fun getStartOfMonth(): Date {
        val calendar = Calendar.getInstance()

        // Reset time to midnight
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        // Set to first day of the month
        calendar.set(Calendar.DAY_OF_MONTH, 1)

        return calendar.time
    }
}