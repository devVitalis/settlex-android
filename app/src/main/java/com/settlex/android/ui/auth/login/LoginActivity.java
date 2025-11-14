package com.settlex.android.ui.auth.login;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.settlex.android.R;
import com.settlex.android.data.remote.profile.ProfileService;
import com.settlex.android.databinding.ActivityLoginBinding;
import com.settlex.android.ui.auth.forgot_password.ForgotPasswordActivity;
import com.settlex.android.ui.auth.register.SignUpActivity;
import com.settlex.android.ui.auth.AuthViewModel;
import com.settlex.android.ui.common.components.BiometricAuthHelper;
import com.settlex.android.util.ui.ProgressLoaderController;
import com.settlex.android.ui.dashboard.DashboardActivity;
import com.settlex.android.ui.info.help.AuthHelpActivity;
import com.settlex.android.util.network.NetworkMonitor;
import com.settlex.android.util.string.StringFormatter;
import com.settlex.android.util.ui.StatusBar;
import com.settlex.android.ui.common.util.DialogHelper;

import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginActivity extends AppCompatActivity {
    private boolean isPasswordVisible = false;

    // dependencies
    private ProgressLoaderController progressLoader;
    private ActivityLoginBinding binding;
    private AuthViewModel authViewModel;
    private boolean isConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        progressLoader = new ProgressLoaderController(this);

        setupUiActions();
        syncUserStateWithUI();
        observeNetworkStatus();
        observeLoginStatus();
    }

    private void setupUiActions() {
        StatusBar.setStatusBarColor(this, R.color.white);
        setupEditTextFocusHandler();
        setupAuthActionTexts();
        setupInputValidation();
        setupPasswordVisibilityToggle();
        clearFocusOnLastEditTextField();

        // Click listeners
        binding.btnBackBefore.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        binding.btnSignUp.setOnClickListener(v -> navigateTo(SignUpActivity.class));
        binding.btnHelp.setOnClickListener(v -> navigateTo(AuthHelpActivity.class));
        binding.btnForgotPassword.setOnClickListener(v -> navigateTo(ForgotPasswordActivity.class));
        binding.btnSignIn.setOnClickListener(v -> attemptLogin());
        binding.btnSwitchAccount.setOnClickListener(v -> {
            authViewModel.signOut();
            showLoggedOutLayout();
        });
        binding.btnFingerprint.setOnClickListener(v -> promptBiometricsAuth());
    }

    private void syncUserStateWithUI() {
        LoginUiModel currentUser = authViewModel.getCurrentUserLiveData().getValue();
        if (currentUser == null) {
            showLoggedOutLayout();
            return;
        }

        // user is logged in
        showLoggedInLayout(
                currentUser.getPhotoUrl(),
                currentUser.getDisplayName(),
                currentUser.getEmail());

        // check user pref
        boolean isFingerPrintEnabled = Boolean.TRUE.equals(authViewModel.getLoginBiometricsEnabled().getValue());
        binding.btnFingerprint.setVisibility(isFingerPrintEnabled ? View.VISIBLE : View.GONE);
        if (isFingerPrintEnabled) promptBiometricsAuth();
    }

    private void promptBiometricsAuth() {
        if (BiometricAuthHelper.isBiometricAvailable(this)) {
            BiometricAuthHelper biometric = new BiometricAuthHelper(
                    this,
                    this,
                    new BiometricAuthHelper.BiometricAuthCallback() {
                        @Override
                        public void onAuthenticated() {
                            navigateTo(DashboardActivity.class);
                        }

                        @Override
                        public void onError(java.lang.String message) {
                        }

                        @Override
                        public void onFailed() {
                        }
                    });
            biometric.authenticate("Confirm your identity", "Use Password");
        }
    }

    // OBSERVERS ====
    private void observeNetworkStatus() {
        NetworkMonitor.getNetworkStatus().observe(this, isConnected -> {
            if (!isConnected) showNoInternetDialog();
            this.isConnected = isConnected;
        });
    }

    private void observeLoginStatus() {
        authViewModel.getLoginLiveData().observe(this, result -> {
            if (result != null) {
                switch (result.getStatus()) {
                    case LOADING -> progressLoader.show();
                    case SUCCESS -> onLoginStatusSuccess();
                    case FAILURE -> onLoginStatusError(result.getError());
                }
            }
        });
    }

    private void onLoginStatusSuccess() {
        startActivity(new Intent(this, DashboardActivity.class));
        finishAffinity();
        progressLoader.hide();
    }

    private void onLoginStatusError(java.lang.String error) {
        binding.txtError.setText(error);
        binding.txtError.setVisibility(View.VISIBLE);

        progressLoader.hide();
    }

    private void attemptLogin() {
        if (!isConnected) {
            showNoInternetDialog();
            return;
        }

        java.lang.String email = Objects.requireNonNull(binding.editTxtEmail.getText()).toString().trim();
        java.lang.String password = Objects.requireNonNull(binding.editTxtPassword.getText()).toString().trim();

        attemptUserLoginWithEmailAndPassword(email, password);
    }

    private void attemptUserLoginWithEmailAndPassword(java.lang.String email, java.lang.String password) {
        authViewModel.loginWithEmail(email, password);
    }

    private void showLoggedInLayout(java.lang.String photoUrl, java.lang.String displayName, java.lang.String email) {
        java.lang.String userDisplayName = "Hi, " + displayName.toUpperCase();
        java.lang.String userEmail = "(" + email + ")";

        ProfileService.loadProfilePic(photoUrl, binding.userProfile);
        binding.userDisplayName.setText(userDisplayName);
        binding.userEmail.setText(StringFormatter.maskEmail(userEmail));
        binding.editTxtEmail.setText(email);

        // Show
        binding.showCurrentUserLayout.setVisibility(View.VISIBLE);
        binding.btnSwitchAccount.setVisibility(View.VISIBLE);

        // Hide
        binding.logo.setVisibility(View.GONE);
        binding.txtInputLayoutEmail.setVisibility(View.GONE);
        binding.btnSignUp.setVisibility(View.GONE);
    }

    private void showLoggedOutLayout() {
        // Hide
        binding.showCurrentUserLayout.setVisibility(View.GONE);
        binding.btnFingerprint.setVisibility(View.GONE);
        binding.btnSwitchAccount.setVisibility(View.GONE);

        // Show
        binding.txtInputLayoutEmail.setVisibility(View.VISIBLE);
        binding.btnSignUp.setVisibility(View.VISIBLE);
        binding.logo.setVisibility(View.VISIBLE);
    }

    private void showNoInternetDialog() {
        java.lang.String title = "Network Unavailable";
        java.lang.String message = "Please check your Wi-Fi or cellular data and try again";

        DialogHelper.showSimpleAlertDialog(
                this,
                title,
                message);
    }

    private void setupAuthActionTexts() {
        java.lang.String fullText = "Don't have an account yet?\nClick here to register";
        java.lang.String textToHighlight = "Click here to register";

        int startIndex = fullText.indexOf(textToHighlight);
        int endIndex = fullText.length();

        SpannableString signUpText = new SpannableString(fullText);
        signUpText.setSpan(
                new ForegroundColorSpan(Color.parseColor("#0044CC")),
                startIndex,
                endIndex,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        binding.btnSignUp.setText(signUpText);

        setupSwitchAccount();
    }

    private void setupSwitchAccount() {
        java.lang.String fullText = "Not you?\nSwitch Account";
        java.lang.String textToHighlight = "Switch Account";

        int startIndex = fullText.indexOf(textToHighlight);
        int endIndex = startIndex + textToHighlight.length();

        SpannableString switchText = new SpannableString(fullText);
        switchText.setSpan(
                new ForegroundColorSpan(Color.parseColor("#0044CC")),
                startIndex,
                endIndex,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        binding.btnSwitchAccount.setText(switchText);
    }

    private void navigateTo(Class<? extends Activity> activityClass) {
        startActivity(new Intent(this, activityClass));
    }

    private void setupEditTextFocusHandler() {
        int focusBgRes = R.drawable.bg_edit_txt_custom_white_focused;
        int defaultBgRes = R.drawable.bg_edit_txt_custom_gray_not_focused;

        binding.editTxtPassword.setOnFocusChangeListener((v, hasFocus) -> binding.editTxtPasswordBg.setBackgroundResource(hasFocus ? focusBgRes : defaultBgRes));
    }

    private void updateSignInButtonState() {
        java.lang.String email = Objects.requireNonNull(binding.editTxtEmail.getText()).toString().trim();
        java.lang.String password = Objects.requireNonNull(binding.editTxtPassword.getText()).toString().trim();

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
                java.lang.String password = binding.editTxtPassword.getText().toString().trim();
                binding.passwordToggle.setVisibility((!password.isEmpty()) ? View.VISIBLE : View.GONE);
                binding.txtError.setVisibility(View.GONE);
                updateSignInButtonState();
            }
        };

        binding.editTxtEmail.addTextChangedListener(validationWatcher);
        binding.editTxtPassword.addTextChangedListener(validationWatcher);
    }

    private void setupPasswordVisibilityToggle() {
        binding.passwordToggle.setOnClickListener(v -> {
            Typeface currentTypeface = binding.editTxtPassword.getTypeface();
            isPasswordVisible = !isPasswordVisible;

            int inputType = isPasswordVisible ? InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD : InputType.TYPE_TEXT_VARIATION_PASSWORD;

            binding.editTxtPassword.setInputType(InputType.TYPE_CLASS_TEXT | inputType);
            binding.passwordToggle.setImageResource(isPasswordVisible ? R.drawable.ic_visibility_on_filled : R.drawable.ic_visibility_off_filled);

            binding.editTxtPassword.setTypeface(currentTypeface);
            binding.editTxtPassword.setSelection(binding.editTxtPassword.getText().length());
        });
    }

    private void clearFocusOnLastEditTextField() {
        binding.editTxtPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // Hide the keyboard
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                // Clear focus
                v.clearFocus();
                return true;
            }
            return false;
        });
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
}
