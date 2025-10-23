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
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.settlex.android.R;
import com.settlex.android.databinding.ActivityOtpVerificationBinding;
import com.settlex.android.ui.auth.activity.PasswordChangeActivity;
import com.settlex.android.ui.auth.viewmodel.AuthViewModel;
import com.settlex.android.ui.common.util.ProgressLoaderController;
import com.settlex.android.utils.event.Result;
import com.settlex.android.utils.network.NetworkMonitor;
import com.settlex.android.utils.string.StringUtil;
import com.settlex.android.utils.ui.StatusBarUtil;
import com.settlex.android.utils.ui.UiUtil;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class OtpVerificationActivity extends AppCompatActivity {

    private boolean isConnected;

    // dependencies
    private String userEmail;
    private EditText[] otpInputs;
    private AuthViewModel authViewModel;
    private ActivityOtpVerificationBinding binding;
    private ProgressLoaderController progressLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOtpVerificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userEmail = getIntent().getStringExtra("email");
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        progressLoader = new ProgressLoaderController(this);

        setupUiActions();
        observeNetworkStatus();
        observeSendOtpStatus();
        observeVerifyOtpStatus();
    }

    private void observeNetworkStatus(){
        NetworkMonitor.getNetworkStatus().observe(this, isConnected -> {
            if (!isConnected) showNoInternetDialog();
            this.isConnected = isConnected;
        });
    }

    private void showNoInternetDialog() {
        String title = "Network Unavailable";
        String message = "Please check your Wi-Fi or cellular data and try again";

        UiUtil.showSimpleAlertDialog(
                this,
                title,
                message);
    }

    private void observeVerifyOtpStatus() {
        authViewModel.getVerifyEmailResetOtpResult().observe(this, event -> {
            Result<String> result = event.getContentIfNotHandled();
            if (result != null) {
                switch (result.getStatus()) {
                    case LOADING -> progressLoader.show();
                    case SUCCESS -> onVerifyOtpStatusSuccess();
                    case ERROR -> onOtpStatusError(result.getMessage());
                }
            }
        });
    }

    private void onVerifyOtpStatusSuccess() {
        startActivity(new Intent(
                this,
                PasswordChangeActivity.class)
                .putExtra("email", userEmail));
        finish();
        progressLoader.hide();
    }

    private void observeSendOtpStatus() {
        authViewModel.getSendPasswordResetOtpResult().observe(this, event -> {
            Result<String> result = event.getContentIfNotHandled();
            if (result != null) {
                switch (result.getStatus()) {
                    case LOADING -> progressLoader.show();
                    case SUCCESS -> onSendOtpStatusSuccess();
                    case ERROR -> onOtpStatusError(result.getMessage());
                }
            }
        });
    }

    private void onSendOtpStatusSuccess() {
        startResendOtpCooldown();
        progressLoader.hide();
    }

    private void onOtpStatusError(String message) {
        binding.txtOtpFeedback.setText(message);
        binding.txtOtpFeedback.setVisibility(View.VISIBLE);
        progressLoader.hide();
    }

    // UI ACTIONS ==========
    private void setupUiActions() {
        StatusBarUtil.setStatusBarColor(this, R.color.white);
        setupOtpInputBehavior();
        startResendOtpCooldown();
        maskAndDisplayUserEmail();

        binding.btnBackBefore.setOnClickListener(v -> finish());
        binding.btnResendOtp.setOnClickListener(v -> resendOtpCode());
        binding.btnConfirm.setOnClickListener(v -> attemptOtpVerification());
        binding.btnHelp.setOnClickListener(v -> Toast.makeText(
                this,
                "Feature not yet implemented",
                Toast.LENGTH_SHORT
        ).show());
    }

    private void attemptOtpVerification() {
        if (!isConnected) {
            showNoInternetDialog();
            return;
        }
        authViewModel.verifyPasswordResetOtp(userEmail, getEnteredOtpCode());
    }

    private void resendOtpCode() {
        if (!isConnected) {
            showNoInternetDialog();
            return;
        }
        authViewModel.sendPasswordResetOtp(userEmail);
    }

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

    private void startResendOtpCooldown() {
        binding.btnResendOtp.setEnabled(false);
        binding.btnResendOtp.setTag(binding.btnResendOtp.getText());

        new CountDownTimer(60000, 1000) {
            public void onTick(long millisUntilFinished) {

                int seconds = (int) (millisUntilFinished / 1000);
                String COUNT_DOWN = "Resend in " + seconds;

                if (seconds > 0) {
                    binding.btnResendOtp.setText(COUNT_DOWN);
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

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    v.clearFocus();
                    hideKeyboard(v);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
