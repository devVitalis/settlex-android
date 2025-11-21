package com.settlex.android.ui.auth.forgot_password

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.settlex.android.R
import com.settlex.android.data.enums.OtpType
import com.settlex.android.databinding.ActivityForgotPasswordBinding
import com.settlex.android.ui.auth.AuthViewModel
import com.settlex.android.ui.common.event.UiState
import com.settlex.android.util.ui.ProgressLoaderController
import com.settlex.android.util.ui.StatusBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ForgotPasswordActivity : AppCompatActivity() {
    private val progressLoader: ProgressLoaderController by lazy { ProgressLoaderController(this) }
    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var binding: ActivityForgotPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressLoader.setOverlayColor(
            ContextCompat.getColor(
                this,
                R.color.semi_transparent_white
            )
        )

        setupUiActions()
        observeOtpRequestState()
    }

    private fun setupUiActions() {
        StatusBar.setColor(this, R.color.white)
        initEmailInputListener()
        initEditTextFocusHandlers()
        initActionDoneOnEmailEditText()

        binding.btnBackBefore.setOnClickListener { finish() }
        binding.btnContinue.setOnClickListener { sendVerificationCode() }
    }

    private fun observeOtpRequestState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.otpEvent.collect {
                    when (it) {
                        is UiState.Loading -> progressLoader.show()
                        is UiState.Success -> onOtpSendSuccess()
                        is UiState.Failure -> onOtpSendFailure(it.exception.message)
                    }
                }
            }
        }
    }

    private fun onOtpSendSuccess() {
        startActivity(
            Intent(this, OtpVerificationActivity::class.java)
                .putExtra(
                    "email",
                    binding.etEmail.text.toString().lowercase()
                )
                .putExtra("password_flow", "forgot")
        )
        progressLoader.hide()
    }

    private fun onOtpSendFailure(error: String?) {
        binding.tvError.text = error
        binding.tvError.visibility = View.VISIBLE

        progressLoader.hide()
    }

    private fun sendVerificationCode() {
        val email = binding.etEmail.text.toString().lowercase()
        authViewModel.sendVerificationCode(email, OtpType.PASSWORD_RESET)
    }

    private fun initEmailInputListener() {
        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun afterTextChanged(s: Editable) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                binding.etEmail.visibility = View.GONE
                validateContinueButtonState()
            }
        })
    }

    private fun validateContinueButtonState() {
        binding.btnContinue.isEnabled = isEmailValid()
    }

    private fun isEmailValid(): Boolean {
        val email = binding.etEmail.text.toString().lowercase()
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun initEditTextFocusHandlers() {
        val focusBgRes = R.drawable.bg_edit_txt_custom_gray_focused
        val defaultBgRes = R.drawable.bg_edit_txt_custom_gray_not_focused

        // background changes
        binding.etEmail.setOnFocusChangeListener { _, hasFocus: Boolean ->
            binding.etEmailBg.setBackgroundResource(if (hasFocus) focusBgRes else defaultBgRes)
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
                    hideKeyboard(v)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }


    private fun initActionDoneOnEmailEditText() {
        binding.etEmail.setOnEditorActionListener { v: TextView?, actionId: Int, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard(v!!)
                v.clearFocus()
                return@setOnEditorActionListener true
            }
            false
        }
    }

    private fun hideKeyboard(view: View) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}