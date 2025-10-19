package com.settlex.android.ui.dashboard.fragment.home.activity;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.settlex.android.R;
import com.settlex.android.databinding.ActivityCreatePaymentIdBinding;
import com.settlex.android.util.ui.StatusBarUtil;

import java.util.regex.Pattern;

public class CreatePaymentIdActivity extends AppCompatActivity {
    private ActivityCreatePaymentIdBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreatePaymentIdBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupUiActions();
    }

    private void setupUiActions() {
        StatusBarUtil.setStatusBarColor(this, R.color.white);
        setupPaymentIdInputWatcher();
    }

    private void setupPaymentIdInputWatcher() {
        binding.editTxtPaymentId.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {

            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                validateRuleSet(s.toString().trim());
            }
        });
    }

    private void validateRuleSet(String paymentId) {
        binding.error.setText(validatePaymentIdRequirements(paymentId));
        binding.error.setVisibility(!isPaymentIdValid(paymentId) ? View.VISIBLE : View.GONE);

        // Cache drawables and colors
        Drawable validBg = ContextCompat.getDrawable(this, R.drawable.bg_24dp_green_light);
        Drawable invalidBg = ContextCompat.getDrawable(this, R.drawable.bg_24dp_semi_transparent_black10);

        ColorStateList validIcon = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.green));
        ColorStateList invalidIcon = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.gray));

        int validText = ContextCompat.getColor(this, R.color.green);
        int invalidText = ContextCompat.getColor(this, R.color.gray);

        // Evaluate rules once
        boolean startsWith = startsWithLetter(paymentId);
        boolean hasLength = hasMinimumLength(paymentId);
        boolean isValidFormat = isValidAlphaNumericFormat(paymentId);

        // Starts with letter
        binding.layoutRuleStartWith.setBackground(startsWith ? validBg : invalidBg);
        binding.iconCheckRuleStartWith.setImageTintList(startsWith ? validIcon : invalidIcon);
        binding.txtRuleStartWith.setTextColor(startsWith ? validText : invalidText);

        // Minimum length
        binding.layoutRuleLength.setBackground(hasLength ? validBg : invalidBg);
        binding.icCheckRuleLength.setImageTintList(hasLength ? validIcon : invalidIcon);
        binding.txtRuleLength.setTextColor(hasLength ? validText : invalidText);

        // Alphanumeric format
        binding.layoutRuleContains.setBackground(isValidFormat ? validBg : invalidBg);
        binding.icCheckRuleContains.setImageTintList(isValidFormat ? validIcon : invalidIcon);
        binding.txtRuleContains.setTextColor(isValidFormat ? validText : invalidText);
    }

    private boolean isPaymentIdValid(String paymentId) {
        return paymentId.matches("^[a-z0-9]([a-z0-9]*[._]?[a-z0-9]*)+[a-z0-9]$");
    }

    private boolean startsWithLetter(String paymentId) {
        return paymentId.matches("^[A-Za-z].*");
    }

    private boolean hasMinimumLength(String paymentId) {
        return paymentId.length() >= 3;
    }

    private boolean isValidAlphaNumericFormat(String paymentId) {
        if (!hasMinimumLength(paymentId)) {
            return false;
        }
        return paymentId.matches("^[A-Za-z0-9_]+$");
    }

    private String validatePaymentIdRequirements(String paymentId) {
        String PAYMENT_ID_REGEX = "^[a-z0-9]([a-z0-9]*[._]?[a-z0-9]*)+[a-z0-9]$";
        Pattern PAYMENT_ID_PATTERN = Pattern.compile(PAYMENT_ID_REGEX);

        // Check Minimum Length (Must be >= 3 characters)
        if (paymentId.length() < 3) {
            return "Payment ID must be at least 3 characters long.";
        }

        if (!PAYMENT_ID_PATTERN.matcher(paymentId).matches()) {

            if (paymentId.startsWith(".") || paymentId.endsWith(".")) {
                return "Payment ID cannot start or end with '.'";
            }
            if (paymentId.startsWith("_") || paymentId.endsWith("_")) {
                return "Payment ID cannot start or end with '_'";
            }

            if (paymentId.contains("..") || paymentId.contains("__") || paymentId.contains("._") || paymentId.contains("_.")) {
                return "Payment ID cannot contain consecutive '.' or '_' characters";
            }

            return "Payment ID can only contain lowercase letters, numbers, and single periods or underscores";
        }

        // All checks passed
        return null;
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