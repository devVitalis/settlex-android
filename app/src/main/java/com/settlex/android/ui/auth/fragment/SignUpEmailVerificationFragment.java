package com.settlex.android.ui.auth.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.settlex.android.R;
import com.settlex.android.databinding.FragmentSignUpEmailVerificationBinding;
import com.settlex.android.ui.auth.viewmodel.AuthViewModel;
import com.settlex.android.ui.common.util.ProgressLoaderController;
import com.settlex.android.util.event.Result;
import com.settlex.android.util.network.NetworkMonitor;
import com.settlex.android.util.string.StringUtil;
import com.settlex.android.util.ui.StatusBarUtil;
import com.settlex.android.util.ui.UiUtil;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SignUpEmailVerificationFragment extends Fragment {

    private String email;

    // Dependencies
    private static final int OTP_RESEND_COOLDOWN_MS = 60000;
    private static final int COUNTDOWN_INTERVAL_MS = 1000;
    private CountDownTimer resendOtpCountdownTimer;
    private EditText[] otpDigitViews;

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
        observeSendEmailOtpStatus();
        observeOtpVerificationStatus();
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

    //  OBSERVERS =========
    private void observeNetworkStatus() {
        NetworkMonitor.getNetworkStatus().observe(getViewLifecycleOwner(), isConnected -> {
            if (!isConnected) {
                showNoInternet();
            }
            this.isConnected = isConnected;
        });
    }

    private void observeOtpVerificationStatus() {
        authViewModel.getVerifyEmailVerificationOtpResult().observe(getViewLifecycleOwner(), event -> {
            Result<String> result = event.getContentIfNotHandled();
            if (result == null) return;

            switch (result.getStatus()) {
                case LOADING -> progressLoader.show();
                case SUCCESS -> onOtpVerificationSuccess();
                case ERROR -> onSendOrVerifyOtpStatusError(result.getMessage());
            }
        });
    }

    private void onOtpVerificationSuccess() {
        NavOptions navOptions = new NavOptions.Builder()
                .setPopUpTo(R.id.signUpEmailVerificationFragment, true)
                .build();

        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(R.id.signUpUserInfoFragment, null, navOptions);

        progressLoader.hide();
    }

    private void observeSendEmailOtpStatus() {
        authViewModel.getSendEmailVerificationOtpResult().observe(getViewLifecycleOwner(), event -> {
            Result<String> result = event.getContentIfNotHandled();
            if (result == null) return;

            switch (result.getStatus()) {
                case LOADING -> progressLoader.show();
                case SUCCESS -> onSendNewOtpVerificationStatusSuccess();
                case ERROR -> onSendOrVerifyOtpStatusError(result.getMessage());
            }
        });
    }

    private void onSendNewOtpVerificationStatusSuccess() {
        startResendOtpCooldown();
        progressLoader.hide();
    }

    private void onSendOrVerifyOtpStatusError(String reason) {
        binding.txtError.setText(reason);
        binding.txtError.setVisibility(View.VISIBLE);
        progressLoader.hide();
    }

    private void showNoInternet() {
        String title = "Network Unavailable";
        String message = "Please check your Wi-Fi or cellular data and try again";

        UiUtil.showSimpleAlertDialog(
                requireContext(),
                title,
                message
        );
    }

    // UI SETUP ============
    private void setupUiActions() {
        StatusBarUtil.setStatusBarColor(requireActivity(), R.color.white);
        formatInfoText();
        setupOtpInputBehavior();
        maskAndDisplayEmail();

        binding.btnBackBefore.setOnClickListener(v -> navigateBack());
        binding.btnResendOtp.setOnClickListener(v -> resendOtp());
        binding.btnVerify.setOnClickListener(v -> verifyOtp());

        binding.btnHelp.setOnClickListener(v -> Toast.makeText(
                        requireContext(),
                        "Feature not yet implementation",
                        Toast.LENGTH_SHORT).show());

    }

    private void verifyOtp() {
        if (isConnected) {
            authViewModel.verifyEmailOtp(email, getEnteredOtpDigits());
            return;
        }
        showNoInternet();
    }

    private void resendOtp() {
        if (isConnected) {
            authViewModel.sendEmailVerificationOtp(email);
            return;
        }
        showNoInternet();
    }

    private void maskAndDisplayEmail() {
        binding.txtUserEmail.setText(StringUtil.maskEmail(email));
    }

    private void formatInfoText() {
        String infoText = "Didn’t get the email? Make sure to also " +
                "<font color='#FFA500'><b>check your spam/junk folder</b></font> " +
                "if you can't find the email in your inbox.";
        binding.txtInfo.setText(Html.fromHtml(infoText, Html.FROM_HTML_MODE_LEGACY));
    }

    private void startResendOtpCooldown() {
        binding.btnResendOtp.setEnabled(false); // Disable resend btn
        final CharSequence originalText = binding.btnResendOtp.getText();

        resendOtpCountdownTimer = new CountDownTimer(OTP_RESEND_COOLDOWN_MS, COUNTDOWN_INTERVAL_MS) {
            public void onTick(long millisUntilFinished) {
                if (binding == null) return;
                String COUNT_DOWN = "Resend in " + millisUntilFinished / 1000;
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
        for (EditText digit : otpDigitViews) {
            if (TextUtils.isEmpty(digit.getText())) return false;
        }
        return true;
    }

    private String getEnteredOtpDigits() {
        StringBuilder otp = new StringBuilder();
        for (EditText digit : otpDigitViews) {
            otp.append(digit.getText().toString().trim());
        }
        return otp.toString();
    }

    private void setupOtpInputBehavior() {
        otpDigitViews = new EditText[]{binding.otpDigit1, binding.otpDigit2, binding.otpDigit3, binding.otpDigit4, binding.otpDigit5, binding.otpDigit6};

        for (int i = 0; i < otpDigitViews.length; i++) {
            EditText current = otpDigitViews[i];
            EditText next = (i < otpDigitViews.length - 1) ? otpDigitViews[i + 1] : null;
            EditText previous = (i > 0) ? otpDigitViews[i - 1] : null;

            current.setLongClickable(false);
            current.setTextIsSelectable(false);
            current.setEnabled(i == 0);

            current.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (isOtpDigitsFilled()) hideKeyboard();
                    if (TextUtils.isEmpty(s)) binding.txtError.setVisibility(View.GONE);

                    if (s.length() == 1 && next != null) {
                        next.setEnabled(true);
                        next.requestFocus();
                    }

                    binding.btnVerify.setEnabled(isOtpDigitsFilled());
                }
            });

            current.setOnKeyListener((v, keyCode, event) -> {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
                    if (!TextUtils.isEmpty(current.getText())) {
                        current.setText("");
                    } else if (previous != null) {
                        previous.setText("");
                        previous.requestFocus();
                        current.setEnabled(false);
                    }
                    return true;
                }
                return false;
            });
        }
    }

    private void navigateBack() {
        NavHostFragment.findNavController(this).popBackStack();
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        View focusedView = requireActivity().getCurrentFocus();
        if (focusedView != null) {
            imm.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
        }
    }
}
