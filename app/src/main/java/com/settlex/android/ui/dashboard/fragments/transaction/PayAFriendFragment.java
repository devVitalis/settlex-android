package com.settlex.android.ui.dashboard.fragments.transaction;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.settlex.android.R;
import com.settlex.android.data.enums.TransactionServiceType;
import com.settlex.android.data.remote.avater.AvatarService;
import com.settlex.android.databinding.FragmentPayAFriendBinding;
import com.settlex.android.ui.common.util.ProgressLoaderController;
import com.settlex.android.ui.dashboard.adapter.RecipientAdapter;
import com.settlex.android.ui.dashboard.model.RecipientUiModel;
import com.settlex.android.ui.dashboard.model.UserUiModel;
import com.settlex.android.ui.dashboard.util.DashboardUiUtil;
import com.settlex.android.ui.dashboard.util.TransactionIdGenerator;
import com.settlex.android.ui.dashboard.viewmodel.TransactionViewModel;
import com.settlex.android.ui.dashboard.viewmodel.UserViewModel;
import com.settlex.android.util.event.Result;
import com.settlex.android.util.string.StringUtil;
import com.settlex.android.util.ui.StatusBarUtil;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PayAFriendFragment extends Fragment {

    // Dependencies
    private FragmentPayAFriendBinding binding;
    private ProgressLoaderController progressLoader;
    private RecipientAdapter recipientAdapter;
    private UserViewModel userViewModel;
    private TransactionViewModel transactionViewModel;
    private BottomSheetDialog bottomSheetDialog;
    private UserUiModel currentUser;

    // Instance variables for txn data
    private String paymentId;
    private long amount;

    public PayAFriendFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        transactionViewModel = new ViewModelProvider(requireActivity()).get(TransactionViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPayAFriendBinding.inflate(inflater, container, false);

        progressLoader = new ProgressLoaderController(requireActivity());
        recipientAdapter = new RecipientAdapter();

        observeUserState();
        setupUiActions();
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Remove binding
        binding = null;
    }

    // ---------- Observers ----------
    private void observeUserState() {
        userViewModel.getAuthStateLiveData().observe(getViewLifecycleOwner(), uid -> {
            if (uid == null) {
                // logged out/session expired
                return;
            }
            // user is logged in fetch data
            observeUserData();
            observePayFriendAndHandleResult();
            observeRecipientSearchAndHandleResult();
        });
    }

    private void observeUserData() {
        userViewModel.getUserLiveData().observe(getViewLifecycleOwner(), userData -> {
            if (userData == null) return;

            switch (userData.getStatus()) {
                case SUCCESS -> onUserDataSuccess(userData.getData());
                case ERROR -> {
                    // Handle error
                }
            }
        });
    }

    private void onUserDataSuccess(UserUiModel user) {
        if (user == null) return;
        binding.availableBalance.setText(StringUtil.formatToNaira(user.getBalance() + user.getCommissionBalance()));
        this.currentUser = user;
    }

    private void observePayFriendAndHandleResult() {
        transactionViewModel.getPayFriendLiveData().observe(getViewLifecycleOwner(), event -> {
            Result<String> result = event.getContentIfNotHandled();
            if (result == null) return;

            switch (result.getStatus()) {
                case LOADING -> onTransactionLoading();
                case PENDING -> onTransactionPending();
                case SUCCESS -> onTransactionSuccess();
                case ERROR -> onTransactionFailed();
            }
        });
    }

    private void onTransactionLoading() {
        progressLoader.show();
    }

    private void onTransactionPending() {
        navigateToTransactionStatusFragment();
        progressLoader.hide();
        bottomSheetDialog.dismiss();
    }

    private void onTransactionSuccess() {
        navigateToTransactionStatusFragment();
        progressLoader.hide();
        bottomSheetDialog.dismiss();
    }

    private void onTransactionFailed() {
        navigateToTransactionStatusFragment();
        progressLoader.hide();
        bottomSheetDialog.dismiss();
    }

    private void navigateToTransactionStatusFragment() {
        String amount = StringUtil.formatToNaira(this.amount);
        // Pass txn amount
        NavDirections action = PayAFriendFragmentDirections.actionPayAFriendFragmentToTransactionStatusFragment(amount);
        NavHostFragment.findNavController(this).navigate(action);
    }

    private void observeRecipientSearchAndHandleResult() {
        transactionViewModel.getRecipientSearchResult().observe(getViewLifecycleOwner(), result -> {
            if (result == null) return;

            switch (result.getStatus()) {
                case LOADING -> onRecipientSearchLoading();
                case SUCCESS -> onRecipientSearchSuccess(result.getData());
                case ERROR -> onRecipientSearchFailed();
            }
        });
    }

    private void onRecipientSearchLoading() {
        // reset adapter
        recipientAdapter.submitList(Collections.emptyList());
        binding.recipientRecyclerView.setVisibility(View.GONE);

        // hide selected recipient
        binding.selectedRecipient.setVisibility(View.GONE);
        binding.txtErrorFeedback.setVisibility(View.GONE);
        updateNextButtonState();

        binding.shimmerEffect.startShimmer();
        binding.shimmerEffect.setVisibility(View.VISIBLE);
    }

    private void onRecipientSearchSuccess(List<RecipientUiModel> recipient) {
        if (recipient == null || recipient.isEmpty()) {
            // Recipient not found
            String username = StringUtil.addAtToUsername(this.paymentId);
            String ERROR_NO_USER_FOUND = "No user found with Payment ID " + username;

            binding.txtErrorFeedback.setText(ERROR_NO_USER_FOUND);
            binding.txtErrorFeedback.setVisibility(View.VISIBLE);
        }

        // Recipient found
        binding.shimmerEffect.stopShimmer();
        binding.shimmerEffect.setVisibility(View.GONE);

        recipientAdapter.submitList(recipient);
        binding.recipientRecyclerView.setVisibility(View.VISIBLE);
        binding.recipientRecyclerView.setAdapter(recipientAdapter);
    }

    private void onRecipientSearchFailed() {
        binding.shimmerEffect.stopShimmer();
        binding.shimmerEffect.setVisibility(View.GONE);
    }

    private void showPayConfirmation() {
        // Get recipient details
        String recipientUsername = StringUtil.removeAtInUsername(binding.selectedRecipientUsername.getText().toString());
        String recipientName = binding.selectedRecipientName.getText().toString();
        String description = binding.editTxtDescription.getText().toString().trim();

        // Current user is sender
        bottomSheetDialog = DashboardUiUtil.showPayConfirmation(
                requireActivity(),
                recipientUsername,
                recipientName,
                amount,
                currentUser.getBalance(),
                currentUser.getCommissionBalance(),
                () ->
                        // onPay btn clicked
                        startPayFriendTransaction(
                                currentUser.getUid(),
                                recipientUsername,
                                amount,
                                description));
    }

    private void startPayFriendTransaction(String senderUid, String recipient, long amountToSend, String description) {
        transactionViewModel.payFriend(
                senderUid,
                recipient,
                TransactionIdGenerator.generate(senderUid),
                amountToSend,
                String.valueOf(TransactionServiceType.PAY_A_FRIEND),
                description);
    }

    private void setupUiActions() {
        StatusBarUtil.setStatusBarColor(requireActivity(), R.color.white);
        clearFocusOnLastEditTextField();
        setupTextInputWatcher();
        setupEditTextFocusHandlers();
        setupRecipientRecyclerView();

        // Display avail balance
        binding.btnBackBefore.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
        binding.btnVerify.setOnClickListener(v -> searchRecipient(paymentId.replaceAll("\\s+", "")));
        binding.btnNext.setOnClickListener(v -> showPayConfirmation());
    }

    private void searchRecipient(String paymentId) {
        transactionViewModel.searchRecipientWithUsername(paymentId);
    }

    private void setupRecipientRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        binding.recipientRecyclerView.setLayoutManager(layoutManager);

        handleOnRecipientItemClick();
    }

    private void handleOnRecipientItemClick() {
        recipientAdapter.setOnItemClickListener(recipient -> {
            // Sender = receiver
            if (StringUtil.removeAtInUsername(recipient.getUsername()).equals(currentUser.getUsername())) {
                String ERROR_CANNOT_SEND_TO_SELF = "You cannot send a payment to your own account. Please choose a different recipient";

                binding.txtErrorFeedback.setText(ERROR_CANNOT_SEND_TO_SELF);
                binding.txtErrorFeedback.setVisibility(View.VISIBLE);
                return;
            }

            binding.editTxtPaymentId.setText(StringUtil.removeAtInUsername(recipient.getUsername()));
            binding.editTxtPaymentId.setSelection(binding.editTxtPaymentId.getText().length());
            binding.btnVerify.setVisibility(View.GONE);

            // Clear recycler view
            recipientAdapter.submitList(Collections.emptyList());
            binding.recipientRecyclerView.setVisibility(View.GONE);

            AvatarService.loadAvatar(recipient.getFullName(), binding.selectedRecipientProfilePic);
            // binding.selectedRecipientProfilePic.setImageResource(model.getProfileUrl()); //TODO: handle profile pic link
            binding.selectedRecipientName.setText(recipient.getFullName());
            binding.selectedRecipientUsername.setText(recipient.getUsername());
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
                boolean showVerifyBtn = editable.toString().trim().length() >= 3;
                binding.btnVerify.setVisibility(showVerifyBtn ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().isEmpty()) {
                    paymentId = StringUtil.removeAtInUsername(s.toString().trim().toLowerCase());
                }
                binding.txtErrorFeedback.setVisibility(View.GONE);
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
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean isEmpty = s.toString().trim().isEmpty();
                if (!isEmpty) {
                    String cleanedAmount = s.toString().replaceAll(",", "");
                    amount = StringUtil.convertNairaStringToLongCents(cleanedAmount);
                }
                String ERROR_INVALID_AMOUNT = "Amount must be in range of ₦100 - ₦500,000.00";
                boolean shouldShowError = !isAmountValid(amount) && !isEmpty;
                binding.txtAmountFeedback.setText((shouldShowError) ? ERROR_INVALID_AMOUNT : "");
                binding.txtAmountFeedback.setVisibility((shouldShowError) ? View.VISIBLE : View.GONE);

                updateNextButtonState();
            }
        });
    }

    private void updateNextButtonState() {
        boolean recipientSelected = binding.selectedRecipient.getVisibility() == View.VISIBLE;
        enableNextButton(isPaymentIdValid(paymentId) && isAmountValid(amount) && recipientSelected);
    }

    private boolean isPaymentIdValid(String paymentId) {
        return paymentId != null && paymentId.matches("^[a-z0-9]([a-z0-9]*[._]?[a-z0-9]*)+[a-z0-9]$");
    }

    private boolean isAmountValid(long amount) {
        // The amount is stored in kobo (smallest currency unit)
        return amount >= 10_000L && amount <= 50_000_000_000L;
    }


    private void enableNextButton(boolean allValid) {
        // Enable btn when all fields met requirements
        binding.btnNext.setEnabled(allValid);
    }

    private void setupEditTextFocusHandlers() {
        View.OnClickListener focusListener = v -> {
            if (v instanceof EditText) {
                v.setFocusableInTouchMode(true);
                v.setFocusable(true);
                v.requestFocus();
            }
        };
        binding.editTxtPaymentId.setOnClickListener(focusListener);
        binding.editTxtAmount.setOnClickListener(focusListener);
        binding.editTxtDescription.setOnClickListener(focusListener);

        binding.editTxtPaymentId.setOnFocusChangeListener((v, hasFocus) -> binding.editTxtPaymentIdBackground.setBackgroundResource(hasFocus ? R.drawable.bg_edit_txt_custom_white_focused : R.drawable.bg_edit_txt_custom_gray_not_focused));
        binding.editTxtDescription.setOnFocusChangeListener((v, hasFocus) -> binding.editTxtDescriptionBackground.setBackgroundResource(hasFocus ? R.drawable.bg_edit_txt_custom_white_focused : R.drawable.bg_edit_txt_custom_gray_not_focused));

        binding.editTxtAmount.setOnFocusChangeListener((v, hasFocus) -> {
            binding.editTxtAmountBackground.setBackgroundResource(hasFocus ? R.drawable.bg_edit_txt_custom_white_focused : R.drawable.bg_edit_txt_custom_gray_not_focused);

            // format the input to currency format
            String rawInput = Objects.requireNonNull(binding.editTxtAmount.getText()).toString().trim();
            if (rawInput.isEmpty()) {
                return;
            }

            BigDecimal numericValue = binding.editTxtAmount.getNumericValueBigDecimal();

            if (hasFocus) {
                String cleanNumber = numericValue.toPlainString();
                binding.editTxtAmount.setText(cleanNumber);
                binding.editTxtAmount.setSelection(cleanNumber.length());
                return;
            }

            String currencyFormat = StringUtil.formatToCurrency(numericValue);
            binding.editTxtAmount.setText(currencyFormat);
            binding.editTxtAmount.setSelection(currencyFormat.length());
        });
    }

    private void clearFocusOnLastEditTextField() {
        binding.editTxtDescription.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // Hide the keyboard
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                // Clear focus
                v.clearFocus();
                return true;
            }
            return false;
        });
    }
}
