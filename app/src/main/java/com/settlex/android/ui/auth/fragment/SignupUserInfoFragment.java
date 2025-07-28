package com.settlex.android.ui.auth.fragment;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
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
import com.settlex.android.databinding.FragmentSignupUserInfoBinding;
import com.settlex.android.ui.activities.PrivacyPolicyActivity;
import com.settlex.android.ui.activities.TermsAndConditionsActivity;
import com.settlex.android.ui.activities.AuthHelpActivity;
import com.settlex.android.ui.auth.viewmodel.SignUpViewModel;


import java.util.Objects;

public class SignupUserInfoFragment extends Fragment {

    private FragmentSignupUserInfoBinding binding;
    private SignUpViewModel vm;

    /*----------------------------
    Required Public Constructor
    ----------------------------*/
    public SignupUserInfoFragment() { }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSignupUserInfoBinding.inflate(inflater, container, false);

        setupStatusBar();
        setupUIActions();

        vm = new ViewModelProvider(requireActivity()).get(SignUpViewModel.class);
        return binding.getRoot();
    }

    /*-----------------------
    Handle Event Listeners
    -----------------------*/
    private void setupUIActions() {
        reEnableFocus();
        setupInputWatchers();
        setupHideKeyboardOnTouch();

        // Click Listeners
        binding.imgBackBefore.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
        binding.btnHelp.setOnClickListener(v -> startActivity(new Intent(requireActivity(), AuthHelpActivity.class)));

        binding.btnContinue.setOnClickListener(v -> {
            String firstName = Objects.requireNonNull(binding.editTxtFirstName.getText()).toString().trim();
            String lastName = Objects.requireNonNull(binding.editTxtLastName.getText()).toString().trim();

            UserModel user = vm.getUser().getValue();
            if (user == null) user = new UserModel();

            user.setFirstName(firstName);
            user.setLastName(lastName);

            vm.setUser(user);
            loadFragment(new SignUpUserContactInfoFragment());
        });
    }

    /*----------------------------------
    Enable Continue when name is valid
    ----------------------------------*/
    private void updateButtonState() {
        String firstName = Objects.requireNonNull(binding.editTxtFirstName.getText()).toString().trim();
        String lastName = Objects.requireNonNull(binding.editTxtLastName.getText()).toString().trim();

        boolean validFirst = !firstName.isEmpty() && firstName.matches("^[A-Z][a-z]+(?:\\s[A-Z][a-z]+)*$");
        boolean validLast = !lastName.isEmpty() && lastName.matches("^[A-Z][a-z]+(?:\\s[A-Z][a-z]+)*$");

        binding.btnContinue.setEnabled(validFirst && validLast);
    }

    /*----------------------------
    Validate fields while typing
    -----------------------------*/
    private void setupInputWatchers() {
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        binding.editTxtFirstName.addTextChangedListener(watcher);
        binding.editTxtLastName.addTextChangedListener(watcher);
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

        binding.editTxtFirstName.setOnClickListener(enableFocusListener);
        binding.editTxtLastName.setOnClickListener(enableFocusListener);
    }

    /*----------------------------------------------
    Dismiss keyboard and clear focus on outside tap
    ----------------------------------------------*/
    @SuppressLint("ClickableViewAccessibility")
    private void setupHideKeyboardOnTouch() {
        binding.fragmentContainer.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.performClick();
                View focused = requireActivity().getCurrentFocus();
                if (focused instanceof EditText) {
                    Rect outRect = new Rect();
                    focused.getGlobalVisibleRect(outRect);
                    if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                        focused.clearFocus();
                        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (imm != null) {
                            imm.hideSoftInputFromWindow(focused.getWindowToken(), 0);
                        }
                        binding.fragmentContainer.requestFocus();
                    }
                }
            }
            return false;
        });
    }

    /*-----------------------------
    Navigate to another fragment
    -----------------------------*/
    private void loadFragment(Fragment fragment) {
        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
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