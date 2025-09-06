package com.settlex.android.ui.common.components;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.settlex.android.R;
import com.settlex.android.databinding.ActivityOtpVerificationBinding;
import com.settlex.android.ui.auth.activity.PasswordChangeActivity;
import com.settlex.android.util.event.Result;
import com.settlex.android.ui.auth.viewmodel.AuthViewModel;
import com.settlex.android.ui.common.util.SettleXProgressBarController;
import com.settlex.android.util.string.StringUtil;

public class OtpVerificationActivity extends AppCompatActivity {
    private String userEmail;
    private EditText[] otpInputs;
    private AuthViewModel authViewModel;
    private ActivityOtpVerificationBinding binding;
    private SettleXProgressBarController progressBarController;

    // ====================== LIFECYCLE ======================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOtpVerificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userEmail = getIntent().getStringExtra("email");
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        progressBarController = new SettleXProgressBarController(binding.getRoot());

        setupStatusBar();
        setupUiActions();

        observeVerifyOtpResult();
        observeSendOtpResult();
    }

    // ====================== OBSERVERS ======================
    private void observeVerifyOtpResult() {
        authViewModel.getVerifyEmailResetOtpResult().observe(this, event -> {
            Result<String> result = event.getContentIfNotHandled();
            if (result != null) {
                switch (result.getStatus()) {
                    case LOADING -> progressBarController.show();
                    case SUCCESS -> onVerifyOtpSuccess();
                    case FAILED -> showOtpError(result.getMessage());
                }
            }
        });
    }

    private void observeSendOtpResult() {
        authViewModel.getSendEmailResetOtpResult().observe(this, event -> {
            Result<String> result = event.getContentIfNotHandled();
            if (result != null) {
                switch (result.getStatus()) {
                    case LOADING -> progressBarController.show();
                    case SUCCESS -> onSendOtpSuccess();
                    case FAILED -> showOtpError(result.getMessage());
                }
            }
        });
    }

    // ====================== HANDLERS ======================
    private void onVerifyOtpSuccess() {
        startActivity(new Intent(this, PasswordChangeActivity.class).putExtra("email", userEmail));
        finish();
        progressBarController.hide();
    }

    private void onSendOtpSuccess() {
        startResendOtpCooldown();
        progressBarController.hide();
    }

    private void showOtpError(String message) {
        binding.txtOtpFeedback.setText(message);
        binding.txtOtpFeedback.setVisibility(View.VISIBLE);
        progressBarController.hide();
    }

    // ====================== UI SETUP ======================
    private void setupUiActions() {
        setupOtpInputBehavior();
        startResendOtpCooldown();
        maskAndDisplayUserEmail();

        binding.imgBackBefore.setOnClickListener(v -> finish());
        binding.btnResendOtp.setOnClickListener(v -> resendOtp());
        binding.btnConfirm.setOnClickListener(v -> verifyOtp());
    }

    private void setupStatusBar() {
        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        View decorView = window.getDecorView();
        decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }

    // ====================== ACTIONS ======================
    private void verifyOtp() {
        authViewModel.verifyPasswordResetOtp(userEmail, getEnteredOtpCode());
    }

    private void resendOtp() {
        authViewModel.sendPasswordResetOtp(userEmail);
    }

    // ====================== OTP INPUT ======================
    private void setupOtpInputBehavior() {
        otpInputs = new EditText[]{binding.otpDigit1, binding.otpDigit2, binding.otpDigit3, binding.otpDigit4, binding.otpDigit5, binding.otpDigit6};

        for (int i = 0; i < otpInputs.length; i++) {
            EditText current = otpInputs[i];
            EditText next = (i < otpInputs.length - 1) ? otpInputs[i + 1] : null;
            EditText prev = (i > 0) ? otpInputs[i - 1] : null;

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
                    if (isOtpComplete()) hideSoftKeyboard();
                    if (TextUtils.isEmpty(s)) binding.txtOtpFeedback.setVisibility(View.GONE);

                    if (s.length() == 1 && next != null) {
                        next.setEnabled(true);
                        next.requestFocus();
                        next.setText("");
                    }
                    binding.btnConfirm.setEnabled(isOtpComplete());
                }
            });

            current.setOnKeyListener((v, keyCode, event) -> {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
                    if (!TextUtils.isEmpty(current.getText())) {
                        current.setText("");
                    } else if (prev != null) {
                        prev.setText("");
                        prev.requestFocus();
                        current.setEnabled(false);
                    }
                    return true;
                }
                return false;
            });
        }
    }

    private boolean isOtpComplete() {
        for (EditText input : otpInputs) {
            if (TextUtils.isEmpty(input.getText())) return false;
        }
        return true;
    }

    private String getEnteredOtpCode() {
        StringBuilder pin = new StringBuilder();
        for (EditText input : otpInputs) {
            pin.append(input.getText().toString().trim());
        }
        return pin.toString();
    }

    // ====================== HELPERS ======================
    private void startResendOtpCooldown() {
        binding.btnResendOtp.setEnabled(false);
        binding.btnResendOtp.setTag(binding.btnResendOtp.getText());

        new CountDownTimer(60000, 1000) {
            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                if (seconds > 0) {
                    binding.btnResendOtp.setText(getString(R.string.resend_otp_countdown, seconds));
                }
            }

            public void onFinish() {
                binding.btnResendOtp.setText((CharSequence) binding.btnResendOtp.getTag());
                binding.btnResendOtp.setEnabled(true);
            }
        }.start();
    }

    private void maskAndDisplayUserEmail() {
        String maskedEmail = StringUtil.maskEmail(userEmail);
        binding.txtUserEmail.setText(maskedEmail);
    }

    private void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = getCurrentFocus();
        if (view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    // ====================== OVERRIDES ======================
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View currentFocus = getCurrentFocus();
            if (currentFocus instanceof EditText) {
                Rect outRect = new Rect();
                currentFocus.getGlobalVisibleRect(outRect);

                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    currentFocus.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
                    binding.main.requestFocus();
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }
}
