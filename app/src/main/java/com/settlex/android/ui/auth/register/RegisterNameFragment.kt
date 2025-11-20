package com.settlex.android.ui.auth.register

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import com.settlex.android.R
import com.settlex.android.databinding.FragmentRegisterNameBinding
import com.settlex.android.util.string.StringFormatter
import com.settlex.android.util.ui.StatusBar
import dagger.hilt.android.AndroidEntryPoint

/**
 * A [Fragment] that allows the user to enter their first and last name as part of the
 * registration process.
 *
 * This fragment is responsible for:
 * - Displaying input fields for the user's first and last name.
 * - Validating the entered names to ensure they meet the required format.
 * - Enabling the "Continue" button only when both names are valid.
 * - Updating the shared [RegisterViewModel] with the user's name upon continuing.
 * - Navigating to the next step in the registration flow.
 *
 * It uses Hilt for dependency injection.
 */
@AndroidEntryPoint
class RegisterNameFragment : Fragment() {
    private var binding: FragmentRegisterNameBinding? = null
    private val registerViewModel: RegisterViewModel by activityViewModels()

    companion object {
        private const val NAME_VALIDATION_REGEX = "^[a-zA-Z]{2,}(?:\\s[a-zA-Z]{2,})*$"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegisterNameBinding.inflate(inflater, container, false)

        setupUiActions()
        setupClickListeners()
        return binding!!.getRoot()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun setupUiActions() {
        StatusBar.setColor(requireActivity(), R.color.white)
        setupInputValidation()
        setupActionDoneOnLastnameEditText()
    }

    private fun setupClickListeners() {
        binding!!.btnBackBefore.setOnClickListener {
            NavHostFragment.findNavController(
                this
            ).popBackStack()
        }

        binding!!.btnHelp.setOnClickListener {
            StringFormatter.showNotImplementedToast(
                requireContext()
            )
        }

        binding!!.btnContinue.setOnClickListener { updateNameAndContinue() }
    }

    private fun setupInputValidation() {
        val validationWatcher: TextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateContinueButtonState()
            }
        }
        binding!!.etFirstname.addTextChangedListener(validationWatcher)
        binding!!.etLastname.addTextChangedListener(validationWatcher)
    }

    private fun updateNameAndContinue() {
        val firstName = binding!!.etFirstname.getText().toString().trim()
        val lastName = binding!!.etLastname.getText().toString().trim()

        registerViewModel.updateName(
            StringFormatter.capitalizeEachWord(firstName),
            StringFormatter.capitalizeEachWord(lastName)
        )

        val navController = NavHostFragment.findNavController(this)
        navController.navigate(R.id.register_password_fragment)
    }

    private fun updateContinueButtonState() {
        val isValidFirstName = isFirstNameValid()
        val isValidLastName = isLastNameValid()

        binding!!.btnContinue.isEnabled = isValidFirstName && isValidLastName
    }

    private fun isFirstNameValid(): Boolean {
        val firstName = binding!!.etFirstname.toString().trim()
        return firstName.isNotEmpty() && firstName.matches(NAME_VALIDATION_REGEX.toRegex())
    }

    private fun isLastNameValid(): Boolean {
        val lastName = binding!!.etLastname.toString().trim()
        return lastName.isNotEmpty() && lastName.matches(NAME_VALIDATION_REGEX.toRegex())
    }

    private fun setupActionDoneOnLastnameEditText() {
        binding!!.etLastname.setOnEditorActionListener { v: TextView, actionId: Int, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val imm = v.context
                    .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)
                v.clearFocus()
                return@setOnEditorActionListener true
            }
            false
        }
    }
}
