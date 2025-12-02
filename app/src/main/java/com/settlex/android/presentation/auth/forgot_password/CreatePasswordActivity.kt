package com.settlex.android.presentation.auth.forgot_password

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.settlex.android.data.exception.AppException
import com.settlex.android.databinding.ActivityCreatePasswordBinding
import com.settlex.android.presentation.auth.AuthViewModel
import com.settlex.android.presentation.auth.util.PasswordFlow
import com.settlex.android.presentation.auth.util.PasswordFlowParser
import com.settlex.android.presentation.common.extensions.gone
import com.settlex.android.presentation.common.extensions.show
import com.settlex.android.presentation.common.state.UiState
import com.settlex.android.presentation.common.util.DialogHelper
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
        setupInputListeners()
        setupListeners()
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
        binding.btnChangePassword.setOnClickListener { setNewPassword() }
    }

    private fun setNewPassword() = with(binding) {
        val oldPassword = etCurrentPassword.text.toString().trim()
        val newPassword = etPassword.text.toString().trim()

        when(passwordFlow){
            is PasswordFlow.Change -> {
                // TODO: call authenticated password reset API
            }
            else -> viewModel.setNewPassword(userEmail, oldPassword, newPassword)
        }
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

    private fun setupInputListeners() = with(binding) {
        when (passwordFlow) {
            is PasswordFlow.Forgot -> {
                val validationWatcher = object : TextWatcher {
                    override fun beforeTextChanged(
                        p0: CharSequence?,
                        p1: Int,
                        p2: Int,
                        p3: Int
                    ) {
                    }

                    override fun onTextChanged(
                        p0: CharSequence?,
                        p1: Int,
                        p2: Int,
                        p3: Int
                    ) {
                        tvConfirmPasswordError.gone()
                        btnChangePassword.isEnabled = isPasswordValidAndMatch()
                    }

                    override fun afterTextChanged(p0: Editable?) {}
                }
                etPassword.addTextChangedListener(validationWatcher)
                etConfirmPassword.addTextChangedListener(validationWatcher)
            }

            else -> {
                val validationWatcher = object : TextWatcher {
                    override fun beforeTextChanged(
                        p0: CharSequence?,
                        p1: Int,
                        p2: Int,
                        p3: Int
                    ) {
                    }

                    override fun afterTextChanged(p0: Editable?) {}

                    override fun onTextChanged(
                        p0: CharSequence?,
                        p1: Int,
                        p2: Int,
                        p3: Int
                    ) {
                        tvCurrentPasswordError.gone()
                        tvConfirmPasswordError.gone()

                        // Update button state
                        btnChangePassword.isEnabled =
                            isCurrentPasswordValid() && isPasswordValidAndMatch()
                    }
                }
                etPassword.addTextChangedListener(validationWatcher)
                etConfirmPassword.addTextChangedListener(validationWatcher)
                etCurrentPassword.addTextChangedListener(validationWatcher)
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
            val confirmPassword = etCurrentPassword.text.toString().trim()

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

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (keyboardHelper.handleOutsideTouch(event)) return true
        return super.dispatchTouchEvent(event)
    }
}

