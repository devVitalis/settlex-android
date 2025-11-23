package com.settlex.android.ui.auth.login

import android.app.Activity
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.MotionEvent
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.settlex.android.R
import com.settlex.android.data.remote.profile.ProfileService
import com.settlex.android.databinding.ActivityLoginBinding
import com.settlex.android.ui.auth.AuthViewModel
import com.settlex.android.ui.auth.forgot_password.ForgotPasswordActivity
import com.settlex.android.ui.auth.register.RegisterActivity
import com.settlex.android.ui.common.components.BiometricAuthHelper
import com.settlex.android.ui.common.components.BiometricAuthHelper.BiometricAuthCallback
import com.settlex.android.ui.common.state.UiState
import com.settlex.android.ui.dashboard.DashboardActivity
import com.settlex.android.util.string.StringFormatter
import com.settlex.android.util.ui.ProgressLoaderController
import com.settlex.android.util.ui.StatusBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    private var isPasswordVisible = false

    // Dependencies
    private val progressLoader: ProgressLoaderController by lazy { ProgressLoaderController(this) }
    private lateinit var binding: ActivityLoginBinding
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUiActions()
        observeLoginEvent()
        syncUserStateWithUI()
        setupClickListeners()
    }

    private fun setupUiActions() {
        StatusBar.setColor(this, R.color.white)
        setupAuthActionTexts()
        setupInputValidation()
        setupEditTextFocusHandler()
        setupPasswordInputDoneAction()
    }

    private fun setupClickListeners() {
        binding.btnSignUp.setOnClickListener {
            navigateTo(
                RegisterActivity::class.java
            )
        }
        binding.btnForgotPassword.setOnClickListener {
            navigateTo(
                ForgotPasswordActivity::class.java
            )
        }
        binding.btnBackBefore.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.btnSwitchAccount.setOnClickListener { showUnauthenticatedUi() }
        binding.btnFingerprint.setOnClickListener { promptBiometricsAuth() }
        binding.btnTogglePassword.setOnClickListener { togglePasswordVisibility() }
        binding.btnSignIn.setOnClickListener { attemptLogin() }
    }

    private fun syncUserStateWithUI() {
        val userState = authViewModel.userState.value
        when (userState) {
            is LoginState.NoUser -> {
                showUnauthenticatedUi()
            }

            is LoginState.CurrentUser -> {
                showAuthenticatedUi(
                    userState.photoUrl,
                    userState.displayName,
                    userState.email
                )
            }
        }

        // check user pref
        // val isFingerPrintEnabled =
        // Boolean.TRUE == authViewModel.getLoginBiometricsEnabled().getValue()
        // binding.btnFingerprint.visibility = if (isFingerPrintEnabled) View.VISIBLE else View.GONE
        // if (isFingerPrintEnabled) promptBiometricsAuth()
    }

    private fun showAuthenticatedUi(photoUrl: String?, displayName: String, email: String) {
        val formattedDisplayName = "Hi, ${displayName.uppercase()}"
        val formattedEmail = "( $email )"

        ProfileService.loadProfilePic(photoUrl, binding.userProfile)
        binding.tvUserDisplayName.text = formattedDisplayName
        binding.tvUserEmail.text = StringFormatter.maskEmail(email)
        binding.etEmail.setText(formattedEmail)

        // Show
        binding.showCurrentUserLayout.visibility = View.VISIBLE
        binding.btnSwitchAccount.visibility = View.VISIBLE

        // Hide
        binding.logo.visibility = View.GONE
        binding.tilEmail.visibility = View.GONE
        binding.btnSignUp.visibility = View.GONE
    }

    private fun showUnauthenticatedUi() {
        // Hide
        binding.showCurrentUserLayout.visibility = View.GONE
        binding.btnFingerprint.visibility = View.GONE
        binding.btnSwitchAccount.visibility = View.GONE

        // Show
        binding.tilEmail.visibility = View.VISIBLE
        binding.btnSignUp.visibility = View.VISIBLE
        binding.logo.visibility = View.VISIBLE
    }

    // Observers
    private fun observeLoginEvent() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.loginEvent.collect {
                    when (it) {
                        is UiState.Loading -> progressLoader.show()
                        is UiState.Success -> onLoginSuccess()
                        is UiState.Failure -> onLoginFailure(it.exception.message)
                    }
                }
            }
        }
    }

    private fun onLoginSuccess() {
        startActivity(Intent(this, DashboardActivity::class.java))
        finishAffinity()

        progressLoader.hide()
    }

    private fun onLoginFailure(error: String?) {
        binding.tvError.text = error
        binding.tvError.visibility = View.VISIBLE

        progressLoader.hide()
    }

    private fun attemptLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        authViewModel.login(email, password)
    }

    private fun promptBiometricsAuth() {
        if (BiometricAuthHelper.isBiometricAvailable(this)) {
            val biometric = BiometricAuthHelper(
                this,
                this,
                object : BiometricAuthCallback {
                    override fun onAuthenticated() {
                        navigateTo(DashboardActivity::class.java)
                    }

                    override fun onError(message: String?) {
                    }

                    override fun onFailed() {
                    }
                })
            biometric.authenticate("Confirm your identity", "Use Password")
        }
    }

    private fun togglePasswordVisibility() {
        val currentTypeface = binding.etPassword.typeface
        isPasswordVisible = !isPasswordVisible

        val inputType = if (isPasswordVisible) {
            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        binding.etPassword.inputType = InputType.TYPE_CLASS_TEXT or inputType
        binding.btnTogglePassword.setImageResource(if (isPasswordVisible) R.drawable.ic_visibility_on_filled else R.drawable.ic_visibility_off_filled)

        binding.etPassword.setTypeface(currentTypeface)
        binding.etPassword.setSelection(binding.etPassword.getText()!!.length)
    }

    private fun setupAuthActionTexts() {
        val fullText = "Don't have an account yet?\nClick here to register"
        val textToHighlight = "Click here to register"

        val startIndex = fullText.indexOf(textToHighlight)
        val endIndex = fullText.length

        val signUpText = SpannableString(fullText)
        signUpText.setSpan(
            ForegroundColorSpan("#0044CC".toColorInt()),
            startIndex,
            endIndex,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.btnSignUp.text = signUpText

        setupSwitchAccount()
    }

    private fun setupSwitchAccount() {
        val fullText = "Not you?\nSwitch Account"
        val textToHighlight = "Switch Account"

        val startIndex = fullText.indexOf(textToHighlight)
        val endIndex = startIndex + textToHighlight.length

        val switchText = SpannableString(fullText)
        switchText.setSpan(
            ForegroundColorSpan("#0044CC".toColorInt()),
            startIndex,
            endIndex,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.btnSwitchAccount.text = switchText
    }

    private fun navigateTo(activityClass: Class<out Activity>) {
        startActivity(Intent(this, activityClass))
    }

    private fun setupEditTextFocusHandler() {
        val focusBgRes = R.drawable.bg_edit_txt_custom_white_focused
        val defaultBgRes = R.drawable.bg_edit_txt_custom_gray_not_focused

        binding.etPassword.onFocusChangeListener =
            OnFocusChangeListener { _, hasFocus: Boolean ->
                binding.etPasswordBg.setBackgroundResource(if (hasFocus) focusBgRes else defaultBgRes)
            }
    }

    private fun updateSignInButtonState() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        binding.btnSignIn.isEnabled = email.isNotEmpty() && password.isNotEmpty()
    }

    private fun setupInputValidation() {
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
                val password = binding.etPassword.getText().toString().trim()
                binding.btnTogglePassword.visibility =
                    if (password.isNotEmpty()) View.VISIBLE else View.GONE
                binding.tvError.visibility = View.GONE
                updateSignInButtonState()
            }
        }

        binding.etEmail.addTextChangedListener(validationWatcher)
        binding.etPassword.addTextChangedListener(validationWatcher)
    }

    private fun setupPasswordInputDoneAction() {
        binding.etPassword.setOnEditorActionListener { v: TextView?, actionId: Int, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard(v!!)
                v.clearFocus()
                return@setOnEditorActionListener true
            }
            false
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

    private fun hideKeyboard(view: View) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager?
        imm!!.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
