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

        StatusBarUtil.setStatusBarColor(requireActivity(), R.color.white);
        setupUiActions();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        observeTransactionStatusAndHandleResult();
    }

    private void setupUiActions() {
        Bundle args = getArguments();
        if (args != null) {
            binding.txnAmount.setText(args.getString("txn_amount"));
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
}
