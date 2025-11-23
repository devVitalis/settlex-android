package com.settlex.android.presentation.auth.register

import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.settlex.android.R
import com.settlex.android.data.enums.OtpType
import com.settlex.android.data.exception.AppException
import com.settlex.android.databinding.FragmentRegisterEmailVerificationBinding
import com.settlex.android.presentation.auth.AuthViewModel
import com.settlex.android.presentation.common.state.UiState
import com.settlex.android.util.string.StringFormatter
import com.settlex.android.util.ui.ProgressLoaderController
import com.settlex.android.util.ui.StatusBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * A [Fragment] that handles the user's email verification step during the registration process.
 *
 * This screen prompts the user to enter a One-Time Password (OTP) that has been sent
 * to their email address.
 *
 * This fragment observes [AuthViewModel] to handle the state of the email verification
 * and OTP resend API calls. It also interacts with [RegisterViewModel] to retrieve the user's
 * email address provided in the previous step.
 *
 * @see AuthViewModel
 * @see RegisterViewModel
 */
@AndroidEntryPoint
class RegisterEmailVerificationFragment : Fragment() {
    private var _binding: FragmentRegisterEmailVerificationBinding? = null
    private val binding = _binding!!
    private val progressLoader: ProgressLoaderController by lazy {
        ProgressLoaderController(
            requireActivity()
        )
    }

    private val authViewModel: AuthViewModel by activityViewModels()
    private val registerViewModel: RegisterViewModel by activityViewModels()
    private val email: String by lazy { registerViewModel.email }
    private var otpResendCountdownTimer: CountDownTimer? = null

    companion object {
        private const val OTP_RESEND_COOLDOWN_MS = 60000L
        private const val COUNTDOWN_INTERVAL_MS = 1000L
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding =
            FragmentRegisterEmailVerificationBinding.inflate(
                inflater,
                container,
                false
            )

        setupUiActions()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startOtpResendCooldownTimer()
        observeEmailVerificationStatus()
        observeOtpSendState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        clearResources()
    }

    private fun clearResources() {
        otpResendCountdownTimer?.cancel()
        _binding = null
    }

    private fun setupUiActions() {
        StatusBar.setColor(requireActivity(), R.color.white)
        styleSpamHintText()
        addOtpInputTextWatcher()
        displayMaskedEmail()

        binding.btnBackBefore.setOnClickListener {
            NavHostFragment.findNavController(
                this
            ).popBackStack()
        }

        binding.btnHelp.setOnClickListener {
            StringFormatter.showNotImplementedToast(
                requireContext()
            )
        }

        binding.btnResendOtp.setOnClickListener {
            authViewModel.sendVerificationCode(
                email,
                OtpType.EMAIL_VERIFICATION
            )
        }
        binding.btnContinue.setOnClickListener {
            authViewModel.verifyEmail(
                email,
                getEnteredOtp()
            )
        }
    }

    private fun displayMaskedEmail() {
        binding.tvUserEmail.text = StringFormatter.maskEmail(email)
    }

    // Observers
    private fun observeEmailVerificationStatus() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.verifyEmailEvent.collect { state ->
                    when (state) {
                        is UiState.Loading -> progressLoader.show()
                        is UiState.Success -> onEmailVerificationSuccess()
                        is UiState.Failure -> onEmailVerificationFailure(state.exception)
                    }
                }
            }
        }
    }

    private fun onEmailVerificationSuccess() {
        val navOptions: NavOptions = NavOptions.Builder()
            .setPopUpTo(R.id.register_email_verification_fragment, true)
            .build()

        val navController = NavHostFragment.findNavController(this)
        navController.navigate(R.id.register_name_fragment, null, navOptions)

        progressLoader.hide()
    }

    private fun onEmailVerificationFailure(error: AppException) {
        binding.tvError.text = error.message
        binding.tvError.visibility = View.VISIBLE
        progressLoader.hide()
    }

    private fun observeOtpSendState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.otpEvent.collect { state ->
                    when (state) {
                        is UiState.Loading -> progressLoader.show()
                        is UiState.Success -> onResendOtpSuccess()
                        is UiState.Failure -> onResendOtpFailure(state.exception)
                    }
                }
            }
        }
    }

    private fun onResendOtpSuccess() {
        startOtpResendCooldownTimer()
        progressLoader.hide()
    }

    private fun onResendOtpFailure(error: AppException) {
        binding.tvError.text = error.message
        binding.tvError.visibility = View.VISIBLE
        progressLoader.hide()
    }

    private fun styleSpamHintText() {
        val message =
            "Didn’t get the email? Make sure to also check your spam/junk folder if you can't find the email in your inbox"
        val highlightedPhrase = "check your spam/junk folder"

        // Find the phrase location inside the full text
        val startIndex = message.indexOf(highlightedPhrase)
        val endIndex = startIndex + highlightedPhrase.length

        val styledMessage = SpannableStringBuilder(message)
        styledMessage.setSpan(
            ForegroundColorSpan("#FFA500".toColorInt()),
            startIndex,
            endIndex,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.tvInfo.text = styledMessage
    }

    private fun startOtpResendCooldownTimer() {
        binding.btnResendOtp.isEnabled = false
        val originalText = binding.btnResendOtp.text

        otpResendCountdownTimer = object :
            CountDownTimer(
                OTP_RESEND_COOLDOWN_MS,
                COUNTDOWN_INTERVAL_MS
            ) {
            override fun onTick(millisUntilFinished: Long) {
                val countDown = "Resend in ${millisUntilFinished / 1000}"
                binding.btnResendOtp.text = countDown
            }

            override fun onFinish() {
                binding.btnResendOtp.text = originalText
                binding.btnResendOtp.isEnabled = true
            }
        }.start()
    }

    private fun isOtpInputComplete(): Boolean = binding.otpBox.length() == binding.otpBox.itemCount
    private fun getEnteredOtp(): String = binding.otpBox.text.toString()

    private fun addOtpInputTextWatcher() {
        binding.otpBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                text: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun afterTextChanged(text: Editable?) {}

            override fun onTextChanged(
                enteredOtp: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
                if (enteredOtp.toString().isEmpty()) binding.tvError.visibility = View.GONE
                binding.btnContinue.isEnabled = isOtpInputComplete()
            }
        })
    }
}