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

        setupStatusBar();
        setupUiActions();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        startResendOtpCooldown();
        sendEmailVerificationOtpObserver();
        verifyEmailVerificationOtpObserver();
    }

    @Override
    public void onDestroyView() {
        resendOtpCountdownTimer.cancel();
        binding = null;
        super.onDestroyView();
    }

    /**
     * Observes the LiveData for verifying an email verification OTP.
     * It handles loading, success, and error states.
     */
    private void verifyEmailVerificationOtpObserver() {
        authViewModel.getVerifyEmailVerificationOtpResult().observe(getViewLifecycleOwner(), event -> {
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
    }

    /**
     * Observes the LiveData for sending an email verification OTP.
     * It handles loading, success, and error states.
     */
    private void sendEmailVerificationOtpObserver() {
        authViewModel.getSendEmailVerificationOtpResult().observe(getViewLifecycleOwner(), event -> {
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

    private void showOtpError(String message) {
        binding.txtOtpFeedback.setText(message);
        binding.txtOtpFeedback.setVisibility(View.VISIBLE);
    }

    /**
     * Initializes and sets up all UI-related actions and listeners.
     */
    private void setupUiActions() {
        formatInfoText();
        setupOtpInputBehavior();
        displayEmail();

        binding.imgBackBefore.setOnClickListener(v -> navigateBack());
        binding.btnVerify.setOnClickListener(v -> verifyEmailVerificationOtp());
        binding.btnResendOtp.setOnClickListener(v -> requestEmailVerificationOtp());
    }

    private void verifyEmailVerificationOtp() {
        authViewModel.verifyEmailOtp(authViewModel.getEmail(), getEnteredOtpDigits());
    }

    private void requestEmailVerificationOtp() {
        authViewModel.sendEmailVerificationOtp(authViewModel.getEmail());
    }

    /**
     * Starts a cooldown timer for the OTP resend button.
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

    /**
     * Configures OTP input fields with chaining behavior.
     */
    private void setupOtpInputBehavior() {
        otpDigitViews = new EditText[]{
                binding.otpDigit1, binding.otpDigit2, binding.otpDigit3,
                binding.otpDigit4, binding.otpDigit5, binding.otpDigit6
        };

        for (int i = 0; i < otpDigitViews.length; i++) {
            EditText currentDigitView = otpDigitViews[i];
            EditText nextDigitView = (i < otpDigitViews.length - 1) ? otpDigitViews[i + 1] : null;
            EditText previousDigitView = (i > 0) ? otpDigitViews[i - 1] : null;

            currentDigitView.setLongClickable(false);
            currentDigitView.setTextIsSelectable(false);
            currentDigitView.setEnabled(i == 0);

            currentDigitView.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (isOtpDigitsFilled()) hideKeyboard();
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

    /**
     * Checks if all OTP digit input fields are filled.
     * @return True if all fields are filled, otherwise false.
     */
    private boolean isOtpDigitsFilled() {
        for (EditText digitView : otpDigitViews) {
            if (TextUtils.isEmpty(digitView.getText())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Concatenates the digits from the OTP input fields into a single string.
     * @return The complete OTP string.
     */
    private String getEnteredOtpDigits() {
        StringBuilder otpBuilder = new StringBuilder();
        for (EditText digitView : otpDigitViews) {
            otpBuilder.append(digitView.getText().toString().trim());
        }
        return otpBuilder.toString();
    }

    /**
     * Displays the user's email address on the screen.
     */
    private void displayEmail() {
        binding.txtUserEmail.setText(authViewModel.getEmail());
    }

    /**
     * Formats informational text with HTML.
     */
    private void formatInfoText() {
        String infoText = "Didn’t get the email? Make sure to also "
                + "<font color='#FFA500'><b>check your spam/junk folder</b></font> " + "if you can't find the email in your inbox.";
        binding.txtInfo.setText(Html.fromHtml(infoText, Html.FROM_HTML_MODE_LEGACY));
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        View focusedView = requireActivity().getCurrentFocus();
        if (focusedView != null) {
            imm.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
        }
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

    private void setupStatusBar() {
        Window window = requireActivity().getWindow();
        window.setStatusBarColor(ContextCompat.getColor(requireContext(), R.color.white));
        View decorView = window.getDecorView();
        decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }
}