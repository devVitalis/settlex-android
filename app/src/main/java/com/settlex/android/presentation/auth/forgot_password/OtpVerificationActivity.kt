package com.settlex.android.presentation.auth.forgot_password

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
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
import com.settlex.android.databinding.ActivityOtpVerificationBinding
import com.settlex.android.presentation.auth.AuthViewModel
import com.settlex.android.presentation.auth.util.PasswordFlow
import com.settlex.android.presentation.auth.util.PasswordFlowParser
import com.settlex.android.presentation.common.extensions.gone
import com.settlex.android.presentation.common.extensions.maskEmail
import com.settlex.android.presentation.common.extensions.setTextColorRes
import com.settlex.android.presentation.common.extensions.show
import com.settlex.android.presentation.common.state.UiState
import com.settlex.android.presentation.common.util.FocusManager
import com.settlex.android.presentation.common.util.SpannableTextFormatter
import com.settlex.android.util.ui.ProgressDialogManager
import com.settlex.android.util.ui.StatusBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OtpVerificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOtpVerificationBinding
    private val authViewModel: AuthViewModel by viewModels()
    private val progressLoader by lazy { ProgressDialogManager(this) }
    private val focusManager by lazy { FocusManager(this) }

    private lateinit var passwordFlow: PasswordFlow
    private lateinit var userEmail: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        passwordFlow = PasswordFlowParser.fromIntent(intent)
        userEmail = intent.getStringExtra("email")!!

        initViews()
        initObservers()
    }

    private fun initObservers() {
        observePasswordResetVerification()
        observeOtpRequestEvent()
    }

    private fun initViews() {
        StatusBar.setColor(this, R.color.colorSurface)
        setupListeners()
        setupInputWatcher()
        startOtpResendCooldownTimer()

        userEmail.maskEmail().also { maskedEmail ->
            binding.tvInfo.text = SpannableTextFormatter(
                this,
                "We sent a verification code to your email $maskedEmail}",
                maskedEmail,
                R.color.colorPrimary
            )
        }

        binding.tvWarning.text = SpannableTextFormatter(
            this,
            "This code expires in 10mins. Never share this code with anyone.",
            "Never share this code with anyone.",
            R.color.colorOnWarningContainer
        )
    }

    private fun setupListeners() = with(binding) {
        toolbar.setNavigationOnClickListener { finish() }
        tvResendOtp.setOnClickListener { resendVerificationCode() }
        btnConfirm.setOnClickListener { onConfirmButtonClicked() }
    }

    // Observers
    private fun observePasswordResetVerification() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.verifyPasswordResetEvent.collect {
                    when (it) {
                        is UiState.Loading -> progressLoader.show()
                        is UiState.Success -> proceedToCreatePassword()
                        is UiState.Failure -> showPasswordResetError(it.exception)
                    }
                }
            }
        }
    }

    private fun proceedToCreatePassword() {
        startActivity(
            Intent(this, CreatePasswordActivity::class.java)
                .putExtra("email", userEmail)
                .putExtra("password_flow", intent.getStringExtra("password_flow"))
        )
        progressLoader.hide()
        finish()
    }

    private fun showPasswordResetError(error: AppException) {
        with(binding) {
            tvOtpFeedback.text = error.message
            tvOtpFeedback.show()

            progressLoader.hide()
        }
    }

    private fun observeOtpRequestEvent() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.otpEvent.collect { state ->
                    when (state) {
                        is UiState.Loading -> progressLoader.show()
                        is UiState.Success -> onPasswordResetCodeSent()
                        is UiState.Failure -> showPasswordResetCodeError(state.exception)
                    }
                }
            }
        }
    }

    private fun onPasswordResetCodeSent() {
        startOtpResendCooldownTimer()
        progressLoader.hide()
    }

    private fun showPasswordResetCodeError(error: AppException) {
        with(binding) {
            tvOtpFeedback.text = error.message
            tvOtpFeedback.show()

            progressLoader.hide()
        }
    }

    private fun onConfirmButtonClicked() {
        when (passwordFlow) {
            is PasswordFlow.ForgotPassword -> verifyPasswordReset()

            else -> Unit
        }
    }

    private fun verifyPasswordReset() {
        authViewModel.verifyPasswordReset(userEmail, getEnteredOtpCode())
    }

    private fun resendVerificationCode() {
        authViewModel.sendVerificationCode(userEmail, OtpType.PASSWORD_RESET)
    }

    private fun setupInputWatcher() = with(binding) {
        otpView.doOnTextChanged { _, _, _, _ ->
            if (!isOtpComplete()) tvOtpFeedback.gone()
            updateConfirmButtonEnabled()
        }
    }

    private fun updateConfirmButtonEnabled() {
        binding.btnConfirm.isEnabled = isOtpComplete()
    }

    private fun isOtpComplete(): Boolean {
        return binding.otpView.length() == binding.otpView.itemCount
    }

    private fun getEnteredOtpCode(): String {
        return binding.otpView.getText().toString()
    }

    private fun startOtpResendCooldownTimer() = with(binding) {
        tvResendOtp.apply {
            isEnabled = false
            setTextColorRes(R.color.colorOnSurfaceVariant)
            tag = tvResendOtp.text
        }

        object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = (millisUntilFinished / 1000).toInt()
                if (seconds > 0) "Resend in $seconds sec(s)".also { tvResendOtp.text = it }
            }

            override fun onFinish() {
                tvResendOtp.apply {
                    isEnabled = true
                    setTextColorRes(R.color.colorPrimary)
                    text = tvResendOtp.tag as CharSequence
                }
            }
        }.start()
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (focusManager.handleOutsideTouch(event)) return true
        return super.dispatchTouchEvent(event)
    }
}
