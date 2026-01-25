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
import com.settlex.android.presentation.common.extensions.getThemeColor
import com.settlex.android.presentation.common.extensions.gone
import com.settlex.android.presentation.common.extensions.show
import com.settlex.android.presentation.common.state.UiState
import com.settlex.android.presentation.common.util.SpannableTextFormatter
import com.settlex.android.util.ui.ProgressDialogManager
import com.settlex.android.util.ui.StatusBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterEmailVerificationFragment : Fragment() {
    private var _binding: FragmentRegisterEmailVerificationBinding? = null
    private val binding get() = _binding!!
    private val progressLoader by lazy { ProgressDialogManager(requireActivity()) }
    private val authViewModel: AuthViewModel by activityViewModels()
    private val registerViewModel: RegisterViewModel by activityViewModels()
    private val email: String by lazy { registerViewModel.email }
    private var otpResendCountdownTimer: CountDownTimer? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
        StatusBar.setColor(requireActivity(), requireContext().getThemeColor(R.attr.colorSurface))
        setupInputWatcher()

        email.also { email ->
            tvUserEmail.text = SpannableTextFormatter(
                requireContext(),
                text = "We sent a verification code to your email $email. This code will expire after 10 minutes",
                target = email,
                colorRes = R.attr.colorOnSurface,
                setBold = true,
            )
        }

        tvSpamInfo.text = SpannableTextFormatter(
            requireContext(),
            "Didn’t get the email? Make sure to also check your spam/junk folder if you can't find the email in your inbox",
            "check your spam/junk folder",
            R.attr.colorOnWarningContainer
        )

        toolbar.setNavigationOnClickListener {
            NavHostFragment.findNavController(
                this@RegisterEmailVerificationFragment
            ).popBackStack()
        }

        tvResendCode.setOnClickListener {
            tvError.gone()
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

    private fun onEmailVerificationFailure(error: AppException) = with(binding) {
        tvError.text = error.message
        tvError.show()

        progressLoader.hide()
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

    private fun startOtpResendCooldownTimer() = with(binding) {
        tvResendCode.isEnabled = false
        tvResendCode.setTextColor(requireContext().getThemeColor(R.attr.colorOnSurfaceVariant))

        tvResendCode.text.also { originalText ->
            otpResendCountdownTimer = object :
                CountDownTimer(
                    OTP_RESEND_COOLDOWN_MS,
                    COUNTDOWN_INTERVAL_MS
                ) {
                override fun onTick(millisUntilFinished: Long) {
                    val countDownTimer = (millisUntilFinished / 1000).toInt()
                    val countDownText = "Resend in $countDownTimer sec(s)"

                    if (countDownTimer > 0) tvResendCode.text = countDownText
                }

                override fun onFinish() {
                    tvResendCode.text = originalText
                    tvResendCode.isEnabled = true
                    tvResendCode.setTextColor(requireContext().getThemeColor(R.attr.colorPrimary))
                }
            }.start()
        }
    }

    private fun isOtpComplete(): Boolean = binding.etOtpCode.length() == 6
    private fun getEnteredOtp(): String = binding.etOtpCode.text.toString()

    private fun setupInputWatcher() {
        with(binding) {
            etOtpCode.doOnTextChanged { _, _, _, _ ->
                tvError.gone()
                btnContinue.isEnabled = isOtpComplete()
            }
        }
    }

    companion object {
        private const val OTP_RESEND_COOLDOWN_MS = 60000L
        private const val COUNTDOWN_INTERVAL_MS = 1000L
    }
}