package com.settlex.android.ui.dashboard.fragments.transactions;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.settlex.android.R;
import com.settlex.android.SettleXApp;
import com.settlex.android.data.enums.TransactionServiceType;
import com.settlex.android.data.remote.avater.AvatarService;
import com.settlex.android.databinding.FragmentPayAFriendBinding;
import com.settlex.android.ui.common.util.ProgressLoaderController;
import com.settlex.android.ui.dashboard.adapter.RecipientAdapter;
import com.settlex.android.ui.dashboard.model.RecipientUiModel;
import com.settlex.android.ui.dashboard.model.UserUiModel;
import com.settlex.android.ui.dashboard.util.DashboardUiUtil;
import com.settlex.android.ui.dashboard.util.TransactionIdGenerator;
import com.settlex.android.ui.dashboard.viewmodel.TransactionsViewModel;
import com.settlex.android.ui.dashboard.viewmodel.UserViewModel;
import com.settlex.android.util.event.Result;
import com.settlex.android.util.string.StringUtil;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class PayAFriendFragment extends Fragment {
    // Input fields
    private String username;
    private double amount;
    private Bundle bundle; // bundle to pass the txn amount to next screen
    private UserUiModel currentUser; // latest logged in user
    private BottomSheetDialog bottomSheetDialog;

    private FragmentPayAFriendBinding binding;
    private ProgressLoaderController progressLoader;
    private RecipientAdapter recipientAdapter;
    private UserViewModel userViewModel;
    private TransactionsViewModel transactionsViewModel;

    public PayAFriendFragment() {
        // Required empty public constructor
    }

    // ---------- Lifecycle ----------
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPayAFriendBinding.inflate(inflater, container, false);

        progressLoader = new ProgressLoaderController(requireActivity());
        userViewModel = ((SettleXApp) requireActivity().getApplication()).getSharedUserViewModel();
        transactionsViewModel = new ViewModelProvider(requireActivity()).get(TransactionsViewModel.class);
        recipientAdapter = new RecipientAdapter();
        bundle = new Bundle();

        setupStatusBar();
        observeAndGetUserData();
        setupUiActions();


        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        observePayFriendAndHandleResult();
        observeUsernameSearchAndHandleResult();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Remove binding
        binding = null;
    }

    // ---------- Observers ----------
    private void observeAndGetUserData() {
        userViewModel.getUserData().observe(getViewLifecycleOwner(), user -> {
            if (user == null) {
                return;
            }
            binding.availableBalance.setText(StringUtil.formatToNaira(user.getData().getBalance() + user.getData().getCommissionBalance()));
            this.currentUser = user.getData();
        });
    }

    private void observePayFriendAndHandleResult() {
        transactionsViewModel.getPayFriendLiveData().observe(getViewLifecycleOwner(), event -> {
            Result<String> result = event.getContentIfNotHandled();
            if (result == null) return;
            switch (result.getStatus()) {
                case LOADING -> progressLoader.show();
                case PENDING -> onTransactionPending();
                case SUCCESS -> onTransactionSuccess();
                case ERROR -> onTransactionFailed();
            }
        });
    }

    private void observeUsernameSearchAndHandleResult() {
        userViewModel.getUsernameSearchLiveData().observe(getViewLifecycleOwner(), result -> {
            if (result != null) {
                switch (result.getStatus()) {
                    case LOADING -> onUsernameSearchLoading();
                    case SUCCESS -> onUsernameSearchSuccess(result.getData());
                    case ERROR -> onUsernameSearchFailed();
                }
            }
        });
    }

    // ---------- Business Handlers ----------
    private void onTransactionPending() {
        bundle.putString("txn_amount", StringUtil.formatToNaira(amount));
        navigateToFragment(new TransactionStatusFragment(), bundle);
        progressLoader.hide();
        bottomSheetDialog.dismiss();
    }

    private void onTransactionSuccess() {
        bundle.putString("txn_amount", StringUtil.formatToNaira(amount));
        navigateToFragment(new TransactionStatusFragment(), bundle);
        progressLoader.hide();
        bottomSheetDialog.dismiss();
    }

    private void onTransactionFailed() {
        bundle.putString("txn_amount", StringUtil.formatToNaira(amount));
        navigateToFragment(new TransactionStatusFragment(), bundle);
        progressLoader.hide();
        bottomSheetDialog.dismiss();
    }

    private void onUsernameSearchLoading() {
        recipientAdapter.submitList(Collections.emptyList());
        binding.recipientRecyclerView.setVisibility(View.GONE);

        binding.selectedRecipient.setVisibility(View.GONE);
        binding.txtUsernameFeedback.setVisibility(View.GONE);
        validateInputsAndUpdateNextButton();

        binding.shimmerEffect.startShimmer();
        binding.shimmerEffect.setVisibility(View.VISIBLE);
    }

    private void onUsernameSearchSuccess(List<RecipientUiModel> recipient) {
        if (recipient == null || recipient.isEmpty()) {
            // Recipient not found
            String username = StringUtil.addAtToUsername(this.username);
            binding.txtUsernameFeedback.setText(getString(R.string.No_user_found_with_tag, username));
            binding.txtUsernameFeedback.setVisibility(View.VISIBLE);
        }

        // Recipient found
        binding.shimmerEffect.stopShimmer();
        binding.shimmerEffect.setVisibility(View.GONE);

        recipientAdapter.submitList(recipient);
        binding.recipientRecyclerView.setVisibility(View.VISIBLE);
        binding.recipientRecyclerView.setAdapter(recipientAdapter);
    }

    private void onUsernameSearchFailed() {
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
                () -> // onPay btn clicked
                        startPayFriendTransaction(
                                currentUser.getUid(),
                                recipientUsername,
                                amount,
                                description));
    }

    private void startPayFriendTransaction(String senderUid, String recipientUsername, double amountToSend, String description) {
        transactionsViewModel.payFriend(
                senderUid,
                recipientUsername,
                TransactionIdGenerator.generate(senderUid), // UID hash + timestamp + UUID
                amountToSend,
                String.valueOf(TransactionServiceType.PAY_A_FRIEND),
                description);
    }

    private void searchUsername(String query) {
        userViewModel.searchUsername(query);
    }

    // ---------- UI Setup ----------
    private void setupUiActions() {
        setupTextInputWatcher();
        setupEditTextFocusHandlers();
        setupRecipientRecyclerView();
        attachCurrencyFormatter(binding.editTxtAmount);
        clearFocusAndHideKeyboardOnOutsideTap(binding.getRoot());

        // Display avail balance
        binding.imgBackBefore.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
        binding.btnVerify.setOnClickListener(v -> searchUsername(username.replaceAll("\\s+", "")));
        binding.btnNext.setOnClickListener(v -> showPayConfirmation());
    }

    private void setupRecipientRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        binding.recipientRecyclerView.setLayoutManager(layoutManager);
        handleOnRecipientClick();
    }

    private void handleOnRecipientClick() {
        recipientAdapter.setOnItemClickListener(model -> {
            // Sender = receiver
            if (StringUtil.removeAtInUsername(model.getUsername()).equals(currentUser.getUsername())) {
                binding.txtUsernameFeedback.setText("You can't send money to yourself");
                binding.txtUsernameFeedback.setVisibility(View.VISIBLE);
                return;
            }

            binding.editTxtUsername.setText(StringUtil.removeAtInUsername(model.getUsername()));
            binding.editTxtUsername.setSelection(binding.editTxtUsername.getText().length());
            binding.btnVerify.setVisibility(View.GONE);

            // Clear recycler view
            recipientAdapter.submitList(Collections.emptyList());
            binding.recipientRecyclerView.setVisibility(View.GONE);

            AvatarService.loadAvatar(model.getFullName(), binding.selectedRecipientProfilePic);
            // binding.selectedRecipientProfilePic.setImageResource(model.getProfileUrl()); //TODO: handle profile pic link
            binding.selectedRecipientName.setText(model.getFullName());
            binding.selectedRecipientUsername.setText(model.getUsername());
            binding.selectedRecipient.setVisibility(View.VISIBLE);
            validateInputsAndUpdateNextButton();
        });
    }

    // ---------- Validation & Helpers ----------
    private void setupTextInputWatcher() {
        binding.editTxtUsername.addTextChangedListener(new TextWatcher() {
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
                    username = StringUtil.removeAtInUsername(s.toString().trim().toLowerCase());
                }
                binding.txtUsernameFeedback.setVisibility(View.GONE);
                binding.selectedRecipient.setVisibility(View.GONE);
                validateInputsAndUpdateNextButton();
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
                if (!s.toString().isEmpty()) {
                    try {
                        amount = Double.parseDouble(binding.editTxtAmount.getText().toString().replaceAll(",", ""));
                    } catch (NumberFormatException ignored) {
                        //Ignored
                    }
                }
                binding.txtAmountFeedback.setVisibility((!validateAmount(amount) && !s.toString().isEmpty()) ? View.VISIBLE : View.GONE);
                validateInputsAndUpdateNextButton();
            }
        });
    }

    private void validateInputsAndUpdateNextButton() {
        boolean recipientConfirmed = binding.selectedRecipient.getVisibility() == View.VISIBLE;
        updateNextButtonState(validateUsername(username) && validateAmount(amount) && recipientConfirmed);
    }

    private boolean validateUsername(String username) {
        return username.matches("^[a-z0-9]([a-z0-9]*[._]?[a-z0-9]*)+[a-z0-9]$");
    }

    private boolean validateAmount(double amount) {
        return amount >= 100 && amount <= 500_000_000;
    }

    private void updateNextButtonState(boolean allValid) {
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
        binding.editTxtUsername.setOnClickListener(focusListener);
        binding.editTxtAmount.setOnClickListener(focusListener);
        binding.editTxtDescription.setOnClickListener(focusListener);

        binding.editTxtUsername.setOnFocusChangeListener((v, hasFocus) ->
                binding.editTxtUsernameBackground.setBackgroundResource(hasFocus ? R.drawable.bg_edit_txt_custom_white_focused : R.drawable.bg_edit_txt_custom_gray_not_focused));
        binding.editTxtAmount.setOnFocusChangeListener((v, hasFocus)
                -> binding.editTxtAmountBackground.setBackgroundResource(hasFocus ? R.drawable.bg_edit_txt_custom_white_focused : R.drawable.bg_edit_txt_custom_gray_not_focused));
        binding.editTxtDescription.setOnFocusChangeListener((v, hasFocus)
                -> binding.editTxtDescriptionBackground.setBackgroundResource(hasFocus ? R.drawable.bg_edit_txt_custom_white_focused : R.drawable.bg_edit_txt_custom_gray_not_focused));
    }

    public static void attachCurrencyFormatter(EditText editTextAmount) {
        final Locale locale = Locale.US;
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
                    String[] parts = cleanInput.split("\\.", -1);
                    String integerPart = parts[0].replaceFirst("^0+(?!$)", "");
                    String decimalPart = (parts.length > 1) ? parts[1] : null;
                    BigInteger bigInt = new BigInteger(integerPart.isEmpty() ? "0" : integerPart);
                    String groupedInteger = integerFormatter.format(bigInt);
                    String formattedValue = (decimalPart != null) ? (groupedInteger + "." + decimalPart) : groupedInteger;
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
                    String current = editTextAmount.getText().toString();
                    if (current.endsWith(".00") && !text.contains(".")) {
                        String stripped = current.substring(0, current.length() - 3);
                        editTextAmount.setText(stripped);
                        editTextAmount.setSelection(stripped.length());
                    }
                } else {
                    String raw = editTextAmount.getText().toString();
                    String finalText;
                    if (raw.contains(".")) {
                        finalText = twoDecimalFormatter.format(value);
                    } else {
                        finalText = String.format(locale, "%,.2f", value);
                    }
                    editTextAmount.setText(finalText);
                    editTextAmount.setSelection(finalText.length());
                }
            } catch (NumberFormatException ignored) {
            }
        });
    }

    private void navigateToFragment(Fragment fragment, Bundle args) {
        fragment.setArguments(args);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void setupStatusBar() {
        Window window = requireActivity().getWindow();
        window.setStatusBarColor(ContextCompat.getColor(requireContext(), R.color.gray_light));
        View decorView = window.getDecorView();
        decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void clearFocusAndHideKeyboardOnOutsideTap(View root) {
        if (!(root instanceof EditText)) {
            root.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) hideKeyboard();
                return false;
            });
        }
        if (root instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) root).getChildCount(); i++) {
                clearFocusAndHideKeyboardOnOutsideTap(((ViewGroup) root).getChildAt(i));
            }
        }
    }

    private void hideKeyboard() {
        View focusedView = requireActivity().getCurrentFocus();
        if (focusedView != null) {
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
            focusedView.clearFocus();
        }
    }
}
