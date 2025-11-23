package com.settlex.android.ui.auth.forgot_password

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.settlex.android.R
import com.settlex.android.data.enums.OtpType
import com.settlex.android.databinding.ActivityOtpVerificationBinding
import com.settlex.android.ui.auth.AuthViewModel
import com.settlex.android.ui.auth.utils.PasswordFlow
import com.settlex.android.ui.auth.utils.PasswordFlowParser
import com.settlex.android.ui.common.state.UiState
import com.settlex.android.util.string.StringFormatter
import com.settlex.android.util.ui.ProgressLoaderController
import com.settlex.android.util.ui.StatusBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OtpVerificationActivity : AppCompatActivity() {
    private val authViewModel: AuthViewModel by viewModels()
    private val userEmail: String by lazy { intent.getStringExtra("email")!! }
    private val progressLoader: ProgressLoaderController by lazy { ProgressLoaderController(this) }
    private lateinit var binding: ActivityOtpVerificationBinding
    private lateinit var passwordFlow: PasswordFlow


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpVerificationBinding.inflate(layoutInflater)
        setContentView(binding.getRoot())

        passwordFlow = PasswordFlowParser.fromIntent(intent)

        initUi()
        initObservers()
    }

    private fun initObservers() {
        observeVerifyPasswordResetEvent()
        observeOtpRequestEvent()
    }

    private fun initUi() {
        StatusBar.setColor(this, R.color.white)
        updateUiFromIntent()
        initClickListeners()
        initOtpInputWatcher()
        startOtpResendCooldownTimer()
        maskAndDisplayUserEmail()
    }

    private fun initClickListeners() {
        binding.btnBackBefore.setOnClickListener { finish() }
        binding.btnResendOtp.setOnClickListener { resendVerificationCode() }
        binding.btnConfirm.setOnClickListener { verifyPasswordReset() }
        binding.btnHelp.setOnClickListener {
            StringFormatter.showNotImplementedToast(
                this
            )
        }
    }

    private fun updateUiFromIntent() {
        binding.btnHelp.visibility =
            when (passwordFlow) {
                is PasswordFlow.Forgot -> View.VISIBLE
                is PasswordFlow.Change -> View.GONE
                is PasswordFlow.AuthenticatedReset -> View.GONE
            }
    }

    private fun maskAndDisplayUserEmail() {
        val maskedEmail = StringFormatter.maskEmail(userEmail)
        binding.tvUserEmail.text = maskedEmail
    }

    // Observers
    private fun observeVerifyPasswordResetEvent() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.verifyPasswordResetEvent.collect {
                    when (it) {
                        is UiState.Loading -> progressLoader.show()
                        is UiState.Success -> proceedToCreatePassword()
                        is UiState.Failure -> showPasswordResetError(it.exception.message)
                    }
                }
            }
        }
    }

    private fun proceedToCreatePassword() {
        val intent = Intent(this, CreatePasswordActivity::class.java)
        intent.putExtra("email", userEmail)
        intent.putExtra("password_flow", this.intent)
        startActivity(intent)
        finish()

        progressLoader.hide()
    }

    private fun showPasswordResetError(message: String?) {
        binding.tvOtpFeedback.text = message
        binding.tvOtpFeedback.visibility = View.VISIBLE

        progressLoader.hide()
    }

    private fun observeOtpRequestEvent() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.otpEvent.collect {
                    when (it) {
                        is UiState.Loading -> progressLoader.show()
                        is UiState.Success -> onPasswordResetCodeSent()
                        is UiState.Failure -> showPasswordResetCodeError(it.exception.message)
                    }
                }
            }
        }
    }

    private fun onPasswordResetCodeSent() {
        startOtpResendCooldownTimer()
        progressLoader.hide()
    }

    private fun showPasswordResetCodeError(error: String?) {
        binding.tvOtpFeedback.text = error
        binding.tvOtpFeedback.visibility = View.VISIBLE

        progressLoader.hide()
    }

    private fun verifyPasswordReset() {
        authViewModel.verifyPasswordReset(userEmail, getEnteredOtpCode())
    }

    private fun resendVerificationCode() {
        authViewModel.sendVerificationCode(userEmail, OtpType.PASSWORD_RESET)
    }

    private fun initOtpInputWatcher() {
        binding.otpView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
            }

            override fun onTextChanged(otp: CharSequence, start: Int, before: Int, count: Int) {
                if (!isOtpComplete()) binding.tvOtpFeedback.visibility = View.GONE
                updateConfirmButtonEnabled()
            }
        })
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

    private fun startOtpResendCooldownTimer() {
        binding.btnResendOtp.isEnabled = false
        binding.btnResendOtp.tag = binding.btnResendOtp.text

        object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = (millisUntilFinished / 1000).toInt()
                val countDown = "Resend in + $seconds"

                if (seconds > 0) binding.btnResendOtp.text = countDown
            }

            override fun onFinish() {
                binding.btnResendOtp.text = binding.btnResendOtp.tag as CharSequence
                binding.btnResendOtp.isEnabled = true
            }
        }.start()
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

    private fun hideKeyboard(view: View) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
