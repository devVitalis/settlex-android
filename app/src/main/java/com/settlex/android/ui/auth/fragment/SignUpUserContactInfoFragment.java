package com.settlex.android.ui.auth.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Patterns;
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
import com.settlex.android.databinding.FragmentSignUpUserContactInfoBinding;
import com.settlex.android.ui.activities.legal.PrivacyPolicyActivity;
import com.settlex.android.ui.activities.legal.TermsAndConditionsActivity;
import com.settlex.android.ui.auth.util.AuthResult;
import com.settlex.android.ui.auth.viewmodel.AuthViewModel;
import com.settlex.android.ui.common.SettleXProgressBarController;
import com.settlex.android.util.StringUtil;

import java.util.Objects;

public class SignUpUserContactInfoFragment extends Fragment {
    private AuthViewModel authViewModel;
    private SettleXProgressBarController progressController;
    private FragmentSignUpUserContactInfoBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSignUpUserContactInfoBinding.inflate(inflater, container, false);

        progressController = new SettleXProgressBarController(binding.getRoot());
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        configureStatusBar();
        initializeUiComponents();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupVerificationObserver();
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }

    private void initializeUiComponents() {
        setupInputValidation();
        configureLegalLinks();
        setupFocusHandling();
        setupTouchToHideKeyboard(binding.getRoot());

        binding.imgBackBefore.setOnClickListener(v -> navigateBack());
        binding.btnContinue.setOnClickListener(v -> validateAndRequestOtp());
    }

    /**
     * ===============================================================
     * Observes OTP sending result and handles UI state changes:
     * - Shows loading indicator during request
     * - Navigates to verification screen on success
     * - Displays error message if request fails
     * ===============================================================
     */
    private void setupVerificationObserver() {
        authViewModel.getSendVerificationOtpResult().observe(getViewLifecycleOwner(), event -> {
            AuthResult<String> result = event.getContentIfNotHandled();
            if (result != null) {

                switch (result.getStatus()) {
                    case LOADING -> progressController.show();
                    case SUCCESS -> {
                        progressController.hide();
                        navigateToFragment(new SignUpEmailVerificationFragment());
                    }
                    case ERROR -> {
                        progressController.hide();
                        showEmailError(result.getMessage());
                    }
                }
            }
        });
    }

    private void validateAndRequestOtp() {
        String email = Objects.requireNonNull(binding.editTxtEmail.getText()).toString().trim();
        String phone = StringUtil.formatPhoneNumber(binding.editTxtPhoneNumber.getText().toString().trim());

        authViewModel.updateEmail(email);
        authViewModel.updatePhone(phone);
        authViewModel.sendEmailVerificationOtp(email);
    }

    private void showEmailError(String message) {
        binding.txtEmailError.setText(message);
        binding.txtEmailError.setVisibility(View.VISIBLE);
    }

    /**
     * ===============================================
     * Updates continue button state based on:
     * - Email format validity
     * - Phone number format validity
     * - Terms checkbox status
     * ===============================================
     */
    private void updateContinueButtonState() {
        String email = Objects.requireNonNull(binding.editTxtEmail.getText()).toString().trim();
        String phone = Objects.requireNonNull(binding.editTxtPhoneNumber.getText()).toString().trim();

        boolean isValidEmail = Patterns.EMAIL_ADDRESS.matcher(email).matches();
        boolean isValidPhone = phone.matches("^(0)?[7-9][0-1]\\d{8}$");
        boolean isTermsAccepted = binding.checkBoxTermsPrivacy.isChecked();

        binding.btnContinue.setEnabled(isValidEmail && isValidPhone && isTermsAccepted);
    }

    private void setupFocusHandling() {
        View.OnClickListener focusListener = v -> {
            if (v instanceof EditText) {
                v.setFocusable(true);
                v.setFocusableInTouchMode(true);
                v.requestFocus();
            }
        };

        binding.editTxtEmail.setOnClickListener(focusListener);
        binding.editTxtPhoneNumber.setOnClickListener(focusListener);

        binding.editTxtPhoneNumber.setOnFocusChangeListener((v, hasFocus) ->
                binding.editTxtPhoneNumberBg.setBackgroundResource(
                        hasFocus
                                ? R.drawable.bg_edit_txt_custom_white_focused
                                : R.drawable.bg_edit_txt_custom_white_not_focused)
        );

        binding.editTxtEmail.setOnFocusChangeListener((v, hasFocus) ->
                binding.editTxtEmailBg.setBackgroundResource(
                        hasFocus
                                ? R.drawable.bg_edit_txt_custom_white_focused
                                : R.drawable.bg_edit_txt_custom_white_not_focused)
        );
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

        binding.editTxtEmail.addTextChangedListener(validationWatcher);
        binding.editTxtPhoneNumber.addTextChangedListener(validationWatcher);
        binding.checkBoxTermsPrivacy.setOnCheckedChangeListener((buttonView, isChecked) -> updateContinueButtonState());

        // Clear errors when typing
        binding.editTxtEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.txtEmailError.setVisibility(View.GONE);
                binding.emailSectionHeader.setVisibility(!TextUtils.isEmpty(s) ? View.VISIBLE : View.INVISIBLE);
            }
        });
    }

    /**
     * ==============================================================================
     * Configures clickable spans for Terms & Conditions and Privacy Policy links
     * ==============================================================================
     */
    private void configureLegalLinks() {
        String legalText = "I have read, understood and agreed to the Terms & Conditions and Privacy Policy.";
        SpannableStringBuilder span = new SpannableStringBuilder(legalText);

        ClickableSpan termsSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                startActivity(new Intent(requireActivity(), TermsAndConditionsActivity.class));
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                ds.setColor(ContextCompat.getColor(requireContext(), R.color.blue));
                ds.setUnderlineText(false);
            }
        };

        ClickableSpan privacySpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                startActivity(new Intent(requireActivity(), PrivacyPolicyActivity.class));
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                ds.setColor(ContextCompat.getColor(requireContext(), R.color.blue));
                ds.setUnderlineText(false);
            }
        };

        // Set spans for "Terms & Conditions" and "Privacy Policy" text
        span.setSpan(termsSpan, legalText.indexOf("Terms"), legalText.indexOf("Conditions") + "Conditions".length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        span.setSpan(privacySpan, legalText.indexOf("Privacy"), legalText.indexOf("Policy") + "Policy".length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        binding.txtTermsPrivacy.setText(span);
        binding.txtTermsPrivacy.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupTouchToHideKeyboard(View root) {
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
                setupTouchToHideKeyboard(((ViewGroup) root).getChildAt(i));
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
        requireActivity().getOnBackPressedDispatcher().onBackPressed();
        requireActivity().finish();
    }

    private void configureStatusBar() {
        Window window = requireActivity().getWindow();
        window.setStatusBarColor(ContextCompat.getColor(requireContext(), R.color.white));
        View decorView = window.getDecorView();
        decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }
}