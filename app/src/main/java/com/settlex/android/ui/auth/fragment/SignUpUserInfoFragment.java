package com.settlex.android.ui.auth.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.settlex.android.R;
import com.settlex.android.databinding.FragmentSignUpUserInfoBinding;
import com.settlex.android.ui.auth.viewmodel.AuthViewModel;
import com.settlex.android.ui.info.help.AuthHelpActivity;
import com.settlex.android.util.string.StringUtil;
import com.settlex.android.util.ui.StatusBarUtil;

import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SignUpUserInfoFragment extends Fragment {

    private FragmentSignUpUserInfoBinding binding;
    private AuthViewModel authViewModel;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSignUpUserInfoBinding.inflate(inflater, container, false);

        setupUiActions();
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // UI SETUP ==========
    private void setupUiActions() {
        StatusBarUtil.setStatusBarColor(requireActivity(), R.color.white);
        reEnableEditTextFocus();
        setupInputValidation();
        clearFocusOnLastEditTextField();

        binding.btnBackBefore.setOnClickListener(v -> navigateBack());
        binding.btnHelp.setOnClickListener(v -> navigateToHelpActivity());
        binding.btnContinue.setOnClickListener(v -> submitUserInfoAndProceed());
    }

    private void navigateBack() {
        NavHostFragment.findNavController(this).popBackStack();
    }

    private void navigateToHelpActivity() {
        startActivity(new Intent(requireActivity(), AuthHelpActivity.class));
    }

    private void reEnableEditTextFocus() {
        View.OnClickListener focusListener = v -> {
            if (v instanceof EditText) {
                v.setFocusable(true);
                v.setFocusableInTouchMode(true);
                v.requestFocus();
            }
        };

        binding.editTxtFirstName.setOnClickListener(focusListener);
        binding.editTxtLastName.setOnClickListener(focusListener);
    }

    private void setupInputValidation() {
        TextWatcher validationWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateContinueButtonState();
            }
        };

        binding.editTxtFirstName.addTextChangedListener(validationWatcher);
        binding.editTxtLastName.addTextChangedListener(validationWatcher);
    }

    private void submitUserInfoAndProceed() {
        String firstName = StringUtil.capitalizeEachWord(Objects.requireNonNull(binding.editTxtFirstName.getText()).toString().trim());
        String lastName = StringUtil.capitalizeEachWord(Objects.requireNonNull(binding.editTxtLastName.getText()).toString().trim());

        authViewModel.updateFirstName(firstName);
        authViewModel.updateLastName(lastName);

        navigateToPasswordSetupFragment();
    }

    private void navigateToPasswordSetupFragment() {
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(R.id.signUpUserPasswordFragment);
    }

    private void updateContinueButtonState() {
        String firstName = Objects.requireNonNull(binding.editTxtFirstName.getText()).toString().trim();
        String lastName = Objects.requireNonNull(binding.editTxtLastName.getText()).toString().trim();

        boolean isValidFirstName = !firstName.isEmpty() && firstName.matches("^[a-zA-Z]{2,}(?:\\s[a-zA-Z]{2,})*$");
        boolean isValidLastName = !lastName.isEmpty() && lastName.matches("^[a-zA-Z]{2,}(?:\\s[a-zA-Z]{2,})*$");

        binding.btnContinue.setEnabled(isValidFirstName && isValidLastName);
    }

    private void clearFocusOnLastEditTextField() {
        binding.editTxtLastName.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // Hide the keyboard
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                // Clear focus
                v.clearFocus();
                return true;
            }
            return false;
        });
    }
}
