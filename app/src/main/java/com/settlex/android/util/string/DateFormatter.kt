package com.settlex.android.util.string

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

class DateFormatter private constructor() {

    companion object {

        /**
         * Formats a Timestamp into human-readable date, e.g. "12 Oct 2025, 09:45 AM"
         */
        fun formatTimeStampToDateAndTime(timestamp: Timestamp): String {
            return SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US)
                .format(timestamp.toDate())
        }

        fun formatTimeStampToDate(timestamp: Timestamp): String {
            return SimpleDateFormat("dd, MMMM yyyy", Locale.US)
                .format(timestamp.toDate())
        }

        fun formatTimestampToRelative(timestamp: Timestamp?): String {
            if (timestamp == null) return ""

            val second: Long = 1000
            val minute = 60 * second
            val hour = 60 * minute
            val day = 24 * hour
            val week = 7 * day
            val month = 30 * day
            val year = 365 * day

            val time = timestamp.toDate().time
            val now = System.currentTimeMillis()
            val diff = now - time


            if (diff < minute) return "Just now"

            if (diff < hour) {
                val mins = diff / minute
                return (diff / minute).toString() + " min" + (if (mins > 1) "s" else "") + " ago"
            }

            if (diff < day) {
                val hrs = diff / hour
                return hrs.toString() + " hr" + (if (hrs > 1) "s" else "") + " ago"
            }

            if (diff < week) {
                val days = diff / day
                return days.toString() + " day" + (if (days > 1) "s" else "") + " ago"
            }

            if (diff < month) {
                val weeks = diff / week
                return weeks.toString() + " week" + (if (weeks > 1) "s" else "") + " ago"
            }

            if (diff < year) {
                val months = diff / month
                return months.toString() + " month" + (if (months > 1) "s" else "") + " ago"
            }

            val years = diff / year
            return years.toString() + " yr" + (if (years > 1) "s" else "") + " ago"
        }
    }
}