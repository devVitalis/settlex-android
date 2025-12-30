package com.settlex.android.presentation.common.extensions

import android.view.View
import android.widget.TextView
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

// Timestamp
fun Timestamp.toDateTimeString(): String {
    return SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US)
        .format(this.toDate())

}
//
//fun Timestamp.toDateString(): String {
//    return SimpleDateFormat("dddd, MMMM yyyy", Locale.US)
//        .format(this.toDate())
//}

fun Timestamp.toDateString(): String {
    val dateString = SimpleDateFormat("dd, MMMM yyyy", Locale.US)
        .format(this.toDate())

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
