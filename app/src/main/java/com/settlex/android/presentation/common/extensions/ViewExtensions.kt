package com.settlex.android.presentation.common.extensions

import android.view.View
import android.widget.TextView

/**
 * Sets the visibility of the View to VISIBLE.
 */
fun View.show() {
    visibility = View.VISIBLE
}

/**
 * Sets the visibility of the View to GONE.
 */
fun View.gone() {
    visibility = View.GONE
}

/**
 * Sets the visibility of the View to INVISIBLE.
 */
fun View.invisible() {
    visibility = View.INVISIBLE
}

/**
 * Sets the text of a TextView to a string of four asterisks ("****").
 */
fun TextView.setAsterisks() {
    text = "****"
}