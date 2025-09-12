package com.settlex.android.ui.dashboard.fragments.transactions;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.airbnb.lottie.RenderMode;
import com.settlex.android.R;
import com.settlex.android.databinding.FragmentTransactionStatusBinding;
import com.settlex.android.ui.dashboard.viewmodel.TransactionsViewModel;
import com.settlex.android.util.event.Result;

/**
 * This screen displays the outcome of a transaction (Pending, Success, Failed).
 * Animations are used to give real-time feedback to the user about the status.
 */
public class TransactionStatusFragment extends Fragment {
    private FragmentTransactionStatusBinding binding;
    private TransactionsViewModel transactionsViewModel;

    public TransactionStatusFragment() {
        // Default empty constructor required by Fragment
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTransactionStatusBinding.inflate(inflater, container, false);
        transactionsViewModel = new ViewModelProvider(requireActivity()).get(TransactionsViewModel.class);

        customizeStatusBar();
        setupUiActions();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        observeTransactionStatus();
    }

    private void setupUiActions() {
        Bundle args = getArguments();
        if (args != null) {
            binding.txnAmount.setText(args.getString("txn_amount"));
        }

        binding.btnDone.setOnClickListener(v -> requireActivity().finish());
    }

    /**
     * Observes the result of the "Pay a Friend" transaction and updates
     * the UI accordingly based on the status: PENDING, SUCCESS, or ERROR.
     */
    private void observeTransactionStatus() {
        transactionsViewModel.getPayFriendLiveData().observe(getViewLifecycleOwner(), event -> {
            Result<String> transactionResult = event.peekContent();
            if (transactionResult != null) {
                switch (transactionResult.getStatus()) {
                    case PENDING -> showPendingState();
                    case SUCCESS -> showSuccessState();
                    case ERROR -> showFailedState();
                }
            }
        });
    }


    private void showPendingState() {
        binding.txnPendingAnim.setVisibility(View.VISIBLE);
        binding.txnPendingAnim.setRenderMode(RenderMode.SOFTWARE);
        binding.txnPendingAnim.playAnimation();
        binding.txnStatus.setText(getString(R.string.txn_pending));
    }

    private void showSuccessState() {
        binding.txnSuccessAnim.setVisibility(View.VISIBLE);
        binding.txnSuccessAnim.setRenderMode(RenderMode.SOFTWARE);
        binding.txnSuccessAnim.playAnimation();
        binding.txnStatus.setText(getString(R.string.txn_success));
    }

    private void showFailedState() {
        binding.txnFailedAnim.setVisibility(View.VISIBLE);
        binding.txnFailedAnim.setRenderMode(RenderMode.SOFTWARE);
        binding.txnFailedAnim.playAnimation();
        binding.txnStatus.setText(getString(R.string.txn_failed));
    }

    private void customizeStatusBar() {
        Window window = requireActivity().getWindow();
        window.setStatusBarColor(ContextCompat.getColor(requireActivity(), R.color.white));
        View decorView = window.getDecorView();
        decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }
}
