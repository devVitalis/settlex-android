package com.settlex.android.presentation.common.util

import android.view.View
import android.widget.EditText


/**
 * A utility class that changes the background of a [View] based on the focus state of one or more [EditText]s.
 *
 * This class sets an `OnFocusChangeListener` on each provided [EditText]. When an `EditText`
 * gains focus, the background of its corresponding [View] is changed to `focusedBackgroundResource`.
 * When it loses focus, the background is reverted to `defaultBackgroundResource`.
 *
 * This is useful for creating custom input fields where a container's visual state (e.g., border color)
 * needs to reflect the focus state of the text input area within it.
 *
 * @param defaultBackgroundResource The drawable resource ID for the background when an EditText does not have focus.
 * @param focusedBackgroundResource The drawable resource ID for the background when an EditText has focus.
 * @param mappings A vararg of [Pair]s, where each pair maps an [EditText] to the [View] whose background should be changed.
 *                 The first element of the pair is the `EditText` to monitor, and the second is the `View` to update.
 */
class EditTextFocusBackgroundChanger(
    private val defaultBackgroundResource: Int,
    private val focusedBackgroundResource: Int,
    private vararg val mappings: Pair<EditText, View>
) {

    init {
        initFocusHandler()
    }

    private fun initFocusHandler() {
        mappings.forEach { (editText, backgroundView) ->
            editText.onFocusChangeListener =
                View.OnFocusChangeListener { _, hasFocus ->
                    when (hasFocus) {
                        true -> backgroundView.setBackgroundResource(focusedBackgroundResource)
                        false -> backgroundView.setBackgroundResource(defaultBackgroundResource)
                    }
                }
        }
    }
}