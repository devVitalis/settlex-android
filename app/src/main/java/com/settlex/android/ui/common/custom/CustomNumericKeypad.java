package com.settlex.android.ui.common.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.settlex.android.databinding.ItemNumericKeypadBinding;

public class CustomNumericKeypad extends LinearLayout {

    private ItemNumericKeypadBinding binding;
    private OnKeypadInputListener listener;

    public CustomNumericKeypad(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        binding = ItemNumericKeypadBinding.inflate(LayoutInflater.from(context), this, true);
        setupNumericKeypad();
    }

    private void setupNumericKeypad() {
        // Loop through all the child button in the grid
        for (int i = 0; i < binding.keyboardGrid.getChildCount(); i++) {
            View key = binding.keyboardGrid.getChildAt(i);
            if (key instanceof MaterialButton btn) {
                String text = btn.getText().toString();

                if (!text.isEmpty() && text.matches("\\d")) {
                    btn.setOnClickListener(v -> {
                        if (listener != null) listener.onNumberPressed(text);
                    });
                }
            }
        }

        binding.btnKeyDelete.setOnClickListener(view -> {
            if (listener != null) listener.onDeletePressed();
        });

        binding.btnKeyDone.setOnClickListener(view -> {
            if (listener != null) listener.onDonePressed();
        });
    }

    public void setOnKeypadInputListener(OnKeypadInputListener listener) {
        this.listener = listener;
    }

    public interface OnKeypadInputListener {
        void onNumberPressed(String number);

        void onDeletePressed();

        void onDonePressed();
    }
}
