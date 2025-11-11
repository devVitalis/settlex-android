package com.settlex.android.ui.auth.activity;

import android.app.Activity;
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
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.settlex.android.R;
import com.settlex.android.databinding.ActivitySetNewPasswordBinding;
import com.settlex.android.ui.auth.viewmodel.AuthViewModel;
import com.settlex.android.ui.common.util.ProgressLoaderController;
import com.settlex.android.ui.dashboard.account.SettingsActivity;
import com.settlex.android.ui.dashboard.viewmodel.UserViewModel;
import com.settlex.android.utils.event.Result;
import com.settlex.android.utils.network.NetworkMonitor;
import com.settlex.android.utils.ui.StatusBarUtil;
import com.settlex.android.utils.ui.UiUtil;

import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SetNewPasswordActivity extends AppCompatActivity {

    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;
    private boolean isCurrentPasswordVisible = false;

    // dependencies
    private ProgressLoaderController progressLoader;
    private ActivitySetNewPasswordBinding binding;
    private AuthViewModel authViewModel;
    private UserViewModel userViewModel;
    private boolean isConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySetNewPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        progressLoader = new ProgressLoaderController(this);
        progressLoader.setOverlayColor(ContextCompat.getColor(this, R.color.semi_transparent_white));

        setupUiActions();
        observeNetworkStatus();
        observeSetNewPasswordStatus();
        observeUpdatePasswordStatus();
    }

    private void setupUiActions() {
        StatusBarUtil.setStatusBarColor(this, R.color.white);
        setupEditTextFocusHandlers();
        setupPasswordValidation();
        clearFocusOnEditTextField();
        setupPasswordVisibilityToggle();

        binding.btnBackBefore.setOnClickListener(v -> finish());
        binding.btnChangePassword.setOnClickListener(v -> attemptPasswordReset());
        binding.currentPasswordContainer.setVisibility((getIntentPurpose() != null && getIntentPurpose().equals("change_password_from_settings") ? View.VISIBLE : View.GONE));
    }

    private String getIntentPurpose() {
        return getIntent().getStringExtra("purpose");
    }

    private void observeNetworkStatus() {
        NetworkMonitor.getNetworkStatus().observe(this, isConnected -> {
            if (!isConnected) UiUtil.showNoInternetAlertDialog(this);
            this.isConnected = isConnected;
        });
    }

    private void observeUpdatePasswordStatus() {
        userViewModel.getUpdatePasswordLiveData().observe(this, event -> {
            Result<String> result = event.getContentIfNotHandled();
            if (result == null) return;
            switch (result.getStatus()) {
                case LOADING -> progressLoader.show();
                case SUCCESS -> onUpdatePasswordStatusSuccess();
                case FAILURE -> onUpdatePasswordStatusFailed(result.getError());
            }
        });
    }

    private void onUpdatePasswordStatusSuccess() {
        showSuccessDialog();
        progressLoader.hide();
    }

    private void onUpdatePasswordStatusFailed(String error) {
        binding.txtCurrentPasswordError.setText(error);
        binding.txtCurrentPasswordError.setVisibility(View.VISIBLE);
        progressLoader.hide();
    }

    private void observeSetNewPasswordStatus() {
        authViewModel.getSetNewPasswordLiveData().observe(this, event -> {
            Result<String> result = event.getContentIfNotHandled();
            if (result != null) {
                switch (result.getStatus()) {
                    case LOADING -> progressLoader.show();
                    case SUCCESS -> onSetNewPasswordStatusSuccess();
                    case FAILURE -> onSetNewPasswordStatusError(result.getError());
                }
            }
        });
    }

    private void onSetNewPasswordStatusSuccess() {
        showSuccessDialog();
        progressLoader.hide();
    }

    private void onSetNewPasswordStatusError(String error) {
        binding.txtConfirmPasswordError.setText(error);
        binding.txtConfirmPasswordError.setVisibility(View.VISIBLE);
        progressLoader.hide();
    }

    private void showSuccessDialog() {
        String title = "Password Updated";
        String message = "Your password has been changed successfully. You will now need to use your new password to sign in on all your devices";
        String buttonTxt = "Continue";

        UiUtil.showSuccessBottomSheetDialog(this, (dialog, binding) -> {
            binding.title.setText(title);
            binding.message.setText(message);
            binding.btnContinue.setText(buttonTxt);
            binding.anim.playAnimation();

            binding.btnContinue.setOnClickListener(v -> {
                String purpose = getIntentPurpose();
                if (purpose != null) {
                    if (purpose.equals("forgot_password_from_settings") || purpose.equals("change_password_from_settings")) {
                        routeToDestination(SettingsActivity.class);
                    }
                }
                routeToDestination(LoginActivity.class);
                dialog.dismiss();
            });
        });
    }

    private void routeToDestination(Class<? extends Activity> activityClass) {
        startActivity(new Intent(this, activityClass));
        finish();
    }

    private void attemptPasswordReset() {
        if (!isConnected) {
            UiUtil.showNoInternetAlertDialog(this);
            return;
        }

        String email = getIntent().getStringExtra("email");
        String oldPassword = Objects.requireNonNull(binding.editTxtCurrentPassword.getText()).toString().trim();
        String newPassword = binding.editTxtPassword.getText().toString().trim();

        if (getIntentPurpose() != null && getIntentPurpose().equals("change_password_from_settings")) {
            updatePassword(email, oldPassword, newPassword);
        } else {
            setNewPassword(email, newPassword);
        }
    }

    private void setNewPassword(String email, String newPassword) {
        authViewModel.setNewPassword(email, newPassword);
    }

    private void updatePassword(String email, String oldPassword, String newPassword) {
        userViewModel.updatePassword(email, oldPassword, newPassword);
    }

    private void validatePassword() {
        String password = Objects.requireNonNull(binding.editTxtPassword.getText()).toString().trim();
        String confirm = Objects.requireNonNull(binding.editTxtConfirmPassword.getText()).toString().trim();

        boolean isValid = isPasswordRequirementsMet(password, confirm);
        updateResetButtonState(isValid);
    }

    private void updateResetButtonState(boolean isEnabled) {
        binding.btnChangePassword.setEnabled(isEnabled);
    }

    private boolean isPasswordRequirementsMet(String password, String confirm) {
        boolean hasLength = password.length() >= 8;
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasSpecial = password.matches(".*[@#$%^&+=!.].*");
        boolean matches = password.equals(confirm);

        if (!confirm.isEmpty() && !matches) {
            String ERROR_PASSWORD_MISMATCH = "Passwords do not match!";
            binding.txtConfirmPasswordError.setText(ERROR_PASSWORD_MISMATCH);
            binding.txtConfirmPasswordError.setVisibility(View.VISIBLE);
        } else {
            binding.txtConfirmPasswordError.setVisibility(View.GONE);
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
            binding.editTxtPassword.setSelection(binding.editTxtPassword.length());
        });

        binding.toggleConfirmPasswordVisibility.setOnClickListener(v -> {
            Typeface currentTypeface = binding.editTxtPassword.getTypeface();

            isConfirmPasswordVisible = !isConfirmPasswordVisible;
            int inputType = isConfirmPasswordVisible ? InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD : InputType.TYPE_TEXT_VARIATION_PASSWORD;

            binding.editTxtConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | inputType);
            binding.toggleConfirmPasswordVisibility.setImageResource(isConfirmPasswordVisible ? icVisibleOn : icVisibleOff);

            binding.editTxtConfirmPassword.setTypeface(currentTypeface);
            binding.editTxtConfirmPassword.setSelection(binding.editTxtConfirmPassword.length());
        });

        binding.toggleCurrentPasswordVisibility.setOnClickListener(v -> {
            Typeface currentTypeface = binding.editTxtCurrentPassword.getTypeface();

            isCurrentPasswordVisible = !isCurrentPasswordVisible;
            int inputType = isCurrentPasswordVisible ? InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD : InputType.TYPE_TEXT_VARIATION_PASSWORD;

            binding.editTxtCurrentPassword.setInputType(InputType.TYPE_CLASS_TEXT | inputType);
            binding.toggleCurrentPasswordVisibility.setImageResource(isCurrentPasswordVisible ? icVisibleOn : icVisibleOff);

            binding.editTxtCurrentPassword.setTypeface(currentTypeface);
            binding.editTxtCurrentPassword.setSelection(binding.editTxtCurrentPassword.length());
        });
    }

    private void setupEditTextFocusHandlers() {
        int focusBgRes = R.drawable.bg_edit_txt_custom_white_focused;
        int defaultBgRes = R.drawable.bg_edit_txt_custom_gray_not_focused;

        binding.editTxtCurrentPassword.setOnFocusChangeListener((v, hasFocus) -> binding.editTxtCurrentPasswordBg.setBackgroundResource(hasFocus ? focusBgRes : defaultBgRes));
        binding.editTxtPassword.setOnFocusChangeListener((v, hasFocus) -> binding.editTxtPasswordBg.setBackgroundResource(hasFocus ? focusBgRes : defaultBgRes));
        binding.editTxtConfirmPassword.setOnFocusChangeListener((v, hasFocus) -> binding.editTxtConfirmPasswordBg.setBackgroundResource(hasFocus ? focusBgRes : defaultBgRes));
    }

    private void setupPasswordValidation() {
        binding.editTxtCurrentPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {

            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence currentPassword, int i, int i1, int i2) {
                binding.txtConfirmPasswordError.setVisibility(View.GONE);
                binding.toggleCurrentPasswordVisibility.setVisibility(!currentPassword.toString().isEmpty() ? View.VISIBLE : View.INVISIBLE);

            }
        });

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

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
