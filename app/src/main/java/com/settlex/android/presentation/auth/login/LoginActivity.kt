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
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.settlex.android.R
import com.settlex.android.data.exception.AppException
import com.settlex.android.data.remote.profile.ProfileService.loadProfilePhoto
import com.settlex.android.databinding.ActivityLoginBinding
import com.settlex.android.presentation.auth.forgot_password.ForgotPasswordActivity
import com.settlex.android.presentation.auth.register.RegisterActivity
import com.settlex.android.presentation.common.components.BiometricAuthManager
import com.settlex.android.presentation.common.components.BiometricAuthManager.BiometricAuthCallback
import com.settlex.android.presentation.common.extensions.gone
import com.settlex.android.presentation.common.extensions.show
import com.settlex.android.presentation.common.state.UiState
import com.settlex.android.presentation.common.util.DialogHelper
import com.settlex.android.presentation.common.util.KeyboardHelper
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

    private fun initViews() = with(binding) {
        StatusBar.setColor(this@LoginActivity, R.color.colorBackground)
        setupInputValidation()
        keyboardHelper.attachDoneAction(editText = etPassword)

        tvSwitchAccount.text = SpannableTextFormatter(
            this@LoginActivity,
            "Not you?\nSwitch Account",
            "Switch Account",
            ContextCompat.getColor(this@LoginActivity, R.color.colorPrimary)
        )

        tvSignUp.text = SpannableTextFormatter(
            this@LoginActivity,
            "Don't have an account yet?\nClick here to register",
            "Click here to register",
            ContextCompat.getColor(this@LoginActivity, R.color.colorPrimary)
        )
    }

    private fun initObservers() {
        observeLoginEvent()
    }

    private fun initListeners() = with(binding) {
        tvForgotPassword.setOnClickListener {
            launchActivity(
                ForgotPasswordActivity::class.java
            )
        }
        btnBackBefore.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        tvSwitchAccount.setOnClickListener { showLoggedOutView() }
        ivFingerprint.setOnClickListener { authenticateWithBiometrics() }
        tvSignUp.setOnClickListener { launchActivity(RegisterActivity::class.java) }
        btnSignIn.setOnClickListener { tryLogin() }
    }

    private fun syncUserStateWithUI() {
        val state = viewModel.userState.value
        when (state) {
            is LoginState.LoggedOut -> showLoggedOutView()
            is LoginState.LoggedInUser -> {
                showLoggedUser(state)

                with(binding) {
                    // Enable/disable fingerprint auth
                    val isFingerPrintEnabled = viewModel.isLoginBiometricsEnabled.value
                    when (isFingerPrintEnabled) {
                        true -> {
                            ivFingerprint.show()
                            authenticateWithBiometrics()
                        }

                        false -> ivFingerprint.gone()
                    }
                }
            }
        }
    }

    private fun showLoggedUser(user: LoginState.LoggedInUser) {
        with(binding) {
            val formattedDisplayName = "Hi, ${user.displayName.uppercase()}"
            val formattedEmail = "(${StringFormatter.maskEmail(user.email)})"

            loadProfilePhoto(user.photoUrl, ivUserProfilePhoto)
            tvUserDisplayName.text = formattedDisplayName
            tvUserEmail.text = formattedEmail
            etEmail.setText(user.email)

            viewAuthenticatedUi.show()
            tvSwitchAccount.show()

            listOf(ivLogo, tilEmail, tvSignUp).forEach { it.gone() }
        }
    }

    private fun showLoggedOutView() = with(binding) {
        viewModel.logout()

        listOf(
            viewAuthenticatedUi,
            ivFingerprint,
            tvSwitchAccount
        ).forEach { it.gone() }

        listOf(
            tilEmail,
            tvSignUp,
            ivLogo
        ).forEach { it.show() }
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

    private fun onLoginFailure(error: AppException) = with(binding) {
        when (error) {
            is AppException.NetworkException -> showNetworkErrorDialog(error)
            else -> {
                tvLoginError.text = error.message
                tvLoginError.show()
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

    private fun tryLogin() = with(binding) {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        viewModel.login(email, password)
    }

    private fun authenticateWithBiometrics() {
        if (BiometricAuthManager.isBiometricAvailable(this)) {
            val biometric = BiometricAuthManager(
                this, this, object : BiometricAuthCallback {
                    override fun onAuthenticated() {
                        launchActivity(DashboardActivity::class.java)
                    }

                    override fun onError(message: String?) {}
                    override fun onFailed() {}
                })
            biometric.authenticate("Confirm your identity", "Use Password")
        }
    }

    private fun launchActivity(activityClass: Class<out Activity>) {
        startActivity(Intent(this, activityClass))
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
