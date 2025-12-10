package com.settlex.android.presentation.auth.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.MotionEvent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.settlex.android.R
import com.settlex.android.data.exception.AppException
import com.settlex.android.data.remote.profile.ProfileService
import com.settlex.android.databinding.ActivityLoginBinding
import com.settlex.android.presentation.auth.forgot_password.ForgotPasswordActivity
import com.settlex.android.presentation.auth.register.RegisterActivity
import com.settlex.android.presentation.common.components.BiometricAuthManager
import com.settlex.android.presentation.common.components.BiometricAuthManager.BiometricAuthCallback
import com.settlex.android.presentation.common.extensions.gone
import com.settlex.android.presentation.common.extensions.show
import com.settlex.android.presentation.common.state.UiState
import com.settlex.android.presentation.common.util.DialogHelper
import com.settlex.android.presentation.common.util.EditTextFocusBackgroundChanger
import com.settlex.android.presentation.common.util.KeyboardHelper
import com.settlex.android.presentation.common.util.PasswordToggleController
import com.settlex.android.presentation.common.util.SpannableTextFormatter
import com.settlex.android.presentation.dashboard.DashboardActivity
import com.settlex.android.util.string.StringFormatter
import com.settlex.android.util.ui.ProgressDialogManager
import com.settlex.android.util.ui.StatusBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val keyboardHelper: KeyboardHelper by lazy { KeyboardHelper(this) }
    private val progressLoader: ProgressDialogManager by lazy { ProgressDialogManager(this) }
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        initObservers()
        syncUserStateWithUI()
        initListeners()
    }

    private fun initViews() {
        StatusBar.setColor(this, R.color.white)
        setupInputValidation()
        setupFocusBackgroundChanger()
        keyboardHelper.attachDoneAction(editText = binding.etPassword)

        with(binding) {
            tvSwitchAccount.text = SpannableTextFormatter.format(
                "Not you?\nSwitch Account",
                "Switch Account"
            )

            tvSignUp.text = SpannableTextFormatter.format(
                "Don't have an account yet?\nClick here to register",
                "Click here to register"
            )
        }
    }

    private fun initObservers() {
        observeLoginEvent()
    }

    private fun initListeners() {
        with(binding) {
            btnForgotPassword.setOnClickListener {
                toActivity(
                    ForgotPasswordActivity::class.java
                )
            }
            btnBackBefore.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
            tvSwitchAccount.setOnClickListener { showUnauthenticatedUi() }
            ivFingerprint.setOnClickListener { authenticateWithBiometrics() }

            btnTogglePassword.setOnClickListener {
                PasswordToggleController(
                    binding.etPassword,
                    binding.btnTogglePassword
                )
            }

            btnSignIn.setOnClickListener { attemptLogin() }
            tvSignUp.setOnClickListener {
                toActivity(
                    RegisterActivity::class.java
                )
            }
        }
    }

    private fun syncUserStateWithUI() {
        val state = viewModel.userState.value
        when (state) {
            is LoginState.LoggedOut -> showUnauthenticatedUi()
            is LoginState.LoggedInUser -> {
                showLoggedUser(state)

                with(binding) {
                    // Enable/disable fingerprint auth
                    val isFingerPrintEnabled = viewModel.isLoginBiometricsEnabled.value
                    if (isFingerPrintEnabled) ivFingerprint.show() else ivFingerprint.gone()
                    if (isFingerPrintEnabled) authenticateWithBiometrics()
                }
            }
        }
    }

    private fun showLoggedUser(user: LoginState.LoggedInUser) {
        with(binding) {
            val formattedDisplayName = "Hi, ${user.displayName.uppercase()}"
            val formattedEmail = "(${StringFormatter.maskEmail(user.email)})"

            ProfileService.loadProfilePic(user.photoUrl, ivUserProfilePhoto)
            tvUserDisplayName.text = formattedDisplayName
            tvUserEmail.text = formattedEmail
            etEmail.setText(user.email)

            viewLoggedInUi.show()
            tvSwitchAccount.show()

            ivLogo.gone()
            tilEmail.gone()
            tvSignUp.gone()
        }
    }

    private fun showUnauthenticatedUi() {
        with(binding) {
            viewLoggedInUi.gone()
            ivFingerprint.gone()
            tvSwitchAccount.gone()

            tilEmail.show()
            tvSignUp.show()
            ivLogo.show()
        }
    }

    // Observers
    private fun observeLoginEvent() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loginEvent.collect {
                    when (it) {
                        is UiState.Loading -> progressLoader.show()
                        is UiState.Success -> onLoginSuccess()
                        is UiState.Failure -> onLoginFailure(it.exception)
                    }
                }
            }
        }
    }

    private fun onLoginSuccess() {
        startActivity(
            Intent(
                this,
                DashboardActivity::class.java
            )
        )
        finishAffinity()

        progressLoader.hide()
    }

    private fun onLoginFailure(error: AppException) {
        when (error) {
            is AppException.NetworkException -> showNetworkErrorDialog(error)
            else -> {
                binding.tvLoginError.text = error.message
                binding.tvLoginError.show()
            }
        }

        progressLoader.hide()
    }

    private fun showNetworkErrorDialog(error: AppException) {
        DialogHelper.showAlertDialogMessage(
            this
        ) { dialog, binding ->

            with(binding) {
                tvMessage.text = error.message
                "Okay".also { btnPrimary.text = it }

                btnPrimary.setOnClickListener { dialog.dismiss() }
                btnSecondary.gone()
            }
        }
    }

    private fun attemptLogin() = with(binding) {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        viewModel.login(email, password)
    }

    private fun authenticateWithBiometrics() {
        if (BiometricAuthManager.isBiometricAvailable(this)) {
            val biometric = BiometricAuthManager(
                this, this, object : BiometricAuthCallback {
                    override fun onAuthenticated() {
                        toActivity(DashboardActivity::class.java)
                    }

                    override fun onError(message: String?) {}
                    override fun onFailed() {}
                })
            biometric.authenticate("Confirm your identity", "Use Password")
        }
    }

    private fun toActivity(activityClass: Class<out Activity>) {
        startActivity(Intent(this, activityClass))
    }

    private fun setupFocusBackgroundChanger() {
        with(binding) {
            EditTextFocusBackgroundChanger(
                defaultBackgroundResource = R.drawable.bg_edit_txt_custom_gray_not_focused,
                focusedBackgroundResource = R.drawable.bg_edit_txt_custom_white_focused,
                etPassword to etPasswordBackground
            )
        }
    }

    private fun updateSignInButtonState() = with(binding) {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val isEmailValid = Patterns.EMAIL_ADDRESS.matcher(email).matches()

        btnSignIn.isEnabled = isEmailValid && password.isNotEmpty()
    }

    private fun setupInputValidation() = with(binding) {
        val validationWatcher: TextWatcher = object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun afterTextChanged(s: Editable?) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val password = etPassword.text.toString().trim()
                if (password.isNotEmpty()) btnTogglePassword.show() else btnTogglePassword.gone()

                tvLoginError.gone()
                updateSignInButtonState()
            }
        }

        etEmail.addTextChangedListener(validationWatcher)
        etPassword.addTextChangedListener(validationWatcher)
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (keyboardHelper.handleOutsideTouch(event)) return true
        return super.dispatchTouchEvent(event)
    }
}
