package com.settlex.android.ui.dashboard.fragments.transaction_details;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.settlex.android.R;
import com.settlex.android.databinding.FragmentTransactionReceiptBinding;
import com.settlex.android.ui.dashboard.model.TransactionUiModel;
import com.settlex.android.util.string.StringUtil;
import com.settlex.android.util.ui.StatusBarUtil;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TransactionReceiptFragment extends Fragment {
    private FragmentTransactionReceiptBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTransactionReceiptBinding.inflate(inflater, container, false);

        StatusBarUtil.setStatusBarColor(requireActivity(), R.color.white);
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
        binding.copyTransactionId.setOnClickListener(v -> {
            StringUtil.copyToClipboard(
                    requireContext(),
                    "Transaction ID",
                    binding.transactionId.getText().toString());

            // show toast only on older devices < API 33
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                Toast.makeText(requireContext(), "Copied", Toast.LENGTH_SHORT).show();
            }
        });
    }
}