package com.settlex.android.presentation.auth.common.forgot_password

import android.text.InputType
import android.widget.EditText
import android.widget.ImageView

class PasswordToggleController(
    private val editText: EditText,
    toggleButton: ImageView
) {
    private var isVisible = false

    init {
        toggleButton.setOnClickListener { toggle() }
    }

    private fun toggle() {
        isVisible = !isVisible

        editText.inputType = if (isVisible) {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        editText.setSelection(editText.text?.length ?: 0)
    }
}
