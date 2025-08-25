package com.settlex.android.ui.auth.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.settlex.android.R;
import com.settlex.android.databinding.FragmentSignupUserInfoBinding;
import com.settlex.android.ui.info.help.AuthHelpActivity;
import com.settlex.android.ui.auth.viewmodel.AuthViewModel;
import com.settlex.android.util.string.StringUtil;

import java.util.Objects;

public class SignupUserInfoFragment extends Fragment {

    private FragmentSignupUserInfoBinding binding;
    private AuthViewModel authViewModel;

    // ====================== LIFECYCLE ======================
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSignupUserInfoBinding.inflate(inflater, container, false);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        setupStatusBar();
        setupUiActions();

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }

    // ====================== UI SETUP ======================
    private void setupUiActions() {
        reEnableEditTextFocus();
        setupInputValidation();
        clearFocusAndHideKeyboardOnOutsideTap(binding.getRoot());

        binding.imgBackBefore.setOnClickListener(v -> navigateBack());
        binding.btnHelp.setOnClickListener(v -> navigateToHelpActivity());
        binding.btnContinue.setOnClickListener(v -> validateUserInfoAndProceed());
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
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateContinueButtonState();
            }
        };

        binding.editTxtFirstName.addTextChangedListener(validationWatcher);
        binding.editTxtLastName.addTextChangedListener(validationWatcher);
    }

    // ====================== BUSINESS LOGIC ======================

    /**
     * Validates user info, updates ViewModel, and navigates to password setup.
     */
    private void validateUserInfoAndProceed() {
        String firstName = StringUtil.capitalizeEachWord(
                Objects.requireNonNull(binding.editTxtFirstName.getText()).toString().trim()
        );
        String lastName = StringUtil.capitalizeEachWord(
                Objects.requireNonNull(binding.editTxtLastName.getText()).toString().trim()
        );

        authViewModel.updateFirstName(firstName);
        authViewModel.updateLastName(lastName);

        navigateToFragment(new SignUpUserPasswordFragment());
    }

    /**
     * Enables/disables Continue button based on name validation.
     */
    private void updateContinueButtonState() {
        String firstName = Objects.requireNonNull(binding.editTxtFirstName.getText()).toString().trim();
        String lastName = Objects.requireNonNull(binding.editTxtLastName.getText()).toString().trim();

        boolean isValidFirstName = !firstName.isEmpty() && firstName.matches("^[a-zA-Z]{2,}(?:\\s[a-zA-Z]{2,})*$");
        boolean isValidLastName = !lastName.isEmpty() && lastName.matches("^[a-zA-Z]{2,}(?:\\s[a-zA-Z]{2,})*$");

        binding.btnContinue.setEnabled(isValidFirstName && isValidLastName);
    }

    // ====================== NAVIGATION ======================
    private void navigateToFragment(Fragment fragment) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void navigateBack() {
        requireActivity().getSupportFragmentManager().popBackStack();
    }

    private void navigateToHelpActivity() {
        startActivity(new Intent(requireActivity(), AuthHelpActivity.class));
    }

    // ====================== KEYBOARD HANDLING ======================
    @SuppressLint("ClickableViewAccessibility")
    private void clearFocusAndHideKeyboardOnOutsideTap(View root) {
        if (!(root instanceof EditText)) {
            root.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    hideKeyboard();
                }
                return false;
            });
        }

        if (root instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) root).getChildCount(); i++) {
                clearFocusAndHideKeyboardOnOutsideTap(((ViewGroup) root).getChildAt(i));
            }
        }
    }

    private void hideKeyboard() {
        View focusedView = requireActivity().getCurrentFocus();
        if (focusedView != null) {
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
            focusedView.clearFocus();
        }
    }

    private void setupStatusBar() {
        Window window = requireActivity().getWindow();
        window.setStatusBarColor(ContextCompat.getColor(requireContext(), R.color.white));
        View decorView = window.getDecorView();
        decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }
}
