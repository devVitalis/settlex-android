package com.settlex.android.ui.auth.register

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
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import com.settlex.android.R
import com.settlex.android.data.enums.OtpType
import com.settlex.android.databinding.FragmentRegisterContactBinding
import com.settlex.android.ui.auth.AuthViewModel
import com.settlex.android.ui.auth.login.LoginActivity
import com.settlex.android.ui.info.legal.PrivacyPolicyActivity
import com.settlex.android.ui.info.legal.TermsAndConditionsActivity
import com.settlex.android.util.event.UiState
import com.settlex.android.util.string.StringFormatter
import com.settlex.android.util.ui.ProgressLoaderController
import com.settlex.android.util.ui.StatusBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * A [Fragment] that handles the user's contact information input during the registration process.
 *
 * This screen prompts the user to enter their email address and phone number. It performs
 * validation on the inputs and requires the user to agree to the Terms & Conditions and
 * Privacy Policy before proceeding.
 *
 * This fragment is part of the registration navigation graph and uses shared ViewModels
 * ([AuthViewModel], [RegisterViewModel]) to maintain state across different registration steps.
 *
 * @see AuthViewModel
 * @see RegisterViewModel
 */
@AndroidEntryPoint
class RegisterContactFragment : Fragment() {
    private val authViewModel: AuthViewModel by activityViewModels()
    private val registerViewModel: RegisterViewModel by activityViewModels()
    private val progressLoader by lazy { ProgressLoaderController(requireActivity()) }
    private var _binding: FragmentRegisterContactBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val PHONE_NUMBER_REGEX = "^(0)?[7-9][0-1]\\d{8}$"
    }

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

        setupUiActions()
        observeOtpState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupUiActions() {
        StatusBar.setColor(requireActivity(), R.color.white)
        setupLegalLinks()
        setupInputValidation()
        setupEditTextFocusHandlers()
        setupDoneActionOnPhoneEditText()

        binding.btnSignIn.setOnClickListener {
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }
        binding.btnBackBefore.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
            requireActivity().finish()
        }
        binding.btnHelp.setOnClickListener {
            StringFormatter.showNotImplementedToast(
                requireContext()
            )
        }
        binding.btnContinue.setOnClickListener { onContinueClicked() }
    }

    private fun observeOtpState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.otpEvent.collect { otpSendState ->
                    when (otpSendState) {
                        is UiState.Success -> onOtpSendSuccess()
                        is UiState.Failure -> onOtpSendFailure(otpSendState.message)
                        is UiState.Loading -> progressLoader.show()
                    }
                }
            }
        }
    }

    private fun onOtpSendSuccess() {
        val navController = NavHostFragment.findNavController(this)
        navController.navigate(R.id.register_email_verification_fragment)
        progressLoader.hide()
    }

    private fun onOtpSendFailure(error: String?) {
        binding.txtError.text = error
        binding.txtError.visibility = View.VISIBLE
        progressLoader.hide()
    }

    private fun onContinueClicked() {
        val email = binding.editTxtEmail.text.toString().trim().lowercase()
        val phone = binding.editTxtPhone.text.toString().trim()
        val formattedPhone = StringFormatter.formatPhoneWithCode(phone)

        registerViewModel.updateContact(email, formattedPhone)
        sendVerificationCode(email)
    }

    private fun sendVerificationCode(email: String) {
        authViewModel.sendVerificationCode(email, OtpType.EMAIL_VERIFICATION)
    }

    private fun setupEditTextFocusHandlers() {
        // cache drawables
        val focusBgRes = R.drawable.bg_edit_txt_custom_white_focused
        val defaultBgRes = R.drawable.bg_edit_txt_custom_white_not_focused

        binding.editTxtPhone.setOnFocusChangeListener { _, hasFocus: Boolean ->
            binding.editTxtPhoneBg.setBackgroundResource(if (hasFocus) focusBgRes else defaultBgRes)
        }
        binding.editTxtEmail.setOnFocusChangeListener { _, hasFocus: Boolean ->
            binding.editTxtEmailBg.setBackgroundResource(if (hasFocus) focusBgRes else defaultBgRes)
        }
    }

    private fun setupInputValidation() {
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
                binding.txtError.visibility = View.GONE
                updateContinueButtonState()
            }
        }
        binding.editTxtEmail.addTextChangedListener(validationWatcher)
        binding.editTxtPhone.addTextChangedListener(validationWatcher)
        binding.checkBoxTermsPrivacy.setOnCheckedChangeListener { _, _ -> updateContinueButtonState() }
    }

    private fun updateContinueButtonState() {
        val email = binding.editTxtEmail.text.toString().trim().lowercase()
        val phoneNumber = binding.editTxtPhone.text.toString().trim()

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

    private fun setupLegalLinks() {
        val legalText =
            "I have read, understood and agreed to the Terms & Conditions and Privacy Policy."
        val span = SpannableStringBuilder(legalText)

        val termsSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                routeToDestination(TermsAndConditionsActivity::class.java)
            }

            override fun updateDrawState(textPaint: TextPaint) {
                textPaint.color = ContextCompat.getColor(requireContext(), R.color.blue)
                textPaint.isUnderlineText = false
            }
        }

        val privacySpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                routeToDestination(PrivacyPolicyActivity::class.java)
            }

            override fun updateDrawState(textPaint: TextPaint) {
                textPaint.color = ContextCompat.getColor(requireContext(), R.color.blue)
                textPaint.isUnderlineText = false
            }
        }

        span.setSpan(
            termsSpan,
            legalText.indexOf("Terms"),
            legalText.indexOf("Conditions") + "Conditions".length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        span.setSpan(
            privacySpan,
            legalText.indexOf("Privacy"),
            legalText.indexOf("Policy") + "Policy".length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.txtTermsPrivacy.text = span
        binding.txtTermsPrivacy.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun routeToDestination(activityClass: Class<out Activity>) {
        startActivity(Intent(requireContext(), activityClass))
    }

    private fun setupDoneActionOnPhoneEditText() {
        binding.editTxtPhone.setOnEditorActionListener { view, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val inputMethodManager =
                    ContextCompat.getSystemService(view.context, InputMethodManager::class.java)
                inputMethodManager?.hideSoftInputFromWindow(view.windowToken, 0)
                view.clearFocus()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
    }
}