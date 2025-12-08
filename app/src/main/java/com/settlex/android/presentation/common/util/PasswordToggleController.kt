package com.settlex.android.presentation.common.util

import android.text.InputType
import android.widget.EditText
import android.widget.ImageView
import com.settlex.android.R

/**
 * A controller class to manage the visibility toggle functionality for a password [EditText].
 *
 * This class links an [ImageView] (acting as a toggle button) with an [EditText]
 * to allow the user to show or hide the password they are entering.
 *
 * It handles:
 * - Setting an `OnClickListener` on the toggle button.
 * - Changing the icon of the toggle button to reflect the current visibility state
 *   (e.g., a "visible" eye vs. a "slashed" eye).
 * - Toggling the `inputType` of the [EditText] between a visible password and a hidden password.
 * - Preserving the cursor position and typeface of the [EditText] after the `inputType` changes.
 *
 * @param editText The [EditText] field for the password input.
 * @param toggleButton The [ImageView] that the user clicks to toggle password visibility.
 */
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
