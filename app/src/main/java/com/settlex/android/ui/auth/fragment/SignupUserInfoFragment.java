package com.settlex.android.ui.auth.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.settlex.android.databinding.FragmentSignupUserInfoBinding;
import com.settlex.android.utils.StringUtil;
import com.settlex.android.ui.activities.help.AuthHelpActivity;
import com.settlex.android.ui.auth.viewmodel.AuthViewModel;

import java.util.Objects;

public class SignupUserInfoFragment extends Fragment {

    private FragmentSignupUserInfoBinding binding;
    private AuthViewModel vm;

    /*----------------------------
    Required Public Constructor
    ----------------------------*/
    public SignupUserInfoFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSignupUserInfoBinding.inflate(inflater, container, false);

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

    /*-------------------------------------
    Handle Event Listeners & Method Calls
    -------------------------------------*/
    private void setupUIActions() {
        reEnableFocus();
        setupInputWatchers();
        setupUI(binding.fragmentContainer);

        // Click Listeners
        binding.imgBackBefore.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
        binding.btnHelp.setOnClickListener(v -> startActivity(new Intent(requireActivity(), AuthHelpActivity.class)));
        binding.btnContinue.setOnClickListener(v -> updateUserInfoAndMoveOn());
    }

    /*------------------------------------------------
    Update/Save User First and Last Name To ViewModel
    ------------------------------------------------*/
    private void updateUserInfoAndMoveOn() {
        String firstName = Objects.requireNonNull(binding.editTxtFirstName.getText()).toString().trim();
        String lastName = Objects.requireNonNull(binding.editTxtLastName.getText()).toString().trim();

        vm.updateFirstName(StringUtil.capitalizeEachWord(firstName));
        vm.updateLastName(StringUtil.capitalizeEachWord(lastName));

        loadFragment(new SignUpUserPasswordFragment());
    }

    /*----------------------------------
    Enable Continue when name is valid
    ----------------------------------*/
    private void updateButtonState() {
        String firstName = Objects.requireNonNull(binding.editTxtFirstName.getText()).toString().trim();
        String lastName = Objects.requireNonNull(binding.editTxtLastName.getText()).toString().trim();

        boolean validFirst = !firstName.isEmpty() && firstName.matches("^[a-zA-Z]{2,}(?:\\s[a-zA-Z]{2,})*$");
        boolean validLast = !lastName.isEmpty() && lastName.matches("^[a-zA-Z]{2,}(?:\\s[a-zA-Z]{2,})*$");

        binding.btnContinue.setEnabled(validFirst && validLast);
    }

    /*------------------------------------
    Validate EditText fields while typing
    ------------------------------------*/
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

    /*-----------------------------
    Navigate to another fragment
    -----------------------------*/
    private void loadFragment(Fragment fragment) {
        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main, fragment)
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