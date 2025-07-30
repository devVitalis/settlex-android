package com.settlex.android.ui.auth.fragment;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.settlex.android.R;
import com.settlex.android.controller.ProgressViewController;
import com.settlex.android.databinding.FragmentSignUpUserContactInfoBinding;
import com.settlex.android.ui.activities.legal.PrivacyPolicyActivity;
import com.settlex.android.ui.activities.legal.TermsAndConditionsActivity;
import com.settlex.android.ui.auth.activity.SignInActivity;
import com.settlex.android.ui.auth.viewmodel.AuthViewModel;

import java.util.Objects;

public class SignUpUserContactInfoFragment extends Fragment {
    private AuthViewModel vm;
    private ProgressViewController progressBar;
    private FragmentSignUpUserContactInfoBinding binding;

    /*----------------------------------
    Required Empty Public Constructor
    ----------------------------------*/
    public SignUpUserContactInfoFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSignUpUserContactInfoBinding.inflate(inflater, container, false);

        progressBar = new ProgressViewController(binding.fragmentContainer);
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

    /*-----------------------------
    Handle UI & Event Listeners
    -----------------------------*/
    private void setupUIActions() {
        reEnableFocus();
        setupTextWatchers();
        hideInfoMessagePrompt();
        setClickableLegalLinks();
        setupPhoneInputFocusHandler();
        setupUI(binding.fragmentContainer);

        // Click Listeners
        binding.imgBackBefore.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), SignInActivity.class);
            intent.addFlags(FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
        binding.btnHelp.setOnClickListener(v -> loadFragment(new SignUpUserPasswordFragment()));
        binding.btnContinue.setOnClickListener(v -> saveUserInfoAndSendVerificationEmail());
    }

    /*------------------------------------------------------
    Save user info (Live Data) and send OTP to the email
    ------------------------------------------------------*/
    private void saveUserInfoAndSendVerificationEmail() {
        progressBar.show();
        String email = Objects.requireNonNull(binding.editTxtEmail.getText()).toString().trim();
        String phone = Objects.requireNonNull(binding.editTxtPhoneNumber.getText()).toString().trim();

        vm.updateEmail(email);
        vm.updatePhone(phone);
        vm.sendEmailOtp(email);
        vm.getEmailOtpResult().observe(getViewLifecycleOwner(), result -> {
            if (result.isSuccess()) {
                loadFragment(new SignUpEmailVerificationFragment());
            } else {
                binding.txtErrorInfoEmail.setText(result.message());
                binding.txtErrorInfoEmail.setVisibility(View.VISIBLE);
            }
            progressBar.hide();
        });
    }

    /*---------------------------------------
    Enable Continue when inputs are valid
    ---------------------------------------*/
    private void updateButtonState() {
        String email = Objects.requireNonNull(binding.editTxtEmail.getText()).toString().trim();
        String phone = Objects.requireNonNull(binding.editTxtPhoneNumber.getText()).toString().trim();

        boolean validEmail = Patterns.EMAIL_ADDRESS.matcher(email).matches();
        boolean validPhone = phone.matches("^0(7[0-9]|8[0-9]|9[0-9])[0-9]{8}$");
        boolean isChecked = binding.checkBoxTermsPrivacy.isChecked();

        binding.btnContinue.setEnabled(validEmail && validPhone && isChecked);
    }

    /*-----------------------------------
    Enable focus on tap for EditTexts
    -----------------------------------*/
    private void reEnableFocus() {
        View.OnClickListener enableFocusListener = v -> {
            if (v instanceof EditText editText) {
                editText.setFocusable(true);
                editText.setFocusableInTouchMode(true);
                editText.requestFocus();
            }
        };
        binding.editTxtEmail.setOnClickListener(enableFocusListener);
        binding.editTxtPhoneNumber.setOnClickListener(enableFocusListener);
    }

    /*-------------------------------------------
    Enable Dynamic Stroke color on PhoneInputBg
    -------------------------------------------*/
    private void setupPhoneInputFocusHandler() {
        binding.editTxtPhoneNumber.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) {
                binding.phoneInputBg.setBackgroundResource(R.drawable.bg_input_custom_focused);
            } else {
                binding.phoneInputBg.setBackgroundResource(R.drawable.bg_input_custom_not_focused);
            }
        });
        binding.editTxtEmail.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) {
                binding.emailInputBg.setBackgroundResource(R.drawable.bg_input_custom_focused);
            } else {
                binding.emailInputBg.setBackgroundResource(R.drawable.bg_input_custom_not_focused);
            }
        });
    }

    /*-----------------------------------------
    Validate Text inputs & update btn state
    -----------------------------------------*/
    private void setupTextWatchers() {
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateButtonState();
            }
        };

        binding.editTxtEmail.addTextChangedListener(watcher);
        binding.editTxtPhoneNumber.addTextChangedListener(watcher);
        binding.checkBoxTermsPrivacy.setOnCheckedChangeListener((compoundButton, b) -> updateButtonState());
    }

    /*-----------------------------------
    Setup Clickable Legal Links & Color
    -----------------------------------*/
    private void setClickableLegalLinks() {
        String legalText = "I have read, understood and agreed to the Terms & Conditions and Privacy Policy.";
        SpannableStringBuilder span = new SpannableStringBuilder(legalText);

        ClickableSpan terms = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                loadActivity(TermsAndConditionsActivity.class);
            }

            @Override
            public void updateDrawState(@NonNull android.text.TextPaint ds) {
                ds.setColor(Color.parseColor("#0044CC"));
                ds.setUnderlineText(false);
            }
        };

        ClickableSpan privacy = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                loadActivity(PrivacyPolicyActivity.class);
            }

            @Override
            public void updateDrawState(@NonNull android.text.TextPaint ds) {
                ds.setColor(Color.parseColor("#0044CC"));
                ds.setUnderlineText(false);
            }
        };

        span.setSpan(terms, 42, 61, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        span.setSpan(privacy, 65, 80, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        binding.txtTermsPrivacy.setText(span);
        binding.txtTermsPrivacy.setMovementMethod(LinkMovementMethod.getInstance());
    }

    /*-------------------------------------------
    Hide error prompt as soon as the user starts
    fixing the field
    -------------------------------------------*/
    private void hideInfoMessagePrompt() {
        binding.editTxtEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.txtErrorInfoEmail.setVisibility(View.GONE);
                binding.emailSectionHeader.setVisibility(!TextUtils.isEmpty(s) ? View.VISIBLE : View.INVISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.editTxtPhoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.txtErrorMsgPhoneNumber.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    /*------------------------
    Launch external activity
    -------------------------*/
    private void loadActivity(Class<? extends Activity> activityClass) {
        startActivity(new Intent(requireActivity(), activityClass));
    }

    /*----------------------------
    Navigate to another fragment
    ----------------------------*/
    private void loadFragment(Fragment fragment) {
        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction().replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
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

    /*-------------------------------
    Customize status bar appearance
    -------------------------------*/
    private void setupStatusBar() {
        Window window = requireActivity().getWindow();
        window.setStatusBarColor(ContextCompat.getColor(requireContext(), R.color.white));
        View decorView = window.getDecorView();
        int flags = decorView.getSystemUiVisibility();
        flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        decorView.setSystemUiVisibility(flags);
    }
}