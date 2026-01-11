package com.settlex.android.presentation.auth.register

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import com.settlex.android.R
import com.settlex.android.databinding.FragmentRegisterNameBinding
import com.settlex.android.presentation.common.extensions.capitalizeEachWord
import com.settlex.android.presentation.common.util.FocusManager
import com.settlex.android.util.ui.StatusBar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterNameFragment : Fragment() {
    private var _binding: FragmentRegisterNameBinding? = null
    private val binding get() = _binding!!
    private val registerViewModel: RegisterViewModel by activityViewModels()
    private val focusManager by lazy { FocusManager(requireActivity()) }

    companion object {
        private const val NAME_VALIDATION_REGEX = "^[a-zA-Z]{2,}(?:\\s[a-zA-Z]{2,})*$"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegisterNameBinding.inflate(inflater, container, false)

        initViews()
        setupClickListeners()
        return binding.getRoot()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initViews() = with(binding) {
        StatusBar.setColor(requireActivity(), R.color.colorBackground)
        setupInputValidation()
        focusManager.attachDoneAction(etLastname)
    }

    private fun setupClickListeners() = with(binding) {
        btnBackBefore.setOnClickListener {
            NavHostFragment.findNavController(
                this@RegisterNameFragment
            ).popBackStack()
        }
        btnContinue.setOnClickListener { updateNameAndContinue() }
    }

    private fun setupInputValidation() = with(binding) {
        val validationWatcher: TextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateContinueButtonState()
            }
        }
        etFirstname.addTextChangedListener(validationWatcher)
        etLastname.addTextChangedListener(validationWatcher)
    }

    private fun updateNameAndContinue() = with(binding) {
        val firstName = etFirstname.getText().toString().trim()
        val lastName = etLastname.getText().toString().trim()

        registerViewModel.updateName(
            firstName.capitalizeEachWord(),
            lastName.capitalizeEachWord()
        )

        val navController = NavHostFragment.findNavController(this@RegisterNameFragment)
        navController.navigate(R.id.register_password_fragment)
    }

    private fun updateContinueButtonState() = with(binding) {
        val isValidFirstName = isFirstNameValid()
        val isValidLastName = isLastNameValid()

        btnContinue.isEnabled = isValidFirstName && isValidLastName
    }

    private fun isFirstNameValid(): Boolean = with(binding) {
        val firstName = etFirstname.text.toString().trim()
        return firstName.isNotEmpty() && firstName.matches(NAME_VALIDATION_REGEX.toRegex())
    }

    private fun isLastNameValid(): Boolean = with(binding) {
        val lastName = etLastname.text.toString().trim()
        return lastName.isNotEmpty() && lastName.matches(NAME_VALIDATION_REGEX.toRegex())
    }
}
