package com.settlex.android.presentation.auth.forgot_password

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.MotionEvent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.settlex.android.R
import com.settlex.android.data.enums.OtpType
import com.settlex.android.data.exception.AppException
import com.settlex.android.databinding.ActivityForgotPasswordBinding
import com.settlex.android.presentation.auth.AuthViewModel
import com.settlex.android.presentation.common.extensions.gone
import com.settlex.android.presentation.common.extensions.show
import com.settlex.android.presentation.common.state.UiState
import com.settlex.android.presentation.common.util.EditTextFocusBackgroundChanger
import com.settlex.android.presentation.common.util.KeyboardHelper
import com.settlex.android.util.ui.ProgressDialogManager
import com.settlex.android.util.ui.StatusBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ForgotPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityForgotPasswordBinding
    private val keyboardHelper by lazy { KeyboardHelper(this) }
    private val progressLoader by lazy { ProgressDialogManager(this) }
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        progressLoader.setOverlayColor(
//            ContextCompat.getColor(
//                this,
//                R.color.semi_transparent_white
//            )
//        )

        initViews()
        initObservers()
    }

    private fun initObservers() {
        observeOtpRequestState()
    }

    private fun initViews() {
        StatusBar.setColor(this, R.color.white)
        setupInputValidation()

        with(binding) {
            keyboardHelper.attachDoneAction(etEmail)

            EditTextFocusBackgroundChanger(
                defaultBackgroundResource = R.drawable.bg_edit_txt_custom_gray_not_focused,
                focusedBackgroundResource = R.drawable.bg_edit_txt_custom_white_focused,
                etEmail to etEmailBackground
            )

            btnBackBefore.setOnClickListener { finish() }
            btnContinue.setOnClickListener {
                tvError.gone()
                sendVerificationCode()
            }
        }
    }

    private fun observeOtpRequestState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.otpEvent.collect {
                    when (it) {
                        is UiState.Loading -> progressLoader.show()
                        is UiState.Success -> onOtpRequestSuccess()
                        is UiState.Failure -> onOtpRequestFailure(it.exception)
                    }
                }
            }
        }
    }

    private fun onOtpRequestSuccess() {
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

    private fun onOtpRequestFailure(error: AppException) {
        with(binding) {
            tvError.text = error.message
            tvError.show()

            progressLoader.hide()
        }
    }

    private fun sendVerificationCode() = with(binding) {
        val email = etEmail.text.toString().lowercase()
        authViewModel.sendVerificationCode(email, OtpType.PASSWORD_RESET)
    }

    private fun setupInputValidation() {
        with(binding) {
            etEmail.doOnTextChanged { _, _, _, _ ->
                tvError.gone()
                btnContinue.isEnabled = isEmailValid()
            }
        }
    }

    private fun isEmailValid(): Boolean = with(binding) {
        val email = etEmail.text.toString().lowercase()
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }


    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (keyboardHelper.handleOutsideTouch(event)) return true
        return super.dispatchTouchEvent(event)
    }
}