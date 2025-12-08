package com.settlex.android.presentation.auth.forgot_password

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
import com.settlex.android.presentation.auth.util.PasswordFlow
import com.settlex.android.presentation.auth.util.PasswordFlowParser
import com.settlex.android.presentation.common.extensions.gone
import com.settlex.android.presentation.common.extensions.show
import com.settlex.android.presentation.common.state.UiState
import com.settlex.android.presentation.common.util.DialogHelper
import com.settlex.android.presentation.common.util.EditTextFocusBackgroundChanger
import com.settlex.android.presentation.common.util.KeyboardHelper
import com.settlex.android.presentation.common.util.PasswordToggleController
import com.settlex.android.presentation.common.util.PasswordValidator
import com.settlex.android.util.ui.ProgressDialogManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CreatePasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreatePasswordBinding
    private val keyboardHelper: KeyboardHelper by lazy { KeyboardHelper(this) }
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
        updateUiFromIntent()
        setupListeners()
        setupInputValidation()
        setupInputFocusHandler()
        setupPasswordToggles()
    }

    private fun updateUiFromIntent() {
        binding.currentPasswordContainer.visibility =
            when (passwordFlow) {
                is PasswordFlow.Forgot -> View.GONE
                is PasswordFlow.Change -> View.VISIBLE
                is PasswordFlow.AuthenticatedReset -> View.GONE
            }
    }

    private fun setupListeners() {
        binding.btnChangePassword.setOnClickListener { onChangePasswordClicked() }
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
                    dialog.dismiss()
                    finish()
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
        etPassword.doOnTextChanged { text, _, _, _ ->
            val password = text.toString().trim()
            if (password.isNotEmpty()) togglePasswordVisibility.show() else togglePasswordVisibility.gone()
            updateChangePasswordButtonState()
        }

        etConfirmPassword.doOnTextChanged { text, _, _, _ ->
            val confirmPassword = text.toString().trim()
            if (confirmPassword.isNotEmpty()) toggleConfirmPasswordVisibility.show() else toggleConfirmPasswordVisibility.gone()
            updateChangePasswordButtonState()
        }

        etCurrentPassword.doOnTextChanged { text, _, _, _ ->
            val currentPassword = text.toString().trim()
            if (currentPassword.isNotEmpty()) toggleCurrentPasswordVisibility.show() else toggleCurrentPasswordVisibility.gone()
            updateChangePasswordButtonState()
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

        return PasswordValidator.validate(currentPassword)
    }

    private fun isPasswordValidAndMatch(): Boolean {
        with(binding) {
            val newPassword = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            return PasswordValidator.validate(newPassword, confirmPassword)
        }
    }

    private fun setupPasswordToggles() = with(binding) {
        PasswordToggleController(
            etPassword,
            togglePasswordVisibility
        )

        PasswordToggleController(
            etConfirmPassword,
            toggleConfirmPasswordVisibility
        )

        PasswordToggleController(
            etCurrentPassword,
            toggleCurrentPasswordVisibility
        )
    }

    private fun setupInputFocusHandler() {
        with(binding) {
            EditTextFocusBackgroundChanger(
                defaultBackgroundResource = R.drawable.bg_edit_txt_custom_gray_not_focused,
                focusedBackgroundResource = R.drawable.bg_edit_txt_custom_white_focused,
                etCurrentPassword to etCurrentPasswordBackground,
                etPassword to etPasswordBackground,
                etConfirmPassword to etConfirmPasswordBackground
            )
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (keyboardHelper.handleOutsideTouch(event)) return true
        return super.dispatchTouchEvent(event)
    }
}