package com.settlex.android.ui.dashboard.account;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.settlex.android.R;
import com.settlex.android.databinding.ActivitySettingsBinding;
import com.settlex.android.ui.common.components.BiometricAuthHelper;
import com.settlex.android.ui.dashboard.viewmodel.UserViewModel;
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
        observeIsFingerPrintEnabled();
    }

    private void setupUiActions() {
        StatusBarUtil.setStatusBarColor(this, R.color.white);
        shouldEnableBiometricOption();
    }

    private void shouldEnableBiometricOption() {
        boolean isBiometricAvailable = BiometricAuthHelper.isBiometricAvailable(this);

        binding.btnSwitchEnableBiometrics.setEnabled(isBiometricAvailable);
        binding.biometricsError.setText(BiometricAuthHelper.getBiometricFeedback(this));
        binding.biometricsError.setVisibility(!isBiometricAvailable ? View.VISIBLE : View.GONE);
    }

    private void observeIsFingerPrintEnabled() {
        userViewModel.getBiometricsEnabled().observe(this, isEnabled -> {
            // Stop prompt from showing when UI is updating
            binding.btnSwitchEnableBiometrics.setOnCheckedChangeListener(null);
            binding.btnSwitchEnableBiometrics.setChecked(isEnabled);

            binding.btnSwitchEnableBiometrics.setOnCheckedChangeListener((btn, isChecked) -> promptBiometricsAuth(isChecked));
        });
    }

    private void promptBiometricsAuth(boolean isChecked) {
        if (isChecked) {
            BiometricAuthHelper biometric = new BiometricAuthHelper(
                    this,
                    this,
                    new BiometricAuthHelper.BiometricAuthCallback() {
                        @Override
                        public void onAuthenticated() {
                            userViewModel.setBiometricsEnabledLiveData(true);
                        }

                        @Override
                        public void onError(String message) {
                            binding.btnSwitchEnableBiometrics.setChecked(false);
                            Toast.makeText(SettingsActivity.this, message, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailed() {
                            binding.btnSwitchEnableBiometrics.setChecked(false);
                        }
                    });
            biometric.authenticate("Cancel");
        } else {
            userViewModel.setBiometricsEnabledLiveData(false);
        }
    }
}