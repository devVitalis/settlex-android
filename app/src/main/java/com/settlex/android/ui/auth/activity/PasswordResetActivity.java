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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.settlex.android.R;
import com.settlex.android.databinding.ActivityPasswordResetBinding;
import com.settlex.android.ui.auth.components.OtpVerificationActivity;
import com.settlex.android.ui.auth.util.AuthResult;
import com.settlex.android.ui.auth.viewmodel.AuthViewModel;
import com.settlex.android.ui.common.SettleXProgressBarController;

public class PasswordResetActivity extends AppCompatActivity {
    private SettleXProgressBarController progressController;
    private ActivityPasswordResetBinding binding;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPasswordResetBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        progressController = new SettleXProgressBarController(binding.getRoot());

        configureStatusBar();
        initializeUiComponents();

        // Observes password reset OTP sending result
        authViewModel.getSendResetOtpResult().observe(this, event -> {
            AuthResult<String> result = event.getContentIfNotHandled();
            if (result != null) {
                switch (result.getStatus()) {
                    case LOADING -> progressController.show();
                    case SUCCESS -> handleResetOtpSuccess(result.getMessage());
                    case ERROR -> handleResetOtpError(result.getMessage());
                }
            }
        });
    }

    /**
     * Handles successful OTP sending:
     * - Shows success message
     * - Navigates to OTP verification screen
     * - Hides progress indicator
     */
    private void handleResetOtpSuccess(String message) {
        String email = binding.editTxtEmail.getText().toString().trim();
        Intent intent = new Intent(this, OtpVerificationActivity.class);
        intent.putExtra("email", email);
        startActivity(intent);

        progressController.hide();
    }

    /**
     * Handles OTP sending error:
     * - Displays error message
     * - Hides progress indicator
     */
    private void handleResetOtpError(String errorMessage) {
        binding.txtErrorInfoEmail.setText(errorMessage);
        progressController.hide();
    }

    private void initializeUiComponents() {
        setupEmailInputObserver();
        configureEmailFocusHandling();
        setupEmailBackgroundFocusListener();

        binding.imgBackBefore.setOnClickListener(v -> finish());
        binding.btnResetPassword.setOnClickListener(v -> sendPasswordResetOtp());
    }

    /**
     * Initiates password reset process by sending OTP to provided email
     */
    private void sendPasswordResetOtp() {
        String email = binding.editTxtEmail.getText().toString().trim();
        authViewModel.sendPasswordResetOtp(email);
    }

    /**
     * Observes email input field changes to:
     * - Update reset button state
     * - Validate email format in real-time
     */
    private void setupEmailInputObserver() {
        binding.editTxtEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateResetButtonState();
            }
        });
    }

    /**
     * Updates reset button enabled state based on email validity
     */
    private void updateResetButtonState() {
        String email = binding.editTxtEmail.getText().toString().trim();
        boolean isValidEmail = Patterns.EMAIL_ADDRESS.matcher(email).matches();
        binding.btnResetPassword.setEnabled(isValidEmail);
    }

    /**
     * Configures email EditText to regain focus when clicked
     */
    private void configureEmailFocusHandling() {
        binding.editTxtEmail.setOnClickListener(v -> {
            if (v instanceof EditText) {
                v.setFocusable(true);
                v.setFocusableInTouchMode(true);
                v.requestFocus();
            }
        });
    }

    /**
     * Changes email background based on focus state
     */
    private void setupEmailBackgroundFocusListener() {
        binding.editTxtEmail.setOnFocusChangeListener((v, hasFocus) -> {
            int backgroundRes =
                    hasFocus
                            ? R.drawable.bg_edit_txt_custom_gray_focused
                            : R.drawable.bg_edit_txt_custom_gray_not_focused;
            binding.editTxtEmailBackground.setBackgroundResource(backgroundRes);
        });
    }

    /**
     * ==========================================
     * Handles taps outside EditText fields:
     * - Hides keyboard - Clears focus
     * ==========================================
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            handleOutsideTap(event);
        }
        return super.dispatchTouchEvent(event);
    }

    private void handleOutsideTap(MotionEvent event) {
        View currentFocus = getCurrentFocus();
        if (currentFocus instanceof EditText) {
            Rect viewRect = new Rect();
            currentFocus.getGlobalVisibleRect(viewRect);

            if (!viewRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                currentFocus.clearFocus();
                hideKeyboard(currentFocus);
                binding.main.requestFocus();
            }
        }
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void configureStatusBar() {
        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.white));

        View decorView = window.getDecorView();
        decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }
}