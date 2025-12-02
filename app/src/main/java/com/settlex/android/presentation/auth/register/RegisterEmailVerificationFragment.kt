package com.settlex.android.presentation.auth.register

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
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
import com.settlex.android.presentation.common.extensions.gone
import com.settlex.android.presentation.common.extensions.show
import com.settlex.android.presentation.common.state.UiState
import com.settlex.android.presentation.common.util.SpannableTextFormatter
import com.settlex.android.util.string.StringFormatter
import com.settlex.android.util.ui.ProgressDialogManager
import com.settlex.android.util.ui.StatusBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class RegisterEmailVerificationFragment : Fragment() {

    private var _binding: FragmentRegisterEmailVerificationBinding? = null
    private val binding = _binding!!
    private val progressLoader: ProgressDialogManager by lazy {
        ProgressDialogManager(
            requireActivity()
        )
    }
    private val authViewModel: AuthViewModel by activityViewModels()
    private val registerViewModel: RegisterViewModel by activityViewModels()

    private val email: String by lazy { registerViewModel.email }
    private var otpResendCountdownTimer: CountDownTimer? = null

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

        initViews()
        initObservers()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startOtpResendCooldownTimer()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        otpResendCountdownTimer?.cancel()
        _binding = null
    }

    private fun initObservers() {
        observeOtpRequestState()
        observeEmailVerificationStatus()
    }

    private fun initViews() = with(binding) {
        StatusBar.setColor(requireActivity(), R.color.white)
        setupInputWatcher()

        tvUserEmail.text = StringFormatter.maskEmail(email)

        tvSpamInfo.text = SpannableTextFormatter.format(
            "Didn’t get the email? Make sure to also check your spam/junk folder if you can't find the email in your inbox",
            "check your spam/junk folder",
            " #FFA500"
        )

        btnBackBefore.setOnClickListener {
            NavHostFragment.findNavController(
                this@RegisterEmailVerificationFragment
            ).popBackStack()
        }

        btnHelp.setOnClickListener {
            StringFormatter.showNotImplementedToast(
                requireContext()
            )
        }

        btnResendOtp.setOnClickListener {
            authViewModel.sendVerificationCode(
                email,
                OtpType.EMAIL_VERIFICATION
            )
        }

        btnContinue.setOnClickListener {
            authViewModel.verifyEmail(
                email,
                getEnteredOtp()
            )
        }
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
        with(binding) {
            tvError.text = error.message
            tvError.show()

            progressLoader.hide()
        }
    }

    private fun observeOtpRequestState() {
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
        with(binding) {
            tvError.text = error.message
            tvError.show()

            progressLoader.hide()
        }
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
                val countDown = "Resend in ${millisUntilFinished / 1000} seconds"
                binding.btnResendOtp.text = countDown
            }

            override fun onFinish() {
                binding.btnResendOtp.text = originalText
                binding.btnResendOtp.isEnabled = true
            }
        }.start()
    }

    companion object {
        private const val OTP_RESEND_COOLDOWN_MS = 60000L
        private const val COUNTDOWN_INTERVAL_MS = 1000L
    }

    private fun isOtpInputComplete(): Boolean =
        binding.otpView.length() == binding.otpView.itemCount

    private fun getEnteredOtp(): String = binding.otpView.text.toString()

    private fun setupInputWatcher() {
        with(binding) {
            otpView.doOnTextChanged { otp, _, _, _ ->
                if (otp.toString().isEmpty()) binding.tvError.gone()

                binding.btnContinue.isEnabled = isOtpInputComplete()
            }
        }
    }
}