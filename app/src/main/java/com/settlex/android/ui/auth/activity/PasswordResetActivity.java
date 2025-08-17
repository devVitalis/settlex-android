package com.settlex.android.ui.auth.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.settlex.android.R;
import com.settlex.android.databinding.ActivityPasswordResetBinding;
import com.settlex.android.ui.auth.components.OtpVerificationActivity;
import com.settlex.android.ui.auth.util.AuthResult;
import com.settlex.android.ui.auth.viewmodel.AuthViewModel;
import com.settlex.android.ui.common.SettleXProgressBarController;

/**
 * Handles password reset initiation flow:
 */
public class PasswordResetActivity extends AppCompatActivity {

    private ActivityPasswordResetBinding binding;
    private AuthViewModel authViewModel;
    private SettleXProgressBarController progressBarController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPasswordResetBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        progressBarController = new SettleXProgressBarController(binding.getRoot());

        setupStatusBar();
        setupUiComponents();
        observeOtpRequestResult();
    }

    // ====================== CORE FLOW ======================

    /**
     * Monitors OTP request state changes (loading/success/error)
     */
    private void observeOtpRequestResult() {
        authViewModel.getSendEmailResetOtpResult().observe(this, event -> {
            AuthResult<String> result = event.getContentIfNotHandled();
            if (result == null) return;

            switch (result.getStatus()) {
                case LOADING -> progressBarController.show();
                case SUCCESS -> handleOtpRequestSuccess();
                case ERROR -> handleOtpRequestError(result.getMessage());
            }
        });
    }

    private void handleOtpRequestSuccess() {
        String email = binding.editTxtEmail.getText().toString().trim();
        startActivity(new Intent(this, OtpVerificationActivity.class).putExtra("email", email));
        progressBarController.hide();
    }

    private void handleOtpRequestError(String error) {
        binding.txtErrorFeedback.setText(error);
        binding.txtErrorFeedback.setVisibility(View.VISIBLE);
        progressBarController.hide();
    }

    // ====================== UI CONFIGURATION ======================

    /**
     * Configures all interactive UI elements
     */
    private void setupUiComponents() {
        configureEmailValidation();
        setupFocusHandlers();

        binding.imgBackBefore.setOnClickListener(v -> finish());
        binding.btnContinue.setOnClickListener(v -> requestPasswordResetOtp());
    }

    /**
     * Initiates password reset OTP request
     */
    private void requestPasswordResetOtp() {
        authViewModel.sendPasswordResetOtp(
                binding.editTxtEmail.getText().toString().trim()
        );
    }

    /**
     * Sets up real-time email validation
     */
    private void configureEmailValidation() {
        binding.editTxtEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.txtErrorFeedback.setVisibility(View.GONE);
                updateContinueButtonState();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    /**
     * Updates button state based on email validity
     */
    private void updateContinueButtonState() {
        String email = binding.editTxtEmail.getText().toString().trim();
        boolean emailValid = (Patterns.EMAIL_ADDRESS.matcher(email).matches());

        binding.btnContinue.setEnabled(emailValid);
    }

    // ====================== FOCUS MANAGEMENT ======================

    /**
     * Handles EditText focus and background changes
     */
    private void setupFocusHandlers() {
        // Focus restoration
        binding.editTxtEmail.setOnClickListener(v -> {
            if (v instanceof EditText) {
                v.setFocusable(true);
                v.setFocusableInTouchMode(true);
                v.requestFocus();
            }
        });

        // Background changes
        binding.editTxtEmail.setOnFocusChangeListener((v, hasFocus) ->
                binding.editTxtEmailBackground.setBackgroundResource(
                        hasFocus ? R.drawable.bg_edit_txt_custom_gray_focused
                                : R.drawable.bg_edit_txt_custom_gray_not_focused));
    }

    // ====================== UTILITIES ======================

    private void setupStatusBar() {
        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        window.getDecorView().setSystemUiVisibility(
                window.getDecorView().getSystemUiVisibility() |
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }

    /**
     * Handles keyboard dismissal on outside taps
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            handleOutsideTap(event);
        }
        return super.dispatchTouchEvent(event);
    }

    private void handleOutsideTap(MotionEvent event) {
        View focus = getCurrentFocus();
        if (focus instanceof EditText) {
            Rect rect = new Rect();
            focus.getGlobalVisibleRect(rect);
            if (!rect.contains((int) event.getRawX(), (int) event.getRawY())) {
                focus.clearFocus();
                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(focus.getWindowToken(), 0);
                binding.main.requestFocus();
            }
        }
    }
}