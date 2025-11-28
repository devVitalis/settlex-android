package com.settlex.android.presentation.auth.forgot_password

import android.graphics.Rect
import android.os.Bundle
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
import com.settlex.android.databinding.ActivityCreatePasswordBinding
import com.settlex.android.presentation.auth.AuthViewModel
import com.settlex.android.presentation.common.util.PasswordValidator
import com.settlex.android.presentation.common.util.PasswordToggleController
import com.settlex.android.presentation.auth.util.PasswordFlow
import com.settlex.android.presentation.auth.util.PasswordFlowParser
import com.settlex.android.presentation.common.state.UiState
import com.settlex.android.presentation.common.util.DialogHelper
import com.settlex.android.util.ui.ProgressLoaderController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CreatePasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreatePasswordBinding
    private val progressLoader: ProgressLoaderController by lazy { ProgressLoaderController(this) }
    private val viewModel: AuthViewModel by viewModels()

    private lateinit var passwordFlow: PasswordFlow
    private lateinit var userEmail: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreatePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        passwordFlow = PasswordFlowParser.fromIntent(intent)
        userEmail =  intent.getStringExtra("email")!!

        updateUiForFlow()
        setupListeners()
        initInputListeners()
        setupPasswordToggles()
        observeSetNewPasswordEvents()
    }

    private fun updateUiForFlow() {
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

    private fun setNewPassword() {
        val oldPassword = binding.etCurrentPassword.text.toString().trim()
        val newPassword = binding.etPassword.text.toString().trim()

        if (passwordFlow == PasswordFlow.Change) {
            // TODO: call authenticated password reset API
            return
        }
        viewModel.setNewPassword(userEmail, oldPassword, newPassword)
    }

    // Observers
    private fun observeSetNewPasswordEvents() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.setNewPasswordEvent.collect {
                    when (it) {
                        is UiState.Loading -> showLoader()
                        is UiState.Success -> showSuccessDialog()
                        is UiState.Failure -> showPasswordChangeError(it.exception.message)
                    }
                }
            }
        }
    }

    private fun showSuccessDialog() {
        val title = "Success"
        val message = "Your password has been changed successfully"
        val btnTxt = "Continue"

        DialogHelper.showSuccessBottomSheetDialog(this) { dialog, binding ->
            binding.tvTitle.text = title
            binding.tvMessage.text = message
            binding.btnAction.text = btnTxt

            binding.btnAction.setOnClickListener {
                dialog.dismiss()
                finish()
            }
        }
        hideLoader()
    }

    private fun showPasswordChangeError(error: String?) {
        binding.tvConfirmPasswordError.text = error
        binding.tvConfirmPasswordError.visibility = View.VISIBLE

        hideLoader()
    }

    private fun showLoader() {
        progressLoader.show()
    }

    private fun hideLoader() {
        progressLoader.hide()
    }

    private fun initInputListeners() {
        if (passwordFlow == PasswordFlow.Forgot) {
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
                    binding.tvConfirmPasswordError.visibility = View.GONE

                    // Update button state
                    binding.btnChangePassword.isEnabled = isPasswordValidAndMatch()
                }

                override fun afterTextChanged(p0: Editable?) {}
            }
            binding.etPassword.addTextChangedListener(validationWatcher)
            binding.etConfirmPassword.addTextChangedListener(validationWatcher)
        } else {
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
                    binding.tvCurrentPasswordError.visibility = View.GONE
                    binding.tvConfirmPasswordError.visibility = View.GONE

                    // Update button state
                    binding.btnChangePassword.isEnabled =
                        isCurrentPasswordValid() && isPasswordValidAndMatch()
                }
            }
            binding.etPassword.addTextChangedListener(validationWatcher)
            binding.etConfirmPassword.addTextChangedListener(validationWatcher)
            binding.etCurrentPassword.addTextChangedListener(validationWatcher)
        }
    }

    private fun isCurrentPasswordValid(): Boolean {
        val currentPassword = binding.etCurrentPassword.text.toString().trim()

        return PasswordValidator.validate(currentPassword)
    }

    private fun isPasswordValidAndMatch(): Boolean {
        val newPassword = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etCurrentPassword.text.toString().trim()

        return PasswordValidator.validate(newPassword, confirmPassword)
    }

    private fun setupPasswordToggles() {
        PasswordToggleController(
            binding.etPassword,
            binding.togglePasswordVisibility
        )

        PasswordToggleController(
            binding.etConfirmPassword,
            binding.toggleConfirmPasswordVisibility
        )

        PasswordToggleController(
            binding.etCurrentPassword,
            binding.toggleCurrentPasswordVisibility
        )
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

