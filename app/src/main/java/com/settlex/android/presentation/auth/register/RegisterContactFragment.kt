package com.settlex.android.presentation.auth.register

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import com.settlex.android.R
import com.settlex.android.data.enums.OtpType
import com.settlex.android.data.exception.AppException
import com.settlex.android.databinding.FragmentRegisterContactBinding
import com.settlex.android.presentation.auth.AuthViewModel
import com.settlex.android.presentation.auth.login.LoginActivity
import com.settlex.android.presentation.common.extensions.gone
import com.settlex.android.presentation.common.extensions.show
import com.settlex.android.presentation.common.extensions.toNigerianPhoneNumber
import com.settlex.android.presentation.common.state.UiState
import com.settlex.android.presentation.common.util.EditTextFocusBackgroundChanger
import com.settlex.android.presentation.common.util.FocusManager
import com.settlex.android.presentation.legal.PrivacyPolicyActivity
import com.settlex.android.presentation.legal.TermsAndConditionsActivity
import com.settlex.android.util.ui.ProgressDialogManager
import com.settlex.android.util.ui.StatusBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class RegisterContactFragment : Fragment() {
    private val authViewModel: AuthViewModel by activityViewModels()
    private val registerViewModel: RegisterViewModel by activityViewModels()
    private val progressLoader by lazy { ProgressDialogManager(requireActivity()) }
    private val focusManager by lazy { FocusManager(requireActivity()) }
    private var _binding: FragmentRegisterContactBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterContactBinding.inflate(
            inflater,
            container,
            false
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        observeOtpSendState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initViews() {
        StatusBar.setColor(requireActivity(), R.color.surface)
        setupListeners()
        setupLegalLinks()
        setupInputValidation()
        setupFocusHandlers()
        focusManager.attachDoneAction(binding.etPhone)
    }

    private fun setupListeners() = with(binding) {
        btnSignIn.setOnClickListener {
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }

        btnBackBefore.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
            requireActivity().finish()
        }

        btnContinue.setOnClickListener { onContinueClicked() }
    }

    private fun observeOtpSendState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.otpEvent.collect { state ->
                    when (state) {
                        is UiState.Loading -> progressLoader.show()
                        is UiState.Success -> onOtpSent()
                        is UiState.Failure -> onOtpSendFailure(state.exception)
                    }
                }
            }
        }
    }

    private fun onOtpSent() {
        val navController = NavHostFragment.findNavController(this)
        navController.navigate(R.id.register_email_verification_fragment)

        progressLoader.hide()
    }

    private fun onOtpSendFailure(error: AppException) = with(binding) {
        tvError.text = error.message
        tvError.show()

        progressLoader.hide()
    }

    private fun onContinueClicked() = with(binding) {
        tvError.gone()

        val email = etEmail.text.toString().trim().lowercase()
        val phone = etPhone.text.toString().trim()

        registerViewModel.updateContact(email, phone.toNigerianPhoneNumber())
        sendVerificationCode(email)
    }

    private fun sendVerificationCode(email: String) {
        authViewModel.sendVerificationCode(email, OtpType.EMAIL_VERIFICATION)
    }

    private fun setupFocusHandlers() = with(binding) {
        val focusBgRes = R.drawable.bg_edit_txt_custom_white_focused
        val defaultBgRes = R.drawable.bg_input_field_outlined
        EditTextFocusBackgroundChanger(
            defaultBackgroundResource = defaultBgRes,
            focusedBackgroundResource = focusBgRes,
            etPhone to etPhoneBackground,
        )
    }

    private fun setupInputValidation() = with(binding) {
        val validationWatcher: TextWatcher = object : TextWatcher {
            override fun beforeTextChanged(
                text: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun afterTextChanged(text: Editable?) {
            }

            override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
                tvError.gone()
                updateContinueButtonState()
            }
        }
        etEmail.addTextChangedListener(validationWatcher)
        etPhone.addTextChangedListener(validationWatcher)
        checkBoxTermsPrivacy.setOnCheckedChangeListener { _, _ -> updateContinueButtonState() }
    }

    private fun updateContinueButtonState() = with(binding) {
        val email = etEmail.text.toString().trim().lowercase()
        val phoneNumber = etPhone.text.toString().trim()

        setContinueButtonEnabled(isEmailValid(email) && isPhoneValid(phoneNumber) && isTermsAndPrivacyChecked())
    }

    private fun isEmailValid(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isPhoneValid(phone: String): Boolean {
        return phone.isNotEmpty() && phone.matches(PHONE_NUMBER_REGEX.toRegex())
    }

    private fun isTermsAndPrivacyChecked(): Boolean {
        return binding.checkBoxTermsPrivacy.isChecked
    }

    private fun setContinueButtonEnabled(allValid: Boolean) {
        binding.btnContinue.isEnabled = allValid
    }

    private fun setupLegalLinks() = with(binding) {

        val span =
            SpannableStringBuilder("I have read, understood and agreed to the Terms & Conditions and Privacy Policy.")

        val termsSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                startActivity(TermsAndConditionsActivity::class.java)
            }

            override fun updateDrawState(textPaint: TextPaint) {
                textPaint.color = ContextCompat.getColor(requireContext(), R.color.blue_500)
                textPaint.isUnderlineText = true
            }
        }

        val privacySpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                startActivity(PrivacyPolicyActivity::class.java)
            }

            override fun updateDrawState(textPaint: TextPaint) {
                textPaint.color = ContextCompat.getColor(requireContext(), R.color.primary)
                textPaint.isUnderlineText = true
            }
        }

        span.setSpan(
            termsSpan,
            span.indexOf("Terms"),
            span.indexOf("Conditions") + "Conditions".length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        span.setSpan(
            privacySpan,
            span.indexOf("Privacy"),
            span.indexOf("Policy") + "Policy".length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        tvTermsPrivacy.text = span
        tvTermsPrivacy.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun startActivity(activityClass: Class<out Activity>) {
        startActivity(Intent(requireContext(), activityClass))
    }

    companion object {
        private const val PHONE_NUMBER_REGEX = "^(0)?[7-9][0-1]\\d{8}$"
    }
}