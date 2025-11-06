package com.settlex.android.ui.dashboard.account;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.settlex.android.R;
import com.settlex.android.databinding.ActivitySettingsBinding;
import com.settlex.android.ui.common.components.BiometricAuthHelper;
import com.settlex.android.ui.dashboard.model.UserUiModel;
import com.settlex.android.ui.dashboard.viewmodel.UserViewModel;
import com.settlex.android.utils.event.Result;
import com.settlex.android.utils.ui.StatusBarUtil;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SettingsActivity extends AppCompatActivity {
    private ActivitySettingsBinding binding;
    private UserViewModel userViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        setupUiActions();
        observeUserData();
        observePayBiometricEnabled();
        observeLoginBiometricEnabled();
    }

    private void observeUserData() {
        userViewModel.getUserLiveData().observe(this, result -> {
            if (result != null && result.getStatus() == Result.Status.SUCCESS) {
                onUserDataStatusSuccess(result.getData());
            }
        });
    }

    private void onUserDataStatusSuccess(UserUiModel user) {
        boolean hasPin = user.hasPin();

        binding.txtChangePaymentPin.setText(hasPin ? "Change payment pin" : "Set new payment pin");
        binding.txtSetPaymentPin.setVisibility(!hasPin ? View.VISIBLE : View.GONE);
    }

    private void setupUiActions() {
        StatusBarUtil.setStatusBarColor(this, R.color.white);
        shouldEnableBiometricOption();

        binding.btnChangePaymentPin.setOnClickListener(view -> {
        });
        binding.btnForgotPaymentPin.setOnClickListener(view -> {
        });
    }

    private void showDialog(){

    }

    private void shouldEnableBiometricOption() {
        boolean isBiometricAvailable = BiometricAuthHelper.isBiometricAvailable(this);

        // Payment
        binding.btnSwitchPayWithBiometrics.setEnabled(isBiometricAvailable);
        binding.payWithBiometricsError.setText(BiometricAuthHelper.getBiometricFeedback(this));
        binding.payWithBiometricsError.setVisibility(!isBiometricAvailable ? View.VISIBLE : View.GONE);

        // Login
        binding.btnSwitchLoginWithBiometrics.setEnabled(isBiometricAvailable);
        binding.loginWithBiometricsError.setText(BiometricAuthHelper.getBiometricFeedback(this));
        binding.loginWithBiometricsError.setVisibility(!isBiometricAvailable ? View.VISIBLE : View.GONE);
    }

    private void observePayBiometricEnabled() {
        userViewModel.getPayBiometricsEnabled().observe(this, isEnabled -> {
            // UI is updating
            binding.btnSwitchPayWithBiometrics.setOnCheckedChangeListener(null);
            binding.btnSwitchPayWithBiometrics.setChecked(isEnabled);

            binding.btnSwitchPayWithBiometrics.setOnCheckedChangeListener((btn, isChecked) -> {
                promptPayBiometricsAuth(isChecked);
                Log.d("Settings", "login biometrics is checked");
            });
        });
    }

    private void observeLoginBiometricEnabled() {
        Log.d("Settings", "observing login biometric enabled...");

        userViewModel.getLoginBiometricsEnabled().observe(this, isEnabled -> {
            // UI is updating
            binding.btnSwitchLoginWithBiometrics.setOnCheckedChangeListener(null);
            binding.btnSwitchLoginWithBiometrics.setChecked(isEnabled);

            binding.btnSwitchLoginWithBiometrics.setOnCheckedChangeListener((btn, isChecked) -> {
                promptLoginBiometricsAuth(isChecked);
                Log.d("Settings", "login biometrics is checked");
            });
        });
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
            biometric.authenticate("Cancel");
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
            biometric.authenticate("Cancel");
        } else {
            userViewModel.setLoginBiometricsEnabledLiveData(false);
        }
    }
}