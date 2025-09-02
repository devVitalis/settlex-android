package com.settlex.android.ui.dashboard.activity;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.settlex.android.R;
import com.settlex.android.databinding.ActivityPayAfriendBinding;
import com.settlex.android.ui.dashboard.adapter.SuggestionAdapter;
import com.settlex.android.ui.dashboard.util.DashboardUiUtil;
import com.settlex.android.ui.dashboard.viewmodel.DashboardViewModel;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class PayAFriendActivity extends AppCompatActivity {
    private ActivityPayAfriendBinding binding;
    private DashboardViewModel dashboardViewModel;
    private SuggestionAdapter suggestionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPayAfriendBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
        suggestionAdapter = new SuggestionAdapter();

        setupStatusBar();
        setupUiActions();
        setupRecyclerViewLayout();

        // OBSERVERS
        observeUserSuggestions();
    }

    private void observeUserSuggestions() {
        dashboardViewModel.getUsernameSuggestion().observe(this, suggestions -> {
            if (suggestions != null) {
                switch (suggestions.getStatus()) {
                    case LOADING -> binding.shimmerLayout.setVisibility(View.VISIBLE);
                    case SUCCESS -> {
                        suggestionAdapter.submitList(suggestions.getData());
                        binding.shimmerLayout.setVisibility(View.GONE);
                        binding.suggestionsRecyclerView.setVisibility(View.VISIBLE);
                    }
                    case ERROR -> binding.shimmerLayout.setVisibility(View.GONE);
                }
            }
        });
    }

    private void setupRecyclerViewLayout() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        binding.suggestionsRecyclerView.setLayoutManager(layoutManager);
    }

    private void setupUiActions() {
        setupEditTextFocusHandlers();
        setupTextInputWatcher();
        attachCurrencyFormatter(binding.editTxtAmount);

        binding.imgBackBefore.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());
        binding.btnNext.setOnClickListener(view -> {
            DashboardUiUtil.showPayConfirmation(
                    this,
                    "@vitalis",
                    R.drawable.ic_avatar,
                    "BENJAMIN NNAEMEKA",
                    "₦50,500.00",
                    "₦198,535.57",
                    "(₦201,000.32)",
                    "(₦201,000.32)",
                    "(₦456.78)",
                    new Runnable() {
                        @Override
                        public void run() {

                        }
                    }
            );
        });
    }

    private void setupTextInputWatcher() {
        binding.editTxtUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                boolean shouldSearch = editable.length() >= 3;
                if (shouldSearch) dashboardViewModel.searchUsername(editable.toString());
                binding.suggestionsRecyclerView.setVisibility(shouldSearch ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.txtUsernameFeedback.setVisibility((!validateUsername(s.toString().toLowerCase()) && !s.toString().isEmpty()) ? View.VISIBLE : View.GONE);
                setupInputValidation();
            }
        });

        binding.editTxtAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.txtAmountFeedback.setVisibility((!validateAmount(s.toString()) && !s.toString().isEmpty()) ? View.VISIBLE : View.GONE);
                setupInputValidation();
            }
        });
    }

    private void setupInputValidation() {
        String username = binding.editTxtUsername.getText().toString().trim().toLowerCase();
        String amountText = binding.editTxtAmount.getText().toString().trim();

        updateNextButtonState(validateUsername(username) && validateAmount(amountText));
    }

    private boolean validateUsername(String username) {
        return username.matches("^[a-z0-9]([a-z0-9]*[._]?[a-z0-9]*)+[a-z0-9]$");
    }

    private boolean validateAmount(String amountText) {
        String cleanAmount = amountText.replaceAll("[,\\s₦]", "");
        if (!cleanAmount.matches("^[1-9]\\d*(\\.\\d+)?$")) return false;

        try {
            double amount = Double.parseDouble(cleanAmount);
            return amount >= 100 && amount <= 500_000;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    private void updateNextButtonState(boolean allValid) {
        binding.btnNext.setEnabled(allValid);
    }

    /**
     * Attaches a money formatter to an EditText.
     * <p>
     * Behavior:
     * - While typing: shows thousand separators, preserves decimals if typed.
     * - On blur: normalizes to 2 decimal places. If no decimals typed, appends ".00".
     * - On refocus: removes ".00" if it was auto-added (but preserves user decimals).
     */
    public static void attachCurrencyFormatter(EditText editTextAmount) {
        final Locale locale = Locale.US; // Or pass in as parameter
        final NumberFormat integerFormatter = NumberFormat.getIntegerInstance(locale);
        final DecimalFormat twoDecimalFormatter = new DecimalFormat("#,##0.00");

        editTextAmount.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        editTextAmount.addTextChangedListener(new TextWatcher() {
            private boolean isEditing = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (isEditing) return;

                String rawInput = editable.toString();
                if (rawInput.isEmpty()) return;

                String cleanInput = rawInput.replace(",", "");
                if (cleanInput.equals(".") || cleanInput.equals("-") || cleanInput.equals("-."))
                    return;

                try {
                    // Split integer and decimal parts
                    String[] parts = cleanInput.split("\\.", -1);
                    String integerPart = parts[0].replaceFirst("^0+(?!$)", "");
                    String decimalPart = (parts.length > 1) ? parts[1] : null;

                    // Format integer part with grouping
                    BigInteger bigInt = new BigInteger(integerPart.isEmpty() ? "0" : integerPart);
                    String groupedInteger = integerFormatter.format(bigInt);

                    String formattedValue = (decimalPart != null)
                            ? (groupedInteger + "." + decimalPart)
                            : groupedInteger;

                    if (!formattedValue.equals(rawInput)) {
                        isEditing = true;
                        int cursorPos = editTextAmount.getSelectionStart();
                        editTextAmount.setText(formattedValue);

                        int diff = formattedValue.length() - rawInput.length();
                        int newCursorPos = Math.max(0, Math.min(formattedValue.length(), cursorPos + diff));
                        editTextAmount.setSelection(newCursorPos);

                        isEditing = false;
                    }
                } catch (Exception ignored) {
                }
            }
        });

        editTextAmount.setOnFocusChangeListener((v, hasFocus) -> {
            String text = editTextAmount.getText().toString().replace(",", "");
            if (text.isEmpty() || text.equals(".") || text.equals("-") || text.equals("-.")) return;

            try {
                double value = Double.parseDouble(text);

                if (hasFocus) {
                    // Remove auto-added ".00" if user never typed decimals
                    String current = editTextAmount.getText().toString();
                    if (current.endsWith(".00") && !text.contains(".")) {
                        String stripped = current.substring(0, current.length() - 3);
                        editTextAmount.setText(stripped);
                        editTextAmount.setSelection(stripped.length());
                    }
                } else {
                    // Normalize on blur
                    String raw = editTextAmount.getText().toString();
                    String finalText;
                    if (raw.contains(".")) {
                        finalText = twoDecimalFormatter.format(value); // preserve & normalize decimals
                    } else {
                        finalText = String.format(locale, "%,.2f", value); // append .00
                    }
                    editTextAmount.setText(finalText);
                    editTextAmount.setSelection(finalText.length());
                }
            } catch (NumberFormatException ignored) {
            }
        });
    }

    private void setupEditTextFocusHandlers() {
        View.OnClickListener focusListener = v -> {
            if (v instanceof EditText) {
                v.setFocusableInTouchMode(true);
                v.setFocusable(true);
                v.requestFocus();
            }
        };
        binding.editTxtUsername.setOnClickListener(focusListener);
        binding.editTxtAmount.setOnClickListener(focusListener);
        binding.editTxtDescription.setOnClickListener(focusListener);

        binding.editTxtUsername.setOnFocusChangeListener((v, hasFocus) -> {
            int backgroundRes = hasFocus ? R.drawable.bg_edit_txt_custom_white_focused : R.drawable.bg_edit_txt_custom_gray_not_focused;
            binding.editTxtUsernameBackground.setBackgroundResource(backgroundRes);
        });

        binding.editTxtAmount.setOnFocusChangeListener((v, hasFocus) -> {
            int backgroundRes = hasFocus ? R.drawable.bg_edit_txt_custom_white_focused : R.drawable.bg_edit_txt_custom_gray_not_focused;
            binding.editTxtAmountBackground.setBackgroundResource(backgroundRes);
        });

        binding.editTxtDescription.setOnFocusChangeListener((v, hasFocus) -> {
            int backgroundRes = hasFocus ? R.drawable.bg_edit_txt_custom_white_focused : R.drawable.bg_edit_txt_custom_gray_not_focused;
            binding.editTxtDescriptionBackground.setBackgroundResource(backgroundRes);
        });
    }

    private void setupStatusBar() {
        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        View decorView = window.getDecorView();
        decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View focus = getCurrentFocus();
            if (focus instanceof EditText) {
                Rect rect = new Rect();
                focus.getGlobalVisibleRect(rect);
                if (!rect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    focus.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) imm.hideSoftInputFromWindow(focus.getWindowToken(), 0);
                    binding.main.requestFocus();
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }
}