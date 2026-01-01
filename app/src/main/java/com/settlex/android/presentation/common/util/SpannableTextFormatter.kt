package com.settlex.android.presentation.common.util

import android.content.Context
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.settlex.android.R

object SpannableTextFormatter {

    /**
     * Returns a SpannableString where all occurrences of [target] in [text] are colored [color].
     * @param text The full text to format.
     * @param target The substring to color.
     * @param color Hex color string, default color is "#0044CC".
     */
    operator fun invoke(
        context: Context,
        text: String,
        target: String,
        @ColorInt color: Int = ContextCompat.getColor(context, R.color.text_accent),
        setBold: Boolean = false,
        setUnderline: Boolean = false
    ): SpannableString {
        val spannable = SpannableString(text)

        val startIndex = text.indexOf(target)
        val endIndex = text.indexOf(target) + target.length

        spannable.setSpan(
            ForegroundColorSpan(color),
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