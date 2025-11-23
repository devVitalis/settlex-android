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
        binding.icon.setImageResource(transaction.getServiceTypeIcon());
        binding.name.setText(transaction.getServiceTypeName());

        binding.operation.setText(transaction.getOperationSymbol());
        binding.operation.setTextColor(ContextCompat.getColor(binding.getRoot().getContext(), transaction.getOperationColor()));

        binding.amount.setText(transaction.getAmount());
        binding.amount.setTextColor(ContextCompat.getColor(binding.getRoot().getContext(), transaction.getOperationColor()));

        binding.status.setText(transaction.getStatus());
        binding.status.setTextColor(ContextCompat.getColor(binding.getRoot().getContext(), transaction.getStatusColor()));
        binding.status.setBackgroundResource(transaction.getStatusBgColor());

        binding.dateTime.setText(transaction.getTimestamp());

        binding.recipient.setText(transaction.getRecipient());
        binding.recipientName.setText(transaction.getRecipientName());

        binding.sender.setText(transaction.getSender());

        // show description if there any
        if (transaction.getDescription() != null) {
            binding.descriptionContainer.setVisibility(View.VISIBLE);
            binding.description.setText(transaction.getDescription());
        }

        binding.transactionId.setText(transaction.getTransactionId());
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