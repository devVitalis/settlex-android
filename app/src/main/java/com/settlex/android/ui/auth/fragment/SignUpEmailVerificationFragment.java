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
import com.settlex.android.util.StringUtil;

public class SignUpEmailVerificationFragment extends Fragment {

    private static final int OTP_RESEND_COOLDOWN_MS = 60000;
    private static final int COUNTDOWN_INTERVAL_MS = 1000;

    private String userEmail;
    private AuthViewModel authViewModel;
    private CountDownTimer resendOtpCountdownTimer;
    private EditText[] otpDigitViews;
    private SettleXProgressBarController progressBarController;
    private FragmentSignUpEmailVerificationBinding binding;

    // ====================== LIFECYCLE ======================
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSignUpEmailVerificationBinding.inflate(getLayoutInflater(), container, false);

        progressBarController = new SettleXProgressBarController(binding.getRoot());
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        userEmail = authViewModel.getEmail();

        setupStatusBar();
        setupUiActions();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        startResendOtpCooldown();
        observeSendEmailVerificationOtp();
        observeVerifyEmailVerificationOtp();
    }

    @Override
    public void onDestroyView() {
        if (resendOtpCountdownTimer != null) {
            resendOtpCountdownTimer.cancel();
        }
        binding = null;
        super.onDestroyView();
    }

    // ====================== OBSERVERS ======================
    private void observeVerifyEmailVerificationOtp() {
        authViewModel.getVerifyEmailVerificationOtpResult().observe(getViewLifecycleOwner(), event -> {
            AuthResult<String> result = event.getContentIfNotHandled();
            if (result == null) return;

            switch (result.getStatus()) {
                case LOADING -> progressBarController.show();
                case SUCCESS -> onOtpVerificationSuccess();
                case ERROR -> onSendOrVerifyOtpFailure(result.getMessage());
            }
        });
    }

    private void observeSendEmailVerificationOtp() {
        authViewModel.getSendEmailVerificationOtpResult().observe(getViewLifecycleOwner(), event -> {
            AuthResult<String> result = event.getContentIfNotHandled();
            if (result == null) return;

            switch (result.getStatus()) {
                case LOADING -> progressBarController.show();
                case SUCCESS -> startResendOtpCooldown();
                case ERROR -> onSendOrVerifyOtpFailure(result.getMessage());
            }
        });
    }

    private void onOtpVerificationSuccess() {
        navigateToFragment(new SignupUserInfoFragment());
        progressBarController.hide();
    }

    private void onSendOrVerifyOtpFailure(String reason) {
        binding.txtOtpFeedback.setText(reason);
        binding.txtOtpFeedback.setVisibility(View.VISIBLE);
        progressBarController.hide();
    }

    // ====================== CORE FLOW ======================
    private void verifyOtp() {
        authViewModel.verifyEmailOtp(userEmail, getEnteredOtpDigits());
    }

    private void resendOtp() {
        authViewModel.sendEmailVerificationOtp(userEmail);
    }

    // ====================== UI SETUP ======================
    private void setupUiActions() {
        formatInfoText();
        setupOtpInputBehavior();
        maskAndDisplayEmail();

        binding.imgBackBefore.setOnClickListener(v -> navigateBack());
        binding.btnVerify.setOnClickListener(v -> verifyOtp());
        binding.btnResendOtp.setOnClickListener(v -> resendOtp());
    }

    private void setupStatusBar() {
        Window window = requireActivity().getWindow();
        window.setStatusBarColor(ContextCompat.getColor(requireContext(), R.color.white));
        View decorView = window.getDecorView();
        decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }

    private void formatInfoText() {
        String infoText = "Didn’t get the email? Make sure to also " + "<font color='#FFA500'><b>check your spam/junk folder</b></font> " + "if you can't find the email in your inbox.";
        binding.txtInfo.setText(Html.fromHtml(infoText, Html.FROM_HTML_MODE_LEGACY));
    }

    private void maskAndDisplayEmail() {
        binding.txtUserEmail.setText(StringUtil.maskEmail(userEmail));
    }

    // ====================== OTP HANDLING ======================
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

        progressBarController.hide();
    }

    private void setupOtpInputBehavior() {
        otpDigitViews = new EditText[]{binding.otpDigit1, binding.otpDigit2, binding.otpDigit3, binding.otpDigit4, binding.otpDigit5, binding.otpDigit6};

        for (int i = 0; i < otpDigitViews.length; i++) {
            EditText current = otpDigitViews[i];
            EditText next = (i < otpDigitViews.length - 1) ? otpDigitViews[i + 1] : null;
            EditText previous = (i > 0) ? otpDigitViews[i - 1] : null;

            current.setLongClickable(false);
            current.setTextIsSelectable(false);
            current.setEnabled(i == 0);

            current.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (isOtpDigitsFilled()) hideKeyboard();
                    if (TextUtils.isEmpty(s)) binding.txtOtpFeedback.setVisibility(View.GONE);

                    if (s.length() == 1 && next != null) {
                        next.setEnabled(true);
                        next.requestFocus();
                    }

                    binding.btnVerify.setEnabled(isOtpDigitsFilled());
                }
            });

            current.setOnKeyListener((v, keyCode, event) -> {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
                    if (!TextUtils.isEmpty(current.getText())) {
                        current.setText("");
                    } else if (previous != null) {
                        previous.setText("");
                        previous.requestFocus();
                        current.setEnabled(false);
                    }
                    return true;
                }
                return false;
            });
        }
    }

    private boolean isOtpDigitsFilled() {
        for (EditText digit : otpDigitViews) {
            if (TextUtils.isEmpty(digit.getText())) return false;
        }
        return true;
    }

    private String getEnteredOtpDigits() {
        StringBuilder otp = new StringBuilder();
        for (EditText digit : otpDigitViews) {
            otp.append(digit.getText().toString().trim());
        }
        return otp.toString();
    }

    // ====================== NAVIGATION ======================
    private void navigateToFragment(Fragment fragment) {
        requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
    }

    private void navigateBack() {
        requireActivity().getSupportFragmentManager().popBackStack();
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        View focusedView = requireActivity().getCurrentFocus();
        if (focusedView != null) {
            imm.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
        }
    }
}
