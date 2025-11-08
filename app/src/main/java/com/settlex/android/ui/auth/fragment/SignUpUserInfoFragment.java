package com.settlex.android.ui.auth.fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.settlex.android.R;
import com.settlex.android.databinding.FragmentSignUpUserInfoBinding;
import com.settlex.android.ui.auth.viewmodel.AuthViewModel;
import com.settlex.android.utils.network.NetworkMonitor;
import com.settlex.android.utils.string.StringUtil;
import com.settlex.android.utils.ui.StatusBarUtil;
import com.settlex.android.utils.ui.UiUtil;

import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SignUpUserInfoFragment extends Fragment {

    private boolean isConnected;
    private FragmentSignUpUserInfoBinding binding;
    private AuthViewModel authViewModel;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSignUpUserInfoBinding.inflate(inflater, container, false);

        setupUiActions();
        observeNetworkStatus();
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void observeNetworkStatus() {
        NetworkMonitor.getNetworkStatus().observe(getViewLifecycleOwner(), isConnected -> {
            if (!isConnected) {
                showNoInternet();
            }
            this.isConnected = isConnected;
        });
    }

    private void showNoInternet() {
        UiUtil.showNoInternetAlertDialog(requireContext());
    }

    private void setupUiActions() {
        StatusBarUtil.setStatusBarColor(requireActivity(), R.color.white);
        setupInputValidation();
        clearFocusOnLastEditTextField();

        binding.btnBackBefore.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());
        binding.btnContinue.setOnClickListener(v -> submitUserInfoAndProceed());
        binding.btnHelp.setOnClickListener(v -> StringUtil.showNotImplementedToast(requireContext()));
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
                updateContinueButtonState();
            }
        };
        binding.editTxtFirstname.addTextChangedListener(validationWatcher);
        binding.editTxtLastName.addTextChangedListener(validationWatcher);
    }

    private void submitUserInfoAndProceed() {
        if (!isConnected) {
            showNoInternet();
            return;
        }

        String firstName = StringUtil.capitalizeEachWord(Objects.requireNonNull(binding.editTxtFirstname.getText()).toString().trim());
        String lastName = StringUtil.capitalizeEachWord(Objects.requireNonNull(binding.editTxtLastName.getText()).toString().trim());

        authViewModel.updateFirstName(firstName);
        authViewModel.updateLastName(lastName);

        navigateToPasswordSetupFragment();
    }

    private void navigateToPasswordSetupFragment() {
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(R.id.signUpUserPasswordFragment);
    }

    private void updateContinueButtonState() {
        String firstName = Objects.requireNonNull(binding.editTxtFirstname.getText()).toString().trim();
        String lastName = Objects.requireNonNull(binding.editTxtLastName.getText()).toString().trim();

        boolean isValidFirstName = !firstName.isEmpty() && firstName.matches("^[a-zA-Z]{2,}(?:\\s[a-zA-Z]{2,})*$");
        boolean isValidLastName = !lastName.isEmpty() && lastName.matches("^[a-zA-Z]{2,}(?:\\s[a-zA-Z]{2,})*$");

        binding.btnContinue.setEnabled(isValidFirstName && isValidLastName);
    }

    private void clearFocusOnLastEditTextField() {
        binding.editTxtLastName.setOnEditorActionListener((v, actionId, event) -> {
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
