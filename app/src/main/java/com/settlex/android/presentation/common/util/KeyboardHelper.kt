package com.settlex.android.presentation.common.util

import android.app.Activity
import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView

/**
 * Keyboard and focus helper for EditTexts.
 * Allows:
 * 1. Handling 'Done' IME action to hide keyboard and clear focus.
 * 2. Dismissing keyboard when touching outside EditText.
 */
class KeyboardHelper(private val activity: Activity) {

    /**
     * Call this on an EditText to hide keyboard and clear focus when the user
     * presses the 'Done' IME action.
     */
    fun attachDoneAction(editText: EditText) {
        editText.setOnEditorActionListener { v: TextView?, actionId: Int, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard(v!!)
                v.clearFocus()
                return@setOnEditorActionListener true
            }
            false
        }
    }

    /**
     * Call this in Activity's dispatchTouchEvent() to hide the keyboard
     * when the user taps outside an EditText.
     */
    fun handleOutsideTouch(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val v = activity.currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
                    hideKeyboard(v)
                }
            }
        }
        return false
    }

    /**
     * Hides the keyboard for the given view.
     */
    private fun hideKeyboard(view: View) {
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
