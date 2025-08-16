package com.settlex.android.ui.auth.components;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.settlex.android.R;
import com.settlex.android.databinding.ActivityOtpVerificationBinding;
import com.settlex.android.ui.auth.activity.PasswordChangeActivity;
import com.settlex.android.ui.auth.util.AuthResult;
import com.settlex.android.ui.auth.viewmodel.AuthViewModel;
import com.settlex.android.ui.common.SettleXProgressBarController;
import com.settlex.android.util.StringUtil;

public class OtpVerificationActivity extends AppCompatActivity {
    private EditText[] otpCodeInputs;
    private AuthViewModel authViewModel;
    private ActivityOtpVerificationBinding binding;
    private SettleXProgressBarController progressBarController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);
        binding = ActivityOtpVerificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        progressBarController = new SettleXProgressBarController(binding.getRoot());

        setupStatusBar();
        setupUiActions();

        verifyEmailResetOtpObserver();
        sendEmailResetOtpObserver();
    }


    private void verifyEmailResetOtpObserver() {
        authViewModel.getVerifyEmailResetOtpResult().observe(this, event -> {
            AuthResult<String> result = event.getContentIfNotHandled();
            if (result != null) {
                switch (result.getStatus()) {
                    case LOADING -> progressBarController.show();
                    case SUCCESS -> handleVerifyEmailResetSuccess();
                    case ERROR -> handleOtpError(result.getMessage());
                }
            }
        });
    }

    private void sendEmailResetOtpObserver() {
        authViewModel.getSendEmailResetOtpResult().observe(this, event -> {
            AuthResult<String> result = event.getContentIfNotHandled();
            if (result != null) {
                switch (result.getStatus()) {
                    case LOADING -> progressBarController.show();
                    case SUCCESS -> handleSendEmailResetSuccess();
                    case ERROR -> handleOtpError(result.getMessage());
                }
            }
        });
    }

    private void handleVerifyEmailResetSuccess() {
        String email = getIntent().getStringExtra("email");

        Intent intent = new Intent(this, PasswordChangeActivity.class);
        intent.putExtra("email", email);
        startActivity(intent);
        finish();

        progressBarController.hide();
    }

    private void handleSendEmailResetSuccess() {
        startResendOtpCooldown();
        progressBarController.hide();
    }

    /**
     * Sets up UI elements and event handlers for the activity.
     */
    private void setupUiActions() {
        setupOtpInputBehavior();
        startResendOtpCooldown();
        maskAndDisplayUserEmail();

        binding.imgBackBefore.setOnClickListener(v -> finish());
        binding.btnResendOtp.setOnClickListener(view -> resendPasswordResetOtp());
        binding.btnConfirm.setOnClickListener(view -> verifyPasswordResetOtp());
    }

    /**
     * Verifies the OTP entered by the user.
     */
    private void verifyPasswordResetOtp() {
        String email = getIntent().getStringExtra("email");
        String otp = getEnteredOtpCode();

        authViewModel.verifyPasswordResetOtp(email, otp);
    }

    /**
     * Resends the password reset OTP to the user's email.
     */
    private void resendPasswordResetOtp() {
        String email = getIntent().getStringExtra("email");
        authViewModel.sendPasswordResetOtp(email);
    }

    /**
     * Displays an error message to the user.
     */
    private void handleOtpError(String message) {
        binding.txtOtpFeedback.setText(message);
        binding.txtOtpFeedback.setVisibility(View.VISIBLE);

        progressBarController.hide();
    }

    /**
     * Configures the OTP input fields with chaining logic for a smooth user experience.
     */
    private void setupOtpInputBehavior() {
        otpCodeInputs = new EditText[]{
                binding.otpDigit1, binding.otpDigit2, binding.otpDigit3,
                binding.otpDigit4, binding.otpDigit5, binding.otpDigit6
        };

        for (int i = 0; i < otpCodeInputs.length; i++) {
            EditText current = otpCodeInputs[i];
            EditText next = (i < otpCodeInputs.length - 1) ? otpCodeInputs[i + 1] : null;
            EditText prev = (i > 0) ? otpCodeInputs[i - 1] : null;

            current.setLongClickable(false);
            current.setTextIsSelectable(false);
            current.setEnabled(i == 0);

            current.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (isOtpFullyEntered()) hideSoftKeyboard();
                    if (TextUtils.isEmpty(s)) binding.txtOtpFeedback.setVisibility(View.GONE);

                    if (s.length() == 1 && next != null) {
                        next.setEnabled(true);
                        next.requestFocus();
                        next.setText("");
                    }

                    boolean isFilled = true;
                    for (EditText pin : otpCodeInputs) {
                        if (TextUtils.isEmpty(pin.getText())) {
                            isFilled = false;
                            break;
                        }
                    }
                    binding.btnConfirm.setEnabled(isFilled);
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });

            current.setOnKeyListener((v, keyCode, event) -> {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
                    if (!TextUtils.isEmpty(current.getText())) {
                        current.setText("");
                    } else if (prev != null) {
                        prev.setText("");
                        prev.requestFocus();
                        current.setEnabled(false);
                    }
                    return true;
                }
                return false;
            });
        }
    }

    /**
     * Checks if all OTP input fields have been filled.
     */
    private boolean isOtpFullyEntered() {
        for (EditText input : otpCodeInputs) {
            if (TextUtils.isEmpty(input.getText())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Reads the values from the OTP fields and combines them into a single string.
     */
    private String getEnteredOtpCode() {
        StringBuilder pin = new StringBuilder();
        for (EditText input : otpCodeInputs) {
            pin.append(input.getText().toString().trim());
        }
        return pin.toString();
    }

    /**
     * Starts a 60-second cooldown timer for the OTP resend button.
     */
    private void startResendOtpCooldown() {
        binding.btnResendOtp.setEnabled(false);
        binding.btnResendOtp.setTag(binding.btnResendOtp.getText());

        new CountDownTimer(60000, 1000) {

            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                if (seconds > 0) {
                    binding.btnResendOtp.setText(getString(R.string.resend_otp_countdown, seconds));
                }
            }

            public void onFinish() {
                CharSequence defaultTxt = (CharSequence) binding.btnResendOtp.getTag();
                if (defaultTxt != null) binding.btnResendOtp.setText(defaultTxt);
                binding.btnResendOtp.setEnabled(true);
            }
        }.start();
    }

    /**
     * Masks the user's email address and displays it on the screen.
     */
    private void maskAndDisplayUserEmail() {
        String email = StringUtil.maskEmail(getIntent().getStringExtra("email"));
        binding.txtUserEmail.setText(email);
    }

    /**
     * Hides the software keyboard.
     */
    private void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = getCurrentFocus();
        if (view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * Dismisses the keyboard and clears focus if the user taps outside an EditText.
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View currentFocus = getCurrentFocus();
            if (currentFocus instanceof EditText) {
                Rect outRect = new Rect();
                currentFocus.getGlobalVisibleRect(outRect);

                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    currentFocus.clearFocus();

                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
                    }

                    binding.main.requestFocus();
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    /**
     * Configures the status bar to match the activity's design.
     */
    private void setupStatusBar() {
        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.white));

        View decor = window.getDecorView();
        int flags = decor.getSystemUiVisibility();
        flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        decor.setSystemUiVisibility(flags);
    }
}