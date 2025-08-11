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
import android.widget.Toast;

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
    private ActivityOtpVerificationBinding binding;
    private AuthViewModel vm;
    private EditText[] otpCodeInputs;
    private SettleXProgressBarController progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);
        binding = ActivityOtpVerificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        vm = new ViewModelProvider(this).get(AuthViewModel.class);
        progressBar = new SettleXProgressBarController(binding.getRoot());

        setupStatusBar();
        setupUIActions();

        vm.getSendResetOtpResult().observe(this, event -> {
            AuthResult<String> result = event.getContentIfNotHandled();
            if (result != null) {
                switch (result.getStatus()) {
                    case LOADING -> progressBar.show();

                    case SUCCESS -> {
                        disableResendOtpBtn();
                        progressBar.hide();
                    }

                    case ERROR -> {
                        showError(result.getMessage());
                        progressBar.hide();
                    }
                }
            }
        });

        vm.getVerifyResetOtpResult().observe(this, event -> {
            AuthResult<String> result = event.getContentIfNotHandled();
            if (result != null) {
                switch (result.getStatus()) {
                    case LOADING -> progressBar.show();

                    case SUCCESS -> {
                        startActivity(new Intent(this, PasswordChangeActivity.class));
                        progressBar.hide();
                    }

                    case ERROR -> {
                        showError(result.getMessage());
                        progressBar.hide();
                    }
                }
            }
        });
    }

    /*---------------------------
    Setup UI and Event Handlers
    ---------------------------*/
    private void setupUIActions() {
        setupOtpCodeInputs();
        disableResendOtpBtn();
        maskAndDisplayUserEmail();

        // Handle Click Listeners
        binding.imgBackBefore.setOnClickListener(v -> finish());
        binding.btnResendOtp.setOnClickListener(view -> resendPasswordResetOtp());
        binding.btnConfirm.setOnClickListener(view -> verifyPasswordResetOtp());
    }

    /*--------------------------------------
    Setup Otp input chaining logic
    --------------------------------------*/
    private void verifyPasswordResetOtp() {
        String email = getIntent().getStringExtra("email");
        String otp = getEnteredOtpCode();

        vm.verifyPasswordResetOtp(email, otp);
    }

    /*------------------------------------------
    Resend Password Reset Verification OTP email
    ------------------------------------------*/
    private void resendPasswordResetOtp() {
        String email = getIntent().getStringExtra("email");
        vm.sendPasswordResetOtp(email);
    }

    /*-------------------------------------------
    Helper Method to Display Error Info to User
    -------------------------------------------*/
    private void showError(String message) {
        binding.txtOtpFeedback.setText(message);
        binding.txtOtpFeedback.setVisibility(View.VISIBLE);
    }

    /*--------------------------------------
    Setup Otp input chaining logic
    --------------------------------------*/
    private void setupOtpCodeInputs() {
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
                    if (isOtpFullyEntered()) hideKeyboard();
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

    /*--------------------------------------------
    Check if all (OTP) input fields are filled
    --------------------------------------------*/
    private boolean isOtpFullyEntered() {
        for (EditText input : otpCodeInputs) {
            if (TextUtils.isEmpty(input.getText())) {
                return false;
            }
        }
        return true;
    }

    /*-------------------------------
    Read and join Otp field values
    -------------------------------*/
    private String getEnteredOtpCode() {
        StringBuilder pin = new StringBuilder();
        for (EditText input : otpCodeInputs) {
            pin.append(input.getText().toString().trim());
        }
        return pin.toString();
    }

    /*------------------------------
    Disable Resend Btn, 60s (1min)
    ------------------------------*/
    private void disableResendOtpBtn() {
        binding.btnResendOtp.setEnabled(false);
        binding.btnResendOtp.setTag(binding.btnResendOtp.getText());

        new CountDownTimer(60000, 1000) {

            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                if (seconds > 0) {
                    binding.btnResendOtp.setText("Resend in " + seconds);
                }
            }

            public void onFinish() {
                CharSequence defaultTxt = (CharSequence) binding.btnResendOtp.getTag();
                if (defaultTxt != null) binding.btnResendOtp.setText(defaultTxt);
                binding.btnResendOtp.setEnabled(true);
            }
        }.start();
    }

    /*--------------------------------------------------
    Retrieve user email from intent, mask it, and show
    ---------------------------------------------------*/
    private void maskAndDisplayUserEmail() {
        String email = StringUtil.maskEmail(getIntent().getStringExtra("email"));
        binding.txtUserEmail.setText(email);
    }

    /*----------------------
    Hide (System) Keyboard
    -----------------------*/
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = getCurrentFocus();
        if (view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /*----------------------------------------------
    Dismiss keyboard and clear focus on outside tap
    ----------------------------------------------*/
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

    /*-------------------------------------
    Set up status bar appearance and flags
    -------------------------------------*/
    private void setupStatusBar() {
        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.white));

        View decor = window.getDecorView();
        int flags = decor.getSystemUiVisibility();
        flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        decor.setSystemUiVisibility(flags);
    }
}