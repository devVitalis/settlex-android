package com.settlex.android.presentation.transactions;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.settlex.android.R;
import com.settlex.android.databinding.FragmentTransactionDetailsBinding;
import com.settlex.android.presentation.transactions.model.TransactionUiModel;
import com.settlex.android.util.string.StringFormatter;
import com.settlex.android.util.ui.StatusBar;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TransactionDetailsFragment extends Fragment {
    private FragmentTransactionDetailsBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTransactionDetailsBinding.inflate(inflater, container, false);

        StatusBar.setColor(requireActivity(), R.color.white);
        onBackButtonPressed();
        binding.btnBackBefore.setOnClickListener(view -> requireActivity().finish());

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TransactionUiModel transaction = requireActivity().getIntent().getParcelableExtra("transaction");
        if (transaction != null) {
            bindTransaction(transaction);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void bindTransaction(TransactionUiModel transaction) {
        binding.icon.setImageResource(transaction.serviceTypeIcon);
        binding.name.setText(transaction.serviceTypeName);

        binding.operation.setText(transaction.operationSymbol);
        binding.operation.setTextColor(ContextCompat.getColor(binding.getRoot().getContext(), transaction.operationColor));

        binding.amount.setText(transaction.amount);
        binding.amount.setTextColor(ContextCompat.getColor(binding.getRoot().getContext(), transaction.operationColor));

        binding.status.setText(transaction.status);
        binding.status.setTextColor(ContextCompat.getColor(binding.getRoot().getContext(), transaction.statusColor));
        binding.status.setBackgroundResource(transaction.statusBgColor);

        binding.dateTime.setText(transaction.timestamp);

        binding.recipient.setText(transaction.recipient);
        binding.recipientName.setText(transaction.recipientName);

        binding.sender.setText(transaction.sender);

        // show description if there any
        if (transaction.description != null) {
            binding.descriptionContainer.setVisibility(View.VISIBLE);
            binding.description.setText(transaction.description);
        }

        binding.transactionId.setText(transaction.transactionId);
        binding.copyTransactionId.setOnClickListener(v -> StringFormatter.copyToClipboard(
                requireContext(),
                "Transaction ID",
                binding.transactionId.getText().toString(),
                true));
    }

    private void onBackButtonPressed() {
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                requireActivity().finish();
            }
        });
    }
}