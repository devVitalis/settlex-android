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
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.settlex.android.R;
import com.settlex.android.data.remote.profile.ProfileService;
import com.settlex.android.databinding.ActivityLoginBinding;
import com.settlex.android.ui.auth.viewmodel.AuthViewModel;
import com.settlex.android.ui.common.util.ProgressLoaderController;
import com.settlex.android.ui.dashboard.DashboardActivity;
import com.settlex.android.ui.info.help.AuthHelpActivity;
import com.settlex.android.utils.network.NetworkMonitor;
import com.settlex.android.utils.string.StringUtil;
import com.settlex.android.utils.ui.StatusBarUtil;
import com.settlex.android.utils.ui.UiUtil;

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
        observeUserState();
        observeNetworkStatus();
        observeLoginAndHandleResult();
    }

    // OBSERVERS =========
    private void observeNetworkStatus() {
        NetworkMonitor.getNetworkStatus().observe(this, isConnected -> {
            if (!isConnected) showNoInternetDialog();
            this.isConnected = isConnected;
        });
    }

    private void observeUserState() {
        authViewModel.getUserAuthStateLiveData().observe(this, currentUser -> {
            if (currentUser != null) {
                // user is logged in
                showLoggedInLayout(currentUser.getPhotoUrl(), currentUser.getDisplayName(), currentUser.getEmail());
                return;
            }
            showLoggedOutLayout();
        });
    }

    private void observeLoginAndHandleResult() {
        authViewModel.getLoginResult().observe(this, result -> {
            if (result != null) {
                switch (result.getStatus()) {
                    case LOADING -> onLoginLoading();
                    case SUCCESS -> onLoginSuccess();
                    case ERROR -> onLoginFailure(result.getMessage());
                }
            }
        });
    }

    private void onLoginLoading() {
        progressLoader.show();
    }

    private void onLoginSuccess() {
        startActivity(new Intent(this, DashboardActivity.class));
        finishAffinity();
        progressLoader.hide();
    }

    private void onLoginFailure(String error) {
        binding.txtError.setText(error);
        binding.txtError.setVisibility(View.VISIBLE);

        progressLoader.hide();
    }


    private void attemptLogin() {
        if (!isConnected) {
            showNoInternetDialog();
            return;
        }

        String email = Objects.requireNonNull(binding.editTxtEmail.getText()).toString().trim();
        String password = Objects.requireNonNull(binding.editTxtPassword.getText()).toString().trim();

        attemptUserLoginWithEmailAndPassword(email, password);
    }

    private void attemptUserLoginWithEmailAndPassword(String email, String password) {
        authViewModel.loginWithEmail(email, password);
    }

    private void showLoggedInLayout(String photoUrl, String displayName, String email) {
        String userDisplayName = "Hi, " + displayName.toUpperCase();
        String userEmail = "(" + email + ")";

        ProfileService.loadProfilePic(photoUrl, binding.userProfile);
        binding.userDisplayName.setText(userDisplayName);
        binding.userEmail.setText(StringUtil.maskEmail(userEmail));
        binding.editTxtEmail.setText(email);
        // Show
        binding.showCurrentUserLayout.setVisibility(View.VISIBLE);
        binding.btnFingerprint.setVisibility(View.VISIBLE);
        binding.btnSwitchAccount.setVisibility(View.VISIBLE);
        // Hide
        binding.logo.setVisibility(View.GONE);
        binding.txtInputLayoutEmail.setVisibility(View.GONE);
        binding.btnSignUp.setVisibility(View.GONE);
    }

    private void showLoggedOutLayout() {
        // sign out
        authViewModel.signOut();

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
        String title = "Network Unavailable";
        String message = "Please check your Wi-Fi or cellular data and try again";

        UiUtil.showSimpleAlertDialog(
                this,
                title,
                message);
    }

    // UI ACTIONS ==========
    private void setupUiActions() {
        StatusBarUtil.setStatusBarColor(this, R.color.white);
        setupEditTextFocusHandler();
        setupAuthActionTexts();
        setupInputValidation();
        setupPasswordVisibilityToggle();
        clearFocusOnLastEditTextField();

        // Click listeners
        binding.btnBackBefore.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        binding.btnSignUp.setOnClickListener(v -> navigateTo(SignUpActivity.class));
        binding.btnHelp.setOnClickListener(v -> navigateTo(AuthHelpActivity.class));
        binding.btnSwitchAccount.setOnClickListener(view -> showLoggedOutLayout());
        binding.btnForgotPassword.setOnClickListener(v -> navigateTo(PasswordResetActivity.class));
        binding.btnSignIn.setOnClickListener(v -> attemptLogin());
    }

    private void setupAuthActionTexts() {
        String signUpActionText = "Don't have an account yet? <font color='#0044CC'><br>Click here to register</font>";
        binding.btnSignUp.setText(Html.fromHtml(signUpActionText, Html.FROM_HTML_MODE_LEGACY));

        String switchAccountActionText = "Not you? <br><font color='#0044CC'>Switch Account</font>";
        binding.btnSwitchAccount.setText(Html.fromHtml(switchAccountActionText, Html.FROM_HTML_MODE_LEGACY));
    }

    private void navigateTo(Class<? extends Activity> activityClass) {
        startActivity(new Intent(this, activityClass));
    }

    private void setupEditTextFocusHandler() {
        int focusBgRes =  R.drawable.bg_edit_txt_custom_white_focused;
        int defaultBgRes = R.drawable.bg_edit_txt_custom_gray_not_focused;

        binding.editTxtPassword.setOnFocusChangeListener((v, hasFocus) -> binding.editTxtPasswordBg.setBackgroundResource(hasFocus ? focusBgRes : defaultBgRes));
    }

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
                binding.txtError.setVisibility(View.GONE);
                updateSignInButtonState();
            }
        };

        binding.editTxtEmail.addTextChangedListener(validationWatcher);
        binding.editTxtPassword.addTextChangedListener(validationWatcher);
        setupPasswordToggleVisibility();
    }

    private void setupPasswordToggleVisibility() {
        binding.editTxtPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

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
