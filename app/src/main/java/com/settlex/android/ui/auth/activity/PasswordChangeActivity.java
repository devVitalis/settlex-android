package com.settlex.android.ui.auth.activity;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;

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
import android.text.TextUtils;
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
import com.settlex.android.ui.auth.util.AuthResult;
import com.settlex.android.ui.auth.viewmodel.AuthViewModel;
import com.settlex.android.ui.common.SettleXProgressBarController;
import com.settlex.android.ui.dashboard.DashboardActivity;

import java.util.Objects;

public class PasswordChangeActivity extends AppCompatActivity {
    private SettleXProgressBarController progressBarController;
    private ActivityPasswordChangeBinding binding;
    private AuthViewModel authViewModel;

    boolean isPasswordVisible = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_change);
        binding = ActivityPasswordChangeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        progressBarController = new SettleXProgressBarController(binding.getRoot());

        setupStatusBar();
        setupUiActions();

        requestPasswordChangeObserver();
    }

    private void requestPasswordChangeObserver() {
        authViewModel.getPasswordResetResult().observe(this, event -> {
            AuthResult<String> result = event.getContentIfNotHandled();
            if (result != null) {
                switch (result.getStatus()) {
                    case LOADING -> progressBarController.show();
                    case SUCCESS -> handleSuccess();
                    case ERROR -> handleError(result.getMessage());
                }
            }
        });
    }

    private void handleSuccess() {
        Intent intent = new Intent(this, DashboardActivity.class);
        intent.addFlags(FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

        progressBarController.hide();
    }

    private void handleError(String reason) {
        binding.txtErrorFeedback.setText(reason);
        binding.txtErrorFeedback.setVisibility(View.VISIBLE);
        progressBarController.hide();
    }

    private void setupUiActions() {
        reEnableEditTextFocus();
        setupPasswordValidation();
        togglePasswordVisibility();
        setupEditTxtEmailFocusHandler();

        binding.imgBackBefore.setOnClickListener(v -> finish());
        binding.btnResetPassword.setOnClickListener(v -> requestPasswordChange());
    }

    private void requestPasswordChange() {
        String email = getIntent().getStringExtra("email");
        String newPassword = Objects.requireNonNull(binding.editTxtPassword.getText()).toString().trim();

        authViewModel.requestPasswordReset(email, newPassword);
    }

    /*--------------------------------
    Enable focus on tap for EditTexts
    ---------------------------------*/
    private void reEnableEditTextFocus() {
        View.OnClickListener enableFocusListener = v -> {
            if (v instanceof EditText) {
                v.setFocusable(true);
                v.setFocusableInTouchMode(true);
                v.requestFocus();
            }
        };
        binding.editTxtPassword.setOnClickListener(enableFocusListener);
        binding.editTxtConfirmPassword.setOnClickListener(enableFocusListener);
    }

    /*-------------------------------------------
    Enable Dynamic Stroke Color on editText bg
    -------------------------------------------*/
    private void setupEditTxtEmailFocusHandler() {
        binding.editTxtPassword.setOnFocusChangeListener((view, hasFocus) ->
                binding.editTxtPasswordBg.setBackgroundResource((hasFocus)
                        ? R.drawable.bg_edit_txt_custom_white_focused
                        : R.drawable.bg_edit_txt_custom_gray_not_focused));

        binding.editTxtConfirmPassword.setOnFocusChangeListener((view, hasFocus) ->
                binding.editTxtConfirmPasswordBg.setBackgroundResource((hasFocus)
                        ? R.drawable.bg_edit_txt_custom_white_focused
                        : R.drawable.bg_edit_txt_custom_gray_not_focused));
    }

    /**
     * Validates the password against a set of rules.
     */
    private void validatePassword() {
        String password = Objects.requireNonNull(binding.editTxtPassword.getText()).toString().trim();
        String confirmPassword = Objects.requireNonNull(binding.editTxtConfirmPassword.getText()).toString().trim();

        boolean hasLength = password.length() >= 8;
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasSpecial = password.matches(".*[@#$%^&+=!.].*");
        boolean matches = password.equals(confirmPassword);
        boolean allValid = hasLength && hasUpper && hasLower && hasSpecial && matches;

        if (!confirmPassword.isEmpty() && !matches) {
            binding.txtErrorFeedback.setText(getString(R.string.error_password_mismatch));
            binding.txtErrorFeedback.setVisibility(View.VISIBLE);
        } else {
            binding.txtErrorFeedback.setVisibility(View.GONE);
        }

        updateCreateAccountButtonState(allValid);
        showPasswordRequirements(hasLength, hasUpper, hasLower, hasSpecial, password);
    }

    private void updateCreateAccountButtonState(boolean allValid) {
        binding.btnResetPassword.setEnabled(allValid);
    }

    private void showPasswordRequirements(boolean hasLength, boolean hasUpper, boolean hasLower, boolean hasSpecial, String password) {
        SpannableStringBuilder requirements = new SpannableStringBuilder();
        appendRequirement(requirements, hasLength, "At least 8 characters");
        appendRequirement(requirements, hasUpper, "Contains uppercase letter");
        appendRequirement(requirements, hasLower, "Contains lowercase letter");
        appendRequirement(requirements, hasSpecial, "Contains special character (e.g. @#$%^&;+=!.)");

        boolean shouldShowPasswordPrompt = password.isEmpty() || (hasLength && hasUpper && hasLower && hasSpecial);

        binding.txtPasswordPrompt.setVisibility((!shouldShowPasswordPrompt) ? View.VISIBLE : View.GONE);
        binding.txtPasswordPrompt.setText(requirements);
    }

    /**
     * Appends a password requirement with a corresponding icon.
     */
    private void appendRequirement(SpannableStringBuilder builder, boolean isMet, String text) {
        Drawable icon = ContextCompat.getDrawable(this, isMet ? R.drawable.ic_checkbox_checked : R.drawable.ic_checkbox_unchecked);
        if (icon != null) {
            int size = (int) (binding.txtPasswordPrompt.getTextSize() * 1.2f);
            icon.setBounds(0, 0, size, size);
            builder.append(" ");
            builder.setSpan(new ImageSpan(icon, ImageSpan.ALIGN_BOTTOM),
                    builder.length() - 1, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.append(" ").append(text).append("\n");
        }
    }

    /**
     * Sets up a TextWatcher for password validation.
     */
    private void setupPasswordValidation() {
        TextWatcher passwordWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String password = binding.editTxtPassword.getText().toString().trim();
                String confirmPassword = binding.editTxtConfirmPassword.getText().toString().trim();
                boolean shouldShowToggle = (!password.isEmpty() || !confirmPassword.isEmpty());
                binding.togglePasswordVisibility.setVisibility((shouldShowToggle) ? View.VISIBLE : View.INVISIBLE);
                validatePassword();
            }
        };

        binding.editTxtPassword.addTextChangedListener(passwordWatcher);
        binding.editTxtConfirmPassword.addTextChangedListener(passwordWatcher);
    }

    private void togglePasswordVisibility() {
        binding.togglePasswordVisibility.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            Typeface currentTypeface = binding.editTxtPassword.getTypeface();
            if (isPasswordVisible) {
                // Show password
                binding.editTxtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                binding.editTxtConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                binding.togglePasswordVisibility.setImageResource(R.drawable.ic_visibility_on); // visible icon
            } else {
                // Hide password
                binding.editTxtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                binding.editTxtConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                binding.togglePasswordVisibility.setImageResource(R.drawable.ic_visibility_lock); // hidden icon
            }
            binding.editTxtPassword.setTypeface(currentTypeface);
            binding.editTxtConfirmPassword.setTypeface(currentTypeface);
            // Keep cursor at the end
            binding.editTxtPassword.setSelection(binding.editTxtPassword.getText().length());
        });

    }

    // Dismiss keyboard and clear focus on outside tap
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
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
                    }

                    binding.main.requestFocus();
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    private void setupStatusBar() {
        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.white));

        View decorView = window.getDecorView();
        decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }
}