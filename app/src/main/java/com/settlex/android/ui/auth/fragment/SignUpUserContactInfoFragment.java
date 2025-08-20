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
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.settlex.android.R;
import com.settlex.android.databinding.FragmentSignUpUserContactInfoBinding;
import com.settlex.android.ui.activities.help.AuthHelpActivity;
import com.settlex.android.ui.activities.legal.PrivacyPolicyActivity;
import com.settlex.android.ui.activities.legal.TermsAndConditionsActivity;
import com.settlex.android.ui.auth.activity.SignInActivity;
import com.settlex.android.ui.auth.util.AuthResult;
import com.settlex.android.ui.auth.viewmodel.AuthViewModel;
import com.settlex.android.ui.common.SettleXProgressBarController;
import com.settlex.android.util.NetworkMonitor;
import com.settlex.android.util.StringUtil;
import com.settlex.android.util.UiUtil;

import java.util.Objects;

public class SignUpUserContactInfoFragment extends Fragment {
    private boolean isConnected = false;

    private AuthViewModel authViewModel;
    private SettleXProgressBarController progressController;
    private FragmentSignUpUserContactInfoBinding binding;

    // ====================== LIFECYCLE ======================
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSignUpUserContactInfoBinding.inflate(inflater, container, false);
        progressController = new SettleXProgressBarController(binding.getRoot());
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        setupStatusBar();
        setupUiActions();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        observeNetworkStatus();
        observeSendVerificationOtp();
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }

    // ====================== OBSERVERS ======================
    private void observeSendVerificationOtp() {
        authViewModel.getSendEmailVerificationOtpResult().observe(getViewLifecycleOwner(), event -> {
            AuthResult<String> result = event.getContentIfNotHandled();
            if (result != null) {
                switch (result.getStatus()) {
                    case LOADING -> progressController.show();
                    case SUCCESS -> onSendVerificationOtpSuccess();
                    case ERROR -> onSendOtpFailure(result.getMessage());
                }
            }
        });
    }

    private void observeNetworkStatus() {
        NetworkMonitor.getNetworkStatus().observe(requireActivity(), isConnected ->
                this.isConnected = isConnected);
    }

    private void onSendVerificationOtpSuccess() {
        navigateToFragment(new SignUpEmailVerificationFragment());
        progressController.hide();
    }

    private void onSendOtpFailure(String reason) {
        binding.txtErrorFeedback.setText(reason);
        binding.txtErrorFeedback.setVisibility(View.VISIBLE);
        progressController.hide();
    }

    // ====================== UI ACTIONS ======================
    private void setupUiActions() {
        setupInputValidation();
        setupLegalLinks();
        reEnableEditTextFocus();
        clearFocusAndHideKeyboardOnOutsideTap(binding.getRoot());

        binding.btnSignIn.setOnClickListener(view -> navigateToActivity(SignInActivity.class, true));
        binding.btnHelp.setOnClickListener(v -> navigateToActivity(AuthHelpActivity.class, false));
        binding.imgBackBefore.setOnClickListener(v -> navigateBack());
        binding.btnContinue.setOnClickListener(v -> validateUserInfoAndSendOtp());
    }

    private void validateUserInfoAndSendOtp() {
        if (isConnected) {
            String email = Objects.requireNonNull(binding.editTxtEmail.getText()).toString().trim();
            String phone = StringUtil.formatPhoneNumber(binding.editTxtPhoneNumber.getText().toString().trim());

            authViewModel.updateEmail(email);
            authViewModel.updatePhone(phone);
            authViewModel.sendEmailVerificationOtp(email);
        } else {
            onNoInternetConnection();
        }
    }

    private void onNoInternetConnection() {
        UiUtil.showInfoDialog(
                requireActivity(),
                "Network Unavailable",
                "Please check your network connection and try again",
                null);
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
                binding.txtErrorFeedback.setVisibility(View.GONE);
                updateContinueButtonState();
            }
        };

        binding.editTxtEmail.addTextChangedListener(validationWatcher);
        binding.editTxtPhoneNumber.addTextChangedListener(validationWatcher);
        binding.checkBoxTermsPrivacy.setOnCheckedChangeListener((buttonView, isChecked) -> updateContinueButtonState());

        binding.editTxtEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.emailSectionHeader.setVisibility(!TextUtils.isEmpty(s) ? View.VISIBLE : View.INVISIBLE);
            }
        });
    }

    private void setupLegalLinks() {
        String legalText = getString(R.string.terms_privacy_agreement);
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

    private void updateContinueButtonState() {
        String email = Objects.requireNonNull(binding.editTxtEmail.getText()).toString().trim();
        String phone = Objects.requireNonNull(binding.editTxtPhoneNumber.getText()).toString().trim();

        boolean isValidEmail = Patterns.EMAIL_ADDRESS.matcher(email).matches();
        boolean isValidPhone = phone.matches("^(0)?[7-9][0-1]\\d{8}$");
        boolean isTermsAccepted = binding.checkBoxTermsPrivacy.isChecked();

        binding.btnContinue.setEnabled(isValidEmail && isValidPhone && isTermsAccepted);
    }

    // ====================== NAVIGATION ======================
    private void navigateToFragment(Fragment fragment) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null).commit();
    }

    private void navigateToActivity(Class<? extends Activity> activityClass, boolean clearBackStack) {
        if (clearBackStack){
            startActivity(new Intent(requireActivity(), activityClass));
            requireActivity().finish();
        } else {
            startActivity(new Intent(requireActivity(), activityClass));
        }
    }

    private void navigateBack() {
        requireActivity().getOnBackPressedDispatcher().onBackPressed();
        requireActivity().finish();
    }

    // ====================== UTILITIES ======================
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

    private void setupStatusBar() {
        Window window = requireActivity().getWindow();
        window.setStatusBarColor(ContextCompat.getColor(requireContext(), R.color.white));
        View decorView = window.getDecorView();
        decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }
}