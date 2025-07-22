package com.settlex.android.view.auth.fragment;

import static android.view.View.GONE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.settlex.android.data.model.UserModel;
import com.settlex.android.databinding.FragmentSignUpUserContactInfoBinding;
import com.settlex.android.view.activities.AuthHelpActivity;
import com.settlex.android.view.auth.viewmodel.SignUpViewModel;

import java.util.Objects;

public class SignUpUserContactInfoFragment extends Fragment {
    private SignUpViewModel vm;
    private FragmentSignUpUserContactInfoBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSignUpUserContactInfoBinding.inflate(inflater, container, false);

        setupStatusBar();
        setupUIActions();

        vm = new ViewModelProvider(requireActivity()).get(SignUpViewModel.class);
        return binding.getRoot();
    }

    /*---------------------------------
    Required Empty Public Constructor
    ----------------------------------*/
    public SignUpUserContactInfoFragment() {
    }

    /*-----------------------------
    Handle UI & Event Listeners
    -----------------------------*/
    private void setupUIActions() {
        reEnableFocus();
        hideErrorPrompt();
        setupTextWatchers();
        setupHideKeyboardOnTouch();

        // Click Listeners
        binding.imgBackBefore.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
        binding.btnHelp.setOnClickListener(v -> loadActivity(AuthHelpActivity.class));
        binding.btnContinue.setOnClickListener(v -> checkIfEmailOrPhoneExist());
    }

    /*---------------------------------------------------
     Validate if Email and Phone already exist in on db
    ---------------------------------------------------*/
    private void checkIfEmailOrPhoneExist() {
        String email = Objects.requireNonNull(binding.editTxtEmail.getText()).toString().trim();
        String phone = Objects.requireNonNull(binding.editTxtPhoneNumber.getText()).toString().trim();

        UserModel user = vm.getUser().getValue();
        if (user != null) {
            user.setEmail(email);
            user.setPhone(phone);
            vm.setUser(user);
        }
            loadFragment(new SignUpUserPasswordFragment());
    }


    /*---------------------------------------
    Enable Continue when inputs are valid
    ---------------------------------------*/
    private void updateButtonState() {
        String email = Objects.requireNonNull(binding.editTxtEmail.getText()).toString().trim();
        String phone = Objects.requireNonNull(binding.editTxtPhoneNumber.getText()).toString().trim();

        boolean validEmail = Patterns.EMAIL_ADDRESS.matcher(email).matches();
        boolean validPhone = phone.matches("^0(7[0-9]|8[0-9]|9[0-9])[0-9]{8}$");

        binding.btnContinue.setEnabled(validEmail && validPhone);
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
    }

    /*-------------------------------------------
    Hide error prompt as soon as the user starts
    fixing the field
    -------------------------------------------*/
    private void hideErrorPrompt() {
        binding.editTxtEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.txtErrorMsgEmail.setVisibility(GONE);
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
                binding.txtErrorMsgPhoneNumber.setVisibility(GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
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

    /*-------------------------
    Launch external activity
    -------------------------*/
    private void loadActivity(Class<? extends Activity> activityClass) {
        startActivity(new Intent(requireActivity(), activityClass));
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