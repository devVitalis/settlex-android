package com.settlex.android.ui.auth.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.settlex.android.R;
import com.settlex.android.databinding.ActivityPasswordChangeBinding;
import com.settlex.android.databinding.BottomSheetSuccessBinding;
import com.settlex.android.ui.auth.util.AuthResult;
import com.settlex.android.ui.auth.viewmodel.AuthViewModel;
import com.settlex.android.ui.common.SettleXProgressBarController;
import com.settlex.android.util.NetworkMonitor;
import com.settlex.android.util.UiUtil;

import java.util.Objects;

/**
 * Handles password reset flow
 */
public class PasswordChangeActivity extends AppCompatActivity {
    private boolean isPasswordVisible = false;
    private boolean isConnected = false;

    private SettleXProgressBarController progressBarController;
    private ActivityPasswordChangeBinding binding;
    private AuthViewModel authViewModel;


    // ====================== LIFECYCLE ======================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPasswordChangeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        progressBarController = new SettleXProgressBarController(binding.getRoot());

        setupStatusBar();
        setupUiActions();
        observeNetworkStatus();
        observePasswordResetResult();
    }


    // ====================== CORE FLOW ======================
    private void observePasswordResetResult() {
        authViewModel.getPasswordResetResult().observe(this, event -> {
            AuthResult<String> result = event.getContentIfNotHandled();
            if (result != null) {
                switch (result.getStatus()) {
                    case LOADING -> progressBarController.show();
                    case SUCCESS -> onResetSuccess();
                    case ERROR -> onResetFailure(result.getMessage());
                }
            }
        });
    }

    private void observeNetworkStatus() {
        NetworkMonitor.getNetworkStatus().observe(this, isConnected -> this.isConnected = isConnected);
    }

    private void onResetSuccess() {
        showSuccessDialog();
        progressBarController.hide();
    }

    private void showSuccessDialog() {
        UiUtil.showBottomSheet(this, BottomSheetSuccessBinding::inflate, (dialogView, dialog) -> dialogView.btnLogin.setOnClickListener(v -> {
            navigateToSignInActivity();
            dialog.dismiss();
        }));
    }

    private void navigateToSignInActivity() {
        startActivity(new Intent(this, SignInActivity.class));
        finishAffinity();
    }

    private void onResetFailure(String error) {
        binding.txtErrorFeedback.setText(error);
        binding.txtErrorFeedback.setVisibility(View.VISIBLE);
        progressBarController.hide();
    }

    private void attemptPasswordReset() {
        String email = getIntent().getStringExtra("email");
        String newPassword = binding.editTxtPassword.getText().toString().trim();

        if (isConnected) {
            authViewModel.requestPasswordReset(email, newPassword);
        } else {
            onNoInternetConnection();
        }
    }

    private void onNoInternetConnection() {
        binding.txtErrorFeedback.setText(getString(R.string.error_no_internet));
        binding.txtErrorFeedback.setVisibility(View.VISIBLE);
    }

    private void validatePassword() {
        String password = Objects.requireNonNull(binding.editTxtPassword.getText()).toString().trim();
        String confirm = Objects.requireNonNull(binding.editTxtConfirmPassword.getText()).toString().trim();

        boolean isValid = validatePasswordRequirements(password, confirm);
        updateResetButtonState(isValid);
    }

    // ====================== PASSWORD VALIDATION ======================
    private boolean validatePasswordRequirements(String password, String confirm) {
        boolean hasLength = password.length() >= 8;
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasSpecial = password.matches(".*[@#$%^&+=!.].*");
        boolean matches = password.equals(confirm);

        if (!confirm.isEmpty() && !matches) {
            binding.txtErrorFeedback.setText(getString(R.string.error_password_mismatch));
            binding.txtErrorFeedback.setVisibility(View.VISIBLE);
        } else {
            binding.txtErrorFeedback.setVisibility(View.GONE);
        }

        showPasswordRequirements(hasLength, hasUpper, hasLower, hasSpecial, password);
        return hasLength && hasUpper && hasLower && hasSpecial && matches;
    }

    private void showPasswordRequirements(boolean hasLength, boolean hasUpper, boolean hasLower, boolean hasSpecial, String password) {
        SpannableStringBuilder requirements = new SpannableStringBuilder();
        appendRequirement(requirements, hasLength, "At least 8 characters");
        appendRequirement(requirements, hasUpper, "Contains uppercase letter");
        appendRequirement(requirements, hasLower, "Contains lowercase letter");
        appendRequirement(requirements, hasSpecial, "Contains special character");

        boolean shouldHidePrompt = password.isEmpty() || (hasLength && hasUpper && hasLower && hasSpecial);
        binding.txtPasswordPrompt.setVisibility(shouldHidePrompt ? View.GONE : View.VISIBLE);
        binding.txtPasswordPrompt.setText(requirements);
    }

    private void appendRequirement(SpannableStringBuilder builder, boolean isMet, String text) {
        Drawable icon = ContextCompat.getDrawable(this, isMet ? R.drawable.ic_checkbox_checked : R.drawable.ic_checkbox_unchecked);
        if (icon != null) {
            int size = (int) (binding.txtPasswordPrompt.getTextSize() * 1.2f);
            icon.setBounds(0, 0, size, size);
            builder.append(" ");
            builder.setSpan(new ImageSpan(icon, ImageSpan.ALIGN_BOTTOM), builder.length() - 1, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.append(" ").append(text).append("\n");
        }
    }

    // ====================== UI SETUP ======================
    private void setupUiActions() {
        setupEditTextFocusHandlers();
        setupPasswordValidation();
        setupPasswordVisibilityToggle();

        binding.imgBackBefore.setOnClickListener(v -> finish());
        binding.btnResetPassword.setOnClickListener(v -> attemptPasswordReset());
    }

    private void setupPasswordVisibilityToggle() {
        binding.togglePasswordVisibility.setOnClickListener(v -> {
            Typeface currentTypeface = binding.editTxtPassword.getTypeface();

            isPasswordVisible = !isPasswordVisible;
            int inputType = isPasswordVisible ? InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD : InputType.TYPE_TEXT_VARIATION_PASSWORD;

            binding.editTxtPassword.setInputType(InputType.TYPE_CLASS_TEXT | inputType);
            binding.editTxtConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | inputType);
            binding.togglePasswordVisibility.setImageResource(isPasswordVisible ? R.drawable.ic_visibility_on : R.drawable.ic_visibility_off);

            binding.editTxtPassword.setTypeface(currentTypeface);
            binding.editTxtConfirmPassword.setTypeface(currentTypeface);

            binding.editTxtPassword.setSelection(binding.editTxtPassword.getText().length());
        });
    }

    private void setupEditTextFocusHandlers() {
        View.OnClickListener focusListener = v -> {
            if (v instanceof EditText) {
                v.setFocusable(true);
                v.setFocusableInTouchMode(true);
                v.requestFocus();
            }
        };
        binding.editTxtPassword.setOnClickListener(focusListener);
        binding.editTxtConfirmPassword.setOnClickListener(focusListener);

        binding.editTxtPassword.setOnFocusChangeListener((v, hasFocus) -> {
            int backgroundRes = hasFocus ? R.drawable.bg_edit_txt_custom_white_focused : R.drawable.bg_edit_txt_custom_gray_not_focused;
            binding.editTxtPasswordBackground.setBackgroundResource(backgroundRes);
        });

        binding.editTxtConfirmPassword.setOnFocusChangeListener((v, hasFocus) -> {
            int backgroundRes = hasFocus ? R.drawable.bg_edit_txt_custom_white_focused : R.drawable.bg_edit_txt_custom_gray_not_focused;
            binding.editTxtConfirmPasswordBackground.setBackgroundResource(backgroundRes);
        });
    }

    private void setupPasswordValidation() {
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String password = binding.editTxtPassword.getText().toString();
                String confirmPassword = binding.editTxtConfirmPassword.getText().toString();

                boolean shouldShowToggle = !password.isEmpty() || !confirmPassword.isEmpty();
                binding.togglePasswordVisibility.setVisibility(shouldShowToggle ? View.VISIBLE : View.INVISIBLE);

                validatePassword();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        binding.editTxtPassword.addTextChangedListener(watcher);
        binding.editTxtConfirmPassword.addTextChangedListener(watcher);
    }


    // ====================== UTILITIES ======================
    private void setupStatusBar() {
        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        window.getDecorView().setSystemUiVisibility(window.getDecorView().getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View focus = getCurrentFocus();
            if (focus instanceof EditText) {
                Rect rect = new Rect();
                focus.getGlobalVisibleRect(rect);
                if (!rect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    focus.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) imm.hideSoftInputFromWindow(focus.getWindowToken(), 0);
                    binding.main.requestFocus();
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    private void updateResetButtonState(boolean isEnabled) {
        binding.btnResetPassword.setEnabled(isEnabled);
    }
}
