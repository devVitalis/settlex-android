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
import com.settlex.android.ui.auth.viewmodel.AuthViewModel;
import com.settlex.android.ui.common.components.OtpVerificationActivity;
import com.settlex.android.ui.common.util.ProgressLoaderController;
import com.settlex.android.util.event.Result;
import com.settlex.android.util.network.NetworkMonitor;

/**
 * Handles password reset initiation flow:
 */
public class PasswordResetActivity extends AppCompatActivity {
    private boolean isConnected = false;

    private ActivityPasswordResetBinding binding;
    private AuthViewModel authViewModel;
    private ProgressLoaderController progressLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPasswordResetBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        progressLoader = new ProgressLoaderController(this);
        progressLoader.setOverlayColor(ContextCompat.getColor(this, R.color.semi_transparent_white));

        setupStatusBar();
        setupUiActions();
        observeNetworkStatus();
        observeOtpRequestResult();
    }

    // OBSERVERS ===============
    private void observeOtpRequestResult() {
        authViewModel.getSendEmailResetOtpResult().observe(this, event -> {
            Result<String> result = event.getContentIfNotHandled();
            if (result == null) return;

            switch (result.getStatus()) {
                case LOADING -> progressLoader.show();
                case SUCCESS -> handleOtpRequestSuccess();
                case ERROR -> handleOtpRequestError(result.getMessage());
            }
        });
    }

    private void observeNetworkStatus() {
        NetworkMonitor.getNetworkStatus().observe(this, isConnected -> this.isConnected = isConnected);
    }

    private void handleOtpRequestSuccess() {
        String email = binding.editTxtEmail.getText().toString().trim();
        startActivity(new Intent(this, OtpVerificationActivity.class).putExtra("email", email));
        progressLoader.hide();
    }

    private void handleOtpRequestError(String error) {
        binding.txtErrorFeedback.setText(error);
        binding.txtErrorFeedback.setVisibility(View.VISIBLE);
        progressLoader.hide();
    }

    private void showNoInternetConnection() {
        binding.txtErrorFeedback.setText(getString(R.string.error_no_internet));
        binding.txtErrorFeedback.setVisibility(View.VISIBLE);
    }

    private void requestPasswordResetOtp() {
        if (isConnected) {
            authViewModel.sendPasswordResetOtp(binding.editTxtEmail.getText().toString().trim());
        } else {
            showNoInternetConnection();
        }
    }

    //  UI CONFIGURATION ===============

    private void setupUiActions() {
        setupEmailInputValidation();
        setupFocusHandlers();

        binding.imgBackBefore.setOnClickListener(v -> finish());
        binding.btnContinue.setOnClickListener(v -> requestPasswordResetOtp());
    }


    private void setupEmailInputValidation() {
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

    private void updateContinueButtonState() {
        String email = binding.editTxtEmail.getText().toString().trim();
        boolean emailValid = (Patterns.EMAIL_ADDRESS.matcher(email).matches());

        binding.btnContinue.setEnabled(emailValid);
    }

    // FOCUS MANAGEMENT =================
    private void setupFocusHandlers() {
        View.OnClickListener focusListener = (v -> {
            if (v instanceof EditText) {
                v.setFocusable(true);
                v.setFocusableInTouchMode(true);
                v.requestFocus();
            }
        });
        binding.editTxtEmail.setOnClickListener(focusListener);

        // Background changes
        binding.editTxtEmail.setOnFocusChangeListener((v, hasFocus) -> {
            int emailBackgroundRes = (hasFocus) ? R.drawable.bg_edit_txt_custom_gray_focused : R.drawable.bg_edit_txt_custom_gray_not_focused;
            binding.editTxtEmailBackground.setBackgroundResource(emailBackgroundRes);
        });
    }

    // UTILITIES =============

    private void setupStatusBar() {
        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        window.getDecorView().setSystemUiVisibility(window.getDecorView().getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }

    // Dismiss keyboard on outside taps
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
                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(focus.getWindowToken(), 0);
                binding.main.requestFocus();
            }
        }
    }
}