package com.settlex.android.presentation.auth.register

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.ImageSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import com.settlex.android.R
import com.settlex.android.data.exception.AppException
import com.settlex.android.databinding.FragmentRegisterPasswordBinding
import com.settlex.android.presentation.auth.AuthViewModel
import com.settlex.android.presentation.auth.login.LoginActivity
import com.settlex.android.presentation.common.extensions.getThemeColor
import com.settlex.android.presentation.common.extensions.gone
import com.settlex.android.presentation.common.extensions.show
import com.settlex.android.presentation.common.state.UiState
import com.settlex.android.presentation.common.util.DialogHelper
import com.settlex.android.presentation.common.util.FocusManager
import com.settlex.android.presentation.common.util.ValidationUtil
import com.settlex.android.presentation.dashboard.DashboardActivity
import com.settlex.android.util.ui.ProgressDialogManager
import com.settlex.android.util.ui.StatusBar
import kotlinx.coroutines.launch

class RegisterPasswordFragment : Fragment() {

    private var binding: FragmentRegisterPasswordBinding? = null
    private val authViewModel: AuthViewModel by activityViewModels()
    private val registerViewModel: RegisterViewModel by activityViewModels()
    private val focusManager by lazy { FocusManager(requireActivity()) }
    private val progressLoader by lazy { ProgressDialogManager(requireActivity()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterPasswordBinding.inflate(inflater, container, false)

        initViews()
        setupListeners()

        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObservers()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun initViews() {
        StatusBar.setColor(requireActivity(), requireContext().getThemeColor(R.attr.colorSurface))
        setupInputValidation()
        togglePasswordVisibilityIcons(false)
        focusManager.attachDoneAction(binding!!.etInvitationCode)
    }

    private fun setupListeners() = with(binding!!) {
        btnExpendInvitationCode.setOnClickListener { toggleReferralCodeVisibility() }
        btnCreateAccount.setOnClickListener { validateAndCreateAccount() }

        toolbar.setNavigationOnClickListener {
            NavHostFragment.findNavController(this@RegisterPasswordFragment).popBackStack()
        }
    }

    private fun initObservers() {
        observeRegistrationState()
    }

    private fun observeRegistrationState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.registrationEvent.collect { state ->
                    when (state) {
                        is UiState.Loading -> progressLoader.show()
                        is UiState.Success -> onRegistrationSuccess()
                        is UiState.Failure -> onRegistrationFailure(state.exception)
                    }
                }
            }
        }
    }

    private fun onRegistrationSuccess() {
        startActivity(Intent(requireContext(), DashboardActivity::class.java))
        requireActivity().finishAffinity()

        progressLoader.hide()
    }

    private fun onRegistrationFailure(error: AppException) {
        with(binding!!) {
            when (error) {
                is AppException.AuthException -> showLoginRedirectDialog(error.message)
                else -> {
                    tvError.text = error.message
                    tvError.show()
                }
            }

            progressLoader.hide()
        }
    }

    private fun showLoginRedirectDialog(message: String) {
        DialogHelper.showCustomAlertDialog(
            requireContext()
        ) { dialog, binding ->
            with(binding) {
                tvMessage.text = message
                "Sign In".also { btnPrimary.text = it }

                btnSecondary.gone()
                btnPrimary.setOnClickListener {
                    startActivity(
                        Intent(
                            requireContext(),
                            LoginActivity::class.java
                        )
                    )
                    requireActivity().finishAffinity()
                    dialog.dismiss()
                }
            }
        }
    }

    private fun validateAndCreateAccount() {
        with(binding!!) {
            val password: String = etPassword.text.toString().trim()
            val invitationCode: String = etInvitationCode.text.toString().trim()

            registerViewModel.updateReferralCode(invitationCode)
            val user = registerViewModel.buildUserModel(uid = "")
            authViewModel.register(user, password)
        }
    }

    private fun updateCreateAccountButtonState() = with(binding!!) {
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        val allValid = validatePasswordRequirements(password, confirmPassword)
        setCreateAccountBtnEnabled(allValid)
    }

    private fun setCreateAccountBtnEnabled(isPasswordValid: Boolean) {
        binding!!.btnCreateAccount.isEnabled = isPasswordValid
    }

    companion object {
        private const val LENGTH = 8
        private const val HAS_ALLOWED_SPECIAL_CHARS = ValidationUtil.ALLOWED_SPECIAL_CHARS
        private const val ERROR_PASSWORD_MISMATCH = ValidationUtil.ERROR_PASSWORD_MISMATCH
    }

    private fun validatePasswordRequirements(password: String, confirm: String): Boolean {
        with(binding!!) {
            val hasLength = password.length >= LENGTH
            val hasUpper = password.any { it.isUpperCase() }
            val hasLower = password.any { it.isLowerCase() }
            val hasSpecial = password.any { HAS_ALLOWED_SPECIAL_CHARS.contains(it) }
            val matches = password == confirm

            when (confirm.isNotEmpty() && !matches) {
                true -> {
                    tvError.text = ERROR_PASSWORD_MISMATCH
                    tvError.show()
                }

                false -> tvError.gone()
            }

            showPasswordRequirements(hasLength, hasUpper, hasLower, hasSpecial, password)
            return hasLength && hasUpper && hasLower && hasSpecial && matches
        }
    }

    private fun showPasswordRequirements(
        hasLength: Boolean,
        hasUpper: Boolean,
        hasLower: Boolean,
        hasSpecial: Boolean,
        password: String
    ) {
        val requirements = SpannableStringBuilder()
        appendRequirement(requirements, hasLength, "At least 8 characters")
        appendRequirement(requirements, hasUpper, "Contains uppercase letter")
        appendRequirement(requirements, hasLower, "Contains lowercase letter")
        appendRequirement(
            requirements,
            hasSpecial,
            "Contains special character (e.g. !@#$%^&*()_+-=[]{};:,.?)"
        )

        with(binding!!) {
            val showPasswordPrompt =
                password.isEmpty() || (hasLength && hasUpper && hasLower && hasSpecial)
            tvPasswordPrompt.visibility = if (!showPasswordPrompt) View.VISIBLE else View.GONE
            tvPasswordPrompt.text = requirements
        }
    }

    private fun appendRequirement(builder: SpannableStringBuilder, isMet: Boolean, text: String?) {
        val icon = ContextCompat.getDrawable(
            requireContext(),
            if (isMet) R.drawable.ic_checkbox_checked else R.drawable.ic_checkbox_unchecked
        )!!

        val size = (binding!!.tvPasswordPrompt.textSize * 1.2f).toInt()
        icon.setBounds(0, 0, size, size)
        builder.append(" ")
        builder.setSpan(
            ImageSpan(icon, ImageSpan.ALIGN_BOTTOM),
            builder.length - 1,
            builder.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        builder.append(" ").append(text).append("\n")
    }

    private fun setupInputValidation() {
        val passwordWatcher: TextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                updateCreateAccountButtonState()
                togglePasswordVisibilityIcons(s.isNotEmpty())
            }
        }
        with(binding!!) {
            etPassword.addTextChangedListener(passwordWatcher)
            etConfirmPassword.addTextChangedListener(passwordWatcher)
        }
    }

    private fun togglePasswordVisibilityIcons(show: Boolean) {
        with(binding!!) {
            tilPassword.isEndIconVisible = show
            tilConfirmPassword.isEndIconVisible = show
        }
    }

    private fun toggleReferralCodeVisibility() = with(binding!!) {
        val iconExpendLess = R.drawable.ic_expend_less
        val iconExpendMore = R.drawable.ic_expend_more
        val isVisible = etInvitationCode.isVisible

        when (isVisible) {
            true -> {
                etInvitationCode.gone()
                btnExpendInvitationCode.setImageResource(iconExpendLess)
            }

            false -> {
                etInvitationCode.show()
                btnExpendInvitationCode.setImageResource(iconExpendMore)
            }
        }
    }
}

