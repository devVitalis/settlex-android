package com.settlex.android.ui.auth.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.settlex.android.R;
import com.settlex.android.databinding.ActivitySignInBinding;
import com.settlex.android.ui.info.help.AuthHelpActivity;
import com.settlex.android.ui.auth.viewmodel.AuthViewModel;
import com.settlex.android.ui.common.util.SettleXProgressBarController;
import com.settlex.android.ui.dashboard.activity.DashboardActivity;
import com.settlex.android.util.network.NetworkMonitor;
import com.settlex.android.util.string.StringUtil;
import com.settlex.android.util.ui.UiUtil;

import java.util.Objects;

/**
 * Handles user sign-in flow including:
 */
public class SignInActivity extends AppCompatActivity {
    private boolean isPasswordVisible = false;
    private boolean isConnected = false;

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
        observeNetworkStatus();
        observeLoginResult();
    }

    // ====================== NETWORK & DATA OBSERVERS ======================
    private void observeUserState() {
        authViewModel.getUserState().observe(this, currentUser -> {
            if (currentUser != null) {
                showLoggedInLayout(currentUser.getDisplayName(), currentUser.getEmail());
            } else {
                showLoggedOutLayout();
            }
        });
    }

    private void observeNetworkStatus() {
        NetworkMonitor.getNetworkStatus().observe(this, isConnected ->
                this.isConnected = isConnected);
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

    // ====================== LOGIN FLOW ======================
    private void onLoginSuccess() {
        startActivity(new Intent(this, DashboardActivity.class));
        finishAffinity();
        progressBarController.hide();
    }

    private void onLoginFailure(String error) {
        binding.txtErrorFeedback.setText(error);
        binding.txtErrorFeedback.setVisibility(View.VISIBLE);

        progressBarController.hide();
    }

    private void onNoInternetConnection() {
        UiUtil.showInfoDialog(
                this,
                "Network Unavailable",
                "Please check your network connection and try again",
                null);
    }

    private void attemptLogin() {
        if (isConnected) {
            String email = Objects.requireNonNull(binding.editTxtEmail.getText()).toString().trim();
            String password = Objects.requireNonNull(binding.editTxtPassword.getText()).toString().trim();

            authViewModel.loginWithEmail(email, password);
        } else {
            onNoInternetConnection();
        }
    }

    private void showLoggedInLayout(String firstName, String email) {
        binding.userDisplayName.setText(getString(R.string.greeting_with_firstName, firstName));
        binding.userEmail.setText(getString(R.string.mask_email_with_parathesis, StringUtil.maskEmail(email)));
        binding.editTxtEmail.setText(email);
        // Show
        binding.showUserInfoLayout.setVisibility(View.VISIBLE);
        binding.showBiometricsLayout.setVisibility(View.VISIBLE);
        binding.btnSwitchAccount.setVisibility(View.VISIBLE);
        // Hide
        binding.logo.setVisibility(View.GONE);
        binding.txtInputLayoutEmail.setVisibility(View.GONE);
        binding.btnSignUp.setVisibility(View.GONE);
    }

    private void showLoggedOutLayout() {
        // Hide
        binding.showUserInfoLayout.setVisibility(View.GONE);
        binding.showBiometricsLayout.setVisibility(View.GONE);
        binding.btnSwitchAccount.setVisibility(View.GONE);
        // Show
        binding.txtInputLayoutEmail.setVisibility(View.VISIBLE);
        binding.btnSignUp.setVisibility(View.VISIBLE);
        binding.logo.setVisibility(View.VISIBLE);
    }

    // ====================== UI COMPONENT SETUP ======================
    private void setupUiActions() {
        setupFocusHandlers();
        setupAuthActionTexts();
        setupInputValidation();
        setupPasswordVisibilityToggle();

        // Click listeners
        binding.imgBackBefore.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        binding.btnSignUp.setOnClickListener(v -> navigateTo(SignUpActivity.class));
        binding.btnHelp.setOnClickListener(v -> navigateTo(AuthHelpActivity.class));
        binding.btnSwitchAccount.setOnClickListener(view -> showLoggedOutLayout());
        binding.btnForgotPassword.setOnClickListener(v -> navigateTo(PasswordResetActivity.class));
        binding.btnSignIn.setOnClickListener(v -> attemptLogin());
    }

    private void setupAuthActionTexts() {
        String signUpActionText = "Don't have an account yet? <font color='#0044CC'><br>Click here to register</font>";
        binding.btnSignUp.setText(Html.fromHtml(signUpActionText, Html.FROM_HTML_MODE_LEGACY));

        String switchAccountActionText = "Not you? <font color='#0044CC'>Switch Account</font>";
        binding.btnSwitchAccount.setText(Html.fromHtml(switchAccountActionText, Html.FROM_HTML_MODE_LEGACY));
    }

    private void navigateTo(Class<? extends Activity> activityClass) {
        startActivity(new Intent(this, activityClass));
    }

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

        binding.editTxtPassword.setOnFocusChangeListener((v, hasFocus) -> {
            int backgroundRes = (hasFocus) ? R.drawable.bg_edit_txt_custom_white_focused : R.drawable.bg_edit_txt_custom_gray_not_focused;
            binding.editTxtPasswordBackground.setBackgroundResource(backgroundRes);
        });
    }

    // ====================== PASSWORD VALIDATION ======================
    private void updateSignInButtonState() {
        String email = Objects.requireNonNull(binding.editTxtEmail.getText()).toString().trim();
        String password = Objects.requireNonNull(binding.editTxtPassword.getText()).toString().trim();

        binding.btnSignIn.setEnabled(!email.isEmpty() && !password.isEmpty());
    }

    private void setupInputValidation() {
        TextWatcher validationWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.txtErrorFeedback.setVisibility(View.GONE);
                updateSignInButtonState();
            }
        };

        binding.editTxtEmail.addTextChangedListener(validationWatcher);
        binding.editTxtPassword.addTextChangedListener(validationWatcher);
        setupPasswordToggleVisibility();
    }

    private void setupPasswordToggleVisibility(){
        binding.editTxtPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.passwordToggle.setVisibility(!TextUtils.isEmpty(s) ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void setupPasswordVisibilityToggle() {
        binding.passwordToggle.setOnClickListener(v -> {
            Typeface currentTypeface = binding.editTxtPassword.getTypeface();
            isPasswordVisible = !isPasswordVisible;

            int inputType = isPasswordVisible ? InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD : InputType.TYPE_TEXT_VARIATION_PASSWORD;

            binding.editTxtPassword.setInputType(InputType.TYPE_CLASS_TEXT | inputType);
            binding.passwordToggle.setImageResource(isPasswordVisible ? R.drawable.ic_visibility_on : R.drawable.ic_visibility_off);

            binding.editTxtPassword.setTypeface(currentTypeface);
            binding.editTxtPassword.setSelection(binding.editTxtPassword.getText().length());
        });
    }

    // ====================== UTILITIES ======================
    private void setupStatusBar() {
        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        View decorView = window.getDecorView();
        decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
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
}
