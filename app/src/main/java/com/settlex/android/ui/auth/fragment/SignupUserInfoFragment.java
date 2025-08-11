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
import com.settlex.android.ui.activities.help.AuthHelpActivity;
import com.settlex.android.ui.auth.viewmodel.AuthViewModel;
import com.settlex.android.util.StringUtil;

import java.util.Objects;

public class SignupUserInfoFragment extends Fragment {
    private FragmentSignupUserInfoBinding binding;
    private AuthViewModel authViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSignupUserInfoBinding.inflate(inflater, container, false);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        configureStatusBar();
        initializeUiComponents();
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }

    private void initializeUiComponents() {
        setupFocusHandling();
        setupInputValidation();
        configureTouchToHideKeyboard(binding.getRoot());

        binding.imgBackBefore.setOnClickListener(v -> navigateBack());
        binding.btnHelp.setOnClickListener(v -> launchHelpActivity());
        binding.btnContinue.setOnClickListener(v -> validateUserInfoAndNext());
    }

    /**==============================================================
     Validates and saves user information before proceeding:
        - Capitalizes first and last names
        - Updates ViewModel with formatted names
        - Navigates to password fragment
     =============================================================*/
    private void validateUserInfoAndNext() {
        String firstName = StringUtil.capitalizeEachWord(Objects.requireNonNull(binding.editTxtFirstName.getText()).toString().trim());
        String lastName = StringUtil.capitalizeEachWord(Objects.requireNonNull(binding.editTxtLastName.getText()).toString().trim());

        authViewModel.updateFirstName(firstName);
        authViewModel.updateLastName(lastName);

        navigateToFragment(new SignUpUserPasswordFragment());
    }

    /**=============================================================
     Updates continue button state based on:
        - Non-empty first and last names
        - Valid name format (letters only, minimum 2 characters)
     =============================================================*/
    private void updateContinueButtonState() {
        String firstName = Objects.requireNonNull(binding.editTxtFirstName.getText()).toString().trim();
        String lastName = Objects.requireNonNull(binding.editTxtLastName.getText()).toString().trim();

        boolean isValidFirstName = !firstName.isEmpty() && firstName.matches("^[a-zA-Z]{2,}(?:\\s[a-zA-Z]{2,})*$");
        boolean isValidLastName = !lastName.isEmpty() && lastName.matches("^[a-zA-Z]{2,}(?:\\s[a-zA-Z]{2,})*$");

        binding.btnContinue.setEnabled(isValidFirstName && isValidLastName);
    }

    private void setupFocusHandling() {
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
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateContinueButtonState();
            }
        };

        binding.editTxtFirstName.addTextChangedListener(validationWatcher);
        binding.editTxtLastName.addTextChangedListener(validationWatcher);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void configureTouchToHideKeyboard(View root) {
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
                configureTouchToHideKeyboard(((ViewGroup) root).getChildAt(i));
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

    private void launchHelpActivity() {
        startActivity(new Intent(requireActivity(), AuthHelpActivity.class));
    }

    private void configureStatusBar() {
        Window window = requireActivity().getWindow();
        window.setStatusBarColor(ContextCompat.getColor(requireContext(), R.color.white));
        View decorView = window.getDecorView();
        decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }
}