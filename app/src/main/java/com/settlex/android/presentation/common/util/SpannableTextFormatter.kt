package com.settlex.android.presentation.common.util

import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import androidx.core.graphics.toColorInt

object SpannableTextFormatter {

    /**
     * Returns a SpannableString where all occurrences of [target] in [text] are colored [color].
     * @param text The full text to format.
     * @param target The substring to color.
     * @param color Hex color string, default color is "#0044CC".
     */
    operator fun invoke(
        text: String,
        target: String,
        color: String = "#0044CC",
        setBold: Boolean = false,
        setUnderline: Boolean = false
    ): SpannableString {
        val spannable = SpannableString(text)
        val colorInt: Int = color.toColorInt()

        val startIndex = text.indexOf(target)
        val endIndex = text.indexOf(target) + target.length

        spannable.setSpan(
            ForegroundColorSpan(colorInt),
            startIndex,
            endIndex,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        if (setBold) {
            spannable.setSpan(
                StyleSpan(Typeface.BOLD),
                startIndex,
                endIndex,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        if (setUnderline) {
            spannable.setSpan(
                UnderlineSpan(),
                startIndex,
                endIndex,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        return spannable
    }
}