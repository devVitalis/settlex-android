package com.settlex.android.ui.auth.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.settlex.android.R;
import com.settlex.android.data.model.UserModel;
import com.settlex.android.databinding.FragmentSignUpUserPasswordBinding;
import com.settlex.android.ui.activities.help.AuthHelpActivity;
import com.settlex.android.ui.auth.viewmodel.AuthViewModel;
import com.settlex.android.ui.common.SettleXProgressBarController;
import com.settlex.android.ui.dashboard.DashboardActivity;

import java.util.Objects;

public class SignUpUserPasswordFragment extends Fragment {
    private FragmentSignUpUserPasswordBinding binding;
    private SettleXProgressBarController progressController;
    private AuthViewModel authViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSignUpUserPasswordBinding.inflate(inflater, container, false);

        progressController = new SettleXProgressBarController(binding.fragmentContainer);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        setupStatusBar();
        setupUiActions();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRegistrationObserver();
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }

    /**
     * Observes the user registration process.
     */
    private void setupRegistrationObserver() {
        authViewModel.getRegisterResult().observe(getViewLifecycleOwner(), result -> {
            if (result == null) return;

            switch (result.getStatus()) {
                case LOADING -> progressController.show();
                case SUCCESS -> {
                    navigateToDashboard();
                    progressController.hide();
                }
                case ERROR -> {
                    showRegistrationError(result.getMessage());
                    progressController.hide();
                }
            }
        });
    }

    private void showRegistrationError(String message) {
        binding.txtErrorFeedback.setText(message);
        binding.txtErrorFeedback.setVisibility(View.VISIBLE);
    }

    private void setupUiActions() {
        setupPasswordValidation();
        reEnableEditTextFocus();
        clearFocusAndHideKeyboardOnOutsideTap(binding.getRoot());
        togglePasswordVisibilityIcons(false);

        binding.imgBackBefore.setOnClickListener(v -> navigateBack());
        binding.btnHelp.setOnClickListener(v -> launchHelpActivity());
        binding.icExpendLess.setOnClickListener(v -> toggleReferralCodeVisibility());
        binding.btnCreateAccount.setOnClickListener(v -> validateAndCreateAccount());
    }

    /**
     * Validates inputs and initiates account creation.
     */
    private void validateAndCreateAccount() {
        String password = Objects.requireNonNull(binding.editTxtPassword.getText()).toString().trim();
        String invitationCode = Objects.requireNonNull(binding.editTxtInvitationCode.getText()).toString().trim();

        authViewModel.applyDefaultUserValues(invitationCode);
        UserModel user = authViewModel.getUser().getValue();
        authViewModel.registerUser(authViewModel.getEmail(), password, user);
    }

    /**
     * Validates the password against a set of rules.
     */
    private void validatePassword() {
        String password = Objects.requireNonNull(binding.editTxtPassword.getText()).toString().trim();
        String confirmPassword = Objects.requireNonNull(binding.editTxtConfirmPassword.getText()).toString().trim();

        boolean hasLength = password.length() >= 8;
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasSpecial = password.matches(".*[@#$%^&+=!.].*");
        boolean matches = password.equals(confirmPassword);

        if (!confirmPassword.isEmpty() && !matches) {
            binding.txtErrorFeedback.setText(getString(R.string.error_password_mismatch));
            binding.txtErrorFeedback.setVisibility(View.VISIBLE);
        } else {
            binding.txtErrorFeedback.setVisibility(View.GONE);
        }

        updateCreateAccountButtonState(hasLength, hasUpper, hasLower, hasSpecial, matches);
        showPasswordRequirements(hasLength, hasUpper, hasLower, hasSpecial, password);
    }

    private void updateCreateAccountButtonState(boolean hasLength, boolean hasUpper, boolean hasLower, boolean hasSpecial, boolean matches) {
        binding.btnCreateAccount.setEnabled(hasLength && hasUpper && hasLower && hasSpecial && matches);
    }

    private void showPasswordRequirements(boolean hasLength, boolean hasUpper, boolean hasLower, boolean hasSpecial, String password) {
        SpannableStringBuilder requirements = new SpannableStringBuilder();
        appendRequirement(requirements, hasLength, "At least 8 characters");
        appendRequirement(requirements, hasUpper, "Contains uppercase letter");
        appendRequirement(requirements, hasLower, "Contains lowercase letter");
        appendRequirement(requirements, hasSpecial, "Contains special character (e.g. @#$%^&;+=!.)");

        boolean shouldShowPasswordPrompt = password.isEmpty() || (hasLength && hasUpper && hasLower && hasSpecial);

        binding.txtPasswordPrompt.setVisibility((!shouldShowPasswordPrompt) ? View.VISIBLE : View.GONE);
        binding.txtPasswordPrompt.setText(requirements);
    }

    /**
     * Appends a password requirement with a corresponding icon.
     */
    private void appendRequirement(SpannableStringBuilder builder, boolean isMet, String text) {
        Drawable icon = ContextCompat.getDrawable(requireContext(), isMet ? R.drawable.ic_checkbox_checked : R.drawable.ic_checkbox_unchecked);
        if (icon != null) {
            int size = (int) (binding.txtPasswordPrompt.getTextSize() * 1.2f);
            icon.setBounds(0, 0, size, size);
            builder.append(" ");
            builder.setSpan(new ImageSpan(icon, ImageSpan.ALIGN_BOTTOM),
                    builder.length() - 1, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.append(" ").append(text).append("\n");
        }
    }

    /**
     * Sets up a TextWatcher for password validation.
     */
    private void setupPasswordValidation() {
        TextWatcher passwordWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePassword();
                togglePasswordVisibilityIcons(!TextUtils.isEmpty(s));
            }
        };

        binding.editTxtPassword.addTextChangedListener(passwordWatcher);
        binding.editTxtConfirmPassword.addTextChangedListener(passwordWatcher);
    }

    /**
     * Shows or hides the password visibility toggle icons.
     */
    private void togglePasswordVisibilityIcons(boolean show) {
        binding.txtInputLayoutPassword.setEndIconVisible(show);
        binding.txtInputLayoutConfirmPassword.setEndIconVisible(show);
    }

    private void reEnableEditTextFocus() {
        View.OnClickListener focusListener = v -> {
            if (v instanceof EditText) {
                v.setFocusable(true);
                v.setFocusableInTouchMode(true);
                v.requestFocus();
            }
        };

        binding.editTxtPassword.setOnClickListener(focusListener);
        binding.editTxtConfirmPassword.setOnClickListener(focusListener);
        binding.editTxtInvitationCode.setOnClickListener(focusListener);
    }

    /**
     * Toggles the visibility of the referral code input field.
     */
    private void toggleReferralCodeVisibility() {
        boolean isVisible = binding.editTxtInvitationCode.getVisibility() == View.VISIBLE;
        binding.editTxtInvitationCode.setVisibility(isVisible ? View.GONE : View.VISIBLE);
        binding.icExpendLess.setImageResource(isVisible ? R.drawable.ic_expend_less : R.drawable.ic_expend_more);
    }

    private void navigateToDashboard() {
        Intent intent = new Intent(requireContext(), DashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private void navigateBack() {
        requireActivity().getSupportFragmentManager().popBackStack();
    }

    private void launchHelpActivity() {
        startActivity(new Intent(requireActivity(), AuthHelpActivity.class));
    }

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