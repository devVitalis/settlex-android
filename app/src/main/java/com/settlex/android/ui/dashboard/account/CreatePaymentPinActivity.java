package com.settlex.android.ui.dashboard.account;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.settlex.android.databinding.ActivityCreatePaymentPinBinding;

public class CreatePaymentPinActivity extends AppCompatActivity {

    private ActivityCreatePaymentPinBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreatePaymentPinBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

    }

    private void pinValidation() {
        /**
         * Validation Step,Logic Check,Error Message
         * Step 1: Format Check,"Is the input exactly 4 numeric characters? (Uses matches(""^\\d{4}$""))","""PIN must be exactly 4 digits."""
         * Step 2: Consecutive Repeats,"Does it contain three or more identical digits in a row? (Uses matches(""(\\d)\\1{2,}""))","""PIN cannot contain 3 or more repeating digits (e.g., 111, 222)."""
         * Step 3: Ascending Sequences,Is the PIN a sequence like 1234 or 5678? (Uses a list of banned sequences.),"""PIN cannot be a simple sequence (e.g., 1234, 4321)."""
         * Step 4: Descending Sequences,Is the PIN a sequence like 4321 or 8765? (Uses a list of banned sequences.),"""PIN cannot be a simple sequence (e.g., 1234, 4321)."""
         * Final Result,If all checks pass.,Returns null (or empty string).
         */
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