package com.settlex.android.ui.dashboard.home;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.settlex.android.R;
import com.settlex.android.databinding.ActivityCreatePaymentIdBinding;
import com.settlex.android.ui.common.util.ProgressLoaderController;
import com.settlex.android.ui.dashboard.DashboardActivity;
import com.settlex.android.ui.dashboard.model.UserUiModel;
import com.settlex.android.ui.dashboard.viewmodel.UserViewModel;
import com.settlex.android.utils.event.Result;
import com.settlex.android.utils.ui.StatusBarUtil;
import com.settlex.android.utils.ui.UiUtil;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CreatePaymentIdActivity extends AppCompatActivity {

    // instance vars for user data
    private String userUid;
    private String userPaymentId;

    // validation
    private boolean isFormatValid = false;
    private boolean exists = false;

    // dependencies
    private UserViewModel userViewModel;
    private ActivityCreatePaymentIdBinding binding;
    private ProgressLoaderController progressLoader;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable pendingCheckRunnable = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreatePaymentIdBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        progressLoader = new ProgressLoaderController(this);

        setupUiActions();
        observeUserDataStatus();
        observePaymentIdAvailabilityStatus();
        observePaymentIdStoreStatus();
    }

    private void setupUiActions() {
        StatusBarUtil.setStatusBarColor(this, R.color.white);
        setupPaymentIdInputWatcher();
        setupEditTextFocusHandler();
        handleOnBackPressed();

        binding.btnContinue.setOnClickListener(view -> storeUserPaymentId(userPaymentId, userUid));
    }

    private void observeUserDataStatus() {
        userViewModel.getUserLiveData().observe(this, user -> {
            if (user == null) return;

            switch (user.getStatus()) {
                case SUCCESS -> onUserDataStatusSuccess(user.getData());
                case FAILURE -> {
                    // TODO: Handle error
                }
            }
        });
    }

    private void onUserDataStatusSuccess(UserUiModel user) {
        userUid = user.getUid();
    }

    private void observePaymentIdStoreStatus() {
        userViewModel.getSetPaymentIdLiveData().observe(this, resultEvent -> {
            Result<String> result = resultEvent.getContentIfNotHandled();
            if (result == null) return;

            switch (result.getStatus()) {
                case LOADING -> progressLoader.show();
                case SUCCESS -> onPaymentIdStoreStatusSuccess();
                case FAILURE -> onPaymentIdStoreStatusError(result.getErrorMessage());
            }
        });
    }

    private void onPaymentIdStoreStatusSuccess() {
        progressLoader.hide();
        UiUtil.showSuccessBottomSheetDialog(
                this,
                (dialog, dialogBinding) -> {
                    String title = "Success";
                    String message = "Your Payment ID was successfully created.";

                    dialogBinding.anim.playAnimation();
                    dialogBinding.title.setText(title);
                    dialogBinding.message.setText(message);
                    dialogBinding.btnContinue.setOnClickListener(view -> {
                        startActivity(new Intent(this, DashboardActivity.class));
                        finish();
                        dialog.dismiss();
                    });
                });
    }

    private void onPaymentIdStoreStatusError(String error) {
        binding.paymentIdAvailabilityFeedback.setText(error);
        progressLoader.hide();
    }

    private void observePaymentIdAvailabilityStatus() {
        userViewModel.getPaymentIdExistsStatus().observe(this, resultEvent -> {
            Result<Boolean> result = resultEvent.getContentIfNotHandled();
            if (result == null) return;

            switch (result.getStatus()) {
                case LOADING -> onPaymentIdAvailabilityCheckStatusLoading();
                case SUCCESS -> onPaymentIdAvailabilityCheckStatusSuccess(result.getData());
                case FAILURE -> onPaymentIdAvailabilityCheckStatusError(result.getErrorMessage());
            }
        });
    }

    private void onPaymentIdAvailabilityCheckStatusLoading() {
        binding.paymentIdAvailableCheck.setVisibility(View.GONE);
        binding.paymentIdProgressBar.show();
        binding.paymentIdProgressBar.setVisibility(View.VISIBLE);
    }

    private void onPaymentIdAvailabilityCheckStatusSuccess(boolean exists) {
        this.exists = exists;

        // Hide progress bar
        binding.paymentIdProgressBar.hide();
        binding.paymentIdProgressBar.setVisibility(View.GONE);

        binding.paymentIdAvailableCheck.setVisibility(!exists ? View.VISIBLE : View.GONE);

        String feedback = (!exists) ? "Available" : "Not Available";
        int feedbackColor = (!exists) ? ContextCompat.getColor(this, R.color.green) : ContextCompat.getColor(this, R.color.red);

        binding.paymentIdAvailabilityFeedback.setTextColor(feedbackColor);
        binding.paymentIdAvailabilityFeedback.setVisibility(View.VISIBLE);
        binding.paymentIdAvailabilityFeedback.setText(feedback);

        // Validate button state
        updateContinueButtonState();
    }

    private void onPaymentIdAvailabilityCheckStatusError(String error) {
        binding.txtError.setText(error);
        binding.txtError.setVisibility(View.VISIBLE);

        binding.paymentIdProgressBar.hide();
        binding.paymentIdProgressBar.setVisibility(View.GONE);
        binding.paymentIdAvailableCheck.setVisibility(View.GONE);
        binding.paymentIdAvailabilityFeedback.setVisibility(View.GONE);
    }

    private void storeUserPaymentId(String paymentId, String uid) {
        userViewModel.setPaymentId(paymentId, uid);
    }

    private void setupEditTextFocusHandler() {
        // cache drawables
        Drawable focus = ContextCompat.getDrawable(this, R.drawable.bg_edit_txt_custom_white_focused);
        Drawable notFocus = ContextCompat.getDrawable(this, R.drawable.bg_edit_txt_custom_white_not_focused);

        binding.editTxtPaymentId.setOnFocusChangeListener((view, hasFocus) -> binding.editTxtPaymentIdBackground.setBackground(hasFocus ? focus : notFocus));
    }

    private void setupPaymentIdInputWatcher() {
        binding.editTxtPaymentId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {

                setContinueButtonEnabled(false); // disable continue btn

                // don't trim
                validatePaymentIdRuleSet(s.toString());
                userPaymentId = s.toString();

                // Hide feedbacks
                binding.paymentIdAvailableCheck.setVisibility(View.GONE);
                binding.paymentIdAvailabilityFeedback.setVisibility(View.GONE);
                binding.txtError.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable e) {

                // don't trim
                String eString = e.toString();
                boolean shouldSearch = isFormatValid;

                // cancel any previous pending check
                if (pendingCheckRunnable != null) {
                    handler.removeCallbacks(pendingCheckRunnable);
                }

                if (shouldSearch) {
                    pendingCheckRunnable = () -> checkPaymentIdAvailability(eString);
                    handler.postDelayed(pendingCheckRunnable, 1500);
                }
            }
        });
    }

    private void checkPaymentIdAvailability(String paymentId) {
        userViewModel.checkPaymentIdExists(paymentId);
    }

    private void validatePaymentIdRuleSet(String paymentId) {
        // Cache drawables and colors
        Drawable validBg = ContextCompat.getDrawable(this, R.drawable.bg_8dp_green_light);
        Drawable invalidBg = ContextCompat.getDrawable(this, R.drawable.bg_8dp_gray_light);

        ColorStateList validIcon = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.green));
        ColorStateList invalidIcon = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.gray));

        int validText = ContextCompat.getColor(this, R.color.green);
        int invalidText = ContextCompat.getColor(this, R.color.gray);

        // Evaluate rules once
        boolean startsWith = startsWithLetter(paymentId);
        boolean hasMinimumLength = hasMinimumLength(paymentId);
        boolean isValidFormat = isAlphaNumericFormat(paymentId);

        // Starts with letter
        binding.layoutRuleStartWith.setBackground(startsWith ? validBg : invalidBg);
        binding.iconCheckRuleStartWith.setImageTintList(startsWith ? validIcon : invalidIcon);
        binding.txtRuleStartWith.setTextColor(startsWith ? validText : invalidText);

        // Minimum length
        binding.layoutRuleLength.setBackground(hasMinimumLength ? validBg : invalidBg);
        binding.icCheckRuleLength.setImageTintList(hasMinimumLength ? validIcon : invalidIcon);
        binding.txtRuleLength.setTextColor(hasMinimumLength ? validText : invalidText);

        // Alphanumeric format
        binding.layoutRuleContains.setBackground(isValidFormat ? validBg : invalidBg);
        binding.icCheckRuleContains.setImageTintList(isValidFormat ? validIcon : invalidIcon);
        binding.txtRuleContains.setTextColor(isValidFormat ? validText : invalidText);

        // Only update the flag here
        isFormatValid = startsWith && hasMinimumLength && isValidFormat;
    }

    private boolean startsWithLetter(String paymentId) {
        return paymentId.matches("^[A-Za-z].*");
    }

    private boolean hasMinimumLength(String paymentId) {
        return paymentId.length() >= 5 && paymentId.length() <= 20;
    }

    private boolean isAlphaNumericFormat(String paymentId) {
        return paymentId.matches("^[a-z0-9]+$");
    }

    private void updateContinueButtonState() {
        // Button only enabled when format is valid AND ID does not exist
        setContinueButtonEnabled(isFormatValid && !exists);
    }

    private void setContinueButtonEnabled(boolean enable) {
        binding.btnContinue.setEnabled(enable);
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

    public void handleOnBackPressed() {
        getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        // Prevent user from going back
                    }
                }
        );
    }
}