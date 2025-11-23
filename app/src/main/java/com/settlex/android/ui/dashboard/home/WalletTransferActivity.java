package com.settlex.android.ui.dashboard.home;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.settlex.android.R;
import com.settlex.android.data.enums.TransactionServiceType;
import com.settlex.android.data.enums.TransactionStatus;
import com.settlex.android.data.remote.profile.ProfileService;
import com.settlex.android.databinding.ActivityWalletTransferBinding;
import com.settlex.android.ui.dashboard.account.CreatePaymentPinActivity;
import com.settlex.android.ui.dashboard.adapter.RecipientAdapter;
import com.settlex.android.ui.dashboard.model.RecipientUiModel;
import com.settlex.android.ui.dashboard.model.UserUiModel;
import com.settlex.android.ui.dashboard.util.DialogHelper;
import com.settlex.android.ui.dashboard.util.TransactionIdGenerator;
import com.settlex.android.ui.dashboard.viewmodel.TransactionViewModel;
import com.settlex.android.ui.dashboard.viewmodel.UserViewModel;
import com.settlex.android.ui.common.state.UiState;
import com.settlex.android.util.string.CurrencyFormatter;
import com.settlex.android.util.string.StringFormatter;
import com.settlex.android.util.ui.ProgressLoaderController;
import com.settlex.android.util.ui.StatusBar;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class WalletTransferActivity extends AppCompatActivity {

    private java.lang.String recipientPhotoUrl;
    private java.lang.String recipientPaymentId;
    private long amountToSend;
    private boolean isPinVerified = false;

    // dependencies
    private ActivityWalletTransferBinding binding;
    private ProgressLoaderController progressLoader;
    private RecipientAdapter recipientAdapter;
    private UserViewModel userViewModel;
    private TransactionViewModel transactionViewModel;
    private BottomSheetDialog bottomSheetDialog;
    private UserUiModel currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWalletTransferBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);

        progressLoader = new ProgressLoaderController(this);
        recipientAdapter = new RecipientAdapter();

        setupUiActions();
        observeUserAuthState();
    }

    private void setupUiActions() {
        StatusBar.setColor(this, R.color.white);
        setupRecipientRecyclerView();
        setupTextInputWatcher();
        setupEditTextFocusHandlers();
        clearFocusOnLastEditTextField();

        binding.btnBackBefore.setOnClickListener(v -> this.getOnBackPressedDispatcher().onBackPressed());
        binding.btnVerify.setOnClickListener(v -> searchRecipient(recipientPaymentId.replaceAll("\\s+", "")));
        binding.btnNext.setOnClickListener(v -> startPaymentProcess());
    }

    private void observeUserAuthState() {
        userViewModel.getAuthStateLiveData().observe(this, uid -> {
            if (uid == null) {
                // logged out/session expired
                return;
            }
            // user is logged in fetch data
            observeUserDataStatus();
            observePayFriendStatus();
            observeRecipientSearchStatus();
            observeVerifyPaymentPinStatus();
        });
    }

    private void observeUserDataStatus() {
        userViewModel.getUserLiveData().observe(this, userData -> {
            if (userData == null) return;

            switch (userData.status) {
                case SUCCESS -> onUserDataStatusSuccess(userData.data);
                case FAILURE -> {
                    // Handle error
                }
            }
        });
    }

    private void onUserDataStatusSuccess(UserUiModel user) {
        if (user == null) return;
        binding.availableBalance.setText(CurrencyFormatter.formatToNaira(user.balance + user.commissionBalance));
        this.currentUser = user;
    }

    private void observePayFriendStatus() {
        transactionViewModel.getTransferFundsLiveData().observe(this, event -> {
            UiState<java.lang.String> result = event.getContentIfNotHandled();
            if (result == null) return;

            switch (result.status) {
                case LOADING -> progressLoader.show();
                // case PENDING -> onTransactionStatusPending();
                case SUCCESS -> onTransactionStatusSuccess();
                case FAILURE -> onTransactionStatusFailed(result.getError());
            }
        });
    }

    private void onTransactionStatusPending() {
        navigateToTransactionStatusActivity(TransactionStatus.PENDING, null);
        progressLoader.hide();
    }

    private void onTransactionStatusSuccess() {
        navigateToTransactionStatusActivity(TransactionStatus.SUCCESS, null);
        progressLoader.hide();
    }

    private void onTransactionStatusFailed(java.lang.String error) {
        navigateToTransactionStatusActivity(TransactionStatus.FAILED, error);
        progressLoader.hide();
    }

    private void navigateToTransactionStatusActivity(TransactionStatus transactionStatus, java.lang.String feedback) {
        bottomSheetDialog.dismiss();
        java.lang.String formattedAmount = CurrencyFormatter.formatToNaira(this.amountToSend);

        // Pass txn data
        Intent intent = new Intent(this, TransactionStatusActivity.class);
        intent.putExtra("amount", formattedAmount);
        intent.putExtra("status", transactionStatus.name());
        intent.putExtra("message", feedback);
        startActivity(intent);
        finish();
    }

    private void observeRecipientSearchStatus() {
        transactionViewModel.getRecipientSearchResult().observe(this, result -> {
            if (result == null) return;

            switch (result.status) {
                case LOADING -> onRecipientSearchStatusLoading();
                case SUCCESS -> onRecipientSearchStatusSuccess(result.data);
                case FAILURE -> onRecipientSearchStatusFailed();
            }
        });
    }

    private void onRecipientSearchStatusLoading() {
        // reset adapter
        recipientAdapter.submitList(Collections.emptyList());
        binding.recipientRecyclerView.setVisibility(View.GONE);

        // hide selected recipient
        binding.selectedRecipient.setVisibility(View.GONE);
        binding.txtError.setVisibility(View.GONE);
        updateNextButtonState();

        binding.shimmerEffect.startShimmer();
        binding.shimmerEffect.setVisibility(View.VISIBLE);
    }

    private void onRecipientSearchStatusSuccess(List<RecipientUiModel> recipient) {
        if (recipient == null || recipient.isEmpty()) {
            // Recipient not found
            recipientAdapter.submitList(Collections.emptyList());

            java.lang.String paymentId = StringFormatter.addAtToPaymentId(this.recipientPaymentId);
            java.lang.String ERROR_NO_USER_FOUND = "No user found with Payment ID " + paymentId;

            binding.txtError.setText(ERROR_NO_USER_FOUND);
            binding.txtError.setVisibility(View.VISIBLE);
        }

        // Recipient found
        binding.shimmerEffect.stopShimmer();
        binding.shimmerEffect.setVisibility(View.GONE);

        recipientAdapter.submitList(recipient);
        binding.recipientRecyclerView.setVisibility(View.VISIBLE);
        binding.recipientRecyclerView.setAdapter(recipientAdapter);
    }

    private void onRecipientSearchStatusFailed() {
        binding.shimmerEffect.stopShimmer();
        binding.shimmerEffect.setVisibility(View.GONE);
    }

    private void observeVerifyPaymentPinStatus() {
        userViewModel.getVerifyPaymentPinLiveData().observe(this, event -> {
            UiState<Boolean> result = event.getContentIfNotHandled();
            if (result == null) return;

            switch (result.status) {
                case LOADING -> progressLoader.show();
                case SUCCESS -> onVerifyPaymentPinStatusSuccess(result.data);
                case FAILURE -> onVerifyPaymentPinStatusError(result.getError());
            }
        });
    }

    private void onVerifyPaymentPinStatusSuccess(boolean isVerified) {
        progressLoader.hide();

        if (!isVerified) {
            showMessageDialog();
            return;
        }

        // start transaction
        startPayFriendTransaction(
                currentUser.uid,
                recipientPaymentId,
                amountToSend,
                binding.editTxtDescription.getText().toString().trim()
        );
    }

    private void onVerifyPaymentPinStatusError(java.lang.String message) {
        progressLoader.hide();
        showSimpleAlertDialog(message);
    }

    private void showMessageDialog() {
        java.lang.String message = "Incorrect PIN. Please try again, or click on the Forgot PIN to reset your PIN";
        java.lang.String priButton = "Forgot Pin";
        java.lang.String secButton = "Retry";

        DialogHelper.showAlertDialogMessage(
                this,
                (dialog, binding) -> {
                    binding.message.setText(message);
                    binding.btnPrimary.setText(priButton);
                    binding.btnSecondary.setText(secButton);

                    binding.btnSecondary.setOnClickListener(v -> dialog.dismiss());
                    binding.btnPrimary.setOnClickListener(v -> {
                        // TODO direct to pin reset
                    });
                }
        );
    }

    private void showSimpleAlertDialog(java.lang.String message) {
        com.settlex.android.ui.common.util.DialogHelper.showSimpleAlertDialog(
                this,
                "Error",
                message
        );
    }

    private void verifyPaymentPin(java.lang.String pin) {
        userViewModel.verifyPaymentPin(pin);
    }

    private void startPaymentProcess() {
        // Get recipient details
        java.lang.String recipientPaymentId = StringFormatter.removeAtInPaymentId(binding.selectedRecipientPaymentId.getText().toString());
        java.lang.String recipientName = binding.selectedRecipientName.getText().toString();

        // Current user is sender
        bottomSheetDialog = DialogHelper.showPayConfirmation(
                this,
                recipientPaymentId,
                recipientName,
                recipientPhotoUrl,
                amountToSend,
                currentUser.balance,
                currentUser.commissionBalance,
                () -> {
                    if (!currentUser.hasPin()) {
                        promptTransactionPinCreation();
                        return;
                    }
                    // ask for authorization
                    DialogHelper.showBottomSheetPaymentPinConfirmation(
                            this,
                            (binding, runnable) -> runnable[0] = () -> {
                                verifyPaymentPin(Objects.requireNonNull(binding.pinView.getText()).toString());
                            });
                }
        );
    }

    private void startPayFriendTransaction(java.lang.String fromSenderUid, java.lang.String toRecipient, long amountToSend, java.lang.String description) {
        transactionViewModel.transferFunds(
                fromSenderUid,
                toRecipient,
                TransactionIdGenerator.generate(fromSenderUid),
                amountToSend,
                java.lang.String.valueOf(TransactionServiceType.PAY_A_FRIEND),
                description);
    }

    private void promptTransactionPinCreation() {
        java.lang.String title = "Payment PIN Required";
        java.lang.String message = "Please set up your Payment PIN to complete this transaction securely";
        java.lang.String btnPriText = "Create PIN";
        java.lang.String btnSecText = "Cancel";

        DialogHelper.showAlertDialogWithIcon(
                this,
                (dialog, dialogBinding) -> {
                    dialogBinding.title.setText(title);
                    dialogBinding.message.setText(message);
                    dialogBinding.btnPrimary.setText(btnPriText);
                    dialogBinding.btnSecondary.setText(btnSecText);
                    dialogBinding.icon.setImageResource(R.drawable.ic_lock_filled);

                    dialogBinding.btnSecondary.setOnClickListener(v -> dialog.dismiss());
                    dialogBinding.btnPrimary.setOnClickListener(v -> {
                        startActivity(new Intent(this, CreatePaymentPinActivity.class));
                        dialog.dismiss();
                    });
                }
        );
    }

    private void searchRecipient(java.lang.String paymentId) {
        // Prevent self search
        if (StringFormatter.removeAtInPaymentId(paymentId).equals(currentUser.paymentId)) {
            java.lang.String ERROR_CANNOT_SEND_TO_SELF = "You cannot send a payment to your own account. Please choose a different recipient";

            binding.txtError.setText(ERROR_CANNOT_SEND_TO_SELF);
            binding.txtError.setVisibility(View.VISIBLE);
            return;
        }
        transactionViewModel.findRecipientByPaymentId(paymentId);
    }

    private void setupRecipientRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        binding.recipientRecyclerView.setLayoutManager(layoutManager);

        handleOnRecipientItemClick();
    }

    private void handleOnRecipientItemClick() {
        recipientAdapter.setOnItemClickListener(recipient -> {
            binding.editTxtPaymentId.setText(recipient.getPaymentId());
            binding.editTxtPaymentId.setSelection(binding.editTxtPaymentId.getText().length());
            binding.btnVerify.setVisibility(View.GONE);

            // Clear recycler view
            recipientAdapter.submitList(Collections.emptyList());
            binding.recipientRecyclerView.setVisibility(View.GONE);

            recipientPhotoUrl = recipient.getPhotoUrl();
            ProfileService.loadProfilePic(recipientPhotoUrl, binding.selectedRecipientProfilePic);
            binding.selectedRecipientName.setText(recipient.getFullName());
            binding.selectedRecipientPaymentId.setText(recipient.getPaymentId());
            binding.selectedRecipient.setVisibility(View.VISIBLE);
            updateNextButtonState();
        });
    }

    private void setupTextInputWatcher() {
        binding.editTxtPaymentId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                boolean showVerifyBtn = editable.toString().trim().length() >= 5;
                binding.btnVerify.setVisibility(showVerifyBtn ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().isEmpty()) {
                    recipientPaymentId = StringFormatter.removeAtInPaymentId(s.toString().trim().toLowerCase());
                }
                binding.txtError.setVisibility(View.GONE);
                binding.selectedRecipient.setVisibility(View.GONE);
                updateNextButtonState();
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
            public void onTextChanged(CharSequence rawAmount, int start, int before, int count) {
                boolean isAmountEmpty = rawAmount.toString().trim().isEmpty();
                java.lang.String ERROR_INVALID_AMOUNT = "Amount must be in range of ₦100 - ₦1,000,000.00";

                if (!isAmountEmpty) {
                    java.lang.String cleanedAmount = rawAmount.toString().replaceAll(",", "");
                    amountToSend = StringFormatter.convertNairaStringToKobo(cleanedAmount);
                }

                boolean shouldShowError = !isAmountInRange(amountToSend) && !isAmountEmpty;
                binding.txtAmountFeedback.setText((shouldShowError) ? ERROR_INVALID_AMOUNT : "");
                binding.txtAmountFeedback.setVisibility((shouldShowError) ? View.VISIBLE : View.GONE);

                updateNextButtonState();
            }
        });
    }

    private void updateNextButtonState() {
        boolean recipientSelected = binding.selectedRecipient.getVisibility() == View.VISIBLE;
        enableNextButton(isPaymentIdValid(recipientPaymentId) && isAmountInRange(amountToSend) && recipientSelected);
    }

    private boolean isPaymentIdValid(java.lang.String paymentId) {
        return paymentId != null && paymentId.matches("^@?[A-Za-z][A-Za-z0-9]{4,19}$");
    }

    private boolean isAmountInRange(long amount) {
        // The amount is stored in kobo (smallest currency unit)
        return amount >= 10_000L && amount <= 100_000_000L;
    }


    private void enableNextButton(boolean allValid) {
        // Enable btn when all fields met requirements
        binding.btnNext.setEnabled(allValid);
    }

    private void setupEditTextFocusHandlers() {
        binding.editTxtPaymentId.setOnFocusChangeListener((v, hasFocus) -> binding.editTxtPaymentIdBackground.setBackgroundResource(hasFocus ? R.drawable.bg_edit_txt_custom_white_focused : R.drawable.bg_edit_txt_custom_gray_not_focused));
        binding.editTxtDescription.setOnFocusChangeListener((v, hasFocus) -> binding.editTxtDescriptionBackground.setBackgroundResource(hasFocus ? R.drawable.bg_edit_txt_custom_white_focused : R.drawable.bg_edit_txt_custom_gray_not_focused));

        binding.editTxtAmount.setOnFocusChangeListener((v, hasFocus) -> {
            binding.editTxtAmountBackground.setBackgroundResource(hasFocus ? R.drawable.bg_edit_txt_custom_white_focused : R.drawable.bg_edit_txt_custom_gray_not_focused);

            // format the input to currency format
            java.lang.String rawInput = Objects.requireNonNull(binding.editTxtAmount.getText()).toString().trim();
            if (rawInput.isEmpty()) {
                return;
            }

            BigDecimal numericValue = binding.editTxtAmount.getNumericValueBigDecimal();

            if (hasFocus) {
                java.lang.String cleanNumber = numericValue.toPlainString();
                binding.editTxtAmount.setText(cleanNumber);
                binding.editTxtAmount.setSelection(cleanNumber.length());
                return;
            }

            java.lang.String currencyFormat = StringFormatter.formatToCurrency(numericValue);
            binding.editTxtAmount.setText(currencyFormat);
            binding.editTxtAmount.setSelection(currencyFormat.length());
        });
    }

    private void clearFocusOnLastEditTextField() {
        binding.editTxtDescription.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // Hide the keyboard
                hideKeyboard(v);
                v.clearFocus();
                return true;
            }
            return false;
        });
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