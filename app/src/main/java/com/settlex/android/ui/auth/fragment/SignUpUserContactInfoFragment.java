package com.settlex.android.ui.auth.fragment;

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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.settlex.android.R;
import com.settlex.android.databinding.FragmentSignUpUserContactInfoBinding;
import com.settlex.android.ui.auth.activity.LoginActivity;
import com.settlex.android.ui.auth.viewmodel.AuthViewModel;
import com.settlex.android.ui.common.util.ProgressLoaderController;
import com.settlex.android.ui.info.legal.PrivacyPolicyActivity;
import com.settlex.android.ui.info.legal.TermsAndConditionsActivity;
import com.settlex.android.utils.event.Result;
import com.settlex.android.utils.network.NetworkMonitor;
import com.settlex.android.utils.string.StringUtil;
import com.settlex.android.utils.ui.StatusBarUtil;
import com.settlex.android.utils.ui.UiUtil;

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
        observeSendVerificationOtpStatus();
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }

    // OBSERVERS ==========
    private void observeNetworkStatus() {
        NetworkMonitor.getNetworkStatus().observe(getViewLifecycleOwner(), isConnected -> {
            if (!isConnected) {
                showNoInternetDialog();
            }
            this.isConnected = isConnected;
        });
    }

    private void observeSendVerificationOtpStatus() {
        authViewModel.getSendEmailVerificationOtpResult().observe(getViewLifecycleOwner(), event -> {
            Result<String> result = event.getContentIfNotHandled();
            if (result == null) {
                return;
            }

            switch (result.getStatus()) {
                case LOADING -> progressLoader.show();
                case SUCCESS -> onSendVerificationOtpStatusSuccess();
                case ERROR -> onSendVerificationOtpStatusError(result.getMessage());
            }
        });
    }

    private void onSendVerificationOtpStatusSuccess() {
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(R.id.signUpEmailVerificationFragment);
        progressLoader.hide();
    }

    private void onSendVerificationOtpStatusError(String reason) {
        binding.txtError.setText(reason);
        binding.txtError.setVisibility(View.VISIBLE);
        progressLoader.hide();
    }

    // UI ACTIONS =========
    private void setupUiActions() {
        StatusBarUtil.setStatusBarColor(requireActivity(), R.color.white);
        setupEditTextInputValidation();
        setupLegalLinks();
        setupEditTextFocusHandlers();
        clearFocusOnLastEditTextField();

        binding.btnSignIn.setOnClickListener(view -> navigateToActivity(
                LoginActivity.class,
                true));
        binding.btnBackBefore.setOnClickListener(v -> onBackButtonPressed());
        binding.btnContinue.setOnClickListener(v -> storeUserInfoInModel());

        binding.btnHelp.setOnClickListener(v -> Toast.makeText(
                requireContext(),
                "Feature not yet implementation",
                Toast.LENGTH_SHORT).show());
    }

    private void storeUserInfoInModel() {
        if (!isConnected) {
            showNoInternetDialog();
            return;
        }

        String email = binding.editTxtEmail.getText().toString().trim().toLowerCase();
        String phoneNumber = binding.editTxtPhoneNumber.getText().toString().trim();

        authViewModel.updateEmail(email);
        authViewModel.updatePhone(StringUtil.formatPhoneNumberWithCountryCode(phoneNumber));

        // send OTP
        sendEmailVerificationOtp(email);
    }

    private void sendEmailVerificationOtp(String email) {
        authViewModel.sendEmailVerificationOtp(email);
    }

    private void showNoInternetDialog() {
        String title = "Network Unavailable";
        String message = "Please check your Wi-Fi or cellular data and try again";

        UiUtil.showSimpleAlertDialog(
                requireContext(),
                title,
                message
        );
    }

    private void setupEditTextFocusHandlers() {
        // cache drawables
        int focusBgRes = R.drawable.bg_edit_txt_custom_white_focused;
        int defaultBgRes = R.drawable.bg_edit_txt_custom_white_not_focused;

        binding.editTxtPhoneNumber.setOnFocusChangeListener((v, hasFocus) -> binding.editTxtPhoneNumberBackground.setBackgroundResource(hasFocus ? focusBgRes : defaultBgRes));
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
        binding.editTxtPhoneNumber.addTextChangedListener(validationWatcher);
        binding.checkBoxTermsPrivacy.setOnCheckedChangeListener((buttonView, isChecked) -> updateContinueButtonState());
    }

    private void updateContinueButtonState() {
        String email = binding.editTxtEmail.getText().toString().trim().toLowerCase();
        String phoneNumber = binding.editTxtPhoneNumber.getText().toString().trim();

        setContinueButtonEnabled(isEmailValid(email) && isPhoneNumberValid(phoneNumber) && isCheckBoxTermsPrivacyChecked());
    }

    private boolean isEmailValid(String email) {
        return email != null && !email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isPhoneNumberValid(String phoneNumber) {
        return phoneNumber != null && !phoneNumber.isEmpty() && phoneNumber.matches("^(0)?[7-9][0-1]\\d{8}$");
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

    private void clearFocusOnLastEditTextField() {
        binding.editTxtPhoneNumber.setOnEditorActionListener((v, actionId, event) -> {
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