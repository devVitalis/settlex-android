package com.settlex.android.ui.auth.register;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.settlex.android.R;
import com.settlex.android.databinding.FragmentSignUpUserContactInfoBinding;
import com.settlex.android.ui.auth.AuthViewModel;
import com.settlex.android.ui.auth.login.LoginActivity;
import com.settlex.android.ui.common.util.DialogHelper;
import com.settlex.android.ui.info.legal.PrivacyPolicyActivity;
import com.settlex.android.ui.info.legal.TermsAndConditionsActivity;
import com.settlex.android.util.event.UiState;
import com.settlex.android.util.network.NetworkMonitor;
import com.settlex.android.util.string.CurrencyFormatter;
import com.settlex.android.util.string.StringFormatter;
import com.settlex.android.util.ui.ProgressLoaderController;
import com.settlex.android.util.ui.StatusBar;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SignUpUserContactInfoFragment extends Fragment {
    private boolean isConnected = false;
    private AuthViewModel authViewModel;
    private ProgressLoaderController progressLoader;
    private FragmentSignUpUserContactInfoBinding binding;


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

        observeNetworkStatus();
        observeSendVerificationCodeStatus();
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }

    private void setupUiActions() {
        StatusBar.setStatusBarColor(requireActivity(), R.color.white);
        setupEditTextInputValidation();
        setupLegalLinks();
        setupEditTextFocusHandlers();
        clearFocusOnLastEditTextField();

        binding.btnSignIn.setOnClickListener(view -> navigateToActivity(LoginActivity.class, true));
        binding.btnBackBefore.setOnClickListener(v -> onBackButtonPressed());
        binding.btnContinue.setOnClickListener(v -> storeUserInfoInModel());
        binding.btnHelp.setOnClickListener(v -> StringFormatter.showNotImplementedToast(requireContext()));
    }

    // Observers
    private void observeNetworkStatus() {
        NetworkMonitor.getNetworkStatus().observe(getViewLifecycleOwner(), isConnected -> {
            if (!isConnected) {
                showNoInternetDialog();
            }
            this.isConnected = isConnected;
        });
    }

    private void observeSendVerificationCodeStatus() {
        authViewModel.getSendVerificationCodeLiveData().observe(getViewLifecycleOwner(), event -> {
            UiState<java.lang.String> result = event.getContentIfNotHandled();
            if (result == null) {
                return;
            }

            switch (result.status) {
                case LOADING -> progressLoader.show();
                case SUCCESS -> onSendVerificationCodeStatusSuccess();
                case FAILURE -> onSendVerificationCodeStatusError(result.getError());
            }
        });
    }

    private void onSendVerificationCodeStatusSuccess() {
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(R.id.sign_up_email_verification_fragment);
        progressLoader.hide();
    }

    private void onSendVerificationCodeStatusError(java.lang.String reason) {
        binding.txtError.setText(reason);
        binding.txtError.setVisibility(View.VISIBLE);
        progressLoader.hide();
    }

    private void storeUserInfoInModel() {
        if (!isConnected) {
            showNoInternetDialog();
            return;
        }

        java.lang.String email = binding.editTxtEmail.getText().toString().trim().toLowerCase();
        java.lang.String phoneNumber = binding.editTxtPhone.getText().toString().trim();

        authViewModel.updateEmail(email);
        authViewModel.updatePhone(CurrencyFormatter.formatPhoneNumberWithCountryCode(phoneNumber));

        // send OTP
        sendEmailVerificationOtp(email);
    }

    private void sendEmailVerificationOtp(java.lang.String email) {
        authViewModel.sendVerificationCode(email);
    }

    private void showNoInternetDialog() {
        java.lang.String title = "Network Unavailable";
        java.lang.String message = "Please check your Wi-Fi or cellular data and try again";

        DialogHelper.showSimpleAlertDialog(
                requireContext(),
                title,
                message
        );
    }

    private void setupEditTextFocusHandlers() {
        // cache drawables
        int focusBgRes = R.drawable.bg_edit_txt_custom_white_focused;
        int defaultBgRes = R.drawable.bg_edit_txt_custom_white_not_focused;

        binding.editTxtPhone.setOnFocusChangeListener((v, hasFocus) -> binding.editTxtPhoneBg.setBackgroundResource(hasFocus ? focusBgRes : defaultBgRes));
        binding.editTxtEmail.setOnFocusChangeListener((v, hasFocus) -> binding.editTxtEmailBg.setBackgroundResource(hasFocus ? focusBgRes : defaultBgRes));
    }

    private void setupEditTextInputValidation() {
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
                updateContinueButtonState();
            }
        };

        // attach validation watcher
        binding.editTxtEmail.addTextChangedListener(validationWatcher);
        binding.editTxtPhone.addTextChangedListener(validationWatcher);
        binding.checkBoxTermsPrivacy.setOnCheckedChangeListener((buttonView, isChecked) -> updateContinueButtonState());
    }

    private void updateContinueButtonState() {
        java.lang.String email = binding.editTxtEmail.getText().toString().trim().toLowerCase();
        java.lang.String phoneNumber = binding.editTxtPhone.getText().toString().trim();

        setContinueButtonEnabled(isEmailValid(email) && isPhoneNumberValid(phoneNumber) && isCheckBoxTermsPrivacyChecked());
    }

    private boolean isEmailValid(java.lang.String email) {
        return email != null && !email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isPhoneNumberValid(java.lang.String phoneNumber) {
        return phoneNumber != null && !phoneNumber.isEmpty() && phoneNumber.matches("^(0)?[7-9][0-1]\\d{8}$");
    }

    private boolean isCheckBoxTermsPrivacyChecked() {
        return binding.checkBoxTermsPrivacy.isChecked();
    }

    private void setContinueButtonEnabled(boolean allValid) {
        binding.btnContinue.setEnabled(allValid);
    }

    private void setupLegalLinks() {
        java.lang.String legalText = "I have read, understood and agreed to the Terms & Conditions and Privacy Policy.";
        SpannableStringBuilder span = new SpannableStringBuilder(legalText);

        ClickableSpan termsSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                navigateToActivity(TermsAndConditionsActivity.class, false);
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
                navigateToActivity(PrivacyPolicyActivity.class, false);
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

    private void navigateToActivity(Class<? extends Activity> activityClass, boolean clearBackStack) {
        if (!clearBackStack) {
            startActivity(new Intent(requireContext(), activityClass));
            return;
        }
        startActivity(new Intent(requireContext(), activityClass));
        requireActivity().finish();
    }

    private void onBackButtonPressed() {
        requireActivity().getOnBackPressedDispatcher().onBackPressed();
        requireActivity().finish();
    }

    private void clearFocusOnLastEditTextField() {
        binding.editTxtPhone.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // Hide the keyboard
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                v.clearFocus();
                return true;
            }
            return false;
        });
    }
}