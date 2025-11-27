package com.settlex.android.presentation.common.util

import android.text.InputType
import android.widget.EditText
import android.widget.ImageView
import com.settlex.android.R

class PasswordToggleController(
    private val editText: EditText,
    private var toggleButton: ImageView
) {
    private var isVisible = false

    init {
        toggleButton.setOnClickListener { toggle() }
    }

    private fun toggle() {
        toggleButton.setImageResource(
            when (isVisible) {
                true -> R.drawable.ic_visibility_on_filled
                false -> R.drawable.ic_visibility_off_filled
            }
        )

        val typeface = editText.typeface
        editText.inputType = when (isVisible) {
            true -> InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            false -> InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        editText.setSelection(editText.text?.length ?: 0)
        editText.typeface = typeface

        isVisible = !isVisible
    }
}
