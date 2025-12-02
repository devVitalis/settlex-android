package com.settlex.android.presentation.auth.register

import android.os.Bundle
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import com.settlex.android.R
import com.settlex.android.presentation.common.util.KeyboardHelper
import dagger.hilt.android.AndroidEntryPoint

/**
 * Hosts the multi-step user registration flow using fragments.
 */
@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {

    private val keyboardHelper: KeyboardHelper by lazy { KeyboardHelper(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (keyboardHelper.handleOutsideTouch(event)) return true
        return super.dispatchTouchEvent(event)
    }
}