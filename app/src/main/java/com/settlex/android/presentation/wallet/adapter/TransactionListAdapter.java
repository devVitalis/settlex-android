package com.settlex.android.presentation.wallet.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.settlex.android.databinding.ItemTransactionBinding;
import com.settlex.android.presentation.transactions.model.TransactionUiModel;

/**
 * Base adapter for displaying transaction items in RecyclerView
 */
public class TransactionListAdapter extends ListAdapter<TransactionUiModel, TransactionListAdapter.TransactionViewHolder> {
    private OnTransactionClickListener listener;

    public void setOnTransactionClickListener(OnTransactionClickListener listener) {
        this.listener = listener;
    }

    public TransactionListAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<TransactionUiModel> DIFF_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull TransactionUiModel oldItem, @NonNull TransactionUiModel newItem) {
            return oldItem.transactionId.equals(newItem.transactionId);
        }

        @Override
        public boolean areContentsTheSame(@NonNull TransactionUiModel oldItem, @NonNull TransactionUiModel newItem) {
            return oldItem.equals(newItem);
        }
    };


    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTransactionBinding binding = ItemTransactionBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new TransactionViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        holder.Bind(getItem(position), listener);
    }

    /**
     * ViewHolder for transaction items containing all transaction display elements
     */
    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        private final ItemTransactionBinding binding;

        public TransactionViewHolder(@NonNull ItemTransactionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void Bind(TransactionUiModel transaction, OnTransactionClickListener listener) {
            binding.icon.setImageResource(transaction.serviceTypeIcon);
            binding.name.setText(transaction.serviceTypeName);

            binding.operation.setText(transaction.operationSymbol);
            binding.operation.setTextColor(ContextCompat.getColor(binding.getRoot().getContext(), transaction.operationColor));

            binding.amount.setText(transaction.amount);
            binding.amount.setTextColor(ContextCompat.getColor(binding.getRoot().getContext(), transaction.operationColor));

            binding.dateTime.setText(transaction.timestamp);
            binding.recipientOrSender.setText(transaction.displayName);

            binding.status.setText(transaction.status);
            binding.status.setTextColor(ContextCompat.getColor(binding.getRoot().getContext(), transaction.statusColor));
            binding.status.setBackgroundResource(transaction.statusBgColor);

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onClick(transaction);
                }
            });
        }
    }

    public interface OnTransactionClickListener {
        void onClick(TransactionUiModel uiModel);
    }
}