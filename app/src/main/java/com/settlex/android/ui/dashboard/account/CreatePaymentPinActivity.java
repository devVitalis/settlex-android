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

import com.settlex.android.R;
import com.settlex.android.databinding.ActivityCreatePaymentPinBinding;
import com.settlex.android.ui.common.util.ProgressLoaderController;
import com.settlex.android.ui.dashboard.viewmodel.UserViewModel;
import com.settlex.android.utils.event.Result;
import com.settlex.android.utils.ui.StatusBarUtil;
import com.settlex.android.utils.ui.UiUtil;

import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CreatePaymentPinActivity extends AppCompatActivity {

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

        setupUiActions();
        observeCreatePaymentPinStatus();
    }

    private void setupUiActions() {
        StatusBarUtil.setStatusBarColor(this, R.color.white);
        setupPinInputWatcher();

        binding.btnBackBefore.setOnClickListener(v -> finish());
        binding.btnConfirm.setOnClickListener(view -> createPaymentPin(Objects.requireNonNull(binding.pin.getText()).toString()));
    }

    private void createPaymentPin(String pin) {
        userViewModel.createPaymentPin(pin);
    }

    private void observeCreatePaymentPinStatus() {
        userViewModel.getCreatePaymentPinLiveData().observe(this, event -> {
            Result<String> result = event.getContentIfNotHandled();
            if (result == null) return;

            switch (result.getStatus()) {
                case LOADING -> progressLoader.show();
                case SUCCESS -> onCreatePaymentPinStatusSuccess();
                case ERROR -> onCreatePaymentPinStatusError(result.getMessage());
            }
        });
    }

    private void onCreatePaymentPinStatusSuccess() {
        progressLoader.hide();
        showSuccessBottomSheetDialog();
    }

    private void showSuccessBottomSheetDialog() {
        String title = "Success";
        String message = "Your payment PIN has been created successfully";
        String buttonTxt = "Continue";

        UiUtil.showSuccessBottomSheetDialog(
                this,
                (dialog, binding) -> {
                    binding.title.setText(title);
                    binding.message.setText(message);
                    binding.btnContinue.setText(buttonTxt);
                    binding.btnContinue.setOnClickListener(view -> finish());
                    binding.anim.playAnimation();
                }
        );
    }

    private void onCreatePaymentPinStatusError(String message) {
        progressLoader.hide();
        UiUtil.showSimpleAlertDialog(
                this,
                "Error",
                message
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
                String pin = Objects.requireNonNull(binding.pin.getText()).toString();
                String confirmPin = Objects.requireNonNull(binding.confirmPin.getText()).toString();

                String ERROR_REPEATED_NUMBERS = "PIN must not contain any repeated digits";
                boolean hasConsecutive = hasConsecutive(pin);

                binding.pinError.setText((hasConsecutive) ? ERROR_REPEATED_NUMBERS : "");
                binding.pinError.setVisibility((hasConsecutive) ? View.VISIBLE : View.GONE);

                // config confirm pin
                String ERROR_PIN_MISMATCH = "Pin does not match";
                boolean isMatch = isPinMatch(pin, confirmPin);

                binding.confirmPinError.setText((!isMatch && !confirmPin.isEmpty()) ? ERROR_PIN_MISMATCH : "");
                binding.confirmPinError.setVisibility((!isMatch && !confirmPin.isEmpty()) ? View.VISIBLE : View.GONE);

                updateButtonConfirmState(pin, confirmPin);
            }
        };
        binding.pin.addTextChangedListener(watcher);
        binding.confirmPin.addTextChangedListener(watcher);
    }

    private void updateButtonConfirmState(String pin, String confirmPin) {
        boolean hasConsecutive = hasConsecutive(pin);
        boolean isPinFilled = pin.length() == binding.pin.getItemCount();
        boolean isPinMatch = isPinMatch(pin, confirmPin);

        binding.btnConfirm.setEnabled(!pin.isEmpty() && isPinFilled && !hasConsecutive && isPinMatch);
    }

    private boolean hasConsecutive(String pin) {
        return !pin.isEmpty() && pin.matches(".*(\\d)\\1.*");
    }

    private boolean isPinMatch(String pin, String confirmPin) {
        return pin.length() == binding.pin.getItemCount() && pin.equals(confirmPin);
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