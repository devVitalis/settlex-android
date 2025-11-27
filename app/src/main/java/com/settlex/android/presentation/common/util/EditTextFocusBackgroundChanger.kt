package com.settlex.android.presentation.common.util

import android.view.View
import android.widget.EditText

/**
 * A utility class that changes the background of a [View] based on the focus state of an [EditText].
 *
 * This is useful for creating custom input fields where the visual state (e.g., border color)
 * changes when the user focuses on the text input area. The class sets an `OnFocusChangeListener`
 * on the provided [EditText] and updates the background resource of a target [View] accordingly.
 *
 * @param defaultBackgroundResource The drawable resource ID for the background when the EditText does not have focus.
 * @param focusedBackgroundResource The drawable resource ID for the background when the EditText has focus.
 * @param editText The [EditText] to monitor for focus changes.
 * @param backgroundView The [View] whose background will be changed based on the EditText's focus state.
 *                   This can be the EditText itself or a parent container.
 */
class EditTextFocusBackgroundChanger(
    private val defaultBackgroundResource: Int,
    private val focusedBackgroundResource: Int,
    private val editText: EditText,
    private val backgroundView: View,
) {

    init {
        initFocusHandler()
    }

    private fun initFocusHandler() {
        editText.onFocusChangeListener =
            View.OnFocusChangeListener { _, hasFocus ->
                when (hasFocus) {
                    true -> backgroundView.setBackgroundResource(focusedBackgroundResource)
                    false -> backgroundView.setBackgroundResource(defaultBackgroundResource)
                }
            }
    }
}