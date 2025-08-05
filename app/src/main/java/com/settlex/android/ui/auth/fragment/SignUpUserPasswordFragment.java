package com.settlex.android.ui.auth.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.settlex.android.R;
import com.settlex.android.data.model.UserModel;
import com.settlex.android.databinding.FragmentSignUpUserPasswordBinding;
import com.settlex.android.ui.activities.help.AuthHelpActivity;
import com.settlex.android.ui.auth.viewmodel.AuthViewModel;
import com.settlex.android.ui.common.SettleXProgressBarController;
import com.settlex.android.ui.dashboard.DashboardActivity;
import com.settlex.android.util.LiveDataUtils;

import java.util.Objects;

public class SignUpUserPasswordFragment extends Fragment {

    private FragmentSignUpUserPasswordBinding binding;
    private SettleXProgressBarController progressBar;
    private AuthViewModel vm;

    /*----------------------------
    Required Public Constructor
    ----------------------------*/
    public SignUpUserPasswordFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSignUpUserPasswordBinding.inflate(inflater, container, false);

        progressBar = new SettleXProgressBarController(binding.fragmentContainer);
        vm = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        setupStatusBar();
        setupUIActions();

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }

    /*----------------------------
    Handle UI & Event Listeners
    ----------------------------*/
    private void setupUIActions() {
        reEnableFocus();
        observePasswordFields();
        setupUI(binding.fragmentContainer);

        binding.txtInputLayoutPassword.setEndIconVisible(false);
        binding.txtInputLayoutConfirmPassword.setEndIconVisible(false);

        // Click Listeners
        binding.imgBackBefore.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
        binding.btnHelp.setOnClickListener(v -> startActivity(new Intent(requireActivity(), AuthHelpActivity.class)));
        binding.icExpendLess.setOnClickListener(v -> referralCodeToggle());
        binding.btnCreateAccount.setOnClickListener(v -> attemptAccountCreation());
    }

    /*----------------------------
    Handle final form submission
    ----------------------------*/
    private void attemptAccountCreation() {
        progressBar.show();

        String password = Objects.requireNonNull(binding.editTxtPassword.getText()).toString().trim();
        String invitationCode = Objects.requireNonNull(binding.editTxtInvitationCode.getText()).toString().trim();

        vm.finalizeUserFields(invitationCode);
        UserModel user = vm.getUser().getValue();

        vm.signUpUser(user, vm.getEmail(), password);

        LiveDataUtils.observeOnce(vm.getSignUpResult(), requireActivity(), signUpResult -> {
            if (signUpResult.isSuccess()) {
                navigateToDashboard();
            } else {
                showError(signUpResult.message());
            }
            progressBar.hide();
        });
    }

    /*-------------------------------------------
    Helper Method to Display Error Info to User
    -------------------------------------------*/
    private void showError(String message) {
        binding.txtPasswordError.setText(message);
        binding.txtPasswordError.setVisibility(View.VISIBLE);
    }

    /*------------------------------
    Validate password and update UI
    -------------------------------*/
    private void updateButtonState() {
        String password = Objects.requireNonNull(binding.editTxtPassword.getText()).toString().trim();
        String confirmPassword = Objects.requireNonNull(binding.editTxtConfirmPassword.getText()).toString().trim();

        boolean length = password.length() >= 8;
        boolean upper = password.matches(".*[A-Z].*");
        boolean lower = password.matches(".*[a-z].*");
        boolean special = password.matches(".*[@#$%^&+=!.].*");
        boolean match = password.equals(confirmPassword);
        boolean allGood = length && upper && lower && special;

        SpannableStringBuilder feedback = new SpannableStringBuilder();
        appendCondition(feedback, length, "At least 8 characters\n");
        appendCondition(feedback, upper, "Contains uppercase letter\n");
        appendCondition(feedback, lower, "Contains lowercase letter\n");
        appendCondition(feedback, special, "Contains special character (e.g. @#$%^&+=!.)\n");

        binding.txtPasswordPrompt.setVisibility(allGood || password.isEmpty() ? View.GONE : View.VISIBLE);
        binding.txtPasswordPrompt.setText(feedback);
        binding.txtInputLayoutPassword.setEndIconVisible(!password.isEmpty());
        binding.txtInputLayoutConfirmPassword.setEndIconVisible(!confirmPassword.isEmpty());
        binding.btnCreateAccount.setEnabled(length && upper && lower && special && match);
    }

    /*----------------------------------------
    Check password match and update UI state
    ----------------------------------------*/
    private void validatePassword() {
        String original = Objects.requireNonNull(binding.editTxtPassword.getText()).toString().trim();
        String confirm = Objects.requireNonNull(binding.editTxtConfirmPassword.getText()).toString().trim();

        boolean condition = confirm.isEmpty() || confirm.equals(original);
        binding.txtPasswordError.setVisibility(!condition ? View.VISIBLE : View.GONE);

        updateButtonState();
    }

    /*----------------------------------
    Observe and validate password input
    -----------------------------------*/
    private void observePasswordFields() {
        TextWatcher passwordWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePassword();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        binding.editTxtPassword.addTextChangedListener(passwordWatcher);
        binding.editTxtConfirmPassword.addTextChangedListener(passwordWatcher);
    }

    /*---------------------------------------
    Show check or uncheck icon beside rules
    ---------------------------------------*/
    private void appendCondition(SpannableStringBuilder builder, boolean condition, String text) {
        Drawable icon = ContextCompat.getDrawable(requireContext(), condition ? R.drawable.ic_checkbox_checked : R.drawable.ic_checkbox_unchecked);
        if (icon != null) {
            int size = (int) (binding.txtPasswordPrompt.getTextSize() * 1.3f);
            icon.setBounds(0, 0, size, size);
            ImageSpan span = new ImageSpan(icon, ImageSpan.ALIGN_BOTTOM);
            int start = builder.length();
            builder.append(" ");
            builder.setSpan(span, start, start + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        builder.append(" ").append(text);
    }

    /*---------------------------------
    Enable focus on tap for EditTexts
    ---------------------------------*/
    private void reEnableFocus() {
        View.OnClickListener enableFocusListener = v -> {
            if (v instanceof EditText editText) {
                editText.setFocusable(true);
                editText.setFocusableInTouchMode(true);
                editText.requestFocus();
            }
        };

        binding.editTxtPassword.setOnClickListener(enableFocusListener);
        binding.editTxtConfirmPassword.setOnClickListener(enableFocusListener);
        binding.editTxtInvitationCode.setOnClickListener(enableFocusListener);
    }

    /*---------------------------------
    Toggle invitation code visibility
    ---------------------------------*/
    private void referralCodeToggle() {
        boolean isVisible = binding.editTxtInvitationCode.getVisibility() == View.VISIBLE;
        binding.editTxtInvitationCode.setVisibility(isVisible ? View.GONE : View.VISIBLE);
        binding.icExpendLess.setImageResource(isVisible ? R.drawable.ic_expend_less : R.drawable.ic_expend_more);
    }

    /*-----------------------------
    Navigate to Dashboard Activity
    -----------------------------*/
    private void navigateToDashboard() {
        Intent intent = new Intent(requireContext(), DashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    /*---------------------------------------
    Set up touch listener to hide keyboard
    when user taps outside EditText views
    ---------------------------------------*/
    @SuppressLint("ClickableViewAccessibility")
    private void setupUI(View root) {
        if (!(root instanceof EditText)) {
            root.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    hideKeyboardAndClearFocus();
                }
                return false;
            });
        }

        if (root instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) root).getChildCount(); i++) {
                View child = ((ViewGroup) root).getChildAt(i);
                setupUI(child);
            }
        }
    }

    /*----------------------------------------
    Helper method to hide keyboard and
    clear focus from currently focused view
    ----------------------------------------*/
    private void hideKeyboardAndClearFocus() {
        View focused = requireActivity().getCurrentFocus();
        if (focused instanceof EditText) {
            focused.clearFocus();
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(focused.getWindowToken(), 0);
            }
        }
    }

    /*--------------------------
    Setup custom status bar
    --------------------------*/
    private void setupStatusBar() {
        Window window = requireActivity().getWindow();
        window.setStatusBarColor(ContextCompat.getColor(requireContext(), R.color.white));
        View decorView = window.getDecorView();
        int flags = decorView.getSystemUiVisibility();
        flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        decorView.setSystemUiVisibility(flags);
    }
}
