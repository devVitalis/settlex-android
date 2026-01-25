package com.settlex.android.presentation.common.util

import android.content.Context
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import androidx.annotation.AttrRes
import com.settlex.android.R
import com.settlex.android.presentation.common.extensions.getThemeColor

object SpannableTextFormatter {

    /**
     * Returns a SpannableString where all occurrences of [target] in [text] are colored [colorRes].
     * @param text The full text to format.
     * @param target The substring to color.
     * @param colorRes Hex color string, default color is "#0044CC".
     */
    @JvmName("format")
    operator fun invoke(
        context: Context,
        text: String,
        target: String,
        @AttrRes colorRes: Int = R.attr.colorPrimary,
        setBold: Boolean = false,
        setUnderline: Boolean = false
    ): SpannableString {
        val spannable = SpannableString(text)

        val startIndex = text.indexOf(target)
        val endIndex = text.indexOf(target) + target.length
        val colorTheme = context.getThemeColor(colorRes)

        spannable.setSpan(
            ForegroundColorSpan(colorTheme),
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