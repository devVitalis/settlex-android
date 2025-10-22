package com.settlex.android.ui.auth.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
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
import androidx.navigation.fragment.NavHostFragment;

import com.settlex.android.R;
import com.settlex.android.data.model.UserModel;
import com.settlex.android.databinding.FragmentSignUpUserPasswordBinding;
import com.settlex.android.ui.auth.viewmodel.AuthViewModel;
import com.settlex.android.ui.common.util.ProgressLoaderController;
import com.settlex.android.ui.dashboard.DashboardActivity;
import com.settlex.android.util.network.NetworkMonitor;
import com.settlex.android.util.ui.StatusBarUtil;
import com.settlex.android.util.ui.UiUtil;

import java.util.Objects;

public class SignUpUserPasswordFragment extends Fragment {

    private FragmentSignUpUserPasswordBinding binding;
    private ProgressLoaderController progressLoader;
    private AuthViewModel authViewModel;
    private boolean isConnected = false;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        progressLoader = new ProgressLoaderController(requireActivity());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSignUpUserPasswordBinding.inflate(inflater, container, false);

        setupUiActions();
        observeNetworkStatus();
        generateAndObserveFcmToken();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        observeRegistrationStatus();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void observeNetworkStatus() {
        NetworkMonitor.getNetworkStatus().observe(getViewLifecycleOwner(), isConnected -> {
            if (!isConnected) {
                showNoInternetConnection();
            }
            this.isConnected = isConnected;
        });
    }

    private void generateAndObserveFcmToken() {
        authViewModel.generateUserFcmToken().observe(getViewLifecycleOwner(), result -> {
            if (result == null) return;

            switch (result.getStatus()) {
                case SUCCESS -> setUserFcmToken(result.getData());
                case ERROR -> setUserFcmToken(null);
            }
        });
    }

    private void setUserFcmToken(String token) {
        authViewModel.updateFcmToken(token);
    }

    private void observeRegistrationStatus() {
        authViewModel.getRegisterResult().observe(getViewLifecycleOwner(), result -> {
            if (result == null) return;

            switch (result.getStatus()) {
                case LOADING -> progressLoader.show();
                case SUCCESS -> onRegistrationStatusSuccess();
                case ERROR -> onRegistrationStatusFailure(result.getMessage());
            }
        });
    }

    private void onRegistrationStatusSuccess() {
        startActivity(new Intent(requireContext(), DashboardActivity.class));
        requireActivity().finishAffinity();

        progressLoader.hide();
    }

    private void onRegistrationStatusFailure(String error) {
        binding.txtError.setText(error);
        binding.txtError.setVisibility(View.VISIBLE);

        progressLoader.hide();
    }

    private void showNoInternetConnection() {
        String title = "Network Unavailable";
        String message = "Please check your Wi-Fi or cellular data and try again";

        UiUtil.showSimpleAlertDialog(
                requireContext(),
                title,
                message
        );
    }

    // UI ACTIONS ========
    private void setupUiActions() {
        StatusBarUtil.setStatusBarColor(requireActivity(), R.color.white);
        setupInputValidation();
        clearFocusOnLastEditTextField();
        togglePasswordVisibilityIcons(false);

        binding.btnBackBefore.setOnClickListener(v -> navigateBack());
        binding.btnExpendLess.setOnClickListener(v -> toggleReferralCodeVisibility());
        binding.btnCreateAccount.setOnClickListener(v -> validateAndCreateAccount());

        binding.btnHelp.setOnClickListener(v -> Toast.makeText(
                requireContext(),
                "Feature not yet implementation",
                Toast.LENGTH_SHORT).show());
    }

    private void validateAndCreateAccount() {
        if (!isConnected) {
            showNoInternetConnection();
            return;
        }

        String password = Objects.requireNonNull(binding.editTxtPassword.getText()).toString().trim();
        String invitationCode = Objects.requireNonNull(binding.editTxtInvitationCode.getText()).toString().trim();

        // Apply default values
        finalizeUserData(invitationCode);

        UserModel user = authViewModel.getUser().getValue();
        createUserAccount(password, user);
    }

    private void finalizeUserData(String invitationCode) {
        authViewModel.applyDefaultUserValues(invitationCode);
    }

    private void createUserAccount(String password, UserModel user) {
        authViewModel.registerUser(authViewModel.getEmail(), password, user);
    }

    private void validateRequirements() {
        String password = Objects.requireNonNull(binding.editTxtPassword.getText()).toString().trim();
        String confirmPassword = Objects.requireNonNull(binding.editTxtConfirmPassword.getText()).toString().trim();

        boolean allValid = validatePasswordRequirements(password, confirmPassword);
        enableCreateAccountButton(allValid);
    }

    private void enableCreateAccountButton(boolean isPasswordValid) {
        binding.btnCreateAccount.setEnabled(isPasswordValid);
    }

    private boolean validatePasswordRequirements(String password, String confirm) {
        boolean hasLength = password.length() >= 8;
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasSpecial = password.matches(".*[@#$%^&+=!.].*");
        boolean matches = password.equals(confirm);

        if (!confirm.isEmpty() && !matches) {
            String ERROR_PASSWORD_MISMATCH = "Passwords do not match!";
            binding.txtError.setText(ERROR_PASSWORD_MISMATCH);
            binding.txtError.setVisibility(View.VISIBLE);
        } else {
            binding.txtError.setVisibility(View.GONE);
        }

        showPasswordRequirements(hasLength, hasUpper, hasLower, hasSpecial, password);
        return hasLength && hasUpper && hasLower && hasSpecial && matches;
    }

    private void showPasswordRequirements(boolean hasLength, boolean hasUpper, boolean hasLower, boolean hasSpecial, String password) {
        SpannableStringBuilder requirements = new SpannableStringBuilder();
        appendRequirement(requirements, hasLength, "At least 8 characters");
        appendRequirement(requirements, hasUpper, "Contains uppercase letter");
        appendRequirement(requirements, hasLower, "Contains lowercase letter");
        appendRequirement(requirements, hasSpecial, "Contains special character (e.g. @#$%^&;+=!.)");

        boolean shouldShowPasswordPrompt = password.isEmpty() || (hasLength && hasUpper && hasLower && hasSpecial);
        binding.txtPasswordPrompt.setVisibility((!shouldShowPasswordPrompt) ? View.VISIBLE : View.GONE);
        binding.txtPasswordPrompt.setText(requirements);
    }

    private void appendRequirement(SpannableStringBuilder builder, boolean isMet, String text) {
        Drawable icon = ContextCompat.getDrawable(requireContext(), isMet ? R.drawable.ic_checkbox_checked : R.drawable.ic_checkbox_unchecked);
        if (icon != null) {
            int size = (int) (binding.txtPasswordPrompt.getTextSize() * 1.2f);
            icon.setBounds(0, 0, size, size);
            builder.append(" ");
            builder.setSpan(new ImageSpan(icon, ImageSpan.ALIGN_BOTTOM), builder.length() - 1, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.append(" ").append(text).append("\n");
        }
    }

    private void setupInputValidation() {
        TextWatcher passwordWatcher = new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateRequirements();
                togglePasswordVisibilityIcons(!TextUtils.isEmpty(s));
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
        binding.editTxtPassword.addTextChangedListener(passwordWatcher);
        binding.editTxtConfirmPassword.addTextChangedListener(passwordWatcher);
    }

    private void togglePasswordVisibilityIcons(boolean show) {
        binding.txtInputLayoutPassword.setEndIconVisible(show);
        binding.txtInputLayoutConfirmPassword.setEndIconVisible(show);
    }

    private void toggleReferralCodeVisibility() {
        boolean isVisible = binding.editTxtInvitationCode.getVisibility() == View.VISIBLE;
        binding.editTxtInvitationCode.setVisibility(isVisible ? View.GONE : View.VISIBLE);
        binding.btnExpendLess.setImageResource(isVisible ? R.drawable.ic_expend_less : R.drawable.ic_expend_more);
    }

    private void navigateBack() {
        NavHostFragment.findNavController(this).popBackStack();
    }

    private void clearFocusOnLastEditTextField() {
        binding.editTxtInvitationCode.setOnEditorActionListener((v, actionId, event) -> {
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
