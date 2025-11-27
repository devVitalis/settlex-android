package com.settlex.android.presentation.transactions.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.settlex.android.databinding.ItemTransactionBinding;
import com.settlex.android.presentation.transactions.model.TransactionItemUiModel;

/**
 * Base adapter for displaying transaction items in RecyclerView
 */
public class TransactionListAdapter extends ListAdapter<TransactionItemUiModel, TransactionListAdapter.TransactionViewHolder> {
    private OnTransactionClickListener listener;

    public void setOnTransactionClickListener(OnTransactionClickListener listener) {
        this.listener = listener;
    }

    public TransactionListAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<TransactionItemUiModel> DIFF_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull TransactionItemUiModel oldItem, @NonNull TransactionItemUiModel newItem) {
            return oldItem.getTransactionId().equals(newItem.getTransactionId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull TransactionItemUiModel oldItem, @NonNull TransactionItemUiModel newItem) {
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

        public void Bind(TransactionItemUiModel transaction, OnTransactionClickListener listener) {
            binding.icon.setImageResource(transaction.getServiceTypeIcon());
            binding.name.setText(transaction.getServiceTypeName());

            binding.operation.setText(transaction.getOperationSymbol());
            binding.operation.setTextColor(ContextCompat.getColor(binding.getRoot().getContext(), transaction.getOperationColor()));

            binding.amount.setText(transaction.getAmount());
            binding.amount.setTextColor(ContextCompat.getColor(binding.getRoot().getContext(), transaction.getOperationColor()));

            binding.dateTime.setText(transaction.getTimestamp());
            binding.recipientOrSender.setText(transaction.getRecipientOrSenderName());

            binding.status.setText(transaction.getStatus());
            binding.status.setTextColor(ContextCompat.getColor(binding.getRoot().getContext(), transaction.getStatusColor()));
            binding.status.setBackgroundResource(transaction.getStatusBackgroundColor());

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onClick(transaction);
                }
            });
        }
    }

    public interface OnTransactionClickListener {
        void onClick(TransactionItemUiModel uiModel);
    }
}