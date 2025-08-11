package com.settlex.android.ui.auth.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
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
import com.settlex.android.databinding.FragmentSignUpEmailVerificationBinding;
import com.settlex.android.ui.auth.util.AuthResult;
import com.settlex.android.ui.auth.viewmodel.AuthViewModel;
import com.settlex.android.ui.common.SettleXProgressBarController;

public class SignUpEmailVerificationFragment extends Fragment {
    // Cooldown duration between OTP resend attempts (60 seconds)
    private static final int OTP_RESEND_COOLDOWN_MS = 60000;
    private static final int COUNTDOWN_INTERVAL_MS = 1000;

    private AuthViewModel authViewModel;
    private CountDownTimer resendOtpCountdownTimer;
    private EditText[] otpDigitViews;
    private SettleXProgressBarController progressController;
    private FragmentSignUpEmailVerificationBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSignUpEmailVerificationBinding.inflate(getLayoutInflater(), container, false);

        progressController = new SettleXProgressBarController(binding.getRoot());
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        configureStatusBar();
        initializeUiComponents();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        startResendOtpCooldown();
        setupVerificationObservers();
    }

    @Override
    public void onDestroyView() {
        resendOtpCountdownTimer.cancel();
        binding = null;
        super.onDestroyView();
    }

    private void initializeUiComponents() {
        setupEmailVerificationInfoText();
        configureOtpInputBehavior();
        displayEmail();

        binding.imgBackBefore.setOnClickListener(v -> navigateBack());
        binding.btnVerify.setOnClickListener(v -> verifyEmailWithOtp());
        binding.btnResendOtp.setOnClickListener(v -> resendVerificationOtp());
    }

    private void setupVerificationObservers() {
        authViewModel.getVerifyVerificationOtpResult().observe(getViewLifecycleOwner(), event -> {
            AuthResult<String> result = event.getContentIfNotHandled();
            if (result != null) {
                switch (result.getStatus()) {
                    case LOADING -> progressController.show();
                    case SUCCESS -> {
                        navigateToFragment(new SignupUserInfoFragment());
                        progressController.hide();
                    }
                    case ERROR -> {
                        showOtpError(result.getMessage());
                        progressController.hide();
                    }
                }
            }
        });

        authViewModel.getSendVerificationOtpResult().observe(getViewLifecycleOwner(), event -> {
            AuthResult<String> result = event.getContentIfNotHandled();
            if (result != null) {
                switch (result.getStatus()) {
                    case LOADING -> progressController.show();
                    case SUCCESS -> {
                        startResendOtpCooldown();
                        progressController.hide();
                    }
                    case ERROR -> {
                        showOtpError(result.getMessage());
                        progressController.hide();
                    }
                }
            }
        });
    }

    private void verifyEmailWithOtp() {
        authViewModel.verifyEmailVerificationOtp(authViewModel.getEmail(), getEnteredOtpDigits());
    }

    private void resendVerificationOtp() {
        authViewModel.sendEmailVerificationOtp(authViewModel.getEmail());
    }

    private void showOtpError(String message) {
        binding.txtOtpFeedback.setText(message);
        binding.txtOtpFeedback.setVisibility(View.VISIBLE);
    }

    /**
     * =======================================================
     * Configures OTP input fields with chaining behavior:
     * - Auto-focuses to next field when digit is entered
     * - Handles backspace to navigate to previous field
     * - Disables text selection to prevent UX issues
     * =======================================================
     */
    private void configureOtpInputBehavior() {
        otpDigitViews = new EditText[]{
                binding.otpDigit1, binding.otpDigit2, binding.otpDigit3,
                binding.otpDigit4, binding.otpDigit5, binding.otpDigit6
        };

        for (int i = 0; i < otpDigitViews.length; i++) {
            EditText currentDigitView = otpDigitViews[i];
            EditText nextDigitView = (i < otpDigitViews.length - 1) ? otpDigitViews[i + 1] : null;
            EditText previousDigitView = (i > 0) ? otpDigitViews[i - 1] : null;

            // Disable text selection to prevent UX issues with small input fields
            currentDigitView.setLongClickable(false);
            currentDigitView.setTextIsSelectable(false);
            currentDigitView.setEnabled(i == 0);

            currentDigitView.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (isOtpDigitsFilled()) hideSoftKeyboard();
                    if (TextUtils.isEmpty(s)) binding.txtOtpFeedback.setVisibility(View.GONE);

                    if (s.length() == 1 && nextDigitView != null) {
                        nextDigitView.setEnabled(true);
                        nextDigitView.requestFocus();
                    }

                    binding.btnVerify.setEnabled(isOtpDigitsFilled());
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });

            currentDigitView.setOnKeyListener((v, keyCode, event) -> {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
                    if (!TextUtils.isEmpty(currentDigitView.getText())) {
                        currentDigitView.setText("");
                    } else if (previousDigitView != null) {
                        previousDigitView.setText("");
                        previousDigitView.requestFocus();
                        currentDigitView.setEnabled(false);
                    }
                    return true;
                }
                return false;
            });
        }
    }

    private boolean isOtpDigitsFilled() {
        for (EditText digitView : otpDigitViews) {
            if (TextUtils.isEmpty(digitView.getText())) {
                return false;
            }
        }
        return true;
    }

    private String getEnteredOtpDigits() {
        StringBuilder otpBuilder = new StringBuilder();
        for (EditText digitView : otpDigitViews) {
            otpBuilder.append(digitView.getText().toString().trim());
        }
        return otpBuilder.toString();
    }

    /**
     * =====================================================
     * Starts cooldown timer that:
     * - Disables resend button during countdown
     * - Shows remaining time in button text
     * - Restores original button state when complete
     * =====================================================
     */
    private void startResendOtpCooldown() {
        binding.btnResendOtp.setEnabled(false);
        final CharSequence originalText = binding.btnResendOtp.getText();

        resendOtpCountdownTimer = new CountDownTimer(OTP_RESEND_COOLDOWN_MS, COUNTDOWN_INTERVAL_MS) {
            public void onTick(long millisUntilFinished) {
                if (binding == null) return;
                binding.btnResendOtp.setText(getString(R.string.resend_otp_countdown, millisUntilFinished / 1000));
            }

            public void onFinish() {
                if (binding != null) {
                    binding.btnResendOtp.setText(originalText);
                    binding.btnResendOtp.setEnabled(true);
                }
            }
        }.start();
    }

    private void displayEmail() {
        binding.txtUserEmail.setText(authViewModel.getEmail());
    }

    private void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        View focusedView = requireActivity().getCurrentFocus();
        if (focusedView != null) {
            imm.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
        }
    }

    private void setupEmailVerificationInfoText() {
        String infoText = "Didn’t get the email? Make sure to also "
                + "<font color='#FFA500'><b>check your spam/junk folder</b></font> " + "if you can't find the email in your inbox.";
        binding.txtInfo.setText(Html.fromHtml(infoText, Html.FROM_HTML_MODE_LEGACY));
    }

    private void navigateToFragment(Fragment fragment) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void navigateBack() {
        requireActivity().getSupportFragmentManager().popBackStack();
    }

    private void configureStatusBar() {
        Window window = requireActivity().getWindow();
        window.setStatusBarColor(ContextCompat.getColor(requireContext(), R.color.white));
        View decorView = window.getDecorView();
        decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }
}