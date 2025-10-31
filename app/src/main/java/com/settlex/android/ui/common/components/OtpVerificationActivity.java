package com.settlex.android.ui.common.components;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

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

import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class OtpVerificationActivity extends AppCompatActivity {

    private boolean isConnected;

    // dependencies
    private String userEmail;
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

    private void setupUiActions() {
        StatusBarUtil.setStatusBarColor(this, R.color.white);
        setupOtpInputWatcher();
        startResendOtpCooldown();
        maskAndDisplayUserEmail();

        binding.btnBackBefore.setOnClickListener(v -> finish());
        binding.btnResendOtp.setOnClickListener(v -> resendOtpCode());
        binding.btnConfirm.setOnClickListener(v -> attemptOtpVerification());
        binding.btnHelp.setOnClickListener(v -> StringUtil.showNotImplementedToast(this));
    }


    private void observeNetworkStatus() {
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
        Intent intent = new Intent(this, PasswordChangeActivity.class);
        intent.putExtra("email", userEmail);
        startActivity(intent);
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

    private void setupOtpInputWatcher() {
        binding.otpBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence otp, int start, int before, int count) {
                if (otp.toString().isEmpty()) binding.txtOtpFeedback.setVisibility(View.GONE);
                binding.btnConfirm.setEnabled(isOtpComplete());
            }
        });
    }


    private boolean isOtpComplete() {
        return binding.otpBox.length() == binding.otpBox.getItemCount();
    }

    private String getEnteredOtpCode() {
        return Objects.requireNonNull(binding.otpBox.getText()).toString();
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
