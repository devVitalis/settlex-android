package com.settlex.android.presentation.common.util

import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.core.graphics.toColorInt

object SpannableTextFormatter {


    /**
     * Returns a SpannableString where all occurrences of [target] in [text] are colored [color].
     * @param text The full text to format.
     * @param target The substring to color.
     * @param color Hex color string, e.g., "0044CC" or "#0044CC".
     */
    fun format(text: String, target: String, color: String = "0044CC"): SpannableString {
        val spannable = SpannableString(text)
        val colorInt = color.toColorInt()

        spannable.setSpan(
            ForegroundColorSpan(colorInt),
            text.indexOf(target),
            text.indexOf(target) + target.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        return spannable
    }
}