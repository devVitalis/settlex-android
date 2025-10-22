package com.settlex.android.ui.auth.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.settlex.android.R;
import com.settlex.android.databinding.ActivityPasswordChangeBinding;
import com.settlex.android.ui.auth.viewmodel.AuthViewModel;
import com.settlex.android.ui.common.util.ProgressLoaderController;
import com.settlex.android.util.event.Result;
import com.settlex.android.util.network.NetworkMonitor;
import com.settlex.android.util.ui.StatusBarUtil;
import com.settlex.android.util.ui.UiUtil;

import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PasswordChangeActivity extends AppCompatActivity {

    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    // dependencies
    private ProgressLoaderController progressLoader;
    private ActivityPasswordChangeBinding binding;
    private AuthViewModel authViewModel;
    private boolean isConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPasswordChangeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        progressLoader = new ProgressLoaderController(this);

        setupUiActions();
        observeNetworkStatus();
        observePasswordResetStatus();
    }

    private void setupUiActions() {
        StatusBarUtil.setStatusBarColor(this, R.color.white);
        setupEditTextFocusHandlers();
        setupPasswordValidation();
        clearFocusOnEditTextField();
        setupPasswordVisibilityToggle();

        binding.btnBackBefore.setOnClickListener(v -> finish());
        binding.btnResetPassword.setOnClickListener(v -> attemptPasswordReset());
    }

    // OBSERVERS =========
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

    private void observePasswordResetStatus() {
        authViewModel.getChangeUserPasswordResult().observe(this, event -> {
            Result<String> result = event.getContentIfNotHandled();
            if (result != null) {
                switch (result.getStatus()) {
                    case LOADING -> progressLoader.show();
                    case SUCCESS -> onPasswordResetStatusSuccess();
                    case ERROR -> onPasswordResetStatusError(result.getMessage());
                }
            }
        });
    }

    private void onPasswordResetStatusSuccess() {
        showSuccessDialog();
        progressLoader.hide();
    }

    private void onPasswordResetStatusError(String error) {
        binding.txtError.setText(error);
        binding.txtError.setVisibility(View.VISIBLE);
        progressLoader.hide();
    }

    private void showSuccessDialog() {
        String title = "Password Updated";
        String message = "Your password has been changed successfully. You will now need to use your new password to sign in on all your devices";
        String buttonTxt = "Continue";

        UiUtil.showBottomSheetDialog(this, (dialog, binding) -> {
            binding.title.setText(title);
            binding.message.setText(message);
            binding.btnContinue.setText(buttonTxt);
            binding.anim.playAnimation();

            binding.btnContinue.setOnClickListener(v -> {
                navigateToSignInActivity();
                dialog.dismiss();
            });
            dialog.show();
        });
    }

    private void navigateToSignInActivity() {
        startActivity(new Intent(this, LoginActivity.class));
        finishAffinity();
    }

    private void attemptPasswordReset() {
        if (!isConnected) {
            showNoInternetDialog();
            return;
        }

        String email = getIntent().getStringExtra("email");
        String newPassword = binding.editTxtPassword.getText().toString().trim();

        initiateUserPasswordChange(email, newPassword);
    }

    private void initiateUserPasswordChange(String email, String newPassword) {
        authViewModel.changeUserPassword(email, newPassword);
    }

    private void validatePassword() {
        String password = Objects.requireNonNull(binding.editTxtPassword.getText()).toString().trim();
        String confirm = Objects.requireNonNull(binding.editTxtConfirmPassword.getText()).toString().trim();

        boolean isValid = validatePasswordRequirements(password, confirm);
        updateResetButtonState(isValid);
    }

    private void updateResetButtonState(boolean isEnabled) {
        binding.btnResetPassword.setEnabled(isEnabled);
    }

    private boolean validatePasswordRequirements(String password, String confirm) {
        boolean hasLength = password.length() >= 8;
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasSpecial = password.matches(".*[@#$%^&+=!.].*");
        boolean matches = password.equals(confirm);

        if (!confirm.isEmpty() && !matches) {
            String ERROR_PASSWORD_MISMATCH = "Passwords do not match!";
            binding.txtError.setText(ERROR_PASSWORD_MISMATCH);
            binding.txtError.setVisibility(View.VISIBLE);
        } else {
            binding.txtError.setVisibility(View.GONE);
        }

        return hasLength && hasUpper && hasLower && hasSpecial && matches;
    }

    private void setupPasswordVisibilityToggle() {
        int icVisibleOn = R.drawable.ic_visibility_on_filled;
        int icVisibleOff = R.drawable.ic_visibility_off_filled;

        binding.togglePasswordVisibility.setOnClickListener(v -> {
            Typeface currentTypeface = binding.editTxtPassword.getTypeface();

            isPasswordVisible = !isPasswordVisible;
            int inputType = isPasswordVisible ? InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD : InputType.TYPE_TEXT_VARIATION_PASSWORD;

            binding.editTxtPassword.setInputType(InputType.TYPE_CLASS_TEXT | inputType);
            binding.togglePasswordVisibility.setImageResource(isPasswordVisible ? icVisibleOn : icVisibleOff);

            binding.editTxtPassword.setTypeface(currentTypeface);
            binding.editTxtPassword.setSelection(binding.editTxtPassword.getText().length());
        });

        binding.toggleConfirmPasswordVisibility.setOnClickListener(v -> {
            Typeface currentTypeface = binding.editTxtPassword.getTypeface();

            isConfirmPasswordVisible = !isConfirmPasswordVisible;
            int inputType = isConfirmPasswordVisible ? InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD : InputType.TYPE_TEXT_VARIATION_PASSWORD;

            binding.editTxtConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | inputType);
            binding.toggleConfirmPasswordVisibility.setImageResource(isPasswordVisible ? icVisibleOn : icVisibleOff);

            binding.editTxtConfirmPassword.setTypeface(currentTypeface);
            binding.editTxtConfirmPassword.setSelection(binding.editTxtPassword.getText().length());
        });
    }

    private void setupEditTextFocusHandlers() {
        int focusBgRes = R.drawable.bg_edit_txt_custom_white_focused;
        int defaultBgRes = R.drawable.bg_edit_txt_custom_gray_not_focused;

        binding.editTxtPassword.setOnFocusChangeListener((v, hasFocus) -> binding.editTxtPasswordBg.setBackgroundResource(hasFocus ? focusBgRes : defaultBgRes));
        binding.editTxtConfirmPassword.setOnFocusChangeListener((v, hasFocus) -> binding.editTxtConfirmPasswordBg.setBackgroundResource(hasFocus ? focusBgRes : defaultBgRes));
    }

    private void setupPasswordValidation() {
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String password = binding.editTxtPassword.getText().toString();
                String confirmPassword = binding.editTxtConfirmPassword.getText().toString();

                binding.togglePasswordVisibility.setVisibility(!password.isEmpty() ? View.VISIBLE : View.INVISIBLE);
                binding.toggleConfirmPasswordVisibility.setVisibility(!confirmPassword.isEmpty() ? View.VISIBLE : View.INVISIBLE);

                validatePassword();
            }
        };
        binding.editTxtPassword.addTextChangedListener(watcher);
        binding.editTxtConfirmPassword.addTextChangedListener(watcher);
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
        binding.editTxtConfirmPassword.setOnEditorActionListener((v, actionId, event) -> {
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
