package com.settlex.android.ui.auth.register;

import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.settlex.android.R;
import com.settlex.android.databinding.FragmentSignUpEmailVerificationBinding;
import com.settlex.android.ui.auth.AuthViewModel;
import com.settlex.android.util.ui.ProgressLoaderController;
import com.settlex.android.util.event.Result;
import com.settlex.android.util.network.NetworkMonitor;
import com.settlex.android.util.string.StringFormatter;
import com.settlex.android.util.ui.StatusBar;
import com.settlex.android.ui.common.util.DialogHelper;

import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SignUpEmailVerificationFragment extends Fragment {

    private java.lang.String email;

    // Dependencies
    private static final int OTP_RESEND_COOLDOWN_MS = 60000;
    private static final int COUNTDOWN_INTERVAL_MS = 1000;
    private CountDownTimer resendOtpCountdownTimer;

    private AuthViewModel authViewModel;
    private ProgressLoaderController progressLoader;
    private FragmentSignUpEmailVerificationBinding binding;
    private boolean isConnected = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        progressLoader = new ProgressLoaderController(requireActivity());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSignUpEmailVerificationBinding.inflate(getLayoutInflater(), container, false);

        email = authViewModel.getEmail();
        setupUiActions();
        observeNetworkStatus();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        startResendOtpCooldown();
        observeSendVerificationCodeStatus();
        observeEmailVerificationStatus();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        clearResources();
    }

    private void clearResources() {
        // Cancel timer
        // Remove binding
        if (resendOtpCountdownTimer != null) resendOtpCountdownTimer.cancel();
        binding = null;
    }

    private void setupUiActions() {
        StatusBar.setStatusBarColor(requireActivity(), R.color.white);
        styleSpamHintText();
        setupOtpInputWatcher();
        maskAndDisplayEmail();

        binding.btnBackBefore.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());
        binding.btnResendOtp.setOnClickListener(v -> resendOtp());
        binding.btnContinue.setOnClickListener(v -> verifyOtp());
        binding.btnHelp.setOnClickListener(v -> StringFormatter.showNotImplementedToast(requireContext()));
    }

    private void observeNetworkStatus() {
        NetworkMonitor.getNetworkStatus().observe(getViewLifecycleOwner(), isConnected -> {
            if (!isConnected) {
                showNoInternet();
            }
            this.isConnected = isConnected;
        });
    }

    private void observeEmailVerificationStatus() {
        authViewModel.getVerifyEmailLiveData().observe(getViewLifecycleOwner(), event -> {
            Result<java.lang.String> result = event.getContentIfNotHandled();
            if (result == null) return;

            switch (result.getStatus()) {
                case LOADING -> progressLoader.show();
                case SUCCESS -> onEmailVerificationSuccess();
                case FAILURE -> onEmailVerificationStatusError(result.getError());
            }
        });
    }

    private void onEmailVerificationSuccess() {
        NavOptions navOptions = new NavOptions.Builder()
                .setPopUpTo(R.id.signUpEmailVerificationFragment, true)
                .build();

        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(R.id.signUpUserInfoFragment, null, navOptions);

        progressLoader.hide();
    }

    private void onEmailVerificationStatusError(java.lang.String error) {
        binding.txtError.setText(error);
        binding.txtError.setVisibility(View.VISIBLE);
        progressLoader.hide();
    }

    private void observeSendVerificationCodeStatus() {
        authViewModel.getSendVerificationCodeLiveData().observe(getViewLifecycleOwner(), event -> {
            Result<java.lang.String> result = event.getContentIfNotHandled();
            if (result == null) return;

            switch (result.getStatus()) {
                case LOADING -> progressLoader.show();
                case SUCCESS -> onSendVerificationCodeStatusSuccess();
                case FAILURE -> onVerificationCodeStatusError(result.getError());
            }
        });
    }

    private void onSendVerificationCodeStatusSuccess() {
        startResendOtpCooldown();
        progressLoader.hide();
    }

    private void onVerificationCodeStatusError(java.lang.String error) {
        binding.txtError.setText(error);
        binding.txtError.setVisibility(View.VISIBLE);
        progressLoader.hide();
    }

    private void showNoInternet() {
        DialogHelper.showNoInternetAlertDialog(requireContext());
    }

    private void verifyOtp() {
        if (isConnected) {
            authViewModel.verifyEmail(email, getEnteredOtpDigits());
            return;
        }
        showNoInternet();
    }

    private void resendOtp() {
        if (isConnected) {
            authViewModel.sendVerificationCode(email);
            return;
        }
        showNoInternet();
    }

    private void maskAndDisplayEmail() {
        binding.userEmail.setText(StringFormatter.maskEmail(email));
    }

    private void styleSpamHintText() {
        java.lang.String message = "Didn’t get the email? Make sure to also check your spam/junk folder if you can't find the email in your inbox";
        java.lang.String highlightedPhrase = "check your spam/junk folder";

        // Find the phrase location inside the full text
        int startIndex = message.indexOf(highlightedPhrase);
        int endIndex = startIndex + highlightedPhrase.length();

        SpannableStringBuilder styledMessage = new SpannableStringBuilder(message);
        styledMessage.setSpan(
                new ForegroundColorSpan(Color.parseColor("#FFA500")),
                startIndex,
                endIndex,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        // Update the TextView
        binding.txtInfo.setText(styledMessage);
    }


    private void startResendOtpCooldown() {
        binding.btnResendOtp.setEnabled(false); // Disable resend btn
        final CharSequence originalText = binding.btnResendOtp.getText();

        resendOtpCountdownTimer = new CountDownTimer(OTP_RESEND_COOLDOWN_MS, COUNTDOWN_INTERVAL_MS) {
            public void onTick(long millisUntilFinished) {
                if (binding == null) return;
                java.lang.String COUNT_DOWN = "Resend in " + millisUntilFinished / 1000;
                binding.btnResendOtp.setText(COUNT_DOWN);
            }

            public void onFinish() {
                if (binding != null) {
                    binding.btnResendOtp.setText(originalText);
                    binding.btnResendOtp.setEnabled(true);
                }
            }
        }.start();
    }

    private boolean isOtpDigitsFilled() {
        return binding.otpBox.length() == binding.otpBox.getItemCount();
    }

    private java.lang.String getEnteredOtpDigits() {
        return Objects.requireNonNull(binding.otpBox.getText()).toString();
    }

    private void setupOtpInputWatcher() {
        binding.otpBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence otp, int start, int before, int count) {
                if (otp.toString().isEmpty()) binding.txtError.setVisibility(View.GONE);
                binding.btnContinue.setEnabled(isOtpDigitsFilled());
            }
        });
    }
}
