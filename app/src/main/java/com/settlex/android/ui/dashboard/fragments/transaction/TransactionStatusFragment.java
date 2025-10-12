package com.settlex.android.ui.dashboard.fragments.transaction;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.airbnb.lottie.RenderMode;
import com.settlex.android.R;
import com.settlex.android.databinding.FragmentTransactionStatusBinding;
import com.settlex.android.ui.dashboard.viewmodel.TransactionViewModel;
import com.settlex.android.util.event.Result;
import com.settlex.android.util.ui.StatusBarUtil;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * This screen displays the outcome of a transaction (Pending, Success, Failed).
 */
@AndroidEntryPoint
public class TransactionStatusFragment extends Fragment {

    // Dependencies
    private FragmentTransactionStatusBinding binding;
    private TransactionViewModel transactionViewModel;

    public TransactionStatusFragment() {
        // Default empty constructor required by Fragment
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        transactionViewModel = new ViewModelProvider(requireActivity()).get(TransactionViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTransactionStatusBinding.inflate(inflater, container, false);

        setupUiActions();
        observeTransactionStatusAndHandleResult();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void setupUiActions() {
        StatusBarUtil.setStatusBarColor(requireActivity(), R.color.white);

        if (getArguments() != null) {
            TransactionStatusFragmentArgs args = TransactionStatusFragmentArgs.fromBundle(getArguments());
            binding.txnAmount.setText(args.getAmount());
        }

        binding.btnDone.setOnClickListener(v -> requireActivity().finish());
    }

    private void observeTransactionStatusAndHandleResult() {
        transactionViewModel.getPayFriendLiveData().observe(getViewLifecycleOwner(), event -> {
            Result<String> transactionResult = event.peekContent();
            if (transactionResult == null) return;

            switch (transactionResult.getStatus()) {
                case PENDING -> showPendingState();
                case SUCCESS -> showSuccessState();
                case ERROR -> showFailedState();
            }
        });
    }

    private void showPendingState() {
        String PENDING_STATUS = "Transaction Pending";
        binding.txnPendingAnim.setVisibility(View.VISIBLE);
        binding.txnPendingAnim.setRenderMode(RenderMode.SOFTWARE);
        binding.txnPendingAnim.playAnimation();
        binding.txnStatus.setText(PENDING_STATUS);
    }

    private void showSuccessState() {
        String SUCCESS_STATUS = "Transaction Successful";
        binding.txnSuccessAnim.setVisibility(View.VISIBLE);
        binding.txnSuccessAnim.setRenderMode(RenderMode.SOFTWARE);
        binding.txnSuccessAnim.playAnimation();
        binding.txnStatus.setText(SUCCESS_STATUS);
    }

    private void showFailedState() {
        String FAILED_STATUS = "Transaction Failed";
        binding.txnFailedAnim.setVisibility(View.VISIBLE);
        binding.txnFailedAnim.setRenderMode(RenderMode.SOFTWARE);
        binding.txnFailedAnim.playAnimation();
        binding.txnStatus.setText(FAILED_STATUS);
    }
}
