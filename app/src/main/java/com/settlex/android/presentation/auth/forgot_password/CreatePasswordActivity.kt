package com.settlex.android.presentation.auth.forgot_password

import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.settlex.android.R
import com.settlex.android.data.exception.AppException
import com.settlex.android.databinding.ActivityCreatePasswordBinding
import com.settlex.android.presentation.auth.AuthViewModel
import com.settlex.android.presentation.auth.login.LoginActivity
import com.settlex.android.presentation.auth.util.PasswordFlow
import com.settlex.android.presentation.auth.util.PasswordFlowParser
import com.settlex.android.presentation.common.extensions.gone
import com.settlex.android.presentation.common.extensions.show
import com.settlex.android.presentation.common.state.UiState
import com.settlex.android.presentation.common.util.DialogHelper
import com.settlex.android.presentation.common.util.FocusManager
import com.settlex.android.presentation.common.util.ValidationUtil
import com.settlex.android.util.ui.ProgressDialogManager
import com.settlex.android.util.ui.StatusBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CreatePasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreatePasswordBinding
    private val focusManager: FocusManager by lazy { FocusManager(this) }
    private val progressLoader: ProgressDialogManager by lazy { ProgressDialogManager(this) }
    private val viewModel: AuthViewModel by viewModels()

    private lateinit var passwordFlow: PasswordFlow
    private lateinit var userEmail: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreatePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        passwordFlow = PasswordFlowParser.fromIntent(intent)
        userEmail = intent.getStringExtra("email")!!

        initViews()
        initObservers()
    }

    private fun initObservers() {
        observeSetNewPasswordEvents()
    }

    private fun initViews() {
        StatusBar.setColor(this, R.color.colorBackground)
        updateUiFromIntent()
        setupListeners()
        setupInputValidation()
        hidePasswordToggles()
    }

    private fun setupListeners() = with(binding) {
        btnChangePassword.setOnClickListener { onChangePasswordClicked() }
        btnBackBefore.setOnClickListener { finish() }
    }

    private fun updateUiFromIntent() {
        binding.viewCurrentPassword.visibility =
            when (passwordFlow) {
                is PasswordFlow.Change -> View.VISIBLE
                else -> View.GONE
            }
    }

    private fun onChangePasswordClicked() = with(binding) {
        when (passwordFlow) {
            is PasswordFlow.Change -> {
                // TODO: call authenticated password reset API
            }

            else -> setNewPassword()
        }
    }

    private fun setNewPassword() = with(binding) {
        val oldPassword = etCurrentPassword.text.toString().trim()
        val newPassword = etPassword.text.toString().trim()

        viewModel.setNewPassword(userEmail, oldPassword, newPassword)
    }

    // Observers
    private fun observeSetNewPasswordEvents() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.setNewPasswordEvent.collect {
                    when (it) {
                        is UiState.Loading -> progressLoader.show()
                        is UiState.Success -> showSuccessDialog()
                        is UiState.Failure -> showPasswordChangeError(it.exception)
                    }
                }
            }
        }
    }

    private fun showSuccessDialog() {
        DialogHelper.showSuccessBottomSheetDialog(this) { dialog, binding ->
            with(binding) {
                "Success".also { tvTitle.text = it }
                "Your password has been changed successfully".also { tvMessage.text = it }
                "Continue".also { btnAction.text = it }

                btnAction.setOnClickListener {
                    when (passwordFlow) {
                        is PasswordFlow.Forgot -> {
                            startActivity(
                                Intent(
                                    this@CreatePasswordActivity,
                                    LoginActivity::class.java
                                )
                            )
                            finishAffinity()
                        }

                        else -> {
                            dialog.dismiss()
                            finish()
                        }
                    }
                }
            }
        }
        progressLoader.hide()
    }

    private fun showPasswordChangeError(error: AppException) {
        with(binding) {
            tvConfirmPasswordError.text = error.message
            tvConfirmPasswordError.show()

            progressLoader.hide()
        }
    }

    private fun setupInputValidation() = with(binding) {
        etCurrentPassword.doOnTextChanged { text, _, _, _ ->
            val currentPassword = text.toString().trim()
            tilCurrentPassword.isEndIconVisible = currentPassword.isNotEmpty()
            tvCurrentPasswordError.gone()

            updateChangePasswordButtonState()
        }

        etPassword.doOnTextChanged { text, _, _, _ ->
            val password = text.toString().trim()
            tilPassword.isEndIconVisible = password.isNotEmpty()

            validatePasswordMatch()
            updateChangePasswordButtonState()
        }

        etConfirmPassword.doOnTextChanged { text, _, _, _ ->
            val confirmPassword = text.toString().trim()
            tilConfirmPassword.isEndIconVisible = confirmPassword.isNotEmpty()

            validatePasswordMatch()
            updateChangePasswordButtonState()
        }
    }

    private fun validatePasswordMatch() {
        with(binding) {

            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            when (password.isNotEmpty() && confirmPassword.isNotEmpty()) {
                true -> {
                    if (ValidationUtil.isPasswordsMatch(
                            password = password,
                            confirmationPassword = confirmPassword
                        )
                    ) tvConfirmPasswordError.gone() else {
                        tvConfirmPasswordError.text = ValidationUtil.ERROR_PASSWORD_MISMATCH
                        tvConfirmPasswordError.show()
                    }
                }

                false -> tvConfirmPasswordError.gone()
            }
        }
    }

    private fun updateChangePasswordButtonState() {
        with(binding) {
            btnChangePassword.isEnabled = when (passwordFlow) {
                is PasswordFlow.Change -> isCurrentPasswordValid() && isPasswordValidAndMatch()
                else -> isPasswordValidAndMatch()
            }
        }
    }

    private fun isCurrentPasswordValid(): Boolean {
        val currentPassword = binding.etCurrentPassword.text.toString().trim()
        return ValidationUtil.isPasswordValid(currentPassword)
    }

    private fun isPasswordValidAndMatch(): Boolean {
        with(binding) {
            val newPassword = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            return ValidationUtil.isPasswordAndConfirmationValid(newPassword, confirmPassword)
        }
    }

    private fun hidePasswordToggles() = with(binding) {
        tilCurrentPassword.isEndIconVisible = false
        tilPassword.isEndIconVisible = false
        tilConfirmPassword.isEndIconVisible = false
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (focusManager.handleOutsideTouch(event)) return true
        return super.dispatchTouchEvent(event)
    }
}