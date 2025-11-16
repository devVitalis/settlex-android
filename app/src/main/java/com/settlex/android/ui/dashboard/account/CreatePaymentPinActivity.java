package com.settlex.android.ui.dashboard.account;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.chaos.view.PinView;
import com.settlex.android.R;
import com.settlex.android.databinding.ActivityCreatePaymentPinBinding;
import com.settlex.android.util.ui.ProgressLoaderController;
import com.settlex.android.ui.dashboard.util.DashboardUiUtil;
import com.settlex.android.ui.dashboard.viewmodel.UserViewModel;
import com.settlex.android.util.event.UiState;
import com.settlex.android.util.ui.StatusBar;
import com.settlex.android.ui.common.util.DialogHelper;

import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CreatePaymentPinActivity extends AppCompatActivity {
    private final String ERROR_PIN_MISMATCH = "PIN does not match";
    private final String ERROR_REPEATED_NUMBERS = "PIN must not contain any repeated digits";
    private final String ERROR_CURRENT_PIN_IS_SAME_AS_NEW = "New PIN must not be the same as current";

    // dependencies
    private String intentPurpose;
    private ActivityCreatePaymentPinBinding binding;
    private ProgressLoaderController progressLoader;
    private UserViewModel userViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreatePaymentPinBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        progressLoader = new ProgressLoaderController(this);
        intentPurpose = getIntent().getStringExtra("purpose");

        initObservers();
        setupUiActions();
    }

    private void initObservers() {
        observePaymentPinStatus();
    }

    private void setupUiActions() {
        StatusBar.setStatusBarColor(this, R.color.white);
        setupPinInputWatcher();
        setupUiState();

        binding.btnBackBefore.setOnClickListener(v -> finish());
    }

    private void setupUiState() {
        boolean isPinChange = intentPurpose != null && intentPurpose.equals("change_payment_pin");

        binding.title.setText(isPinChange ? "Reset Payment PIN" : "Create Payment PIN");
        binding.currentPinViewContainer.setVisibility(isPinChange ? View.VISIBLE : View.GONE);

        binding.btnConfirm.setOnClickListener(view -> {
            if (isPinChange) {
                changePaymentPin();
                return;
            }
            createPaymentPin();
        });
    }

    private void createPaymentPin() {
        String pin = Objects.requireNonNull(binding.pinView.getText()).toString();

        userViewModel.createPaymentPin(pin);
    }

    private void changePaymentPin() {
        String oldPin = Objects.requireNonNull(binding.currentPinView.getText()).toString();
        String newPin = Objects.requireNonNull(binding.pinView.getText()).toString();

        userViewModel.changePaymentPin(oldPin, newPin);
    }

    // Observers
    private void observePaymentPinStatus() {
        userViewModel.getCreatePaymentPinLiveData().observe(this, event -> {
            UiState<String> result = event.getContentIfNotHandled();
            handlePinOperationStatus(result);
        });

        userViewModel.getChangePaymentPinLiveData().observe(this, event -> {
            UiState<String> result = event.getContentIfNotHandled();
            handlePinOperationStatus(result);
        });
    }

    private void handlePinOperationStatus(UiState<String> result) {
        if (result == null) return;
        switch (result.status) {
            case NO_INTERNET -> showErrorDialog(result.message);

            case LOADING -> progressLoader.show();

            case SUCCESS -> {
                progressLoader.hide();
                showSuccessBottomSheetDialog();
            }
            case FAILURE -> {
                progressLoader.hide();
                showErrorDialog(result.getError());
            }
        }
    }

    private void showSuccessBottomSheetDialog() {
        String title = "Success";
        String buttonTxt = "Continue";

        boolean isPinChange = intentPurpose != null && intentPurpose.equals("change_payment_pin");
        String message = (isPinChange) ? "Your payment PIN has been updated successfully" : "Your payment PIN has been created successfully";

        DialogHelper.showSuccessBottomSheetDialog(
                this,
                (dialog, binding) -> {
                    binding.title.setText(title);
                    binding.message.setText(message);
                    binding.btnContinue.setText(buttonTxt);
                    binding.anim.playAnimation();
                    binding.btnContinue.setOnClickListener(view -> finish());
                }
        );
    }

    private void showErrorDialog(String error) {
        String priBtn = "Okay";

        DashboardUiUtil.showAlertDialogMessage(
                this,
                (dialog, binding) -> {
                    binding.btnSecondary.setVisibility(View.GONE);
                    binding.message.setText(error);
                    binding.btnPrimary.setText(priBtn);
                    binding.btnPrimary.setOnClickListener(v -> dialog.dismiss());
                }
        );
    }

    private void setupPinInputWatcher() {
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {

            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String currentPin = Objects.requireNonNull(binding.currentPinView.getText()).toString();
                String pin = Objects.requireNonNull(binding.pinView.getText()).toString();
                String confirmPin = Objects.requireNonNull(binding.confirmPinView.getText()).toString();

                // Conditions
                boolean hasRepeatedNumbers = hasRepeatedNumbers(pin);
                boolean isPinMatchesConfirmPin = isPinMatch(pin, confirmPin);
                boolean isNewPinSameAsCurrent = isPinMatch(pin, currentPin);

                boolean isPinFilled = isPinViewFilled(pin, binding.pinView);
                boolean isCurrentPinFilled = isPinViewFilled(currentPin, binding.currentPinView);
                boolean isConfirmPinFilled = isPinViewFilled(confirmPin, binding.confirmPinView);

                boolean shouldShowPinMismatch = isConfirmPinFilled && isPinFilled && !isPinMatchesConfirmPin;

                binding.pinViewError.setText((hasRepeatedNumbers) ? ERROR_REPEATED_NUMBERS : "");
                binding.pinViewError.setVisibility((hasRepeatedNumbers) ? View.VISIBLE : View.GONE);

                if (intentPurpose != null && intentPurpose.equals("change_payment_pin")) {

                    if (isPinFilled && isCurrentPinFilled && isNewPinSameAsCurrent) {
                        binding.pinViewError.setText(ERROR_CURRENT_PIN_IS_SAME_AS_NEW);
                        binding.pinViewError.setVisibility(View.VISIBLE);
                    } else {
                        binding.confirmPinViewError.setText(shouldShowPinMismatch ? ERROR_PIN_MISMATCH : "");
                        binding.confirmPinViewError.setVisibility(shouldShowPinMismatch ? View.VISIBLE : View.GONE);
                    }

                } else {
                    binding.confirmPinViewError.setText(shouldShowPinMismatch ? ERROR_PIN_MISMATCH : "");
                    binding.confirmPinViewError.setVisibility(shouldShowPinMismatch ? View.VISIBLE : View.GONE);
                }

                updateButtonConfirmState(currentPin, pin, confirmPin);
            }
        };
        binding.pinView.addTextChangedListener(watcher);
        binding.confirmPinView.addTextChangedListener(watcher);
        binding.currentPinView.addTextChangedListener(watcher);
    }

    private void updateButtonConfirmState(String currentPin, String pin, String confirmPin) {
        boolean isPinChange = intentPurpose != null && intentPurpose.equals("change_payment_pin");
        boolean isCurrentPinFilled = isPinViewFilled(currentPin, binding.currentPinView);
        boolean isCurrentPinSameAsNew = isPinMatch(currentPin, pin);

        boolean hasRepeatedNumbers = hasRepeatedNumbers(pin);
        boolean isPinFilled = isPinViewFilled(pin, binding.pinView);
        boolean isPinMatch = isPinMatch(pin, confirmPin);

        binding.btnConfirm.setEnabled((isPinChange)
                ? !isCurrentPinSameAsNew && isCurrentPinFilled && isPinFilled && !hasRepeatedNumbers && isPinMatch
                : isPinFilled && !hasRepeatedNumbers && isPinMatch);
    }

    private boolean hasRepeatedNumbers(String pin) {
        return !pin.isEmpty() && pin.matches(".*(\\d)\\1.*");
    }

    private boolean isPinMatch(String pin, String pinToCompare) {
        return pin.length() == binding.pinView.getItemCount() && pin.equals(pinToCompare);
    }

    private boolean isPinViewFilled(String pin, PinView view) {
        return pin.length() == view.getItemCount();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    v.clearFocus();
                    hideKeyboard(v);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}