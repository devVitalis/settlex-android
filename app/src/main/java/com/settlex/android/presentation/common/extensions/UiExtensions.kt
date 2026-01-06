package com.settlex.android.presentation.common.extensions

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.firebase.Timestamp
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale


// View
fun View.show() {
    visibility = View.VISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

// TextView
/**
 * Sets the text of a TextView to a string of four asterisks ("****").
 */
fun TextView.setAsterisks() {
    text = "****"
}

// String
/**
 * Returns a new string by prepending an "@" symbol.
 * eg., "PaymentId" becomes "@PaymentId".
 */
fun String.addAtPrefix(): String {
    return "@$this"
}

fun String.maskEmail(): String {
    this.also { email ->
        val emailParts = email.split("@")
        val localPart = emailParts[0]
        val domainPart = emailParts[1]

        val maskedLocalPart = "${localPart[0]}****${localPart.substring(localPart.length - 1)}"
        return "$maskedLocalPart@$domainPart"
    }
}

fun String.maskPhoneNumber(): String {
    this.also { phoneNumber ->
        val visiblePrefixLength = 7
        val visibleSuffixLength = 3

        // Extract visible parts
        val prefix = phoneNumber.substring(0, visiblePrefixLength)
        val suffix = phoneNumber.substring(phoneNumber.length - visibleSuffixLength)

        // Build masked section with asterisks
//        val maskedPartLength = phoneNumber.length - (visiblePrefixLength + visibleSuffixLength)
//        val mask = StringBuilder()
//        for (index in 0..<maskedPartLength) {
//            mask.append("*")
//        }

        return "$prefix****$suffix"
    }
}

fun String.toNigerianPhoneNumber(): String {
    val formatedPhone = this.also { phoneNumber ->
        if (phoneNumber.startsWith("0")) phoneNumber.substring(1) else phoneNumber
    }
    return "+234$formatedPhone"
}

fun String.capitalizeEachWord(): String {
    this.also { text ->
        val words = text.lowercase().trim().split("\\s+".toRegex())
        val result = StringBuilder()

        for (word in words) {
            if (word.isNotEmpty()) {
                result.append(word[0].uppercaseChar())
                    .append(word.substring(1))
                    .append(" ")
            }
        }

        return result.toString().trim()
    }
}

// Timestamp
fun Timestamp.toDateTimeString(): String {
    return SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US)
        .format(this.toDate())

}

fun Timestamp.toDateString(): String {
    val dateString = SimpleDateFormat("dd, MMMM yyyy", Locale.US)
        .format(this.toDate())

    val day = SimpleDateFormat("dd", Locale.US)
        .format(this.toDate()).toInt()

    Log.d("UiExtensions", "day: $day")
    Log.d("UiExtensions", "day suffix: ${day % 10}")
    Log.d("UiExtensions", "day suffix: ${day.toString().padStart(2, '0')}")

    val suffix = when {
        day in 11..13 -> "th"
        day % 10 == 1 -> "st"
        day % 10 == 2 -> "nd"
        day % 10 == 3 -> "rd"
        else -> "th"
    }

    return dateString.replace(day.toString().padStart(2, '0'), "$day$suffix")
}

fun Timestamp.getTimeAgo(): String {
    this.also { timestamp ->
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

fun Long.toNairaString(): String {
    return this.let { amountInKobo ->
        val kobo: BigDecimal = BigDecimal.valueOf(amountInKobo)
        val naira = kobo.divide(BigDecimal.valueOf(100), 2, RoundingMode.UNNECESSARY)

        val formatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-NG"))
        formatter.minimumFractionDigits = 2
        formatter.maximumFractionDigits = 2

        formatter.format(naira)
    }
}

fun TextView.copyToClipboard(clipLabel: String) {
    this.text.also { text ->
        val clipboardManager = ContextCompat.getSystemService(context, ClipboardManager::class.java)
        val clipData = ClipData.newPlainText(clipLabel, text)
        clipboardManager?.setPrimaryClip(clipData)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
        }
    }
}

fun View.toastNotImplemented() {
    Toast.makeText(context, "Feature not yet implemented", Toast.LENGTH_SHORT).show()
}
