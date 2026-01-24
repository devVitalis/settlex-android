package com.settlex.android.presentation.common.extensions

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat.getParcelableExtra
import com.google.android.material.color.MaterialColors
import com.google.firebase.Timestamp
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
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

// TextView
/**
 * Sets the text of a TextView to a string of four asterisks ("****").
 */
fun TextView.setAsterisks() {
    text = "****"
}

fun TextView.setTextColorRes(color: Int) {
    setTextColor(ContextCompat.getColor(context, color))
}

// String
/**
 * Returns a new string by prepending an "@" symbol.
 * eg., "PaymentId" becomes "@PaymentId".
 */
fun String.addAtPrefix(): String {
    return "@$this"
}

fun String.removeAtPrefix(): String {
    return when (this.startsWith("@")) {
        true -> this.substring(1)
        false -> this
    }
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
        val prefix = phoneNumber.take(visiblePrefixLength)
        val suffix = phoneNumber.substring(phoneNumber.length - visibleSuffixLength)

        Log.d("UiExtension", "Prefix: $prefix")
        Log.d("UiExtension", "Suffix: $suffix")
        return "$prefix****$suffix"
    }
}

fun String.toNigerianPhoneNumber(): String {
    return "+234" + this.let {
        if (it.startsWith("0")) it.substring(1) else it
    }
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
    val dateString = SimpleDateFormat("dd MMM, hh:mm a", Locale.US).format(this.toDate())

    val day = SimpleDateFormat("dd", Locale.US)
        .format(this.toDate()).toInt()

    val suffix = when {
        day in 11..13 -> "th"
        day % 10 == 1 -> "st"
        day % 10 == 2 -> "nd"
        day % 10 == 3 -> "rd"
        else -> "th"
    }

    return dateString.replace(day.toString().padStart(2, '0'), "$day$suffix")
}

fun Timestamp.toFullDateTimeString(): String {
    val dateString =
        SimpleDateFormat("EEEE, dd MMMM, yyyy hh:mm a", Locale.US).format(this.toDate())

    val day = SimpleDateFormat("dd", Locale.US)
        .format(this.toDate()).toInt()

    val suffix = when {
        day in 11..13 -> "th"
        day % 10 == 1 -> "st"
        day % 10 == 2 -> "nd"
        day % 10 == 3 -> "rd"
        else -> "th"
    }

    return dateString.replace(day.toString().padStart(2, '0'), "$day$suffix")
}

fun Timestamp.toDateString(): String {
    val dateString = SimpleDateFormat("dd MMMM, yyyy", Locale.US)
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

fun Long.toNairaStringShort(): String {
    this.also { amountInKobo ->
        val symbol = "₦"
        val df = DecimalFormat("#.##")

        val naira = BigDecimal.valueOf(amountInKobo)
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)

        when {
            naira < BigDecimal.valueOf(1000) -> return symbol + naira.toPlainString()
            naira < BigDecimal.valueOf(1000000) -> {
                val thousands = naira.divide(BigDecimal.valueOf(1000), 1, RoundingMode.HALF_UP)
                return symbol + df.format(thousands) + "K"
            }

            naira < BigDecimal.valueOf(1000000000) -> {
                val millions = naira.divide(BigDecimal.valueOf(1000000), 1, RoundingMode.HALF_UP)
                return symbol + df.format(millions) + "M"
            }

            else -> {
                val billions = naira.divide(BigDecimal.valueOf(1000000000), 1, RoundingMode.HALF_UP)
                return symbol + df.format(billions) + "B"
            }
        }
    }
}

fun String.fromNairaStringToKobo(): Long {
    this.also { nairaString ->
        if (nairaString.isBlank()) return 0L
        val cleanedNairaString = nairaString.replace("₦", "").replace(",", "")
        Log.d("UiExtension", "Naira String: $this")
        Log.d("UiExtension", "Cleaned Naira String: $cleanedNairaString")

        try {
            val amount = BigDecimal(cleanedNairaString)
            val amountInKobo = amount.multiply(BigDecimal("100"))

            // Round to nearest whole Kobo
            val roundedKobo = amountInKobo.setScale(0, RoundingMode.HALF_UP)

            return roundedKobo.longValueExact()
        } catch (e: Exception) {
            Log.e("UiExtension", "Error: ${e.message} ", e)
            return 0L
        }
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

inline fun <reified T> Intent.getParcelableExtraCompat(key: String): T {
    return getParcelableExtra(this, key, T::class.java)!!
}

@ColorInt
fun Context.getThemeColor(@AttrRes attrs: Int): Int {
    return MaterialColors.getColor(this, attrs, Color.MAGENTA)
}
