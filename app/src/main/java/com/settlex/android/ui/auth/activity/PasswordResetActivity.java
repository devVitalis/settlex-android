package com.settlex.android.ui.auth.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.settlex.android.R;
import com.settlex.android.databinding.ActivityPasswordResetBinding;
import com.settlex.android.ui.auth.components.OtpVerificationActivity;

public class PasswordResetActivity extends AppCompatActivity {
    private ActivityPasswordResetBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_reset);
        binding = ActivityPasswordResetBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupStatusBar();
        setupUIActions();

    }

    /*---------------------------
    Setup UI and Event Handlers
    ---------------------------*/
    private void setupUIActions() {
        emailEditTextWatcher();
        reEnableEmailEditTextFocus();
        setupEditTxtEmailFocusHandler();

        // Handle Click Listeners
        binding.imgBackBefore.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        binding.btnResetPassword.setOnClickListener(v -> startActivity(new Intent(this, OtpVerificationActivity.class)));
    }

    /*--------------------------------------
    ---TODO
    --------------------------------------*/
    private void emailEditTextWatcher() {
        binding.editTxtEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                boolean isEmpty = TextUtils.isEmpty(charSequence);
                binding.emailSectionHeader.setVisibility((!isEmpty) ? View.VISIBLE : View.INVISIBLE);

                updateResetPasswordBtn();
            }
        });
    }

    /*--------------------------------------
    Update btn State on Valid Email Address
    --------------------------------------*/
    private void updateResetPasswordBtn() {
        String email = binding.editTxtEmail.getText().toString().trim();

        boolean isEmailValid = Patterns.EMAIL_ADDRESS.matcher(email).matches();

        binding.btnResetPassword.setEnabled(isEmailValid);
    }

    /*--------------------------------------
    Enable focus on tap for Email EditText
    --------------------------------------*/
    private void reEnableEmailEditTextFocus() {
        View.OnClickListener enableFocusListener = v -> {
            if (v instanceof EditText editText) {
                editText.setFocusable(true);
                editText.setFocusableInTouchMode(true);
                editText.requestFocus();
            }
        };
        binding.editTxtEmail.setOnClickListener(enableFocusListener);
    }

    /*-------------------------------------------------
    Enable Dynamic Stroke Color on editText background
    | Via Focus Handler
    -------------------------------------------------*/
    private void setupEditTxtEmailFocusHandler() {
        binding.editTxtEmail.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) {
                binding.editTxtEmailBg.setBackgroundResource(R.drawable.bg_edit_txt_custom_gray_focused);
            } else {
                binding.editTxtEmailBg.setBackgroundResource(R.drawable.bg_edit_txt_custom_gray_not_focused);
            }
        });
    }

    /*---------------------------------------------
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