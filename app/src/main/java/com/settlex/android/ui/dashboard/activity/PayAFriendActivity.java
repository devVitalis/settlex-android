package com.settlex.android.ui.dashboard.activity;

import android.content.Context;
import android.content.Intent;
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
import com.settlex.android.data.enums.TransactionServiceType;
import com.settlex.android.databinding.ActivityPayAfriendBinding;
import com.settlex.android.ui.common.util.SettleXProgressBarController;
import com.settlex.android.ui.dashboard.adapter.SuggestionAdapter;
import com.settlex.android.ui.dashboard.model.SuggestionsUiModel;
import com.settlex.android.ui.dashboard.model.UserUiModel;
import com.settlex.android.ui.dashboard.util.DashboardUiUtil;
import com.settlex.android.ui.dashboard.util.TxnIdGenerator;
import com.settlex.android.ui.dashboard.viewmodel.DashboardViewModel;
import com.settlex.android.util.event.Result;
import com.settlex.android.util.string.StringUtil;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class PayAFriendActivity extends AppCompatActivity {
    private ActivityPayAfriendBinding binding;
    private SettleXProgressBarController progressBarController;
    private SuggestionAdapter suggestionAdapter;
    private DashboardViewModel dashboardViewModel;
    private UserUiModel currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPayAfriendBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressBarController = new SettleXProgressBarController(binding.getRoot());
        dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
        suggestionAdapter = new SuggestionAdapter();

        setupStatusBar();
        setupUiActions();
        setupSuggestionRecyclerView();

        // OBSERVERS
        observeUserData();
        observeSendMoney();
        observeUserSuggestions();
    }

    private void observeUserData() {
        dashboardViewModel.getAuthState().observe(this, authState -> {
            if (authState == null) return;
            dashboardViewModel.getUserData(authState.getUid()).observe(this, user -> this.currentUser = user);
        });
    }

    private void observeSendMoney() {
        dashboardViewModel.getPayFriendResult().observe(this, event -> {
            Result<String> result = event.getContentIfNotHandled();
            if (result == null) return;
            switch (result.getStatus()) {
                case LOADING -> progressBarController.show();
                case SUCCESS -> onSendMoneySuccess();
                case ERROR -> onSendMoneyFailed();
            }
        });
    }

    private void onSendMoneySuccess() {
        startActivity(new Intent(this, TxnStatusActivity.class));
        finish();
        progressBarController.hide();
    }

    private void onSendMoneyFailed() {
        progressBarController.hide();
    }

    private void showPayConfirmation(double senderBalance, double senderCommBalance, String senderUid, String senderUsername) {
        String recipientName = binding.selectedRecipientName.getText().toString();
        String recipientUsername = StringUtil.removeAtInUsername(binding.selectedRecipientUsername.getText().toString().trim());
        double amountToSend = Double.parseDouble(binding.editTxtAmount.getText().toString().replaceAll(",", ""));
        String description = binding.editTxtDescription.getText().toString().trim();

        DashboardUiUtil.showPayConfirmation(
                this,
                recipientUsername,
                R.drawable.ic_avatar, // TODO: setup profile pic with real data
                recipientName,
                amountToSend,
                senderBalance,
                senderCommBalance,
                () -> initPayAFriend(senderUid, recipientUsername, amountToSend, senderUsername, description)
        );
    }

    private void initPayAFriend(String senderUid, String recipientUsername, double amountToSend, String senderUsername, String description) {
        dashboardViewModel.payFriend(
                senderUid,
                recipientUsername,
                TxnIdGenerator.generate(senderUsername),
                amountToSend,
                String.valueOf(TransactionServiceType.PAY_A_FRIEND),
                description
        );
    }

    private void observeUserSuggestions() {
        dashboardViewModel.getUsernameSuggestion().observe(this, suggestions -> {
            if (suggestions == null) return;

            switch (suggestions.getStatus()) {
                case LOADING -> onSuggestionResultLoading();
                case SUCCESS -> onSuggestionResultSuccess(suggestions.getData());
                case ERROR -> onSuggestionResultError();
            }
        });
    }

    private void searchUsername(String query) {
        dashboardViewModel.searchUsername(query);
    }

    private void onSuggestionResultLoading() {
        suggestionAdapter.submitList(Collections.emptyList());
        binding.suggestionsRecyclerView.setVisibility(View.GONE);

        binding.selectedRecipient.setVisibility(View.GONE);
        binding.txtUsernameFeedback.setVisibility(View.GONE);

        binding.shimmerLayout.startShimmer();
        binding.shimmerLayout.setVisibility(View.VISIBLE);
    }

    private void onSuggestionResultSuccess(List<SuggestionsUiModel> suggestions) {
        if (suggestions == null || suggestions.isEmpty()) {
            String username = StringUtil.addAtToUsername(binding.editTxtUsername.getText().toString().trim().toLowerCase());
            binding.txtUsernameFeedback.setText(getString(R.string.No_user_found_with_tag, username));
            binding.txtUsernameFeedback.setVisibility(View.VISIBLE);
        }

        binding.shimmerLayout.stopShimmer();
        binding.shimmerLayout.setVisibility(View.GONE);

        suggestionAdapter.submitList(suggestions);
        binding.suggestionsRecyclerView.setVisibility(View.VISIBLE);
        binding.suggestionsRecyclerView.setAdapter(suggestionAdapter);
    }

    private void onSuggestionResultError() {
        binding.shimmerLayout.stopShimmer();
        binding.shimmerLayout.setVisibility(View.GONE);
    }

    private void setupSuggestionRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        binding.suggestionsRecyclerView.setLayoutManager(layoutManager);

        handleOnSuggestionClick();
    }

    private void handleOnSuggestionClick() {
        suggestionAdapter.setOnItemClickListener(model -> {
            binding.editTxtUsername.setText(StringUtil.removeAtInUsername(model.getUsername()));
            binding.editTxtUsername.setSelection(binding.editTxtUsername.getText().length());

            suggestionAdapter.submitList(Collections.emptyList());
            binding.suggestionsRecyclerView.setVisibility(View.GONE);

            binding.selectedRecipientName.setText(model.getFullName().toUpperCase());
            binding.selectedRecipientUsername.setText(model.getUsername());
            binding.selectedRecipient.setVisibility(View.VISIBLE);
        });
    }

    private void setupUiActions() {
        setupEditTextFocusHandlers();
        setupTextInputWatcher();
        attachCurrencyFormatter(binding.editTxtAmount);

        // Listeners
        binding.imgBackBefore.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        binding.btnVerify.setOnClickListener(v -> searchUsername(binding.editTxtUsername.getText().toString().trim().toLowerCase()));
        binding.btnNext.setOnClickListener(v -> showPayConfirmation(
                currentUser.getBalance(),
                currentUser.getCommissionBalance(),
                currentUser.getUid(),
                currentUser.getUsername()));
    }

    private void setupTextInputWatcher() {
        binding.editTxtUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                boolean shouldSearch = editable.toString().trim().length() >= 3;
                binding.btnVerify.setVisibility((shouldSearch) ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.txtUsernameFeedback.setVisibility(View.GONE);
                binding.selectedRecipient.setVisibility(View.GONE);
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
        boolean recipientConfirmed = binding.selectedRecipient.getVisibility() == View.VISIBLE;

        updateNextButtonState(validateUsername(username) && validateAmount(amountText) && recipientConfirmed);
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