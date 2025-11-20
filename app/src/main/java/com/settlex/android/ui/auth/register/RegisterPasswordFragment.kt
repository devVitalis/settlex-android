package com.settlex.android.ui.auth.register

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
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
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
import com.settlex.android.ui.auth.AuthViewModel
import com.settlex.android.ui.common.event.UiState
import com.settlex.android.ui.dashboard.DashboardActivity
import com.settlex.android.util.ui.ProgressLoaderController
import com.settlex.android.util.ui.StatusBar
import kotlinx.coroutines.launch

class RegisterPasswordFragment : Fragment() {
    private val progressLoader: ProgressLoaderController by lazy {
        ProgressLoaderController(
            requireActivity()
        )
    }
    private var binding: FragmentRegisterPasswordBinding? = null
    private val authViewModel: AuthViewModel by activityViewModels()
    private val registerViewModel: RegisterViewModel by activityViewModels()

    companion object {
        private const val ERROR_PASSWORD_MISMATCH = "Passwords do not match!"
        private const val LENGTH = 8
        private const val HAS_UPPER_REGEX = ".*[A-Z].*"
        private const val HAS_LOWER_REGEX = ".*[a-z].*"
        private const val HAS_SPECIAL_CHAR_REGEX = ".*[@#$%^&+=!.].*"

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegisterPasswordBinding.inflate(inflater, container, false)

        setupUiActions()
        setupClickListeners()

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

    private fun setupUiActions() {
        StatusBar.setColor(requireActivity(), R.color.white)
        setupInputValidation()
        togglePasswordVisibilityIcons(false)
        setupActionDoneOnInvitationCodeEditText()
    }

    private fun setupClickListeners() {
        binding!!.btnExpend.setOnClickListener { toggleReferralCodeVisibility() }
        binding!!.btnCreateAccount.setOnClickListener { validateAndCreateAccount() }

        binding!!.btnBackBefore.setOnClickListener {
            NavHostFragment.findNavController(this).popBackStack()
        }

        binding!!.btnHelp.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "Feature not yet implementation",
                Toast.LENGTH_SHORT
            ).show()
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
        binding!!.tvError.text = error.message
        binding!!.tvError.visibility = View.VISIBLE

        progressLoader.hide()
    }

    private fun validateAndCreateAccount() {
        val password: String = binding!!.etPassword.text.toString().trim()
        val invitationCode: String = binding!!.etInvitationCode.text.toString().trim()

        registerViewModel.updateReferralCode(invitationCode)
        val user = registerViewModel.buildUserModel(uid = "")
        authViewModel.register(user, password)
    }

    private fun validateRequirements() {
        val password = binding!!.etPassword.text.toString().trim()
        val confirmPassword = binding!!.etConfirmPassword.text.toString().trim()

        val allValid = validatePasswordRequirements(password, confirmPassword)
        setCreateAccountBtnEnabled(allValid)
    }

    private fun setCreateAccountBtnEnabled(isPasswordValid: Boolean) {
        binding!!.btnCreateAccount.isEnabled = isPasswordValid
    }

    private fun validatePasswordRequirements(password: String, confirm: String): Boolean {
        val hasLength = password.length >= LENGTH
        val hasUpper = password.matches(HAS_UPPER_REGEX.toRegex())
        val hasLower = password.matches(HAS_LOWER_REGEX.toRegex())
        val hasSpecial = password.matches(HAS_SPECIAL_CHAR_REGEX.toRegex())
        val matches = password == confirm

        if (confirm.isNotEmpty() && !matches) {
            binding!!.tvError.text = ERROR_PASSWORD_MISMATCH
            binding!!.tvError.visibility = View.VISIBLE
        } else {
            binding!!.tvError.visibility = View.GONE
        }

        showPasswordRequirements(hasLength, hasUpper, hasLower, hasSpecial, password)
        return hasLength && hasUpper && hasLower && hasSpecial && matches
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
        appendRequirement(requirements, hasSpecial, "Contains special character (e.g. @#$%^&;+=!.)")

        val showPasswordPrompt =
            password.isEmpty() || (hasLength && hasUpper && hasLower && hasSpecial)
        binding!!.tvPasswordPrompt.visibility = if (!showPasswordPrompt) View.VISIBLE else View.GONE
        binding!!.tvPasswordPrompt.text = requirements
    }

    private fun appendRequirement(builder: SpannableStringBuilder, isMet: Boolean, text: String?) {
        val icon = ContextCompat.getDrawable(
            requireContext(),
            if (isMet) R.drawable.ic_checkbox_checked else R.drawable.ic_checkbox_unchecked
        )

        val size = (binding!!.tvPasswordPrompt.textSize * 1.2f).toInt()
        icon!!.setBounds(0, 0, size, size)
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
                validateRequirements()
                togglePasswordVisibilityIcons(s.isNotEmpty())
            }
        }
        binding!!.etPassword.addTextChangedListener(passwordWatcher)
        binding!!.etConfirmPassword.addTextChangedListener(passwordWatcher)
    }

    private fun togglePasswordVisibilityIcons(show: Boolean) {
        binding!!.tilPassword.isEndIconVisible = show
        binding!!.tilConfirmPassword.isEndIconVisible = show
    }

    private fun toggleReferralCodeVisibility() {
        val isVisible = binding!!.etInvitationCode.isVisible
        binding!!.etInvitationCode.visibility = if (isVisible) View.GONE else View.VISIBLE
        binding!!.btnExpend.setImageResource(if (isVisible) R.drawable.ic_expend_less else R.drawable.ic_expend_more)
    }

    private fun setupActionDoneOnInvitationCodeEditText() {
        binding!!.etInvitationCode.setOnEditorActionListener { v: TextView, actionId: Int, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val imm = v.context.getSystemService(InputMethodManager::class.java)
                imm.hideSoftInputFromWindow(v.windowToken, 0)

                v.clearFocus()
                return@setOnEditorActionListener true
            }
            false
        }
    }
}
