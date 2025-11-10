package com.settlex.android.ui.dashboard.account;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.settlex.android.R;
import com.settlex.android.databinding.ActivitySettingsBinding;
import com.settlex.android.ui.auth.activity.SetNewPasswordActivity;
import com.settlex.android.ui.auth.viewmodel.AuthViewModel;
import com.settlex.android.ui.common.components.BiometricAuthHelper;
import com.settlex.android.ui.common.components.OtpVerificationActivity;
import com.settlex.android.ui.dashboard.model.UserUiModel;
import com.settlex.android.ui.dashboard.util.DashboardUiUtil;
import com.settlex.android.ui.dashboard.viewmodel.UserViewModel;
import com.settlex.android.utils.event.Result;
import com.settlex.android.utils.network.NetworkMonitor;
import com.settlex.android.utils.string.StringUtil;
import com.settlex.android.utils.ui.StatusBarUtil;
import com.settlex.android.utils.ui.UiUtil;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SettingsActivity extends AppCompatActivity {
    private String userEmail;

    // dependencies
    private ActivitySettingsBinding binding;
    private UserViewModel userViewModel;
    private AuthViewModel authViewModel;
    private boolean isInternetConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        initObservers();
        setupUiActions();
    }

    private void initObservers() {
        observeNetworkStatus();
        observeUserData();
        observePayBiometricEnabled();
        observeLoginBiometricEnabled();
    }

    private void setupUiActions() {
        StatusBarUtil.setStatusBarColor(this, R.color.white);

        binding.btnBackBefore.setOnClickListener(view -> finish());

        binding.btnForgotPassword.setOnClickListener(view -> showOtpConfirmationDialog(true));
        binding.btnChangePassword.setOnClickListener(view -> {
            Intent intent = new Intent(this, SetNewPasswordActivity.class);
            intent.putExtra("email", userEmail);
            intent.putExtra("purpose", "change_password_from_settings");
            startActivity(intent);
        });

        binding.btnCreatePaymentPin.setOnClickListener(view -> startActivity(new Intent(this, CreatePaymentPinActivity.class)));
        binding.btnForgotPaymentPin.setOnClickListener(view -> showOtpConfirmationDialog(false));
        binding.btnChangePaymentPin.setOnClickListener(view -> {
            Intent intent = new Intent(this, CreatePaymentPinActivity.class);
            intent.putExtra("purpose", "change_payment_pin");
            startActivity(intent);
        });
    }

    // Observers
    private void observeNetworkStatus() {
        NetworkMonitor.getNetworkStatus().observe(this, isConnected -> this.isInternetConnected = isConnected);
    }

    private void observeUserData() {
        userViewModel.getUserLiveData().observe(this, result -> {
            if (result != null && result.getStatus() == Result.Status.SUCCESS) {

                UserUiModel user = result.getData();
                this.userEmail = result.getData().getEmail();

                binding.btnCreatePaymentPin.setVisibility(!user.hasPin() ? View.VISIBLE : View.GONE);
                binding.btnChangePaymentPin.setVisibility(!user.hasPin() ? View.GONE : View.VISIBLE);

                shouldEnableBiometricOption(user.hasPin());
            }
        });
    }

    private void observePayBiometricEnabled() {
        userViewModel.getPayBiometricsEnabled().observe(this, isEnabled -> {
            // UI is updating
            binding.btnSwitchPayWithBiometrics.setOnCheckedChangeListener(null);
            binding.btnSwitchPayWithBiometrics.setChecked(isEnabled);

            binding.btnSwitchPayWithBiometrics.setOnCheckedChangeListener((btn, isChecked) -> promptPayBiometricsAuth(isChecked));
        });
    }

    private void observeLoginBiometricEnabled() {
        userViewModel.getLoginBiometricsEnabled().observe(this, isEnabled -> {
            binding.btnSwitchLoginWithBiometrics.setOnCheckedChangeListener(null);
            binding.btnSwitchLoginWithBiometrics.setChecked(isEnabled);

            binding.btnSwitchLoginWithBiometrics.setOnCheckedChangeListener((btn, isChecked) -> promptLoginBiometricsAuth(isChecked));
        });
    }

    private void shouldEnableBiometricOption(boolean hasPin) {
        boolean isBiometricAvailable = BiometricAuthHelper.isBiometricAvailable(this);

        // Payment biometrics
        binding.btnSwitchPayWithBiometrics.setEnabled(isBiometricAvailable && hasPin);
        binding.payWithBiometricsError.setText(BiometricAuthHelper.getBiometricFeedback(this));
        binding.payWithBiometricsError.setVisibility(!isBiometricAvailable ? View.VISIBLE : View.GONE);

        // Login biometrics
        binding.btnSwitchLoginWithBiometrics.setEnabled(isBiometricAvailable);
        binding.loginWithBiometricsError.setText(BiometricAuthHelper.getBiometricFeedback(this));
        binding.loginWithBiometricsError.setVisibility(!isBiometricAvailable ? View.VISIBLE : View.GONE);
    }

    private void promptPayBiometricsAuth(boolean isChecked) {
        if (isChecked) {
            BiometricAuthHelper biometric = new BiometricAuthHelper(
                    this,
                    this,
                    new BiometricAuthHelper.BiometricAuthCallback() {
                        @Override
                        public void onAuthenticated() {
                            userViewModel.setPayBiometricsEnabledLiveData(true);
                        }

                        @Override
                        public void onError(String message) {
                            binding.btnSwitchPayWithBiometrics.setChecked(false);
                            Toast.makeText(SettingsActivity.this, message, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailed() {
                            binding.btnSwitchPayWithBiometrics.setChecked(false);
                        }
                    });
            biometric.authenticate("Enable Fingerprint Payment", "Cancel");
        } else {
            userViewModel.setPayBiometricsEnabledLiveData(false);
        }
    }

    private void promptLoginBiometricsAuth(boolean isChecked) {
        if (isChecked) {
            BiometricAuthHelper biometric = new BiometricAuthHelper(
                    this,
                    this,
                    new BiometricAuthHelper.BiometricAuthCallback() {
                        @Override
                        public void onAuthenticated() {
                            userViewModel.setLoginBiometricsEnabledLiveData(true);
                        }

                        @Override
                        public void onError(String message) {
                            binding.btnSwitchLoginWithBiometrics.setChecked(false);
                            Toast.makeText(SettingsActivity.this, message, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailed() {
                            binding.btnSwitchLoginWithBiometrics.setChecked(false);
                        }
                    });
            biometric.authenticate("Enable Fingerprint Login", "Cancel");
        } else {
            userViewModel.setLoginBiometricsEnabledLiveData(false);
        }
    }

    private void sendPinResetCode() {
        if (!isInternetConnected) {
            UiUtil.showNoInternetAlertDialog(this);
            return;
        }
        authViewModel.sendPasswordResetCode(userEmail);
    }

    private void sendPasswordResetCode() {
        if (!isInternetConnected) {
            UiUtil.showNoInternetAlertDialog(this);
            return;
        }
        authViewModel.sendPasswordResetCode(userEmail);
    }

    private void showOtpConfirmationDialog(boolean isPasswordReset) {
        final String maskedEmail = StringUtil.maskEmail(userEmail);
        final String message = "To continue, we will send you a one-time password (OTP) to your registered email address " + maskedEmail;
        final String btnSecondary = "Cancel";
        final String btnPrimary = "Continue";

        int startIndex = message.indexOf(maskedEmail);
        int endIndex = message.indexOf(maskedEmail) + maskedEmail.length();

        SpannableString spannable = new SpannableString(message);
        spannable.setSpan(
                new StyleSpan(Typeface.BOLD),
                startIndex,
                endIndex,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        DashboardUiUtil.showAlertDialogMessage(
                this,
                (dialog, binding) -> {
                    binding.message.setText(spannable);
                    binding.btnSecondary.setText(btnSecondary);
                    binding.btnPrimary.setText(btnPrimary);

                    binding.btnSecondary.setOnClickListener(view -> dialog.dismiss());
                    binding.btnPrimary.setOnClickListener(view -> {
                        if (isPasswordReset) {
                            sendPasswordResetCode();
                            Intent intent = new Intent(this, OtpVerificationActivity.class);
                            intent.putExtra("email", userEmail);
                            intent.putExtra("purpose", "forgot_password_from_settings");
                            startActivity(intent);
                        } else {
                            // sendPinResetCode();
                            // TODO pin reset
                            Intent intent = new Intent(this, OtpVerificationActivity.class);
                            intent.putExtra("email", userEmail);
                            intent.putExtra("purpose", "forgot_payment_pin");
                            startActivity(intent);
                        }
                        dialog.dismiss();
                    });
                }
        );
    }
}