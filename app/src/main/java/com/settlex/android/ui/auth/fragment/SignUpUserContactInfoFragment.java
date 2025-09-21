package com.settlex.android.ui.auth.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.settlex.android.R;
import com.settlex.android.databinding.FragmentSignUpUserContactInfoBinding;
import com.settlex.android.ui.auth.activity.SignInActivity;
import com.settlex.android.ui.auth.viewmodel.AuthViewModel;
import com.settlex.android.ui.common.util.ProgressLoaderController;
import com.settlex.android.ui.info.help.AuthHelpActivity;
import com.settlex.android.ui.info.legal.PrivacyPolicyActivity;
import com.settlex.android.ui.info.legal.TermsAndConditionsActivity;
import com.settlex.android.util.event.Result;
import com.settlex.android.util.network.NetworkMonitor;
import com.settlex.android.util.ui.StatusBarUtil;
import com.settlex.android.util.ui.UiUtil;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SignUpUserContactInfoFragment extends Fragment {
    // Dependencies
    private boolean isConnected = false;
    private AuthViewModel authViewModel;
    private ProgressLoaderController progressLoader;
    private FragmentSignUpUserContactInfoBinding binding;

    // Instance variables for user data
    private String email;
    private String phone;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        progressLoader = new ProgressLoaderController(requireActivity());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSignUpUserContactInfoBinding.inflate(inflater, container, false);

        setupUiActions();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        observeNetworkState();
        observeAndHandleSendVerificationOtpResult();
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }

    // OBSERVERS ==========
    private void observeNetworkState() {
        NetworkMonitor.getNetworkStatus().observe(requireActivity(), isConnected -> this.isConnected = isConnected);
    }

    private void observeAndHandleSendVerificationOtpResult() {
        authViewModel.getSendEmailVerificationOtpResult().observe(getViewLifecycleOwner(), event -> {
            Result<String> result = event.getContentIfNotHandled();
            if (result == null) {
                return;
            }

            switch (result.getStatus()) {
                case LOADING -> onSendVerificationOtpLoading();
                case SUCCESS -> onSendVerificationOtpSuccess();
                case ERROR -> onSendVerificationOtpError(result.getMessage());
            }
        });
    }

    private void onSendVerificationOtpLoading() {
        progressLoader.show();
    }

    private void onSendVerificationOtpSuccess() {
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(R.id.signUpEmailVerificationFragment);
        progressLoader.hide();
    }

    private void onSendVerificationOtpError(String reason) {
        binding.txtErrorFeedback.setText(reason);
        binding.txtErrorFeedback.setVisibility(View.VISIBLE);
        progressLoader.hide();
    }

    // UI ACTIONS =========
    private void setupUiActions() {
        StatusBarUtil.setStatusBarColor(requireActivity(), R.color.white);
        setupInputValidation();
        setupLegalLinks();
        reEnableEditTextFocus();
        clearFocusAndHideKeyboardOnOutsideTap(binding.getRoot());

        binding.btnSignIn.setOnClickListener(view -> navigateToActivity(SignInActivity.class, true));
        binding.btnHelp.setOnClickListener(v -> navigateToActivity(AuthHelpActivity.class, false));
        binding.btnBackBefore.setOnClickListener(v -> onBackButtonPressed());
        binding.btnContinue.setOnClickListener(v -> storeUserInfoInModel());
    }

    private void storeUserInfoInModel() {
        if (!isConnected) {
            // Check for network state
            showNoInternetDialog();
            return;
        }

        authViewModel.updateEmail(email);
        authViewModel.updatePhone(phone);

        // Send OTP
        sendEmailVerificationOtp();
    }

    private void sendEmailVerificationOtp() {
        authViewModel.sendEmailVerificationOtp(email);
    }

    private void showNoInternetDialog() {
        String title = "Network Unavailable";
        String message = "Please check your Wi-Fi or cellular data and try again";

        UiUtil.showAlertDialog(requireActivity(), (alertDialog, binding) -> {
            binding.icon.setImageResource(R.drawable.ic_signal_disconnected);
            binding.title.setText(title);
            binding.message.setText(message);

            binding.btnOkay.setOnClickListener(view -> alertDialog.dismiss());
            alertDialog.show();
        });
    }

    private void setupInputValidation() {
        // Email watcher
        binding.editTxtEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                email = s.toString().trim();
                binding.emailSectionHeader.setVisibility(!TextUtils.isEmpty(email) ? View.VISIBLE : View.INVISIBLE);
            }
        });

        // Phone watcher
        binding.editTxtPhoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                phone = s.toString().trim();
            }
        });

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
                updateContinueButtonState();
            }
        };

        // attach validation watcher
        binding.editTxtEmail.addTextChangedListener(validationWatcher);
        binding.editTxtPhoneNumber.addTextChangedListener(validationWatcher);
        binding.checkBoxTermsPrivacy.setOnCheckedChangeListener((buttonView, isChecked) -> updateContinueButtonState());
    }

    private void updateContinueButtonState() {
        setContinueButtonEnabled(isEmailValid() && isPhoneNumberValid() && isCheckBoxTermsPrivacyChecked());
    }

    private boolean isEmailValid() {
        return email != null && !email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isPhoneNumberValid() {
        return phone != null && !phone.isEmpty() && phone.matches("^(0)?[7-9][0-1]\\d{8}$");
    }

    private boolean isCheckBoxTermsPrivacyChecked() {
        return binding.checkBoxTermsPrivacy.isChecked();
    }

    private void setContinueButtonEnabled(boolean allValid) {
        binding.btnContinue.setEnabled(allValid);
    }

    private void setupLegalLinks() {
        String legalText = "I have read, understood and agreed to the Terms & Conditions and Privacy Policy.";
        SpannableStringBuilder span = new SpannableStringBuilder(legalText);

        ClickableSpan termsSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                startActivity(new Intent(requireActivity(), TermsAndConditionsActivity.class));
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                ds.setColor(ContextCompat.getColor(requireContext(), R.color.blue));
                ds.setUnderlineText(false);
            }
        };

        ClickableSpan privacySpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                startActivity(new Intent(requireActivity(), PrivacyPolicyActivity.class));
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                ds.setColor(ContextCompat.getColor(requireContext(), R.color.blue));
                ds.setUnderlineText(false);
            }
        };

        span.setSpan(termsSpan, legalText.indexOf("Terms"), legalText.indexOf("Conditions") + "Conditions".length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        span.setSpan(privacySpan, legalText.indexOf("Privacy"), legalText.indexOf("Policy") + "Policy".length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        binding.txtTermsPrivacy.setText(span);
        binding.txtTermsPrivacy.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void reEnableEditTextFocus() {
        View.OnClickListener focusListener = v -> {
            if (v instanceof EditText) {
                v.setFocusable(true);
                v.setFocusableInTouchMode(true);
                v.requestFocus();
            }
        };
        binding.editTxtEmail.setOnClickListener(focusListener);
        binding.editTxtPhoneNumber.setOnClickListener(focusListener);

        // Background changes
        binding.editTxtPhoneNumber.setOnFocusChangeListener((v, hasFocus) -> binding.editTxtPhoneNumberBackground.setBackgroundResource(hasFocus ? R.drawable.bg_edit_txt_custom_white_focused : R.drawable.bg_edit_txt_custom_white_not_focused));
        binding.editTxtEmail.setOnFocusChangeListener((v, hasFocus) -> binding.editTxtEmailBg.setBackgroundResource(hasFocus ? R.drawable.bg_edit_txt_custom_white_focused : R.drawable.bg_edit_txt_custom_white_not_focused));
    }

    private void navigateToActivity(Class<? extends Activity> activityClass, boolean clearBackStack) {
        if (!clearBackStack) {
            startActivity(new Intent(requireActivity(), activityClass));
            return;
        }
        startActivity(new Intent(requireActivity(), activityClass));
        requireActivity().finish();
    }

    private void onBackButtonPressed() {
        requireActivity().getOnBackPressedDispatcher().onBackPressed();
        requireActivity().finish();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void clearFocusAndHideKeyboardOnOutsideTap(View root) {
        if (!(root instanceof EditText)) {
            root.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) hideKeyboard();
                return false;
            });
        }

        if (root instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) root).getChildCount(); i++) {
                clearFocusAndHideKeyboardOnOutsideTap(((ViewGroup) root).getChildAt(i));
            }
        }
    }

    private void hideKeyboard() {
        View focusedView = requireActivity().getCurrentFocus();
        if (focusedView != null) {
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
            focusedView.clearFocus();
        }
    }
}