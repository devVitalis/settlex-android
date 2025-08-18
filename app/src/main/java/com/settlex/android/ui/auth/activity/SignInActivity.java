package com.settlex.android.ui.auth.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import com.settlex.android.databinding.ActivitySignInBinding;
import com.settlex.android.ui.auth.viewmodel.AuthViewModel;
import com.settlex.android.ui.common.SettleXProgressBarController;
import com.settlex.android.ui.dashboard.DashboardActivity;
import com.settlex.android.util.StringUtil;

import java.util.Objects;

/**
 * Handles user sign-in flow including:
 */
public class SignInActivity extends AppCompatActivity {
    private AuthViewModel authViewModel;
    private ActivitySignInBinding binding;
    private SettleXProgressBarController progressBarController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        progressBarController = new SettleXProgressBarController(binding.getRoot());

        setupStatusBar();
        setupUiActions();
        observeUserState();
        observeLoginResult();
    }

    // ====================== CORE LOGIN FLOW ======================
    private void observeUserState() {
        authViewModel.getUserState().observe(this, currentUser -> {
            if (currentUser != null) {
                showLoggedInLayout(currentUser.getDisplayName(), currentUser.getEmail());
            } else {
                showLoggedOutLayout();
            }
        });
    }

    private void observeLoginResult() {
        authViewModel.getLoginResult().observe(this, result -> {
            if (result != null) {
                switch (result.getStatus()) {
                    case LOADING -> progressBarController.show();
                    case SUCCESS -> onLoginSuccess();
                    case ERROR -> onLoginFailure(result.getMessage());
                }
            }
        });
    }

    private void onLoginSuccess() {
        startActivity(new Intent(this, DashboardActivity.class));
        finish();
        progressBarController.hide();
    }

    private void onLoginFailure(String error) {
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        progressBarController.hide();
    }

    private void attemptLogin() {
        String email = Objects.requireNonNull(binding.editTxtEmail.getText()).toString().trim();
        String password = Objects.requireNonNull(binding.editTxtPassword.getText()).toString().trim();

        authViewModel.loginWithEmail(email, password);
    }

    private void showLoggedInLayout(String firstName, String email){
        binding.userAccount.setText("Hi, " + firstName + "\n" + StringUtil.maskEmail(email));
        // Show
        binding.showUserInfoLayout.setVisibility(View.VISIBLE);
        binding.showBiometricsLayout.setVisibility(View.VISIBLE);
        // Hide
        binding.editTxtEmailBg.setVisibility(View.GONE);
        binding.logo.setVisibility(View.GONE);
    }

    private void showLoggedOutLayout(){
        // Hide
        binding.showUserInfoLayout.setVisibility(View.GONE);
        binding.showBiometricsLayout.setVisibility(View.GONE);
        // Show
        binding.editTxtEmailBg.setVisibility(View.VISIBLE);
    }

    // ====================== UI ACTIONS ======================
    private void setupUiActions() {
        setupFocusHandlers();
        formatSignUpText();
        setupInputValidation();

        // Initial UI states
        binding.txtInputLayoutPassword.setEndIconVisible(false);

        // Click listeners
        binding.imgBackBefore.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        binding.btnSignUp.setOnClickListener(v -> navigateTo(SignUpActivity.class));
        binding.btnHelp.setOnClickListener(v -> navigateTo(PasswordChangeActivity.class));
        binding.btnForgotPassword.setOnClickListener(v -> navigateTo(PasswordResetActivity.class));
        binding.btnSignIn.setOnClickListener(v -> attemptLogin());
    }

    private void formatSignUpText() {
        String signUpText = "Don't have an account yet? <font color='#0044CC'><br>Click here to register</font>";
        binding.btnSignUp.setText(Html.fromHtml(signUpText, Html.FROM_HTML_MODE_LEGACY));
    }

    // ====================== INPUT HANDLING ======================
    private void setupInputValidation() {
        TextWatcher validationWatcher = new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateInputFields();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        binding.editTxtEmail.addTextChangedListener(validationWatcher);
        binding.editTxtPassword.addTextChangedListener(validationWatcher);

        // Password visibility icon toggle
        binding.editTxtPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.txtInputLayoutPassword.setEndIconVisible(!TextUtils.isEmpty(s));
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
     * Validates both email and password fields to enable/disable login button
     */
    private void validateInputFields() {
        String email = Objects.requireNonNull(binding.editTxtEmail.getText()).toString().trim();
        String password = Objects.requireNonNull(binding.editTxtPassword.getText()).toString().trim();

        binding.btnSignIn.setEnabled(!email.isEmpty() && !password.isEmpty());
    }

    // ====================== FOCUS MANAGEMENT ======================
    private void setupFocusHandlers() {
        View.OnClickListener focusListener = v -> {
            if (v instanceof EditText editText) {
                editText.setFocusable(true);
                editText.setFocusableInTouchMode(true);
                editText.requestFocus();
            }
        };
        binding.editTxtEmail.setOnClickListener(focusListener);
        binding.editTxtPassword.setOnClickListener(focusListener);

        binding.editTxtEmail.setOnFocusChangeListener((v, hasFocus) -> {
            int backgroundRes = hasFocus ? R.drawable.bg_edit_txt_custom_gray_focused : R.drawable.bg_edit_txt_custom_gray_not_focused;
            binding.editTxtEmailBg.setBackgroundResource(backgroundRes);
        });
    }

    // ====================== UTILITIES ======================

    private void navigateTo(Class<? extends Activity> activityClass) {
        startActivity(new Intent(this, activityClass));
    }

    private void setupStatusBar() {
        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        View decorView = window.getDecorView();
        decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }

    /**
     * Handles keyboard dismissal when tapping outside EditText fields
     */
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
}
