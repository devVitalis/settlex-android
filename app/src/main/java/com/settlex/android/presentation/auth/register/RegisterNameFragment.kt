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
import com.settlex.android.presentation.common.util.KeyboardHelper
import com.settlex.android.util.string.StringFormatter
import com.settlex.android.util.ui.StatusBar
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class RegisterNameFragment : Fragment() {
    private var binding: FragmentRegisterNameBinding? = null
    private val registerViewModel: RegisterViewModel by activityViewModels()
    private val keyboardHelper by lazy { KeyboardHelper(requireActivity()) }

    companion object {
        private const val NAME_VALIDATION_REGEX = "^[a-zA-Z]{2,}(?:\\s[a-zA-Z]{2,})*$"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegisterNameBinding.inflate(inflater, container, false)

        initViews()
        setupClickListeners()
        return binding!!.getRoot()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun initViews() = with(binding!!) {
        StatusBar.setColor(requireActivity(), R.color.white)
        setupInputValidation()
        keyboardHelper.attachDoneAction(etLastname)
    }

    private fun setupClickListeners() = with(binding!!) {
        btnBackBefore.setOnClickListener {
            NavHostFragment.findNavController(
                this@RegisterNameFragment
            ).popBackStack()
        }

        btnHelp.setOnClickListener {
            StringFormatter.showNotImplementedToast(
                requireContext()
            )
        }

        btnContinue.setOnClickListener { updateNameAndContinue() }
    }

    private fun setupInputValidation() = with(binding!!) {
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

    private fun updateNameAndContinue() = with(binding!!) {
        val firstName = etFirstname.getText().toString().trim()
        val lastName = etLastname.getText().toString().trim()

        registerViewModel.updateName(
            StringFormatter.capitalizeEachWord(firstName),
            StringFormatter.capitalizeEachWord(lastName)
        )

        val navController = NavHostFragment.findNavController(this@RegisterNameFragment)
        navController.navigate(R.id.register_password_fragment)
    }

    private fun updateContinueButtonState() = with(binding!!) {
        val isValidFirstName = isFirstNameValid()
        val isValidLastName = isLastNameValid()

        btnContinue.isEnabled = isValidFirstName && isValidLastName
    }

    private fun isFirstNameValid(): Boolean {
        val firstName = binding!!.etFirstname.toString().trim()
        return firstName.isNotEmpty() && firstName.matches(NAME_VALIDATION_REGEX.toRegex())
    }

    private fun isLastNameValid(): Boolean {
        val lastName = binding!!.etLastname.toString().trim()
        return lastName.isNotEmpty() && lastName.matches(NAME_VALIDATION_REGEX.toRegex())
    }
}
