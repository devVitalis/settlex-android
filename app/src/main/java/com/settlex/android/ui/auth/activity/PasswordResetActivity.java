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
import android.view.inputmethod.EditorInfo;
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
import com.settlex.android.util.ui.StatusBarUtil;
import com.settlex.android.util.ui.UiUtil;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Handles password reset initiation flow:
 */
@AndroidEntryPoint
public class PasswordResetActivity extends AppCompatActivity {

    private ProgressLoaderController progressLoader;
    private ActivityPasswordResetBinding binding;
    private AuthViewModel authViewModel;
    private boolean isConnected = false;

    // instance var
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPasswordResetBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        progressLoader = new ProgressLoaderController(this);
        progressLoader.setOverlayColor(ContextCompat.getColor(this, R.color.semi_transparent_white));

        setupUiActions();
        observeNetworkStatus();
        observeOtpRequestAndHandleResult();
    }

    private void setupUiActions() {
        StatusBarUtil.setStatusBarColor(this, R.color.white);
        setupEmailInputValidation();
        setupEditTextFocusHandlers();
        clearFocusOnEditTextField();

        binding.btnBackBefore.setOnClickListener(v -> finish());
        binding.btnContinue.setOnClickListener(v -> requestPasswordResetOtp());
    }


    private void observeNetworkStatus() {
        NetworkMonitor.getNetworkStatus().observe(this, isConnected -> {
            if (!isConnected) {
                showNoInternetDialog();
            }
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

    private void observeOtpRequestAndHandleResult() {
        authViewModel.getSendPasswordResetOtpResult().observe(this, event -> {
            Result<String> result = event.getContentIfNotHandled();
            if (result == null) return;

            switch (result.getStatus()) {
                case LOADING -> progressLoader.show();
                case SUCCESS -> handleOtpRequestSuccess();
                case ERROR -> handleOtpRequestError(result.getMessage());
            }
        });
    }

    private void handleOtpRequestSuccess() {
        startActivity(new Intent(this, OtpVerificationActivity.class).putExtra("email", email));
        progressLoader.hide();
    }

    private void handleOtpRequestError(String error) {
        binding.txtError.setText(error);
        binding.txtError.setVisibility(View.VISIBLE);
        progressLoader.hide();
    }

    private void requestPasswordResetOtp() {
        if (!isConnected) {
            showNoInternetDialog();
            return;
        }
        authViewModel.sendPasswordResetOtp(binding.editTxtEmail.getText().toString().trim());
    }

    private void setupEmailInputValidation() {
        binding.editTxtEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                email = s.toString().trim().toLowerCase();

                binding.txtError.setVisibility(View.GONE);
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
        enableButtonContinue(isEmailValid(email));
    }

    private boolean isEmailValid(String email) {
        return (Patterns.EMAIL_ADDRESS.matcher(email).matches());
    }

    private void enableButtonContinue(boolean emailValid) {
        binding.btnContinue.setEnabled(emailValid);
    }

    private void setupEditTextFocusHandlers() {
        int focusBgRes = R.drawable.bg_edit_txt_custom_gray_focused;
        int defaultBgRes = R.drawable.bg_edit_txt_custom_gray_not_focused;

        // background changes
        binding.editTxtEmail.setOnFocusChangeListener((v, hasFocus) ->
                binding.editTxtEmailBackground.setBackgroundResource(hasFocus ? focusBgRes : defaultBgRes));
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

    private void clearFocusOnEditTextField() {
        binding.editTxtEmail.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // Hide the keyboard
                hideKeyboard(v);
                v.clearFocus();
                return true;
            }
            return false;
        });
    }
}