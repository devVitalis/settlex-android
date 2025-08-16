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

public class PasswordResetActivity extends AppCompatActivity {
    private SettleXProgressBarController progressBarController;
    private ActivityPasswordResetBinding binding;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPasswordResetBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        progressBarController = new SettleXProgressBarController(binding.getRoot());

        setupStatusBar();
        setupUiActions();

        sendEmailResetOtpObserver();
    }

    /**
     * Observes the LiveData for sending a password reset OTP.
     * It handles loading, success, and error states.
     */
    private void sendEmailResetOtpObserver (){
        authViewModel.getSendEmailResetOtpResult().observe(this, event -> {
            AuthResult<String> result = event.getContentIfNotHandled();
            if (result != null) {
                switch (result.getStatus()) {
                    case LOADING -> progressBarController.show();
                    case SUCCESS -> handleResetOtpSuccess();
                    case ERROR -> handleResetError(result.getMessage());
                }
            }
        });
    }

    private void handleResetOtpSuccess() {
        String email = binding.editTxtEmail.getText().toString().trim();

        Intent intent = new Intent(this, OtpVerificationActivity.class);
        intent.putExtra("email", email);
        startActivity(intent);

        progressBarController.hide();
    }

    private void handleResetError(String errorMessage) {
        binding.txtErrorFeedback.setText(errorMessage);
        binding.txtErrorFeedback.setVisibility(View.VISIBLE);
        progressBarController.hide();
    }

    /**
     * Initializes and sets up all UI-related actions and listeners.
     */
    private void setupUiActions() {
        setupEmailInputObserver();
        reEnableEditTextFocus();
        setupEmailBackgroundFocusListener();

        binding.imgBackBefore.setOnClickListener(v -> finish());
        binding.btnContinue.setOnClickListener(v -> sendPasswordResetOtp());
    }

    private void sendPasswordResetOtp() {
        String email = binding.editTxtEmail.getText().toString().trim();
        authViewModel.sendPasswordResetOtp(email);
    }

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
                binding.txtErrorFeedback.setVisibility(View.GONE);
                updateResetButtonState();
            }
        });
    }

    private void updateResetButtonState() {
        String email = binding.editTxtEmail.getText().toString().trim();
        boolean isValidEmail = Patterns.EMAIL_ADDRESS.matcher(email).matches();

        binding.btnContinue.setEnabled(isValidEmail);
    }

    /**
     * Re-enables focus on EditTexts when it's clicked.
     */
    private void reEnableEditTextFocus() {
        binding.editTxtEmail.setOnClickListener(v -> {
            if (v instanceof EditText) {
                v.setFocusable(true);
                v.setFocusableInTouchMode(true);
                v.requestFocus();
            }
        });
    }

    private void setupEmailBackgroundFocusListener() {
        binding.editTxtEmail.setOnFocusChangeListener((v, hasFocus) -> {
            int backgroundRes = hasFocus ? R.drawable.bg_edit_txt_custom_gray_focused : R.drawable.bg_edit_txt_custom_gray_not_focused;
            binding.editTxtEmailBackground.setBackgroundResource(backgroundRes);
        });
    }

    /**
     * Intercepts touch events to hide the keyboard
     * and clear focus if the user taps outside an EditText.
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            clearFocusAndHideKeyboardOnOutsideTap(event);
        }
        return super.dispatchTouchEvent(event);
    }

    private void clearFocusAndHideKeyboardOnOutsideTap(MotionEvent event) {
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

    private void setupStatusBar() {
        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.white));

        View decorView = window.getDecorView();
        decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }
}