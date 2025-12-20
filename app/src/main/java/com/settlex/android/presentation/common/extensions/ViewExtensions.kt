package com.settlex.android.presentation.common.extensions

import android.view.View
import android.widget.TextView


// Visibility
fun View.show() {
    visibility = View.VISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

/**
 * Sets the text of a TextView to a string of four asterisks ("****").
 */
fun TextView.setAsterisks() {
    text = "****"
}

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